package br.ucsal.vitalis.clinico.paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovoContatoEmergenciaRequest(
        @NotBlank @Size(max = 160) String nome,
        @Size(max = 60) String parentesco,
        @NotBlank @Size(max = 30) String telefone,
        boolean principal
) {
}
