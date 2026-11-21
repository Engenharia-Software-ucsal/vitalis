package br.ucsal.vitalis.clinico.prontuario;

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

@RestController
@RequestMapping("/pacientes/{pacienteId}/prontuario")
public class ProntuarioController {

    private final ProntuarioService prontuarioService;

    public ProntuarioController(ProntuarioService prontuarioService) {
        this.prontuarioService = prontuarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMINISTRADOR')")
    public ProntuarioCompletoResponse obter(@PathVariable Long pacienteId) {
        return prontuarioService.obterCompleto(pacienteId);
    }

    @PostMapping("/evolucoes")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMINISTRADOR')")
    public ResponseEntity<EvolucaoResponse> registrarEvolucao(@PathVariable Long pacienteId,
                                                              @Valid @RequestBody NovaEvolucaoRequest req) {
        EvolucaoResponse salva = prontuarioService.registrarEvolucao(pacienteId, req);
        return ResponseEntity
                .created(URI.create("/pacientes/" + pacienteId + "/prontuario/evolucoes/" + salva.id()))
                .body(salva);
    }
}
