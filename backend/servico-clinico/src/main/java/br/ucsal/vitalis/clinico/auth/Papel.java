package br.ucsal.vitalis.clinico.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "papel")
public class Papel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Column(nullable = false, length = 160)
    private String descricao;

    protected Papel() {
    }

    public Papel(String codigo, String descricao) {
        this.codigo = codigo;
        this.descricao = descricao;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
}
