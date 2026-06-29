package model;

import java.util.Objects;

public class Pessoa {
    private final String nome;
    private final Sexo sexo;
    private final boolean ativo;
    private final boolean podeSerResponsavel;
    private final boolean podeSerAjudante;
    private final boolean podeFazerLeitura;
    private final boolean podeFazerDiscurso;

    public Pessoa(
            String nome,
            Sexo sexo,
            boolean ativo,
            boolean podeSerResponsavel,
            boolean podeSerAjudante,
            boolean podeFazerLeitura,
            boolean podeFazerDiscurso
    ) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.sexo = Objects.requireNonNull(sexo, "sexo não pode ser nulo");
        this.ativo = ativo;
        this.podeSerResponsavel = podeSerResponsavel;
        this.podeSerAjudante = podeSerAjudante;
        this.podeFazerLeitura = podeFazerLeitura;
        this.podeFazerDiscurso = podeFazerDiscurso;
    }

    public String getNome() {
        return nome;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean podeSerResponsavel() {
        return podeSerResponsavel;
    }

    public boolean podeSerAjudante() {
        return podeSerAjudante;
    }

    public boolean podeFazerLeitura() {
        return podeFazerLeitura;
    }

    public boolean podeFazerDiscurso() {
        return podeFazerDiscurso;
    }

    @Override
    public String toString() {
        return nome;
    }
}
