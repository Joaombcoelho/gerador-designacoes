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

public class ProgramacaoView {

    private final BorderPane root;

    /*
     * Agora representa o mês selecionado.
     * Mantemos DatePicker para evitar alterar
     * desnecessariamente a estrutura atual.
     */
    private final DatePicker campoData;

    private final ListView<LocalDate> listaSemanas;

    private final ObservableList<LocalDate> semanas;

    private final Map<LocalDate, Boolean> statusSemanas;

    private final Button botaoAdicionarSemana;

    private final Button botaoEditarSemana;

    private final Button botaoGerar;

    private final ListView<Parte> listaPartesDisponiveis;

    private final ListView<Parte> listaPartesSelecionadas;

    private final Button botaoAdicionar;

    private final Button botaoRemover;

    private final TextField campoTema;

    private final Button botaoSalvarTema;

    private final Label labelStatus;


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


        listaPartesDisponiveis =
                new ListView<>();


        listaPartesSelecionadas =
                new ListView<>();


        botaoAdicionar =
                new Button(
                        "Adicionar"
                );


        botaoRemover =
                new Button(
                        "Remover"
                );


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

        configurarListas();

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


    private void configurarListas() {

        listaPartesDisponiveis.setCellFactory(
                lista ->
                        new ListCell<>() {

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

                                    setText(null);

                                } else {

                                    setText(
                                            parte.getNome()
                                    );
                                }
                            }
                        }
        );


        listaPartesSelecionadas.setCellFactory(
                lista ->
                        new ListCell<>() {

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

                                    setText(null);

                                } else {

                                    setText(
                                            parte.getNome()
                                    );
                                }
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


        Label tituloDisponiveis =
                new Label(
                        "Partes disponíveis"
                );


        tituloDisponiveis.setStyle(
                "-fx-font-weight: bold;"
        );


        Label tituloSelecionadas =
                new Label(
                        "Partes selecionadas"
                );


        tituloSelecionadas.setStyle(
                "-fx-font-weight: bold;"
        );


        VBox painelDisponiveis =
                new VBox(
                        8,
                        tituloDisponiveis,
                        listaPartesDisponiveis
                );


        VBox painelSelecionadas =
                new VBox(
                        8,
                        tituloSelecionadas,
                        listaPartesSelecionadas
                );


        HBox.setHgrow(
                painelDisponiveis,
                Priority.ALWAYS
        );


        HBox.setHgrow(
                painelSelecionadas,
                Priority.ALWAYS
        );


        HBox botoes =
                new HBox(
                        10,
                        botaoAdicionar,
                        botaoRemover
                );


        botoes.setAlignment(
                Pos.CENTER
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


        HBox editorPartes =
                new HBox(
                        15,
                        painelDisponiveis,
                        botoes,
                        painelSelecionadas
                );


        editorPartes.setAlignment(
                Pos.CENTER
        );


        VBox painelEditor =
                new VBox(
                        15,
                        editorPartes,
                        painelTema
                );


        VBox.setVgrow(
                editorPartes,
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
                        botaoGerar
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


    public ListView<Parte> getListaPartesDisponiveis() {

        return listaPartesDisponiveis;
    }


    public ListView<Parte> getListaPartesSelecionadas() {

        return listaPartesSelecionadas;
    }


    public Button getBotaoAdicionar() {

        return botaoAdicionar;
    }


    public Button getBotaoRemover() {

        return botaoRemover;
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
}