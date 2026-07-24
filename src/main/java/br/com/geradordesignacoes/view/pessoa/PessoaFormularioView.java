package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.Pessoa;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

public class PessoaFormularioView {

    private final GridPane root;

    private final TextField campoNome;

    private final ComboBox<Sexo> comboSexo;

    private final ComboBox<Privilegio> comboPrivilegio;

    private final CheckBox checkResponsavel;

    private final CheckBox checkAjudante;

    private final CheckBox checkLeitura;

    private final CheckBox checkDiscurso;

    private final CheckBox checkAtivo;

    private final Runnable aoCancelar;

    private final Consumer<Pessoa> aoSalvar;


    public PessoaFormularioView(
            Consumer<Pessoa> aoSalvar,
            Runnable aoCancelar
    ) {

        this.aoSalvar = aoSalvar;
        this.aoCancelar = aoCancelar;

        root = new GridPane();

        campoNome = new TextField();

        comboSexo = new ComboBox<>();
        comboSexo.getItems().addAll(Sexo.values());

        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems().addAll(Privilegio.values());

        checkResponsavel = new CheckBox("Pode ser responsável");

        checkAjudante = new CheckBox("Pode ser ajudante");

        checkLeitura = new CheckBox("Pode fazer leitura");

        checkDiscurso = new CheckBox("Pode fazer discurso");

        checkAtivo = new CheckBox("Ativo");
        checkAtivo.setSelected(true);

        criarLayout();
    }


    private void criarLayout() {

        root.setPadding(new Insets(20));
        root.setHgap(10);
        root.setVgap(10);


        root.add(new Label("Nome:"), 0, 0);
        root.add(campoNome, 1, 0);


        root.add(new Label("Sexo:"), 0, 1);
        root.add(comboSexo, 1, 1);


        root.add(new Label("Privilégio:"), 0, 2);
        root.add(comboPrivilegio, 1, 2);


        root.add(checkResponsavel, 0, 3);
        root.add(checkAjudante, 1, 3);

        root.add(checkLeitura, 0, 4);
        root.add(checkDiscurso, 1, 4);

        root.add(checkAtivo, 0, 5);


        Button salvar = new Button("Salvar");
        Button cancelar = new Button("Cancelar");

        salvar.setOnAction(event -> salvarPessoa());

        cancelar.setOnAction(event -> aoCancelar.run());


        HBox botoes = new HBox(
                10,
                salvar,
                cancelar
        );

        botoes.setAlignment(Pos.CENTER);


        root.add(botoes, 1, 5);
    }


    public Parent getView() {
        return root;
    }

    private void salvarPessoa() {

        Pessoa pessoa = new Pessoa(

                campoNome.getText(),

                comboSexo.getValue(),

                checkAtivo.isSelected(),

                checkResponsavel.isSelected(),

                checkAjudante.isSelected(),

                checkLeitura.isSelected(),

                checkDiscurso.isSelected(),

                comboPrivilegio.getValue()

        );


        aoSalvar.accept(pessoa);
    }

}