package br.ucsal.vitalis.clinico.prescricao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "item_prescricao")
public class ItemPrescricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prescricao_id", nullable = false)
    private Long prescricaoId;

    @Column(name = "medicamento_id", nullable = false)
    private Long medicamentoId;

    @Column(nullable = false, length = 120)
    private String dose;

    @Column(nullable = false, length = 60)
    private String via;

    @Column(nullable = false, length = 120)
    private String frequencia;

    @Column(length = 120)
    private String duracao;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(length = 1000)
    private String instrucoes;

    protected ItemPrescricao() {
    }

    public ItemPrescricao(Long prescricaoId,
                          Long medicamentoId,
                          String dose,
                          String via,
                          String frequencia,
                          String duracao,
                          BigDecimal quantidade,
                          String instrucoes) {
        this.prescricaoId = prescricaoId;
        this.medicamentoId = medicamentoId;
        this.dose = dose;
        this.via = via;
        this.frequencia = frequencia;
        this.duracao = duracao;
        this.quantidade = quantidade;
        this.instrucoes = instrucoes;
    }

    public Long getId() { return id; }
    public Long getPrescricaoId() { return prescricaoId; }
    public Long getMedicamentoId() { return medicamentoId; }
    public String getDose() { return dose; }
    public String getVia() { return via; }
    public String getFrequencia() { return frequencia; }
    public String getDuracao() { return duracao; }
    public BigDecimal getQuantidade() { return quantidade; }
    public String getInstrucoes() { return instrucoes; }
}
