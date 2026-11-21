package br.ucsal.vitalis.clinico.paciente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "contato_emergencia")
public class ContatoEmergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 60)
    private String parentesco;

    @Column(nullable = false, length = 30)
    private String telefone;

    @Column(nullable = false)
    private boolean principal;

    protected ContatoEmergencia() {
    }

    public ContatoEmergencia(Long pacienteId, String nome, String parentesco, String telefone, boolean principal) {
        this.pacienteId = pacienteId;
        this.nome = nome;
        this.parentesco = parentesco;
        this.telefone = telefone;
        this.principal = principal;
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public String getNome() { return nome; }
    public String getParentesco() { return parentesco; }
    public String getTelefone() { return telefone; }
    public boolean isPrincipal() { return principal; }
}
