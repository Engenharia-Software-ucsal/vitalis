package br.ucsal.vitalis.ia.provider;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adaptador padrao compativel com a especificacao OpenAI (/chat/completions).
 * Funciona sem alteracao de codigo com OpenRouter, Ollama local, OpenAI, Groq e vLLM.
 */
public class OpenAiCompatibleLlmProvider implements LlmProvider {

    private final AiProperties props;
    private final RestClient restClient;

    public OpenAiCompatibleLlmProvider(AiProperties props, RestClient.Builder builder) {
        this.props = props;
        int timeoutMs = (int) Math.max(500L, props.timeoutMs());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));

        RestClient.Builder configured = builder
                .baseUrl(normalizarBaseUrl(props.baseUrl()))
                .requestFactory(factory)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (props.apiKey() != null && !props.apiKey().isBlank()) {
            configured.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.apiKey().trim());
        }
        // Headers recomendados pelo OpenRouter (inofensivos para Ollama/OpenAI)
        if (props.extraReferer() != null && !props.extraReferer().isBlank()) {
            configured.defaultHeader("HTTP-Referer", props.extraReferer().trim());
        }
        if (props.extraTitle() != null && !props.extraTitle().isBlank()) {
            configured.defaultHeader("X-Title", props.extraTitle().trim());
        }

        this.restClient = configured.build();
    }

    @Override
    public String providerName() {
        String url = props.baseUrl() == null ? "" : props.baseUrl().toLowerCase();
        if (url.contains("openrouter.ai")) return "openrouter";
        if (url.contains("11434") || url.contains("ollama")) return "ollama";
        if (url.contains("openai.com")) return "openai";
        return "openai-compatible";
    }

    @Override
    public String modelName() {
        return props.model();
    }

    @Override
    public LlmResponse completar(LlmRequest request) {
        long inicio = System.nanoTime();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", props.model());
        body.put("temperature", request.temperature());
        body.put("top_p", request.topP());
        body.put("max_tokens", request.maxTokens());
        body.put("messages", List.of(
                Map.of("role", "system", "content", request.systemPrompt()),
                Map.of("role", "user", "content", request.userPayloadJson())
        ));

        JsonNode resp = restClient.post()
                .uri("/chat/completions")
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        int latencyMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);
        if (resp == null || !resp.has("choices") || resp.get("choices").isEmpty()) {
            throw new IllegalStateException("Resposta vazia do provedor de IA");
        }

        String content = resp.path("choices").get(0).path("message").path("content").asText("");
        String modelReturned = resp.path("model").asText(props.model());
        int promptTokens = resp.path("usage").path("prompt_tokens").asInt(0);
        int completionTokens = resp.path("usage").path("completion_tokens").asInt(0);

        return new LlmResponse(content, providerName(), modelReturned, promptTokens, completionTokens, latencyMs);
    }

    private static String normalizarBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:11434/v1";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
