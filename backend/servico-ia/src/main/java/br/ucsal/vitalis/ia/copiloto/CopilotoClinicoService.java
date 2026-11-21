package br.ucsal.vitalis.ia.copiloto;

import br.ucsal.vitalis.ia.auditoria.DecisaoHumanaIa;
import br.ucsal.vitalis.ia.auditoria.DecisaoHumanaIaRepository;
import br.ucsal.vitalis.ia.auditoria.DecisaoMedica;
import br.ucsal.vitalis.ia.auditoria.LogInferenciaIa;
import br.ucsal.vitalis.ia.auditoria.LogInferenciaIaRepository;
import br.ucsal.vitalis.ia.auditoria.ModuloIa;
import br.ucsal.vitalis.ia.deterministico.CalculadoraCockcroftGault;
import br.ucsal.vitalis.ia.deterministico.InteracaoMedicamentosaLocal;
import br.ucsal.vitalis.ia.deterministico.InteracaoMedicamentosaRepository;
import br.ucsal.vitalis.ia.privacidade.SanitizadorPiiService;
import br.ucsal.vitalis.ia.provider.AiProperties;
import br.ucsal.vitalis.ia.provider.LlmProvider;
import br.ucsal.vitalis.ia.provider.LlmRequest;
import br.ucsal.vitalis.ia.provider.LlmResponse;
import br.ucsal.vitalis.ia.rag.KnowledgeRetriever;
import br.ucsal.vitalis.ia.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class CopilotoClinicoService {

    private static final String SYSTEM_PROMPT_PRESCRICAO = """
            Voce e o Copiloto Farmacoterapeutico do Sistema Vitalis. Sua funcao e exclusivamente consultiva de suporte a prescricao medica (HITL).
            REGRAS OBRIGATORIAS:
            1. Responda apenas com base nos trechos documentais fornecidos (grounding estrito).
            2. Nunca deduza condutas ou invente dados ausentes.
            3. Cite expressamente a fonte oficial para qualquer alerta identificado.
            4. Responda em portugues de forma objetiva e clinica (maximo 3 paragrafos).
            """;

    private static final String SYSTEM_PROMPT_SUMARIO_ALTA = """
            Voce e o assistente de documentacao clinica do Sistema Vitalis.
            Elabore uma minuta concisa e estruturada de sumario de alta hospitalar em portugues (Motivo da Internacao, Evolucao Resumida, Conduta/Orientacoes de Alta) usando exclusivamente os dados clinicos anonimizados fornecidos.
            Esta minuta sera obrigatoriamente revisada e assinada pelo medico assistente.
            """;

    private final LlmProvider llmProvider;
    private final CircuitBreaker circuitBreaker;
    private final AiProperties aiProperties;
    private final SanitizadorPiiService sanitizadorPii;
    private final KnowledgeRetriever knowledgeRetriever;
    private final InteracaoMedicamentosaRepository interacaoRepository;
    private final LogInferenciaIaRepository logRepository;
    private final DecisaoHumanaIaRepository decisaoRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final ObjectMapper objectMapper;

    public CopilotoClinicoService(LlmProvider llmProvider,
                                  CircuitBreaker circuitBreaker,
                                  AiProperties aiProperties,
                                  SanitizadorPiiService sanitizadorPii,
                                  KnowledgeRetriever knowledgeRetriever,
                                  InteracaoMedicamentosaRepository interacaoRepository,
                                  LogInferenciaIaRepository logRepository,
                                  DecisaoHumanaIaRepository decisaoRepository,
                                  UsuarioAutenticado usuarioAutenticado,
                                  ObjectMapper objectMapper) {
        this.llmProvider = llmProvider;
        this.circuitBreaker = circuitBreaker;
        this.aiProperties = aiProperties;
        this.sanitizadorPii = sanitizadorPii;
        this.knowledgeRetriever = knowledgeRetriever;
        this.interacaoRepository = interacaoRepository;
        this.logRepository = logRepository;
        this.decisaoRepository = decisaoRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Map<String, Object> analisarPrescricao(AnalisePrescricaoRequest req) {
        List<String> alergias = req.alergiasAtivas() == null ? List.of() : req.alergiasAtivas();
        List<String> vigentes = req.prescricoesVigentes() == null ? List.of() : req.prescricoesVigentes();
        List<String> medicamentosSolicitados = req.itensSolicitados().stream()
                .map(AnalisePrescricaoRequest.ItemSolicitado::medicamento)
                .filter(m -> m != null && !m.isBlank())
                .toList();

        // 1. Camada deterministica (sempre executada, independe de IA)
        BigDecimal clcr = CalculadoraCockcroftGault.calcular(
                req.idadeAnos(), req.pesoKg(), req.creatininaSericaMgDl(), req.sexo()
        );
        boolean exigeAjusteRenal = CalculadoraCockcroftGault.exigeAjusteRenal(clcr);
        List<Map<String, Object>> alertasDeterministicos = detectarAlertasDeterministicos(
                medicamentosSolicitados, vigentes, alergias, clcr, exigeAjusteRenal
        );

        // 2. Recuperacao RAG
        List<String> termosRag = new ArrayList<>(medicamentosSolicitados);
        termosRag.addAll(vigentes);
        List<KnowledgeRetriever.TrechoClinico> trechos = knowledgeRetriever.buscar(termosRag, 5);

        // 3. Sanitizacao PII (RF-018 / RN-3)
        String tokenSessao = sanitizadorPii.gerarTokenSessao(req.atendimentoId());
        Map<String, Object> payloadSanitizado = new LinkedHashMap<>();
        payloadSanitizado.put("sessao_pseudonimizada", tokenSessao);
        payloadSanitizado.put("idade_anos", req.idadeAnos());
        payloadSanitizado.put("sexo", req.sexo());
        payloadSanitizado.put("clearance_creatinina_ml_min", clcr);
        payloadSanitizado.put("alergias_ativas", alergias.stream()
                .map(a -> sanitizadorPii.sanitizarTexto(a, req.nomePaciente())).toList());
        payloadSanitizado.put("prescricoes_vigentes", vigentes.stream()
                .map(v -> sanitizadorPii.sanitizarTexto(v, req.nomePaciente())).toList());
        payloadSanitizado.put("prescricao_solicitada", req.itensSolicitados());
        payloadSanitizado.put("grounding_context", trechos);

        String payloadJson = toJson(payloadSanitizado);
        String promptHash = sanitizadorPii.hashSha256(payloadJson);

        // 4. Chamada ao LlmProvider protegida por Circuit Breaker (RN-4)
        boolean contingencia = false;
        String parecerClinico;
        String provedorUsado = llmProvider.providerName();
        String modeloUsado = llmProvider.modelName();
        int tokensPrompt = 0;
        int tokensResp = 0;
        int latenciaMs = 0;

        long inicio = System.nanoTime();
        try {
            LlmResponse llmResp = circuitBreaker.executeSupplier(() -> llmProvider.completar(
                    new LlmRequest(
                            SYSTEM_PROMPT_PRESCRICAO,
                            payloadJson,
                            aiProperties.temperaturaSegura(),
                            aiProperties.topP(),
                            512
                    )
            ));
            parecerClinico = llmResp.content();
            provedorUsado = llmResp.providerName();
            modeloUsado = llmResp.modelVersion();
            tokensPrompt = llmResp.promptTokens();
            tokensResp = llmResp.completionTokens();
            latenciaMs = llmResp.latencyMs();
        } catch (Exception ex) {
            contingencia = true;
            provedorUsado = "motor-deterministico-local";
            modeloUsado = "vitalis-regras-v1";
            latenciaMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);
            parecerClinico = montarParecerFallback(alertasDeterministicos, trechos);
        }

        String nivelAlerta = calcularNivelAlerta(alertasDeterministicos);

        Map<String, Object> respostaEstruturada = new LinkedHashMap<>();
        respostaEstruturada.put("nivelAlerta", nivelAlerta);
        respostaEstruturada.put("clearanceCreatininaMlMin", clcr);
        respostaEstruturada.put("exigeAjusteRenal", exigeAjusteRenal);
        respostaEstruturada.put("alertasDeterministicos", alertasDeterministicos);
        respostaEstruturada.put("parecerConsultivo", parecerClinico);
        respostaEstruturada.put("referenciasRag", trechos);
        respostaEstruturada.put("modoContingenciaAtivo", contingencia);
        respostaEstruturada.put("mensagemContingencia", contingencia
                ? "Modo de Contingencia Deterministica Ativo: Validacao executada por regras locais de seguranca."
                : null);

        LogInferenciaIa log = logRepository.save(new LogInferenciaIa(
                req.atendimentoId(),
                usuarioAutenticado.id(),
                ModuloIa.COPILOTO_PRESCRICAO,
                provedorUsado,
                modeloUsado,
                promptHash,
                tokensPrompt,
                tokensResp,
                latenciaMs,
                contingencia ? new BigDecimal("1.000") : new BigDecimal("0.850"),
                contingencia,
                respostaEstruturada
        ));

        Map<String, Object> out = new LinkedHashMap<>(respostaEstruturada);
        out.put("inferenciaId", log.getId());
        out.put("provedor", provedorUsado);
        out.put("modelo", modeloUsado);
        out.put("latenciaMs", latenciaMs);
        out.put("circuitBreakerEstado", circuitBreaker.getState().name());
        return out;
    }

    @Transactional
    public Map<String, Object> gerarSumarioAlta(SumarioAltaRequest req) {
        String queixa = sanitizadorPii.sanitizarTexto(req.queixaPrincipal(), req.nomePaciente());
        List<String> evolucoes = req.evolucoes() == null ? List.of() : req.evolucoes().stream()
                .map(e -> sanitizadorPii.sanitizarTexto(e, req.nomePaciente())).toList();
        List<String> prescricoes = req.prescricoes() == null ? List.of() : req.prescricoes().stream()
                .map(p -> sanitizadorPii.sanitizarTexto(p, req.nomePaciente())).toList();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessao_pseudonimizada", sanitizadorPii.gerarTokenSessao(req.atendimentoId()));
        payload.put("idade_anos", req.idadeAnos());
        payload.put("queixa_principal", queixa);
        payload.put("evolucoes", evolucoes);
        payload.put("prescricoes", prescricoes);

        String payloadJson = toJson(payload);
        String promptHash = sanitizadorPii.hashSha256(payloadJson);

        boolean contingencia = false;
        String minuta;
        String provedorUsado = llmProvider.providerName();
        String modeloUsado = llmProvider.modelName();
        int tokensPrompt = 0;
        int tokensResp = 0;
        int latenciaMs = 0;

        long inicio = System.nanoTime();
        try {
            LlmResponse resp = circuitBreaker.executeSupplier(() -> llmProvider.completar(
                    new LlmRequest(
                            SYSTEM_PROMPT_SUMARIO_ALTA,
                            payloadJson,
                            aiProperties.temperaturaSegura(),
                            aiProperties.topP(),
                            512
                    )
            ));
            minuta = resp.content();
            provedorUsado = resp.providerName();
            modeloUsado = resp.modelVersion();
            tokensPrompt = resp.promptTokens();
            tokensResp = resp.completionTokens();
            latenciaMs = resp.latencyMs();
        } catch (Exception ex) {
            contingencia = true;
            provedorUsado = "motor-deterministico-local";
            modeloUsado = "vitalis-template-alta-v1";
            latenciaMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);
            minuta = "SUMARIO DE ALTA (MINUTA DETERMINISTICA - REVISAO MEDICA OBRIGATORIA)\n"
                    + "Queixa principal: " + (queixa.isBlank() ? "Nao informada" : queixa) + "\n"
                    + "Total de evolucoes registradas: " + evolucoes.size() + ".\n"
                    + "Conduta: Paciente com melhora clinica e criterios de alta hospitalar. Manter seguimento ambulatorial.";
        }

        Map<String, Object> resposta = Map.of(
                "minuta", minuta,
                "modoContingenciaAtivo", contingencia
        );

        LogInferenciaIa log = logRepository.save(new LogInferenciaIa(
                req.atendimentoId(),
                usuarioAutenticado.id(),
                ModuloIa.SUMARIO_ALTA,
                provedorUsado,
                modeloUsado,
                promptHash,
                tokensPrompt,
                tokensResp,
                latenciaMs,
                contingencia ? new BigDecimal("1.000") : new BigDecimal("0.850"),
                contingencia,
                resposta
        ));

        return Map.of(
                "inferenciaId", log.getId(),
                "minuta", minuta,
                "modoContingenciaAtivo", contingencia,
                "provedor", provedorUsado,
                "modelo", modeloUsado,
                "latenciaMs", latenciaMs
        );
    }

    @Transactional
    public DecisaoHumanaIa registrarDecisaoHitl(Long inferenciaId, DecisaoHitlRequest req) {
        if (!logRepository.existsById(inferenciaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inferencia de IA nao encontrada");
        }
        if (decisaoRepository.existsByLogInferenciaId(inferenciaId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Decisao medica ja registrada para esta inferencia");
        }
        String justificativa = req.justificativaHumana() == null ? null : req.justificativaHumana().trim();
        if ((req.decisaoMedica() == DecisaoMedica.MODIFICADA || req.decisaoMedica() == DecisaoMedica.REJEITADA)
                && (justificativa == null || justificativa.length() < 10)) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "RN-2: Decisoes MODIFICADA ou REJEITADA exigem justificativa clinica com pelo menos 10 caracteres"
            );
        }

        return decisaoRepository.save(new DecisaoHumanaIa(
                inferenciaId,
                usuarioAutenticado.id(),
                req.decisaoMedica(),
                justificativa
        ));
    }

    public Map<String, Object> metricasGovernanca() {
        long totalInferencias = logRepository.count();
        long totalContingencia = logRepository.countByModoContingenciaAtivoTrue();
        long aceitas = decisaoRepository.countByDecisaoMedica(DecisaoMedica.ACEITA);
        long modificadas = decisaoRepository.countByDecisaoMedica(DecisaoMedica.MODIFICADA);
        long rejeitadas = decisaoRepository.countByDecisaoMedica(DecisaoMedica.REJEITADA);
        long totalDecisoes = aceitas + modificadas + rejeitadas;

        return Map.of(
                "provedorConfigurado", llmProvider.providerName(),
                "modeloConfigurado", llmProvider.modelName(),
                "circuitBreakerEstado", circuitBreaker.getState().name(),
                "totalInferencias", totalInferencias,
                "acionamentosContingencia", totalContingencia,
                "decisoesHitl", Map.of(
                        "aceitas", aceitas,
                        "modificadas", modificadas,
                        "rejeitadas", rejeitadas,
                        "taxaAceitePercentual", totalDecisoes == 0 ? 0.0 : Math.round((aceitas * 1000.0) / totalDecisoes) / 10.0
                )
        );
    }

    private List<Map<String, Object>> detectarAlertasDeterministicos(List<String> solicitados,
                                                                     List<String> vigentes,
                                                                     List<String> alergias,
                                                                     BigDecimal clcr,
                                                                     boolean exigeAjusteRenal) {
        List<Map<String, Object>> alertas = new ArrayList<>();

        for (String med : solicitados) {
            String medNorm = med.toLowerCase(Locale.ROOT);
            for (String alergia : alergias) {
                String alNorm = alergia.toLowerCase(Locale.ROOT);
                boolean conflitoDireto = medNorm.contains(alNorm) || alNorm.contains(medNorm)
                        || (alNorm.contains("penicil") && (medNorm.contains("amoxicil") || medNorm.contains("ampicil")));
                if (conflitoDireto) {
                    alertas.add(Map.of(
                            "tipo", "ALERGIA_CRITICA",
                            "gravidade", "CRITICO",
                            "mensagem", "Veto por alergia conhecida: '" + med + "' conflita com alergia ativa '" + alergia + "'.",
                            "referencia", "Cadastro de Alergias do Prontuario / RN-008"
                    ));
                }
            }
        }

        List<String> todosFarmacos = new ArrayList<>(solicitados);
        todosFarmacos.addAll(vigentes);
        List<InteracaoMedicamentosaLocal> interacoes = interacaoRepository.findAll();
        for (InteracaoMedicamentosaLocal inter : interacoes) {
            boolean temA = todosFarmacos.stream().anyMatch(f -> f.toLowerCase(Locale.ROOT).contains(inter.getFarmacoA().toLowerCase(Locale.ROOT)));
            boolean temB = todosFarmacos.stream().anyMatch(f -> f.toLowerCase(Locale.ROOT).contains(inter.getFarmacoB().toLowerCase(Locale.ROOT)));
            if (temA && temB) {
                alertas.add(Map.of(
                        "tipo", "INTERACAO_MEDICAMENTOSA",
                        "gravidade", "X".equalsIgnoreCase(inter.getCategoria()) ? "CRITICO" : "ALTO",
                        "categoria", inter.getCategoria(),
                        "mensagem", inter.getFarmacoA() + " + " + inter.getFarmacoB() + ": " + inter.getDescricao(),
                        "referencia", inter.getReferencia()
                ));
            }
        }

        if (exigeAjusteRenal) {
            alertas.add(Map.of(
                    "tipo", "AJUSTE_RENAL",
                    "gravidade", "ALTO",
                    "mensagem", "Clearance de creatinina (Cockcroft-Gault) estimado em " + clcr
                            + " mL/min (< 50 mL/min). Revisar ajuste de dose/intervalo conforme monografia.",
                    "referencia", "Formula de Cockcroft-Gault / Diretriz Renal Vitalis"
            ));
        }

        return alertas;
    }

    private static String calcularNivelAlerta(List<Map<String, Object>> alertas) {
        if (alertas.stream().anyMatch(a -> "CRITICO".equals(a.get("gravidade")))) return "CRITICO";
        if (alertas.stream().anyMatch(a -> "ALTO".equals(a.get("gravidade")))) return "ALTO";
        if (!alertas.isEmpty()) return "MODERADO";
        return "BAIXO";
    }

    private static String montarParecerFallback(List<Map<String, Object>> alertas,
                                                List<KnowledgeRetriever.TrechoClinico> trechos) {
        if (alertas.isEmpty()) {
            return "Motor deterministico local nao identificou interacoes graves (Categoria X/D) ou veto direto por alergia. "
                    + (trechos.isEmpty() ? "" : "Consulte as " + trechos.size() + " referencias oficiais anexadas.");
        }
        return "Foram identificados " + alertas.size()
                + " alerta(s) pelo motor deterministico local. Revise cada item e as referencias oficiais antes de confirmar a prescricao.";
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao serializar payload sanitizado", e);
        }
    }
}
