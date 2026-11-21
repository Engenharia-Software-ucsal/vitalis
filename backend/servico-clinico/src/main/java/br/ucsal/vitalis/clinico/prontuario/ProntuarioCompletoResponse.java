package br.ucsal.vitalis.clinico.prontuario;

import br.ucsal.vitalis.clinico.alergia.Alergia;
import br.ucsal.vitalis.clinico.atendimento.Atendimento;
import br.ucsal.vitalis.clinico.paciente.Paciente;
import br.ucsal.vitalis.clinico.prescricao.PrescricaoResponse;
import br.ucsal.vitalis.clinico.triagem.TriagemResponse;

import java.util.List;

public record ProntuarioCompletoResponse(
        Long prontuarioId,
        Paciente paciente,
        List<Alergia> alergias,
        List<Atendimento> atendimentos,
        List<TriagemResponse> triagens,
        List<EvolucaoResponse> evolucoes,
        List<PrescricaoResponse> prescricoes
) {
}
