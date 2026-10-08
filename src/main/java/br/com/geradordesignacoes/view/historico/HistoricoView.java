package br.com.geradordesignacoes.view.historico;

import br.com.geradordesignacoes.controller.HistoricoController;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.view.escala.ItemEscala;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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

        root.getStyleClass().add(
                "content-area"
        );

        root.setPadding(
                new Insets(24)
        );


        tabela = new TableView<>();

        tabela.getStyleClass().add(
                "table-view"
        );


        tabelaDetalhes = new TableView<>();

        tabelaDetalhes.getStyleClass().add(
                "table-view"
        );


        botaoExcluir =
                new Button(
                        "Excluir escala"
                );

        botaoExcluir.getStyleClass().add(
                "danger-button"
        );


        comboMes =
                new ComboBox<>();

        comboMes.getStyleClass().add(
                "combo-box"
        );


        criarCabecalho();
        criarConteudo();
        criarRodape();

        criarTabela();
        criarTabelaDetalhes();


        controller =
                new HistoricoController(
                        this
                );


        configurarSelecao();

        configurarExclusao();

        configurarFiltro();
    }


    private void criarCabecalho() {

        Label titulo =
                new Label(
                        "Consultar Histórico"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label subtitulo =
                new Label(
                        "Consulte as escalas geradas e visualize as designações de cada semana."
                );

        subtitulo.getStyleClass().add(
                "page-subtitle"
        );


        VBox cabecalho =
                new VBox(
                        6,
                        titulo,
                        subtitulo
                );


        root.setTop(
                cabecalho
        );
    }


    private void criarConteudo() {

        VBox painelEscalas =
                criarPainelEscalas();


        VBox painelDetalhes =
                criarPainelDetalhes();


        HBox conteudo =
                new HBox(
                        16,
                        painelEscalas,
                        painelDetalhes
                );


        HBox.setHgrow(
                painelEscalas,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                painelDetalhes,
                Priority.ALWAYS
        );


        root.setCenter(
                conteudo
        );


        BorderPane.setMargin(
                conteudo,
                new Insets(
                        24,
                        0,
                        0,
                        0
                )
        );
    }


    private VBox criarPainelEscalas() {

        Label titulo =
                new Label(
                        "Escalas geradas"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        Label descricao =
                new Label(
                        "Selecione uma escala para visualizar os detalhes."
                );

        descricao.getStyleClass().add(
                "page-subtitle"
        );


        Label labelMes =
                new Label(
                        "Filtrar por mês"
                );

        labelMes.getStyleClass().add(
                "label"
        );


        comboMes.setPrefWidth(
                220
        );


        HBox filtro =
                new HBox(
                        10,
                        labelMes,
                        comboMes
                );

        filtro.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox.setVgrow(
                tabela,
                Priority.ALWAYS
        );


        VBox painel =
                new VBox(
                        12,
                        titulo,
                        descricao,
                        filtro,
                        tabela
                );


        painel.getStyleClass().add(
                "card"
        );


        return painel;
    }


    private VBox criarPainelDetalhes() {

        Label titulo =
                new Label(
                        "Detalhes da escala"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        Label descricao =
                new Label(
                        "Designações atribuídas na escala selecionada."
                );

        descricao.getStyleClass().add(
                "page-subtitle"
        );


        VBox.setVgrow(
                tabelaDetalhes,
                Priority.ALWAYS
        );


        VBox painel =
                new VBox(
                        12,
                        titulo,
                        descricao,
                        tabelaDetalhes
                );


        painel.getStyleClass().add(
                "card"
        );


        return painel;
    }


    private void criarRodape() {

        Separator separador =
                new Separator();


        HBox rodape =
                new HBox(
                        botaoExcluir
                );


        rodape.setAlignment(
                Pos.CENTER_RIGHT
        );


        VBox conteudo =
                new VBox(
                        12,
                        separador,
                        rodape
                );


        BorderPane.setMargin(
                conteudo,
                new Insets(
                        20,
                        0,
                        0,
                        0
                )
        );


        root.setBottom(
                conteudo
        );
    }


    private void configurarFiltro() {

        comboMes.setOnAction(
                event -> {

                    YearMonth mes =
                            comboMes.getValue();

                    controller.filtrarPorMes(
                            mes
                    );
                }
        );
    }


    private void configurarSelecao() {

        tabela.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (obs,
                         antiga,
                         nova) -> {

                            if (
                                    nova != null
                            ) {

                                controller.carregarDetalhes(
                                        nova
                                );
                            }
                        }
                );
    }


    private void criarTabela() {

        TableColumn<Escala, String> colunaData =
                new TableColumn<>(
                        "Data"
                );


        colunaData.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getData()
                                        .format(
                                                DateTimeFormatter.ofPattern(
                                                        "dd/MM/yyyy"
                                                )
                                        )
                        )
        );


        colunaData.setPrefWidth(
                120
        );


        TableColumn<Escala, String> colunaStatus =
                new TableColumn<>(
                        "Status"
                );


        colunaStatus.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getStatus()
                                        .name()
                        )
        );


        colunaStatus.setCellFactory(
                coluna ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String status,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        status,
                                        empty
                                );


                                if (
                                        empty
                                                || status == null
                                ) {

                                    setText(null);

                                    return;
                                }


                                Label badge =
                                        new Label(
                                                status
                                        );

                                badge.getStyleClass().add(
                                        "badge-blue"
                                );


                                setGraphic(
                                        badge
                                );

                                setText(null);
                            }
                        }
        );


        colunaStatus.setPrefWidth(
                140
        );


        TableColumn<Escala, Number> colunaQuantidade =
                new TableColumn<>(
                        "Designações"
                );


        colunaQuantidade.setCellValueFactory(
                data ->
                        new SimpleIntegerProperty(
                                data.getValue()
                                        .getDesignacoes()
                                        .size()
                        )
        );


        colunaQuantidade.setPrefWidth(
                120
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


        tabela.setPlaceholder(
                new Label(
                        "Nenhuma escala encontrada."
                )
        );
    }


    private void criarTabelaDetalhes() {

        TableColumn<ItemEscala, String> colunaParte =
                new TableColumn<>(
                        "Parte"
                );


        colunaParte.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getParte()
                        )
        );


        TableColumn<ItemEscala, String> colunaResponsavel =
                new TableColumn<>(
                        "Responsável"
                );


        colunaResponsavel.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
                                data.getValue()
                                        .getResponsavel()
                        )
        );


        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>(
                        "Ajudante"
                );


        colunaAjudante.setCellValueFactory(
                data ->
                        new SimpleStringProperty(
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


        tabelaDetalhes.setPlaceholder(
                new Label(
                        "Selecione uma escala para visualizar os detalhes."
                )
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

                        if (
                                mes == null
                        ) {

                            return "";
                        }


                        String nomeMes =
                                mes.getMonth()
                                        .getDisplayName(
                                                TextStyle.FULL,
                                                Locale.of(
                                                        "pt",
                                                        "BR"
                                                )
                                        );


                        String primeiraLetra =
                                nomeMes
                                        .substring(
                                                0,
                                                1
                                        )
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
                        && meses.contains(
                        mesAtual
                )
        ) {

            comboMes.setValue(
                    mesAtual
            );

        } else if (
                !meses.isEmpty()
        ) {

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

                        java.util.stream.IntStream
                                .range(
                                        0,
                                        designacoes.size()
                                )
                                .mapToObj(
                                        i -> {

                                            Designacao designacao =
                                                    designacoes.get(
                                                            i
                                                    );


                                            return new ItemEscala(

                                                    i + 1,

                                                    designacao
                                                            .parte()
                                                            .getNome(),

                                                    designacao
                                                            .responsavel()
                                                            == null
                                                            ? ""
                                                            : designacao
                                                            .responsavel()
                                                            .getNome(),

                                                    designacao
                                                            .ajudante()
                                                            == null
                                                            ? ""
                                                            : designacao
                                                            .ajudante()
                                                            .getNome()
                                            );
                                        }
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
                            tabela
                                    .getSelectionModel()
                                    .getSelectedItem();


                    if (
                            escalaSelecionada == null
                    ) {

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
                            "Excluir escala"
                    );


                    confirmacao.setContentText(
                            "Deseja realmente excluir a escala do dia "
                                    + escalaSelecionada
                                    .getData()
                                    + "?"
                    );


                    confirmacao
                            .showAndWait()
                            .ifPresent(
                                    resposta -> {

                                        if (
                                                resposta
                                                        == ButtonType.OK
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