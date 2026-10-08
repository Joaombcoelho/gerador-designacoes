package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.controller.EscalaController;
import br.com.geradordesignacoes.controller.ProgramacaoController;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.BackupDatabase;
import br.com.geradordesignacoes.database.RestaurarDatabase;
import br.com.geradordesignacoes.service.PessoaService;
import br.com.geradordesignacoes.view.edicao.EdicaoEscalaView;
import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.historico.HistoricoView;
import br.com.geradordesignacoes.view.parte.ParteView;
import br.com.geradordesignacoes.view.pessoa.PessoaView;
import br.com.geradordesignacoes.view.programacao.ProgramacaoView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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

    private final ProgramacaoView programacaoView =
            new ProgramacaoView();

    private final EdicaoEscalaView edicaoEscalaView =
            new EdicaoEscalaView();

    private final HistoricoView historicoView;

    private final EscalaController escalaController;

    private final ProgramacaoController programacaoController;


    public MainView() {

        root = new BorderPane();

        root.getStyleClass().add("main-root");

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

        historicoView =
                new HistoricoView();

        criarLayout();

        criarTelaInicial();
    }


    private void criarLayout() {

        VBox menuLateral =
                criarMenuLateral();

        HBox barraSuperior =
                criarBarraSuperior();

        root.setLeft(menuLateral);

        root.setTop(barraSuperior);
    }


    // =========================================================
    // MENU LATERAL
    // =========================================================

    private VBox criarMenuLateral() {

        VBox menu =
                new VBox();

        menu.getStyleClass().add("sidebar");

        menu.setPrefWidth(220);

        // -----------------------------------------------------
        // TÍTULO
        // -----------------------------------------------------

        Label titulo =
                new Label(
                        "Gerador de\nDesignações"
                );

        titulo.getStyleClass().add(
                "sidebar-title"
        );

        menu.getChildren().add(
                titulo
        );


        // -----------------------------------------------------
        // PRINCIPAL
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "PRINCIPAL"
        );

        Button inicio =
                criarBotaoMenu(
                        "Início",
                        true
                );

        inicio.setOnAction(
                event ->
                        criarTelaInicial()
        );

        menu.getChildren().add(
                inicio
        );


        // -----------------------------------------------------
        // CADASTROS
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "CADASTROS"
        );

        Button pessoas =
                criarBotaoMenu(
                        "Pessoas"
                );

        pessoas.setOnAction(
                event ->
                        mostrarTela(
                                pessoaView.getView()
                        )
        );

        Button partes =
                criarBotaoMenu(
                        "Partes"
                );

        partes.setOnAction(
                event ->
                        mostrarTela(
                                parteView.getView()
                        )
        );

        menu.getChildren().addAll(
                pessoas,
                partes
        );


        // -----------------------------------------------------
        // PROGRAMAÇÃO
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "PROGRAMAÇÃO"
        );

        Button programacao =
                criarBotaoMenu(
                        "Programação Semanal"
                );

        programacao.setOnAction(
                event ->
                        mostrarTela(
                                programacaoView.getView()
                        )
        );

        menu.getChildren().add(
                programacao
        );


        // -----------------------------------------------------
        // ESCALAS
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "ESCALAS"
        );

        Button gerarEscala =
                criarBotaoMenu(
                        "Gerar Escala"
                );

        gerarEscala.setOnAction(
                event ->
                        mostrarTela(
                                escalaView.getView()
                        )
        );


        Button editarEscala =
                criarBotaoMenu(
                        "Editar Escalas"
                );

        editarEscala.setOnAction(
                event ->
                        mostrarTela(
                                edicaoEscalaView.getView()
                        )
        );

        menu.getChildren().addAll(
                gerarEscala,
                editarEscala
        );


        // -----------------------------------------------------
        // HISTÓRICO
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "HISTÓRICO"
        );

        Button historico =
                criarBotaoMenu(
                        "Consultar Histórico"
                );

        historico.setOnAction(
                event -> {

                    historicoView.atualizar();

                    mostrarTela(
                            historicoView.getView()
                    );
                }
        );

        menu.getChildren().add(
                historico
        );


        // -----------------------------------------------------
        // ESPAÇO FLEXÍVEL
        // -----------------------------------------------------

        Region espaco =
                new Region();

        VBox.setVgrow(
                espaco,
                Priority.ALWAYS
        );

        menu.getChildren().add(
                espaco
        );


        // -----------------------------------------------------
        // ARQUIVO
        // -----------------------------------------------------

        adicionarTituloSecao(
                menu,
                "SISTEMA"
        );

        Button backup =
                criarBotaoMenu(
                        "Fazer Backup"
                );

        backup.setOnAction(
                event ->
                        fazerBackup()
        );


        Button restaurar =
                criarBotaoMenu(
                        "Restaurar Backup"
                );

        restaurar.setOnAction(
                event ->
                        restaurarBackup()
        );


        Button sobre =
                criarBotaoMenu(
                        "Sobre"
                );

        sobre.setOnAction(
                event ->
                        mostrarSobre()
        );


        Button sair =
                criarBotaoMenu(
                        "Sair"
                );

        sair.setOnAction(
                event -> {

                    if (
                            root.getScene() != null
                    ) {

                        root.getScene()
                                .getWindow()
                                .hide();
                    }
                }
        );


        menu.getChildren().addAll(
                backup,
                restaurar,
                sobre,
                sair
        );


        return menu;
    }


    private void adicionarTituloSecao(
            VBox menu,
            String texto
    ) {

        Label titulo =
                new Label(
                        texto
                );

        titulo.getStyleClass().add(
                "sidebar-section"
        );

        menu.getChildren().add(
                titulo
        );
    }


    private Button criarBotaoMenu(
            String texto
    ) {

        return criarBotaoMenu(
                texto,
                false
        );
    }


    private Button criarBotaoMenu(
            String texto,
            boolean ativo
    ) {

        Button botao =
                new Button(
                        texto
                );

        botao.setMaxWidth(
                Double.MAX_VALUE
        );

        botao.getStyleClass().add(
                "sidebar-button"
        );

        if (ativo) {

            botao.getStyleClass().add(
                    "sidebar-button-active"
            );
        }

        return botao;
    }


    // =========================================================
    // BARRA SUPERIOR
    // =========================================================

    private HBox criarBarraSuperior() {

        HBox barra =
                new HBox();

        barra.getStyleClass().add(
                "top-bar"
        );

        barra.setAlignment(
                Pos.CENTER_LEFT
        );

        barra.setSpacing(
                10
        );


        Label titulo =
                new Label(
                        "Gerador de Designações"
                );

        titulo.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #111827;"
        );


        Region espaco =
                new Region();

        HBox.setHgrow(
                espaco,
                Priority.ALWAYS
        );


        Label status =
                new Label(
                        "● Sistema pronto"
                );

        status.getStyleClass().add(
                "status-success"
        );


        barra.getChildren().addAll(
                titulo,
                espaco,
                status
        );


        return barra;
    }


    // =========================================================
    // TELA INICIAL
    // =========================================================

    private void criarTelaInicial() {

        VBox painelPrincipal =
                new VBox(
                        25
                );

        painelPrincipal.getStyleClass().add(
                "content-area"
        );

        painelPrincipal.setAlignment(
                Pos.TOP_LEFT
        );


        // -----------------------------------------------------
        // CABEÇALHO
        // -----------------------------------------------------

        Label titulo =
                new Label(
                        "Bem-vindo ao Gerador de Designações"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label subtitulo =
                new Label(
                        "Gerencie pessoas, programação e escalas " +
                                "das reuniões em um único lugar."
                );

        subtitulo.getStyleClass().add(
                "page-subtitle"
        );


        VBox cabecalho =
                new VBox(
                        6
                );

        cabecalho.getChildren().addAll(
                titulo,
                subtitulo
        );


        // -----------------------------------------------------
        // CARDS
        // -----------------------------------------------------

        HBox cards =
                new HBox(
                        15
                );

        cards.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox cardPessoas =
                criarCard(
                        "Pessoas",
                        "Cadastro de participantes"
                );


        VBox cardPartes =
                criarCard(
                        "Partes",
                        "Partes das reuniões"
                );


        VBox cardProgramacao =
                criarCard(
                        "Programação",
                        "Programação semanal"
                );


        VBox cardEscalas =
                criarCard(
                        "Escalas",
                        "Geração das designações"
                );


        cards.getChildren().addAll(
                cardPessoas,
                cardPartes,
                cardProgramacao,
                cardEscalas
        );


        // -----------------------------------------------------
        // ACESSO RÁPIDO
        // -----------------------------------------------------

        Label acessoTitulo =
                new Label(
                        "Acesso rápido"
                );

        acessoTitulo.getStyleClass().add(
                "page-title"
        );

        acessoTitulo.setStyle(
                "-fx-font-size: 18px;"
        );


        HBox botoes =
                new HBox(
                        12
                );


        Button botaoGerarEscala =
                criarBotaoPrincipal(
                        "Gerar Escala"
                );


        Button botaoProgramacao =
                criarBotaoSecundario(
                        "Programação"
                );


        Button botaoPessoas =
                criarBotaoSecundario(
                        "Pessoas"
                );


        Button botaoPartes =
                criarBotaoSecundario(
                        "Partes"
                );


        Button botaoHistorico =
                criarBotaoSecundario(
                        "Histórico"
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


        botoes.getChildren().addAll(
                botaoGerarEscala,
                botaoProgramacao,
                botaoPessoas,
                botaoPartes,
                botaoHistorico
        );


        // -----------------------------------------------------
        // INFORMAÇÃO
        // -----------------------------------------------------

        VBox informacao =
                new VBox(
                        5
                );

        informacao.getStyleClass().add(
                "card"
        );


        Label informacaoTitulo =
                new Label(
                        "Sistema pronto"
                );

        informacaoTitulo.getStyleClass().add(
                "card-title"
        );


        Label informacaoTexto =
                new Label(
                        "Utilize o menu lateral para acessar " +
                                "as funções do sistema."
                );

        informacaoTexto.getStyleClass().add(
                "page-subtitle"
        );


        informacao.getChildren().addAll(
                informacaoTitulo,
                informacaoTexto
        );


        painelPrincipal.getChildren().addAll(
                cabecalho,
                cards,
                acessoTitulo,
                botoes,
                informacao
        );


        root.setCenter(
                painelPrincipal
        );
    }


    private VBox criarCard(
            String titulo,
            String descricao
    ) {

        VBox card =
                new VBox(
                        8
                );

        card.getStyleClass().add(
                "card"
        );

        card.setPrefWidth(
                210
        );

        Label labelTitulo =
                new Label(
                        titulo
                );

        labelTitulo.getStyleClass().add(
                "card-title"
        );


        Label labelDescricao =
                new Label(
                        descricao
                );

        labelDescricao.getStyleClass().add(
                "page-subtitle"
        );

        labelDescricao.setWrapText(
                true
        );


        card.getChildren().addAll(
                labelTitulo,
                labelDescricao
        );


        return card;
    }


    private Button criarBotaoPrincipal(
            String texto
    ) {

        Button botao =
                new Button(
                        texto
                );

        botao.getStyleClass().add(
                "primary-button"
        );

        return botao;
    }


    private Button criarBotaoSecundario(
            String texto
    ) {

        Button botao =
                new Button(
                        texto
                );

        botao.getStyleClass().add(
                "secondary-button"
        );

        return botao;
    }


    // =========================================================
    // NAVEGAÇÃO
    // =========================================================

    private void mostrarTela(
            Parent view
    ) {

        root.setCenter(
                view
        );
    }


    public Parent getView() {

        return root;
    }


    // =========================================================
    // CSS
    // =========================================================

    public void aplicarEstilo(
            Scene scene
    ) {

        var css =
                getClass()
                        .getResource(
                                "/style.css"
                        );

        if (
                css != null &&
                        !scene.getStylesheets()
                                .contains(
                                        css.toExternalForm()
                                )
        ) {

            scene.getStylesheets().add(
                    css.toExternalForm()
            );
        }
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
                .ifPresent(
                        resposta -> {

                            if (
                                    resposta ==
                                            javafx.scene.control.ButtonType.OK
                            ) {

                                executarRestauracao(
                                        arquivo
                                );
                            }
                        }
                );
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
