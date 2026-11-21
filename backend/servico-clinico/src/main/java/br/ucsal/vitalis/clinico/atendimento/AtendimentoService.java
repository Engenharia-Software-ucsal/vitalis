package br.ucsal.vitalis.clinico.atendimento;

import br.ucsal.vitalis.clinico.auditoria.AcaoAuditoria;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.paciente.Paciente;
import br.ucsal.vitalis.clinico.paciente.PacienteRepository;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import br.ucsal.vitalis.clinico.triagem.PrioridadeManchester;
import br.ucsal.vitalis.clinico.triagem.Triagem;
import br.ucsal.vitalis.clinico.triagem.TriagemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AtendimentoService {

    private static final List<StatusAtendimento> STATUS_ATIVOS = List.of(
            StatusAtendimento.ABERTO, StatusAtendimento.EM_ANDAMENTO
    );

    private final AtendimentoRepository atendimentoRepository;
    private final PacienteRepository pacienteRepository;
    private final TriagemRepository triagemRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AuditoriaService auditoriaService;
    private final RestClient leitosRestClient;

    public AtendimentoService(AtendimentoRepository atendimentoRepository,
                              PacienteRepository pacienteRepository,
                              TriagemRepository triagemRepository,
                              UsuarioAutenticado usuarioAutenticado,
                              AuditoriaService auditoriaService,
                              RestClient.Builder restClientBuilder,
                              @Value("${vitalis.leitos-url:http://localhost:8082}") String leitosUrl) {
        this.atendimentoRepository = atendimentoRepository;
        this.pacienteRepository = pacienteRepository;
        this.triagemRepository = triagemRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.auditoriaService = auditoriaService;
        this.leitosRestClient = restClientBuilder.baseUrl(leitosUrl).build();
    }

    @Transactional
    public Atendimento abrir(NovoAtendimentoRequest req) {
        if (!pacienteRepository.existsById(req.pacienteId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado");
        }
        Atendimento atendimento = new Atendimento(req.pacienteId(), usuarioAutenticado.id(), req.tipo());
        Atendimento salvo = atendimentoRepository.save(atendimento);
        auditoriaService.registrar(AcaoAuditoria.CRIACAO, "atendimento", salvo.getId(), Map.of(
                "pacienteId", req.pacienteId(),
                "tipo", req.tipo().name()
        ));
        return salvo;
    }

    public Atendimento buscar(Long id) {
        return atendimentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento nao encontrado"));
    }

    public List<ItemFilaResponse> filaPrioritaria() {
        List<Atendimento> ativos = atendimentoRepository.findByStatusIn(STATUS_ATIVOS);
        if (ativos.isEmpty()) {
            return List.of();
        }

        List<Long> pacienteIds = ativos.stream().map(Atendimento::getPacienteId).distinct().toList();
        Map<Long, Paciente> pacientes = pacienteRepository.findAllById(pacienteIds).stream()
                .collect(Collectors.toMap(Paciente::getId, Function.identity()));

        List<Long> atendimentoIds = ativos.stream().map(Atendimento::getId).toList();
        Map<Long, Triagem> ultimaTriagemPorAtendimento = new HashMap<>();
        for (Triagem t : triagemRepository.findByAtendimentoIdInOrderByRealizadaEmDesc(atendimentoIds)) {
            ultimaTriagemPorAtendimento.putIfAbsent(t.getAtendimentoId(), t);
        }

        return ativos.stream()
                .map(a -> {
                    Paciente p = pacientes.get(a.getPacienteId());
                    Triagem t = ultimaTriagemPorAtendimento.get(a.getId());
                    return new ItemFilaResponse(
                            a.getId(),
                            a.getPacienteId(),
                            p != null ? p.getNome() : "Paciente #" + a.getPacienteId(),
                            p != null ? p.getCpf() : null,
                            a.getTipo(),
                            a.getStatus(),
                            a.getIniciadoEm(),
                            t != null ? t.getPrioridade() : null,
                            t != null ? t.getEscoreQsofa() : null,
                            t != null ? t.getEscoreNews2() : null,
                            t != null && t.isAlertaSepse(),
                            t != null,
                            t != null ? t.getQueixaPrincipal() : null
                    );
                })
                .sorted(Comparator
                        .comparingInt((ItemFilaResponse item) -> ordemPrioridade(item.prioridade()))
                        .thenComparing(ItemFilaResponse::iniciadoEm))
                .toList();
    }

    @Transactional
    public Atendimento assumir(Long id) {
        Atendimento atendimento = buscar(id);
        if (atendimento.getStatus().isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Atendimento encerrado nao pode ser assumido");
        }
        atendimento.assumir(usuarioAutenticado.id());
        return atendimentoRepository.save(atendimento);
    }

    public ElegibilidadeInternacaoResponse verificarElegibilidadeInternacao(Long id) {
        Atendimento atendimento = buscar(id);
        boolean possuiTriagem = triagemRepository.existsByAtendimentoId(id);

        if (atendimento.getStatus().isEncerrado()) {
            return new ElegibilidadeInternacaoResponse(
                    id, false, possuiTriagem, atendimento.getStatus(),
                    "Atendimento ja encerrado"
            );
        }
        if (!possuiTriagem) {
            return new ElegibilidadeInternacaoResponse(
                    id, false, false, atendimento.getStatus(),
                    "RN-004: internacao exige triagem previa registrada"
            );
        }
        return new ElegibilidadeInternacaoResponse(
                id, true, true, atendimento.getStatus(),
                "Atendimento elegivel para internacao"
        );
    }

    @Transactional
    public Atendimento concederAlta(Long id, AltaRequest req) {
        Atendimento atendimento = buscar(id);
        if (atendimento.getStatus().isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Atendimento ja se encontra encerrado");
        }

        liberarLeitoSeAlocado(id);

        atendimento.concederAlta(req.motivoEncerramento().trim());
        Atendimento salvo = atendimentoRepository.save(atendimento);

        auditoriaService.registrar(AcaoAuditoria.ALTERACAO, "atendimento", salvo.getId(), Map.of(
                "evento", "ALTA",
                "motivoEncerramento", salvo.getMotivoEncerramento()
        ));
        return salvo;
    }

    private void liberarLeitoSeAlocado(Long atendimentoId) {
        try {
            String token = usuarioAutenticado.token();
            leitosRestClient.post()
                    .uri("/alocacoes/atendimento/{id}/liberar", atendimentoId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Falha ao liberar leito no servico-leitos: " + e.getMessage(),
                    e
            );
        }
    }

    private static int ordemPrioridade(PrioridadeManchester prioridade) {
        return prioridade == null ? Integer.MAX_VALUE : prioridade.ordemGravidade();
    }
}
