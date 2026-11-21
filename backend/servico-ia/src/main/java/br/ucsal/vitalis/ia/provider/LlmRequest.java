package br.ucsal.vitalis.ia.provider;

public record LlmRequest(
        String systemPrompt,
        String userPayloadJson,
        double temperature,
        double topP,
        int maxTokens
) {
}
