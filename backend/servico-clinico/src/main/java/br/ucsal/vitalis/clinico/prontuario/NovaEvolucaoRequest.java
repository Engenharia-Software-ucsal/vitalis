package br.ucsal.vitalis.clinico.prontuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaEvolucaoRequest(
        @NotNull Long atendimentoId,
        @NotBlank @Size(max = 8000) String texto
) {
}
