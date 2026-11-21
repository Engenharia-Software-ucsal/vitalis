package br.ucsal.vitalis.clinico.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class UsuarioAutenticado {

    public Long id() {
        Jwt jwt = jwtAtual();
        Object uid = jwt.getClaim("uid");
        if (uid instanceof Number n) {
            return n.longValue();
        }
        if (uid instanceof String s) {
            return Long.parseLong(s);
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sem identificacao de usuario");
    }

    public String email() {
        return jwtAtual().getSubject();
    }

    public List<String> roles() {
        List<String> roles = jwtAtual().getClaimAsStringList("roles");
        return roles == null ? List.of() : roles;
    }

    public String token() {
        return jwtAtual().getTokenValue();
    }

    public Long idOuNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            Object uid = jwt.getClaim("uid");
            if (uid instanceof Number n) {
                return n.longValue();
            }
            if (uid instanceof String s) {
                return Long.parseLong(s);
            }
        }
        return null;
    }

    private Jwt jwtAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao autenticado");
        }
        return jwt;
    }
}
