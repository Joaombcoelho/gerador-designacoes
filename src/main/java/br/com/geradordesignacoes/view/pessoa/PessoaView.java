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


public class PessoaView {

    private final BorderPane root;

    private final PessoaService pessoaService;

    private final TableView<Pessoa> tabela;

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

        colunaPrivilegio.setCellFactory(coluna -> new TableCell<>() {

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

        colunaAtivo.setCellFactory(coluna -> new TableCell<>() {

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
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabela.setPlaceholder(
                new Label("Nenhuma pessoa cadastrada.")
        );

        tabela.getStyleClass().add("table-view");

        root.setCenter(tabela);
    }


    private void criarBotoes() {

        Button novo =
                new Button(
                        "+ Nova Pessoa"
                );

        novo.getStyleClass().add(
                "primary-button"
        );


        Button editar =
                new Button(
                        "Editar"
                );

        editar.getStyleClass().add(
                "secondary-button"
        );


        Button excluir =
                new Button(
                        "Excluir"
                );

        excluir.getStyleClass().add(
                "danger-button"
        );


        novo.setOnAction(
                event ->
                        abrirFormulario()
        );


        editar.setOnAction(
                event ->
                        abrirFormularioEdicao()
        );


        excluir.setOnAction(
                event ->
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
                pessoaService.listarTodas()
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

        root.setCenter(
                tabela
        );
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


        alert.showAndWait();
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

        Pessoa pessoa =
                obterPessoaSelecionada();


        if (pessoa == null) {

            mostrarAviso(
                    "Selecione uma pessoa."
            );

            return;
        }


        confirmarExclusao(
                pessoa
        );
    }



    private void confirmarExclusao(Pessoa pessoa) {

        pessoaPendenteExclusao = pessoa;

        Label mensagem = new Label(
                "Deseja realmente excluir \"" + pessoa.getNome() + "\"?"
        );
        mensagem.getStyleClass().add("page-subtitle");

        Button confirmar = new Button("Confirmar exclusão");
        confirmar.getStyleClass().add("danger-button");

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("secondary-button");

        HBox botoes = new HBox(10, confirmar, cancelar);
        botoes.setAlignment(Pos.CENTER_RIGHT);

        painelConfirmacao = new VBox(12, mensagem, botoes);
        painelConfirmacao.getStyleClass().add("card");
        painelConfirmacao.setPadding(new Insets(16));

        confirmar.setOnAction(event -> {
            if (pessoaPendenteExclusao != null) {
                pessoaService.excluir(pessoaPendenteExclusao.getId());
                pessoaPendenteExclusao = null;
                voltarParaTabela();
            }
        });

        cancelar.setOnAction(event -> {
            pessoaPendenteExclusao = null;
            voltarParaTabela();
        });

        VBox conteudo = new VBox(16, tabela, painelConfirmacao);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        root.setCenter(conteudo);
    }

}