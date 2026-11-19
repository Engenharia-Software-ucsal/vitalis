package br.ucsal.vitalis.clinico.paciente;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteRepository repository;

    public PacienteController(PacienteRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Paciente> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "paciente nao encontrado"));
    }

    @PostMapping
    public ResponseEntity<Paciente> cadastrar(@Valid @RequestBody NovoPacienteRequest req) {
        if (repository.existsByCpf(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF ja cadastrado");
        }
        Paciente salvo = repository.save(new Paciente(req.nome(), req.cpf(), req.cns(), req.dataNascimento()));
        return ResponseEntity.created(URI.create("/pacientes/" + salvo.getId())).body(salvo);
    }
}
