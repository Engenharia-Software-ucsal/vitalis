package br.ucsal.vitalis.clinico.prontuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "evolucao_clinica")
public class EvolucaoClinica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prontuario_id", nullable = false)
    private Long prontuarioId;

    @Column(name = "atendimento_id")
    private Long atendimentoId;

    @Column(name = "profissional_id", nullable = false)
    private Long profissionalId;

    @Column(nullable = false, length = 8000)
    private String texto;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private OffsetDateTime criadaEm = OffsetDateTime.now();

    @Column(name = "assinatura_ref", length = 500)
    private String assinaturaRef;

    @Column(name = "assinada_em")
    private OffsetDateTime assinadaEm;

    protected EvolucaoClinica() {
    }

    public EvolucaoClinica(Long prontuarioId, Long atendimentoId, Long profissionalId, String texto) {
        this.prontuarioId = prontuarioId;
        this.atendimentoId = atendimentoId;
        this.profissionalId = profissionalId;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public Long getProntuarioId() { return prontuarioId; }
    public Long getAtendimentoId() { return atendimentoId; }
    public Long getProfissionalId() { return profissionalId; }
    public String getTexto() { return texto; }
    public OffsetDateTime getCriadaEm() { return criadaEm; }
    public String getAssinaturaRef() { return assinaturaRef; }
    public OffsetDateTime getAssinadaEm() { return assinadaEm; }
}
