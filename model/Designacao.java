package model;

import java.time.LocalDate;
import java.util.Objects;

public class Designacao {
    private final LocalDate data;
    private final Parte parte;
    private final Pessoa pessoa;

    public Designacao(LocalDate data, Parte parte, Pessoa pessoa) {
        this.data = Objects.requireNonNull(data, "data não pode ser nula");
        this.parte = Objects.requireNonNull(parte, "parte não pode ser nula");
        this.pessoa = Objects.requireNonNull(pessoa, "pessoa não pode ser nula");
    }

    public LocalDate getData() {
        return data;
    }

    public Parte getParte() {
        return parte;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    @Override
    public String toString() {
        return data + " - " + parte + ": " + pessoa;
    }
}
