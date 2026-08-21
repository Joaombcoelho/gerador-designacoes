package br.com.geradordesignacoes.view.programacao;

import br.com.geradordesignacoes.model.Parte;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProgramacaoView {

    private final BorderPane root;

    private final DatePicker campoData;

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


        listaPartesDisponiveis =
                new ListView<>();


        listaPartesSelecionadas =
                new ListView<>();


        botaoAdicionar =
                new Button("Adicionar");


        botaoRemover =
                new Button("Remover");


        campoTema =
                new TextField();


        campoTema.setPromptText(
                "Informe o tema da parte"
        );


        botaoSalvarTema =
                new Button("Salvar Tema");


        labelStatus =
                new Label(
                        "Selecione uma data para configurar a programação."
                );


        configurarListas();

        criarCabecalho();

        criarConteudo();

        criarRodape();
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
                        "Programação Semanal"
                );


        titulo.setStyle(
                "-fx-font-size: 20px;"
        );


        HBox linhaData =
                new HBox(
                        10,
                        new Label("Data da reunião:"),
                        campoData
                );


        linhaData.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox topo =
                new VBox(
                        15,
                        titulo,
                        linhaData
                );


        root.setTop(
                topo
        );
    }


    private void criarConteudo() {

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
                        new Label("Tema da parte selecionada:"),
                        campoTema,
                        botaoSalvarTema
                );


        painelTema.setPadding(
                new Insets(10, 0, 0, 0)
        );


        HBox conteudo =
                new HBox(
                        15,
                        painelDisponiveis,
                        botoes,
                        painelSelecionadas
                );


        conteudo.setAlignment(
                Pos.CENTER
        );


        VBox centro =
                new VBox(
                        15,
                        conteudo,
                        painelTema
                );


        VBox.setVgrow(
                conteudo,
                Priority.ALWAYS
        );


        root.setCenter(
                centro
        );
    }


    private void criarRodape() {

        HBox rodape =
                new HBox(
                        labelStatus
                );


        rodape.setAlignment(
                Pos.CENTER_LEFT
        );


        rodape.setPadding(
                new Insets(15, 0, 0, 0)
        );


        root.setBottom(
                rodape
        );
    }


    public Parent getView() {

        return root;
    }


    public DatePicker getCampoData() {

        return campoData;
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
}