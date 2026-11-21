package br.ucsal.vitalis.clinico.alergia;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NovaAlergiaRequest(
        @NotBlank @Size(max = 160) String substancia,
        @Size(max = 1000) String reacao,
        @NotNull GravidadeAlergia gravidade
) {
}
