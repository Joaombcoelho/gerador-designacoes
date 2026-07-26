package br.com.geradordesignacoes.view.parte;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class ParteView {

    private final BorderPane root;

    private final TableView<Parte> tabela;

    private final ParteDAO parteDAO;

    private final ObservableList<Parte> dados;


    public ParteView() {

        this.parteDAO = new ParteDAO();

        this.root = new BorderPane();

        this.tabela = new TableView<>();

        this.dados = FXCollections.observableArrayList();


        configurarTabela();

        montarTela();

        carregarDados();
    }


    private void montarTela() {

        root.setPadding(new Insets(10));

        Button novo = new Button("Novo");

        Button editar = new Button("Editar");

        Button excluir = new Button("Excluir");


        novo.setOnAction(e -> abrirFormulario(null));

        editar.setOnAction(e -> editarSelecionado());

        excluir.setOnAction(e -> excluirSelecionado());


        ToolBar barra = new ToolBar(
                novo,
                editar,
                excluir
        );


        root.setTop(barra);

        root.setCenter(tabela);
    }


    private void configurarTabela() {

        TableColumn<Parte, String> colunaNome =
                new TableColumn<>("Nome");

        colunaNome.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNome()
                )
        );


        TableColumn<Parte, String> colunaTipo =
                new TableColumn<>("Tipo");

        colunaTipo.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getTipo().toString()
                )
        );


        TableColumn<Parte, String> colunaPrivilegio =
                new TableColumn<>("Privilégio");

        colunaPrivilegio.setCellValueFactory(
                data -> new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPrivilegioMinimo().toString()
                )
        );


        tabela.getColumns().addAll(
                colunaNome,
                colunaTipo,
                colunaPrivilegio
        );


        tabela.setItems(dados);

        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }


    private void carregarDados() {

        dados.clear();

        dados.addAll(
                parteDAO.listarTodos()
        );
    }

    private void abrirFormulario(Parte parte) {

        ParteFormularioView formulario =
                new ParteFormularioView(
                        parte,
                        resultado -> {
                            carregarDados();
                            voltarParaTabela();
                        },
                        this::voltarParaTabela
                );


        root.setCenter(
                formulario.getView()
        );
    }


    private void editarSelecionado() {

        Parte selecionada =
                tabela.getSelectionModel()
                        .getSelectedItem();


        if (selecionada == null) {

            mostrarAviso(
                    "Selecione uma Parte para editar."
            );

            return;
        }


        abrirFormulario(selecionada);
    }


    private void excluirSelecionado() {

        Parte selecionada =
                tabela.getSelectionModel()
                        .getSelectedItem();


        if (selecionada == null) {

            mostrarAviso(
                    "Selecione uma Parte para excluir."
            );

            return;
        }


        Alert alerta = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Excluir a Parte: "
                        + selecionada.getNome()
                        + "?",
                ButtonType.YES,
                ButtonType.NO
        );


        alerta.showAndWait()
                .ifPresent(resposta -> {

                    if (resposta == ButtonType.YES) {

                        parteDAO.excluir(
                                selecionada.getId()
                        );

                        carregarDados();
                    }
                });
    }


    private void mostrarAviso(String mensagem) {

        Alert alerta = new Alert(
                Alert.AlertType.WARNING,
                mensagem,
                ButtonType.OK
        );

        alerta.showAndWait();
    }


    public Parent getView() {

        return root;
    }

    private void voltarParaTabela() {

        carregarDados();

        root.setCenter(tabela);

    }
}