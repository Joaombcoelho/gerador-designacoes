package br.com.geradordesignacoes.view.historico;


import br.com.geradordesignacoes.controller.HistoricoController;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.view.escala.ItemEscala;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.SplitPane;

import java.util.List;


public class HistoricoView {


    private final BorderPane root;

    private final TableView<Escala> tabela;

    private final TableView<ItemEscala> tabelaDetalhes;

    private final HistoricoController controller;

    private final Button botaoExcluir;


    public HistoricoView() {


        root = new BorderPane();


        tabela = new TableView<>();

        tabelaDetalhes = new TableView<>();

        botaoExcluir = new Button("Excluir escala");

        criarTabela();

        criarTabelaDetalhes();


        SplitPane splitPane =
                new SplitPane();


        splitPane.getItems()
                .addAll(
                        tabela,
                        tabelaDetalhes
                );


        root.setCenter(splitPane);

        root.setBottom(botaoExcluir);



        controller =
                new HistoricoController(this);


        configurarSelecao();

        configurarExclusao();
    }



    private void criarTabela() {


        TableColumn<Escala, String> colunaData =
                new TableColumn<>("Data");


        colunaData.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue()
                                        .getData()
                                        .toString()
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
                                data.getValue().getParte()
                        )
        );


        TableColumn<ItemEscala, String> colunaResponsavel =
                new TableColumn<>("Responsável");


        colunaResponsavel.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getResponsavel()
                        )
        );


        TableColumn<ItemEscala, String> colunaAjudante =
                new TableColumn<>("Ajudante");


        colunaAjudante.setCellValueFactory(
                data ->
                        new javafx.beans.property.SimpleStringProperty(
                                data.getValue().getAjudante()
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



    public void carregarDetalhes(
            List<Designacao> designacoes
    ) {


        tabelaDetalhes.setItems(

                FXCollections.observableArrayList(

                        designacoes.stream()
                                .map(designacao ->
                                        new ItemEscala(

                                                designacao.parte().getNome(),

                                                designacao.responsavel().getNome(),

                                                designacao.ajudante() == null
                                                        ?
                                                        ""
                                                        :
                                                        designacao.ajudante().getNome()
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

        botaoExcluir.setOnAction(event -> {

            Escala escalaSelecionada =
                    tabela.getSelectionModel()
                            .getSelectedItem();


            if (escalaSelecionada == null) {

                Alert alerta = new Alert(
                        Alert.AlertType.WARNING
                );

                alerta.setTitle("Nenhuma escala selecionada");
                alerta.setHeaderText(null);
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

            confirmacao.setTitle("Excluir escala");
            confirmacao.setHeaderText(null);

            confirmacao.setContentText(
                    "Deseja realmente excluir a escala do dia "
                            + escalaSelecionada.getData()
                            + "?"
            );


            confirmacao.showAndWait()
                    .ifPresent(resposta -> {

                        if (resposta == ButtonType.OK) {

                            controller.excluirEscala(
                                    escalaSelecionada
                            );

                            tabelaDetalhes.getItems()
                                    .clear();
                        }

                    });

        });
    }

}