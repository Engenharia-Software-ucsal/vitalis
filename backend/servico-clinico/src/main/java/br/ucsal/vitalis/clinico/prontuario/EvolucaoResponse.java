package br.ucsal.vitalis.clinico.prontuario;

import java.time.OffsetDateTime;

public record EvolucaoResponse(
        Long id,
        Long prontuarioId,
        Long atendimentoId,
        Long autorId,
        String autorNome,
        String texto,
        OffsetDateTime criadaEm
) {
}
