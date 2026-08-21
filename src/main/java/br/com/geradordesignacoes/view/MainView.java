package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.controller.EscalaController;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.BackupDatabase;
import br.com.geradordesignacoes.database.RestaurarDatabase;
import br.com.geradordesignacoes.service.PessoaService;
import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.historico.HistoricoView;
import br.com.geradordesignacoes.view.parte.ParteView;
import br.com.geradordesignacoes.view.pessoa.PessoaView;
import br.com.geradordesignacoes.controller.ProgramacaoController;
import br.com.geradordesignacoes.view.programacao.ProgramacaoView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

public class MainView {

    private final BorderPane root;

    private final PessoaView pessoaView;
    private final ParteView parteView =
            new ParteView();
    private final EscalaView escalaView =
            new EscalaView();
    private final HistoricoView historicoView =
            new HistoricoView();
    private final ProgramacaoView programacaoView =
            new ProgramacaoView();

    private final EscalaController escalaController;

    private final ProgramacaoController programacaoController;


    public MainView() {

        root = new BorderPane();

        PessoaDAO pessoaDAO =
                new PessoaDAO();

        PessoaService pessoaService =
                new PessoaService(
                        pessoaDAO
                );

        pessoaView =
                new PessoaView(
                        pessoaService
                );

        escalaController =
                new EscalaController(
                        escalaView
                );

        programacaoController =
                new ProgramacaoController(
                        programacaoView,
                        escalaController
                );

        criarMenu();
        criarTelaInicial();
    }


    private void criarMenu() {

        MenuBar menuBar =
                new MenuBar();


        // =====================================================
        // BOTÃO INÍCIO
        // =====================================================

        Button botaoInicio =
                new Button("Início");

        botaoInicio.setPrefHeight(25);

        botaoInicio.setOnAction(
                event -> criarTelaInicial()
        );


        // =====================================================
        // ARQUIVO
        // =====================================================

        Menu menuArquivo =
                new Menu("Arquivo");

        MenuItem itemBackup =
                new MenuItem("Fazer Backup");

        MenuItem itemRestaurarBackup =
                new MenuItem("Restaurar Backup");

        MenuItem itemSair =
                new MenuItem("Sair");


        itemBackup.setOnAction(
                event -> fazerBackup()
        );

        itemRestaurarBackup.setOnAction(
                event -> restaurarBackup()
        );

        itemSair.setOnAction(
                event -> {

                    if (root.getScene() != null) {

                        root.getScene()
                                .getWindow()
                                .hide();
                    }
                }
        );


        menuArquivo.getItems().addAll(
                itemBackup,
                itemRestaurarBackup,
                new SeparatorMenuItem(),
                itemSair
        );


        // =====================================================
        // CADASTROS
        // =====================================================

        Menu menuCadastros =
                new Menu("Cadastros");

        MenuItem itemPessoas =
                new MenuItem("Pessoas");

        MenuItem itemPartes =
                new MenuItem("Partes");


        itemPessoas.setOnAction(
                event ->
                        mostrarTela(
                                pessoaView.getView()
                        )
        );

        itemPartes.setOnAction(
                event ->
                        mostrarTela(
                                parteView.getView()
                        )
        );


        menuCadastros.getItems().addAll(
                itemPessoas,
                itemPartes
        );

        // =====================================================
        // PROGRAMAÇÃO
        // =====================================================

        Menu menuProgramacao =
                new Menu("Programação");

        MenuItem itemProgramacao =
                new MenuItem("Programação Semanal");


        itemProgramacao.setOnAction(
                event ->
                        mostrarTela(
                                programacaoView.getView()
                        )
        );


        menuProgramacao.getItems().add(
                itemProgramacao
        );

        // =====================================================
        // ESCALA
        // =====================================================

        Menu menuEscala =
                new Menu("Escala");

        MenuItem itemGerarEscala =
                new MenuItem("Gerar Escala");


        itemGerarEscala.setOnAction(
                event ->
                        mostrarTela(
                                escalaView.getView()
                        )
        );


        menuEscala.getItems().add(
                itemGerarEscala
        );


        // =====================================================
        // HISTÓRICO
        // =====================================================

        Menu menuHistorico =
                new Menu("Histórico");

        MenuItem itemConsultarHistorico =
                new MenuItem(
                        "Consultar Histórico"
                );


        itemConsultarHistorico.setOnAction(
                event -> {

                    historicoView.atualizar();

                    mostrarTela(
                            historicoView.getView()
                    );
                }
        );


        menuHistorico.getItems().add(
                itemConsultarHistorico
        );


        // =====================================================
        // AJUDA
        // =====================================================

        Menu menuAjuda =
                new Menu("Ajuda");

        MenuItem itemSobre =
                new MenuItem("Sobre");


        itemSobre.setOnAction(
                event -> mostrarSobre()
        );


        menuAjuda.getItems().add(
                itemSobre
        );


        // =====================================================
        // MENU PRINCIPAL
        // =====================================================

        menuBar.getMenus().addAll(
                menuArquivo,
                menuCadastros,
                menuProgramacao,
                menuEscala,
                menuHistorico,
                menuAjuda
        );


        // =====================================================
        // BARRA SUPERIOR
        // =====================================================

        HBox barraSuperior =
                new HBox();

        barraSuperior.setAlignment(
                Pos.CENTER_LEFT
        );

        barraSuperior.setSpacing(5);

        barraSuperior.setPadding(
                new Insets(2, 5, 2, 5)
        );


        barraSuperior.getChildren().addAll(
                botaoInicio,
                menuBar
        );


        root.setTop(
                barraSuperior
        );
    }


