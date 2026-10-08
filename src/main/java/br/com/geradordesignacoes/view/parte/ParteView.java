package br.com.geradordesignacoes.view.parte;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ParteView {

    private final BorderPane root;
    private final TableView<Parte> tabela;
    private final ParteDAO parteDAO;
    private final ObservableList<Parte> dados;

    public ParteView() {
        parteDAO = new ParteDAO();

        root = new BorderPane();
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        tabela = new TableView<>();
        dados = FXCollections.observableArrayList();

        configurarTabela();
        montarTela();
        carregarDados();
    }

    private void montarTela() {
        Label titulo = new Label("Partes");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Gerencie as partes e suas configurações para as reuniões."
        );
        subtitulo.getStyleClass().add("page-subtitle");

        VBox textos = new VBox(6, titulo, subtitulo);

        Button novo = new Button("+ Nova Parte");
        novo.getStyleClass().add("primary-button");
        novo.setOnAction(e -> abrirFormulario(null));

        Button editar = new Button("Editar");
        editar.getStyleClass().add("secondary-button");
        editar.setOnAction(e -> editarSelecionado());

        Button excluir = new Button("Excluir");
        excluir.getStyleClass().add("danger-button");
        excluir.setOnAction(e -> excluirSelecionado());

        HBox botoes = new HBox(10, novo, editar, excluir);
        botoes.setAlignment(Pos.CENTER_LEFT);

        HBox cabecalho = new HBox(20, textos, criarEspacador(), botoes);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.setPadding(new Insets(0, 0, 22, 0));

        VBox painelTabela = new VBox(tabela);
        painelTabela.getStyleClass().add("card");
        painelTabela.setPadding(new Insets(12));
        VBox.setVgrow(tabela, Priority.ALWAYS);

        root.setTop(cabecalho);
        root.setCenter(painelTabela);
    }

    private Region criarEspacador() {
        Region espacador = new Region();
        HBox.setHgrow(espacador, Priority.ALWAYS);
        return espacador;
    }

    private void configurarTabela() {
        TableColumn<Parte, String> colunaNome =
                new TableColumn<>("Nome");

        colunaNome.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNome())
        );

        TableColumn<Parte, String> colunaTipo =
                new TableColumn<>("Tipo");

        colunaTipo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTipo() == null
                                ? ""
                                : data.getValue().getTipo().toString()
                )
        );

        TableColumn<Parte, String> colunaPrivilegio =
                new TableColumn<>("Privilégio mínimo");

        colunaPrivilegio.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getPrivilegioMinimo() == null
                                ? ""
                                : data.getValue().getPrivilegioMinimo().toString()
                )
        );

        TableColumn<Parte, String> colunaVariacao =
                new TableColumn<>("Variação");

        colunaVariacao.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTipoVariacao() == null
                                ? ""
                                : data.getValue().getTipoVariacao().toString()
                )
        );

        tabela.getColumns().setAll(
                colunaNome,
                colunaTipo,
                colunaPrivilegio,
                colunaVariacao
        );

        tabela.setItems(dados);
        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        tabela.getStyleClass().add("table-view");
        tabela.setPlaceholder(
                new Label("Nenhuma parte cadastrada.")
        );
    }

    private void carregarDados() {
        dados.setAll(parteDAO.listarTodos());
    }

    private void abrirFormulario(Parte parte) {
        ParteFormularioView formulario = new ParteFormularioView(
                parte,
                resultado -> {
                    carregarDados();
                    voltarParaTabela();
                },
                this::voltarParaTabela
        );

        root.setCenter(formulario.getView());
    }

    private void editarSelecionado() {
        Parte selecionada = tabela.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAviso("Selecione uma parte para editar.");
            return;
        }

        abrirFormulario(selecionada);
    }


    private void excluirSelecionado() {
        Parte selecionada = tabela.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            mostrarAviso("Selecione uma parte para excluir.");
            return;
        }

        VBox painelAtual = (VBox) root.getCenter();

        // Evita exibir mais de uma confirmação.
        painelAtual.getChildren().removeIf(
                node -> node.getProperties().containsKey("confirmacao-exclusao")
        );

        Label mensagem = new Label(
                "Deseja realmente excluir a parte \""
                        + selecionada.getNome() + "\"?"
        );
        mensagem.getStyleClass().add("page-subtitle");

        Button confirmar = new Button("Confirmar exclusão");
        confirmar.getStyleClass().add("danger-button");

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("secondary-button");

        HBox botoesConfirmacao = new HBox(10, confirmar, cancelar);
        botoesConfirmacao.setAlignment(Pos.CENTER_RIGHT);

        VBox confirmacao = new VBox(12, mensagem, botoesConfirmacao);
        confirmacao.getStyleClass().add("card");
        confirmacao.setPadding(new Insets(16));
        confirmacao.getProperties().put("confirmacao-exclusao", true);

        confirmar.setOnAction(event -> {
            painelAtual.getChildren().remove(confirmacao);
            parteDAO.excluir(selecionada.getId());
            carregarDados();
        });

        cancelar.setOnAction(event ->
                painelAtual.getChildren().remove(confirmacao)
        );

        painelAtual.getChildren().add(confirmacao);
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(
                Alert.AlertType.WARNING,
                mensagem,
                ButtonType.OK
        );

        alerta.setTitle("Atenção");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    public Parent getView() {
        return root;
    }

    private void voltarParaTabela() {
        carregarDados();
        root.setCenter(criarPainelTabela());
    }

    private VBox criarPainelTabela() {
        VBox painel = new VBox(tabela);
        painel.getStyleClass().add("card");
        painel.setPadding(new Insets(12));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return painel;
    }
}