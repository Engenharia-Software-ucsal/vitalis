package br.ucsal.vitalis.ia.rag;

import java.util.List;

/**
 * Porta agnostica para recuperacao de conhecimento clinico (RAG).
 * Implementacao padrao usa busca lexical em PostgreSQL; pode ser trocada por pgvector/Qdrant sem alterar o dominio.
 */
public interface KnowledgeRetriever {

    record TrechoClinico(String documento, String fonteOficial, String secao, String trecho) {
    }

    List<TrechoClinico> buscar(List<String> termos, int topK);
}
