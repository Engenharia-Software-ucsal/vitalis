package br.ucsal.vitalis.clinico.prescricao;

import br.ucsal.vitalis.clinico.alergia.Alergia;
import br.ucsal.vitalis.clinico.alergia.AlergiaRepository;
import br.ucsal.vitalis.clinico.alergia.GravidadeAlergia;
import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.atendimento.AtendimentoRepository;
import br.ucsal.vitalis.clinico.atendimento.TipoAtendimento;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescricaoServiceTest {

    @Mock private PrescricaoRepository prescricaoRepository;
    @Mock private ItemPrescricaoRepository itemPrescricaoRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private AtendimentoRepository atendimentoRepository;
    @Mock private AlergiaRepository alergiaRepository;
    @Mock private UsuarioAutenticado usuarioAutenticado;
    @Mock private AuditoriaService auditoriaService;

    private PrescricaoService service;

    @BeforeEach
    void setUp() {
        service = new PrescricaoService(
                prescricaoRepository,
                itemPrescricaoRepository,
                medicamentoRepository,
                atendimentoRepository,
                alergiaRepository,
                usuarioAutenticado,
                auditoriaService
        );
    }

    @Test
    void deveBloquearPrescricaoSemReconhecimentoDeAlergiaAtiva_RN008() {
        Atendimento atendimento = new Atendimento(10L, 4L, TipoAtendimento.EMERGENCIA);
        Alergia alergia = new Alergia(10L, "Penicilina", "Anafilaxia", GravidadeAlergia.GRAVE, 4L);

        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(atendimento));
        when(alergiaRepository.findByPacienteIdAndAtivaTrue(10L)).thenReturn(List.of(alergia));

        var req = new NovaPrescricaoRequest(false, List.of(
                new NovaPrescricaoRequest.NovoItemPrescricaoRequest(
                        2L, "500mg", "VO", "8/8h", "7 dias", BigDecimal.ONE, null
                )
        ));

        assertThatThrownBy(() -> service.criar(1L, req))
                .isInstanceOf(AlergiaNaoReconhecidaException.class)
                .hasMessageContaining("RN-008");
    }

    @Test
    void devePermitirPrescricaoAposReconhecimentoExplicitoDeAlergia_RN008() {
        Atendimento atendimento = new Atendimento(10L, 4L, TipoAtendimento.EMERGENCIA);
        Alergia alergia = new Alergia(10L, "Penicilina", "Anafilaxia", GravidadeAlergia.GRAVE, 4L);
        Medicamento med = org.mockito.Mockito.mock(Medicamento.class);
        when(med.getId()).thenReturn(2L);
        when(med.getNome()).thenReturn("Dipirona 500mg");

        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(atendimento));
        when(alergiaRepository.findByPacienteIdAndAtivaTrue(10L)).thenReturn(List.of(alergia));
        when(medicamentoRepository.findAllById(List.of(2L))).thenReturn(List.of(med));
        when(usuarioAutenticado.id()).thenReturn(4L);
        when(prescricaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(itemPrescricaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new NovaPrescricaoRequest(true, List.of(
                new NovaPrescricaoRequest.NovoItemPrescricaoRequest(
                        2L, "500mg", "VO", "6/6h", "3 dias", BigDecimal.ONE, "Se dor"
                )
        ));

        PrescricaoResponse resp = service.criar(1L, req);
        assertThat(resp.alergiasReconhecidas()).isTrue();
        assertThat(resp.itens()).hasSize(1);
        verify(auditoriaService).registrar(any(), any(), any(), any());
    }

    @Test
    void deveBloquearPrescricaoEmAtendimentoComAlta_RN12() {
        Atendimento atendimento = new Atendimento(10L, 4L, TipoAtendimento.HOSPITALAR);
        atendimento.concederAlta("Melhora clinica");

        when(atendimentoRepository.findById(1L)).thenReturn(Optional.of(atendimento));

        var req = new NovaPrescricaoRequest(true, List.of(
                new NovaPrescricaoRequest.NovoItemPrescricaoRequest(
                        2L, "500mg", "VO", "8/8h", null, BigDecimal.ONE, null
                )
        ));

        assertThatThrownBy(() -> service.criar(1L, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
