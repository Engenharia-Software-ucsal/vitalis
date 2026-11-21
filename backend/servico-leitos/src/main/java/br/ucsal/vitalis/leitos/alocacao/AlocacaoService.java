package br.ucsal.vitalis.leitos.alocacao;

import br.ucsal.vitalis.leitos.integracao.ClinicoClient;
import br.ucsal.vitalis.leitos.integracao.ElegibilidadeInternacaoResponse;
import br.ucsal.vitalis.leitos.leito.HistoricoStatusLeito;
import br.ucsal.vitalis.leitos.leito.HistoricoStatusLeitoRepository;
import br.ucsal.vitalis.leitos.leito.Leito;
import br.ucsal.vitalis.leitos.leito.LeitoRepository;
import br.ucsal.vitalis.leitos.leito.StatusLeito;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;

@Service
public class AlocacaoService {

    public record LiberacaoResponse(boolean liberado, Long leitoId) {
    }

    private final LeitoRepository leitoRepository;
    private final AlocacaoLeitoRepository alocacaoRepository;
    private final HistoricoStatusLeitoRepository historicoRepository;
    private final ClinicoClient clinicoClient;

    public AlocacaoService(
            LeitoRepository leitoRepository,
            AlocacaoLeitoRepository alocacaoRepository,
            HistoricoStatusLeitoRepository historicoRepository,
            ClinicoClient clinicoClient
    ) {
        this.leitoRepository = leitoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.historicoRepository = historicoRepository;
        this.clinicoClient = clinicoClient;
    }

