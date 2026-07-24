package br.com.geradordesignacoes.view.escala;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class EscalaView {

    private final StackPane root;

    public EscalaView() {

        root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Gerar Escala");

        root.getChildren().add(titulo);
    }

    public Parent getView() {
        return root;
    }

}