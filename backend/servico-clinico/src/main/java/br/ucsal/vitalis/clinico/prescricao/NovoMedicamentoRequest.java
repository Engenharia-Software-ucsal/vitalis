package br.ucsal.vitalis.clinico.prescricao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NovoMedicamentoRequest(
        @NotBlank @Size(max = 200) String nome,
        @Size(max = 200) String principioAtivo,
        @NotBlank @Size(max = 40) String unidade
) {
}
