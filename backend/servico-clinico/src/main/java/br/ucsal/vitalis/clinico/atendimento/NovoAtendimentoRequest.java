package br.ucsal.vitalis.clinico.atendimento;

import jakarta.validation.constraints.NotNull;

public record NovoAtendimentoRequest(
        @NotNull Long pacienteId,
        @NotNull TipoAtendimento tipo
) {
}
