package br.ucsal.vitalis.clinico.paciente;

public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    public static boolean isCpfValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int soma1 = 0;
        for (int i = 0; i < 9; i++) {
            soma1 += (cpf.charAt(i) - '0') * (10 - i);
        }
        int resto1 = (soma1 * 10) % 11;
        int digito1 = (resto1 == 10) ? 0 : resto1;
        if (digito1 != (cpf.charAt(9) - '0')) {
            return false;
        }

        int soma2 = 0;
        for (int i = 0; i < 10; i++) {
            soma2 += (cpf.charAt(i) - '0') * (11 - i);
        }
        int resto2 = (soma2 * 10) % 11;
        int digito2 = (resto2 == 10) ? 0 : resto2;
        return digito2 == (cpf.charAt(10) - '0');
    }

    public static boolean isCnsValido(String cns) {
        return cns == null || cns.matches("\\d{15}");
    }
}
