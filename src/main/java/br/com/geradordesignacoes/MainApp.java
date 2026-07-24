package br.com.geradordesignacoes;

import br.com.geradordesignacoes.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        MainView mainView = new MainView();

        Scene scene = new Scene(mainView.getView(), 1000, 700);

        stage.setTitle("Gerador de Designações");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}