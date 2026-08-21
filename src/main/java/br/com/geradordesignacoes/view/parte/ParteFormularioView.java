package br.com.geradordesignacoes.view.parte;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParteFormularioView {

    private final GridPane root;

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

        root = new GridPane();

        campoNome = new TextField();


        comboTipo = new ComboBox<>();
        comboTipo.getItems()
                .addAll(TipoParte.values());


        comboSecao = new ComboBox<>();
        comboSecao.getItems()
                .addAll(SecaoParte.values());


        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems()
                .addAll(Privilegio.values());


        comboSexo = new ComboBox<>();
        comboSexo.getItems()
                .addAll(SexoPermitido.values());


        comboNivelLeitura = new ComboBox<>();
        comboNivelLeitura.getItems()
                .addAll(NivelLeitura.values());

        comboNivelLeitura.setDisable(true);


        comboTipoVariacao = new ComboBox<>();
        comboTipoVariacao.getItems()
                .addAll(TipoVariacaoParte.values());


        comboTipo.setOnAction(event -> {

            boolean leitura =
                    comboTipo.getValue()
                            == TipoParte.LEITURA;

            comboNivelLeitura.setDisable(!leitura);

            if (!leitura) {

                comboNivelLeitura.setValue(
                        NivelLeitura.BASICO
                );
            }

            atualizarParticipacoesAutomaticas();
        });


        checkExigeAjudante =
                new CheckBox(
                        "Exige ajudante"
                );


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
                    new CheckBox(
                            tipo.toString()
                    );

            check.setUserData(tipo);

            checkParticipacoes.add(check);
        }
    }


    private void criarLayout() {

        root.setPadding(
                new Insets(20)
        );

        root.setHgap(15);

        root.setVgap(15);


        Label titulo =
                new Label(
                        "Cadastro de Parte"
                );

        titulo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold;"
        );


        root.add(
                titulo,
                0,
                0,
                2,
                1
        );


        campoNome.setPrefWidth(250);

        comboTipo.setPrefWidth(250);

        comboSecao.setPrefWidth(250);

        comboPrivilegio.setPrefWidth(250);

        comboSexo.setPrefWidth(250);

        comboNivelLeitura.setPrefWidth(250);

        comboTipoVariacao.setPrefWidth(250);


        root.add(
                new Label("Nome:"),
                0,
                1
        );

        root.add(
                campoNome,
                1,
                1
        );


        root.add(
                new Label("Tipo:"),
                0,
                2
        );

        root.add(
                comboTipo,
                1,
                2
        );


        root.add(
                new Label("Seção:"),
                0,
                3
        );

        root.add(
                comboSecao,
                1,
                3
        );


        root.add(
                new Label("Privilégio mínimo:"),
                0,
                4
        );

        root.add(
                comboPrivilegio,
                1,
                4
        );


        root.add(
                new Label("Sexo permitido:"),
                0,
                5
        );

        root.add(
                comboSexo,
                1,
                5
        );


        root.add(
                new Label("Nível leitura mínimo:"),
                0,
                6
        );

        root.add(
                comboNivelLeitura,
                1,
                6
        );


        root.add(
                new Label("Tipo de variação:"),
                0,
                7
        );

        root.add(
                comboTipoVariacao,
                1,
                7
        );


        root.add(
                checkExigeAjudante,
                1,
                8
        );


        VBox boxParticipacoes =
                new VBox(
                        8
                );


        Label tituloParticipacoes =
                new Label(
                        "Participações necessárias:"
                );


        boxParticipacoes.getChildren()
                .add(tituloParticipacoes);

        boxParticipacoes.getChildren()
                .addAll(
                        checkParticipacoes
                );


        root.add(
                boxParticipacoes,
                1,
                9
        );


        Button salvar =
                new Button(
                        "Salvar"
                );

        Button cancelar =
                new Button(
                        "Cancelar"
                );


        salvar.setOnAction(
                event -> salvar()
        );

        cancelar.setOnAction(
                event -> aoCancelar.run()
        );


        HBox botoes =
                new HBox(
                        10,
                        salvar,
                        cancelar
                );


        botoes.setAlignment(
                Pos.CENTER_RIGHT
        );


        root.add(
                botoes,
                1,
                10
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

        alert.setTitle(
                "Atenção"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                mensagem
        );

        alert.showAndWait();
    }


    public Parent getView() {

        return root;
    }


    private void atualizarParticipacoesAutomaticas() {

        TipoParte tipo = comboTipo.getValue();

        if (tipo == null) {
            return;
        }


        for (CheckBox check : checkParticipacoes) {

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


            case PRESIDENTE_REUNIAO -> selecionarParticipacao(
                    TipoParticipacao.PRESIDENTE
            );


            case ORACAO_INICIAL -> selecionarParticipacao(
                    TipoParticipacao.ORACAO_INICIAL
            );


            case DIRIGENTE_ESTUDO -> {

                selecionarParticipacao(
                        TipoParticipacao.DIRIGENTE
                );

                selecionarParticipacao(
                        TipoParticipacao.LEITOR
                );
            }
        }
    }


    private void selecionarParticipacao(
            TipoParticipacao participacao
    ) {

        for (CheckBox check : checkParticipacoes) {

            if (check.getUserData()
                    == participacao) {

                check.setSelected(true);

                return;
            }
        }
    }
}