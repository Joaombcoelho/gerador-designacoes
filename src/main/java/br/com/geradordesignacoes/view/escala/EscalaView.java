package br.com.geradordesignacoes.view.escala;

import br.com.geradordesignacoes.controller.EscalaController;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EscalaView {

    private final BorderPane root;

    private final DatePicker campoData;

    private final Button botaoGerar;

    private final Button botaoGerarNovamente;

    private final Button botaoSalvar;

    private final TableView<ItemEscala> tabela;

    private final EscalaController controller;

    private final Label labelStatus;

    private final Label labelResumo;

    public EscalaView() {

        root = new BorderPane();

        root.setPadding(new Insets(10));

        campoData = new DatePicker();

        botaoGerar = new Button("Gerar Escala");

        botaoGerarNovamente = new Button("Gerar Novamente");

        botaoSalvar = new Button("Salvar Escala");

        labelStatus = new Label("Aguardando geração da escala...");

        labelResumo = new Label("");

        tabela = new TableView<>();

        criarCabecalho();
        criarTabela();
        criarRodape();

        controller = new EscalaController(this);
    }

    private void criarCabecalho() {

        Label titulo = new Label("Geração de Escala");

        titulo.setStyle(
                "-fx-font-size: 20px;"
        );

        HBox linhaData = new HBox(
                10,
                new Label("Data da reunião:"),
                campoData
        );

        linhaData.setAlignment(Pos.CENTER_LEFT);

        HBox linhaBotao = new HBox(botaoGerar);

        linhaBotao.setAlignment(Pos.CENTER);

        VBox painelStatus = new VBox(
                5,
                new Label("Status"),
                labelStatus,
                labelResumo
        );

        painelStatus.setPadding(new Insets(10));

        painelStatus.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-border-color: lightgray;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );

        VBox topo = new VBox(
                15,
                titulo,
                linhaData,
                linhaBotao,
                painelStatus
        );

        root.setTop(topo);

        root.setTop(topo);
    }

    private void criarTabela() {

        TableColumn<ItemEscala, String> colunaParte =
                new TableColumn<>("Parte");

        colunaParte.setCellValueFactory(
                new PropertyValueFactory<>("parte")
        );

        TableColumn<ItemEscala, String> colunaResponsavel =
                new TableColumn<>("Responsável");

        colunaResponsavel.setCellValueFactory(
                new PropertyValueFactory<>("responsavel")
        );

        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>("Ajudante");

        colunaAjudante.setCellValueFactory(
                new PropertyValueFactory<>("ajudante")
        );

        tabela.getColumns().addAll(
                colunaParte,
                colunaResponsavel,
                colunaAjudante
        );

        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        root.setCenter(tabela);
    }

    private void criarRodape() {

        HBox botoes = new HBox(
                10,
                botaoGerarNovamente,
                botaoSalvar
        );

        botoes.setAlignment(Pos.CENTER_RIGHT);

        botoes.setPadding(
                new Insets(15, 0, 0, 0)
        );

        root.setBottom(botoes);
    }

    public Parent getView() {
        return root;
    }

    public DatePicker getCampoData() {
        return campoData;
    }

    public Button getBotaoGerar() {
        return botaoGerar;
    }

    public Button getBotaoGerarNovamente() {
        return botaoGerarNovamente;
    }

    public Button getBotaoSalvar() {
        return botaoSalvar;
    }

    public TableView<ItemEscala> getTabela() {
        return tabela;
    }

    public void atualizarStatus(String mensagem) {

        labelStatus.setText(mensagem);

    }

    public void atualizarResumo(String mensagem) {

        labelResumo.setText(mensagem);

    }
}