package br.ucsal.vitalis.clinico.auth;

import java.util.List;

public record UsuarioResumoResponse(
        Long id,
        String nome,
        String email,
        List<String> roles
) {
    public static UsuarioResumoResponse de(Usuario usuario) {
        return new UsuarioResumoResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.codigosPapeis()
        );
    }
}
