package br.com.geradordesignacoes.view.edicao;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;

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

        root.getStyleClass().add("content-area");

        root.setPadding(
                new Insets(24)
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

        campoMes.getStyleClass().add(
                "date-picker"
        );

        campoMes.setPrefWidth(180);


        listaSemanas = new ListView<>();

        listaSemanas.getStyleClass().add(
                "list-view"
        );

        listaSemanas
                .getSelectionModel()
                .setSelectionMode(
                        SelectionMode.SINGLE
                );


        listaDesignacoes = new ListView<>();

        listaDesignacoes.getStyleClass().add(
                "list-view"
        );

        listaDesignacoes
                .getSelectionModel()
                .setSelectionMode(
                        SelectionMode.SINGLE
                );


        labelStatus = new Label(
                "Selecione um mês para visualizar as semanas."
        );

        labelStatus.getStyleClass().add(
                "status-warning"
        );


        labelSemanaSelecionada = new Label(
                "Nenhuma semana selecionada."
        );

        labelSemanaSelecionada.getStyleClass().add(
                "card-title"
        );


        comboParte =
                criarComboParte();

        comboParte.getStyleClass().add(
                "combo-box"
        );

        comboParte.setPrefWidth(
                Double.MAX_VALUE
        );


        comboResponsavel =
                criarComboPessoa();

        comboResponsavel.getStyleClass().add(
                "combo-box"
        );

        comboResponsavel.setPrefWidth(
                Double.MAX_VALUE
        );


        comboAjudante =
                criarComboPessoa();

        comboAjudante.getStyleClass().add(
                "combo-box"
        );

        comboAjudante.setPrefWidth(
                Double.MAX_VALUE
        );


        botaoCarregar = new Button(
                "Carregar Semanas"
        );

        botaoCarregar.getStyleClass().add(
                "primary-button"
        );


        botaoAdicionarParte =
                new Button(
                        "+ Adicionar Parte"
                );

        botaoAdicionarParte.getStyleClass().add(
                "secondary-button"
        );

        botaoAdicionarParte.setDisable(
                true
        );


        botaoSalvar = new Button(
                "Salvar Alterações"
        );

        botaoSalvar.getStyleClass().add(
                "success-button"
        );

        botaoSalvar.setDisable(
                true
        );


        criarCabecalho();

        criarConteudo();

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

        combo.setPromptText(
                "Selecione uma pessoa"
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

                if (
                        empty
                                || pessoa == null
                ) {

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

        Label titulo =
                new Label(
                        "Edição de Escalas"
                );

        titulo.getStyleClass().add(
                "page-title"
        );


        Label instrucao =
                new Label(
                        "Selecione o mês e a semana para editar as designações."
                );

        instrucao.getStyleClass().add(
                "page-subtitle"
        );


        Label labelMes =
                new Label(
                        "Mês da escala"
                );

        labelMes.getStyleClass().add(
                "label"
        );


        HBox linhaMes =
                new HBox(
                        10,
                        labelMes,
                        campoMes,
                        botaoCarregar
                );

        linhaMes.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox topo =
                new VBox(
                        6,
                        titulo,
                        instrucao,
                        criarEspaco(8),
                        linhaMes
                );


        root.setTop(
                topo
        );
    }


    private void criarConteudo() {

        VBox painelSemanas =
                criarPainelSemanas();


        VBox painelDesignacoes =
                criarPainelDesignacoes();


        VBox painelEdicao =
                criarPainelEdicao();


        HBox centro =
                new HBox(
                        16,
                        painelSemanas,
                        painelDesignacoes,
                        painelEdicao
                );


        HBox.setHgrow(
                painelSemanas,
                Priority.SOMETIMES
        );

        HBox.setHgrow(
                painelDesignacoes,
                Priority.ALWAYS
        );

        HBox.setHgrow(
                painelEdicao,
                Priority.SOMETIMES
        );


        root.setCenter(
                centro
        );

        BorderPane.setMargin(
                centro,
                new Insets(
                        24,
                        0,
                        0,
                        0
                )
        );
    }


    private VBox criarPainelSemanas() {

        Label titulo =
                new Label(
                        "Semanas disponíveis"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        Label descricao =
                new Label(
                        "Escolha uma semana para visualizar suas designações."
                );

        descricao.getStyleClass().add(
                "page-subtitle"
        );


        VBox.setVgrow(
                listaSemanas,
                Priority.ALWAYS
        );


        VBox painel =
                new VBox(
                        10,
                        titulo,
                        descricao,
                        listaSemanas
                );

        painel.getStyleClass().add(
                "card"
        );


        painel.setPrefWidth(
                260
        );

        painel.setMinWidth(
                230
        );


        return painel;
    }


    private VBox criarPainelDesignacoes() {

        Label titulo =
                new Label(
                        "Designações da semana"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        VBox.setVgrow(
                listaDesignacoes,
                Priority.ALWAYS
        );


        VBox painel =
                new VBox(
                        10,
                        labelSemanaSelecionada,
                        titulo,
                        listaDesignacoes
                );

        painel.getStyleClass().add(
                "card"
        );


        configurarCelulasDesignacoes();


        return painel;
    }


    private void configurarCelulasDesignacoes() {

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


                                if (
                                        empty
                                                || designacao == null
                                ) {

                                    setGraphic(null);
                                    setText(null);

                                    return;
                                }


                                String parte =
                                        designacao
                                                .parte()
                                                .getNome();


                                String responsavel =
                                        designacao.responsavel() == null
                                                ? "-"
                                                : designacao
                                                .responsavel()
                                                .getNome();


                                String ajudante =
                                        designacao.ajudante() == null
                                                ? "-"
                                                : designacao
                                                .ajudante()
                                                .getNome();


                                Label labelParte =
                                        new Label(
                                                parte
                                        );

                                labelParte.getStyleClass().add(
                                        "card-title"
                                );


                                Label labelResponsavel =
                                        new Label(
                                                "Responsável: "
                                                        + responsavel
                                        );


                                Label labelAjudante =
                                        new Label(
                                                "Ajudante: "
                                                        + ajudante
                                        );


                                VBox conteudo =
                                        new VBox(
                                                5,
                                                labelParte,
                                                labelResponsavel,
                                                labelAjudante
                                        );


                                conteudo.setPadding(
                                        new Insets(
                                                10
                                        )
                                );


                                setGraphic(
                                        conteudo
                                );

                                setText(null);

                                setPadding(
                                        new Insets(
                                                3
                                        )
                                );
                            }
                        }
        );
    }


    private VBox criarPainelEdicao() {

        Label titulo =
                new Label(
                        "Editar designação"
                );

        titulo.getStyleClass().add(
                "card-title"
        );


        Label descricao =
                new Label(
                        "Selecione uma designação ou adicione uma nova parte."
                );

        descricao.getStyleClass().add(
                "page-subtitle"
        );


        Label labelParte =
                new Label(
                        "Adicionar parte"
                );

        labelParte.getStyleClass().add(
                "label"
        );


        Label labelResponsavel =
                new Label(
                        "Responsável"
                );

        labelResponsavel.getStyleClass().add(
                "label"
        );


        Label labelAjudante =
                new Label(
                        "Ajudante"
                );

        labelAjudante.getStyleClass().add(
                "label"
        );


        VBox campoParte =
                new VBox(
                        6,
                        labelParte,
                        comboParte,
                        botaoAdicionarParte
                );


        VBox campoResponsavel =
                new VBox(
                        6,
                        labelResponsavel,
                        comboResponsavel
                );


        VBox campoAjudante =
                new VBox(
                        6,
                        labelAjudante,
                        comboAjudante
                );


        VBox campos =
                new VBox(
                        16,
                        campoParte,
                        campoResponsavel,
                        campoAjudante
                );


        VBox painel =
                new VBox(
                        10,
                        titulo,
                        descricao,
                        criarEspaco(6),
                        campos,
                        criarEspaco(10),
                        botaoSalvar
                );


        painel.getStyleClass().add(
                "card"
        );


        painel.setPrefWidth(
                310
        );

        painel.setMinWidth(
                280
        );


        botaoSalvar.setMaxWidth(
                Double.MAX_VALUE
        );

        botaoAdicionarParte.setMaxWidth(
                Double.MAX_VALUE
        );


        return painel;
    }


    private void criarRodape() {

        Separator separador =
                new Separator();


        HBox conteudo =
                new HBox(
                        labelStatus
                );


        conteudo.setAlignment(
                Pos.CENTER_LEFT
        );


        VBox rodape =
                new VBox(
                        10,
                        separador,
                        conteudo
                );


        BorderPane.setMargin(
                rodape,
                new Insets(
                        20,
                        0,
                        0,
                        0
                )
        );


        root.setBottom(
                rodape
        );
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


                                if (
                                        empty
                                                || escala == null
                                ) {

                                    setGraphic(null);
                                    setText(null);

                                    return;
                                }


                                String data =
                                        escala
                                                .getData()
                                                .format(
                                                        DateTimeFormatter.ofPattern(
                                                                "dd/MM/yyyy"
                                                        )
                                                );


                                Label titulo =
                                        new Label(
                                                "Semana"
                                        );

                                titulo.getStyleClass().add(
                                        "page-subtitle"
                                );


                                Label dataLabel =
                                        new Label(
                                                data
                                        );

                                dataLabel.getStyleClass().add(
                                        "card-title"
                                );


                                VBox conteudo =
                                        new VBox(
                                                3,
                                                titulo,
                                                dataLabel
                                        );


                                conteudo.setPadding(
                                        new Insets(
                                                8
                                        )
                                );


                                setGraphic(
                                        conteudo
                                );

                                setText(null);
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

                            if (
                                    selecionada == null
                            ) {

                                return;
                            }


                            designacaoSelecionada =
                                    selecionada;


                            atualizarListasDePessoas(
                                    selecionada.parte(),
                                    selecionada.responsavel(),
                                    selecionada.ajudante()
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


    private void registrarEventos() {

        botaoCarregar.setOnAction(
                event -> carregarSemanas()
        );


        campoMes.setOnAction(
                event -> carregarSemanas()
        );


        comboParte.getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         antiga,
                         selecionada) -> {

                            if (
                                    selecionada == null
                            ) {

                                limparSelecaoPessoas();

                                return;
                            }


                            atualizarListasDePessoas(
                                    selecionada,
                                    null,
                                    null
                            );
                        }
                );


        listaSemanas
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable,
                         antigo,
                         selecionado) -> {

                            if (
                                    selecionado == null
                            ) {

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


                                designacaoSelecionada =
                                        null;


                                limparSelecaoPessoas();


                                comboParte.setValue(
                                        null
                                );


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


    private void atualizarListasDePessoas(
            Parte parte,
            Pessoa responsavelAtual,
            Pessoa ajudanteAtual
    ) {

        TipoParticipacao tipoResponsavel =
                tipoParticipacaoDoResponsavel(
                        parte
                );


        comboResponsavel.setItems(
                FXCollections.observableArrayList(
                        controller.listarPessoasParaSelecao(
                                parte,
                                tipoResponsavel,
                                responsavelAtual
                        )
                )
        );


        comboAjudante.setItems(
                FXCollections.observableArrayList(
                        controller.listarPessoasParaSelecao(
                                parte,
                                TipoParticipacao.AJUDANTE,
                                ajudanteAtual
                        )
                )
        );
    }


    private TipoParticipacao tipoParticipacaoDoResponsavel(
            Parte parte
    ) {

        if (
                parte.getTipo()
                        == TipoParte.DIRIGENTE_ESTUDO
        ) {

            return TipoParticipacao.DIRIGENTE;
        }


        return parte.getParticipacoesNecessarias()
                .stream()
                .filter(
                        tipo ->
                                tipo != TipoParticipacao.AJUDANTE
                )
                .findFirst()
                .orElse(
                        TipoParticipacao.RESPONSAVEL
                );
    }


    private void adicionarParte() {

        if (controller == null) {

            return;
        }


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


            comboParte.setValue(
                    null
            );


            limparSelecaoPessoas();


            designacaoSelecionada =
                    null;


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

        comboResponsavel.setValue(
                null
        );

        comboAjudante.setValue(
                null
        );

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
                YearMonth.from(
                        data
                );


        try {

            List<Escala> escalas =
                    controller
                            .listarEscalasDoMes(
                                    mes
                            );


            listaSemanas
                    .getItems()
                    .setAll(
                            escalas
                    );


            listaDesignacoes
                    .getItems()
                    .clear();


            designacaoSelecionada =
                    null;


            limparSelecaoPessoas();


            comboParte.setValue(
                    null
            );


            atualizarListaPartes();


            botaoSalvar.setDisable(
                    true
            );


            botaoAdicionarParte.setDisable(
                    true
            );


            if (
                    escalas.isEmpty()
            ) {

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

        if (
                designacaoSelecionada == null
        ) {

            return;
        }


        Pessoa novoResponsavel =
                comboResponsavel.getValue();


        Pessoa novoAjudante =
                comboAjudante.getValue();


        if (
                novoResponsavel == null
        ) {

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


            if (
                    !conflitos.isEmpty()
            ) {

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


        mensagem.append(
                "\n"
        );


        if (
                ajudante != null
        ) {

            mensagem.append(
                    "• Ajudante: "
            );


            mensagem.append(
                    ajudante.getNome()
            );


            mensagem.append(
                    "\n"
            );
        }


        mensagem.append(
                "\nDatas com conflito:\n"
        );


        for (
                LocalDate data :
                conflitos
        ) {

            mensagem.append(
                    "• "
            );


            mensagem.append(
                    data.format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy"
                            )
                    )
            );


            mensagem.append(
                    "\n"
            );
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
                .setAll(
                        designacoes
                );


        designacaoSelecionada =
                null;


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


        alerta.setTitle(
                titulo
        );


        alerta.setHeaderText(
                null
        );


        alerta.setContentText(
                mensagem == null
                        ? "Erro desconhecido."
                        : mensagem
        );


        alerta.showAndWait();
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