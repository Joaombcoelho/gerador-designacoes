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
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParteView {

    private static final Logger LOGGER = Logger.getLogger(ParteView.class.getName());

    private final BorderPane root;
    private final TableView<Parte> tabela;
    private final ParteDAO parteDAO;
    private final ObservableList<Parte> dados;
    private TabPane abas;
    private Tab abaAtivos;
    private Tab abaArquivados;
    private boolean exibindoArquivados;
    private Button botaoNovo;
    private Button botaoEditar;
    private Button botaoArquivar;

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
        botaoNovo = novo;
        novo.getStyleClass().add("primary-button");
        novo.setOnAction(_ -> abrirFormulario(null));

        Button editar = new Button("Editar");
        botaoEditar = editar;
        editar.getStyleClass().add("secondary-button");
        editar.setOnAction(_ -> editarSelecionado());

        Button excluir = new Button("Arquivar");
        botaoArquivar = excluir;
        excluir.getStyleClass().add("danger-button");
        excluir.setOnAction(_ -> excluirSelecionado());

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
        abaAtivos = new Tab("Ativos");
        abaArquivados = new Tab("Arquivados");
        abaAtivos.setClosable(false);
        abaArquivados.setClosable(false);
        abaAtivos.setContent(painelTabela);
        abas = new TabPane(abaAtivos, abaArquivados);
        abas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, selecionada) -> {
            exibindoArquivados = selecionada == abaArquivados;
            if (anterior != null) anterior.setContent(null);
            selecionada.setContent(criarPainelTabela());
            if (botaoNovo != null) botaoNovo.setDisable(exibindoArquivados);
            if (botaoEditar != null) botaoEditar.setDisable(exibindoArquivados);
            if (botaoArquivar != null) botaoArquivar.setText(exibindoArquivados ? "Restaurar" : "Arquivar");
            carregarDados();
        });
        root.setCenter(abas);
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
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        tabela.getStyleClass().add("table-view");
        tabela.setPlaceholder(
                new Label("Nenhuma parte cadastrada.")
        );
    }

    private void carregarDados() {
        dados.setAll(exibindoArquivados ? parteDAO.listarArquivados() : parteDAO.listarAtivos());
    }

    private void abrirFormulario(Parte parte) {
        ParteFormularioView formulario = new ParteFormularioView(
                parte,
                _ -> {
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
            mostrarAviso(exibindoArquivados ? "Selecione uma parte para restaurar." : "Selecione uma parte para arquivar.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                exibindoArquivados
                        ? "Deseja restaurar a parte " + selecionada.getNome() + " para os cadastros ativos?"
                        : "Deseja arquivar a parte " + selecionada.getNome() + "? O histórico será preservado.",
                ButtonType.CANCEL, ButtonType.OK);
        prepararAlerta(confirmacao);
        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            if (exibindoArquivados) parteDAO.restaurar(selecionada.getId());
            else parteDAO.arquivar(selecionada.getId());
            carregarDados();
        } catch (RuntimeException erro) {
            LOGGER.log(Level.WARNING, "Não foi possível alterar o arquivamento da parte.", erro);
            mostrarAviso(erro instanceof IllegalStateException && erro.getMessage() != null
                    ? erro.getMessage()
                    : "Não foi possível atualizar a parte. Verifique os vínculos e tente novamente.");
        }
    }

    private void mostrarAviso(String mensagem) {
        Alert alerta = new Alert(
                Alert.AlertType.WARNING,
                mensagem,
                ButtonType.OK
        );

        alerta.setTitle("Atenção");
        alerta.setHeaderText(null);
        prepararAlerta(alerta);
        alerta.showAndWait();
    }

    private void prepararAlerta(Alert alerta) {
        if (alerta.getTitle() == null || alerta.getTitle().isBlank()) {
            alerta.setTitle("Confirmação");
        }
        if (root.getScene() != null && root.getScene().getWindow() != null) {
            alerta.initOwner(root.getScene().getWindow());
        }
    }

    public Parent getView() {
        return root;
    }

    private void voltarParaTabela() {
        carregarDados();
        root.setCenter(abas);
    }

    private VBox criarPainelTabela() {
        VBox painel = new VBox(tabela);
        painel.getStyleClass().add("card");
        painel.setPadding(new Insets(12));
        VBox.setVgrow(tabela, Priority.ALWAYS);
        return painel;
    }
}