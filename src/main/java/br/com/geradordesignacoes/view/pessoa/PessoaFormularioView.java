package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
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

    private final CheckBox checkResponsavel;

    private final CheckBox checkAjudante;

    private final CheckBox checkLeitura;

    private final CheckBox checkDiscurso;

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

        // Cria os componentes
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

        // Monta a interface
        criarLayout();

        // Se estiver editando, preenche os campos
        if (pessoaEdicao != null) {
            preencherFormulario();
        }
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


        HBox botoes = new HBox(10, salvar, cancelar);
        botoes.setAlignment(Pos.CENTER);

        root.add(botoes, 1, 6);
    }

    public Parent getView() {
        return root;
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

        if (pessoaEdicao != null) {
            pessoa.setId(pessoaEdicao.getId());
        }

        if (pessoaEdicao == null) {

            aoSalvar.accept(pessoa);

        } else {

            aoAtualizar.accept(pessoa);

        }
    }

    private void mostrarMensagem(String mensagem) {

        Alert alert = new Alert(Alert.AlertType.WARNING);

        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);

        alert.showAndWait();
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
    }


}