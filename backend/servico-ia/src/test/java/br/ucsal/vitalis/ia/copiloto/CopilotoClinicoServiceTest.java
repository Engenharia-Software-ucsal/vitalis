package br.ucsal.vitalis.ia.copiloto;

import br.ucsal.vitalis.ia.auditoria.DecisaoHumanaIaRepository;
import br.ucsal.vitalis.ia.auditoria.DecisaoMedica;
import br.ucsal.vitalis.ia.auditoria.LogInferenciaIa;
import br.ucsal.vitalis.ia.auditoria.LogInferenciaIaRepository;
import br.ucsal.vitalis.ia.deterministico.InteracaoMedicamentosaLocal;
import br.ucsal.vitalis.ia.deterministico.InteracaoMedicamentosaRepository;
import br.ucsal.vitalis.ia.privacidade.SanitizadorPiiService;
import br.ucsal.vitalis.ia.provider.AiProperties;
import br.ucsal.vitalis.ia.provider.LlmProvider;
import br.ucsal.vitalis.ia.rag.KnowledgeRetriever;
import br.ucsal.vitalis.ia.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopilotoClinicoServiceTest {

    @Mock private LlmProvider llmProvider;
    @Mock private KnowledgeRetriever knowledgeRetriever;
    @Mock private InteracaoMedicamentosaRepository interacaoRepository;
    @Mock private LogInferenciaIaRepository logRepository;
    @Mock private DecisaoHumanaIaRepository decisaoRepository;
    @Mock private UsuarioAutenticado usuarioAutenticado;

    private CopilotoClinicoService service;

    @BeforeEach
    void setUp() {
        AiProperties props = new AiProperties("openai-compatible", "http://localhost:11434/v1", "k", "llama3.1", 2000, 0.0, 0.85, "", "", "secret");
        service = new CopilotoClinicoService(
                llmProvider,
                CircuitBreaker.ofDefaults("test-cb"),
                props,
                new SanitizadorPiiService(props),
                knowledgeRetriever,
                interacaoRepository,
                logRepository,
                decisaoRepository,
                usuarioAutenticado,
                new ObjectMapper()
        );
    }

    @Test
    void deveAcionarFallbackDeterministicoQuandoLlmFalharOuTimeout_RN4() {
        when(usuarioAutenticado.id()).thenReturn(4L);
        when(llmProvider.providerName()).thenReturn("openrouter");
        when(llmProvider.modelName()).thenReturn("google/gemini-2.5-flash");
        when(llmProvider.completar(any())).thenThrow(new RuntimeException("Read timed out (>2000ms)"));
        when(knowledgeRetriever.buscar(any(), anyInt())).thenReturn(List.of());
        when(interacaoRepository.findAll()).thenReturn(List.of(
                new InteracaoMedicamentosaLocal("Meropenem", "Acido Valproico", "X", "Queda de niveis sericos", "ANVISA")
        ));
        when(logRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var req = new AnalisePrescricaoRequest(
                1L, "Joao da Silva", 68, "M", 72.0, 2.4,
                List.of("Penicilinas"),
                List.of("Acido Valproico 500mg"),
                List.of(new AnalisePrescricaoRequest.ItemSolicitado("Meropenem 1g", "1g", "IV", "8/8h"))
        );

        Map<String, Object> resp = service.analisarPrescricao(req);

        assertThat(resp.get("modoContingenciaAtivo")).isEqualTo(true);
        assertThat(resp.get("nivelAlerta")).isEqualTo("CRITICO");
        assertThat(resp.get("exigeAjusteRenal")).isEqualTo(true);
        assertThat((List<?>) resp.get("alertasDeterministicos")).hasSize(2);
    }

    @Test
    void deveExigirJustificativaComMinimo10CaracteresParaRejeicaoHitl_RN2() {
        when(logRepository.existsById(99L)).thenReturn(true);
        when(decisaoRepository.existsByLogInferenciaId(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.registrarDecisaoHitl(99L, new DecisaoHitlRequest(DecisaoMedica.REJEITADA, "curta")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
    }
}
