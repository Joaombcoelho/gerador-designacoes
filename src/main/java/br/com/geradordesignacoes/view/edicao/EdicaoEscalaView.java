package br.com.geradordesignacoes.view.edicao;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class EdicaoEscalaView {

    private final BorderPane root;

    private final DatePicker campoMes;

    private final ListView<Escala> listaSemanas;

    private final ListView<Designacao> listaDesignacoes;

    private final Label labelStatus;

    private final Label labelSemanaSelecionada;

    private final ComboBox<Parte> comboParte;

    private final ComboBox<Pessoa> comboResponsavel;

    private final ComboBox<Pessoa> comboAjudante;

    private final Button botaoCarregar;

    private final Button botaoAdicionarParte;

    private final Button botaoSalvar;

    private final EdicaoEscalaController controller;

    private final PessoaDAO pessoaDAO;

    private final ParteDAO parteDAO;

    private final List<Pessoa> todasAsPessoas;

    private final List<Parte> todasAsPartes;

    private Designacao designacaoSelecionada;


    public EdicaoEscalaView() {

        root = new BorderPane();

        root.setPadding(
                new Insets(10)
        );


        pessoaDAO = new PessoaDAO();

        parteDAO = new ParteDAO();

        todasAsPessoas =
                carregarListaPessoas();

        todasAsPartes =
                carregarListaPartes();


        campoMes = new DatePicker();

        campoMes.setPromptText(
                "Selecione o mês"
        );


        listaSemanas = new ListView<>();

        listaSemanas
                .getSelectionModel()
                .setSelectionMode(
                        SelectionMode.SINGLE
                );


        listaDesignacoes = new ListView<>();

        listaDesignacoes
                .getSelectionModel()
                .setSelectionMode(
                        SelectionMode.SINGLE
                );


        labelStatus = new Label(
                "Selecione um mês para visualizar as semanas."
        );


        labelSemanaSelecionada = new Label(
                "Nenhuma semana selecionada."
        );

        labelSemanaSelecionada.setStyle(
                "-fx-font-weight: bold;"
        );


        comboParte =
                criarComboParte();

        comboParte.setPrefWidth(250);


        comboResponsavel =
                criarComboPessoa();

        comboResponsavel.setPrefWidth(250);


        comboAjudante =
                criarComboPessoa();

        comboAjudante.setPrefWidth(250);


        botaoCarregar = new Button(
                "Carregar Semanas"
        );


        botaoAdicionarParte =
                new Button(
                        "Adicionar Parte"
                );

        botaoAdicionarParte.setDisable(true);


        botaoSalvar = new Button(
                "Salvar Alterações"
        );

        botaoSalvar.setDisable(true);


        criarCabecalho();

        criarListaSemanas();

        criarListaDesignacoes();

        criarPainelEdicao();

        criarRodape();


        controller =
                new EdicaoEscalaController();


        configurarListaSemanas();

        configurarListaDesignacoes();


        registrarEventos();
    }


    private List<Pessoa> carregarListaPessoas() {

        try {

            return pessoaDAO.listarTodos()
                    .stream()
                    .sorted(
                            Comparator.comparing(
                                    Pessoa::getNome,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                    )
                    .toList();

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao carregar pessoas.",
                    e.getMessage()
            );

            return List.of();
        }
    }


    private List<Parte> carregarListaPartes() {

        try {

            return parteDAO.listarTodos()
                    .stream()
                    .sorted(
                            Comparator.comparing(
                                    Parte::getNome,
                                    String.CASE_INSENSITIVE_ORDER
                            )
                    )
                    .toList();

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao carregar partes.",
                    e.getMessage()
            );

            return List.of();
        }
    }


    private ComboBox<Parte> criarComboParte() {

        ComboBox<Parte> combo =
                new ComboBox<>();

        combo.setItems(
                FXCollections.observableArrayList(
                        todasAsPartes
                )
        );

        combo.setCellFactory(
                listView ->
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

                                if (empty
                                        || parte == null) {

                                    setText(null);

                                } else {

                                    setText(
                                            parte.getNome()
                                    );
                                }
                            }
                        }
        );

        combo.setButtonCell(
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

                        if (empty
                                || parte == null) {

                            setText(null);

                        } else {

                            setText(
                                    parte.getNome()
                            );
                        }
                    }
                }
        );

        combo.setPromptText(
                "Selecione a parte"
        );

        return combo;
    }


    private ComboBox<Pessoa> criarComboPessoa() {

        ComboBox<Pessoa> combo =
                new ComboBox<>();

        combo.setItems(
                FXCollections.observableArrayList(
                        todasAsPessoas
                )
        );


        combo.setCellFactory(
                listView ->
                        criarCelulaPessoa()
        );


        combo.setButtonCell(
                criarCelulaPessoa()
        );


        return combo;
    }


    private ListCell<Pessoa> criarCelulaPessoa() {

        return new ListCell<>() {

            @Override
            protected void updateItem(
                    Pessoa pessoa,
                    boolean empty
            ) {

                super.updateItem(
                        pessoa,
                        empty
                );


                if (empty
                        || pessoa == null) {

                    setText(null);

                } else {

                    setText(
                            pessoa.getNome()
                    );
                }
            }
        };
    }


    private void atualizarListaPartes() {

        List<Parte> partesAtualizadas =
                carregarListaPartes();

        comboParte.setItems(
                FXCollections.observableArrayList(
                        partesAtualizadas
                )
        );
    }


    private void criarCabecalho() {

        Label titulo = new Label(
                "Edição de Escalas"
        );

        titulo.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );


        Label instrucao = new Label(
                "Selecione o mês e depois a semana que deseja editar."
        );


        HBox linhaMes = new HBox(
                10,
                new Label("Mês:"),
                campoMes,
                botaoCarregar
        );

        linhaMes.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox topo = new VBox(
                10,
                titulo,
                instrucao,
                linhaMes
        );


        topo.setPadding(
                new Insets(
                        0,
                        0,
                        10,
                        0
                )
        );


        root.setTop(topo);
    }


    private void criarListaSemanas() {

        Label titulo = new Label(
                "Semanas disponíveis"
        );

        titulo.setStyle(
                "-fx-font-weight: bold;"
        );


        VBox painel = new VBox(
                8,
                titulo,
                listaSemanas
        );


        VBox.setVgrow(
                listaSemanas,
                Priority.ALWAYS
        );


        painel.setPrefWidth(260);


        root.setLeft(painel);
    }


    private void criarListaDesignacoes() {

        Label titulo = new Label(
                "Designações"
        );

        titulo.setStyle(
                "-fx-font-weight: bold;"
        );


        VBox painel = new VBox(
                8,
                labelSemanaSelecionada,
                titulo,
                listaDesignacoes
        );


        painel.setPadding(
                new Insets(
                        0,
                        10,
                        0,
                        15
                )
        );


        VBox.setVgrow(
                listaDesignacoes,
                Priority.ALWAYS
        );


        root.setCenter(painel);


        listaDesignacoes.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Designacao designacao,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        designacao,
                                        empty
                                );


                                if (empty
                                        || designacao == null) {

                                    setText(null);

                                } else {

                                    String parte =
                                            designacao
                                                    .parte()
                                                    .getNome();


                                    String responsavel =
                                            designacao
                                                    .responsavel()
                                                    .getNome();


                                    String ajudante =
                                            designacao
                                                    .ajudante()
                                                    == null
                                                    ? "-"
                                                    : designacao
                                                    .ajudante()
                                                    .getNome();


                                    setText(
                                            parte
                                                    + "\nResponsável: "
                                                    + responsavel
                                                    + "\nAjudante: "
                                                    + ajudante
                                    );


                                    setPadding(
                                            new Insets(8)
                                    );
                                }
                            }
                        }
        );
    }


    private void criarPainelEdicao() {

        Label titulo =
                new Label(
                        "Editar designação"
                );

        titulo.setStyle(
                "-fx-font-size: 15px;"
                        + "-fx-font-weight: bold;"
        );


        Label labelParte =
                new Label(
                        "Adicionar parte:"
                );


        Label labelResponsavel =
                new Label(
                        "Responsável:"
                );


        Label labelAjudante =
                new Label(
                        "Ajudante:"
                );


        VBox parte =
                new VBox(
                        5,
                        labelParte,
                        comboParte,
                        botaoAdicionarParte
                );


        VBox responsavel =
                new VBox(
                        5,
                        labelResponsavel,
                        comboResponsavel
                );


        VBox ajudante =
                new VBox(
                        5,
                        labelAjudante,
                        comboAjudante
                );


        VBox campos =
                new VBox(
                        15,
                        parte,
                        responsavel,
                        ajudante
                );


        VBox painel =
                new VBox(
                        10,
                        titulo,
                        campos,
                        botaoSalvar
                );


        painel.setPadding(
                new Insets(
                        10,
                        0,
                        10,
                        15
                )
        );


        root.setRight(painel);
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
                new Insets(
                        10,
                        0,
                        0,
                        0
                )
        );


        root.setBottom(rodape);
    }


    private void configurarListaSemanas() {

        listaSemanas.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    Escala escala,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        escala,
                                        empty
                                );


                                if (empty
                                        || escala == null) {

                                    setText(null);

                                } else {

                                    String data =
                                            escala
                                                    .getData()
                                                    .format(
                                                            DateTimeFormatter.ofPattern(
                                                                    "dd/MM/yyyy"
                                                            )
                                                    );


                                    setText(
                                            "Semana - "
                                                    + data
                                    );
                                }
                            }
                        }
        );
    }


    private void configurarListaDesignacoes() {

        listaDesignacoes
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         antiga,
                         selecionada) -> {

                            if (selecionada == null) {

                                return;
                            }


                            designacaoSelecionada =
                                    selecionada;


                            restaurarListaPessoas(
                                    comboResponsavel
                            );

                            restaurarListaPessoas(
                                    comboAjudante
                            );


                            comboResponsavel.setValue(
                                    selecionada.responsavel()
                            );


                            comboAjudante.setValue(
                                    selecionada.ajudante()
                            );


                            botaoSalvar.setDisable(
                                    false
                            );
                        }
                );
    }


    private void restaurarListaPessoas(
            ComboBox<Pessoa> combo
    ) {

        combo.setItems(
                FXCollections.observableArrayList(
                        todasAsPessoas
                )
        );
    }


    private void registrarEventos() {

        botaoCarregar.setOnAction(
                event -> carregarSemanas()
        );


        campoMes.setOnAction(
                event -> carregarSemanas()
        );


        listaSemanas
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         antigo,
                         selecionado) -> {

                            if (selecionado == null) {

                                return;
                            }


                            try {

                                Escala escala =
                                        controller
                                                .selecionarEscala(
                                                        selecionado.getId()
                                                );


                                labelSemanaSelecionada.setText(
                                        "Semana: "
                                                + escala
                                                .getData()
                                                .format(
                                                        DateTimeFormatter.ofPattern(
                                                                "dd/MM/yyyy"
                                                        )
                                                )
                                );


                                listaDesignacoes
                                        .getItems()
                                        .setAll(
                                                controller
                                                        .listarDesignacoesSelecionadas()
                                        );


                                designacaoSelecionada = null;


                                limparSelecaoPessoas();


                                comboParte.setValue(null);

                                // Recarrega as partes diretamente do banco.
                                atualizarListaPartes();


                                botaoSalvar.setDisable(
                                        true
                                );


                                botaoAdicionarParte.setDisable(
                                        false
                                );


                                labelStatus.setText(
                                        escala
                                                .getDesignacoes()
                                                .size()
                                                + " designação(ões) encontrada(s)."
                                );

                            } catch (Exception e) {

                                mostrarErro(
                                        "Erro ao selecionar a semana.",
                                        e.getMessage()
                                );
                            }
                        }
                );


        botaoSalvar.setOnAction(
                event -> salvarAlteracoes()
        );


        botaoAdicionarParte.setOnAction(
                event -> adicionarParte()
        );
    }


    private void adicionarParte() {

        if (controller == null) {

            return;
        }


        // Garante que o ComboBox esteja sempre atualizado
        // antes de selecionar uma nova parte.
        atualizarListaPartes();


        Parte parte =
                comboParte.getValue();


        Pessoa responsavel =
                comboResponsavel.getValue();


        Pessoa ajudante =
                comboAjudante.getValue();


        if (parte == null) {

            mostrarErro(
                    "Adicionar parte",
                    "Selecione uma parte."
            );

            return;
        }


        if (responsavel == null) {

            mostrarErro(
                    "Adicionar parte",
                    "Selecione um responsável."
            );

            return;
        }


        try {

            controller.adicionarParte(
                    parte,
                    responsavel,
                    ajudante
            );


            listaDesignacoes
                    .getItems()
                    .setAll(
                            controller
                                    .listarDesignacoesSelecionadas()
                    );


            comboParte.setValue(null);

            limparSelecaoPessoas();


            designacaoSelecionada = null;


            botaoSalvar.setDisable(
                    true
            );


            labelStatus.setText(
                    "Parte adicionada com sucesso."
            );

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao adicionar parte.",
                    e.getMessage()
            );
        }
    }


    private void limparSelecaoPessoas() {

        comboResponsavel.setValue(null);

        comboAjudante.setValue(null);

        comboResponsavel.getEditor().clear();

        comboAjudante.getEditor().clear();
    }


    private void carregarSemanas() {

        LocalDate data =
                campoMes.getValue();


        if (data == null) {

            labelStatus.setText(
                    "Selecione um mês."
            );


            listaSemanas
                    .getItems()
                    .clear();


            listaDesignacoes
                    .getItems()
                    .clear();


            botaoAdicionarParte.setDisable(
                    true
            );


            return;
        }


        YearMonth mes =
                YearMonth.from(data);


        try {

            List<Escala> escalas =
                    controller
                            .listarEscalasDoMes(mes);


            listaSemanas
                    .getItems()
                    .setAll(escalas);


            listaDesignacoes
                    .getItems()
                    .clear();


            designacaoSelecionada = null;


            limparSelecaoPessoas();


            comboParte.setValue(null);

            // Também atualiza ao carregar um novo mês.
            atualizarListaPartes();


            botaoSalvar.setDisable(
                    true
            );


            botaoAdicionarParte.setDisable(
                    true
            );


            if (escalas.isEmpty()) {

                labelStatus.setText(
                        "Nenhuma escala encontrada para "
                                + mes.format(
                                DateTimeFormatter.ofPattern(
                                        "MM/yyyy"
                                )
                        )
                                + "."
                );

            } else {

                labelStatus.setText(
                        escalas.size()
                                + " semana(s) encontrada(s)."
                );
            }

        } catch (Exception e) {

            listaSemanas
                    .getItems()
                    .clear();


            listaDesignacoes
                    .getItems()
                    .clear();


            botaoAdicionarParte.setDisable(
                    true
            );


            labelStatus.setText(
                    "Erro ao carregar as semanas."
            );


            mostrarErro(
                    "Erro ao carregar as semanas.",
                    e.getMessage()
            );
        }
    }


    private void salvarAlteracoes() {

        if (designacaoSelecionada == null) {

            return;
        }


        Pessoa novoResponsavel =
                comboResponsavel.getValue();


        Pessoa novoAjudante =
                comboAjudante.getValue();


        if (novoResponsavel == null) {

            mostrarErro(
                    "Responsável",
                    "Selecione um responsável da lista."
            );

            return;
        }


        try {

            List<LocalDate> conflitos =
                    controller.verificarConflitos(
                            designacaoSelecionada.id(),
                            novoResponsavel,
                            novoAjudante
                    );


            if (!conflitos.isEmpty()) {

                boolean confirmou =
                        confirmarConflitos(
                                novoResponsavel,
                                novoAjudante,
                                conflitos
                        );


                if (!confirmou) {

                    labelStatus.setText(
                            "Alteração cancelada."
                    );

                    return;
                }
            }


            controller.salvarAlteracoes(
                    designacaoSelecionada.id(),
                    novoResponsavel,
                    novoAjudante
            );


            atualizarListaDesignacoes();


            labelStatus.setText(
                    "Alterações salvas com sucesso."
            );

        } catch (Exception e) {

            mostrarErro(
                    "Erro ao salvar alterações.",
                    e.getMessage()
            );
        }
    }


    private boolean confirmarConflitos(
            Pessoa responsavel,
            Pessoa ajudante,
            List<LocalDate> conflitos
    ) {

        StringBuilder mensagem =
                new StringBuilder();


        mensagem.append(
                "Foi encontrado conflito de escala no mês.\n\n"
        );


        mensagem.append(
                "Participantes envolvidos:\n"
        );


        mensagem.append(
                "• Responsável: "
        );


        mensagem.append(
                responsavel.getNome()
        );


        mensagem.append("\n");


        if (ajudante != null) {

            mensagem.append(
                    "• Ajudante: "
            );


            mensagem.append(
                    ajudante.getNome()
            );


            mensagem.append("\n");
        }


        mensagem.append(
                "\nDatas com conflito:\n"
        );


        for (LocalDate data : conflitos) {

            mensagem.append("• ");


            mensagem.append(
                    data.format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy"
                            )
                    )
            );


            mensagem.append("\n");
        }


        mensagem.append(
                "\nDeseja salvar mesmo assim?"
        );


        Alert alerta =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alerta.setTitle(
                "Conflito de escala"
        );


        alerta.setHeaderText(
                "Conflito encontrado"
        );


        alerta.setContentText(
                mensagem.toString()
        );


        return alerta
                .showAndWait()
                .filter(
                        resposta ->
                                resposta == ButtonType.OK
                )
                .isPresent();
    }


    private void atualizarListaDesignacoes() {

        List<Designacao> designacoes =
                controller
                        .listarDesignacoesSelecionadas();


        listaDesignacoes
                .getItems()
                .setAll(designacoes);


        designacaoSelecionada = null;


        limparSelecaoPessoas();


        botaoSalvar.setDisable(
                true
        );
    }


    public Parent getView() {

        return root;
    }


    public DatePicker getCampoMes() {

        return campoMes;
    }


    public ListView<Escala> getListaSemanas() {

        return listaSemanas;
    }


    public ListView<Designacao> getListaDesignacoes() {

        return listaDesignacoes;
    }


    public Label getLabelStatus() {

        return labelStatus;
    }


    private void mostrarErro(
            String titulo,
            String mensagem
    ) {

        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );


        alerta.setTitle(titulo);

        alerta.setHeaderText(null);

        alerta.setContentText(
                mensagem == null
                        ? "Erro desconhecido."
                        : mensagem
        );


        alerta.showAndWait();
    }
}