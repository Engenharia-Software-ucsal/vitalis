package br.ucsal.vitalis.ia.copiloto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AnalisePrescricaoRequest(
        @NotNull Long atendimentoId,
        String nomePaciente,
        Integer idadeAnos,
        String sexo,
        Double pesoKg,
        Double creatininaSericaMgDl,
        List<String> alergiasAtivas,
        List<String> prescricoesVigentes,
        @NotEmpty List<ItemSolicitado> itensSolicitados
) {
    public record ItemSolicitado(
            String medicamento,
            String dose,
            String via,
            String frequencia
    ) {
    }
}
