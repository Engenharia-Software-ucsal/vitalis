package br.ucsal.vitalis.clinico.triagem;

public enum PrioridadeManchester {
    VERMELHA,
    LARANJA,
    AMARELA,
    VERDE,
    AZUL;

    public int ordemGravidade() {
        return ordinal();
    }
}
