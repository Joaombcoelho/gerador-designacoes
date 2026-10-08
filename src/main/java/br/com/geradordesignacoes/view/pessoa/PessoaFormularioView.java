package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;

public class PessoaFormularioView {

    private final BorderPane root;

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

        root = new BorderPane();
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        campoNome = new TextField();
        campoNome.setPromptText("Digite o nome completo");
        campoNome.getStyleClass().add("text-field");

        comboSexo = new ComboBox<>();
        comboSexo.getItems().addAll(Sexo.values());
        comboSexo.setMaxWidth(Double.MAX_VALUE);
        comboSexo.getStyleClass().add("combo-box");
        comboSexo.setOnAction(event -> atualizarPermissoesSexo());

        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems().addAll(Privilegio.values());
        comboPrivilegio.setMaxWidth(Double.MAX_VALUE);
        comboPrivilegio.getStyleClass().add("combo-box");
        comboPrivilegio.setOnAction(event -> atualizarPermissoesAnciao());

        comboNivelLeitura = new ComboBox<>();
        comboNivelLeitura.getItems().addAll(NivelLeitura.values());
        comboNivelLeitura.setValue(NivelLeitura.BASICO);
        comboNivelLeitura.setMaxWidth(Double.MAX_VALUE);
        comboNivelLeitura.getStyleClass().add("combo-box");

        checkResponsavel = new CheckBox("Pode ser responsável");
        checkAjudante = new CheckBox("Pode ser ajudante");
        checkLeitura = new CheckBox("Pode fazer leitura");
        checkDiscurso = new CheckBox("Pode fazer discurso");
        checkOracao = new CheckBox("Pode fazer oração");
        checkPresidente = new CheckBox("Pode ser presidente");
        checkDirigente = new CheckBox("Pode ser dirigente");

        checkPresidente.setDisable(true);
        checkDirigente.setDisable(true);

        checkAtivo = new CheckBox("Pessoa ativa");
        checkAtivo.setSelected(true);

        criarLayout();

