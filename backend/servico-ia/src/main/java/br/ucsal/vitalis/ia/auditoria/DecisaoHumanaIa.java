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

import java.time.OffsetDateTime;

@Entity
@Immutable
@Table(name = "decisao_humana_ia")
public class DecisaoHumanaIa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_inferencia_id", nullable = false, unique = true)
    private Long logInferenciaId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decisao_medica", nullable = false, length = 20)
    private DecisaoMedica decisaoMedica;

    @Column(name = "justificativa_humana", columnDefinition = "text")
    private String justificativaHumana;

    @Column(name = "decidido_em", nullable = false, updatable = false)
    private OffsetDateTime decididoEm = OffsetDateTime.now();

    protected DecisaoHumanaIa() {
    }

    public DecisaoHumanaIa(Long logInferenciaId, Long usuarioId, DecisaoMedica decisaoMedica, String justificativaHumana) {
        this.logInferenciaId = logInferenciaId;
        this.usuarioId = usuarioId;
        this.decisaoMedica = decisaoMedica;
        this.justificativaHumana = justificativaHumana;
    }

    public Long getId() { return id; }
    public Long getLogInferenciaId() { return logInferenciaId; }
    public Long getUsuarioId() { return usuarioId; }
    public DecisaoMedica getDecisaoMedica() { return decisaoMedica; }
    public String getJustificativaHumana() { return justificativaHumana; }
    public OffsetDateTime getDecididoEm() { return decididoEm; }
}
