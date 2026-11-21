package br.ucsal.vitalis.clinico.paciente;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadorCpfTest {

    @Test
    void deveAceitarCpfsValidos() {
        assertThat(ValidadorCpf.isCpfValido("52998224725")).isTrue();
        assertThat(ValidadorCpf.isCpfValido("11144477735")).isTrue();
    }

    @Test
    void deveRejeitarCpfsInvalidosOuRepetidos() {
        assertThat(ValidadorCpf.isCpfValido("11111111111")).isFalse();
        assertThat(ValidadorCpf.isCpfValido("52998224720")).isFalse();
        assertThat(ValidadorCpf.isCpfValido("123")).isFalse();
        assertThat(ValidadorCpf.isCpfValido(null)).isFalse();
    }

    @Test
    void deveValidarCnsOpcionalCom15Digitos() {
        assertThat(ValidadorCpf.isCnsValido(null)).isTrue();
        assertThat(ValidadorCpf.isCnsValido("700000000000001")).isTrue();
        assertThat(ValidadorCpf.isCnsValido("12345")).isFalse();
    }
}
