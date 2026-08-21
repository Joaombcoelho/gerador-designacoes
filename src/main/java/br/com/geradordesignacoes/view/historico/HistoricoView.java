package br.com.geradordesignacoes.view.historico;

import br.com.geradordesignacoes.controller.HistoricoController;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.view.escala.ItemEscala;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.SplitPane;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;


public class HistoricoView {

    private final BorderPane root;

    private final TableView<Escala> tabela;

    private final TableView<ItemEscala> tabelaDetalhes;

    private final HistoricoController controller;

    private final Button botaoExcluir;

    private final ComboBox<YearMonth> comboMes;


    public HistoricoView() {

        root = new BorderPane();

        tabela = new TableView<>();

        tabelaDetalhes = new TableView<>();

        botaoExcluir =
                new Button("Excluir escala");

        comboMes =
                new ComboBox<>();


        criarFiltro();

        criarTabela();

        criarTabelaDetalhes();


        SplitPane splitPane =
                new SplitPane();


        splitPane.getItems()
                .addAll(
                        tabela,
                        tabelaDetalhes
                );


        root.setCenter(
                splitPane
        );

        root.setBottom(
                botaoExcluir
        );


        controller =
                new HistoricoController(this);


        configurarSelecao();

        configurarExclusao();

        configurarFiltro();
    }


    private void criarFiltro() {

        Label label =
                new Label("Mês:");

        comboMes.setPrefWidth(180);

        HBox filtro =
                new HBox(
                        10,
                        label,
                        comboMes
                );

        filtro.setAlignment(
                Pos.CENTER_LEFT
        );

        filtro.setPadding(
                new Insets(10)
        );

        root.setTop(
                filtro
        );
    }


    private void configurarFiltro() {

        comboMes.setOnAction(
                event -> {

                    YearMonth mes =
                            comboMes
                                    .getValue();

                    controller.filtrarPorMes(
                            mes
                    );
                }
        );
    }


    private void criarTabela() {

        TableColumn<Escala, String> colunaData =
                new TableColumn<>("Data");


        colunaData.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getData()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "dd/MM/yyyy"
                                                )
                                        )
                        )
        );


        TableColumn<Escala, String> colunaStatus =
                new TableColumn<>("Status");


        colunaStatus.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getStatus()
                                        .name()
                        )
        );


        TableColumn<Escala, Number> colunaQuantidade =
                new TableColumn<>("Designações");


        colunaQuantidade.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleIntegerProperty(
                                data.getValue()
                                        .getDesignacoes()
                                        .size()
                        )
        );


        tabela.getColumns()
                .addAll(
                        colunaData,
                        colunaStatus,
                        colunaQuantidade
                );


        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }


    private void configurarSelecao() {

        tabela.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs, antiga, nova) -> {

                            if (nova != null) {

                                controller.carregarDetalhes(
                                        nova
                                );
                            }

                        }
                );
    }


    private void criarTabelaDetalhes() {

        TableColumn<ItemEscala, String> colunaParte =
                new TableColumn<>("Parte");


        colunaParte.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getParte()
                        )
        );


        TableColumn<ItemEscala, String> colunaResponsavel =
                new TableColumn<>("Responsável");


        colunaResponsavel.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getResponsavel()
                        )
        );


        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>("Ajudante");


        colunaAjudante.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getAjudante()
                        )
        );


        tabelaDetalhes.getColumns()
                .addAll(
                        colunaParte,
                        colunaResponsavel,
                        colunaAjudante
                );


        tabelaDetalhes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }


    public void carregarEscalas(
            List<Escala> escalas
    ) {

        tabela.setItems(
                FXCollections.observableArrayList(
                        escalas
                )
        );
    }


    public void atualizarMeses(
            List<Escala> escalas
    ) {

        YearMonth mesAtual =
                comboMes.getValue();


        List<YearMonth> meses =
                escalas.stream()
                        .map(
                                escala ->
                                        YearMonth.from(
                                                escala.getData()
                                        )
                        )
                        .distinct()
                        .sorted()
                        .toList();


        comboMes.setItems(
                FXCollections.observableArrayList(
                        meses
                )
        );


        comboMes.setConverter(
                new javafx.util.StringConverter<>() {

                    @Override
                    public String toString(
                            YearMonth mes
                    ) {

                        if (mes == null) {
                            return "";
                        }

                        String nomeMes =
                                mes.getMonth()
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.of("pt", "BR")
                                        );

                        String primeiraLetra =
                                nomeMes.substring(0, 1)
                                        .toUpperCase();

                        nomeMes =
                                primeiraLetra
                                        + nomeMes.substring(1);

                        return nomeMes
                                + " "
                                + mes.getYear();
                    }


                    @Override
                    public YearMonth fromString(
                            String string
                    ) {

                        return null;
                    }
                }
        );


        if (
                mesAtual != null
                        && meses.contains(mesAtual)
        ) {

            comboMes.setValue(
                    mesAtual
            );

        } else if (!meses.isEmpty()) {

            comboMes.setValue(
                    meses.get(
                            meses.size() - 1
                    )
            );
        }
    }


    public void carregarDetalhes(
            List<Designacao> designacoes
    ) {

        tabelaDetalhes.setItems(

                FXCollections.observableArrayList(

                        designacoes.stream()
                                .map(
                                        designacao ->
                                                new ItemEscala(

                                                        designacao
                                                                .parte()
                                                                .getNome(),

                                                        designacao
                                                                .responsavel()
                                                                .getNome(),

                                                        designacao
                                                                .ajudante()
                                                                == null
                                                                ? ""
                                                                : designacao
                                                                .ajudante()
                                                                .getNome()
                                                )
                                )
                                .toList()
                )
        );
    }


    public void atualizar() {

        controller.atualizarHistorico();
    }


    public Parent getView() {

        return root;
    }


    private void configurarExclusao() {

        botaoExcluir.setOnAction(
                event -> {

                    Escala escalaSelecionada =
                            tabela.getSelectionModel()
                                    .getSelectedItem();


                    if (escalaSelecionada == null) {

                        Alert alerta =
                                new Alert(
                                        Alert.AlertType.WARNING
                                );

                        alerta.setTitle(
                                "Nenhuma escala selecionada"
                        );

                        alerta.setHeaderText(
                                null
                        );

                        alerta.setContentText(
                                "Selecione uma escala para excluir."
                        );

                        alerta.showAndWait();

                        return;
                    }


                    Alert confirmacao =
                            new Alert(
                                    Alert.AlertType.CONFIRMATION
                            );

                    confirmacao.setTitle(
                            "Excluir escala"
                    );

                    confirmacao.setHeaderText(
                            null
                    );

                    confirmacao.setContentText(
                            "Deseja realmente excluir a escala do dia "
                                    + escalaSelecionada.getData()
                                    + "?"
                    );


                    confirmacao.showAndWait()
                            .ifPresent(
                                    resposta -> {

                                        if (
                                                resposta ==
                                                        ButtonType.OK
                                        ) {

                                            controller.excluirEscala(
                                                    escalaSelecionada
                                            );

                                            tabelaDetalhes
                                                    .getItems()
                                                    .clear();
                                        }

                                    }
                            );

                }
        );
    }
}