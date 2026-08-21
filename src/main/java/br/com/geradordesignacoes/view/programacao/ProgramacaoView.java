package br.com.geradordesignacoes.view.programacao;

import br.com.geradordesignacoes.model.Parte;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ProgramacaoView {

    private final BorderPane root;

    private final DatePicker campoData;

    private final ListView<LocalDate> listaSemanas;

    private final ObservableList<LocalDate> semanas;

    private final Map<LocalDate, Boolean> statusSemanas;

    private final Button botaoAdicionarSemana;

    private final Button botaoEditarSemana;

    private final Button botaoGerar;

    private final Button botaoSalvar;

    /*
     * Lista única das partes variáveis.
     * Cada parte será exibida com um CheckBox.
     */
    private final ListView<Parte> listaPartes;

    private final TextField campoTema;

    private final Button botaoSalvarTema;

    private final Label labelStatus;

    private Consumer<Parte> onParteSelecionadaChanged;

    /*
     * Guarda quais partes estão marcadas.
     */
    private final Map<Integer, Boolean> partesSelecionadas;


    public ProgramacaoView() {

        root = new BorderPane();

        root.setPadding(
                new Insets(10)
        );


        campoData =
                new DatePicker();


        listaSemanas =
                new ListView<>();


        semanas =
                FXCollections.observableArrayList();


        statusSemanas =
                new HashMap<>();


        partesSelecionadas =
                new HashMap<>();


        botaoAdicionarSemana =
                new Button(
                        "Adicionar semana"
                );


        botaoEditarSemana =
                new Button(
                        "Editar semana"
                );


        botaoGerar =
                new Button(
                        "Gerar"
                );


        botaoGerar.setDisable(true);


        botaoSalvar =
                new Button(
                        "Salvar Escalas"
                );


        botaoSalvar.setDisable(true);


        listaPartes =
                new ListView<>();


        campoTema =
                new TextField();


        campoTema.setPromptText(
                "Informe o tema da parte"
        );


        botaoSalvarTema =
                new Button(
                        "Salvar Tema"
                );


        labelStatus =
                new Label(
                        "Selecione um mês para configurar a programação."
                );


        configurarListaSemanas();

        configurarListaPartes();

        criarCabecalho();

        criarConteudo();

        criarRodape();
    }


    private void configurarListaSemanas() {

        listaSemanas.setItems(
                semanas
        );


        listaSemanas.setCellFactory(
                lista ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    LocalDate data,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        data,
                                        empty
                                );


                                if (
                                        empty
                                                || data == null
                                ) {

                                    setText(null);

                                    setStyle("");

                                    return;
                                }


                                boolean configurada =
                                        statusSemanas.getOrDefault(
                                                data,
                                                false
                                        );


                                String status =
                                        configurada
                                                ? "✓ Configurada"
                                                : "✗ Não configurada";


                                setText(
                                        "Reunião: "
                                                + formatarData(data)
                                                + "    "
                                                + status
                                );


                                if (configurada) {

                                    setStyle(
                                            "-fx-background-color: #d5f5d5;"
                                    );

                                } else {

                                    setStyle(
                                            "-fx-background-color: #ffd6d6;"
                                    );
                                }
                            }
                        }
        );
    }


    private void configurarListaPartes() {

        listaPartes.setCellFactory(
                lista ->
                        new ListCell<>() {

                            private final CheckBox checkBox =
                                    new CheckBox();


                            {
                                checkBox.setOnAction(
                                        event -> {

                                            Parte parte =
                                                    getItem();

                                            if (parte == null) {
                                                return;
                                            }

                                            partesSelecionadas.put(
                                                    parte.getId(),
                                                    checkBox.isSelected()
                                            );

                                            if (onParteSelecionadaChanged != null) {

                                                onParteSelecionadaChanged.accept(
                                                        parte
                                                );
                                            }
                                        }
                                );
                            }


                            @Override
                            protected void updateItem(
                                    Parte parte,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        parte,
                                        empty
                                );


                                if (
                                        empty
                                                || parte == null
                                ) {

                                    setGraphic(null);

                                    setText(null);

                                    return;
                                }


                                checkBox.setText(
                                        parte.getNome()
                                );


                                checkBox.setSelected(
                                        partesSelecionadas.getOrDefault(
                                                parte.getId(),
                                                false
                                        )
                                );


                                setGraphic(
                                        checkBox
                                );
                            }
                        }
        );
    }


    private void criarCabecalho() {

        Label titulo =
                new Label(
                        "Programação Mensal"
                );


        titulo.setStyle(
                "-fx-font-size: 20px; -fx-font-weight: bold;"
        );


        campoData.setPromptText(
                "Selecione o mês"
        );


        HBox linhaMes =
                new HBox(
                        10,
                        new Label("Mês:"),
                        campoData
                );


        linhaMes.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox topo =
                new VBox(
                        15,
                        titulo,
                        linhaMes
                );


        root.setTop(
                topo
        );
    }


    private void criarConteudo() {

        Label tituloSemanas =
                new Label(
                        "Reuniões do mês"
                );


        tituloSemanas.setStyle(
                "-fx-font-weight: bold;"
        );


        VBox painelSemanas =
                new VBox(
                        8,
                        tituloSemanas,
                        listaSemanas
                );


        VBox.setVgrow(
                listaSemanas,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                painelSemanas,
                Priority.ALWAYS
        );


        HBox botoesSemana =
                new HBox(
                        10,
                        botaoAdicionarSemana,
                        botaoEditarSemana
                );


        botoesSemana.setAlignment(
                Pos.CENTER
        );


        VBox painelMes =
                new VBox(
                        8,
                        painelSemanas,
                        botoesSemana
                );


        painelMes.setPrefWidth(350);


        Label tituloPartes =
                new Label(
                        "Partes da reunião"
                );


        tituloPartes.setStyle(
                "-fx-font-weight: bold;"
        );


        Label instrucao =
                new Label(
                        "Marque as partes variáveis desejadas:"
                );


        instrucao.setStyle(
                "-fx-text-fill: gray;"
        );


        VBox painelPartes =
                new VBox(
                        8,
                        tituloPartes,
                        instrucao,
                        listaPartes
                );


        VBox.setVgrow(
                listaPartes,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                painelPartes,
                Priority.ALWAYS
        );


        VBox painelTema =
                new VBox(
                        8,
                        new Label(
                                "Tema da parte selecionada:"
                        ),
                        campoTema,
                        botaoSalvarTema
                );


        painelTema.setPadding(
                new Insets(10, 0, 0, 0)
        );


        VBox painelEditor =
                new VBox(
                        15,
                        painelPartes,
                        painelTema
                );


        VBox.setVgrow(
                painelPartes,
                Priority.ALWAYS
        );


        VBox.setVgrow(
                painelEditor,
                Priority.ALWAYS
        );


        HBox centro =
                new HBox(
                        20,
                        painelMes,
                        painelEditor
                );


        HBox.setHgrow(
                painelEditor,
                Priority.ALWAYS
        );


        root.setCenter(
                centro
        );
    }


    private void criarRodape() {

        HBox botoes =
                new HBox(
                        10,
                        labelStatus,
                        botaoGerar,
                        botaoSalvar
                );


        HBox.setHgrow(
                labelStatus,
                Priority.ALWAYS
        );


        botoes.setAlignment(
                Pos.CENTER_LEFT
        );


        botoes.setPadding(
                new Insets(15, 0, 0, 0)
        );


        root.setBottom(
                botoes
        );
    }


    private String formatarData(
            LocalDate data
    ) {

        return data.format(
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                )
        );
    }


    public Parent getView() {

        return root;
    }


    public DatePicker getCampoData() {

        return campoData;
    }


    public ListView<LocalDate> getListaSemanas() {

        return listaSemanas;
    }


    public Button getBotaoAdicionarSemana() {

        return botaoAdicionarSemana;
    }


    public Button getBotaoEditarSemana() {

        return botaoEditarSemana;
    }


    public Button getBotaoGerar() {

        return botaoGerar;
    }


    public Button getBotaoSalvar() {

        return botaoSalvar;
    }


    public ListView<Parte> getListaPartes() {

        return listaPartes;
    }


    public ListView<Parte> getListaPartesDisponiveis() {

        return listaPartes;
    }


    public ListView<Parte> getListaPartesSelecionadas() {

        return listaPartes;
    }


    public TextField getCampoTema() {

        return campoTema;
    }


    public Button getBotaoSalvarTema() {

        return botaoSalvarTema;
    }


    public void atualizarStatus(
            String mensagem
    ) {

        labelStatus.setText(
                mensagem
        );
    }


    public void atualizarSemanas(
            List<LocalDate> novasSemanas,
            Map<LocalDate, Boolean> status
    ) {

        semanas.setAll(
                novasSemanas
        );


        statusSemanas.clear();

        statusSemanas.putAll(
                status
        );


        listaSemanas.refresh();
    }


    public void atualizarStatusSemana(
            LocalDate data,
            boolean configurada
    ) {

        statusSemanas.put(
                data,
                configurada
        );


        listaSemanas.refresh();
    }


    public void atualizarBotaoGerar(
            boolean habilitado
    ) {

        botaoGerar.setDisable(
                !habilitado
        );
    }


    public void atualizarBotaoSalvar(
            boolean habilitado
    ) {

        botaoSalvar.setDisable(
                !habilitado
        );
    }


    public void carregarPartes(
            List<Parte> partes
    ) {

        partesSelecionadas.clear();

        listaPartes.setItems(
                FXCollections.observableArrayList(
                        partes
                )
        );
    }


    public void marcarParte(
            Integer parteId,
            boolean marcada
    ) {

        if (parteId == null) {
            return;
        }

        partesSelecionadas.put(
                parteId,
                marcada
        );

        listaPartes.refresh();
    }


    public boolean isParteSelecionada(
            Integer parteId
    ) {

        if (parteId == null) {
            return false;
        }

        return partesSelecionadas.getOrDefault(
                parteId,
                false
        );
    }


    public void setOnParteSelecionadaChanged(
            Consumer<Parte> callback
    ) {

        this.onParteSelecionadaChanged =
                callback;
    }
}