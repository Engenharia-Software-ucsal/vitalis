package br.ucsal.vitalis.clinico.alergia;

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
@Table(name = "alergia")
public class Alergia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false, length = 160)
    private String substancia;

    @Column(length = 1000)
    private String reacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GravidadeAlergia gravidade;

    @Column(nullable = false)
    private boolean ativa = true;

    @Column(name = "registrada_por", nullable = false)
    private Long registradaPor;

    @Column(name = "registrada_em", nullable = false, updatable = false)
    private OffsetDateTime registradaEm = OffsetDateTime.now();

    protected Alergia() {
    }

    public Alergia(Long pacienteId, String substancia, String reacao, GravidadeAlergia gravidade, Long registradaPor) {
        this.pacienteId = pacienteId;
        this.substancia = substancia;
        this.reacao = reacao;
        this.gravidade = gravidade;
        this.ativa = true;
        this.registradaPor = registradaPor;
    }

    public void inativar() {
        this.ativa = false;
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public String getSubstancia() { return substancia; }
    public String getReacao() { return reacao; }
    public GravidadeAlergia getGravidade() { return gravidade; }
    public boolean isAtiva() { return ativa; }
    public Long getRegistradaPor() { return registradaPor; }
    public OffsetDateTime getRegistradaEm() { return registradaEm; }
}
