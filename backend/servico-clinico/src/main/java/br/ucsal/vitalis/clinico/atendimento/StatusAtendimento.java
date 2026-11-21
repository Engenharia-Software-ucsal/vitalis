package br.ucsal.vitalis.clinico.atendimento;

public enum StatusAtendimento {
    ABERTO,
    EM_ANDAMENTO,
    ALTA,
    CANCELADO;

    public boolean isEncerrado() {
        return this == ALTA || this == CANCELADO;
    }
}
