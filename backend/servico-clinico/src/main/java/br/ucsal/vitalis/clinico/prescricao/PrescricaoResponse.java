package br.ucsal.vitalis.clinico.prescricao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record PrescricaoResponse(
        Long id,
        Long atendimentoId,
        Long prescritorId,
        StatusPrescricao status,
        boolean alergiasReconhecidas,
        OffsetDateTime criadaEm,
        List<ItemPrescricaoResponse> itens
) {
    public record ItemPrescricaoResponse(
            Long id,
            Long medicamentoId,
            String medicamentoNome,
            String dose,
            String via,
            String frequencia,
            String duracao,
            BigDecimal quantidade,
            String instrucoes
    ) {
    }
}
