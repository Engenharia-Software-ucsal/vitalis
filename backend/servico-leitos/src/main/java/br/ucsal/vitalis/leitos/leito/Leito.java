package br.ucsal.vitalis.leitos.leito;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "leito")
public class Leito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String ala;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusLeito status = StatusLeito.LIVRE;

    protected Leito() {
    }

    public Leito(String codigo, String ala) {
        this.codigo = codigo;
        this.ala = ala;
    }

    public void alterarStatus(StatusLeito novo) {
        // Leito em higienizacao precisa ser liberado pela hotelaria antes de receber paciente.
        if (status == StatusLeito.EM_HIGIENIZACAO
                && (novo == StatusLeito.OCUPADO || novo == StatusLeito.RESERVADO)) {
            throw new IllegalStateException("leito em higienizacao deve ser liberado antes de ser ocupado");
        }
        this.status = novo;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getAla() { return ala; }
    public StatusLeito getStatus() { return status; }
}
