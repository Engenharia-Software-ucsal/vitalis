package br.ucsal.vitalis.leitos.alocacao;

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
@Table(name = "alocacao_leito")
public class AlocacaoLeito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leito_id", nullable = false)
    private Long leitoId;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAlocacao status;

    @Column(name = "alocado_em", nullable = false)
    private Instant alocadoEm;

    @Column(name = "liberado_em")
    private Instant liberadoEm;

    @Column(name = "responsavel_id")
    private Long responsavelId;

    protected AlocacaoLeito() {
    }

    public AlocacaoLeito(Long leitoId, Long atendimentoId, StatusAlocacao status, Long responsavelId, Instant alocadoEm) {
        if (status != StatusAlocacao.OCUPADO && status != StatusAlocacao.RESERVADO) {
            throw new IllegalArgumentException("status inicial da alocacao deve ser OCUPADO ou RESERVADO");
        }
        this.leitoId = leitoId;
        this.atendimentoId = atendimentoId;
        this.status = status;
        this.responsavelId = responsavelId;
        this.alocadoEm = alocadoEm != null ? alocadoEm : Instant.now();
    }

    public boolean isAberta() {
        return liberadoEm == null;
    }

    public void encerrar(Instant agora) {
        if (!isAberta()) {
            throw new IllegalStateException("alocacao ja esta encerrada");
        }
        Instant momento = agora != null ? agora : Instant.now();
        this.liberadoEm = momento.isBefore(this.alocadoEm) ? this.alocadoEm : momento;
        this.status = StatusAlocacao.ENCERRADO;
    }

    public Long getId() { return id; }
    public Long getLeitoId() { return leitoId; }
    public Long getAtendimentoId() { return atendimentoId; }
    public StatusAlocacao getStatus() { return status; }
    public Instant getAlocadoEm() { return alocadoEm; }
    public Instant getLiberadoEm() { return liberadoEm; }
    public Long getResponsavelId() { return responsavelId; }
}
