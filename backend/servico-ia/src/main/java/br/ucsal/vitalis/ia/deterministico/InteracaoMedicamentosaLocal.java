package br.ucsal.vitalis.ia.deterministico;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interacao_medicamentosa_local")
public class InteracaoMedicamentosaLocal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "farmaco_a", nullable = false, length = 160)
    private String farmacoA;

    @Column(name = "farmaco_b", nullable = false, length = 160)
    private String farmacoB;

    @Column(nullable = false, length = 10)
    private String categoria;

    @Column(nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(nullable = false, length = 240)
    private String referencia;

    protected InteracaoMedicamentosaLocal() {
    }

    public InteracaoMedicamentosaLocal(String farmacoA, String farmacoB, String categoria, String descricao, String referencia) {
        this.farmacoA = farmacoA;
        this.farmacoB = farmacoB;
        this.categoria = categoria;
        this.descricao = descricao;
        this.referencia = referencia;
    }

    public Long getId() { return id; }
    public String getFarmacoA() { return farmacoA; }
    public String getFarmacoB() { return farmacoB; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getReferencia() { return referencia; }
}
