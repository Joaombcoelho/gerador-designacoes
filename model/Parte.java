package model;

import java.util.Objects;

public class Parte {
    private final String nome;
    private final TipoParte tipo;

    public Parte(String nome, TipoParte tipo) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "tipo não pode ser nulo");
    }

    public String getNome() {
        return nome;
    }

    public TipoParte getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return nome;
    }
}
