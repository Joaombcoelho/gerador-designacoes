package br.com.geradordesignacoes.view.parte;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ParteView {

    private final StackPane root;

    public ParteView() {

        root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Cadastro de Partes");

        root.getChildren().add(titulo);
    }

    public Parent getView() {
        return root;
    }

}