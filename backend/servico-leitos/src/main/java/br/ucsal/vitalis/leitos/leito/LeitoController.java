package br.ucsal.vitalis.leitos.leito;

import br.ucsal.vitalis.leitos.alocacao.AlocacaoLeito;
import br.ucsal.vitalis.leitos.alocacao.AlocacaoLeitoRepository;
import br.ucsal.vitalis.leitos.alocacao.StatusAlocacao;
import br.ucsal.vitalis.leitos.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/leitos")
public class LeitoController {

    public record NovoLeitoRequest(@NotBlank String codigo, @NotBlank String ala) {
    }

    public record AlterarStatusRequest(@NotNull StatusLeito status) {
    }

    public record AlocacaoAtualResponse(
            Long id,
            Long atendimentoId,
            StatusAlocacao status,
            Instant alocadoEm,
            Long responsavelId
    ) {
        static AlocacaoAtualResponse from(AlocacaoLeito a) {
            return a == null ? null : new AlocacaoAtualResponse(
                    a.getId(),
                    a.getAtendimentoId(),
                    a.getStatus(),
                    a.getAlocadoEm(),
                    a.getResponsavelId()
            );
        }
    }

    public record LeitoDetalhadoResponse(
            Long id,
            String codigo,
            String ala,
            StatusLeito status,
            AlocacaoAtualResponse alocacaoAtual
    ) {
        static LeitoDetalhadoResponse from(Leito leito, AlocacaoLeito alocacao) {
            return new LeitoDetalhadoResponse(
                    leito.getId(),
                    leito.getCodigo(),
                    leito.getAla(),
                    leito.getStatus(),
                    AlocacaoAtualResponse.from(alocacao)
            );
        }
    }

    private final LeitoRepository repository;
    private final AlocacaoLeitoRepository alocacaoRepository;
    private final HistoricoStatusLeitoRepository historicoRepository;
    private final UsuarioAutenticado usuarioAutenticado;

    public LeitoController(
            LeitoRepository repository,
            AlocacaoLeitoRepository alocacaoRepository,
            HistoricoStatusLeitoRepository historicoRepository,
            UsuarioAutenticado usuarioAutenticado
    ) {
        this.repository = repository;
        this.alocacaoRepository = alocacaoRepository;
        this.historicoRepository = historicoRepository;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    @GetMapping
    public List<LeitoDetalhadoResponse> listar(
            @RequestParam(required = false) StatusLeito status,
            @RequestParam(required = false) String ala
    ) {
        boolean temAla = ala != null && !ala.isBlank();
        List<Leito> leitos;
        if (status != null && temAla) {
            leitos = repository.findByStatusAndAlaIgnoreCase(status, ala.trim());
        } else if (status != null) {
            leitos = repository.findByStatus(status);
        } else if (temAla) {
            leitos = repository.findByAlaIgnoreCase(ala.trim());
        } else {
            leitos = repository.findAll();
        }

        Map<Long, AlocacaoLeito> alocacoesPorLeito = alocacaoRepository.findByLiberadoEmIsNull()
                .stream()
                .collect(Collectors.toMap(AlocacaoLeito::getLeitoId, Function.identity(), (a, b) -> a));

        return leitos.stream()
                .map(leito -> LeitoDetalhadoResponse.from(leito, alocacoesPorLeito.get(leito.getId())))
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR_LEITOS', 'ADMINISTRADOR')")
    @Transactional
    public ResponseEntity<LeitoDetalhadoResponse> cadastrar(@Valid @RequestBody NovoLeitoRequest req) {
        if (repository.existsByCodigo(req.codigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "codigo de leito ja cadastrado");
        }
        Leito salvo = repository.save(new Leito(req.codigo(), req.ala()));
        historicoRepository.save(new HistoricoStatusLeito(
                salvo.getId(),
                null,
                null,
                salvo.getStatus(),
                null,
                usuarioAutenticado.id(),
                Instant.now()
        ));
        return ResponseEntity.created(URI.create("/leitos/" + salvo.getId()))
                .body(LeitoDetalhadoResponse.from(salvo, null));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GESTOR_LEITOS', 'ENFERMEIRO', 'ADMINISTRADOR')")
    @Transactional
    public LeitoDetalhadoResponse alterarStatus(@PathVariable Long id, @Valid @RequestBody AlterarStatusRequest req) {
        Leito leito = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado"));
        StatusLeito statusAnterior = leito.getStatus();
        try {
            leito.alterarStatus(req.status());
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
        }
        repository.save(leito);
        historicoRepository.save(new HistoricoStatusLeito(
                leito.getId(),
                null,
                statusAnterior,
                leito.getStatus(),
                null,
                usuarioAutenticado.id(),
                Instant.now()
        ));
        AlocacaoLeito alocacaoAtual = alocacaoRepository.findByLeitoIdAndLiberadoEmIsNull(leito.getId()).orElse(null);
        return LeitoDetalhadoResponse.from(leito, alocacaoAtual);
    }

    @PatchMapping("/{id}/higienizar")
    @PreAuthorize("hasAnyRole('GESTOR_LEITOS', 'ADMINISTRADOR')")
    @Transactional
    public LeitoDetalhadoResponse higienizar(@PathVariable Long id) {
        Leito leito = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado"));
        if (leito.getStatus() != StatusLeito.EM_HIGIENIZACAO) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "apenas leitos em higienizacao podem ser liberados para uso"
            );
        }
        leito.alterarStatus(StatusLeito.LIVRE);
        repository.save(leito);
        historicoRepository.save(new HistoricoStatusLeito(
                leito.getId(),
                null,
                StatusLeito.EM_HIGIENIZACAO,
                StatusLeito.LIVRE,
                null,
                usuarioAutenticado.id(),
                Instant.now()
        ));
        return LeitoDetalhadoResponse.from(leito, null);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoStatusLeito> historico(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado");
        }
        return historicoRepository.findByLeitoIdOrderByAlteradoEmDesc(id);
    }
}
