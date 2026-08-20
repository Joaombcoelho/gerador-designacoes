package br.com.geradordesignacoes.model;

import java.util.Objects;

public class ProgramacaoParte {

    private final Integer id;

    private final Parte parte;

    private final int ordem;

    private String tema;


    public ProgramacaoParte(
            Integer id,
            Parte parte,
            int ordem,
            String tema
    ) {

        this.id = id;

        this.parte =
                Objects.requireNonNull(parte);

        this.ordem = ordem;

        this.tema = tema;
    }


    public ProgramacaoParte(
            Parte parte,
            int ordem,
            String tema
    ) {

        this(
                null,
                parte,
                ordem,
                tema
        );
    }


    public ProgramacaoParte(
            Parte parte,
            int ordem
    ) {

        this(
                null,
                parte,
                ordem,
                null
        );
    }


    public Integer getId() {
        return id;
    }


    public Parte getParte() {
        return parte;
    }


    public int getOrdem() {
        return ordem;
    }


    public String getTema() {
        return tema;
    }


    public void setTema(String tema) {
        this.tema = tema;
    }


    public boolean possuiTema() {

        return tema != null
                && !tema.isBlank();
    }


    @Override
    public String toString() {

        return "ProgramacaoParte{" +
                "id=" + id +
                ", parte=" + parte.getNome() +
                ", ordem=" + ordem +
                ", tema='" + tema + '\'' +
                '}';
    }
}