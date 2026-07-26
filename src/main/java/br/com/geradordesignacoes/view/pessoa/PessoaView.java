package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.service.PessoaService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class PessoaView {

    private final BorderPane root;

    private final PessoaService pessoaService;

    private final TableView<Pessoa> tabela;


    public PessoaView(PessoaService pessoaService) {

        this.pessoaService = pessoaService;

        root = new BorderPane();

        root.setPadding(
                new Insets(10)
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
                        "Cadastro de Pessoas"
                );


        titulo.setStyle(
                "-fx-font-size: 20px;"
        );


        VBox topo =
                new VBox(
                        10,
                        titulo
                );


        root.setTop(topo);
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



        TableColumn<Pessoa, String> colunaPrivilegio =
                new TableColumn<>("Privilégio");


        colunaPrivilegio.setCellValueFactory(
                new PropertyValueFactory<>("privilegio")
        );



        TableColumn<Pessoa, Boolean> colunaAtivo =
                new TableColumn<>("Ativo");


        colunaAtivo.setCellValueFactory(
                new PropertyValueFactory<>("ativo")
        );



        tabela.getColumns().addAll(
                colunaNome,
                colunaSexo,
                colunaPrivilegio,
                colunaAtivo
        );


        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        root.setCenter(
                tabela
        );
    }




    private void criarBotoes() {


        Button novo =
                new Button("Novo");


        Button editar =
                new Button("Editar");


        Button excluir =
                new Button("Excluir");



        novo.setOnAction(
                event -> abrirFormulario()
        );


        editar.setOnAction(
                event -> abrirFormularioEdicao()
        );


        excluir.setOnAction(
                event -> excluirPessoa()
        );



        ToolBar barra =
                new ToolBar(
                        novo,
                        editar,
                        excluir
                );


        VBox topo =
                new VBox(
                        10
                );


        Label titulo =
                new Label(
                        "Cadastro de Pessoas"
                );


        titulo.setStyle(
                "-fx-font-size: 20px;"
        );


        topo.getChildren().addAll(
                titulo,
                barra
        );


        root.setTop(
                topo
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



    private void salvarPessoa(Pessoa pessoa) {

        pessoaService.salvar(pessoa);

        voltarParaTabela();

    }



    private Pessoa obterPessoaSelecionada() {

        return tabela
                .getSelectionModel()
                .getSelectedItem();

    }



    private void mostrarAviso(String mensagem) {

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



    private void atualizarPessoa(Pessoa pessoa) {

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

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Confirmação"
        );


        alert.setHeaderText(
                "Excluir Pessoa"
        );


        alert.setContentText(
                "Deseja realmente excluir \"" +
                        pessoa.getNome() +
                        "\"?"
        );


        ButtonType resposta =
                alert.showAndWait()
                        .orElse(ButtonType.CANCEL);



        if (resposta == ButtonType.OK) {

            pessoaService.excluir(
                    pessoa.getId()
            );

            voltarParaTabela();

        }
    }
}