package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

public class Designacao {
    private final LocalDate data;
    private final Parte parte;
    private final Pessoa responsavel;
    private final Pessoa ajudante;

    public Designacao(LocalDate data, Parte parte, Pessoa responsavel) {
        this(data, parte, responsavel, null);
    }

    public Designacao(LocalDate data, Parte parte, Pessoa responsavel, Pessoa ajudante) {
        this.data = Objects.requireNonNull(data, "data não pode ser nula");
        this.parte = Objects.requireNonNull(parte, "parte não pode ser nula");
        this.responsavel = Objects.requireNonNull(responsavel, "responsável não pode ser nulo");
        this.ajudante = ajudante;
    }

    public LocalDate getData() {
        return data;
    }

    public Parte getParte() {
        return parte;
    }

    public Pessoa getResponsavel() {
        return responsavel;
    }

    public Pessoa getAjudante() {
        return ajudante;
    }

    @Override
    public String toString() {
        if (ajudante == null) {
            return data + " - " + parte + ": " + responsavel;
        }

        return data + " - " + parte + ": " + responsavel + " / Ajudante: " + ajudante;
    }
}
