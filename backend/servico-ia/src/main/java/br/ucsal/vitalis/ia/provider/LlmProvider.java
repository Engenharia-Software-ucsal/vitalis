package br.ucsal.vitalis.ia.provider;

/**
 * Porta agnostica a provedor/modelo de IA.
 * Permite trocar entre OpenRouter, Ollama, OpenAI, Groq, vLLM ou Mock apenas via configuracao.
 */
public interface LlmProvider {

    String providerName();

    String modelName();

    LlmResponse completar(LlmRequest request);
}
