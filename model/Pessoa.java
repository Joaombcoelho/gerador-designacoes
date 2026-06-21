package model;

import java.util.Objects;

public class Pessoa {
    private final String nome;

    public Pessoa(String nome) {
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
