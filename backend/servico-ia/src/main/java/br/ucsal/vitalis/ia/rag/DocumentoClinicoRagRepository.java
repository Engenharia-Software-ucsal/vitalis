package br.ucsal.vitalis.ia.rag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoClinicoRagRepository extends JpaRepository<DocumentoClinicoRag, Long> {

    List<DocumentoClinicoRag> findAll();
}
