package br.ucsal.vitalis.ia.privacidade;

import br.ucsal.vitalis.ia.provider.AiProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Gateway de higienizacao de PII (RF-018 / RN-3 / LGPD).
 * Remove identificadores diretos antes de qualquer trafego para provedores de IA.
 */
@Service
public class SanitizadorPiiService {

    private static final Pattern CPF_FORMATADO = Pattern.compile("\\b\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}\\b");
    private static final Pattern CPF_DIGITOS = Pattern.compile("\\b\\d{11}\\b");
    private static final Pattern CNS_DIGITOS = Pattern.compile("\\b\\d{15}\\b");
    private static final Pattern EMAIL = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern TELEFONE = Pattern.compile("(?:\\+?55\\s?)?(?:\\(?\\d{2}\\)?\\s?)?9?\\d{4}-?\\d{4}");
    private static final Pattern CEP = Pattern.compile("\\b\\d{5}-\\d{3}\\b");

    private final String hmacSecret;

    public SanitizadorPiiService(AiProperties props) {
        this.hmacSecret = (props.hmacSecret() == null || props.hmacSecret().isBlank())
                ? "vitalis-pii-pseudonym-hmac-secret-2026!"
                : props.hmacSecret();
    }

    public String sanitizarTexto(String texto, String nomePaciente) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String limpo = texto;
        limpo = EMAIL.matcher(limpo).replaceAll("[EMAIL_OMITIDO]");
        limpo = CNS_DIGITOS.matcher(limpo).replaceAll("[CNS_OMITIDO]");
        limpo = CPF_FORMATADO.matcher(limpo).replaceAll("[CPF_OMITIDO]");
        limpo = CPF_DIGITOS.matcher(limpo).replaceAll("[CPF_OMITIDO]");
        limpo = TELEFONE.matcher(limpo).replaceAll("[TELEFONE_OMITIDO]");
        limpo = CEP.matcher(limpo).replaceAll("[CEP_OMITIDO]");
        if (nomePaciente != null && !nomePaciente.isBlank()) {
            limpo = limpo.replaceAll("(?i)" + Pattern.quote(nomePaciente.trim()), "[PACIENTE_ANONIMIZADO]");
            for (String parte : nomePaciente.trim().split("\\s+")) {
                if (parte.length() >= 4) {
                    limpo = limpo.replaceAll("(?i)\\b" + Pattern.quote(parte) + "\\b", "[PACIENTE]");
                }
            }
        }
        return limpo;
    }

    public String gerarTokenSessao(Long atendimentoId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String payload = atendimentoId + "|" + Instant.now().toEpochMilli();
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 24);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar token HMAC de pseudonimizacao", e);
        }
    }

    public String hashSha256(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest((payload == null ? "" : payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao calcular SHA-256 do prompt sanitizado", e);
        }
    }
}
