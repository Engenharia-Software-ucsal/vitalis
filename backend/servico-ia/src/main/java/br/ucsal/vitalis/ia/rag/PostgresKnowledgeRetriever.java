package br.ucsal.vitalis.ia.rag;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class PostgresKnowledgeRetriever implements KnowledgeRetriever {

    private final DocumentoClinicoRagRepository repository;

    public PostgresKnowledgeRetriever(DocumentoClinicoRagRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TrechoClinico> buscar(List<String> termos, int topK) {
        if (termos == null || termos.isEmpty()) {
            return List.of();
        }
        List<String> termosNorm = termos.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(t -> t.toLowerCase(Locale.ROOT).trim())
                .toList();
        if (termosNorm.isEmpty()) {
            return List.of();
        }

        return repository.findAll().stream()
                .map(doc -> new ScoredDoc(doc, pontuar(doc, termosNorm)))
                .filter(sd -> sd.score() > 0)
                .sorted(Comparator.comparingInt(ScoredDoc::score).reversed())
                .limit(Math.max(1, topK))
                .map(sd -> new TrechoClinico(
                        sd.doc().getTitulo(),
                        sd.doc().getFonteOficial(),
                        sd.doc().getSecao(),
                        sd.doc().getConteudo()
                ))
                .toList();
    }

    private static int pontuar(DocumentoClinicoRag doc, List<String> termos) {
        String med = doc.getMedicamentoRef().toLowerCase(Locale.ROOT);
        String titulo = doc.getTitulo().toLowerCase(Locale.ROOT);
        String conteudo = doc.getConteudo().toLowerCase(Locale.ROOT);
        int score = 0;
        for (String termo : termos) {
            if (termo.contains(med) || med.contains(termo)) {
                score += 10;
            }
            for (String palavra : termo.split("\\s+")) {
                if (palavra.length() < 4) continue;
                if (med.contains(palavra)) score += 6;
                if (titulo.contains(palavra)) score += 3;
                if (conteudo.contains(palavra)) score += 1;
            }
        }
        return score;
    }

    private record ScoredDoc(DocumentoClinicoRag doc, int score) {
    }
}
