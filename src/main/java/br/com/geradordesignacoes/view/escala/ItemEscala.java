package br.com.geradordesignacoes.view.escala;

public class ItemEscala {

    private final int indiceDesignacao;

    private final String parte;

    private final String responsavel;

    private final String ajudante;


    public ItemEscala(
            int indiceDesignacao,
            String parte,
            String responsavel,
            String ajudante
    ) {

        this.indiceDesignacao = indiceDesignacao;
        this.parte = parte;
        this.responsavel = responsavel;
        this.ajudante = ajudante;
    }


    public int getIndiceDesignacao() {
        return indiceDesignacao;
    }


    public String getParte() {
        return parte;
    }


    public String getResponsavel() {
        return responsavel;
    }


    public String getAjudante() {
        return ajudante;
    }
}