package br.com.geradordesignacoes.view.importacao;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.service.importacao.ImportacaoApostilaJsonService;
import br.com.geradordesignacoes.service.importacao.MapeadorParteApostilaService;
import br.com.geradordesignacoes.service.importacao.PreviaImportacaoApostilaService;
import br.com.geradordesignacoes.service.importacao.RevisaoMapeamentoApostilaService;
import br.com.geradordesignacoes.service.importacao.ExtratorApostilaRtfService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Interface de conferência da apostila. Esta tela não grava dados. */
public class ImportacaoApostilaView {

    private final VBox root = new VBox(16);
    private final VBox semanasContainer = new VBox(14);
    private final VBox resultadoContainer = new VBox(8);
    private final ParteDAO parteDAO = new ParteDAO();
    private final ProgramacaoSemanaDAO programacaoDAO = new ProgramacaoSemanaDAO();
    private final ImportacaoApostilaJsonService validador = new ImportacaoApostilaJsonService();
    private final MapeadorParteApostilaService mapeador = new MapeadorParteApostilaService();
    private final PreviaImportacaoApostilaService previaService = new PreviaImportacaoApostilaService();
    private final RevisaoMapeamentoApostilaService revisaoService = new RevisaoMapeamentoApostilaService();
    private final Label arquivoLabel = new Label("Nenhum arquivo selecionado.");
    private final Button conferirButton = new Button("Conferir prévia");
    private final Button salvarCopiaButton = new Button("Salvar cópia do JSON…");
    private final ExtratorApostilaRtfService extrator = new ExtratorApostilaRtfService();
    private Path jsonGeradoTemporario;
    private Path arquivoSelecionado;
    private ImportacaoApostilaJsonService.ResultadoValidacao validacao;
    private List<Parte> catalogo = List.of();
    private final List<SemanaUi> semanasUi = new ArrayList<>();

