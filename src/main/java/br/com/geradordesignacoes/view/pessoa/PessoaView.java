package br.com.geradordesignacoes.view.pessoa;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class PessoaView {

    private final StackPane root;

    public PessoaView() {

        root = new StackPane();
        root.setAlignment(Pos.CENTER);

        Label titulo = new Label("Cadastro de Pessoas");

        root.getChildren().add(titulo);
    }

    public Parent getView() {
        return root;
    }

}