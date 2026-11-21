package br.ucsal.vitalis.clinico.prescricao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record NovaPrescricaoRequest(
        boolean alergiasReconhecidas,
        @NotEmpty @Valid List<NovoItemPrescricaoRequest> itens
) {
    public record NovoItemPrescricaoRequest(
            @NotNull Long medicamentoId,
            @NotBlank @Size(max = 120) String dose,
            @NotBlank @Size(max = 60) String via,
            @NotBlank @Size(max = 120) String frequencia,
            @Size(max = 120) String duracao,
            @NotNull @DecimalMin("0.001") BigDecimal quantidade,
            @Size(max = 1000) String instrucoes
    ) {
    }
}
