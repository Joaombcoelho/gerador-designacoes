package br.com.geradordesignacoes.view.parte;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParteFormularioView {

    private final BorderPane root;

    private final ParteDAO parteDAO;
    private final Parte parteEdicao;
    private final Consumer<Parte> callback;
    private final Runnable aoCancelar;

    private final TextField campoNome;

    private final ComboBox<TipoParte> comboTipo;
    private final ComboBox<SecaoParte> comboSecao;
    private final ComboBox<Privilegio> comboPrivilegio;
    private final ComboBox<SexoPermitido> comboSexo;
    private final ComboBox<NivelLeitura> comboNivelLeitura;
    private final ComboBox<TipoVariacaoParte> comboTipoVariacao;

    private final CheckBox checkExigeAjudante;

    private final List<CheckBox> checkParticipacoes =
            new ArrayList<>();

    public ParteFormularioView(
            Parte parte,
            Consumer<Parte> callback,
            Runnable aoCancelar
    ) {

        this.parteEdicao = parte;
        this.callback = callback;
        this.aoCancelar = aoCancelar;

        this.parteDAO = new ParteDAO();

        root = new BorderPane();
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        campoNome = new TextField();
        campoNome.setPromptText("Digite o nome da parte");
        campoNome.getStyleClass().add("text-field");

        comboTipo = new ComboBox<>();
        comboTipo.getItems().addAll(TipoParte.values());
        comboTipo.setMaxWidth(Double.MAX_VALUE);
        comboTipo.getStyleClass().add("combo-box");

        comboSecao = new ComboBox<>();
        comboSecao.getItems().addAll(SecaoParte.values());
        comboSecao.setMaxWidth(Double.MAX_VALUE);
        comboSecao.getStyleClass().add("combo-box");

        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems().addAll(Privilegio.values());
        comboPrivilegio.setMaxWidth(Double.MAX_VALUE);
        comboPrivilegio.getStyleClass().add("combo-box");

        comboSexo = new ComboBox<>();
        comboSexo.getItems().addAll(SexoPermitido.values());
        comboSexo.setMaxWidth(Double.MAX_VALUE);
        comboSexo.getStyleClass().add("combo-box");

        comboNivelLeitura = new ComboBox<>();
        comboNivelLeitura.getItems().addAll(NivelLeitura.values());
        comboNivelLeitura.setMaxWidth(Double.MAX_VALUE);
        comboNivelLeitura.getStyleClass().add("combo-box");
        comboNivelLeitura.setDisable(true);

        comboTipoVariacao = new ComboBox<>();
        comboTipoVariacao.getItems().addAll(TipoVariacaoParte.values());
        comboTipoVariacao.setMaxWidth(Double.MAX_VALUE);
        comboTipoVariacao.getStyleClass().add("combo-box");

        comboTipo.setOnAction(event -> {

            boolean leitura =
                    comboTipo.getValue() == TipoParte.LEITURA;

            comboNivelLeitura.setDisable(!leitura);

            if (!leitura) {
                comboNivelLeitura.setValue(
                        NivelLeitura.BASICO
                );
            }

            atualizarParticipacoesAutomaticas();
        });

        checkExigeAjudante =
                new CheckBox("Exige ajudante");

        criarParticipacoes();
        criarLayout();

        if (parteEdicao != null) {
            preencherFormulario();
        }
    }

    private void criarParticipacoes() {

        for (TipoParticipacao tipo :
                TipoParticipacao.values()) {

            CheckBox check =
                    new CheckBox(tipo.toString());

            check.setUserData(tipo);

            checkParticipacoes.add(check);
        }
    }

    private void criarLayout() {

        Label titulo =
                new Label(
                        parteEdicao == null
                                ? "Nova Parte"
                                : "Editar Parte"
                );

        titulo.getStyleClass().add("page-title");

        Label subtitulo =
                new Label(
                        "Configure os dados, requisitos e participações desta parte."
                );

        subtitulo.getStyleClass().add("page-subtitle");

        VBox cabecalho =
                new VBox(
                        6,
                        titulo,
                        subtitulo
                );

        cabecalho.setPadding(
                new Insets(0, 0, 20, 0)
        );


        VBox dadosBasicos =
                criarCartao("Dados da parte");

        GridPane gradeDados =
                new GridPane();

        gradeDados.setHgap(18);
        gradeDados.setVgap(14);


        ColumnConstraints colunaRotulo =
                new ColumnConstraints();

        colunaRotulo.setMinWidth(150);


        ColumnConstraints colunaCampo =
                new ColumnConstraints();

        colunaCampo.setHgrow(
                Priority.ALWAYS
        );

        colunaCampo.setFillWidth(true);


        gradeDados.getColumnConstraints()
                .addAll(
                        colunaRotulo,
                        colunaCampo
                );


        adicionarCampo(
                gradeDados,
                "Nome",
                campoNome,
                0
        );

        adicionarCampo(
                gradeDados,
                "Tipo",
                comboTipo,
                1
        );

        adicionarCampo(
                gradeDados,
                "Seção",
                comboSecao,
                2
        );

        adicionarCampo(
                gradeDados,
                "Privilégio mínimo",
                comboPrivilegio,
                3
        );

        adicionarCampo(
                gradeDados,
                "Sexo permitido",
                comboSexo,
                4
        );

        adicionarCampo(
                gradeDados,
                "Nível de leitura mínimo",
                comboNivelLeitura,
                5
        );

        adicionarCampo(
                gradeDados,
                "Tipo de variação",
                comboTipoVariacao,
                6
        );


        dadosBasicos.getChildren()
                .add(gradeDados);


        VBox requisitos =
                criarCartao("Requisitos");

        Label descricaoRequisitos =
                new Label(
                        "Defina requisitos adicionais para a execução desta parte."
                );

        descricaoRequisitos.getStyleClass()
                .add("page-subtitle");

        descricaoRequisitos.setWrapText(true);


        requisitos.getChildren()
                .addAll(
                        descricaoRequisitos,
                        checkExigeAjudante
                );


        VBox participacoes =
                criarCartao(
                        "Participações necessárias"
                );

        Label descricaoParticipacoes =
                new Label(
                        "Selecione quais funções são necessárias para esta parte."
                );

        descricaoParticipacoes.getStyleClass()
                .add("page-subtitle");

        descricaoParticipacoes.setWrapText(true);


        GridPane gradeParticipacoes =
                new GridPane();

        gradeParticipacoes.setHgap(24);
        gradeParticipacoes.setVgap(14);


        for (int i = 0;
             i < checkParticipacoes.size();
             i++) {

            CheckBox check =
                    checkParticipacoes.get(i);

            int coluna = i % 2;
            int linha = i / 2;

            gradeParticipacoes.add(
                    check,
                    coluna,
                    linha
            );
        }


        participacoes.getChildren()
                .addAll(
                        descricaoParticipacoes,
                        gradeParticipacoes
                );


        VBox conteudo =
                new VBox(
                        18,
                        dadosBasicos,
                        requisitos,
                        participacoes
                );


        ScrollPane rolagem =
                new ScrollPane(conteudo);

        rolagem.setFitToWidth(true);

        rolagem.setHbarPolicy(
                ScrollPane.ScrollBarPolicy.NEVER
        );

        rolagem.getStyleClass()
                .add("scroll-pane");


        Button salvar =
                new Button(
                        parteEdicao == null
                                ? "Salvar parte"
                                : "Salvar alterações"
                );

        salvar.getStyleClass()
                .add("primary-button");

        salvar.setDefaultButton(true);

        salvar.setOnAction(
                event -> salvar()
        );


        Button cancelar =
                new Button("Cancelar");

        cancelar.getStyleClass()
                .add("secondary-button");

        cancelar.setCancelButton(true);

        cancelar.setOnAction(
                event -> aoCancelar.run()
        );


        HBox botoes =
                new HBox(
                        10,
                        cancelar,
                        salvar
                );

        botoes.setAlignment(
                Pos.CENTER_RIGHT
        );

        botoes.setPadding(
                new Insets(
                        18,
                        0,
                        0,
                        0
                )
        );


        root.setTop(cabecalho);
        root.setCenter(rolagem);
        root.setBottom(botoes);
    }

    private VBox criarCartao(String tituloTexto) {

        Label titulo =
                new Label(tituloTexto);

        titulo.getStyleClass()
                .add("card-title");


        VBox cartao =
                new VBox(16);

        cartao.getStyleClass()
                .add("card");

        cartao.setPadding(
                new Insets(20)
        );

        cartao.getChildren()
                .add(titulo);

        return cartao;
    }

    private void adicionarCampo(
            GridPane grade,
            String texto,
            Control campo,
            int linha
    ) {

        Label rotulo =
                new Label(texto);

        rotulo.setMinWidth(150);


        if (campo instanceof TextField textField) {

            textField.setMaxWidth(
                    Double.MAX_VALUE
            );
        }


        if (campo instanceof ComboBox<?> comboBox) {

            comboBox.setMaxWidth(
                    Double.MAX_VALUE
            );
        }


        grade.add(
                rotulo,
                0,
                linha
        );

        grade.add(
                campo,
                1,
                linha
        );


        GridPane.setHgrow(
                campo,
                Priority.ALWAYS
        );
    }

    private void preencherFormulario() {

        campoNome.setText(
                parteEdicao.getNome()
        );


        comboTipo.setValue(
                parteEdicao.getTipo()
        );


        comboSecao.setValue(
                parteEdicao.getSecao()
        );


        comboPrivilegio.setValue(
                parteEdicao.getPrivilegioMinimo()
        );


        comboSexo.setValue(
                parteEdicao.getSexoPermitido()
        );


        comboTipoVariacao.setValue(
                parteEdicao.getTipoVariacao()
        );


        if (parteEdicao.getTipo()
                == TipoParte.LEITURA) {

            comboNivelLeitura.setDisable(false);

            comboNivelLeitura.setValue(
                    parteEdicao.getNivelLeituraMinimo()
            );

        } else {

            comboNivelLeitura.setValue(
                    NivelLeitura.BASICO
            );

            comboNivelLeitura.setDisable(true);
        }


        checkExigeAjudante.setSelected(
                parteEdicao.getExigeAjudante()
        );


        for (CheckBox check :
                checkParticipacoes) {

            TipoParticipacao tipo =
                    (TipoParticipacao)
                            check.getUserData();

            check.setSelected(
                    parteEdicao
                            .getParticipacoesNecessarias()
                            .contains(tipo)
            );
        }
    }

    private void salvar() {

        if (campoNome.getText().isBlank()) {

            mostrarMensagem(
                    "Informe o nome da Parte."
            );

            return;
        }


        if (comboTipo.getValue() == null) {

            mostrarMensagem(
                    "Informe o tipo da Parte."
            );

            return;
        }


        if (comboSecao.getValue() == null) {

            mostrarMensagem(
                    "Informe a seção da Parte."
            );

            return;
        }


        if (comboPrivilegio.getValue() == null) {

            mostrarMensagem(
                    "Informe o privilégio mínimo da Parte."
            );

            return;
        }


        if (comboSexo.getValue() == null) {

            mostrarMensagem(
                    "Informe o sexo permitido da Parte."
            );

            return;
        }


        if (comboTipoVariacao.getValue() == null) {

            mostrarMensagem(
                    "Informe o tipo de variação da Parte."
            );

            return;
        }


        if (comboNivelLeitura.getValue() == null
                && comboTipo.getValue()
                == TipoParte.LEITURA) {

            mostrarMensagem(
                    "Informe o nível de leitura mínimo."
            );

            return;
        }


        List<TipoParticipacao> participacoes =
                new ArrayList<>();


        for (CheckBox check :
                checkParticipacoes) {

            if (check.isSelected()) {

                participacoes.add(
                        (TipoParticipacao)
                                check.getUserData()
                );
            }
        }


        NivelLeitura nivelLeitura =
                comboTipo.getValue()
                        == TipoParte.LEITURA
                        ?
                        comboNivelLeitura.getValue()
                        :
                        NivelLeitura.BASICO;


        Parte parte =
                new Parte(

                        parteEdicao == null
                                ? null
                                : parteEdicao.getId(),

                        campoNome.getText(),

                        comboTipo.getValue(),

                        comboPrivilegio.getValue(),

                        checkExigeAjudante.isSelected(),

                        comboSexo.getValue(),

                        1,

                        true,

                        nivelLeitura,

                        comboSecao.getValue(),

                        comboTipoVariacao.getValue(),

                        parteEdicao != null
                                && parteEdicao.possuiTema(),

                        participacoes
                );


        if (parteEdicao == null) {

            parteDAO.salvar(parte);

        } else {

            parteDAO.atualizar(parte);
        }


        callback.accept(parte);
    }

    private void mostrarMensagem(
            String mensagem
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.WARNING
                );

        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
    }

    public Parent getView() {

        return root;
    }

    private void atualizarParticipacoesAutomaticas() {

        TipoParte tipo =
                comboTipo.getValue();

        if (tipo == null) {
            return;
        }


        for (CheckBox check :
                checkParticipacoes) {

            check.setSelected(false);
        }


        switch (tipo) {

            case LEITURA -> selecionarParticipacao(
                    TipoParticipacao.LEITOR
            );


            case DISCURSO -> selecionarParticipacao(
                    TipoParticipacao.ORADOR
            );


            case DEMONSTRACAO -> {

                selecionarParticipacao(
                        TipoParticipacao.RESPONSAVEL
                );

                selecionarParticipacao(
                        TipoParticipacao.AJUDANTE
                );
            }


            case PRESIDENTE_REUNIAO ->
                    selecionarParticipacao(
                            TipoParticipacao.PRESIDENTE
                    );


            case ORACAO_INICIAL ->
                    selecionarParticipacao(
                            TipoParticipacao.ORACAO_INICIAL
                    );


            case DIRIGENTE_ESTUDO ->
                    selecionarParticipacao(
                            TipoParticipacao.DIRIGENTE
                    );
        }
    }

    private void selecionarParticipacao(
            TipoParticipacao participacao
    ) {

        for (CheckBox check :
                checkParticipacoes) {

            if (check.getUserData()
                    == participacao) {

                check.setSelected(true);

                return;
            }
        }
    }
}