package br.ucsal.vitalis.clinico.triagem;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraEscoresClinicosTest {

    @Test
    void deveCalcularQsofaDeZeroATres() {
        assertThat(CalculadoraEscoresClinicos.calcularQsofa(16, 120, 15)).isEqualTo(0);
        assertThat(CalculadoraEscoresClinicos.calcularQsofa(24, 120, 15)).isEqualTo(1);
        assertThat(CalculadoraEscoresClinicos.calcularQsofa(24, 95, 15)).isEqualTo(2);
        assertThat(CalculadoraEscoresClinicos.calcularQsofa(26, 88, 12)).isEqualTo(3);
    }

    @Test
    void deveCalcularNews2NormalSemAlerta() {
        var res = CalculadoraEscoresClinicos.calcularNews2(
                16, new BigDecimal("98.0"), false, 120, 75, 15, new BigDecimal("36.8")
        );
        assertThat(res.escore()).isEqualTo(0);
        assertThat(res.parametroCriticoIndividual()).isFalse();
        assertThat(CalculadoraEscoresClinicos.isAlertaSepse(0, res.escore(), res.parametroCriticoIndividual())).isFalse();
    }

    @Test
    void deveDispararAlertaSepseQuandoParametroIndividualForTresOuNews2Critico() {
        var resCritico = CalculadoraEscoresClinicos.calcularNews2(
                26, new BigDecimal("90.0"), true, 88, 132, 13, new BigDecimal("39.5")
        );
        assertThat(resCritico.escore()).isGreaterThanOrEqualTo(7);
        assertThat(resCritico.parametroCriticoIndividual()).isTrue();
        assertThat(CalculadoraEscoresClinicos.isAlertaSepse(3, resCritico.escore(), resCritico.parametroCriticoIndividual())).isTrue();

        var resParametroUnico = CalculadoraEscoresClinicos.calcularNews2(
                16, new BigDecimal("98.0"), false, 120, 75, 13, new BigDecimal("36.8")
        );
        assertThat(resParametroUnico.escore()).isEqualTo(3);
        assertThat(resParametroUnico.parametroCriticoIndividual()).isTrue();
        assertThat(CalculadoraEscoresClinicos.isAlertaSepse(1, resParametroUnico.escore(), resParametroUnico.parametroCriticoIndividual())).isTrue();
    }
}
