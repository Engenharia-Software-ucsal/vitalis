package br.ucsal.vitalis.clinico.prescricao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "medicamento")
public class Medicamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(name = "principio_ativo", length = 200)
    private String principioAtivo;

    @Column(nullable = false, length = 40)
    private String unidade;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Medicamento() {
    }

    public Medicamento(String nome, String principioAtivo, String unidade) {
        this.nome = nome;
        this.principioAtivo = principioAtivo;
        this.unidade = unidade;
        this.ativo = true;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getPrincipioAtivo() { return principioAtivo; }
    public String getUnidade() { return unidade; }
    public boolean isAtivo() { return ativo; }
}
