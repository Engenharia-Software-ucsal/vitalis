package br.ucsal.vitalis.ia.provider;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class LlmProviderConfig {

    @Bean
    public LlmProvider llmProvider(AiProperties props, RestClient.Builder restClientBuilder) {
        String provider = props.provider() == null ? "openai-compatible" : props.provider().trim().toLowerCase();
        return switch (provider) {
            case "mock" -> new MockLlmProvider();
            case "openai-compatible", "openrouter", "ollama", "openai" ->
                    new OpenAiCompatibleLlmProvider(props, restClientBuilder);
            default -> throw new IllegalArgumentException("Provedor de IA nao suportado: " + provider);
        };
    }

    @Bean
    public CircuitBreaker iaCircuitBreaker() {
        // RN-4 / Secao 10.5: taxa de falhas > 5% ou falhas consecutivas abrem circuito;
        // espera 30s em OPEN e testa 5 chamadas em HALF_OPEN.
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(20)
                .minimumNumberOfCalls(5)
                .failureRateThreshold(5.0f)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(5)
                .build();
        return CircuitBreaker.of("vitalis-llm", config);
    }
}
