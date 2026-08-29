package br.com.geradordesignacoes.view.edicao;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Pessoa;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Tela mensal de edição de escalas salvas. */
public class EdicaoEscalaView {

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BorderPane root = new BorderPane();
    private final DatePicker campoMes = new DatePicker();
    private final ListView<Escala> listaSemanas = new ListView<>();
    private final ListView<Designacao> listaDesignacoes = new ListView<>();
    private final ComboBox<Pessoa> comboResponsavel = new ComboBox<>();
    private final ComboBox<Pessoa> comboAjudante = new ComboBox<>();
    private final Button botaoSalvar = new Button("Salvar Alterações");
    private final Label labelStatus = new Label("Selecione um mês para visualizar as semanas.");
    private final Label labelSemanaSelecionada = new Label("Nenhuma semana selecionada.");
    private final EdicaoEscalaController controller = new EdicaoEscalaController();
    private Designacao designacaoSelecionada;

    public EdicaoEscalaView() {
        root.setPadding(new Insets(10));
        botaoSalvar.setDisable(true);
        labelSemanaSelecionada.setStyle("-fx-font-weight: bold;");
        configurarListas();
        carregarPessoas();
        registrarEventos();

        Button botaoCarregar = new Button("Carregar Semanas");
        botaoCarregar.setOnAction(event -> carregarSemanas());
        campoMes.setOnAction(event -> carregarSemanas());
        HBox cabecalho = new HBox(10, new Label("Mês:"), campoMes, botaoCarregar);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        root.setTop(new VBox(10, new Label("Edição de Escalas"), cabecalho));

        VBox semanas = new VBox(8, new Label("Semanas disponíveis"), listaSemanas);
        semanas.setPrefWidth(250);
        VBox.setVgrow(listaSemanas, Priority.ALWAYS);
        root.setLeft(semanas);

        VBox designacoes = new VBox(8, labelSemanaSelecionada, new Label("Designações"), listaDesignacoes);
        designacoes.setPadding(new Insets(0, 10, 0, 15));
        VBox.setVgrow(listaDesignacoes, Priority.ALWAYS);
        root.setCenter(designacoes);

        VBox edicao = new VBox(8, new Label("Editar designação"),
                new Label("Responsável:"), comboResponsavel,
                new Label("Ajudante:"), comboAjudante, botaoSalvar);
        edicao.setPrefWidth(250);
        root.setRight(edicao);
        root.setBottom(new HBox(labelStatus));
    }

    private void configurarListas() {
        listaSemanas.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(Escala escala, boolean empty) {
                super.updateItem(escala, empty);
                setText(empty || escala == null ? null : "Semana - " + escala.getData().format(FORMATADOR));
            }
        });
        listaDesignacoes.setCellFactory(lista -> new ListCell<>() {
            @Override protected void updateItem(Designacao designacao, boolean empty) {
                super.updateItem(designacao, empty);
                setText(empty || designacao == null ? null : designacao.parte().getNome()
                        + "\nResponsável: " + designacao.responsavel().getNome()
                        + "\nAjudante: " + (designacao.ajudante() == null ? "-" : designacao.ajudante().getNome()));
            }
        });
        StringConverter<Pessoa> conversor = new StringConverter<>() {
            @Override public String toString(Pessoa pessoa) { return pessoa == null ? "" : pessoa.getNome(); }
            @Override public Pessoa fromString(String texto) { return null; }
        };
        comboResponsavel.setConverter(conversor);
        comboAjudante.setConverter(conversor);
    }

    private void registrarEventos() {
        listaSemanas.getSelectionModel().selectedItemProperty().addListener((obs, antiga, escala) -> selecionarEscala(escala));
        listaDesignacoes.getSelectionModel().selectedItemProperty().addListener((obs, antiga, designacao) -> {
            designacaoSelecionada = designacao;
            if (designacao != null) {
                comboResponsavel.setValue(designacao.responsavel());
                comboAjudante.setValue(designacao.ajudante());
            }
            botaoSalvar.setDisable(designacao == null);
        });
        botaoSalvar.setOnAction(event -> salvarAlteracoes());
    }

    private void carregarPessoas() {
        List<Pessoa> pessoas = new PessoaDAO().listarTodos();
        comboResponsavel.setItems(FXCollections.observableArrayList(pessoas));
        comboAjudante.setItems(FXCollections.observableArrayList(pessoas));
    }

    private void carregarSemanas() {
        LocalDate data = campoMes.getValue();
        if (data == null) { limparSelecao(); labelStatus.setText("Selecione um mês."); return; }
        List<Escala> escalas = controller.listarEscalasDoMes(YearMonth.from(data));
        listaSemanas.setItems(FXCollections.observableArrayList(escalas));
        limparSelecao();
        labelStatus.setText(escalas.isEmpty() ? "Nenhuma escala encontrada." : escalas.size() + " semana(s) encontrada(s).");
    }

    private void selecionarEscala(Escala escala) {
        if (escala == null) { return; }
        Escala carregada = controller.selecionarEscala(escala.getId());
        labelSemanaSelecionada.setText("Semana: " + carregada.getData().format(FORMATADOR));
        listaDesignacoes.setItems(FXCollections.observableArrayList(controller.listarDesignacoesSelecionadas()));
        designacaoSelecionada = null;
        botaoSalvar.setDisable(true);
    }

    private void salvarAlteracoes() {
        Pessoa responsavel = comboResponsavel.getValue();
        Pessoa ajudante = comboAjudante.getValue();
        if (designacaoSelecionada == null || responsavel == null) { return; }
        List<LocalDate> conflitos = controller.verificarConflitos(designacaoSelecionada.id(), responsavel, ajudante);
        if (!conflitos.isEmpty() && !confirmarConflitos(responsavel, ajudante, conflitos)) {
            comboResponsavel.setValue(designacaoSelecionada.responsavel());
            comboAjudante.setValue(designacaoSelecionada.ajudante());
            return;
        }
        controller.salvarAlteracoes(designacaoSelecionada.id(), responsavel, ajudante);
        listaDesignacoes.setItems(FXCollections.observableArrayList(controller.listarDesignacoesSelecionadas()));
        labelStatus.setText("Alterações salvas com sucesso.");
    }

    private boolean confirmarConflitos(Pessoa responsavel, Pessoa ajudante, List<LocalDate> datas) {
        String pessoas = ajudante == null || ajudante.getId().equals(responsavel.getId()) ? responsavel.getNome() : responsavel.getNome() + " e/ou " + ajudante.getNome();
        String datasFormatadas = datas.stream().map(data -> "• " + data.format(FORMATADOR)).collect(java.util.stream.Collectors.joining("\n"));
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Conflito de designações");
        alerta.setHeaderText(null);
        alerta.setContentText(pessoas + " já possui outras designações neste mês:\n\n" + datasFormatadas + "\n\nDeseja realmente atribuir esta nova designação?");
        return alerta.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private void limparSelecao() {
        listaDesignacoes.getItems().clear();
        designacaoSelecionada = null;
        comboResponsavel.setValue(null);
        comboAjudante.setValue(null);
        botaoSalvar.setDisable(true);
    }

    public Parent getView() { return root; }

    public DatePicker getCampoMes() { return campoMes; }

    public ListView<Escala> getListaSemanas() { return listaSemanas; }

    public ListView<Designacao> getListaDesignacoes() { return listaDesignacoes; }

    public Label getLabelStatus() { return labelStatus; }
}
