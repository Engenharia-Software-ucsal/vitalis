package br.ucsal.vitalis.leitos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAutenticado {

    public Long id() {
        Jwt jwt = currentJwt();
        if (jwt == null) {
            return null;
        }
        Object uid = jwt.getClaim("uid");
        if (uid instanceof Number number) {
            return number.longValue();
        }
        if (uid instanceof String str && !str.isBlank()) {
            return Long.parseLong(str);
        }
        return null;
    }

    public String bearerToken() {
        Jwt jwt = currentJwt();
        if (jwt == null) {
            return null;
        }
        return "Bearer " + jwt.getTokenValue();
    }

    private Jwt currentJwt() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken();
        }
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}
