package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.function.Consumer;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final CheckBox checkSalaPrincipal;
    private final CheckBox checkSalaB;
    private final CheckBox checkSalaC;
    private final Map<HabilidadeDesignacaoCodigo, CheckBox> checkboxesHabilidades = new EnumMap<>(HabilidadeDesignacaoCodigo.class);

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
        comboSexo.setOnAction(_ -> atualizarPermissoesSexo());

        comboPrivilegio = new ComboBox<>();
        comboPrivilegio.getItems().addAll(Privilegio.values());
        comboPrivilegio.setMaxWidth(Double.MAX_VALUE);
        comboPrivilegio.getStyleClass().add("combo-box");
        comboPrivilegio.setOnAction(_ -> atualizarPermissoesAnciao());

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
        checkSalaPrincipal = new CheckBox("Sala Principal");
        checkSalaPrincipal.setSelected(true);

        checkSalaB = new CheckBox("Sala B");
        checkSalaB.setSelected(true);

        checkSalaC = new CheckBox("Sala C");
        checkSalaC.setSelected(true);

        for (HabilidadeDesignacaoCodigo habilidade : HabilidadeDesignacaoCodigo.values()) {
            checkboxesHabilidades.put(habilidade, new CheckBox(habilidade.nome()));
        }

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


        VBox habilidades = criarCartao("Habilidades para designações");
        Label descricaoHabilidades = new Label(
                "Marque individualmente as atividades para as quais a pessoa já está apta. "
                        + "Essas habilidades não substituem as regras de sexo, privilégio, leitura ou participação.");
        descricaoHabilidades.setWrapText(true);
        descricaoHabilidades.getStyleClass().add("page-subtitle");
        GridPane gradeHabilidades = new GridPane();
        gradeHabilidades.setHgap(24);
        gradeHabilidades.setVgap(12);
        int indiceHabilidade = 0;
        for (CheckBox checkBox : checkboxesHabilidades.values()) {
            gradeHabilidades.add(checkBox, indiceHabilidade % 2, indiceHabilidade / 2);
            indiceHabilidade++;
        }
        habilidades.getChildren().addAll(descricaoHabilidades, gradeHabilidades);

        VBox status = criarCartao("Status do cadastro");
        status.getChildren().add(checkAtivo);
        VBox salas = criarCartao("Permissões de participação");

        Label descricaoSalas = new Label(
                "Selecione em quais salas esta pessoa poderá receber designações."
        );
        descricaoSalas.getStyleClass().add("page-subtitle");
        descricaoSalas.setWrapText(true);

        HBox opcoesSalas = new HBox(
                24,
                checkSalaPrincipal,
                checkSalaB,
                checkSalaC
        );

        salas.getChildren().addAll(
                descricaoSalas,
                opcoesSalas
        );

        VBox conteudo = new VBox(
                18,
                dadosPessoais,
                funcoes,
                habilidades,
                salas,
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
        salvar.setOnAction(_ -> salvarPessoa());

        Button cancelar = new Button("Cancelar");
        cancelar.getStyleClass().add("secondary-button");
        cancelar.setCancelButton(true);
        cancelar.setOnAction(_ -> aoCancelar.run());

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
        Privilegio privilegio = comboPrivilegio.getValue();
        boolean anciao = privilegio == Privilegio.ANCIAO;
        boolean podeDirigirEstudo = privilegioPodeDirigirEstudo(privilegio);

        checkPresidente.setDisable(!anciao);
        checkDirigente.setDisable(!podeDirigirEstudo);

        if (anciao) {
            checkPresidente.setSelected(true);
            checkDirigente.setSelected(true);
        } else {
            checkPresidente.setSelected(false);
            if (!podeDirigirEstudo) {
                checkDirigente.setSelected(false);
            }
        }
    }

    static boolean privilegioPodeDirigirEstudo(Privilegio privilegio) {
        return privilegio == Privilegio.ANCIAO
                || privilegio == Privilegio.SERVO_MINISTERIAL;
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
                comboNivelLeitura.getValue(),
                checkSalaPrincipal.isSelected(),
                checkSalaB.isSelected(),
                checkSalaC.isSelected()
        );

        pessoa.setHabilidadesDesignacao(checkboxesHabilidades.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(entry -> entry.getKey().codigo())
                .collect(Collectors.toSet()));

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

        for (HabilidadeDesignacaoCodigo habilidade : HabilidadeDesignacaoCodigo.values()) {
            checkboxesHabilidades.get(habilidade).setSelected(
                    pessoaEdicao.possuiHabilidade(habilidade.codigo()));
        }

        checkSalaPrincipal.setSelected(
                pessoaEdicao.podeParticiparSalaPrincipal()
        );

        checkSalaB.setSelected(
                pessoaEdicao.podeParticiparSalaB()
        );

        checkSalaC.setSelected(
                pessoaEdicao.podeParticiparSalaC()
        );
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