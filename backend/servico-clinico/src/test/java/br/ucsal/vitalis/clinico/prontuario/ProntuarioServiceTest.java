package br.ucsal.vitalis.clinico.prontuario;

import br.ucsal.vitalis.clinico.alergia.AlergiaRepository;
import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.atendimento.AtendimentoRepository;
import br.ucsal.vitalis.clinico.atendimento.TipoAtendimento;
import br.ucsal.vitalis.clinico.auditoria.AuditoriaService;
import br.ucsal.vitalis.clinico.auth.UsuarioRepository;
import br.ucsal.vitalis.clinico.paciente.PacienteRepository;
import br.ucsal.vitalis.clinico.prescricao.PrescricaoService;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import br.ucsal.vitalis.clinico.triagem.TriagemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProntuarioServiceTest {

    @Mock private ProntuarioRepository prontuarioRepository;
    @Mock private EvolucaoClinicaRepository evolucaoRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private AlergiaRepository alergiaRepository;
    @Mock private AtendimentoRepository atendimentoRepository;
    @Mock private TriagemRepository triagemRepository;
    @Mock private PrescricaoService prescricaoService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioAutenticado usuarioAutenticado;
    @Mock private AuditoriaService auditoriaService;

    private ProntuarioService service;

    @BeforeEach
    void setUp() {
        service = new ProntuarioService(
                prontuarioRepository,
                evolucaoRepository,
                pacienteRepository,
                alergiaRepository,
                atendimentoRepository,
                triagemRepository,
                prescricaoService,
                usuarioRepository,
                usuarioAutenticado,
                auditoriaService
        );
    }

    @Test
    void deveBloquearEvolucaoEmAtendimentoEncerrado_RN12() {
        Atendimento atendimento = new Atendimento(10L, 4L, TipoAtendimento.HOSPITALAR);
        atendimento.concederAlta("Alta hospitalar");

        when(pacienteRepository.existsById(10L)).thenReturn(true);
        when(atendimentoRepository.findById(5L)).thenReturn(Optional.of(atendimento));

        var req = new NovaEvolucaoRequest(5L, "Tentativa de evolucao pos-alta");

        assertThatThrownBy(() -> service.registrarEvolucao(10L, req))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
