package br.com.geradordesignacoes.view.escala;

public class ItemEscala {

    private final String parte;

    private final String responsavel;

    private final String ajudante;

    public ItemEscala(
            String parte,
            String responsavel,
            String ajudante) {

        this.parte = parte;
        this.responsavel = responsavel;
        this.ajudante = ajudante;
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