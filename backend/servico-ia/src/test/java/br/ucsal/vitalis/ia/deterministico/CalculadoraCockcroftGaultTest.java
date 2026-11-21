package br.ucsal.vitalis.ia.deterministico;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraCockcroftGaultTest {

    @Test
    void deveCalcularClearanceCreatininaCorretamenteParaMasculinoEFeminino() {
        // Exemplo da Secao 10: M, 68 anos, 72 kg, Cr 2.4 -> ((140-68)*72)/(72*2.4) = 30.0 mL/min
        BigDecimal clcrM = CalculadoraCockcroftGault.calcular(68, 72.0, 2.4, "M");
        assertThat(clcrM).isEqualByComparingTo("30.0");
        assertThat(CalculadoraCockcroftGault.exigeAjusteRenal(clcrM)).isTrue();

        BigDecimal clcrF = CalculadoraCockcroftGault.calcular(68, 72.0, 2.4, "F");
        assertThat(clcrF).isEqualByComparingTo("25.5");
        assertThat(CalculadoraCockcroftGault.exigeAjusteRenal(clcrF)).isTrue();

        BigDecimal clcrNormal = CalculadoraCockcroftGault.calcular(30, 70.0, 0.9, "M");
        assertThat(CalculadoraCockcroftGault.exigeAjusteRenal(clcrNormal)).isFalse();
    }
}
