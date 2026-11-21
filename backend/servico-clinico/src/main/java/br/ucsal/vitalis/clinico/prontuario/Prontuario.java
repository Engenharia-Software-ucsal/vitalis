package br.ucsal.vitalis.clinico.prontuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "prontuario")
public class Prontuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false, unique = true)
    private Long pacienteId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    protected Prontuario() {
    }

    public Prontuario(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Long getId() { return id; }
    public Long getPacienteId() { return pacienteId; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