    public ImportacaoApostilaView() {
        root.getStyleClass().add("content-area");
        root.setPadding(new Insets(24));

        Label titulo = new Label("Importação de apostila — prévia");
        titulo.getStyleClass().add("page-title");
        Label instrucao = new Label(
                "Selecione o JSON gerado pelo extrator. Revise as datas reais das reuniões e os mapeamentos. "
                        + "Esta tela é somente de conferência: não importa nem altera programações."
        );
        instrucao.setWrapText(true);
        instrucao.getStyleClass().add("page-subtitle");

        Button extrair = new Button("Extrair apostila de arquivos RTF…");
        extrair.setOnAction(_ -> selecionarPastaEExtrair());

        Button selecionar = new Button("Abrir JSON existente…");
        selecionar.setOnAction(_ -> selecionarArquivo());

        salvarCopiaButton.setDisable(true);
        salvarCopiaButton.setOnAction(_ -> salvarCopiaJson());

        HBox arquivoLinha = new HBox(12, extrair, selecionar, salvarCopiaButton);
        arquivoLinha.setAlignment(Pos.CENTER_LEFT);
        arquivoLabel.setWrapText(true);

        conferirButton.setDisable(true);
        conferirButton.setOnAction(_ -> conferirPrevia());

        // A página inteira precisa rolar: os resultados da prévia podem conter
        // vários cards e não devem comprimir a área de correção dos mapeamentos.
        VBox conteudo = new VBox(16, titulo, instrucao, arquivoLinha, arquivoLabel,
                semanasContainer, conferirButton, resultadoContainer);
        conteudo.setPadding(new Insets(0));
        ScrollPane scrollTela = new ScrollPane(conteudo);
        scrollTela.setFitToWidth(true);
        scrollTela.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollTela.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollTela, Priority.ALWAYS);
        root.getChildren().add(scrollTela);
    }

    public Parent getView() {
        return root;
    }

    private void selecionarArquivo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecionar JSON da apostila");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON", "*.json")
        );
        File arquivo = chooser.showOpenDialog(root.getScene() == null
                ? null : root.getScene().getWindow());
        if (arquivo == null) {
            return;
        }
        jsonGeradoTemporario = null;
        salvarCopiaButton.setDisable(true);
        carregarArquivo(arquivo.toPath(), arquivo.getName());
    }

    private void selecionarPastaEExtrair() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecione a pasta com os arquivos RTF da apostila");
        File pasta = chooser.showDialog(root.getScene() == null
                ? null : root.getScene().getWindow());
        if (pasta == null) {
            return;
        }

        salvarCopiaButton.setDisable(true);
        try {
            var resultado = extrator.extrair(pasta.toPath());
            Path temporario = Files.createTempFile("gerador-apostila-", ".json");
            Files.writeString(temporario, resultado.json(), StandardCharsets.UTF_8);
            jsonGeradoTemporario = temporario;

            carregarArquivo(temporario, "JSON extraído: " + pasta.getName()
                    + " (" + resultado.quantidadeArquivos() + " arquivo(s), "
                    + resultado.quantidadeSemanas() + " semana(s))");

            Alert resumo = new Alert(resultado.alertas().isEmpty()
                    ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            resumo.setTitle("Extração concluída");
            resumo.setHeaderText("JSON provisório gerado");
            resumo.setContentText("Arquivos RTF: " + resultado.quantidadeArquivos()
                    + "\nSemanas identificadas: " + resultado.quantidadeSemanas()
                    + "\nAlertas para conferência: " + resultado.alertas().size()
                    + "\n\nO JSON foi carregado na tela. Nenhuma programação foi alterada.");
            resumo.showAndWait();
            salvarCopiaButton.setDisable(false);
        } catch (IOException | RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Não foi possível extrair a apostila", e.getMessage());
        }
    }

    private void salvarCopiaJson() {
        if (jsonGeradoTemporario == null || !Files.isRegularFile(jsonGeradoTemporario)) {
            mostrarAlerta(Alert.AlertType.WARNING, "JSON indisponível",
                    "Gere um JSON pela extração integrada antes de salvar uma cópia.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Salvar cópia do JSON extraído");
        chooser.setInitialFileName("apostila-extraida.json");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Arquivo JSON", "*.json")
        );
        File destino = chooser.showSaveDialog(root.getScene() == null
                ? null : root.getScene().getWindow());
        if (destino == null) {
            return;
        }

        Path caminho = destino.toPath();
        if (!caminho.getFileName().toString().toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            caminho = caminho.resolveSibling(caminho.getFileName() + ".json");
        }
        try {
            Files.copy(jsonGeradoTemporario, caminho);
            Alert sucesso = new Alert(Alert.AlertType.INFORMATION);
            sucesso.setTitle("Cópia salva");
            sucesso.setHeaderText("JSON salvo com sucesso");
            sucesso.setContentText(caminho.toAbsolutePath().toString());
            sucesso.showAndWait();
        } catch (java.nio.file.FileAlreadyExistsException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "O arquivo já existe",
                    "Para evitar sobrescrever um arquivo existente, escolha outro nome ou local.");
        } catch (IOException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Não foi possível salvar a cópia do JSON", e.getMessage());
        }
    }

    private void carregarArquivo(Path caminho, String descricaoArquivo) {
        try {
            var resultado = validador.validar(caminho);
            arquivoSelecionado = caminho;
            validacao = resultado;
            catalogo = parteDAO.listarTodos();
            arquivoLabel.setText(descricaoArquivo);
            montarSemanas();
            resultadoContainer.getChildren().clear();
            exibirMensagens("Validação do JSON", resultado.erros(), resultado.avisos());
            conferirButton.setDisable(!resultado.validoParaPrevia());
        } catch (IOException | RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Não foi possível ler o JSON", e.getMessage());
        }
    }

    private void montarSemanas() {
        semanasContainer.getChildren().clear();
        semanasUi.clear();
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (var semana : validacao.semanas()) {
            Label cabecalho = new Label("Semana da apostila: "
                    + semana.dataInicio().format(formato) + " a "
                    + semana.dataFim().format(formato));
            cabecalho.getStyleClass().add("card-title");
            Label leitura = new Label("Leitura bíblica: "
                    + (semana.leituraBiblica() == null ? "não identificada" : semana.leituraBiblica()));
            Label canticos = new Label("Cânticos: "
                    + valor(semana.canticos().abertura()) + " / "
                    + valor(semana.canticos().intermediario()) + " / "
                    + valor(semana.canticos().encerramento()));

            DatePicker dataReuniao = new DatePicker();
            dataReuniao.setPromptText("Data real da reunião");
            HBox dataLinha = new HBox(10, new Label("Data efetiva da reunião:"), dataReuniao);
            dataLinha.setAlignment(Pos.CENTER_LEFT);

            VBox cartao = new VBox(8, cabecalho, leitura, canticos, dataLinha);
            cartao.getStyleClass().add("card");
            cartao.setPadding(new Insets(14));
            List<ParteUi> partesUi = new ArrayList<>();

            for (var parteJson : semana.partes()) {
                var sugestao = mapeador.mapear(parteJson, catalogo);
                Label descricao = new Label(parteJson.numero() + ". " + parteJson.tema()
                        + " (" + parteJson.duracaoMinutos() + " min) — "
                        + (parteJson.subtitulo() == null ? "tipo não identificado" : parteJson.subtitulo()));
                descricao.setWrapText(true);

                ComboBox<Parte> escolha = new ComboBox<>();
                escolha.getItems().setAll(candidatosCompativeis(sugestao));
                escolha.setPromptText("Escolher parte do catálogo");
                escolha.setMaxWidth(Double.MAX_VALUE);
                escolha.setCellFactory(celulaParte());
                escolha.setButtonCell(criarCelulaParte());
                Label estado = new Label(rotulo(sugestao.situacao()));
                HBox linha = new HBox(10, descricao, escolha, estado);
                linha.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(descricao, Priority.ALWAYS);
                HBox.setHgrow(escolha, Priority.ALWAYS);
                cartao.getChildren().add(linha);

                escolha.valueProperty().addListener((obs, anterior, selecionada) -> {
                    if (selecionada == null) {
                        estado.setText("Pendente de conferência");
                        return;
                    }
                    try {
                        var revisado = revisaoService.aplicarSelecaoManual(sugestao, selecionada);
                        estado.setText("Conferido: " + revisado.candidatos().getFirst().getNome());
                    } catch (IllegalArgumentException e) {
                        escolha.getSelectionModel().clearSelection();
                        estado.setText(e.getMessage());
                    }
                });
                if (sugestao.possuiCorrespondenciaUnica()) {
                    escolha.getSelectionModel().select(sugestao.candidatos().getFirst());
                }
                partesUi.add(new ParteUi(parteJson.numero(), escolha));
            }

            semanasUi.add(new SemanaUi(semana.dataInicio(), dataReuniao, partesUi));
            semanasContainer.getChildren().add(cartao);
        }
    }

    private List<Parte> candidatosCompativeis(
            MapeadorParteApostilaService.ResultadoMapeamento resultado
    ) {
        if (!resultado.candidatos().isEmpty()) {
            return resultado.candidatos();
        }
        return catalogo.stream()
                .filter(p -> p.getSecao() == resultado.secao())
                .filter(p -> resultado.tipoInferido() == null || p.getTipo() == resultado.tipoInferido())
                .toList();
    }

    private Callback<ListView<Parte>, ListCell<Parte>> celulaParte() {
        return lista -> criarCelulaParte();
    }

    private ListCell<Parte> criarCelulaParte() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Parte parte, boolean empty) {
                super.updateItem(parte, empty);
                setText(empty || parte == null
                        ? null
                        : parte.getNome() + " (ID " + parte.getId() + ")");
            }
        };
    }

    private void conferirPrevia() {
        if (arquivoSelecionado == null || validacao == null) {
            return;
        }

        Map<LocalDate, LocalDate> datas = new HashMap<>();
        Map<LocalDate, Map<Integer, Parte>> escolhasManuais = new HashMap<>();
        Set<LocalDate> existentes = new HashSet<>();
        List<String> errosTela = new ArrayList<>();

        for (SemanaUi semana : semanasUi) {
            Map<Integer, Parte> escolhasDaSemana = new HashMap<>();
            for (ParteUi parteUi : semana.partes()) {
                Parte selecionada = parteUi.escolha().getValue();
                if (selecionada != null) {
                    escolhasDaSemana.put(parteUi.numero(), selecionada);
                }
            }
            escolhasManuais.put(semana.dataInicio(), escolhasDaSemana);
            LocalDate data = semana.dataReuniao().getValue();
            if (data == null) {
                errosTela.add("Informe a data efetiva da reunião para a semana iniciada em "
                        + semana.dataInicio() + ".");
                continue;
            }
            datas.put(semana.dataInicio(), data);
            try {
                if (programacaoDAO.buscarPorData(data) != null) {
                    existentes.add(data);
                }
            } catch (RuntimeException e) {
                errosTela.add("Não foi possível verificar a programação de " + data + ": " + e.getMessage());
            }
        }

        if (!errosTela.isEmpty()) {
            resultadoContainer.getChildren().clear();
            exibirMensagens("Pendências", errosTela, List.of());
            return;
        }

        try {
            var previa = previaService.preparar(
                    arquivoSelecionado, datas, catalogo, existentes, escolhasManuais);
            resultadoContainer.getChildren().clear();
            exibirMensagens("Resultado da prévia", previa.erros(), previa.avisos());

            DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (var semanaPrevia : previa.semanas()) {
                VBox painelSemana = new VBox(5);
                painelSemana.getStyleClass().add("card");
                painelSemana.setPadding(new Insets(12));
                Label tituloSemana = new Label("Semana " + semanaPrevia.dataInicioApostila().format(formato)
                        + " — reunião em " + semanaPrevia.dataReuniao().format(formato));
                tituloSemana.getStyleClass().add("card-title");
                painelSemana.getChildren().addAll(tituloSemana,
                        new Label("Leitura bíblica: " + (semanaPrevia.leituraBiblica() == null
                                ? "não identificada" : semanaPrevia.leituraBiblica())),
                        new Label("Cânticos: " + valor(semanaPrevia.canticos().abertura()) + " / "
                                + valor(semanaPrevia.canticos().intermediario()) + " / "
                                + valor(semanaPrevia.canticos().encerramento())));
                if (semanaPrevia.conflitoProgramacaoExistente()) {
                    Label conflito = new Label("BLOQUEIO: já existe programação nesta data.");
                    conflito.setWrapText(true);
                    painelSemana.getChildren().add(conflito);
                }
                for (var mapeamento : semanaPrevia.partes()) {
                    String destino = mapeamento.possuiCorrespondenciaUnica()
                            ? mapeamento.candidatos().getFirst().getNome()
                                    + " (ID " + mapeamento.candidatos().getFirst().getId() + ")"
                            : "PENDENTE — " + rotulo(mapeamento.situacao());
                    Label linhaParte = new Label(mapeamento.numeroParteApostila() + ". "
                            + mapeamento.tema() + " → " + destino);
                    linhaParte.setWrapText(true);
                    painelSemana.getChildren().add(linhaParte);
                }
                resultadoContainer.getChildren().add(painelSemana);
            }

            Label resumo = new Label(
                    "Prévia técnica concluída usando as escolhas atuais. "
                            + (previa.podeProsseguirParaConfirmacao()
                            ? "O mapeamento está completo, mas a confirmação/gravação ainda não está implementada."
                            : "Existem pendências ou conflitos que precisam ser corrigidos. ")
                            + "Nenhuma alteração foi gravada no banco."
            );
            resumo.setWrapText(true);
            resumo.getStyleClass().add("card-title");
            resultadoContainer.getChildren().add(resumo);
        } catch (IOException | RuntimeException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Falha ao preparar prévia", e.getMessage());
        }
    }

    private void exibirMensagens(String titulo, List<String> erros, List<String> avisos) {
        VBox painel = new VBox(6);
        painel.getStyleClass().add("card");
        painel.setPadding(new Insets(12));
        Label cabecalho = new Label(titulo);
        cabecalho.getStyleClass().add("card-title");
        painel.getChildren().add(cabecalho);

        if (erros.isEmpty() && avisos.isEmpty()) {
            painel.getChildren().add(new Label("Nenhum erro ou aviso encontrado."));
        }
        for (String erro : erros) {
            Label linha = new Label("ERRO: " + erro);
            linha.setWrapText(true);
            painel.getChildren().add(linha);
        }
        for (String aviso : avisos) {
            Label linha = new Label("Aviso: " + aviso);
            linha.setWrapText(true);
            painel.getChildren().add(linha);
        }
        resultadoContainer.getChildren().add(painel);
    }

    private static String valor(Integer numero) {
        return numero == null ? "—" : numero.toString();
    }

    private static String rotulo(MapeadorParteApostilaService.SituacaoMapeamento situacao) {
        return switch (situacao) {
            case CORRESPONDENCIA_UNICA -> "Correspondência única";
            case AMBIGUO -> "Ambíguo";
            case NAO_ENCONTRADO -> "Não encontrado";
            case REQUER_CONFERENCIA -> "Requer conferência";
        };
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensagem == null ? "Erro não especificado." : mensagem);
        alerta.showAndWait();
    }

    private record SemanaUi(
            LocalDate dataInicio,
            DatePicker dataReuniao,
            List<ParteUi> partes
    ) {
        private SemanaUi {
            partes = List.copyOf(partes);
        }
    }

    private record ParteUi(int numero, ComboBox<Parte> escolha) {
    }
}
