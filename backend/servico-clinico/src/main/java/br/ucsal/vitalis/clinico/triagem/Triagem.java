package br.ucsal.vitalis.clinico.triagem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "triagem")
public class Triagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "atendimento_id", nullable = false)
    private Long atendimentoId;

    @Column(name = "enfermeiro_id", nullable = false)
    private Long enfermeiroId;

    @Column(nullable = false, length = 20)
    private String protocolo = "MANCHESTER";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadeManchester prioridade;

    @Column(name = "queixa_principal", nullable = false, length = 2000)
    private String queixaPrincipal;

    @Column(name = "temperatura_c", precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "frequencia_cardiaca")
    private Integer frequenciaCardiaca;

    @Column(name = "frequencia_respiratoria")
    private Integer frequenciaRespiratoria;

    @Column(name = "pressao_sistolica")
    private Integer pressaoSistolica;

    @Column(name = "pressao_diastolica")
    private Integer pressaoDiastolica;

    @Column(name = "saturacao_o2", precision = 5, scale = 2)
    private BigDecimal saturacaoO2;

    @Column(name = "escala_dor")
    private Integer escalaDor;

    @Column(name = "escala_glasgow")
    private Short escalaGlasgow;

    @Column(name = "oxigenio_suplementar", nullable = false)
    private boolean oxigenioSuplementar;

    @Column(name = "escore_qsofa", nullable = false)
    private short escoreQsofa;

    @Column(name = "escore_news2", nullable = false)
    private short escoreNews2;

    @Column(name = "alerta_sepse", nullable = false)
    private boolean alertaSepse;

    @Column(length = 4000)
    private String observacoes;

    @Column(name = "realizada_em", nullable = false, updatable = false)
    private OffsetDateTime realizadaEm = OffsetDateTime.now();

    protected Triagem() {
    }

    public Triagem(Long atendimentoId,
                   Long enfermeiroId,
                   PrioridadeManchester prioridade,
                   String queixaPrincipal,
                   BigDecimal temperaturaC,
                   Integer frequenciaCardiaca,
                   Integer frequenciaRespiratoria,
                   Integer pressaoSistolica,
                   Integer pressaoDiastolica,
                   BigDecimal saturacaoO2,
                   Integer escalaDor,
                   Short escalaGlasgow,
                   boolean oxigenioSuplementar,
                   short escoreQsofa,
                   short escoreNews2,
                   boolean alertaSepse,
                   String observacoes) {
        this.atendimentoId = atendimentoId;
        this.enfermeiroId = enfermeiroId;
        this.prioridade = prioridade;
        this.queixaPrincipal = queixaPrincipal;
        this.temperaturaC = temperaturaC;
        this.frequenciaCardiaca = frequenciaCardiaca;
        this.frequenciaRespiratoria = frequenciaRespiratoria;
        this.pressaoSistolica = pressaoSistolica;
        this.pressaoDiastolica = pressaoDiastolica;
        this.saturacaoO2 = saturacaoO2;
        this.escalaDor = escalaDor;
        this.escalaGlasgow = escalaGlasgow;
        this.oxigenioSuplementar = oxigenioSuplementar;
        this.escoreQsofa = escoreQsofa;
        this.escoreNews2 = escoreNews2;
        this.alertaSepse = alertaSepse;
        this.observacoes = observacoes;
    }

    public Long getId() { return id; }
    public Long getAtendimentoId() { return atendimentoId; }
    public Long getEnfermeiroId() { return enfermeiroId; }
    public String getProtocolo() { return protocolo; }
    public PrioridadeManchester getPrioridade() { return prioridade; }
    public String getQueixaPrincipal() { return queixaPrincipal; }
    public BigDecimal getTemperaturaC() { return temperaturaC; }
    public Integer getFrequenciaCardiaca() { return frequenciaCardiaca; }
    public Integer getFrequenciaRespiratoria() { return frequenciaRespiratoria; }
    public Integer getPressaoSistolica() { return pressaoSistolica; }
    public Integer getPressaoDiastolica() { return pressaoDiastolica; }
    public BigDecimal getSaturacaoO2() { return saturacaoO2; }
    public Integer getEscalaDor() { return escalaDor; }
    public Short getEscalaGlasgow() { return escalaGlasgow; }
    public boolean isOxigenioSuplementar() { return oxigenioSuplementar; }
    public short getEscoreQsofa() { return escoreQsofa; }
    public short getEscoreNews2() { return escoreNews2; }
    public boolean isAlertaSepse() { return alertaSepse; }
    public String getObservacoes() { return observacoes; }
    public OffsetDateTime getRealizadaEm() { return realizadaEm; }
}
