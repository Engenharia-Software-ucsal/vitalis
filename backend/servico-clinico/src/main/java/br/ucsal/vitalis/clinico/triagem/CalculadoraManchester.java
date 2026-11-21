package br.ucsal.vitalis.clinico.triagem;

import java.math.BigDecimal;

public final class CalculadoraManchester {

    private CalculadoraManchester() {
    }

    public static PrioridadeManchester calcular(BigDecimal spo2,
                                                Integer pas,
                                                Integer fr,
                                                Integer fc,
                                                Integer glasgow,
                                                BigDecimal tempC,
                                                Integer dor,
                                                int qsofa,
                                                int news2) {
        if ((spo2 != null && spo2.doubleValue() < 90.0)
                || (pas != null && pas < 90)
                || (fr != null && (fr < 8 || fr > 30))
                || (fc != null && (fc < 40 || fc > 130))
                || (glasgow != null && glasgow <= 8)
                || news2 >= 7) {
            return PrioridadeManchester.VERMELHA;
        }

        if ((spo2 != null && spo2.doubleValue() < 92.0)
                || (pas != null && pas < 100)
                || (tempC != null && (tempC.doubleValue() >= 40.0 || tempC.doubleValue() < 35.0))
                || (glasgow != null && glasgow < 15)
                || (dor != null && dor >= 8)
                || news2 >= 5
                || qsofa >= 2) {
            return PrioridadeManchester.LARANJA;
        }

        if ((tempC != null && tempC.doubleValue() >= 38.5)
                || (dor != null && dor >= 5)
                || (fc != null && fc > 110)
                || (fr != null && fr > 20)
                || news2 >= 3) {
            return PrioridadeManchester.AMARELA;
        }

        if ((dor != null && dor >= 1)
                || (tempC != null && tempC.doubleValue() >= 37.5)
                || news2 >= 1) {
            return PrioridadeManchester.VERDE;
        }

        return PrioridadeManchester.AZUL;
    }
}
