package br.ucsal.vitalis.leitos.alocacao;

import br.ucsal.vitalis.leitos.integracao.ClinicoClient;
import br.ucsal.vitalis.leitos.integracao.ElegibilidadeInternacaoResponse;
import br.ucsal.vitalis.leitos.leito.HistoricoStatusLeito;
import br.ucsal.vitalis.leitos.leito.HistoricoStatusLeitoRepository;
import br.ucsal.vitalis.leitos.leito.Leito;
import br.ucsal.vitalis.leitos.leito.LeitoRepository;
import br.ucsal.vitalis.leitos.leito.StatusLeito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlocacaoServiceTest {

    @Mock
    private LeitoRepository leitoRepository;

    @Mock
    private AlocacaoLeitoRepository alocacaoRepository;

    @Mock
    private HistoricoStatusLeitoRepository historicoRepository;

    @Mock
    private ClinicoClient clinicoClient;

    private AlocacaoService service;

    @BeforeEach
    void setUp() {
        service = new AlocacaoService(leitoRepository, alocacaoRepository, historicoRepository, clinicoClient);
    }

    @Test
    void deveBloquearAlocacaoQuandoAtendimentoNaoPassouPorTriagem() {
        when(clinicoClient.verificarElegibilidadeInternacao(10L, "Bearer token"))
                .thenReturn(new ElegibilidadeInternacaoResponse(
                        10L, false, false, "AGUARDANDO_TRIAGEM",
                        "Paciente precisa passar pela triagem antes da alocacao de leito"
                ));

        assertThatThrownBy(() -> service.alocar(1L, 10L, StatusAlocacao.OCUPADO, 99L, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(rse.getReason()).contains("triagem");
                });

        verify(alocacaoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void deveBloquearAlocacaoEmLeitoEmHigienizacao() {
        when(clinicoClient.verificarElegibilidadeInternacao(10L, "Bearer token"))
                .thenReturn(new ElegibilidadeInternacaoResponse(10L, true, true, "EM_ATENDIMENTO", null));

        Leito leito = criarLeito(3L, "UTI-03", "UTI Adulto", StatusLeito.EM_HIGIENIZACAO);
        when(leitoRepository.findById(3L)).thenReturn(Optional.of(leito));

        assertThatThrownBy(() -> service.alocar(3L, 10L, StatusAlocacao.OCUPADO, 99L, "Bearer token"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
                    assertThat(rse.getReason()).contains("higienizacao");
                });

        verify(alocacaoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void deveTransferirPacienteEntreLeitosERegistrarDoisHistoricos() {
        Leito origem = criarLeito(1L, "ENF-101", "Clinica Medica", StatusLeito.OCUPADO);
        Leito destino = criarLeito(2L, "UTI-01", "UTI Adulto", StatusLeito.LIVRE);

        AlocacaoLeito alocacaoOrigem = new AlocacaoLeito(1L, 50L, StatusAlocacao.OCUPADO, 7L, Instant.now().minusSeconds(60));
        ReflectionTestUtils.setField(alocacaoOrigem, "id", 100L);

        when(alocacaoRepository.findById(100L)).thenReturn(Optional.of(alocacaoOrigem));
        when(leitoRepository.findById(1L)).thenReturn(Optional.of(origem));
        when(leitoRepository.findById(2L)).thenReturn(Optional.of(destino));
        when(alocacaoRepository.findByLeitoIdAndLiberadoEmIsNull(2L)).thenReturn(Optional.empty());
        when(alocacaoRepository.saveAndFlush(any(AlocacaoLeito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(alocacaoRepository.save(any(AlocacaoLeito.class))).thenAnswer(inv -> {
            AlocacaoLeito salva = inv.getArgument(0);
            ReflectionTestUtils.setField(salva, "id", 101L);
            return salva;
        });

        AlocacaoLeito novaAlocacao = service.transferir(100L, 2L, 88L);

        assertThat(alocacaoOrigem.getStatus()).isEqualTo(StatusAlocacao.ENCERRADO);
        assertThat(alocacaoOrigem.getLiberadoEm()).isNotNull();
        assertThat(origem.getStatus()).isEqualTo(StatusLeito.EM_HIGIENIZACAO);

        assertThat(destino.getStatus()).isEqualTo(StatusLeito.OCUPADO);
        assertThat(novaAlocacao.getLeitoId()).isEqualTo(2L);
        assertThat(novaAlocacao.getAtendimentoId()).isEqualTo(50L);
        assertThat(novaAlocacao.getStatus()).isEqualTo(StatusAlocacao.OCUPADO);
        assertThat(novaAlocacao.isAberta()).isTrue();

        ArgumentCaptor<HistoricoStatusLeito> captor = ArgumentCaptor.forClass(HistoricoStatusLeito.class);
        verify(historicoRepository, times(2)).save(captor.capture());
        List<HistoricoStatusLeito> historicos = captor.getAllValues();

        assertThat(historicos.get(0).getLeitoId()).isEqualTo(1L);
        assertThat(historicos.get(0).getStatusAnterior()).isEqualTo(StatusLeito.OCUPADO);
        assertThat(historicos.get(0).getStatusNovo()).isEqualTo(StatusLeito.EM_HIGIENIZACAO);

        assertThat(historicos.get(1).getLeitoId()).isEqualTo(2L);
        assertThat(historicos.get(1).getStatusAnterior()).isEqualTo(StatusLeito.LIVRE);
        assertThat(historicos.get(1).getStatusNovo()).isEqualTo(StatusLeito.OCUPADO);
    }

    @Test
    void deveLiberarLeitoNaAltaEMoverParaEmHigienizacao() {
        Leito leito = criarLeito(1L, "UTI-01", "UTI Adulto", StatusLeito.OCUPADO);
        AlocacaoLeito alocacao = new AlocacaoLeito(1L, 77L, StatusAlocacao.OCUPADO, 5L, Instant.now().minusSeconds(120));
        ReflectionTestUtils.setField(alocacao, "id", 200L);

        when(alocacaoRepository.findByAtendimentoIdAndLiberadoEmIsNull(77L)).thenReturn(Optional.of(alocacao));
        when(leitoRepository.findById(1L)).thenReturn(Optional.of(leito));

        AlocacaoService.LiberacaoResponse resposta = service.liberarPorAtendimento(77L, 9L);

        assertThat(resposta.liberado()).isTrue();
        assertThat(resposta.leitoId()).isEqualTo(1L);
        assertThat(alocacao.getStatus()).isEqualTo(StatusAlocacao.ENCERRADO);
        assertThat(alocacao.getLiberadoEm()).isNotNull();
        assertThat(leito.getStatus()).isEqualTo(StatusLeito.EM_HIGIENIZACAO);

        ArgumentCaptor<HistoricoStatusLeito> captor = ArgumentCaptor.forClass(HistoricoStatusLeito.class);
        verify(historicoRepository).save(captor.capture());
        assertThat(captor.getValue().getLeitoId()).isEqualTo(1L);
        assertThat(captor.getValue().getStatusAnterior()).isEqualTo(StatusLeito.OCUPADO);
        assertThat(captor.getValue().getStatusNovo()).isEqualTo(StatusLeito.EM_HIGIENIZACAO);
        assertThat(captor.getValue().getAtendimentoId()).isEqualTo(77L);
    }

    @Test
    void deveRetornarLiberadoFalseQuandoNaoExisteAlocacaoAbertaParaAtendimento() {
        when(alocacaoRepository.findByAtendimentoIdAndLiberadoEmIsNull(999L)).thenReturn(Optional.empty());

        AlocacaoService.LiberacaoResponse resposta = service.liberarPorAtendimento(999L, 9L);

        assertThat(resposta.liberado()).isFalse();
        assertThat(resposta.leitoId()).isNull();
        verify(leitoRepository, never()).save(any());
        verify(historicoRepository, never()).save(any());
    }

    private Leito criarLeito(Long id, String codigo, String ala, StatusLeito status) {
        Leito leito = new Leito(codigo, ala);
        ReflectionTestUtils.setField(leito, "id", id);
        if (status != StatusLeito.LIVRE) {
            leito.alterarStatus(status);
        }
        return leito;
    }
}
