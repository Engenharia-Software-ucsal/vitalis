package br.ucsal.vitalis.ia.copiloto;

import br.ucsal.vitalis.ia.auditoria.DecisaoMedica;
import jakarta.validation.constraints.NotNull;

public record DecisaoHitlRequest(
        @NotNull DecisaoMedica decisaoMedica,
        String justificativaHumana
) {
}
