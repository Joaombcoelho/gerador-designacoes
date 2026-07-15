package br.com.geradordesignacoes.model;

public enum Privilegio {

    PUBLICADOR(1),
    BATIZADO(2),
    SERVO_MINISTERIAL(3),
    ANCIAO(4);

    private final int nivel;

    Privilegio(int nivel) {
        this.nivel = nivel;
    }

    public int getNivel() {
        return nivel;
    }

    public boolean atende(Privilegio privilegioMinimo) {
        return this.nivel >= privilegioMinimo.nivel;
    }
}