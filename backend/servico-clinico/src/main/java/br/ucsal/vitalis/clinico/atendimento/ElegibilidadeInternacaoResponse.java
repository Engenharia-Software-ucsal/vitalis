package br.ucsal.vitalis.clinico.atendimento;

public record ElegibilidadeInternacaoResponse(
        Long atendimentoId,
        boolean elegivel,
        boolean possuiTriagem,
        StatusAtendimento status,
        String motivo
) {
}
