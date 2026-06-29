package model;

import java.util.Objects;

public class Pessoa {
    private final String nome;
    private final String sexo;
    private final boolean podeResponsavel;
    private final boolean podeAjudante;
    private final boolean podeLeitura;
    private final boolean podeDiscurso;

    public Pessoa(
            String nome,
            String sexo,
            boolean podeResponsavel,
            boolean podeAjudante,
            boolean podeLeitura,
            boolean podeDiscurso
    ) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.sexo = Objects.requireNonNull(sexo, "sexo não pode ser nulo");
        this.podeResponsavel = podeResponsavel;
        this.podeAjudante = podeAjudante;
        this.podeLeitura = podeLeitura;
        this.podeDiscurso = podeDiscurso;
    }

    public String getNome() {
        return nome;
    }

    public String getSexo() {
        return sexo;
    }

    public boolean isPodeResponsavel() {
        return podeResponsavel;
    }

    public boolean isPodeAjudante() {
        return podeAjudante;
    }

    public boolean isPodeLeitura() {
        return podeLeitura;
    }

    public boolean isPodeDiscurso() {
        return podeDiscurso;
    }

    @Override
    public String toString() {
        return nome;
    }
}
