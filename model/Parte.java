package model;

import java.util.Objects;

public class Parte {
    private final String nome;

    public Parte(String nome) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
