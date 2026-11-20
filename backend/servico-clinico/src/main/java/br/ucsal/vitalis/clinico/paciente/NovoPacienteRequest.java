package br.ucsal.vitalis.clinico.paciente;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record NovoPacienteRequest(
        @NotBlank String nome,
        @NotBlank @CPF @Pattern(regexp = "\\d{11}", message = "informe apenas os 11 digitos") String cpf,
        @Pattern(regexp = "\\d{15}", message = "CNS deve ter 15 digitos") String cns,
        @NotNull @Past LocalDate dataNascimento
) {
}
