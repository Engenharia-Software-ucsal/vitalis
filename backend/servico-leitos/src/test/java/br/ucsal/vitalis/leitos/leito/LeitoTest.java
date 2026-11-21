package br.ucsal.vitalis.leitos.leito;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeitoTest {

    @Test
    void deveBloquearTransicaoDeEmHigienizacaoParaOcupado() {
        Leito leito = new Leito("UTI-03", "UTI Adulto");
        leito.alterarStatus(StatusLeito.EM_HIGIENIZACAO);

        assertThatThrownBy(() -> leito.alterarStatus(StatusLeito.OCUPADO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("higienizacao");
    }

    @Test
    void deveBloquearTransicaoDeEmHigienizacaoParaReservado() {
        Leito leito = new Leito("UTI-03", "UTI Adulto");
        leito.alterarStatus(StatusLeito.EM_HIGIENIZACAO);

        assertThatThrownBy(() -> leito.alterarStatus(StatusLeito.RESERVADO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("higienizacao");
    }

    @Test
    void devePermitirTransicaoDeEmHigienizacaoParaLivre() {
        Leito leito = new Leito("UTI-03", "UTI Adulto");
        leito.alterarStatus(StatusLeito.EM_HIGIENIZACAO);

        leito.alterarStatus(StatusLeito.LIVRE);

        assertThat(leito.getStatus()).isEqualTo(StatusLeito.LIVRE);
    }
}
