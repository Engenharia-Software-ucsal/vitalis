package br.ucsal.vitalis.clinico.triagem;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NovaTriagemRequest(
        @NotBlank @Size(max = 2000) String queixaPrincipal,
        @DecimalMin("25.0") @DecimalMax("45.0") BigDecimal temperaturaC,
        @Min(0) @Max(300) Integer frequenciaCardiaca,
        @Min(0) @Max(100) Integer frequenciaRespiratoria,
        @Min(0) @Max(300) Integer pressaoSistolica,
        @Min(0) @Max(200) Integer pressaoDiastolica,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal saturacaoO2,
        @Min(0) @Max(10) Integer escalaDor,
        @Min(3) @Max(15) Integer escalaGlasgow,
        boolean oxigenioSuplementar,
        PrioridadeManchester prioridadeManual,
        @Size(max = 4000) String observacoes
) {
}
