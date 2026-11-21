package br.ucsal.vitalis.clinico.atendimento;

import br.ucsal.vitalis.clinico.triagem.PrioridadeManchester;

import java.time.OffsetDateTime;

public record ItemFilaResponse(
        Long atendimentoId,
        Long pacienteId,
        String pacienteNome,
        String pacienteCpf,
        TipoAtendimento tipo,
        StatusAtendimento status,
        OffsetDateTime iniciadoEm,
        PrioridadeManchester prioridade,
        Short escoreQsofa,
        Short escoreNews2,
        boolean alertaSepse,
        boolean possuiTriagem,
        String queixaPrincipal
) {
}
