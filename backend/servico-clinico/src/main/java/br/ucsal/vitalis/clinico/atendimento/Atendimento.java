package br.ucsal.vitalis.clinico.atendimento;

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
@Table(name = "atendimento")
public class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "agendamento_id")
    private Long agendamentoId;

    @Column(name = "responsavel_id", nullable = false)
    private Long responsavelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoAtendimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAtendimento status = StatusAtendimento.ABERTO;

    @Column(name = "iniciado_em", nullable = false, updatable = false)
    private OffsetDateTime iniciadoEm = OffsetDateTime.now();

    @Column(name = "encerrado_em")
    private OffsetDateTime encerradoEm;

    @Column(name = "motivo_encerramento", length = 500)
    private String motivoEncerramento;

    protected Atendimento() {
    }

    public Atendimento(Long pacienteId, Long responsavelId, TipoAtendimento tipo) {
        this.pacienteId = pacienteId;
        this.responsavelId = responsavelId;
        this.tipo = tipo;
        this.status = StatusAtendimento.ABERTO;
        this.iniciadoEm = OffsetDateTime.now();
    }

    public void assumir(Long novoResponsavelId) {
        this.status = StatusAtendimento.EM_ANDAMENTO;
        this.responsavelId = novoResponsavelId;
    }

    public void concederAlta(String motivoEncerramento) {
        this.status = StatusAtendimento.ALTA;
        this.encerradoEm = OffsetDateTime.now();
        this.motivoEncerramento = motivoEncerramento;
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public Long getAgendamentoId() { return agendamentoId; }
    public Long getResponsavelId() { return responsavelId; }
    public TipoAtendimento getTipo() { return tipo; }
    public StatusAtendimento getStatus() { return status; }
    public OffsetDateTime getIniciadoEm() { return iniciadoEm; }
    public OffsetDateTime getEncerradoEm() { return encerradoEm; }
    public String getMotivoEncerramento() { return motivoEncerramento; }
}
