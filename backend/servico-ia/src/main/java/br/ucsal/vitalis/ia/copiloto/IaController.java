package br.ucsal.vitalis.ia.copiloto;

import br.ucsal.vitalis.ia.auditoria.DecisaoHumanaIa;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class IaController {

    private final CopilotoClinicoService copilotoService;

    public IaController(CopilotoClinicoService copilotoService) {
        this.copilotoService = copilotoService;
    }

    @PostMapping("/copiloto/prescricao")
    @PreAuthorize("hasAnyRole('MEDICO','ADMINISTRADOR')")
    public Map<String, Object> analisarPrescricao(@Valid @RequestBody AnalisePrescricaoRequest req) {
        return copilotoService.analisarPrescricao(req);
    }

    @PostMapping("/sumario-alta")
    @PreAuthorize("hasAnyRole('MEDICO','ADMINISTRADOR')")
    public Map<String, Object> gerarSumarioAlta(@Valid @RequestBody SumarioAltaRequest req) {
        return copilotoService.gerarSumarioAlta(req);
    }

    @PostMapping("/inferencias/{id}/decisao")
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','ADMINISTRADOR')")
    public ResponseEntity<DecisaoHumanaIa> registrarDecisao(@PathVariable Long id,
                                                            @Valid @RequestBody DecisaoHitlRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(copilotoService.registrarDecisaoHitl(id, req));
    }

    @GetMapping("/governanca/metricas")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','MEDICO')")
    public Map<String, Object> metricas() {
        return copilotoService.metricasGovernanca();
    }
}
