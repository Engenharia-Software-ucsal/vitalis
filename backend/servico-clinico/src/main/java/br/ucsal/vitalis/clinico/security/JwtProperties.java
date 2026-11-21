package br.ucsal.vitalis.clinico.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vitalis.jwt")
public record JwtProperties(
        String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
