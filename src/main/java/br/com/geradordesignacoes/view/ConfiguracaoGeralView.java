package br.com.geradordesignacoes.view;

import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Tela para consultar e editar as preferências gerais do aplicativo. */
public class ConfiguracaoGeralView {

    private static final Locale LOCALE_PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter FORMATO_HORARIO =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ConfiguracaoGeralService service;
    private final VBox root;
    private final TextField nomeCongregacao;
    private final ComboBox<DayOfWeek> diaReuniao;
    private final TextField horarioInicio;
    private final TextField duracaoCanticoInicial;
    private final TextField duracaoComentariosIniciais;
    private final TextField duracaoCanticoMeio;
    private final TextField duracaoComentariosFinais;
    private final TextField duracaoCanticoFinal;
    private final Label mensagem;

    public ConfiguracaoGeralView() {
        this(new ConfiguracaoGeralService());
    }

    ConfiguracaoGeralView(ConfiguracaoGeralService service) {
        this.service = service;
        nomeCongregacao = new TextField();
        nomeCongregacao.setPromptText("Nome da congregação");
        diaReuniao = new ComboBox<>();
        diaReuniao.getItems().setAll(DayOfWeek.values());
        diaReuniao.setMaxWidth(Double.MAX_VALUE);
        diaReuniao.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(DayOfWeek dia) {
                return dia == null ? "" : dia.getDisplayName(TextStyle.FULL, LOCALE_PT_BR);
            }

            @Override
            public DayOfWeek fromString(String texto) {
                for (DayOfWeek dia : DayOfWeek.values()) {
                    if (dia.getDisplayName(TextStyle.FULL, LOCALE_PT_BR)
                            .equalsIgnoreCase(texto)) {
                        return dia;
                    }
                }
                return null;
            }
        });
        horarioInicio = campo("19:30");
        duracaoCanticoInicial = campo("5");
        duracaoComentariosIniciais = campo("1");
        duracaoCanticoMeio = campo("4");
        duracaoComentariosFinais = campo("3");
        duracaoCanticoFinal = campo("5");
        mensagem = new Label();
        mensagem.setWrapText(true);
        root = criarLayout();
    }

    private VBox criarLayout() {
        Label titulo = new Label("Configurações gerais");
        titulo.getStyleClass().add("page-title");

        Label subtitulo = new Label(
                "Configure os dados habituais da congregação e os tempos dos itens complementares da reunião."
        );
        subtitulo.getStyleClass().add("page-subtitle");
        subtitulo.setWrapText(true);

        VBox card = new VBox(14);
        card.getStyleClass().add("card");
        card.setMaxWidth(760);

        Label tituloIdentificacao = new Label("Identificação e horário");
        tituloIdentificacao.getStyleClass().add("card-title");

        GridPane campos = new GridPane();
        campos.setHgap(14);
        campos.setVgap(12);
        campos.add(rotulo("Nome da congregação"), 0, 0);
        campos.add(nomeCongregacao, 1, 0);
        campos.add(rotulo("Dia habitual da reunião"), 0, 1);
        campos.add(diaReuniao, 1, 1);
        campos.add(rotulo("Horário habitual de início (HH:mm)"), 0, 2);
        campos.add(horarioInicio, 1, 2);
        GridPane.setHgrow(nomeCongregacao, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(diaReuniao, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(horarioInicio, javafx.scene.layout.Priority.ALWAYS);

        Label tituloDuracoes = new Label("Durações dos itens complementares");
        tituloDuracoes.getStyleClass().add("card-title");

        GridPane duracoes = new GridPane();
        duracoes.setHgap(14);
        duracoes.setVgap(12);
        adicionarDuracao(duracoes, 0, "Cântico e oração iniciais (min)", duracaoCanticoInicial);
        adicionarDuracao(duracoes, 1, "Comentários iniciais (min)", duracaoComentariosIniciais);
        adicionarDuracao(duracoes, 2, "Cântico do meio (min)", duracaoCanticoMeio);
        adicionarDuracao(duracoes, 3, "Comentários finais (min)", duracaoComentariosFinais);
        adicionarDuracao(duracoes, 4, "Cântico e oração finais (min)", duracaoCanticoFinal);

        Label aviso = new Label(
                "Esses valores são padrões gerais. As durações das partes programáveis continuam no Cadastro de Partes, e ajustes específicos permanecem na programação/escala."
        );
        aviso.getStyleClass().add("page-subtitle");
        aviso.setWrapText(true);

        Button salvar = new Button("Salvar configurações");
        salvar.getStyleClass().add("primary-button");
        salvar.setOnAction(event -> salvar());

        Button recarregar = new Button("Recarregar");
        recarregar.getStyleClass().add("secondary-button");
        recarregar.setOnAction(event -> atualizar());

        HBox botoes = new HBox(10, salvar, recarregar);
        botoes.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(
                tituloIdentificacao, campos,
                tituloDuracoes, duracoes,
                aviso, botoes, mensagem
        );

        VBox conteudo = new VBox(18, titulo, subtitulo, card);
        conteudo.getStyleClass().add("content-area");
        conteudo.setPadding(new Insets(28));
        atualizar();
        return conteudo;
    }

    private static TextField campo(String prompt) {
        TextField campo = new TextField();
        campo.setPromptText(prompt);
        campo.setPrefColumnCount(8);
        return campo;
    }

    private static Label rotulo(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("label");
        return label;
    }

    private static void adicionarDuracao(
            GridPane grid, int linha, String texto, TextField campo) {
        grid.add(rotulo(texto), 0, linha);
        grid.add(campo, 1, linha);
        campo.setPrefWidth(120);
    }

    public void atualizar() {
        try {
            ConfiguracaoGeral configuracao = service.buscar();
            nomeCongregacao.setText(configuracao.nomeCongregacao());
            diaReuniao.setValue(configuracao.diaReuniaoHabitual());
            horarioInicio.setText(configuracao.horarioInicio().format(FORMATO_HORARIO));
            duracaoCanticoInicial.setText(
                    Integer.toString(configuracao.duracaoCanticoOracaoInicialMinutos()));
            duracaoComentariosIniciais.setText(
                    Integer.toString(configuracao.duracaoComentariosIniciaisMinutos()));
            duracaoCanticoMeio.setText(
                    Integer.toString(configuracao.duracaoCanticoMeioMinutos()));
            duracaoComentariosFinais.setText(
                    Integer.toString(configuracao.duracaoComentariosFinaisMinutos()));
            duracaoCanticoFinal.setText(
                    Integer.toString(configuracao.duracaoCanticoOracaoFinalMinutos()));
            mostrarMensagem("", true);
        } catch (RuntimeException e) {
            mostrarMensagem("Não foi possível carregar as configurações gerais.", false);
        }
    }

    private void salvar() {
        try {
            String nome = nomeCongregacao.getText() == null
                    ? "" : nomeCongregacao.getText().trim();
            DayOfWeek dia = diaReuniao.getValue();
            if (dia == null) {
                throw new IllegalArgumentException("Selecione o dia habitual da reunião.");
            }

            LocalTime inicio;
            try {
                inicio = LocalTime.parse(horarioInicio.getText().trim(), FORMATO_HORARIO);
            } catch (RuntimeException e) {
                throw new IllegalArgumentException(
                        "Informe o horário no formato HH:mm, por exemplo 19:30.");
            }

            ConfiguracaoGeral configuracao = new ConfiguracaoGeral(
                    nome,
                    dia,
                    inicio,
                    lerDuracao(duracaoCanticoInicial, "cântico e oração iniciais"),
                    lerDuracao(duracaoComentariosIniciais, "comentários iniciais"),
                    lerDuracao(duracaoCanticoMeio, "cântico do meio"),
                    lerDuracao(duracaoComentariosFinais, "comentários finais"),
                    lerDuracao(duracaoCanticoFinal, "cântico e oração finais")
            );
            service.salvar(configuracao);
            mostrarMensagem("Configurações gerais salvas com sucesso.", true);
        } catch (IllegalArgumentException e) {
            mostrarMensagem(e.getMessage(), false);
        } catch (RuntimeException e) {
            mostrarMensagem("Não foi possível salvar as configurações gerais.", false);
        }
    }

    private static int lerDuracao(TextField campo, String descricao) {
        try {
            int minutos = Integer.parseInt(campo.getText().trim());
            if (minutos <= 0) {
                throw new NumberFormatException();
            }
            return minutos;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                    "Informe uma duração inteira maior que zero para " + descricao + ".");
        }
    }

    private void mostrarMensagem(String texto, boolean sucesso) {
        mensagem.setText(texto);
        mensagem.getStyleClass().removeAll("status-success", "status-error");
        if (!texto.isBlank()) {
            mensagem.getStyleClass().add(sucesso ? "status-success" : "status-error");
        }
    }

    public Parent getView() {
        return root;
    }
}
