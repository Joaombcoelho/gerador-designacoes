package br.com.geradordesignacoes.view.historico;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class HistoricoView {

    private final StackPane root;

    public HistoricoView() {

        root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Histórico de Designações");

        root.getChildren().add(titulo);
    }

    public Parent getView() {
        return root;
    }

}