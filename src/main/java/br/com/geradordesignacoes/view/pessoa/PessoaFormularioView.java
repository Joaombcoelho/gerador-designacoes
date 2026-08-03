package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class PessoaFormularioView {

    private final GridPane root;

    private final TextField campoNome;

    private final ComboBox<Sexo> comboSexo;

    private final ComboBox<Privilegio> comboPrivilegio;

    private final ComboBox<NivelLeitura> comboNivelLeitura;


    private final CheckBox checkResponsavel;
    private final CheckBox checkAjudante;
    private final CheckBox checkLeitura;
    private final CheckBox checkDiscurso;
    private final CheckBox checkOracao;
    private final CheckBox checkPresidente;
    private final CheckBox checkDirigente;
    private final CheckBox checkAtivo;


    private final Consumer<Pessoa> aoSalvar;
    private final Consumer<Pessoa> aoAtualizar;
    private final Runnable aoCancelar;

    private final Pessoa pessoaEdicao;


    public PessoaFormularioView(
            Pessoa pessoaEdicao,
            Consumer<Pessoa> aoSalvar,
            Consumer<Pessoa> aoAtualizar,
            Runnable aoCancelar
    ) {

        this.pessoaEdicao = pessoaEdicao;
        this.aoSalvar = aoSalvar;
        this.aoAtualizar = aoAtualizar;
        this.aoCancelar = aoCancelar;


        root = new GridPane();

        campoNome = new TextField();


        comboSexo = new ComboBox<>();
        comboSexo.getItems().addAll(Sexo.values());


        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems().addAll(Privilegio.values());


        comboNivelLeitura = new ComboBox<>();
        comboNivelLeitura.getItems().addAll(NivelLeitura.values());
        comboNivelLeitura.setValue(NivelLeitura.BASICO);



        checkResponsavel = new CheckBox("Pode ser responsável");
        checkAjudante = new CheckBox("Pode ser ajudante");
        checkLeitura = new CheckBox("Pode fazer leitura");
        checkDiscurso = new CheckBox("Pode fazer discurso");
        checkOracao = new CheckBox("Pode fazer oração");

        checkPresidente = new CheckBox("Pode ser presidente");
        checkDirigente = new CheckBox("Pode ser dirigente");


        checkPresidente.setDisable(true);
        checkDirigente.setDisable(true);


        checkAtivo = new CheckBox("Ativo");
        checkAtivo.setSelected(true);



        comboPrivilegio.setOnAction(event ->
                atualizarPermissoesAnciao()
        );



        criarLayout();


        if (pessoaEdicao != null) {
            preencherFormulario();
        }
    }



    private void atualizarPermissoesAnciao() {

        boolean anciao =
                comboPrivilegio.getValue()
                        == Privilegio.ANCIAO;


        checkPresidente.setDisable(!anciao);
        checkDirigente.setDisable(!anciao);


        if (anciao) {

            checkPresidente.setSelected(true);
            checkDirigente.setSelected(true);

        } else {

            checkPresidente.setSelected(false);
            checkDirigente.setSelected(false);

        }
    }




    private void criarLayout() {

        root.setPadding(new Insets(20));
        root.setHgap(15);
        root.setVgap(15);


        Label titulo =
                new Label("Cadastro de Pessoa");


        titulo.setStyle(
                "-fx-font-size:18px;-fx-font-weight:bold;"
        );


        root.add(titulo,0,0,2,1);



        root.add(new Label("Nome:"),0,1);
        root.add(campoNome,1,1);


        root.add(new Label("Sexo:"),0,2);
        root.add(comboSexo,1,2);


        root.add(new Label("Privilégio:"),0,3);
        root.add(comboPrivilegio,1,3);


        root.add(new Label("Nível leitura:"),0,4);
        root.add(comboNivelLeitura,1,4);



        root.add(checkResponsavel,0,5);
        root.add(checkAjudante,1,5);


        root.add(checkLeitura,0,6);
        root.add(checkDiscurso,1,6);


        root.add(checkOracao,0,7);


        root.add(checkPresidente,0,8);
        root.add(checkDirigente,1,8);


        root.add(checkAtivo,0,9);



        Button salvar =
                new Button("Salvar");


        Button cancelar =
                new Button("Cancelar");


        salvar.setOnAction(e -> salvarPessoa());

        cancelar.setOnAction(e -> aoCancelar.run());


        HBox botoes =
                new HBox(
                        10,
                        salvar,
                        cancelar
                );


        botoes.setAlignment(Pos.CENTER_RIGHT);


        root.add(
                botoes,
                1,
                10
        );
    }



    private void salvarPessoa() {

        if (campoNome.getText().isBlank()) {
            mostrarMensagem("Informe o nome da pessoa.");
            return;
        }


        if (comboSexo.getValue() == null) {
            mostrarMensagem("Selecione o sexo.");
            return;
        }


        if (comboPrivilegio.getValue() == null) {
            mostrarMensagem("Selecione o privilégio.");
            return;
        }



        Pessoa pessoa =
                new Pessoa(
                        campoNome.getText(),
                        comboSexo.getValue(),
                        checkAtivo.isSelected(),
                        checkResponsavel.isSelected(),
                        checkAjudante.isSelected(),
                        checkLeitura.isSelected(),
                        checkDiscurso.isSelected(),
                        checkOracao.isSelected(),
                        checkPresidente.isSelected(),
                        checkDirigente.isSelected(),
                        comboPrivilegio.getValue(),
                        comboNivelLeitura.getValue()
                );


        if (pessoaEdicao != null) {
            pessoa.setId(
                    pessoaEdicao.getId()
            );
        }


        if (pessoaEdicao == null) {
            aoSalvar.accept(pessoa);
        } else {
            aoAtualizar.accept(pessoa);
        }
    }



    private void preencherFormulario() {

        campoNome.setText(
                pessoaEdicao.getNome()
        );

        comboSexo.setValue(
                pessoaEdicao.getSexo()
        );


        comboPrivilegio.setValue(
                pessoaEdicao.getPrivilegio()
        );


        atualizarPermissoesAnciao();


        comboNivelLeitura.setValue(
                pessoaEdicao.getNivelLeitura()
        );


        checkAtivo.setSelected(
                pessoaEdicao.isAtivo()
        );


        checkResponsavel.setSelected(
                pessoaEdicao.podeSerResponsavel()
        );


        checkAjudante.setSelected(
                pessoaEdicao.podeSerAjudante()
        );


        checkLeitura.setSelected(
                pessoaEdicao.podeFazerLeitura()
        );


        checkDiscurso.setSelected(
                pessoaEdicao.podeFazerDiscurso()
        );


        checkOracao.setSelected(
                pessoaEdicao.podeFazerOracao()
        );


        checkPresidente.setSelected(
                pessoaEdicao.podeSerPresidente()
        );


        checkDirigente.setSelected(
                pessoaEdicao.podeSerDirigente()
        );
    }



    private void mostrarMensagem(String mensagem) {

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
}