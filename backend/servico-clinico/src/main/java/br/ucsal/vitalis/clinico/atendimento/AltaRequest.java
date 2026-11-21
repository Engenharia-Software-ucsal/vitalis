package br.ucsal.vitalis.clinico.atendimento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AltaRequest(
        @NotBlank @Size(max = 500) String motivoEncerramento
) {
}
