package br.ucsal.vitalis.clinico.auth;

import br.ucsal.vitalis.clinico.security.JwtProperties;
import br.ucsal.vitalis.clinico.security.UsuarioAutenticado;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;
    private final UsuarioAutenticado usuarioAutenticado;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtEncoder jwtEncoder,
                       JwtDecoder jwtDecoder,
                       JwtProperties jwtProperties,
                       UsuarioAutenticado usuarioAutenticado) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.jwtProperties = jwtProperties;
        this.usuarioAutenticado = usuarioAutenticado;
    }

    public TokenResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(req.email().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas"));

        if (!"ATIVO".equals(usuario.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inativo ou bloqueado");
        }

        if (!passwordEncoder.matches(req.senha(), usuario.getSenhaHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        return emitirTokens(usuario);
    }

    public TokenResponse refresh(RefreshRequest req) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(req.refreshToken());
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalido ou expirado");
        }

        if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token informado nao e um refresh token");
        }

        Object uidClaim = jwt.getClaim("uid");
        if (!(uidClaim instanceof Number n)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalido");
        }

        Usuario usuario = usuarioRepository.findById(n.longValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));

        if (!"ATIVO".equals(usuario.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario inativo ou bloqueado");
        }

        return emitirTokens(usuario);
    }

    public UsuarioResumoResponse me() {
        Usuario usuario = usuarioRepository.findById(usuarioAutenticado.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));
        return UsuarioResumoResponse.de(usuario);
    }

    private TokenResponse emitirTokens(Usuario usuario) {
        Instant agora = Instant.now();
        List<String> roles = usuario.codigosPapeis();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet accessClaims = JwtClaimsSet.builder()
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(jwtProperties.accessTtlSeconds()))
                .claim("uid", usuario.getId())
                .claim("nome", usuario.getNome())
                .claim("roles", roles)
                .claim("typ", "access")
                .build();

        JwtClaimsSet refreshClaims = JwtClaimsSet.builder()
                .subject(usuario.getEmail())
                .issuedAt(agora)
                .expiresAt(agora.plusSeconds(jwtProperties.refreshTtlSeconds()))
                .claim("uid", usuario.getId())
                .claim("typ", "refresh")
                .build();

        String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(header, accessClaims)).getTokenValue();
        String refreshToken = jwtEncoder.encode(JwtEncoderParameters.from(header, refreshClaims)).getTokenValue();

        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtProperties.accessTtlSeconds(),
                UsuarioResumoResponse.de(usuario)
        );
    }
}
