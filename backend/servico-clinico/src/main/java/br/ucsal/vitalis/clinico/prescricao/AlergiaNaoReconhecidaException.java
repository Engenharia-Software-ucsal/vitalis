package br.ucsal.vitalis.clinico.prescricao;

import br.ucsal.vitalis.clinico.alergia.Alergia;

import java.util.List;

public class AlergiaNaoReconhecidaException extends RuntimeException {

    private final List<Alergia> alergiasAtivas;

    public AlergiaNaoReconhecidaException(List<Alergia> alergiasAtivas) {
        super("RN-008: Paciente possui alergias ativas. Reconhecimento explicito obrigatorio antes de confirmar a prescricao.");
        this.alergiasAtivas = List.copyOf(alergiasAtivas);
    }

    public List<Alergia> getAlergiasAtivas() {
        return alergiasAtivas;
    }
}
