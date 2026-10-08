package br.com.geradordesignacoes.view.escala;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.model.TipoParte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EscalaView {

    private final BorderPane root;

    private final DatePicker campoData;

    private final Button botaoGerar;

    private final Button botaoGerarNovamente;

    private final Button botaoSalvar;

    private final TableView<ItemEscala> tabela;

    private final br.com.geradordesignacoes.controller.EscalaController controller;

    private final Label labelStatus;

    private final Label labelResumo;


    public EscalaView() {

        root = new BorderPane();

        root.getStyleClass().add("content-area");

        root.setPadding(
                new Insets(24)
        );


        campoData =
                new DatePicker();

        campoData.getStyleClass().add(
                "date-picker"
        );

        campoData.setPrefWidth(180);


        botaoGerar =
                new Button(
                        "Gerar Escala"
                );

        botaoGerar.getStyleClass().add(
                "primary-button"
        );


        botaoGerarNovamente =
                new Button(
                        "Gerar Novamente"
                );

        botaoGerarNovamente.getStyleClass().add(
                "secondary-button"
        );


        botaoSalvar =
                new Button(
                        "Salvar Escala"
                );

        botaoSalvar.getStyleClass().add(
                "success-button"
        );


        labelStatus =
                new Label(
                        "Aguardando geração das escalas..."
                );

        labelStatus.getStyleClass().add(
                "status-warning"
        );


        labelResumo =
                new Label();

        labelResumo.getStyleClass().add(
                "page-subtitle"
        );


        tabela =
                new TableView<>();

        tabela.getStyleClass().add(
                "table-view"
        );


        criarCabecalho();

        criarTabela();

        criarRodape();


        controller =
                new br.com.geradordesignacoes.controller.EscalaController(
                        this
                );
    }


    private void criarCabecalho() {

        Label titulo =
                new Label(
                        "Geração de Escalas"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label subtitulo =
                new Label(
                        "Gere e acompanhe as designações das reuniões do mês."
                );

        subtitulo.getStyleClass().add(
                "page-subtitle"
        );


        Label labelMes =
                new Label(
                        "Mês da programação"
                );

        labelMes.getStyleClass().add(
                "label"
        );


        campoData.setPromptText(
                "Selecione o mês"
        );


        HBox seletorMes =
                new HBox(
                        10,
                        labelMes,
                        campoData
                );

        seletorMes.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox painelStatus =
                criarPainelStatus();


        VBox topo =
                new VBox(
                        6,
                        titulo,
                        subtitulo,
                        criarEspaco(10),
                        seletorMes,
                        criarEspaco(8),
                        painelStatus
                );


        root.setTop(
                topo
        );
    }


    private VBox criarPainelStatus() {

        Label titulo =
                new Label(
                        "Status da geração"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        VBox painel =
                new VBox(
                        5,
                        titulo,
                        labelStatus,
                        labelResumo
                );

        painel.getStyleClass().add(
                "card"
        );


        return painel;
    }


    private void criarTabela() {

        TableColumn<ItemEscala, String> colunaParte =
                new TableColumn<>(
                        "Parte"
                );


        colunaParte.setCellValueFactory(
                new PropertyValueFactory<>(
                        "parte"
                )
        );


        TableColumn<ItemEscala, String> colunaResponsavel =
                new TableColumn<>(
                        "Responsável / Dirigente"
                );


        colunaResponsavel.setCellValueFactory(
                new PropertyValueFactory<>(
                        "responsavel"
                )
        );


        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>(
                        "Ajudante / Leitor"
                );


        colunaAjudante.setCellValueFactory(
                new PropertyValueFactory<>(
                        "ajudante"
                )
        );


        tabela.getColumns().addAll(
                colunaParte,
                colunaResponsavel,
                colunaAjudante
        );


        colunaParte.setPrefWidth(420);

        colunaResponsavel.setPrefWidth(260);

        colunaAjudante.setPrefWidth(260);


        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        tabela.setPlaceholder(
                new Label(
                        "Nenhuma escala gerada."
                )
        );


        BorderPane.setMargin(
                tabela,
                new Insets(20, 0, 0, 0)
        );


        root.setCenter(
                tabela
        );
    }


    private void criarRodape() {

        Separator separador =
                new Separator();


        HBox botoes =
                new HBox(
                        10,
                        botaoGerarNovamente,
                        botaoSalvar
                );


        botoes.setAlignment(
                Pos.CENTER_RIGHT
        );


        VBox rodape =
                new VBox(
                        12,
                        separador,
                        botoes
                );


        BorderPane.setMargin(
                rodape,
                new Insets(20, 0, 0, 0)
        );


        root.setBottom(
                rodape
        );
    }


    /**
     * Exibe as escalas geradas para as reuniões do mês.
     */
    public void exibirEscalas(
            Map<LocalDate, ResultadoGeracaoEscala> resultados
    ) {

        List<ItemEscala> itens =
                new ArrayList<>();


        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );


        resultados.entrySet()
                .stream()
                .sorted(
                        Map.Entry.comparingByKey()
                )
                .forEach(
                        entrada -> {

                            LocalDate data =
                                    entrada.getKey();


                            ResultadoGeracaoEscala resultado =
                                    entrada.getValue();


                            List<Designacao> designacoes =
                                    resultado.escala()
                                            .getDesignacoes();


                            for (
                                    int indice = 0;
                                    indice < designacoes.size();
                                    indice++
                            ) {

                                Designacao designacao =
                                        designacoes.get(
                                                indice
                                        );


                                if (
                                        designacao.parte()
                                                .getTipo()
                                                == TipoParte.DIRIGENTE_ESTUDO
                                ) {

                                    if (
                                            indice > 0
                                                    && designacoes
                                                    .get(indice - 1)
                                                    .parte()
                                                    .equals(
                                                            designacao.parte()
                                                    )
                                    ) {

                                        continue;
                                    }


                                    String dirigente =
                                            designacoes.stream()
                                                    .filter(
                                                            item ->
                                                                    item.parte()
                                                                            .equals(
                                                                                    designacao.parte()
                                                                            )
                                                    )
                                                    .map(
                                                            Designacao::responsavel
                                                    )
                                                    .filter(
                                                            java.util.Objects::nonNull
                                                    )
                                                    .map(
                                                            br.com.geradordesignacoes.model.Pessoa::getNome
                                                    )
                                                    .findFirst()
                                                    .orElse(
                                                            ""
                                                    );


                                    String leitor =
                                            designacoes.stream()
                                                    .filter(
                                                            item ->
                                                                    item.parte()
                                                                            .equals(
                                                                                    designacao.parte()
                                                                            )
                                                    )
                                                    .map(
                                                            Designacao::ajudante
                                                    )
                                                    .filter(
                                                            java.util.Objects::nonNull
                                                    )
                                                    .map(
                                                            br.com.geradordesignacoes.model.Pessoa::getNome
                                                    )
                                                    .findFirst()
                                                    .orElse(
                                                            ""
                                                    );


                                    itens.add(
                                            new ItemEscala(
                                                    indice,
                                                    data.format(
                                                            formatter
                                                    )
                                                            + " - "
                                                            + designacao
                                                            .parte()
                                                            .getNome(),
                                                    dirigente,
                                                    leitor
                                            )
                                    );


                                    continue;
                                }


                                String parte =
                                        data.format(
                                                formatter
                                        )
                                                + " - "
                                                + designacao.parte()
                                                .getNome();


                                String responsavel =
                                        designacao.responsavel()
                                                == null
                                                ? ""
                                                : designacao.responsavel()
                                                .getNome();


                                String ajudante =
                                        designacao.ajudante()
                                                == null
                                                ? ""
                                                : designacao.ajudante()
                                                .getNome();


                                itens.add(
                                        new ItemEscala(
                                                indice,
                                                parte,
                                                responsavel,
                                                ajudante
                                        )
                                );
                            }
                        }
                );


        tabela.getItems().setAll(
                itens
        );


        labelStatus.setText(
                "Escalas geradas com sucesso."
        );

        labelStatus.getStyleClass().removeAll(
                "status-warning",
                "status-error"
        );

        labelStatus.getStyleClass().add(
                "status-success"
        );


        labelResumo.setText(
                resultados.size()
                        + " reunião(ões) gerada(s)."
        );
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


    public void atualizarStatus(
            String mensagem
    ) {

        labelStatus.setText(
                mensagem
        );
    }


    public void atualizarResumo(
            String mensagem
    ) {

        labelResumo.setText(
                mensagem
        );
    }


    public br.com.geradordesignacoes.controller.EscalaController getController() {

        return controller;
    }


    private VBox criarEspaco(
            double altura
    ) {

        VBox espaco =
                new VBox();

        espaco.setMinHeight(
                altura
        );

        espaco.setPrefHeight(
                altura
        );

        espaco.setMaxHeight(
                altura
        );

        return espaco;
    }
}