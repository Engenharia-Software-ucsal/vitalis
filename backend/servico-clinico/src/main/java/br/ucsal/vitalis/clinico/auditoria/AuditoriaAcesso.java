package br.ucsal.vitalis.clinico.auditoria;

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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Immutable
@Table(name = "auditoria_acesso")
public class AuditoriaAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ator_id")
    private Long atorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcaoAuditoria acao;

    @Column(nullable = false, length = 120)
    private String recurso;

    @Column(name = "recurso_id", length = 120)
    private String recursoId;

    @Column(length = 1000)
    private String justificativa;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> metadados = new HashMap<>();

    @Column(name = "ocorrido_em", nullable = false, updatable = false)
    private OffsetDateTime ocorridoEm = OffsetDateTime.now();

    protected AuditoriaAcesso() {
    }

    public AuditoriaAcesso(Long atorId,
                           AcaoAuditoria acao,
                           String recurso,
                           String recursoId,
                           String justificativa,
                           Map<String, Object> metadados) {
        this.atorId = atorId;
        this.acao = acao;
        this.recurso = recurso;
        this.recursoId = recursoId;
        this.justificativa = justificativa;
        this.metadados = metadados == null ? new HashMap<>() : new HashMap<>(metadados);
    }

    public Long getId() { return id; }
    public Long getAtorId() { return atorId; }
    public AcaoAuditoria getAcao() { return acao; }
    public String getRecurso() { return recurso; }
    public String getRecursoId() { return recursoId; }
    public String getJustificativa() { return justificativa; }
    public Map<String, Object> getMetadados() { return metadados; }
    public OffsetDateTime getOcorridoEm() { return ocorridoEm; }
}
