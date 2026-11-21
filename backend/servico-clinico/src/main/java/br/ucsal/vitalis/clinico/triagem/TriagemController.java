package br.ucsal.vitalis.clinico.triagem;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/atendimentos/{atendimentoId}/triagens")
public class TriagemController {

    private final TriagemService triagemService;

    public TriagemController(TriagemService triagemService) {
        this.triagemService = triagemService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENFERMEIRO', 'MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<TriagemResponse> registrar(@PathVariable Long atendimentoId,
                                                     @Valid @RequestBody NovaTriagemRequest req) {
        TriagemResponse resp = triagemService.registrar(atendimentoId, req);
        return ResponseEntity.created(URI.create("/atendimentos/" + atendimentoId + "/triagens/" + resp.id()))
                .body(resp);
    }

    @GetMapping
    public List<TriagemResponse> listar(@PathVariable Long atendimentoId) {
        return triagemService.listarPorAtendimento(atendimentoId);
    }
}
