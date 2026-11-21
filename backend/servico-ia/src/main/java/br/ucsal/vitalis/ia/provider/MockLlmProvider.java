package br.ucsal.vitalis.ia.provider;

/**
 * Adaptador local deterministico/mock para testes automatizados e ambientes sem LLM externo.
 */
public class MockLlmProvider implements LlmProvider {

    @Override
    public String providerName() {
        return "mock";
    }

    @Override
    public String modelName() {
        return "vitalis-mock-v1";
    }

    @Override
    public LlmResponse completar(LlmRequest request) {
        String resposta = """
                {
                  "status": "ATENCAO",
                  "nivel_alerta": "MODERADO",
                  "justificativa": "Analise consultiva simulada (provedor mock) baseada nos trechos documentais anexados. Revise posologia, alergias e funcao renal antes de assinar.",
                  "referencia_documental": "Bulario Eletronico ANVISA / Protocolo Vitalis"
                }
                """;
        return new LlmResponse(resposta, providerName(), modelName(), 120, 48, 12);
    }
}
