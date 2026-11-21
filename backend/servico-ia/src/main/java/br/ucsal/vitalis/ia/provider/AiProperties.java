package br.ucsal.vitalis.ia.provider;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vitalis.ia")
public record AiProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        long timeoutMs,
        double temperature,
        double topP,
        String extraReferer,
        String extraTitle,
        String hmacSecret
) {
    public double temperaturaSegura() {
        // Secao 10: limite estrito tau <= 0.1 para coibir alucinacoes clinicas
        return Math.min(Math.max(temperature, 0.0), 0.1);
    }
}