        if (pessoaEdicao != null) {
            preencherFormulario();
        } else {
            atualizarPermissoesAnciao();
            atualizarPermissoesSexo();
        }
    }

    private void criarLayout() {
        Label titulo = new Label(
                pessoaEdicao == null ? "Nova Pessoa" : "Editar Pessoa"
        );
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Cadastre os dados e defina as funções permitidas."
        );
        subtitulo.getStyleClass().add("page-subtitle");

        VBox cabecalho = new VBox(6, titulo, subtitulo);
        cabecalho.setPadding(new Insets(0, 0, 20, 0));

        VBox dadosPessoais = criarCartao("Dados pessoais");

        GridPane gradeDados = new GridPane();
        gradeDados.setHgap(18);
        gradeDados.setVgap(14);

        ColumnConstraints colunaRotulo = new ColumnConstraints();
        colunaRotulo.setMinWidth(120);

        ColumnConstraints colunaCampo = new ColumnConstraints();
        colunaCampo.setHgrow(Priority.ALWAYS);
        colunaCampo.setFillWidth(true);

        gradeDados.getColumnConstraints().addAll(
                colunaRotulo,
                colunaCampo
        );

        adicionarCampo(gradeDados, "Nome completo", campoNome, 0);
        adicionarCampo(gradeDados, "Sexo", comboSexo, 1);
        adicionarCampo(gradeDados, "Privilégio", comboPrivilegio, 2);
        adicionarCampo(gradeDados, "Nível de leitura", comboNivelLeitura, 3);

        dadosPessoais.getChildren().add(gradeDados);

        VBox funcoes = criarCartao("Funções e participações");

        Label descricaoFuncoes = new Label(
                "Marque as funções que esta pessoa está autorizada a exercer."
        );
        descricaoFuncoes.getStyleClass().add("page-subtitle");
        descricaoFuncoes.setWrapText(true);

        GridPane gradeFuncoes = new GridPane();
        gradeFuncoes.setHgap(24);
        gradeFuncoes.setVgap(16);

        gradeFuncoes.add(checkResponsavel, 0, 0);
        gradeFuncoes.add(checkAjudante, 1, 0);
        gradeFuncoes.add(checkLeitura, 0, 1);
        gradeFuncoes.add(checkDiscurso, 1, 1);
        gradeFuncoes.add(checkOracao, 0, 2);
        gradeFuncoes.add(checkPresidente, 0, 3);
        gradeFuncoes.add(checkDirigente, 1, 3);

        funcoes.getChildren().addAll(
                descricaoFuncoes,
                gradeFuncoes
        );

        VBox status = criarCartao("Status do cadastro");
        status.getChildren().add(checkAtivo);

        VBox conteudo = new VBox(
                18,
                dadosPessoais,
                funcoes,
                status
        );

        ScrollPane rolagem = new ScrollPane(conteudo);
        rolagem.setFitToWidth(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rolagem.getStyleClass().add("scroll-pane");
        VBox.setVgrow(rolagem, Priority.ALWAYS);

        Button salvar = new Button(
                pessoaEdicao == null ? "Salvar pessoa" : "Salvar alterações"
        );
        salvar.getStyleClass().add("primary-button");
        salvar.setDefaultButton(true);
        salvar.setOnAction(event -> salvarPessoa());

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("secondary-button");
        cancelar.setCancelButton(true);
        cancelar.setOnAction(event -> aoCancelar.run());

        HBox botoes = new HBox(10, cancelar, salvar);
        botoes.setAlignment(Pos.CENTER_RIGHT);
        botoes.setPadding(new Insets(18, 0, 0, 0));

        root.setTop(cabecalho);
        root.setCenter(rolagem);
        root.setBottom(botoes);
    }

    private VBox criarCartao(String tituloTexto) {
        Label titulo = new Label(tituloTexto);
        titulo.getStyleClass().add("card-title");

        VBox cartao = new VBox(16);
        cartao.getStyleClass().add("card");
        cartao.setPadding(new Insets(20));
        cartao.getChildren().add(titulo);

        return cartao;
    }

    private void adicionarCampo(
            GridPane grade,
            String texto,
            Control campo,
            int linha
    ) {
        Label rotulo = new Label(texto);
        rotulo.setMinWidth(120);

        if (campo instanceof TextField textField) {
            textField.setMaxWidth(Double.MAX_VALUE);
        }

        if (campo instanceof ComboBox<?> comboBox) {
            comboBox.setMaxWidth(Double.MAX_VALUE);
        }

        grade.add(rotulo, 0, linha);
        grade.add(campo, 1, linha);

        GridPane.setHgrow(campo, Priority.ALWAYS);
    }

    private void atualizarPermissoesAnciao() {
        boolean anciao =
                comboPrivilegio.getValue() == Privilegio.ANCIAO;

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

    private void atualizarPermissoesSexo() {
        boolean feminino = comboSexo.getValue() == Sexo.FEMININO;

        checkLeitura.setDisable(feminino);
        checkDiscurso.setDisable(feminino);
        checkOracao.setDisable(feminino);

        if (feminino) {
            checkLeitura.setSelected(false);
            checkDiscurso.setSelected(false);
            checkOracao.setSelected(false);
        }
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

        if (comboSexo.getValue() == Sexo.FEMININO) {
            checkLeitura.setSelected(false);
            checkDiscurso.setSelected(false);
            checkOracao.setSelected(false);
        }

        Pessoa pessoa = new Pessoa(
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
            pessoa.setId(pessoaEdicao.getId());
            aoAtualizar.accept(pessoa);
        } else {
            aoSalvar.accept(pessoa);
        }
    }

    private void preencherFormulario() {
        campoNome.setText(pessoaEdicao.getNome());
        comboSexo.setValue(pessoaEdicao.getSexo());
        comboPrivilegio.setValue(pessoaEdicao.getPrivilegio());

        atualizarPermissoesAnciao();

        comboNivelLeitura.setValue(pessoaEdicao.getNivelLeitura());

        checkAtivo.setSelected(pessoaEdicao.isAtivo());
        checkResponsavel.setSelected(pessoaEdicao.podeSerResponsavel());
        checkAjudante.setSelected(pessoaEdicao.podeSerAjudante());
        checkLeitura.setSelected(pessoaEdicao.podeFazerLeitura());
        checkDiscurso.setSelected(pessoaEdicao.podeFazerDiscurso());
        checkOracao.setSelected(pessoaEdicao.podeFazerOracao());
        checkPresidente.setSelected(pessoaEdicao.podeSerPresidente());
        checkDirigente.setSelected(pessoaEdicao.podeSerDirigente());

        atualizarPermissoesSexo();
    }

    private void mostrarMensagem(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    public Parent getView() {
        return root;
    }
}