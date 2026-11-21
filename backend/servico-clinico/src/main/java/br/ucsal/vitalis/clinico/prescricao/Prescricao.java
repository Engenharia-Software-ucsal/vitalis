package br.ucsal.vitalis.clinico.prescricao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prescricao")
public class Prescricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "prescritor_id", nullable = false)
    private Long prescritorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPrescricao status = StatusPrescricao.ATIVA;

    @Column(name = "alergias_reconhecidas", nullable = false)
    private boolean alergiasReconhecidas;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private OffsetDateTime criadaEm = OffsetDateTime.now();

    @Column(name = "assinada_em")
    private OffsetDateTime assinadaEm;

    @Column(name = "assinatura_ref", length = 500)
    private String assinaturaRef;

    protected Prescricao() {
    }

    public Prescricao(Long atendimentoId, Long prescritorId, boolean alergiasReconhecidas) {
        this.atendimentoId = atendimentoId;
        this.prescritorId = prescritorId;
        this.status = StatusPrescricao.ATIVA;
        this.alergiasReconhecidas = alergiasReconhecidas;
    }

    public Long getId() { return id; }
    public Long getAtendimentoId() { return atendimentoId; }
    public Long getPrescritorId() { return prescritorId; }
    public StatusPrescricao getStatus() { return status; }
    public boolean isAlergiasReconhecidas() { return alergiasReconhecidas; }
    public OffsetDateTime getCriadaEm() { return criadaEm; }
    public OffsetDateTime getAssinadaEm() { return assinadaEm; }
    public String getAssinaturaRef() { return assinaturaRef; }
}
