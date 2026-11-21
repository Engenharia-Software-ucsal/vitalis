package br.ucsal.vitalis.clinico.prescricao;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/medicamentos")
public class MedicamentoController {

    private final MedicamentoRepository repository;

    public MedicamentoController(MedicamentoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Medicamento> listar(@RequestParam(name = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            return repository.findByAtivoTrueOrderByNomeAsc();
        }
        String termo = q.trim();
        return repository.findByAtivoTrueAndNomeContainingIgnoreCaseOrAtivoTrueAndPrincipioAtivoContainingIgnoreCaseOrderByNomeAsc(
                termo, termo
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO', 'FARMACEUTICO', 'ADMINISTRADOR')")
    public ResponseEntity<Medicamento> cadastrar(@Valid @RequestBody NovoMedicamentoRequest req) {
        Medicamento salvo = repository.save(new Medicamento(
                req.nome().trim(),
                req.principioAtivo() != null ? req.principioAtivo().trim() : null,
                req.unidade().trim()
        ));
        return ResponseEntity.created(URI.create("/medicamentos/" + salvo.getId())).body(salvo);
    }
}
