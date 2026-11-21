package br.ucsal.vitalis.leitos.integracao;

public record ElegibilidadeInternacaoResponse(
        Long atendimentoId,
        boolean elegivel,
        boolean possuiTriagem,
        String status,
        String motivo
) {
}