    private void criarTelaInicial() {

        VBox painelPrincipal =
                new VBox(20);

        painelPrincipal.setAlignment(
                Pos.CENTER
        );

        painelPrincipal.setPadding(
                new Insets(40)
        );


        // =====================================================
        // TÍTULO
        // =====================================================

        Label titulo =
                new Label(
                        "Gerador de Designações"
                );

        titulo.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;"
        );


        Label subtitulo =
                new Label(
                        "Automatize a geração e o gerenciamento " +
                                "das designações das reuniões."
                );

        subtitulo.setStyle(
                "-fx-font-size: 15px;"
        );


        // =====================================================
        // STATUS
        // =====================================================

        Label status =
                new Label(
                        "● Sistema pronto"
                );

        status.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;"
        );


        // =====================================================
        // BOTÕES DE ACESSO RÁPIDO
        // =====================================================

        Button botaoGerarEscala =
                new Button(
                        "Gerar Escala"
                );

        Button botaoPessoas =
                new Button(
                        "Pessoas"
                );

        Button botaoPartes =
                new Button(
                        "Partes"
                );

        Button botaoHistorico =
                new Button(
                        "Histórico"
                );

        Button botaoProgramacao =
                new Button(
                        "Programação"
                );


        configurarBotaoPrincipal(
                botaoGerarEscala
        );

        configurarBotaoPrincipal(
                botaoProgramacao
        );

        configurarBotaoPrincipal(
                botaoPessoas
        );

        configurarBotaoPrincipal(
                botaoPartes
        );

        configurarBotaoPrincipal(
                botaoHistorico
        );


        botaoGerarEscala.setOnAction(
                event ->
                        mostrarTela(
                                escalaView.getView()
                        )
        );

        botaoProgramacao.setOnAction(
                event ->
                        mostrarTela(
                                programacaoView.getView()
                        )
        );

        botaoPessoas.setOnAction(
                event ->
                        mostrarTela(
                                pessoaView.getView()
                        )
        );


        botaoPartes.setOnAction(
                event ->
                        mostrarTela(
                                parteView.getView()
                        )
        );


        botaoHistorico.setOnAction(
                event -> {

                    historicoView.atualizar();

                    mostrarTela(
                            historicoView.getView()
                    );
                }
        );


        HBox botoes =
                new HBox(15);

        botoes.setAlignment(
                Pos.CENTER
        );

        botoes.getChildren().addAll(
                botaoGerarEscala,
                botaoProgramacao,
                botaoPessoas,
                botaoPartes,
                botaoHistorico
        );


        // =====================================================
        // INFORMAÇÃO
        // =====================================================

        Label informacao =
                new Label(
                        "Use o menu superior ou os atalhos " +
                                "abaixo para começar."
                );

        informacao.setStyle(
                "-fx-font-size: 13px;"
        );


        painelPrincipal.getChildren().addAll(
                titulo,
                subtitulo,
                status,
                botoes,
                informacao
        );


        root.setCenter(
                painelPrincipal
        );
    }


    private void configurarBotaoPrincipal(
            Button botao
    ) {

        botao.setPrefWidth(130);
        botao.setPrefHeight(40);

        botao.setStyle(
                "-fx-font-size: 14px;"
        );
    }


    private void mostrarTela(
            Parent view
    ) {

        root.setCenter(null);

        root.setCenter(view);
    }


    public Parent getView() {

        return root;
    }


    // =========================================================
    // BACKUP
    // =========================================================

    private void fazerBackup() {

        FileChooser fileChooser =
                new FileChooser();

        fileChooser.setTitle(
                "Salvar backup do banco"
        );

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

            alerta.setTitle(
                    "Backup"
            );

            alerta.setHeaderText(
                    null
            );

            alerta.setContentText(
                    "Backup realizado com sucesso."
            );

            alerta.showAndWait();


        } catch (IOException e) {

            Alert alerta =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alerta.setTitle(
                    "Erro"
            );

            alerta.setHeaderText(
                    null
            );

            alerta.setContentText(
                    "Não foi possível criar o backup.\n"
                            + e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    // =========================================================
    // RESTAURAÇÃO
    // =========================================================

    private void restaurarBackup() {

        FileChooser fileChooser =
                new FileChooser();

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

                    if (
                            resposta ==
                                    javafx.scene.control.ButtonType.OK
                    ) {

                        executarRestauracao(
                                arquivo
                        );
                    }

                });
    }


    private void executarRestauracao(
            File arquivo
    ) {

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

            alerta.setHeaderText(
                    null
            );

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

            alerta.setHeaderText(
                    null
            );

            alerta.setContentText(
                    e.getMessage()
            );


            alerta.showAndWait();
        }
    }


    // =========================================================
    // SOBRE
    // =========================================================

    private void mostrarSobre() {

        Alert alerta =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alerta.setTitle(
                "Sobre"
        );

        alerta.setHeaderText(
                "Gerador de Designações"
        );

        alerta.setContentText(
                "Sistema para gerenciamento e geração "
                        + "automática de designações.\n\n"
                        + "Versão 1.0"
        );

        alerta.showAndWait();
    }
}