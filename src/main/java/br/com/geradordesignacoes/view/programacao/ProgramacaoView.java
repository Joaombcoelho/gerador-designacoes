package br.com.geradordesignacoes.view.programacao;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

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

    private final Button botaoExportarS89;

    private final Button botaoSalvar;

    private final ListView<Parte> listaPartes;

    private final TextField campoTema;

    private final Button botaoSalvarTema;

    private final TextField campoNumeroOficial;

    private final Button botaoSalvarNumeroOficial;

    private final Label labelStatus;

    private Consumer<Parte> onParteSelecionadaChanged;

    private final Map<Integer, Boolean> partesSelecionadas;

    private final Map<Integer, Integer> numerosOficiais;


    public ProgramacaoView() {

        root = new BorderPane();
        root.getStyleClass().add("content-area");

        root.setPadding(new Insets(24));

        campoData = new DatePicker();

        listaSemanas = new ListView<>();

        semanas = FXCollections.observableArrayList();

        statusSemanas = new HashMap<>();

        partesSelecionadas = new HashMap<>();

        numerosOficiais = new HashMap<>();

        botaoAdicionarSemana = new Button("+ Adicionar semana");
        botaoAdicionarSemana.getStyleClass().add("primary-button");

        botaoEditarSemana = new Button("Editar semana");
        botaoEditarSemana.getStyleClass().add("secondary-button");

        botaoGerar = new Button("Gerar Escala");
        botaoGerar.getStyleClass().add("success-button");
        botaoGerar.setDisable(true);

        botaoExportarS89 = new Button("Exportar S-89");
        botaoExportarS89.getStyleClass().add("secondary-button");

        botaoSalvar = new Button("Salvar");
        botaoSalvar.getStyleClass().add("primary-button");
        botaoSalvar.setDisable(true);

        listaPartes = new ListView<>();
        listaPartes.getStyleClass().add("list-view");

        campoTema = new TextField();
        campoTema.setPromptText("Informe o tema da parte");
        campoTema.getStyleClass().add("text-field");

        botaoSalvarTema = new Button("Salvar Tema");
        botaoSalvarTema.getStyleClass().add("secondary-button");

        campoNumeroOficial = new TextField();
        campoNumeroOficial.setPromptText("Número positivo ou vazio");
        campoNumeroOficial.getStyleClass().add("text-field");
        campoNumeroOficial.setPrefWidth(180);

        botaoSalvarNumeroOficial = new Button("Salvar Número");
        botaoSalvarNumeroOficial.getStyleClass().add("secondary-button");

        labelStatus = new Label(
                "Selecione um mês para configurar a programação."
        );

        labelStatus.getStyleClass().add("label");

        configurarListaSemanas();
        configurarListaPartes();

        criarCabecalho();
        criarConteudo();
        criarRodape();
    }


    private void configurarListaSemanas() {

        listaSemanas.setItems(semanas);

        listaSemanas.setCellFactory(
                lista -> new ListCell<>() {

                    @Override
                    protected void updateItem(
                            LocalDate data,
                            boolean empty
                    ) {

                        super.updateItem(data, empty);

                        if (empty || data == null) {

                            setText(null);
                            setGraphic(null);
                            getStyleClass().removeAll(
                                    "programacao-configurada",
                                    "programacao-pendente"
                            );

                            return;
                        }

                        boolean configurada =
                                statusSemanas.getOrDefault(
                                        data,
                                        false
                                );

                        VBox conteudo = new VBox(4);

                        Label dataLabel =
                                new Label(
                                        "Reunião de "
                                                + formatarData(data)
                                );

                        dataLabel.setStyle(
                                "-fx-font-size: 14px; "
                                        + "-fx-font-weight: bold;"
                        );

                        Label statusLabel =
                                new Label(
                                        configurada
                                                ? "✓ Configurada"
                                                : "○ Não configurada"
                                );

                        if (configurada) {

                            statusLabel.getStyleClass()
                                    .add("status-success");

                        } else {

                            statusLabel.getStyleClass()
                                    .add("status-warning");
                        }

                        conteudo.getChildren().addAll(
                                dataLabel,
                                statusLabel
                        );

                        setGraphic(conteudo);
                        setText(null);

                        getStyleClass().removeAll(
                                "programacao-configurada",
                                "programacao-pendente"
                        );

                        getStyleClass().add(
                                configurada
                                        ? "programacao-configurada"
                                        : "programacao-pendente"
                        );
                    }
                }
        );
    }


    private void configurarListaPartes() {

        listaPartes.setCellFactory(
                lista -> new ListCell<>() {

                    private final CheckBox checkBox =
                            new CheckBox();

                    {
                        checkBox.setOnAction(event -> {

                            Parte parte = getItem();

                            if (parte == null) {
                                return;
                            }

                            if (
                                    parte.getTipoVariacao()
                                            == TipoVariacaoParte.FIXA
                            ) {

                                checkBox.setSelected(true);

                                return;
                            }

                            partesSelecionadas.put(
                                    parte.getId(),
                                    checkBox.isSelected()
                            );

                            if (
                                    onParteSelecionadaChanged
                                            != null
                            ) {

                                onParteSelecionadaChanged.accept(
                                        parte
                                );
                            }
                        });
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

                        if (empty || parte == null) {

                            setGraphic(null);
                            setText(null);

                            return;
                        }

                        boolean fixa =
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA;

                        Integer numeroOficial =
                                numerosOficiais.get(parte.getId());

                        checkBox.setText(
                                parte.getNome()
                                        + (numeroOficial == null
                                        ? ""
                                        : " - Nº oficial: "
                                        + numeroOficial)
                        );

                        checkBox.getStyleClass()
                                .add("check-box");

                        if (fixa) {

                            checkBox.setSelected(true);
                            checkBox.setDisable(true);

                            partesSelecionadas.put(
                                    parte.getId(),
                                    true
                            );

                        } else {

                            checkBox.setDisable(false);

                            checkBox.setSelected(
                                    partesSelecionadas.getOrDefault(
                                            parte.getId(),
                                            false
                                    )
                            );
                        }

                        setGraphic(checkBox);
                    }
                }
        );
    }


    private void criarCabecalho() {

        Label titulo =
                new Label("Programação Mensal");

        titulo.getStyleClass().add("page-title");

        Label subtitulo =
                new Label(
                        "Configure as reuniões, partes e temas do mês."
                );

        subtitulo.getStyleClass().add("page-subtitle");

        Label labelMes =
                new Label("Mês da programação");

        labelMes.getStyleClass().add("label");

        campoData.setPromptText("Selecione o mês");
        campoData.getStyleClass().add("date-picker");

        campoData.setPrefWidth(180);

        HBox seletorMes =
                new HBox(
                        10,
                        labelMes,
                        campoData
                );

        seletorMes.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox topo =
                new VBox(
                        6,
                        titulo,
                        subtitulo,
                        criarEspacamento(8),
                        seletorMes
                );

        root.setTop(topo);
    }


    private void criarConteudo() {

        VBox painelSemanas =
                criarPainelSemanas();

        VBox painelPartes =
                criarPainelPartes();

        HBox centro =
                new HBox(
                        16,
                        painelSemanas,
                        painelPartes
                );

        HBox.setHgrow(
                painelSemanas,
                Priority.SOMETIMES
        );

        HBox.setHgrow(
                painelPartes,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                painelSemanas,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                painelPartes,
                Priority.ALWAYS
        );

        BorderPane.setMargin(
                centro,
                new Insets(24, 0, 0, 0)
        );

        root.setCenter(centro);
    }


    private VBox criarPainelSemanas() {

        Label titulo =
                new Label("Reuniões do mês");

        titulo.getStyleClass().add("card-title");

        Label descricao =
                new Label(
                        "Selecione uma reunião para editar sua programação."
                );

        descricao.getStyleClass().add("page-subtitle");

        listaSemanas.getStyleClass().add("table-view");

        VBox.setVgrow(
                listaSemanas,
                Priority.ALWAYS
        );

        HBox botoes =
                new HBox(
                        10,
                        botaoAdicionarSemana,
                        botaoEditarSemana
                );

        botoes.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox card =
                new VBox(
                        10,
                        titulo,
                        descricao,
                        listaSemanas,
                        botoes
                );

        card.getStyleClass().add("card");

        card.setPrefWidth(360);

        card.setMinWidth(320);

        VBox.setVgrow(
                listaSemanas,
                Priority.ALWAYS
        );

        return card;
    }


    private VBox criarPainelPartes() {

        Label titulo =
                new Label("Partes da reunião");

        titulo.getStyleClass().add("card-title");

        Label instrucao =
                new Label(
                        "Partes fixas são incluídas automaticamente. "
                                + "Marque as partes variáveis desejadas."
                );

        instrucao.getStyleClass().add("page-subtitle");

        VBox.setVgrow(
                listaPartes,
                Priority.ALWAYS
        );

        VBox painelTema =
                criarPainelTema();

        VBox card =
                new VBox(
                        10,
                        titulo,
                        instrucao,
                        listaPartes,
                        painelTema
                );

        card.getStyleClass().add("card");

        VBox.setVgrow(
                listaPartes,
                Priority.ALWAYS
        );

        return card;
    }


    private VBox criarPainelTema() {

        Label titulo =
                new Label("Tema da parte selecionada");

        titulo.getStyleClass().add("card-title");

        HBox linha =
                new HBox(
                        10,
                        campoTema,
                        botaoSalvarTema
                );

        HBox.setHgrow(
                campoTema,
                Priority.ALWAYS
        );

        linha.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox painel =
                new VBox(
                        8,
                        titulo,
                        linha
                );

        Label tituloNumero =
                new Label("Número oficial da parte");

        tituloNumero.getStyleClass().add("card-title");

        HBox linhaNumero =
                new HBox(
                        10,
                        campoNumeroOficial,
                        botaoSalvarNumeroOficial
                );

        linhaNumero.setAlignment(
                Pos.CENTER_LEFT
        );

        painel.getChildren().addAll(
                criarEspacamento(4),
                tituloNumero,
                linhaNumero
        );

        painel.setPadding(
                new Insets(12, 0, 0, 0)
        );

        return painel;
    }


    private void criarRodape() {

        Separator separador =
                new Separator();

        HBox.setHgrow(
                labelStatus,
                Priority.ALWAYS
        );

        labelStatus.setMaxWidth(
                Double.MAX_VALUE
        );

        HBox botoes =
                new HBox(
                        10,
                        labelStatus,
                        botaoSalvar,
                        botaoExportarS89,
                        botaoGerar
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

        root.setBottom(rodape);
    }


    private Region criarEspacamento(double altura) {

        Region region = new Region();

        region.setMinHeight(altura);
        region.setPrefHeight(altura);
        region.setMaxHeight(altura);

        return region;
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

    public Button getBotaoExportarS89() {

        return botaoExportarS89;
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

    public TextField getCampoNumeroOficial() {

        return campoNumeroOficial;
    }

    public Button getBotaoSalvarNumeroOficial() {

        return botaoSalvarNumeroOficial;
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

        for (Parte parte : partes) {

            if (
                    parte.getTipoVariacao()
                            == TipoVariacaoParte.FIXA
            ) {

                partesSelecionadas.put(
                        parte.getId(),
                        true
                );
            }
        }

        listaPartes.setItems(
                FXCollections.observableArrayList(
                        partes
                )
        );

        listaPartes.refresh();
    }

    public void carregarNumerosOficiais(
            Map<Integer, Integer> numeros
    ) {

        numerosOficiais.clear();
        numerosOficiais.putAll(numeros);
        listaPartes.refresh();
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