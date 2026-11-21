package br.ucsal.vitalis.clinico.paciente;

import br.ucsal.vitalis.clinico.prontuario.Prontuario;
import br.ucsal.vitalis.clinico.prontuario.ProntuarioRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository repository;
    private final ProntuarioRepository prontuarioRepository;
    private final ContatoEmergenciaRepository contatoRepository;

    public PacienteController(PacienteRepository repository,
                              ProntuarioRepository prontuarioRepository,
                              ContatoEmergenciaRepository contatoRepository) {
        this.repository = repository;
        this.prontuarioRepository = prontuarioRepository;
        this.contatoRepository = contatoRepository;
    }

    @GetMapping
    public List<Paciente> listar(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return repository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
        }
        String termo = q.trim();
        return repository.findByNomeContainingIgnoreCaseOrCpfContainingOrderByNomeAsc(termo, termo);
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paciente nao encontrado"));
    }

    @PostMapping
    @Transactional
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<Paciente> cadastrar(@Valid @RequestBody NovoPacienteRequest req) {
        if (!ValidadorCpf.isCpfValido(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF invalido");
        }
        if (repository.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF ja cadastrado");
        }
        String cns = (req.cns() != null && req.cns().isBlank()) ? null : req.cns();
        Paciente salvo = repository.save(new Paciente(req.nome().trim(), req.cpf(), cns, req.dataNascimento()));
        prontuarioRepository.save(new Prontuario(salvo.getId()));
        return ResponseEntity.created(URI.create("/pacientes/" + salvo.getId())).body(salvo);
    }

    @GetMapping("/{id}/contatos-emergencia")
    public List<ContatoEmergencia> listarContatos(@PathVariable Long id) {
        garantirPacienteExiste(id);
        return contatoRepository.findByPacienteIdOrderByPrincipalDescIdAsc(id);
    }

    @PostMapping("/{id}/contatos-emergencia")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<ContatoEmergencia> adicionarContato(@PathVariable Long id,
                                                              @Valid @RequestBody NovoContatoEmergenciaRequest req) {
        garantirPacienteExiste(id);
        ContatoEmergencia salvo = contatoRepository.save(new ContatoEmergencia(
                id,
                req.nome().trim(),
                req.parentesco(),
                req.telefone().trim(),
                req.principal()
        ));
        return ResponseEntity.created(URI.create("/pacientes/" + id + "/contatos-emergencia/" + salvo.getId()))
                .body(salvo);
    }

    @DeleteMapping("/{id}/contatos-emergencia/{contatoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO', 'ADMINISTRADOR')")
    public void removerContato(@PathVariable Long id, @PathVariable Long contatoId) {
        ContatoEmergencia contato = contatoRepository.findByIdAndPacienteId(contatoId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "contato de emergencia nao encontrado"));
        contatoRepository.delete(contato);
    }

    private void garantirPacienteExiste(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "paciente nao encontrado");
        }
    }
}
