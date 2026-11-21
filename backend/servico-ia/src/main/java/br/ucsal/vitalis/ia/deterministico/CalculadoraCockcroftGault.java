package br.ucsal.vitalis.ia.deterministico;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Formula classica de Cockcroft-Gault (Secao 10.2.4):
 * ClCr = ((140 - idade) * peso) / (72 * creatinina) * (0.85 se feminino)
 */
public final class CalculadoraCockcroftGault {

    private CalculadoraCockcroftGault() {
    }

    public static BigDecimal calcular(Integer idadeAnos, Double pesoKg, Double creatininaSericaMgDl, String sexo) {
        if (idadeAnos == null || pesoKg == null || creatininaSericaMgDl == null
                || idadeAnos <= 0 || pesoKg <= 0 || creatininaSericaMgDl <= 0) {
            return null;
        }
        double clcr = ((140.0 - idadeAnos) * pesoKg) / (72.0 * creatininaSericaMgDl);
        if (sexo != null && sexo.trim().equalsIgnoreCase("F")) {
            clcr *= 0.85;
        }
        return BigDecimal.valueOf(Math.max(0.0, clcr)).setScale(1, RoundingMode.HALF_UP);
    }

    public static boolean exigeAjusteRenal(BigDecimal clcr) {
        return clcr != null && clcr.doubleValue() < 50.0;
    }
}
