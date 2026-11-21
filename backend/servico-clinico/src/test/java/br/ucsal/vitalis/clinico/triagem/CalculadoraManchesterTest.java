package br.ucsal.vitalis.clinico.triagem;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class CalculadoraManchesterTest {

    @Test
    void deveClassificarVermelhaEmSinaisCriticos() {
        PrioridadeManchester p = CalculadoraManchester.calcular(
                new BigDecimal("87.0"), 115, 18, 90, 15, new BigDecimal("36.8"), 2, 0, 3
        );
        assertThat(p).isEqualTo(PrioridadeManchester.VERMELHA);
    }

    @Test
    void deveClassificarLaranjaComDorIntensaOuQsofaElevado() {
        PrioridadeManchester p = CalculadoraManchester.calcular(
                new BigDecimal("96.0"), 115, 18, 88, 15, new BigDecimal("37.0"), 9, 0, 0
        );
        assertThat(p).isEqualTo(PrioridadeManchester.LARANJA);
    }

    @Test
    void deveClassificarAmarelaVerdeEAzul() {
        assertThat(CalculadoraManchester.calcular(
                new BigDecimal("97.0"), 120, 18, 90, 15, new BigDecimal("38.8"), 4, 0, 1
        )).isEqualTo(PrioridadeManchester.AMARELA);

        assertThat(CalculadoraManchester.calcular(
                new BigDecimal("98.0"), 120, 16, 80, 15, new BigDecimal("37.6"), 2, 0, 0
        )).isEqualTo(PrioridadeManchester.VERDE);

        assertThat(CalculadoraManchester.calcular(
                new BigDecimal("99.0"), 120, 16, 72, 15, new BigDecimal("36.5"), 0, 0, 0
        )).isEqualTo(PrioridadeManchester.AZUL);
    }
}
