package br.ucsal.vitalis.ia.provider;

public record LlmResponse(
        String content,
        String providerName,
        String modelVersion,
        int promptTokens,
        int completionTokens,
        int latencyMs
) {
}
