package br.ucsal.vitalis.clinico.prescricao;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/atendimentos/{atendimentoId}/prescricoes")
public class PrescricaoController {

    private final PrescricaoService prescricaoService;

    public PrescricaoController(PrescricaoService prescricaoService) {
        this.prescricaoService = prescricaoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MEDICO','ADMINISTRADOR')")
    public ResponseEntity<PrescricaoResponse> criar(@PathVariable Long atendimentoId,
                                                    @Valid @RequestBody NovaPrescricaoRequest req) {
        PrescricaoResponse criada = prescricaoService.criar(atendimentoId, req);
        return ResponseEntity
                .created(URI.create("/atendimentos/" + atendimentoId + "/prescricoes/" + criada.id()))
                .body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MEDICO','ENFERMEIRO','FARMACEUTICO','ADMINISTRADOR')")
    public List<PrescricaoResponse> listar(@PathVariable Long atendimentoId) {
        return prescricaoService.listarPorAtendimento(atendimentoId);
    }

    @ExceptionHandler(AlergiaNaoReconhecidaException.class)
    public ResponseEntity<Map<String, Object>> handleAlergiaNaoReconhecida(AlergiaNaoReconhecidaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of(
                "message", ex.getMessage(),
                "alergiasAtivas", ex.getAlergiasAtivas()
        ));
    }
}
