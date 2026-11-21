package br.ucsal.vitalis.leitos.leito;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "historico_status_leito")
public class HistoricoStatusLeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leito_id", nullable = false)
    private Long leitoId;

    @Column(name = "alocacao_id")
    private Long alocacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusLeito statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false, length = 20)
    private StatusLeito statusNovo;

    @Column(name = "atendimento_id")
    private Long atendimentoId;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    @Column(name = "alterado_em", nullable = false)
    private Instant alteradoEm;

    protected HistoricoStatusLeito() {
    }

    public HistoricoStatusLeito(
            Long leitoId,
            Long alocacaoId,
            StatusLeito statusAnterior,
            StatusLeito statusNovo,
            Long atendimentoId,
            Long responsavelId,
            Instant alteradoEm
    ) {
        this.leitoId = leitoId;
        this.alocacaoId = alocacaoId;
        this.statusAnterior = statusAnterior;
        this.statusNovo = statusNovo;
        this.atendimentoId = atendimentoId;
        this.responsavelId = responsavelId;
        this.alteradoEm = alteradoEm != null ? alteradoEm : Instant.now();
    }

    public Long getId() { return id; }
    public Long getLeitoId() { return leitoId; }
    public Long getAlocacaoId() { return alocacaoId; }
    public StatusLeito getStatusAnterior() { return statusAnterior; }
    public StatusLeito getStatusNovo() { return statusNovo; }
    public Long getAtendimentoId() { return atendimentoId; }
    public Long getResponsavelId() { return responsavelId; }
    public Instant getAlteradoEm() { return alteradoEm; }
}