    @Transactional
    public AlocacaoLeito alocar(
            Long leitoId,
            Long atendimentoId,
            StatusAlocacao statusSolicitado,
            Long responsavelId,
            String bearerToken
    ) {
        StatusAlocacao statusAlocacao = statusSolicitado != null ? statusSolicitado : StatusAlocacao.OCUPADO;
        if (statusAlocacao != StatusAlocacao.OCUPADO && statusAlocacao != StatusAlocacao.RESERVADO) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "status de alocacao deve ser OCUPADO ou RESERVADO"
            );
        }

        // RN-004: Paciente precisa ter passado por triagem e estar elegivel para internacao.
        ElegibilidadeInternacaoResponse elegibilidade =
                clinicoClient.verificarElegibilidadeInternacao(atendimentoId, bearerToken);
        if (!elegibilidade.elegivel()) {
            String motivo = (elegibilidade.motivo() != null && !elegibilidade.motivo().isBlank())
                    ? elegibilidade.motivo()
                    : "Paciente precisa passar pela triagem antes da alocacao de leito";
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, motivo);
        }

        Leito leito = leitoRepository.findById(leitoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado"));

        // RN-010: Leito precisa estar LIVRE (nunca EM_HIGIENIZACAO ou outro status).
        if (leito.getStatus() == StatusLeito.EM_HIGIENIZACAO) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "leito em higienizacao deve ser liberado antes de ser ocupado"
            );
        }
        if (leito.getStatus() != StatusLeito.LIVRE) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "leito nao esta livre para alocacao"
            );
        }

        if (alocacaoRepository.findByLeitoIdAndLiberadoEmIsNull(leitoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "leito ja possui alocacao aberta");
        }
        if (alocacaoRepository.findByAtendimentoIdAndLiberadoEmIsNull(atendimentoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "atendimento ja possui leito alocado");
        }

        Instant agora = Instant.now();
        AlocacaoLeito alocacao = alocacaoRepository.save(
                new AlocacaoLeito(leitoId, atendimentoId, statusAlocacao, responsavelId, agora)
        );

        StatusLeito statusAnterior = leito.getStatus();
        StatusLeito novoStatusLeito = statusAlocacao == StatusAlocacao.RESERVADO
                ? StatusLeito.RESERVADO
                : StatusLeito.OCUPADO;
        leito.alterarStatus(novoStatusLeito);
        leitoRepository.save(leito);

        historicoRepository.save(new HistoricoStatusLeito(
                leito.getId(),
                alocacao.getId(),
                statusAnterior,
                novoStatusLeito,
                atendimentoId,
                responsavelId,
                agora
        ));

        return alocacao;
    }

    @Transactional
    public AlocacaoLeito transferir(Long alocacaoId, Long leitoDestinoId, Long responsavelId) {
        AlocacaoLeito alocacaoOrigem = alocacaoRepository.findById(alocacaoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "alocacao nao encontrada"));

        if (!alocacaoOrigem.isAberta()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "alocacao ja esta encerrada");
        }

        if (alocacaoOrigem.getLeitoId().equals(leitoDestinoId)) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "leito de destino deve ser diferente do leito de origem"
            );
        }

        Leito leitoOrigem = leitoRepository.findById(alocacaoOrigem.getLeitoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito de origem nao encontrado"));

        Leito leitoDestino = leitoRepository.findById(leitoDestinoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito de destino nao encontrado"));

        if (leitoDestino.getStatus() != StatusLeito.LIVRE
                || alocacaoRepository.findByLeitoIdAndLiberadoEmIsNull(leitoDestinoId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "leito de destino nao esta livre");
        }

        Instant agora = Instant.now();
        alocacaoOrigem.encerrar(agora);
        alocacaoRepository.saveAndFlush(alocacaoOrigem);

        StatusLeito statusAnteriorOrigem = leitoOrigem.getStatus();
        leitoOrigem.alterarStatus(StatusLeito.EM_HIGIENIZACAO);
        leitoRepository.save(leitoOrigem);

        AlocacaoLeito novaAlocacao = alocacaoRepository.save(new AlocacaoLeito(
                leitoDestino.getId(),
                alocacaoOrigem.getAtendimentoId(),
                StatusAlocacao.OCUPADO,
                responsavelId,
                agora
        ));

        StatusLeito statusAnteriorDestino = leitoDestino.getStatus();
        leitoDestino.alterarStatus(StatusLeito.OCUPADO);
        leitoRepository.save(leitoDestino);

        historicoRepository.save(new HistoricoStatusLeito(
                leitoOrigem.getId(),
                alocacaoOrigem.getId(),
                statusAnteriorOrigem,
                StatusLeito.EM_HIGIENIZACAO,
                alocacaoOrigem.getAtendimentoId(),
                responsavelId,
                agora
        ));
        historicoRepository.save(new HistoricoStatusLeito(
                leitoDestino.getId(),
                novaAlocacao.getId(),
                statusAnteriorDestino,
                StatusLeito.OCUPADO,
                novaAlocacao.getAtendimentoId(),
                responsavelId,
                agora
        ));

        return novaAlocacao;
    }

    @Transactional
    public LiberacaoResponse liberarPorAtendimento(Long atendimentoId, Long responsavelId) {
        Optional<AlocacaoLeito> alocacaoOpt = alocacaoRepository.findByAtendimentoIdAndLiberadoEmIsNull(atendimentoId);
        if (alocacaoOpt.isEmpty()) {
            return new LiberacaoResponse(false, null);
        }

        AlocacaoLeito alocacao = alocacaoOpt.get();
        Instant agora = Instant.now();
        alocacao.encerrar(agora);
        alocacaoRepository.save(alocacao);

        Leito leito = leitoRepository.findById(alocacao.getLeitoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "leito nao encontrado"));

        StatusLeito statusAnterior = leito.getStatus();
        leito.alterarStatus(StatusLeito.EM_HIGIENIZACAO);
        leitoRepository.save(leito);

        historicoRepository.save(new HistoricoStatusLeito(
                leito.getId(),
                alocacao.getId(),
                statusAnterior,
                StatusLeito.EM_HIGIENIZACAO,
                atendimentoId,
                responsavelId,
                agora
        ));

        return new LiberacaoResponse(true, leito.getId());
    }
}
