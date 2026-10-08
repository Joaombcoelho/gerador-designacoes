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
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;

import java.io.File;
import java.io.IOException;

public class MainView {

    private final BorderPane root;

    private final PessoaView pessoaView;
    private final ParteView parteView = new ParteView();
    private final EscalaView escalaView = new EscalaView();
    private final ProgramacaoView programacaoView = new ProgramacaoView();
    private final EdicaoEscalaView edicaoEscalaView =
            new EdicaoEscalaView();

    private final HistoricoView historicoView;

    private final EscalaController escalaController;
    private final ProgramacaoController programacaoController;

    private Button botaoAtivo;


    public MainView() {

        root = new BorderPane();

        root.getStyleClass().add("main-root");


        PessoaDAO pessoaDAO = new PessoaDAO();

        PessoaService pessoaService =
                new PessoaService(pessoaDAO);

        pessoaView =
                new PessoaView(pessoaService);


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

        root.setLeft(
                criarMenuLateral()
        );

        root.setTop(
                criarBarraSuperior()
        );
    }


    private VBox criarMenuLateral() {

        VBox sidebar =
                new VBox(8);

        sidebar.getStyleClass().add(
                "sidebar"
        );


        Label titulo =
                new Label(
                        "GERADOR\nDE DESIGNAÇÕES"
                );

        titulo.getStyleClass().add(
                "sidebar-title"
        );


        sidebar.getChildren().add(
                titulo
        );


        Region espacoInicial =
                new Region();

        espacoInicial.setPrefHeight(20);

        sidebar.getChildren().add(
                espacoInicial
        );


        Label secaoPrincipal =
                new Label("PRINCIPAL");

        secaoPrincipal.getStyleClass().add(
                "sidebar-section"
        );

        sidebar.getChildren().add(
                secaoPrincipal
        );


        Button inicio =
                criarBotaoMenu(
                        FontAwesomeSolid.HOME,
                        "Início",
                        true
                );

        inicio.setOnAction(
                event -> {

                    ativarBotao(
                            inicio
                    );

                    criarTelaInicial();
                }
        );

        sidebar.getChildren().add(
                inicio
        );


        Button pessoas =
                criarBotaoMenu(
                        FontAwesomeSolid.USERS,
                        "Pessoas",
                        false
                );

        pessoas.setOnAction(
                event -> {

                    ativarBotao(
                            pessoas
                    );

                    mostrarTela(
                            pessoaView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                pessoas
        );


        Button partes =
                criarBotaoMenu(
                        FontAwesomeSolid.LIST,
                        "Partes",
                        false
                );

        partes.setOnAction(
                event -> {

                    ativarBotao(
                            partes
                    );

                    mostrarTela(
                            parteView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                partes
        );


        Button programacao =
                criarBotaoMenu(
                        FontAwesomeSolid.CALENDAR_ALT,
                        "Programação Semanal",
                        false
                );

        programacao.setOnAction(
                event -> {

                    ativarBotao(
                            programacao
                    );

                    mostrarTela(
                            programacaoView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                programacao
        );


        Label secaoEscala =
                new Label("ESCALA");

        secaoEscala.getStyleClass().add(
                "sidebar-section"
        );

        VBox.setMargin(
                secaoEscala,
                new javafx.geometry.Insets(
                        18, 0, 0, 0
                )
        );

        sidebar.getChildren().add(
                secaoEscala
        );


        Button gerarEscala =
                criarBotaoMenu(
                        FontAwesomeSolid.CALENDAR_CHECK,
                        "Gerar Escala",
                        false
                );

        gerarEscala.setOnAction(
                event -> {

                    ativarBotao(
                            gerarEscala
                    );

                    mostrarTela(
                            escalaView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                gerarEscala
        );


        Button editarEscalas =
                criarBotaoMenu(
                        FontAwesomeSolid.EDIT,
                        "Editar Escalas",
                        false
                );

        editarEscalas.setOnAction(
                event -> {

                    ativarBotao(
                            editarEscalas
                    );

                    mostrarTela(
                            edicaoEscalaView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                editarEscalas
        );


        Button historico =
                criarBotaoMenu(
                        FontAwesomeSolid.HISTORY,
                        "Consultar Histórico",
                        false
                );

        historico.setOnAction(
                event -> {

                    ativarBotao(
                            historico
                    );

                    historicoView.atualizar();

                    mostrarTela(
                            historicoView.getView()
                    );
                }
        );

        sidebar.getChildren().add(
                historico
        );


        Label secaoSistema =
                new Label("SISTEMA");

        secaoSistema.getStyleClass().add(
                "sidebar-section"
        );

        VBox.setMargin(
                secaoSistema,
                new javafx.geometry.Insets(
                        18, 0, 0, 0
                )
        );

        sidebar.getChildren().add(
                secaoSistema
        );


        Button backup =
                criarBotaoMenu(
                        FontAwesomeSolid.DOWNLOAD,
                        "Fazer Backup",
                        false
                );

        backup.setOnAction(
                event ->
                        fazerBackup()
        );

        sidebar.getChildren().add(
                backup
        );


        Button restaurar =
                criarBotaoMenu(
                        FontAwesomeSolid.UPLOAD,
                        "Restaurar Backup",
                        false
                );

        restaurar.setOnAction(
                event ->
                        restaurarBackup()
        );

        sidebar.getChildren().add(
                restaurar
        );


        Button sobre =
                criarBotaoMenu(
                        FontAwesomeSolid.INFO_CIRCLE,
                        "Sobre",
                        false
                );

        sobre.setOnAction(event -> {
            ativarBotao(sobre);
            mostrarSobre();
        });

        sidebar.getChildren().add(
                sobre
        );


        Region espaco =
                new Region();

        VBox.setVgrow(
                espaco,
                Priority.ALWAYS
        );

        sidebar.getChildren().add(
                espaco
        );


        Separator separador =
                new Separator();

        sidebar.getChildren().add(
                separador
        );


        Button sair =
                criarBotaoMenu(
                        FontAwesomeSolid.SIGN_OUT_ALT,
                        "Sair",
                        false
                );

        sair.setOnAction(
                event ->
                        System.exit(0)
        );

        sidebar.getChildren().add(
                sair
        );


        return sidebar;
    }


    private Button criarBotaoMenu(
            FontAwesomeSolid icone,
            String texto,
            boolean ativo
    ) {

        Button botao =
                new Button();

        botao.setMaxWidth(
                Double.MAX_VALUE
        );

        botao.setAlignment(
                Pos.CENTER_LEFT
        );

        botao.getStyleClass().add(
                "sidebar-button"
        );


        FontIcon fontIcon =
                new FontIcon(
                        icone
                );

        fontIcon.setIconSize(
                16
        );

        fontIcon.setIconColor(
                Color.web(
                        ativo
                                ? "#2B82C5"
                                : "#9CA3AF"
                )
        );


        Label label =
                new Label(
                        texto
                );

        label.setStyle(
                "-fx-text-fill: #F3F4F6;"
        );


        HBox conteudo =
                new HBox(12);

        conteudo.setAlignment(
                Pos.CENTER_LEFT
        );

        conteudo.getChildren().addAll(
                fontIcon,
                label
        );


        botao.setGraphic(
                conteudo
        );


        if (ativo) {

            botao.getStyleClass().add(
                    "sidebar-button-active"
            );

            botaoAtivo = botao;
        }


        return botao;
    }


    private void ativarBotao(
            Button botao
    ) {

        if (botaoAtivo != null) {

            botaoAtivo.getStyleClass().remove(
                    "sidebar-button-active"
            );

            atualizarCorIcone(
                    botaoAtivo,
                    false
            );
        }


        botao.getStyleClass().add(
                "sidebar-button-active"
        );

        atualizarCorIcone(
                botao,
                true
        );


        botaoAtivo = botao;
    }


    private void atualizarCorIcone(
            Button botao,
            boolean ativo
    ) {

        if (
                botao.getGraphic() instanceof HBox hbox
                        && !hbox.getChildren().isEmpty()
                        && hbox.getChildren().get(0) instanceof FontIcon icon
        ) {

            icon.setIconColor(
                    Color.web(
                            ativo
                                    ? "#2B82C5"
                                    : "#9CA3AF"
                    )
            );
        }
    }


    private HBox criarBarraSuperior() {

        HBox topBar =
                new HBox();

        topBar.getStyleClass().add(
                "top-bar"
        );

        topBar.setAlignment(
                Pos.CENTER_LEFT
        );


        Label titulo =
                new Label(
                        "Painel principal"
                );

        titulo.setStyle(
                "-fx-text-fill: #F3F4F6;"
        );


        topBar.getChildren().add(
                titulo
        );


        return topBar;
    }


    private void criarTelaInicial() {

        VBox content =
                new VBox(20);

        content.getStyleClass().add(
                "content-area"
        );


        Label titulo =
                new Label(
                        "Gerador de Designações"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label subtitulo =
                new Label(
                        "Automatize a geração e o gerenciamento das designações das reuniões."
                );

        subtitulo.getStyleClass().add(
                "page-subtitle"
        );


        HBox status =
                new HBox();

        status.setAlignment(
                Pos.CENTER_LEFT
        );


        Label statusLabel =
                new Label(
                        "● Sistema pronto"
                );

        statusLabel.getStyleClass().add(
                "status-success"
        );

        status.getChildren().add(
                statusLabel
        );


        content.getChildren().addAll(
                titulo,
                subtitulo,
                status
        );


        HBox cards =
                new HBox(12);

        cards.setAlignment(
                Pos.CENTER
        );

        cards.setFillHeight(
                true
        );

        cards.setMaxWidth(
                Double.MAX_VALUE
        );


        VBox cardGerarEscala =
                criarCardAtalho(
                        FontAwesomeSolid.CALENDAR_CHECK,
                        "Gerar Escala",
                        "Criar as designações",
                        () -> {

                            ativarBotaoPorTexto(
                                    "Gerar Escala"
                            );

                            mostrarTela(
                                    escalaView.getView()
                            );
                        }
                );


        VBox cardProgramacao =
                criarCardAtalho(
                        FontAwesomeSolid.CALENDAR_ALT,
                        "Programação",
                        "Configurar reuniões",
                        () -> {

                            ativarBotaoPorTexto(
                                    "Programação Semanal"
                            );

                            mostrarTela(
                                    programacaoView.getView()
                            );
                        }
                );


        VBox cardPessoas =
                criarCardAtalho(
                        FontAwesomeSolid.USERS,
                        "Pessoas",
                        "Gerenciar participantes",
                        () -> {

                            ativarBotaoPorTexto(
                                    "Pessoas"
                            );

                            mostrarTela(
                                    pessoaView.getView()
                            );
                        }
                );


        VBox cardPartes =
                criarCardAtalho(
                        FontAwesomeSolid.LIST,
                        "Partes",
                        "Gerenciar partes",
                        () -> {

                            ativarBotaoPorTexto(
                                    "Partes"
                            );

                            mostrarTela(
                                    parteView.getView()
                            );
                        }
                );


        VBox cardHistorico =
                criarCardAtalho(
                        FontAwesomeSolid.HISTORY,
                        "Histórico",
                        "Consultar designações",
                        () -> {

                            ativarBotaoPorTexto(
                                    "Consultar Histórico"
                            );

                            historicoView.atualizar();

                            mostrarTela(
                                    historicoView.getView()
                            );
                        }
                );


        HBox.setHgrow(
                cardGerarEscala,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cardProgramacao,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cardPessoas,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cardPartes,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                cardHistorico,
                Priority.ALWAYS
        );


        cardGerarEscala.setMaxWidth(
                Double.MAX_VALUE
        );

        cardProgramacao.setMaxWidth(
                Double.MAX_VALUE
        );

        cardPessoas.setMaxWidth(
                Double.MAX_VALUE
        );

        cardPartes.setMaxWidth(
                Double.MAX_VALUE
        );

        cardHistorico.setMaxWidth(
                Double.MAX_VALUE
        );


        cards.getChildren().addAll(
                cardGerarEscala,
                cardProgramacao,
                cardPessoas,
                cardPartes,
                cardHistorico
        );


        content.getChildren().add(
                cards
        );


        VBox informacao =
                new VBox(8);

        informacao.getStyleClass().add(
                "card"
        );


        Label tituloInformacao =
                new Label(
                        "Acesso rápido"
                );

        tituloInformacao.getStyleClass().add(
                "card-title"
        );


        Label textoInformacao =
                new Label(
                        "Utilize os atalhos acima para acessar rapidamente as principais funções do sistema."
                );

        textoInformacao.setWrapText(
                true
        );

        informacao.getChildren().addAll(
                tituloInformacao,
                textoInformacao
        );


        content.getChildren().add(
                informacao
        );


        Label rodape =
                new Label(
                        "Gerador de Designações"
                );

        rodape.getStyleClass().add(
                "page-subtitle"
        );


        content.getChildren().add(
                rodape
        );


        root.setCenter(
                content
        );
    }


    private VBox criarCardAtalho(
            FontAwesomeSolid icone,
            String titulo,
            String descricao,
            Runnable acao
    ) {

        VBox card =
                new VBox(10);

        card.getStyleClass().add(
                "card"
        );

        card.setAlignment(
                Pos.CENTER_LEFT
        );

        card.setPrefHeight(
                150
        );

        card.setMaxWidth(
                Double.MAX_VALUE
        );


        FontIcon iconeLabel =
                new FontIcon(
                        icone
                );

        iconeLabel.setIconSize(
                28
        );

        iconeLabel.setIconColor(
                Color.web(
                        "#2B82C5"
                )
        );


        Label tituloLabel =
                new Label(
                        titulo
                );

        tituloLabel.getStyleClass().add(
                "card-title"
        );


        Label descricaoLabel =
                new Label(
                        descricao
                );

        descricaoLabel.setWrapText(
                true
        );

        descricaoLabel.getStyleClass().add(
                "page-subtitle"
        );


        card.getChildren().addAll(
                iconeLabel,
                tituloLabel,
                descricaoLabel
        );


        card.setOnMouseClicked(
                event ->
                        acao.run()
        );


        card.setOnMouseEntered(
                event ->
                        card.setStyle(
                                "-fx-border-color: #2B82C5;" +
                                        "-fx-background-color: #2A303B;"
                        )
        );


        card.setOnMouseExited(
                event ->
                        card.setStyle("")
        );


        return card;
    }


    private void ativarBotaoPorTexto(
            String texto
    ) {

        if (root.getLeft() instanceof VBox sidebar) {

            for (javafx.scene.Node node :
                    sidebar.getChildren()) {

                if (
                        node instanceof Button botao
                                && botao.getGraphic() instanceof HBox hbox
                                && !hbox.getChildren().isEmpty()
                                && hbox.getChildren().get(1) instanceof Label label
                                && label.getText().equals(texto)
                ) {

                    ativarBotao(
                            botao
                    );

                    return;
                }
            }
        }
    }


    private void mostrarTela(
            Parent tela
    ) {

        root.setCenter(
                tela
        );
    }


    private void fazerBackup() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Salvar Backup"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Banco de Dados SQLite",
                        "*.db"
                )
        );


        File arquivo =
                chooser.showSaveDialog(
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
                    "Backup realizado"
            );

            alerta.setContentText(
                    "O backup foi salvo com sucesso."
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
                    "Não foi possível realizar o backup"
            );

            alerta.setContentText(
                    e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    private void restaurarBackup() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Selecionar Backup"
        );

        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Banco de Dados SQLite",
                        "*.db"
                )
        );


        File arquivo =
                chooser.showOpenDialog(
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
                "Restaurar Backup"
        );

        confirmacao.setHeaderText(
                "Restaurar banco de dados?"
        );

        confirmacao.setContentText(
                "Os dados atuais serão substituídos pelo backup selecionado."
        );


        if (
                confirmacao.showAndWait()
                        .orElse(
                                javafx.scene.control.ButtonType.CANCEL
                        )
                        != javafx.scene.control.ButtonType.OK
        ) {

            return;
        }


        try {

            RestaurarDatabase.restaurar(
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
                    "Backup restaurado"
            );

            alerta.setContentText(
                    "O banco de dados foi restaurado com sucesso.\n" +
                            "Reinicie o sistema para carregar os dados restaurados."
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
                    "Não foi possível restaurar o backup"
            );

            alerta.setContentText(
                    e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    private void mostrarSobre() {

        VBox conteudo = new VBox(20);
        conteudo.getStyleClass().add("content-area");
        conteudo.setPadding(new Insets(28));

        Label titulo = new Label("Sobre o sistema");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Informações sobre o Gerador de Designações"
        );
        subtitulo.getStyleClass().add("page-subtitle");

        VBox cardPrincipal = new VBox(14);
        cardPrincipal.getStyleClass().add("card");
        cardPrincipal.setPadding(new Insets(28));
        cardPrincipal.setAlignment(Pos.CENTER);

        FontIcon icone = new FontIcon(FontAwesomeSolid.CALENDAR_CHECK);
        icone.setIconSize(48);
        icone.setIconColor(Color.web("#2B82C5"));

        Label nome = new Label("Gerador de Designações");
        nome.getStyleClass().add("page-title");

        Label versao = new Label("Versão 1.0.3");
        versao.getStyleClass().add("page-subtitle");

        Label descricao = new Label(
                "Aplicativo para auxiliar na organização, geração " +
                        "e gerenciamento das designações das reuniões congregacionais."
        );
        descricao.setWrapText(true);
        descricao.setMaxWidth(560);
        descricao.setAlignment(Pos.CENTER);
        descricao.setStyle(
                "-fx-text-fill: #D1D5DB; -fx-font-size: 14px;"
        );

        cardPrincipal.getChildren().addAll(
                icone,
                nome,
                versao,
                descricao
        );

        VBox cardRecursos = new VBox(12);
        cardRecursos.getStyleClass().add("card");

        Label tituloRecursos = new Label("Principais recursos");
        tituloRecursos.getStyleClass().add("card-title");

        Label recursos = new Label(
                "• Cadastro de pessoas e partes\n" +
                        "• Configuração da programação semanal\n" +
                        "• Geração e edição de escalas\n" +
                        "• Consulta ao histórico de designações\n" +
                        "• Backup e restauração dos dados"
        );
        recursos.setWrapText(true);
        recursos.setStyle("-fx-text-fill: #D1D5DB; -fx-font-size: 13px;");

        cardRecursos.getChildren().addAll(
                tituloRecursos,
                recursos
        );

        Label rodape = new Label(
                "Desenvolvido para facilitar a organização das designações."
        );
        rodape.getStyleClass().add("page-subtitle");
        rodape.setWrapText(true);

        conteudo.getChildren().addAll(
                titulo,
                subtitulo,
                cardPrincipal,
                cardRecursos,
                rodape
        );

        mostrarTela(conteudo);

    }


    public Parent getView() {

        return root;
    }


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
                        !scene.getStylesheets().contains(
                                css.toExternalForm()
                        )
        ) {

            scene.getStylesheets().add(
                    css.toExternalForm()
            );
        }
    }
}