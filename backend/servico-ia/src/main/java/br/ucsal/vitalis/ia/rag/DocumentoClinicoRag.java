package br.ucsal.vitalis.ia.rag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documento_clinico_rag")
public class DocumentoClinicoRag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 240)
    private String titulo;

    @Column(name = "fonte_oficial", nullable = false, length = 240)
    private String fonteOficial;

    @Column(name = "medicamento_ref", nullable = false, length = 160)
    private String medicamentoRef;

    @Column(nullable = false, length = 80)
    private String secao;

    @Column(nullable = false, columnDefinition = "text")
    private String conteudo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private OffsetDateTime criadoEm = OffsetDateTime.now();

    protected DocumentoClinicoRag() {
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getFonteOficial() { return fonteOficial; }
    public String getMedicamentoRef() { return medicamentoRef; }
    public String getSecao() { return secao; }
    public String getConteudo() { return conteudo; }
    public OffsetDateTime getCriadoEm() { return criadoEm; }
}
