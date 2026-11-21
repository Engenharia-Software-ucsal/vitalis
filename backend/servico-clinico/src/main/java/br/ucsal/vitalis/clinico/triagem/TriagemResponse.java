package br.ucsal.vitalis.clinico.triagem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TriagemResponse(
        Long id,
        Long atendimentoId,
        Long enfermeiroId,
        String protocolo,
        PrioridadeManchester prioridade,
        String queixaPrincipal,
        BigDecimal temperaturaC,
        Integer frequenciaCardiaca,
        Integer frequenciaRespiratoria,
        Integer pressaoSistolica,
        Integer pressaoDiastolica,
        BigDecimal saturacaoO2,
        Integer escalaDor,
        Short escalaGlasgow,
        boolean oxigenioSuplementar,
        short escoreQsofa,
        short escoreNews2,
        boolean alertaSepse,
        String observacoes,
        OffsetDateTime realizadaEm
) {
    public static TriagemResponse de(Triagem t) {
        return new TriagemResponse(
                t.getId(),
                t.getAtendimentoId(),
                t.getEnfermeiroId(),
                t.getProtocolo(),
                t.getPrioridade(),
                t.getQueixaPrincipal(),
                t.getTemperaturaC(),
                t.getFrequenciaCardiaca(),
                t.getFrequenciaRespiratoria(),
                t.getPressaoSistolica(),
                t.getPressaoDiastolica(),
                t.getSaturacaoO2(),
                t.getEscalaDor(),
                t.getEscalaGlasgow(),
                t.isOxigenioSuplementar(),
                t.getEscoreQsofa(),
                t.getEscoreNews2(),
                t.isAlertaSepse(),
                t.getObservacoes(),
                t.getRealizadaEm()
        );
    }
}
