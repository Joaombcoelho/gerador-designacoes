package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.service.BackupAutomaticoService;
import br.com.geradordesignacoes.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BackupAutomaticoService backupAutomaticoService;

    @Override
    public void start(Stage stage) {

        DatabaseInitializer.initialize();

        backupAutomaticoService =
                new BackupAutomaticoService();

        backupAutomaticoService.iniciar();

        MainView mainView =
                new MainView();

        Scene scene =
                new Scene(
                        mainView.getView(),
                        1000,
                        700
                );

        stage.setTitle(
                "Gerador de Designações"
        );

        stage.setScene(scene);

        stage.show();

        stage.setOnCloseRequest(event -> backupAutomaticoService.encerrar());
    }

    public static void main(String[] args) {

        launch(args);
    }
}