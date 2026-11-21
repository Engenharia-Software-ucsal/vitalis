package br.ucsal.vitalis.clinico.prontuario;

import br.ucsal.vitalis.clinico.alergia.Alergia;
import br.ucsal.vitalis.clinico.alergia.AlergiaRepository;
import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.atendimento.AtendimentoRepository;
import br.ucsal.vitalis.clinico.auditoria.AcaoAuditoria;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.auth.Usuario;
import br.ucsal.vitalis.clinico.auth.UsuarioRepository;
import br.ucsal.vitalis.clinico.paciente.Paciente;
import br.ucsal.vitalis.clinico.paciente.PacienteRepository;
import br.ucsal.vitalis.clinico.prescricao.PrescricaoResponse;
import br.ucsal.vitalis.clinico.prescricao.PrescricaoService;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import br.ucsal.vitalis.clinico.triagem.TriagemRepository;
import br.ucsal.vitalis.clinico.triagem.TriagemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProntuarioService {

    private final ProntuarioRepository prontuarioRepository;
    private final EvolucaoClinicaRepository evolucaoRepository;
    private final PacienteRepository pacienteRepository;
    private final AlergiaRepository alergiaRepository;
    private final AtendimentoRepository atendimentoRepository;
    private final TriagemRepository triagemRepository;
    private final PrescricaoService prescricaoService;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioAutenticado usuarioAutenticado;
    private final AuditoriaService auditoriaService;

    public ProntuarioService(ProntuarioRepository prontuarioRepository,
                             EvolucaoClinicaRepository evolucaoRepository,
                             PacienteRepository pacienteRepository,
                             AlergiaRepository alergiaRepository,
                             AtendimentoRepository atendimentoRepository,
                             TriagemRepository triagemRepository,
                             PrescricaoService prescricaoService,
                             UsuarioRepository usuarioRepository,
                             UsuarioAutenticado usuarioAutenticado,
                             AuditoriaService auditoriaService) {
        this.prontuarioRepository = prontuarioRepository;
        this.evolucaoRepository = evolucaoRepository;
        this.pacienteRepository = pacienteRepository;
        this.alergiaRepository = alergiaRepository;
        this.atendimentoRepository = atendimentoRepository;
        this.triagemRepository = triagemRepository;
        this.prescricaoService = prescricaoService;
        this.usuarioRepository = usuarioRepository;
        this.usuarioAutenticado = usuarioAutenticado;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public ProntuarioCompletoResponse obterCompleto(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado"));

        Prontuario prontuario = prontuarioRepository.findByPacienteId(pacienteId)
                .orElseGet(() -> prontuarioRepository.save(new Prontuario(pacienteId)));

        List<Alergia> alergias = alergiaRepository.findByPacienteIdOrderByAtivaDescRegistradaEmDesc(pacienteId);
        List<Atendimento> atendimentos = atendimentoRepository.findByPacienteIdOrderByIniciadoEmDesc(pacienteId);
        List<Long> atendimentoIds = atendimentos.stream().map(Atendimento::getId).toList();

        List<TriagemResponse> triagens = atendimentoIds.isEmpty()
                ? List.of()
                : triagemRepository.findByAtendimentoIdInOrderByRealizadaEmDesc(atendimentoIds).stream()
                        .map(TriagemResponse::de)
                        .toList();

        List<EvolucaoClinica> evolucoesEntidades = evolucaoRepository
                .findByProntuarioIdOrderByCriadaEmDesc(prontuario.getId());
        List<Long> autoresIds = evolucoesEntidades.stream()
                .map(EvolucaoClinica::getProfissionalId)
                .distinct()
                .toList();
        Map<Long, String> nomesAutores = autoresIds.isEmpty()
                ? Map.of()
                : usuarioRepository.findAllById(autoresIds).stream()
                        .collect(Collectors.toMap(Usuario::getId, Usuario::getNome));

        List<EvolucaoResponse> evolucoes = evolucoesEntidades.stream()
                .map(e -> new EvolucaoResponse(
                        e.getId(),
                        e.getProntuarioId(),
                        e.getAtendimentoId(),
                        e.getProfissionalId(),
                        nomesAutores.getOrDefault(e.getProfissionalId(), "Profissional #" + e.getProfissionalId()),
                        e.getTexto(),
                        e.getCriadaEm()
                ))
                .toList();

        List<PrescricaoResponse> prescricoes = prescricaoService.listarPorAtendimentos(atendimentoIds);

        auditoriaService.registrar(AcaoAuditoria.LEITURA, "prontuario", prontuario.getId(), Map.of(
                "pacienteId", pacienteId
        ));

        return new ProntuarioCompletoResponse(
                prontuario.getId(),
                paciente,
                alergias,
                atendimentos,
                triagens,
                evolucoes,
                prescricoes
        );
    }

    @Transactional
    public EvolucaoResponse registrarEvolucao(Long pacienteId, NovaEvolucaoRequest req) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente nao encontrado");
        }

        Atendimento atendimento = atendimentoRepository.findById(req.atendimentoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atendimento nao encontrado"));

        if (!atendimento.getPacienteId().equals(pacienteId)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Atendimento nao pertence ao paciente informado");
        }

        if (atendimento.getStatus().isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "RN-12: Prontuario encerrado (ALTA/CANCELADO) nao permite novas evolucoes");
        }

        Prontuario prontuario = prontuarioRepository.findByPacienteId(pacienteId)
                .orElseGet(() -> prontuarioRepository.save(new Prontuario(pacienteId)));

        Long autorId = usuarioAutenticado.id();
        EvolucaoClinica salva = evolucaoRepository.save(
                new EvolucaoClinica(prontuario.getId(), atendimento.getId(), autorId, req.texto().trim())
        );

        auditoriaService.registrar(AcaoAuditoria.CRIACAO, "evolucao_clinica", salva.getId(), Map.of(
                "prontuarioId", prontuario.getId(),
                "atendimentoId", atendimento.getId(),
                "pacienteId", pacienteId
        ));

        String autorNome = usuarioRepository.findById(autorId)
                .map(Usuario::getNome)
                .orElse("Profissional #" + autorId);

        return new EvolucaoResponse(
                salva.getId(),
                salva.getProntuarioId(),
                salva.getAtendimentoId(),
                salva.getProfissionalId(),
                autorNome,
                salva.getTexto(),
                salva.getCriadaEm()
        );
    }
}
