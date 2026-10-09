package br.com.geradordesignacoes.model;

import java.util.Objects;

public class ProgramacaoParte {

    private final Integer id;
    private final Parte parte;
    private final int ordem;
    private String tema;

    /*
     * Número oficial da parte na programação semanal.
     * É diferente da ordem interna utilizada pelo sistema.
     */
    private Integer numeroOficial;

    public ProgramacaoParte(
            Integer id,
            Parte parte,
            int ordem,
            String tema
    ) {
        this(id, parte, ordem, tema, null);
    }

    public ProgramacaoParte(
            Integer id,
            Parte parte,
            int ordem,
            String tema,
            Integer numeroOficial
    ) {
        this.id = id;
        this.parte = Objects.requireNonNull(parte);
        this.ordem = ordem;
        this.tema = tema;
        setNumeroOficial(numeroOficial);
    }

    public ProgramacaoParte(
            Parte parte,
            int ordem,
            String tema
    ) {
        this(null, parte, ordem, tema, null);
    }

    public ProgramacaoParte(
            Parte parte,
            int ordem
    ) {
        this(null, parte, ordem, null, null);
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

    public Integer getNumeroOficial() {
        return numeroOficial;
    }

    public void setNumeroOficial(Integer numeroOficial) {
        if (numeroOficial != null && numeroOficial < 1) {
            throw new IllegalArgumentException(
                    "O número oficial deve ser maior que zero."
            );
        }

        this.numeroOficial = numeroOficial;
    }

    public boolean possuiTema() {
        return tema != null && !tema.isBlank();
    }

    @Override
    public String toString() {
        return "ProgramacaoParte{" +
                "id=" + id +
                ", parte=" + parte.getNome() +
                ", ordem=" + ordem +
                ", numeroOficial=" + numeroOficial +
                ", tema='" + tema + '\'' +
                '}';
    }
}
