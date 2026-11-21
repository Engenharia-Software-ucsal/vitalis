package br.ucsal.vitalis.ia.privacidade;

import br.ucsal.vitalis.ia.provider.AiProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizadorPiiServiceTest {

    @Test
    void deveRemoverTodosOsIdentificadoresPiiAntesDeEnvioExterno_RF018() {
        AiProperties props = new AiProperties("mock", "", "", "mock", 2000, 0.0, 0.85, "", "", "secret-test");
        SanitizadorPiiService service = new SanitizadorPiiService(props);

        String bruto = "Paciente Joao Carlos da Silva, CPF 529.982.247-25, CNS 700000000000001, "
                + "tel (71) 99999-1010, email joao@exemplo.com, CEP 40000-000.";
        String limpo = service.sanitizarTexto(bruto, "Joao Carlos da Silva");

        assertThat(limpo).doesNotContain("Joao", "529.982.247-25", "700000000000001", "99999-1010", "joao@exemplo.com", "40000-000");
        assertThat(limpo).contains("[PACIENTE_ANONIMIZADO]", "[CPF_OMITIDO]", "[CNS_OMITIDO]", "[EMAIL_OMITIDO]", "[TELEFONE_OMITIDO]", "[CEP_OMITIDO]");
        assertThat(service.hashSha256(limpo)).hasSize(64);
    }
}
