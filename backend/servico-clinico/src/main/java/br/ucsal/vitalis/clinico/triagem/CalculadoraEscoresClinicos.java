package br.ucsal.vitalis.clinico.triagem;

import java.math.BigDecimal;

public final class CalculadoraEscoresClinicos {

    public record ResultadoNews2(int escore, boolean parametroCriticoIndividual) {
    }

    private CalculadoraEscoresClinicos() {
    }

    public static int calcularQsofa(Integer fr, Integer pas, Integer glasgow) {
        int score = 0;
        if (fr != null && fr >= 22) {
            score++;
        }
        if (pas != null && pas <= 100) {
            score++;
        }
        if (glasgow != null && glasgow < 15) {
            score++;
        }
        return score;
    }

    public static ResultadoNews2 calcularNews2(Integer fr,
                                               BigDecimal spo2,
                                               boolean oxigenioSuplementar,
                                               Integer pas,
                                               Integer fc,
                                               Integer glasgow,
                                               BigDecimal tempC) {
        int total = 0;
        boolean critico = false;

        int pFr = pontuarFr(fr);
        total += pFr;
        critico |= (pFr == 3);

        int pSpo2 = pontuarSpo2(spo2);
        total += pSpo2;
        critico |= (pSpo2 == 3);

        if (oxigenioSuplementar) {
            total += 2;
        }

        int pPas = pontuarPas(pas);
        total += pPas;
        critico |= (pPas == 3);

        int pFc = pontuarFc(fc);
        total += pFc;
        critico |= (pFc == 3);

        int pGlasgow = pontuarGlasgow(glasgow);
        total += pGlasgow;
        critico |= (pGlasgow == 3);

        int pTemp = pontuarTemperatura(tempC);
        total += pTemp;
        critico |= (pTemp == 3);

        return new ResultadoNews2(total, critico);
    }

    public static boolean isAlertaSepse(int qsofa, int news2, boolean parametroCritico) {
        return qsofa >= 2 || news2 >= 7 || parametroCritico;
    }

    private static int pontuarFr(Integer fr) {
        if (fr == null) return 0;
        if (fr <= 8 || fr >= 25) return 3;
        if (fr >= 21) return 2;
        if (fr <= 11) return 1;
        return 0;
    }

    private static int pontuarSpo2(BigDecimal spo2) {
        if (spo2 == null) return 0;
        double v = spo2.doubleValue();
        if (v <= 91.0) return 3;
        if (v <= 93.0) return 2;
        if (v <= 95.0) return 1;
        return 0;
    }

    private static int pontuarPas(Integer pas) {
        if (pas == null) return 0;
        if (pas <= 90 || pas >= 220) return 3;
        if (pas <= 100) return 2;
        if (pas <= 110) return 1;
        return 0;
    }

    private static int pontuarFc(Integer fc) {
        if (fc == null) return 0;
        if (fc <= 40 || fc >= 131) return 3;
        if (fc >= 111) return 2;
        if (fc <= 50 || fc >= 91) return 1;
        return 0;
    }

    private static int pontuarGlasgow(Integer glasgow) {
        if (glasgow == null) return 0;
        return glasgow < 15 ? 3 : 0;
    }

    private static int pontuarTemperatura(BigDecimal tempC) {
        if (tempC == null) return 0;
        double v = tempC.doubleValue();
        if (v <= 35.0) return 3;
        if (v >= 39.1) return 2;
        if (v <= 36.0 || v >= 38.1) return 1;
        return 0;
    }
}
