package br.com.geradordesignacoes.view.eventos;

import br.com.geradordesignacoes.dao.EventoEspecialDAO;
import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.EventoEspecialRegistrado;
import br.com.geradordesignacoes.model.TipoEventoEspecial;
import br.com.geradordesignacoes.service.eventos.AcaoResolucaoEvento;
import br.com.geradordesignacoes.service.eventos.ResolvedorEventosEspeciais;
import br.com.geradordesignacoes.service.eventos.ResultadoResolucaoEventos;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Cadastro e prévia de eventos especiais.
 *
 * A prévia é somente leitura: nenhuma ação deste componente aplica alterações
 * à programação, escalas, histórico ou ajustes manuais do S-140.
 */
public class EventosEspeciaisView {

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    private final BorderPane root = new BorderPane();
    private final EventoEspecialDAO dao = new EventoEspecialDAO();
    private final ResolvedorEventosEspeciais resolvedor = new ResolvedorEventosEspeciais();

    private final ComboBox<TipoEventoEspecial> tipo = new ComboBox<>();
    private final DatePicker dataInicio = new DatePicker(LocalDate.now());
    private final DatePicker dataFim = new DatePicker(LocalDate.now());
    private final TextField tema = new TextField();
    private final TextField descricao = new TextField();
    private final DatePicker dataReferencia = new DatePicker(LocalDate.now());
    private final TableView<EventoEspecialRegistrado> tabela = new TableView<>();
    private final TextArea resultadoPrevia = new TextArea();
    private final ComboBox<AcaoResolucaoEvento> escolhaConflito = new ComboBox<>();
    private final Label status = new Label("Gerencie os eventos e consulte a prévia.");
    private final Button salvar = new Button("Cadastrar evento");
    private long idEdicao = -1;

    public EventosEspeciaisView() {
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        criarCabecalho();
        criarFormulario();
        criarTabela();
        criarPrevia();
        carregarEventos();
        atualizarCamposTipo();
    }

    public Parent getView() {
        return root;
    }

    private void criarCabecalho() {
        Label titulo = new Label("Eventos especiais");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label(
                "Cadastre visitas, assembleias, congressos, celebrações e cancelamentos. "
                + "Consulte o impacto proposto antes de qualquer aplicação.");
        subtitulo.getStyleClass().add("page-subtitle");
        VBox cabecalho = new VBox(6, titulo, subtitulo);
        cabecalho.setPadding(new Insets(0, 0, 18, 0));
        root.setTop(cabecalho);
    }

