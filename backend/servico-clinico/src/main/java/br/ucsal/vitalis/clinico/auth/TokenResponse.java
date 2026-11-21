package br.ucsal.vitalis.clinico.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UsuarioResumoResponse usuario
) {
}
