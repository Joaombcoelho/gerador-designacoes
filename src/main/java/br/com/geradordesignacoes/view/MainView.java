package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.historico.HistoricoView;
import br.com.geradordesignacoes.view.parte.ParteView;
import br.com.geradordesignacoes.view.pessoa.PessoaView;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.service.PessoaService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import br.com.geradordesignacoes.database.BackupDatabase;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.Alert;
import br.com.geradordesignacoes.database.RestaurarDatabase;
import java.io.File;
import java.io.IOException;

public class MainView {

    private final BorderPane root;
    private final PessoaView pessoaView;
    private final ParteView parteView = new ParteView();
    private final EscalaView escalaView = new EscalaView();
    private final HistoricoView historicoView = new HistoricoView();

    public MainView() {

        root = new BorderPane();

        PessoaDAO pessoaDAO = new PessoaDAO();

        PessoaService pessoaService = new PessoaService(pessoaDAO);

        pessoaView = new PessoaView(pessoaService);

        criarMenu();
        criarTelaInicial();
    }

    private void criarMenu() {

        MenuBar menuBar = new MenuBar();

        // Arquivo
        Menu menuArquivo = new Menu("Arquivo");

        MenuItem itemBackup = new MenuItem("Fazer Backup");
        MenuItem itemRestaurarBackup = new MenuItem("Restaurar Backup");
        MenuItem itemSair = new MenuItem("Sair");

        itemBackup.setOnAction(event -> fazerBackup());
        itemRestaurarBackup.setOnAction(event -> restaurarBackup());

        menuArquivo.getItems().addAll(
                itemBackup,
                itemRestaurarBackup,
                itemSair
        );

        // Cadastros
        Menu menuCadastros = new Menu("Cadastros");
        MenuItem itemPessoas = new MenuItem("Pessoas");
        MenuItem itemPartes = new MenuItem("Partes");

        menuCadastros.getItems().addAll(itemPessoas, itemPartes);

        // Escala
        Menu menuEscala = new Menu("Escala");
        MenuItem itemGerarEscala = new MenuItem("Gerar Escala");
        menuEscala.getItems().add(itemGerarEscala);

        // Histórico
        Menu menuHistorico = new Menu("Histórico");
        MenuItem itemConsultarHistorico = new MenuItem("Consultar Histórico");
        menuHistorico.getItems().add(itemConsultarHistorico);

        // Ajuda
        Menu menuAjuda = new Menu("Ajuda");
        MenuItem itemSobre = new MenuItem("Sobre");
        menuAjuda.getItems().add(itemSobre);

        // Eventos
        itemPessoas.setOnAction(e ->
                mostrarTela(pessoaView.getView()));

        itemPartes.setOnAction(e ->
                mostrarTela(parteView.getView()));

        itemGerarEscala.setOnAction(e ->
                mostrarTela(escalaView.getView()));

        menuHistorico.setOnAction(event -> {

            historicoView.atualizar();

            mostrarTela(
                    historicoView.getView()
            );

        });
        // Adiciona os menus
        menuBar.getMenus().addAll(
                menuArquivo,
                menuCadastros,
                menuEscala,
                menuHistorico,
                menuAjuda
        );

        root.setTop(menuBar);
    }

    private void criarTelaInicial() {

        Label titulo = new Label("Bem-vindo ao Gerador de Designações");

        StackPane painel = new StackPane(titulo);
        painel.setAlignment(Pos.CENTER);

        root.setCenter(painel);
    }

    private void mostrarTela(Parent view) {

        root.setCenter(null);

        root.setCenter(view);

    }

    public Parent getView() {
        return root;
    }

    private void fazerBackup() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Salvar backup do banco");

        fileChooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Banco SQLite (*.db)",
                                "*.db"
                        )
                );


        File arquivo =
                fileChooser.showSaveDialog(
                        root.getScene().getWindow()
                );


        if (arquivo == null) {
            return;
        }


        try {

            BackupDatabase.criarBackup(
                    arquivo.toPath()
            );


            Alert alerta =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alerta.setTitle("Backup");
            alerta.setHeaderText(null);
            alerta.setContentText(
                    "Backup realizado com sucesso."
            );

            alerta.showAndWait();


        } catch (IOException e) {

            Alert alerta =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alerta.setTitle("Erro");
            alerta.setHeaderText(null);
            alerta.setContentText(
                    "Não foi possível criar o backup.\n"
                            + e.getMessage()
            );

            alerta.showAndWait();
        }
    }

    private void restaurarBackup() {

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle(
                "Selecionar backup do banco"
        );


        fileChooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Banco SQLite (*.db)",
                                "*.db"
                        )
                );


        File arquivo =
                fileChooser.showOpenDialog(
                        root.getScene().getWindow()
                );


        if (arquivo == null) {
            return;
        }


        Alert confirmacao =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        confirmacao.setTitle(
                "Restaurar backup"
        );

        confirmacao.setHeaderText(
                "Atenção: os dados atuais serão substituídos."
        );

        confirmacao.setContentText(
                "Deseja realmente restaurar este backup?"
        );


        confirmacao.showAndWait()
                .ifPresent(resposta -> {

                    if (resposta == javafx.scene.control.ButtonType.OK) {

                        executarRestauracao(
                                arquivo
                        );
                    }

                });
    }

    private void executarRestauracao(File arquivo) {

        try {

            RestaurarDatabase.restaurar(
                    arquivo.toPath()
            );


            Alert alerta =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );


            alerta.setTitle(
                    "Restauração concluída"
            );

            alerta.setHeaderText(null);

            alerta.setContentText(
                    "Backup restaurado com sucesso.\n"
                            + "Reinicie a aplicação para carregar os dados."
            );


            alerta.showAndWait();


        } catch (IOException e) {


            Alert alerta =
                    new Alert(
                            Alert.AlertType.ERROR
                    );


            alerta.setTitle(
                    "Erro na restauração"
            );

            alerta.setHeaderText(null);

            alerta.setContentText(
                    e.getMessage()
            );


            alerta.showAndWait();
        }
    }

}