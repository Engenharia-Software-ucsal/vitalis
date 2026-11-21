package br.ucsal.vitalis.clinico.atendimento;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/atendimentos")
public class AtendimentoController {

    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA', 'ENFERMEIRO', 'MEDICO', 'ADMINISTRADOR')")
    public ResponseEntity<Atendimento> abrir(@Valid @RequestBody NovoAtendimentoRequest req) {
        Atendimento salvo = atendimentoService.abrir(req);
        return ResponseEntity.created(URI.create("/atendimentos/" + salvo.getId())).body(salvo);
    }

    @GetMapping("/fila")
    public List<ItemFilaResponse> fila() {
        return atendimentoService.filaPrioritaria();
    }

    @GetMapping("/{id}")
    public Atendimento buscar(@PathVariable Long id) {
        return atendimentoService.buscar(id);
    }

    @PatchMapping("/{id}/assumir")
    @PreAuthorize("hasAnyRole('MEDICO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public Atendimento assumir(@PathVariable Long id) {
        return atendimentoService.assumir(id);
    }

    @GetMapping("/{id}/elegibilidade-internacao")
    public ElegibilidadeInternacaoResponse elegibilidadeInternacao(@PathVariable Long id) {
        return atendimentoService.verificarElegibilidadeInternacao(id);
    }

    @PostMapping("/{id}/alta")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMINISTRADOR')")
    public Atendimento darAlta(@PathVariable Long id, @Valid @RequestBody AltaRequest req) {
        return atendimentoService.concederAlta(id, req);
    }
}
