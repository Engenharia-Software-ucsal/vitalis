package br.ucsal.vitalis.ia.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UsuarioAutenticado {

    public Long id() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("Usuario autenticado nao encontrado no contexto de seguranca");
        }
        Object uid = jwt.getClaim("uid");
        if (uid instanceof Number n) {
            return n.longValue();
        }
        if (uid instanceof String s) {
            return Long.parseLong(s);
        }
        throw new IllegalStateException("Claim uid ausente no token JWT");
    }
}
