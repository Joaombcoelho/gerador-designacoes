package br.com.geradordesignacoes.model;

import java.util.Objects;

public class Pessoa {
    private Integer id;
    private final String nome;
    private final Sexo sexo;
    private final boolean ativo;
    private final boolean podeSerResponsavel;
    private final boolean podeSerAjudante;
    private final boolean podeFazerLeitura;
    private final boolean podeFazerDiscurso;
    private final Privilegio privilegio;

    public Pessoa(
            String nome,
            Sexo sexo,
            boolean ativo,
            boolean podeSerResponsavel,
            boolean podeSerAjudante,
            boolean podeFazerLeitura,
            boolean podeFazerDiscurso,
            Privilegio privilegio
    ) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.sexo = Objects.requireNonNull(sexo, "sexo não pode ser nulo");
        this.ativo = ativo;
        this.podeSerResponsavel = podeSerResponsavel;
        this.podeSerAjudante = podeSerAjudante;
        this.podeFazerLeitura = podeFazerLeitura;
        this.podeFazerDiscurso = podeFazerDiscurso;
        this.privilegio = Objects.requireNonNull(privilegio, "privilégio não pode ser nulo");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Privilegio getPrivilegio() {
        return privilegio;
    }

    @Override
    public String toString() {
        return nome;
    }

    public boolean podeExercer(TipoParticipacao tipo) {

        return switch (tipo) {
            case RESPONSAVEL -> podeSerResponsavel;
            case AJUDANTE -> podeSerAjudante;
            case LEITOR -> podeFazerLeitura;
            case ORADOR -> podeFazerDiscurso;
            case DIRIGENTE -> podeSerResponsavel;
            case PRESIDENTE -> podeSerResponsavel;
        };
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof Pessoa outra)) {
            return false;
        }

        if (id == null || outra.id == null) {
            return false;
        }

        return id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
