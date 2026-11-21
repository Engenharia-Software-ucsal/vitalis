package br.ucsal.vitalis.ia.copiloto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SumarioAltaRequest(
        @NotNull Long atendimentoId,
        String nomePaciente,
        Integer idadeAnos,
        String queixaPrincipal,
        List<String> evolucoes,
        List<String> prescricoes
) {
}
