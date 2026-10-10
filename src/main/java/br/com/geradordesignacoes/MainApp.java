package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.service.BackupAutomaticoService;
import br.com.geradordesignacoes.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BackupAutomaticoService backupAutomaticoService;
    private ConnectionFactory.ProductionContext productionContext;

    @Override
    public void start(Stage stage) {

        productionContext =
                ConnectionFactory.openProductionContext();
        try {
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

            // Aplica o estilo visual global da aplicação
            mainView.aplicarEstilo(scene);

            stage.setTitle(
                    "Gerador de Designações"
            );

            stage.setScene(scene);

            stage.show();

            stage.setOnCloseRequest(
                    event -> stop()
            );
        } catch (Throwable failure) {
            try {
                stop();
            } catch (Throwable cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            MainApp.<RuntimeException>rethrowUnchecked(failure);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void rethrowUnchecked(
            Throwable failure
    ) throws T {
        throw (T) failure;
    }

    @Override
    public void stop() {
        Throwable cleanupFailure = null;

        if (backupAutomaticoService != null) {
            try {
                backupAutomaticoService.encerrar();
                backupAutomaticoService = null;
            } catch (Throwable failure) {
                cleanupFailure = failure;
            }
        }

        if (productionContext != null) {
            try {
                productionContext.close();
                productionContext = null;
            } catch (Throwable failure) {
                if (cleanupFailure == null) {
                    cleanupFailure = failure;
                } else {
                    cleanupFailure.addSuppressed(failure);
                }
            }
        }

        if (cleanupFailure != null) {
            MainApp.<RuntimeException>rethrowUnchecked(cleanupFailure);
        }
    }

    public static void main(String[] args) {

        launch(args);
    }
}