    private void criarFormulario() {
        tipo.getItems().setAll(TipoEventoEspecial.values());
        tipo.setPromptText("Tipo do evento");
        tipo.setMaxWidth(Double.MAX_VALUE);
        tipo.setOnAction(_ -> atualizarCamposTipo());

        dataInicio.setMaxWidth(Double.MAX_VALUE);
        dataFim.setMaxWidth(Double.MAX_VALUE);
        tema.setPromptText("Tema do discurso (somente visita)");
        descricao.setPromptText("Observação opcional");

        GridPane campos = new GridPane();
        campos.setHgap(12);
        campos.setVgap(8);
        campos.add(new Label("Tipo"), 0, 0);
        campos.add(new Label("Data inicial"), 1, 0);
        campos.add(new Label("Data final"), 2, 0);
        campos.add(tipo, 0, 1);
        campos.add(dataInicio, 1, 1);
        campos.add(dataFim, 2, 1);
        campos.add(new Label("Tema do discurso"), 0, 2);
        campos.add(new Label("Descrição"), 1, 2);
        campos.add(tema, 0, 3, 1, 1);
        campos.add(descricao, 1, 3, 2, 1);
        GridPane.setHgrow(tipo, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(dataInicio, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(dataFim, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(tema, javafx.scene.layout.Priority.ALWAYS);
        GridPane.setHgrow(descricao, javafx.scene.layout.Priority.ALWAYS);

        Button limpar = new Button("Limpar");
        Button editar = new Button("Editar selecionado");
        Button excluir = new Button("Excluir selecionado");
        salvar.getStyleClass().add("primary-button");
        limpar.setOnAction(_ -> limparFormulario());
        salvar.setOnAction(_ -> salvarEvento());
        editar.setOnAction(_ -> carregarSelecionadoParaEdicao());
        excluir.setOnAction(_ -> excluirSelecionado());

        HBox botoes = new HBox(8, salvar, editar, excluir, limpar);
        botoes.setAlignment(Pos.CENTER_LEFT);
        VBox formulario = new VBox(12, campos, botoes, status);
        formulario.setPadding(new Insets(0, 0, 18, 0));
        root.setCenter(formulario);
    }

    private void criarTabela() {
        TableColumn<EventoEspecialRegistrado, String> colunaTipo =
                new TableColumn<>("Tipo");
        colunaTipo.setCellValueFactory(c -> new SimpleStringProperty(
                rotulo(c.getValue().evento().tipo())));
        colunaTipo.setPrefWidth(190);

        TableColumn<EventoEspecialRegistrado, String> colunaPeriodo =
                new TableColumn<>("Período");
        colunaPeriodo.setCellValueFactory(c -> new SimpleStringProperty(
                DATA.format(c.getValue().evento().dataInicio()) + " a "
                        + DATA.format(c.getValue().evento().dataFim())));
        colunaPeriodo.setPrefWidth(200);

        TableColumn<EventoEspecialRegistrado, String> colunaTema =
                new TableColumn<>("Tema / descrição");
        colunaTema.setCellValueFactory(c -> new SimpleStringProperty(
                Optional.ofNullable(c.getValue().evento().temaDiscurso())
                        .orElse(Optional.ofNullable(c.getValue().evento().descricao()).orElse(""))));
        colunaTema.setPrefWidth(260);

        tabela.getColumns().setAll(colunaTipo, colunaPeriodo, colunaTema);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabela.setPrefHeight(190);
        tabela.getSelectionModel().selectedItemProperty().addListener(
                (_, _, selecionado) -> {
                    if (selecionado != null) {
                        atualizarStatus("Evento selecionado: " + rotulo(selecionado.evento().tipo()));
                    }
                });
    }

    private void criarPrevia() {
        Label titulo = new Label("Prévia da reunião");
        titulo.getStyleClass().add("section-title");
        Label aviso = new Label(
                "A prévia não grava nem aplica mudanças. A escolha de conflito vale somente "
                + "para esta consulta e ainda não é aplicada à programação.");
        aviso.setWrapText(true);

        dataReferencia.setMaxWidth(180);
        Button atualizar = new Button("Atualizar prévia");
        atualizar.setOnAction(_ -> atualizarPrevia());

        escolhaConflito.setPromptText("Escolha uma alternativa para simular");
        escolhaConflito.setMaxWidth(320);
        escolhaConflito.setOnAction(_ -> atualizarPrevia());

        resultadoPrevia.setEditable(false);
        resultadoPrevia.setWrapText(true);
        resultadoPrevia.setPrefRowCount(5);

        VBox painel = new VBox(10,
                new Separator(),
                titulo,
                aviso,
                new HBox(10, new Label("Semana de referência:"), dataReferencia, atualizar),
                new Label("Alternativa para conflito (opcional):"),
                escolhaConflito,
                resultadoPrevia);
        painel.setPadding(new Insets(16, 0, 0, 0));
        root.setBottom(painel);
    }

    private void carregarEventos() {
        try {
            tabela.getItems().setAll(dao.listarTodos());
            atualizarPrevia();
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível carregar os eventos.", e);
        }
    }

    private void salvarEvento() {
        try {
            if (tipo.getValue() == null || dataInicio.getValue() == null
                    || dataFim.getValue() == null) {
                atualizarStatus("Informe o tipo e as duas datas.");
                return;
            }
            EventoEspecial evento = new EventoEspecial(
                    tipo.getValue(), dataInicio.getValue(), dataFim.getValue(),
                    tema.getText(), descricao.getText());

            if (idEdicao > 0) {
                dao.atualizar(idEdicao, evento);
                atualizarStatus("Evento atualizado.");
            } else {
                dao.salvar(evento);
                atualizarStatus("Evento cadastrado.");
            }
            limparFormulario();
            carregarEventos();
        } catch (IllegalArgumentException e) {
            atualizarStatus(e.getMessage());
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível salvar o evento.", e);
        }
    }

    private void carregarSelecionadoParaEdicao() {
        EventoEspecialRegistrado selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            atualizarStatus("Selecione um evento para editar.");
            return;
        }
        EventoEspecial evento = selecionado.evento();
        idEdicao = selecionado.id();
        tipo.setValue(evento.tipo());
        dataInicio.setValue(evento.dataInicio());
        dataFim.setValue(evento.dataFim());
        tema.setText(evento.temaDiscurso() == null ? "" : evento.temaDiscurso());
        descricao.setText(evento.descricao() == null ? "" : evento.descricao());
        salvar.setText("Salvar alterações");
        atualizarCamposTipo();
        atualizarStatus("Editando evento #" + idEdicao + ".");
    }

    private void excluirSelecionado() {
        EventoEspecialRegistrado selecionado = tabela.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            atualizarStatus("Selecione um evento para excluir.");
            return;
        }
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Excluir evento");
        confirmacao.setHeaderText("Excluir o evento selecionado?");
        confirmacao.setContentText("A exclusão remove somente o cadastro do evento. "
                + "Nenhuma programação ou escala será alterada.");
        Optional<ButtonType> resposta = confirmacao.showAndWait();
        if (resposta.isPresent() && resposta.get() == ButtonType.OK) {
            try {
                dao.excluir(selecionado.id());
                if (idEdicao == selecionado.id()) {
                    limparFormulario();
                }
                carregarEventos();
                atualizarStatus("Evento excluído; a programação foi preservada.");
            } catch (RuntimeException e) {
                mostrarErro("Não foi possível excluir o evento.", e);
            }
        }
    }

    private void atualizarPrevia() {
        LocalDate referencia = dataReferencia.getValue();
        if (referencia == null) {
            resultadoPrevia.setText("Selecione a data de referência.");
            return;
        }
        try {
            List<EventoEspecial> eventos = dao.listarTodos().stream()
                    .map(EventoEspecialRegistrado::evento)
                    .toList();
            ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(referencia, eventos);
            escolhaConflito.getItems().setAll(resultado.alternativas());
            if (!resultado.exigeDecisaoDoUsuario()) {
                escolhaConflito.setValue(null);
            }

            StringBuilder texto = new StringBuilder();
            texto.append("Semana: ")
                    .append(DATA.format(resultado.dataReuniaoOriginal()))
                    .append("\n")
                    .append(resultado.mensagem());

            if (resultado.exigeDecisaoDoUsuario()) {
                texto.append("\n\nConflito: nenhuma alternativa será aplicada automaticamente.");
                AcaoResolucaoEvento escolha = escolhaConflito.getValue();
                if (escolha != null && resultado.alternativas().contains(escolha)) {
                    texto.append("\nAlternativa selecionada para simulação: ")
                            .append(rotulo(escolha))
                            .append("\nEsta seleção é apenas uma simulação; não altera o banco.");
                }
            } else if (resultado.possuiAlteracaoProposta()) {
                texto.append("\n\nProposta: ").append(rotulo(resultado.acao()));
                if (resultado.dataReuniaoProposta() != null) {
                    texto.append("\nData proposta: ")
                            .append(DATA.format(resultado.dataReuniaoProposta()));
                }
                if (resultado.temaDiscurso() != null) {
                    texto.append("\nTema do discurso: ").append(resultado.temaDiscurso());
                }
            } else {
                texto.append("\n\nA programação permanece como está.");
            }
            resultadoPrevia.setText(texto.toString());
        } catch (RuntimeException e) {
            mostrarErro("Não foi possível calcular a prévia.", e);
        }
    }

    private void atualizarCamposTipo() {
        boolean visita = tipo.getValue() == TipoEventoEspecial.VISITA_SUPERINTENDENTE;
        tema.setDisable(!visita);
        if (!visita) {
            tema.clear();
        }
    }

    private void limparFormulario() {
        idEdicao = -1;
        tipo.setValue(null);
        dataInicio.setValue(LocalDate.now());
        dataFim.setValue(LocalDate.now());
        tema.clear();
        descricao.clear();
        salvar.setText("Cadastrar evento");
        atualizarCamposTipo();
    }

    private void atualizarStatus(String mensagem) {
        status.setText(mensagem == null ? "" : mensagem);
    }

    private void mostrarErro(String mensagem, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Eventos especiais");
        alerta.setHeaderText(mensagem);
        alerta.setContentText(e.getMessage() == null ? "Erro inesperado." : e.getMessage());
        alerta.showAndWait();
    }

    private static String rotulo(TipoEventoEspecial tipo) {
        return switch (tipo) {
            case VISITA_SUPERINTENDENTE -> "Visita do superintendente";
            case ASSEMBLEIA -> "Assembleia";
            case CONGRESSO -> "Congresso";
            case CELEBRACAO -> "Celebração / Memorial";
            case CANCELAMENTO -> "Cancelamento";
        };
    }

    private static String rotulo(AcaoResolucaoEvento acao) {
        return switch (acao) {
            case MANTER_PROGRAMACAO -> "Manter programação";
            case MOVER_REUNIAO_PARA_TERCA -> "Mover reunião para terça-feira";
            case CANCELAR_REUNIAO -> "Marcar reunião como cancelada";
            case SUBSTITUIR_POR_CELEBRACAO -> "Substituir pela celebração";
            case EXIGE_DECISAO -> "Exige decisão";
        };
    }
}
