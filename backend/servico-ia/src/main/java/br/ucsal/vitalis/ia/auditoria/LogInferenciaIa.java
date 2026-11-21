package br.ucsal.vitalis.ia.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Immutable
@Table(name = "log_inferencia_ia")
public class LogInferenciaIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "modulo_ia", nullable = false, length = 40)
    private ModuloIa moduloIa;

    @Column(name = "provedor_ia", nullable = false, length = 60)
    private String provedorIa;

    @Column(name = "modelo_ia_versao", nullable = false, length = 120)
    private String modeloIaVersao;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "prompt_sanitizado_hash", nullable = false, length = 64, columnDefinition = "char(64)")
    private String promptSanitizadoHash;

    @Column(name = "tokens_prompt", nullable = false)
    private int tokensPrompt;

    @Column(name = "tokens_resposta", nullable = false)
    private int tokensResposta;

    @Column(name = "latencia_ms", nullable = false)
    private int latenciaMs;

    @Column(name = "score_confianca", precision = 4, scale = 3)
    private BigDecimal scoreConfianca;

    @Column(name = "modo_contingencia_ativo", nullable = false)
    private boolean modoContingenciaAtivo;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resposta_ia", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> respostaIa = new HashMap<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    protected LogInferenciaIa() {
    }

    public LogInferenciaIa(Long atendimentoId,
                           Long usuarioId,
                           ModuloIa moduloIa,
                           String provedorIa,
                           String modeloIaVersao,
                           String promptSanitizadoHash,
                           int tokensPrompt,
                           int tokensResposta,
                           int latenciaMs,
                           BigDecimal scoreConfianca,
                           boolean modoContingenciaAtivo,
                           Map<String, Object> respostaIa) {
        this.atendimentoId = atendimentoId;
        this.usuarioId = usuarioId;
        this.moduloIa = moduloIa;
        this.provedorIa = provedorIa;
        this.modeloIaVersao = modeloIaVersao;
        this.promptSanitizadoHash = promptSanitizadoHash;
        this.tokensPrompt = tokensPrompt;
        this.tokensResposta = tokensResposta;
        this.latenciaMs = latenciaMs;
        this.scoreConfianca = scoreConfianca;
        this.modoContingenciaAtivo = modoContingenciaAtivo;
        this.respostaIa = respostaIa == null ? new HashMap<>() : new HashMap<>(respostaIa);
    }

    public Long getId() { return id; }
    public Long getAtendimentoId() { return atendimentoId; }
    public Long getUsuarioId() { return usuarioId; }
    public ModuloIa getModuloIa() { return moduloIa; }
    public String getProvedorIa() { return provedorIa; }
    public String getModeloIaVersao() { return modeloIaVersao; }
    public String getPromptSanitizadoHash() { return promptSanitizadoHash; }
    public int getTokensPrompt() { return tokensPrompt; }
    public int getTokensResposta() { return tokensResposta; }
    public int getLatenciaMs() { return latenciaMs; }
    public BigDecimal getScoreConfianca() { return scoreConfianca; }
    public boolean isModoContingenciaAtivo() { return modoContingenciaAtivo; }
    public Map<String, Object> getRespostaIa() { return respostaIa; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
