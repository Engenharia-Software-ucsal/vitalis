package br.ucsal.vitalis.clinico.triagem;

import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.atendimento.AtendimentoRepository;
import br.ucsal.vitalis.clinico.auditoria.AcaoAuditoria;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class TriagemService {

    private final TriagemRepository triagemRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AuditoriaService auditoriaService;

    public TriagemService(TriagemRepository triagemRepository,
                          AtendimentoRepository atendimentoRepository,
                          UsuarioAutenticado usuarioAutenticado,
                          AuditoriaService auditoriaService) {
        this.triagemRepository = triagemRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public TriagemResponse registrar(Long atendimentoId, NovaTriagemRequest req) {
        Atendimento atendimento = atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento nao encontrado"));

        if (atendimento.getStatus().isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Nao e possivel registrar triagem em atendimento encerrado");
        }

        int qsofa = CalculadoraEscoresClinicos.calcularQsofa(
                req.frequenciaRespiratoria(),
                req.pressaoSistolica(),
                req.escalaGlasgow()
        );
        CalculadoraEscoresClinicos.ResultadoNews2 news2 = CalculadoraEscoresClinicos.calcularNews2(
                req.frequenciaRespiratoria(),
                req.saturacaoO2(),
                req.oxigenioSuplementar(),
                req.pressaoSistolica(),
                req.frequenciaCardiaca(),
                req.escalaGlasgow(),
                req.temperaturaC()
        );
        boolean alertaSepse = CalculadoraEscoresClinicos.isAlertaSepse(
                qsofa, news2.escore(), news2.parametroCriticoIndividual()
        );
        PrioridadeManchester calculada = CalculadoraManchester.calcular(
                req.saturacaoO2(),
                req.pressaoSistolica(),
                req.frequenciaRespiratoria(),
                req.frequenciaCardiaca(),
                req.escalaGlasgow(),
                req.temperaturaC(),
                req.escalaDor(),
                qsofa,
                news2.escore()
        );

        PrioridadeManchester prioridadeFinal = req.prioridadeManual() != null ? req.prioridadeManual() : calculada;

        Triagem triagem = new Triagem(
                atendimentoId,
                usuarioAutenticado.id(),
                prioridadeFinal,
                req.queixaPrincipal().trim(),
                req.temperaturaC(),
                req.frequenciaCardiaca(),
                req.frequenciaRespiratoria(),
                req.pressaoSistolica(),
                req.pressaoDiastolica(),
                req.saturacaoO2(),
                req.escalaDor(),
                req.escalaGlasgow() == null ? null : req.escalaGlasgow().shortValue(),
                req.oxigenioSuplementar(),
                (short) qsofa,
                (short) news2.escore(),
                alertaSepse,
                req.observacoes()
        );

        Triagem salva = triagemRepository.save(triagem);
        auditoriaService.registrar(AcaoAuditoria.CRIACAO, "triagem", salva.getId(), Map.of(
                "atendimentoId", atendimentoId,
                "prioridade", prioridadeFinal.name(),
                "qsofa", qsofa,
                "news2", news2.escore(),
                "alertaSepse", alertaSepse
        ));
        return TriagemResponse.de(salva);
    }

    public List<TriagemResponse> listarPorAtendimento(Long atendimentoId) {
        if (!atendimentoRepository.existsById(atendimentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento nao encontrado");
        }
        return triagemRepository.findByAtendimentoIdOrderByRealizadaEmDesc(atendimentoId)
                .stream()
                .map(TriagemResponse::de)
                .toList();
    }
}
