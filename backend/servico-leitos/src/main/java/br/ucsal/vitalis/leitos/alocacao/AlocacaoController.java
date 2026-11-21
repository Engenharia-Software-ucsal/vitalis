package br.ucsal.vitalis.leitos.alocacao;

import br.ucsal.vitalis.leitos.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/alocacoes")
public class AlocacaoController {

    public record NovaAlocacaoRequest(
            @NotNull Long leitoId,
            @NotNull Long atendimentoId,
            StatusAlocacao status
    ) {
    }

    public record TransferirAlocacaoRequest(@NotNull Long leitoDestinoId) {
    }

    private final AlocacaoService service;
    private final UsuarioAutenticado usuarioAutenticado;

    public AlocacaoController(AlocacaoService service, UsuarioAutenticado usuarioAutenticado) {
        this.service = service;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR_LEITOS', 'MEDICO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public ResponseEntity<AlocacaoLeito> alocar(@Valid @RequestBody NovaAlocacaoRequest req) {
        AlocacaoLeito alocacao = service.alocar(
                req.leitoId(),
                req.atendimentoId(),
                req.status(),
                usuarioAutenticado.id(),
                usuarioAutenticado.bearerToken()
        );
        return ResponseEntity.created(URI.create("/alocacoes/" + alocacao.getId())).body(alocacao);
    }

    @PostMapping("/{id}/transferir")
    @PreAuthorize("hasAnyRole('GESTOR_LEITOS', 'MEDICO', 'ENFERMEIRO', 'ADMINISTRADOR')")
    public AlocacaoLeito transferir(@PathVariable Long id, @Valid @RequestBody TransferirAlocacaoRequest req) {
        return service.transferir(id, req.leitoDestinoId(), usuarioAutenticado.id());
    }

    @PostMapping("/atendimento/{atendimentoId}/liberar")
    @PreAuthorize("hasAnyRole('MEDICO', 'GESTOR_LEITOS', 'ADMINISTRADOR')")
    public AlocacaoService.LiberacaoResponse liberar(@PathVariable Long atendimentoId) {
        return service.liberarPorAtendimento(atendimentoId, usuarioAutenticado.id());
    }
}
