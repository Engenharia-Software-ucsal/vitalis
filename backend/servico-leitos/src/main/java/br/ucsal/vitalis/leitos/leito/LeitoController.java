package br.ucsal.vitalis.leitos.leito;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/leitos")
public class LeitoController {

    public record NovoLeitoRequest(@NotBlank String codigo, @NotBlank String ala) {
    }

    public record AlterarStatusRequest(@NotNull StatusLeito status) {
    }

    private final LeitoRepository repository;

    public LeitoController(LeitoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Leito> listar(@RequestParam(required = false) StatusLeito status) {
        return status == null ? repository.findAll() : repository.findByStatus(status);
    }

    @PostMapping
    public ResponseEntity<Leito> cadastrar(@Valid @RequestBody NovoLeitoRequest req) {
        if (repository.existsByCodigo(req.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "codigo de leito ja cadastrado");
        }
        Leito salvo = repository.save(new Leito(req.codigo(), req.ala()));
        return ResponseEntity.created(URI.create("/leitos/" + salvo.getId())).body(salvo);
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public Leito alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest req) {
        Leito leito = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado"));
        try {
            leito.alterarStatus(req.status());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
        return leito;
    }
}
