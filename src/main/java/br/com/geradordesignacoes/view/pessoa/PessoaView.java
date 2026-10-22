package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.service.PessoaService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PessoaView {

    private static final Logger LOGGER = Logger.getLogger(PessoaView.class.getName());

    private final BorderPane root;

    private final PessoaService pessoaService;

    private final TableView<Pessoa> tabela;
    private TabPane abas;
    private Tab abaAtivos;
    private Tab abaArquivados;
    private boolean exibindoArquivados;
    private Button botaoNovo;
    private Button botaoEditar;
    private Button botaoArquivar;

    private VBox painelConfirmacao;

    private Pessoa pessoaPendenteExclusao;


    public PessoaView(PessoaService pessoaService) {

        this.pessoaService = pessoaService;

        root = new BorderPane();

        root.getStyleClass().add(
                "content-area"
        );


        tabela = new TableView<>();

        criarCabecalho();
        criarTabela();
        criarAbas();
        criarBotoes();
        carregarPessoas();
    }


    private void criarCabecalho() {

        Label titulo =
                new Label(
                        "Pessoas"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label subtitulo =
                new Label(
                        "Gerencie os participantes e suas funções na congregação."
                );

        subtitulo.getStyleClass().add(
                "page-subtitle"
        );


        VBox topo =
                new VBox(
                        6,
                        titulo,
                        subtitulo
                );

        topo.setPadding(
                new Insets(
                        0,
                        0,
                        18,
                        0
                )
        );


        root.setTop(
                topo
        );
    }


    private void criarTabela() {

        TableColumn<Pessoa, String> colunaNome =
                new TableColumn<>("Nome");

        colunaNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );


        TableColumn<Pessoa, String> colunaSexo =
                new TableColumn<>("Sexo");

        colunaSexo.setCellValueFactory(
                new PropertyValueFactory<>("sexo")
        );


        TableColumn<Pessoa, Privilegio> colunaPrivilegio =
                new TableColumn<>("Privilégio");

        colunaPrivilegio.setCellValueFactory(
                new PropertyValueFactory<>("privilegio")
        );

        colunaPrivilegio.setCellFactory(_ -> new TableCell<>() {

            @Override
            protected void updateItem(Privilegio privilegio, boolean vazio) {

                super.updateItem(privilegio, vazio);

                if (vazio || privilegio == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label badge = new Label(privilegio.toString());
                badge.getStyleClass().add("badge-blue");

                setGraphic(badge);
                setText(null);
            }
        });


        TableColumn<Pessoa, Boolean> colunaAtivo =
                new TableColumn<>("Status");

        colunaAtivo.setCellValueFactory(
                new PropertyValueFactory<>("ativo")
        );

        colunaAtivo.setCellFactory(_ -> new TableCell<>() {

            @Override
            protected void updateItem(Boolean ativo, boolean vazio) {

                super.updateItem(ativo, vazio);

                if (vazio || ativo == null) {

                    setText(null);
                    setGraphic(null);

                } else {

                    Label badge;

                    if (ativo) {

                        badge = new Label("Ativo");
                        badge.getStyleClass().add("badge-active");

                    } else {

                        badge = new Label("Inativo");
                        badge.getStyleClass().add("badge-inactive");
                    }

                    setGraphic(badge);
                    setText(null);
                }
            }
        });


        tabela.getColumns().addAll(
                colunaNome,
                colunaSexo,
                colunaPrivilegio,
                colunaAtivo
        );


        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabela.setPlaceholder(
                new Label("Nenhuma pessoa cadastrada.")
        );

        tabela.getStyleClass().add("table-view");

    }

    private void criarAbas() {
        abaAtivos = new Tab("Ativos");
        abaArquivados = new Tab("Arquivados");
        abaAtivos.setClosable(false);
        abaArquivados.setClosable(false);
        abaAtivos.setContent(criarPainelTabela());
        abas = new TabPane(abaAtivos, abaArquivados);
        abas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, selecionada) -> {
            exibindoArquivados = selecionada == abaArquivados;
            if (anterior != null) anterior.setContent(null);
            selecionada.setContent(criarPainelTabela());
            if (botaoNovo != null) botaoNovo.setDisable(exibindoArquivados);
            if (botaoEditar != null) botaoEditar.setDisable(exibindoArquivados);
            if (botaoArquivar != null) botaoArquivar.setText(exibindoArquivados ? "Restaurar" : "Arquivar");
            carregarPessoas();
        });
        root.setCenter(abas);
    }

    private VBox criarPainelTabela() {
        VBox painel = new VBox(tabela);
        painel.setSpacing(12);
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return painel;
    }


    private void criarBotoes() {

        Button novo =
                new Button(
                        "+ Nova Pessoa"
                );
        botaoNovo = novo;

        novo.getStyleClass().add(
                "primary-button"
        );


        Button editar =
                new Button(
                        "Editar"
                );
        botaoEditar = editar;

        editar.getStyleClass().add(
                "secondary-button"
        );


        Button excluir =
                new Button(
                        "Arquivar"
                );
        botaoArquivar = excluir;

        excluir.getStyleClass().add(
                "danger-button"
        );


        novo.setOnAction(
                _ ->
                        abrirFormulario()
        );


        editar.setOnAction(
                _ ->
                        abrirFormularioEdicao()
        );


        excluir.setOnAction(
                _ ->
                        excluirPessoa()
        );


        Region espaco =
                new Region();

        HBox.setHgrow(
                espaco,
                Priority.ALWAYS
        );


        HBox barra =
                new HBox(
                        10,
                        espaco,
                        novo,
                        editar,
                        excluir
                );


        barra.setAlignment(
                Pos.CENTER_RIGHT
        );


        VBox topo =
                (VBox) root.getTop();


        topo.getChildren().add(
                barra
        );
    }


    public Parent getView() {

        return root;
    }


    private void carregarPessoas() {

        tabela.getItems().setAll(
                exibindoArquivados ? pessoaService.listarArquivados() : pessoaService.listarAtivos()
        );
    }


    private void abrirFormulario() {

        PessoaFormularioView formulario =
                new PessoaFormularioView(
                        null,
                        this::salvarPessoa,
                        this::atualizarPessoa,
                        this::voltarParaTabela
                );


        root.setCenter(
                formulario.getView()
        );
    }


    private void voltarParaTabela() {

        carregarPessoas();

        root.setCenter(abas);
    }


    private void salvarPessoa(
            Pessoa pessoa
    ) {

        pessoaService.salvar(
                pessoa
        );

        voltarParaTabela();
    }


    private Pessoa obterPessoaSelecionada() {

        return tabela
                .getSelectionModel()
                .getSelectedItem();
    }


    private void mostrarAviso(
            String mensagem
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );


        alert.setTitle(
                "Atenção"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensagem
        );


        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alert.initOwner(root.getScene().getWindow());
        }
        alert.showAndWait();
    }


    private boolean possuiViolacaoChaveEstrangeira(Throwable erro) {
        for (Throwable atual = erro; atual != null; atual = atual.getCause()) {
            if (atual instanceof SQLException sqlException
                    && sqlException.getMessage() != null
                    && sqlException.getMessage().toLowerCase().contains("foreign key")) {
                return true;
            }
        }
        return false;
    }


    private void abrirFormularioEdicao() {

        Pessoa pessoa =
                obterPessoaSelecionada();


        if (pessoa == null) {

            mostrarAviso(
                    "Selecione uma pessoa."
            );

            return;
        }


        PessoaFormularioView formulario =
                new PessoaFormularioView(
                        pessoa,
                        this::salvarPessoa,
                        this::atualizarPessoa,
                        this::voltarParaTabela
                );


        root.setCenter(
                formulario.getView()
        );
    }


    private void atualizarPessoa(
            Pessoa pessoa
    ) {

        pessoaService.atualizar(
                pessoa
        );

        voltarParaTabela();
    }


    private void excluirPessoa() {
        Pessoa pessoa = obterPessoaSelecionada();
        if (pessoa == null) {
            mostrarAviso("Selecione uma pessoa.");
            return;
        }
        if (exibindoArquivados) {
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Deseja restaurar \\"" + pessoa.getNome() + "\\" para os cadastros ativos?",
                    ButtonType.CANCEL, ButtonType.OK);
            prepararAlerta(confirmacao);
            if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                try {
                    pessoaService.restaurar(pessoa.getId());
                    carregarPessoas();
                } catch (RuntimeException e) {
                    LOGGER.log(Level.SEVERE, "Falha ao restaurar pessoa.", e);
                    mostrarAviso("Não foi possível restaurar a pessoa.");
                }
            }
            return;
        }
        confirmarExclusao(pessoa);
    }



    private void confirmarExclusao(Pessoa pessoa) {
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja arquivar \\"" + pessoa.getNome() + "\\"? O histórico será preservado.",
                ButtonType.CANCEL, ButtonType.OK);
        prepararAlerta(confirmacao);
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            pessoaService.arquivar(pessoa.getId());
            carregarPessoas();
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Não foi possível arquivar pessoa.", e);
            String mensagemErro = e instanceof IllegalStateException
                    ? e.getMessage()
                    : "Não foi possível arquivar a pessoa. Verifique os dados e tente novamente.";
            mostrarAviso(mensagemErro);
        }
    }

    private void prepararAlerta(Alert alerta) {
        alerta.setTitle("Confirmação");
        alerta.setHeaderText(null);
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alerta.initOwner(root.getScene().getWindow());
        }
    }
}