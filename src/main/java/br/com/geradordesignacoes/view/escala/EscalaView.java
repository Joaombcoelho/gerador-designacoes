package br.com.geradordesignacoes.view.escala;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
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

        root.setPadding(
                new Insets(10)
        );


        campoData =
                new DatePicker();


        botaoGerar =
                new Button(
                        "Gerar Escala"
                );


        botaoGerarNovamente =
                new Button(
                        "Gerar Novamente"
                );


        botaoSalvar =
                new Button(
                        "Salvar Escala"
                );


        labelStatus =
                new Label(
                        "Aguardando geração das escalas..."
                );


        labelResumo =
                new Label();


        tabela =
                new TableView<>();


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


        titulo.setStyle(
                "-fx-font-size: 20px;"
        );


        HBox linhaData =
                new HBox(
                        10,
                        new Label("Mês:"),
                        campoData
                );


        linhaData.setAlignment(
                Pos.CENTER_LEFT
        );


        HBox linhaBotao =
                new HBox(
                        botaoGerar
                );


        linhaBotao.setAlignment(
                Pos.CENTER
        );


        VBox painelStatus =
                new VBox(
                        5,
                        new Label("Status"),
                        labelStatus,
                        labelResumo
                );


        painelStatus.setPadding(
                new Insets(10)
        );


        painelStatus.setStyle(
                "-fx-background-color: #F5F5F5;" +
                        "-fx-border-color: lightgray;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;"
        );


        VBox topo =
                new VBox(
                        15,
                        titulo,
                        linhaData,
                        linhaBotao,
                        painelStatus
                );


        root.setTop(
                topo
        );
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
                        "Responsável"
                );


        colunaResponsavel.setCellValueFactory(
                new PropertyValueFactory<>(
                        "responsavel"
                )
        );


        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>(
                        "Ajudante"
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


        tabela.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );


        root.setCenter(
                tabela
        );
    }


    private void criarRodape() {

        HBox botoes =
                new HBox(
                        10,
                        botaoGerarNovamente,
                        botaoSalvar
                );


        botoes.setAlignment(
                Pos.CENTER_RIGHT
        );


        botoes.setPadding(
                new Insets(
                        15,
                        0,
                        0,
                        0
                )
        );


        root.setBottom(
                botoes
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
                                        designacoes.get(indice);

                                String parte =
                                        data.format(
                                                formatter
                                        )
                                                + " - "
                                                + designacao.parte()
                                                .getNome();


                                String responsavel =
                                        designacao.responsavel()
                                                .getNome();


                                String ajudante =
                                        designacao.ajudante() == null
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
}