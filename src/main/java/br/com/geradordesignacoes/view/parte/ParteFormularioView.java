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

    private final ComboBox<Privilegio> comboPrivilegio;

    private final ComboBox<SexoPermitido> comboSexo;


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
                .addAll(
                        TipoParte.values()
                );


        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems()
                .addAll(
                        Privilegio.values()
                );


        comboSexo = new ComboBox<>();
        comboSexo.getItems()
                .addAll(
                        SexoPermitido.values()
                );


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


            checkParticipacoes.add(
                    check
            );

        }

    }




    private void criarLayout() {


        root.setPadding(
                new Insets(20)
        );


        root.setHgap(
                15
        );


        root.setVgap(
                15
        );



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

        comboPrivilegio.setPrefWidth(250);

        comboSexo.setPrefWidth(250);



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
                new Label("Privilégio mínimo:"),
                0,
                3
        );


        root.add(
                comboPrivilegio,
                1,
                3
        );



        root.add(
                new Label("Sexo permitido:"),
                0,
                4
        );


        root.add(
                comboSexo,
                1,
                4
        );



        root.add(
                checkExigeAjudante,
                1,
                5
        );



        VBox boxParticipacoes =
                new VBox(
                        8
                );


        Label tituloParticipacoes =
                new Label(
                        "Participações necessárias:"
                );


        boxParticipacoes
                .getChildren()
                .add(
                        tituloParticipacoes
                );


        boxParticipacoes
                .getChildren()
                .addAll(
                        checkParticipacoes
                );



        root.add(
                boxParticipacoes,
                1,
                6
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
                7
        );

    }




    private void preencherFormulario() {


        campoNome.setText(
                parteEdicao.getNome()
        );


        comboTipo.setValue(
                parteEdicao.getTipo()
        );


        comboPrivilegio.setValue(
                parteEdicao.getPrivilegioMinimo()
        );


        comboSexo.setValue(
                parteEdicao.getSexoPermitido()
        );


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


        if (campoNome.getText()
                .isBlank()) {


            mostrarMensagem(
                    "Informe o nome da Parte."
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

                        participacoes

                );



        if (parteEdicao == null) {


            parteDAO.salvar(
                    parte
            );


        } else {


            parteDAO.atualizar(
                    parte
            );

        }



        callback.accept(
                parte
        );

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

}