package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.service.PessoaService;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class PessoaView {

    private final BorderPane root;
    private final PessoaService pessoaService;

    private final TableView<Pessoa> tabela;

    public PessoaView(PessoaService pessoaService) {

        this.pessoaService = pessoaService;

        root = new BorderPane();

        tabela = new TableView<>();

        criarCabecalho();
        criarTabela();
        criarBotoes();
        carregarPessoas();
    }

    private void criarCabecalho() {

        Label titulo = new Label("Cadastro de Pessoas");

        titulo.setStyle(
                "-fx-font-size: 20px;"
        );

        root.setTop(titulo);
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


        root.setCenter(tabela);
    }


    private void criarBotoes() {

        Button novo = new Button("Novo");
        Button editar = new Button("Editar");
        Button excluir = new Button("Excluir");


        HBox botoes = new HBox(
                10,
                novo,
                editar,
                excluir
        );


        botoes.setAlignment(Pos.CENTER);


        root.setBottom(botoes);
    }


    public Parent getView() {
        return root;
    }

    private void carregarPessoas() {

        tabela.getItems().addAll(
                pessoaService.listarTodas()
        );

    }
}