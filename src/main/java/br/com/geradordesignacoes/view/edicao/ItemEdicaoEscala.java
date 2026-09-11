package br.com.geradordesignacoes.view.edicao;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ItemEdicaoEscala {

    private final StringProperty parte;
    private final StringProperty responsavel;
    private final StringProperty ajudante;


    public ItemEdicaoEscala(
            String parte,
            String responsavel,
            String ajudante
    ) {

        this.parte =
                new SimpleStringProperty(
                        parte
                );

        this.responsavel =
                new SimpleStringProperty(
                        responsavel
                );

        this.ajudante =
                new SimpleStringProperty(
                        ajudante
                );
    }


    public String getParte() {

        return parte.get();
    }


    public StringProperty parteProperty() {

        return parte;
    }


    public String getResponsavel() {

        return responsavel.get();
    }


    public StringProperty responsavelProperty() {

        return responsavel;
    }


    public String getAjudante() {

        return ajudante.get();
    }


    public StringProperty ajudanteProperty() {

        return ajudante;
    }
}