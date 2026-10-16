package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import br.com.geradordesignacoes.service.ExportadorS140WordService;
import br.com.geradordesignacoes.service.ExportadorS89Service;
import br.com.geradordesignacoes.view.formularios.GeracaoFormulariosView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Carrega novamente os dados persistidos ao atualizar e ao gerar o S-140.
 * Não reutiliza o resultado de geração que permanece em memória na tela semanal.
 */
public class GeracaoFormulariosController {

    private static final DateTimeFormatter DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Preferences PREFERENCIAS =
            Preferences.userNodeForPackage(GeracaoFormulariosController.class)
                    .node("exportacao-s140");
    private static final String CHAVE_MODELO = "modelo-word";
    private static final String CHAVE_PASTA_DESTINO = "pasta-destino";
    private static final String CHAVE_PASTA_S89 = "pasta-saida-s89";

    private final GeracaoFormulariosView view;
    private final EscalaDAO escalaDAO;
    private final ProgramacaoSemanaDAO programacaoDAO;
    private final ConfiguracaoGeralService configuracaoService;
    private final ExportadorS140WordService exportador;
    private final ExportadorS89Service exportadorS89;

    public GeracaoFormulariosController(GeracaoFormulariosView view) {
        this(
                view,
                new EscalaDAO(),
                new ProgramacaoSemanaDAO(),
                new ConfiguracaoGeralService(),
                new ExportadorS140WordService(),
                new ExportadorS89Service()
        );
    }

    GeracaoFormulariosController(
            GeracaoFormulariosView view,
            EscalaDAO escalaDAO,
            ProgramacaoSemanaDAO programacaoDAO,
            ConfiguracaoGeralService configuracaoService,
            ExportadorS140WordService exportador
    ) {
        this(view, escalaDAO, programacaoDAO, configuracaoService, exportador,
                new ExportadorS89Service());
    }

    GeracaoFormulariosController(
            GeracaoFormulariosView view,
            EscalaDAO escalaDAO,
            ProgramacaoSemanaDAO programacaoDAO,
            ConfiguracaoGeralService configuracaoService,
            ExportadorS140WordService exportador,
            ExportadorS89Service exportadorS89
    ) {
        this.view = view;
        this.escalaDAO = escalaDAO;
        this.programacaoDAO = programacaoDAO;
        this.configuracaoService = configuracaoService;
        this.exportador = exportador;
        this.exportadorS89 = exportadorS89;

        view.getCampoMes().setOnAction(_ -> atualizarDadosSalvos());
        view.getBotaoAtualizar().setOnAction(_ -> atualizarDadosSalvos());
        view.getBotaoExportarS140().setOnAction(_ -> exportarS140());
        view.getBotaoExportarS89().setOnAction(_ -> exportarS89());
        view.getBotaoPastaS89().setOnAction(_ -> escolherPastaS89());
        view.atualizarPastaS89(pastaS89Configurada());
        atualizarDadosSalvos();
    }

    private boolean atualizarDadosSalvos() {
        LocalDate selecionada = view.getCampoMes().getValue();
        if (selecionada == null) {
            view.atualizarDados(List.of());
            view.atualizarDatasS89(List.of());
            view.atualizarStatus("Selecione uma data do mês.");
            return false;
        }

        try {
            YearMonth mes = YearMonth.from(selecionada);
            ConfiguracaoGeral configuracao = configuracaoService.buscar();
            List<LocalDate> datas = exportador.datasReunioes(
                    mes,
                    configuracao.diaReuniaoHabitual()
            );
            List<String> linhas = new ArrayList<>();
            List<LocalDate> datasS89 = new ArrayList<>();

            for (LocalDate data : datas) {
                Escala escala = escalaDAO.buscarPorData(data).orElse(null);
                ProgramacaoSemana programacao = programacaoDAO.buscarPorData(data);
                if (escala != null && programacao != null) {
                    datasS89.add(data);
                }

                StringBuilder linha = new StringBuilder(data.format(DATA));
                if (programacao == null) {
                    linha.append(" — programação não salva");
                } else {
                    linha.append(" — programação salva (")
                            .append(programacao.partes().size())
                            .append(" partes)");
                }

                if (escala == null) {
                    linha.append(" — escala não salva");
                    linhas.add(linha.toString());
                } else {
                    linha.append(" — escala salva (")
                            .append(escala.getDesignacoes().size())
                            .append(" designações)");
                    linhas.add(linha.toString());
                    linhas.add("   Presidente: " + nome(escala.getPresidente()));
                    linhas.add("   Oração inicial: " + nome(escala.getOracaoInicial()));
                    for (br.com.geradordesignacoes.model.Designacao designacao
                            : escala.getDesignacoes()) {
                        linhas.add("   • " + designacao.parte().getNome()
                                + " [" + designacao.sala() + "] — responsável: "
                                + nome(designacao.responsavel())
                                + " — ajudante/leitor: "
                                + nome(designacao.ajudante()));
                    }
                }
            }

            view.atualizarDados(linhas);
            view.atualizarDatasS89(datasS89);
            view.atualizarStatus(
                    "Consulta concluída: " + datas.size()
                            + " reunião(ões) previstas. S-89 disponível para "
                            + datasS89.size() + " reunião(ões) com programação e escala salvas."
            );
            return true;
        } catch (Exception e) {
            view.atualizarDados(List.of());
            view.atualizarDatasS89(List.of());
            view.atualizarStatus(
                    "Não foi possível carregar os dados salvos: " + e.getMessage()
            );
            return false;
        }
    }


    private void exportarS89() {
        LocalDate data = view.getCampoSemanaS89().getValue();
        if (data == null) {
            view.atualizarStatus(
                    "Selecione uma reunião com programação e escala salvas para gerar o S-89."
            );
            return;
        }

        try {
            Path raizSaida = obterPastaS89();
            if (raizSaida == null) {
                return;
            }
            List<Path> arquivos = exportadorS89.exportarSemana(data, raizSaida);
            if (arquivos.isEmpty()) {
                view.atualizarStatus("O S-89 não gerou arquivos para " + data.format(DATA) + ".");
                return;
            }
            view.atualizarStatus("S-89 gerado para " + data.format(DATA) + ": "
                    + arquivos.size() + " arquivo(s) em " + arquivos.getFirst().getParent());
        } catch (Exception e) {
            view.atualizarStatus("Não foi possível gerar o S-89: " + e.getMessage());
        }
    }


    private Path obterPastaS89() {
        String caminho = PREFERENCIAS.get(CHAVE_PASTA_S89, "");
        if (!caminho.isBlank()) {
            Path pasta = Path.of(caminho);
            if (java.nio.file.Files.isDirectory(pasta)) {
                return pasta;
            }
        }
        return escolherPastaS89();
    }

    private Path escolherPastaS89() {
        Window janela = view.getView().getScene() == null
                ? null : view.getView().getScene().getWindow();
        DirectoryChooser seletor = new DirectoryChooser();
        seletor.setTitle("Escolha a pasta base para os arquivos S-89");

        String caminhoAtual = PREFERENCIAS.get(CHAVE_PASTA_S89, "");
        File pastaAtual = caminhoAtual.isBlank() ? null : new File(caminhoAtual);
        if (pastaAtual != null && pastaAtual.isDirectory()) {
            seletor.setInitialDirectory(pastaAtual);
        } else {
            definirPastaInicial(seletor);
        }

        File escolhida = seletor.showDialog(janela);
        if (escolhida == null) {
            view.atualizarStatus("Seleção da pasta S-89 cancelada.");
            return null;
        }

        String caminho = escolhida.getAbsolutePath();
        PREFERENCIAS.put(CHAVE_PASTA_S89, caminho);
        view.atualizarPastaS89(caminho);
        view.atualizarStatus("Pasta de saída do S-89 salva: " + caminho);
        return escolhida.toPath();
    }

    private static void definirPastaInicial(DirectoryChooser seletor) {
        String caminho = PREFERENCIAS.get(CHAVE_PASTA_DESTINO, "");
        File pasta = caminho.isBlank() ? null : new File(caminho);
        if (pasta != null && pasta.isDirectory()) {
            seletor.setInitialDirectory(pasta);
        } else {
            File home = new File(System.getProperty("user.home"));
            if (home.isDirectory()) {
                seletor.setInitialDirectory(home);
            }
        }
    }

    private static String pastaS89Configurada() {
        String caminho = PREFERENCIAS.get(CHAVE_PASTA_S89, "");
        return caminho.isBlank() || !new File(caminho).isDirectory()
                ? "" : caminho;
    }

    private void exportarS140() {
        if (!atualizarDadosSalvos()) {
            return;
        }

        LocalDate selecionada = view.getCampoMes().getValue();
        if (selecionada == null) {
            view.atualizarStatus("Selecione o mês que deseja exportar.");
            return;
        }

        Window janela = view.getView().getScene() == null
                ? null : view.getView().getScene().getWindow();

        FileChooser seletorModelo = new FileChooser();
        seletorModelo.setTitle("Selecione o modelo oficial S-140 em Word");
        seletorModelo.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento Word (*.docx)", "*.docx")
        );
        File modeloSalvo = arquivoExistente(CHAVE_MODELO);
        if (modeloSalvo != null) {
            seletorModelo.setInitialDirectory(modeloSalvo.getParentFile());
            seletorModelo.setInitialFileName(modeloSalvo.getName());
        } else {
            definirPastaInicial(seletorModelo, CHAVE_PASTA_DESTINO);
        }

        File modelo = seletorModelo.showOpenDialog(janela);
        if (modelo == null) {
            view.atualizarStatus("Geração cancelada: modelo não selecionado.");
            return;
        }
        PREFERENCIAS.put(CHAVE_MODELO, modelo.getAbsolutePath());

        FileChooser seletorDestino = new FileChooser();
        seletorDestino.setTitle("Salvar programação mensal S-140");
        seletorDestino.setInitialFileName(
                ProgramacaoController.nomeArquivoS140(YearMonth.from(selecionada))
        );
        seletorDestino.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento Word (*.docx)", "*.docx")
        );
        definirPastaInicial(seletorDestino, CHAVE_PASTA_DESTINO);

        File destino = seletorDestino.showSaveDialog(janela);
        if (destino == null) {
            view.atualizarStatus("Geração do S-140 cancelada.");
            return;
        }

        try {
            Path arquivo = exportador.exportar(
                    YearMonth.from(selecionada),
                    modelo.toPath(),
                    destino.toPath()
            );
            File pastaDestino = destino.getAbsoluteFile().getParentFile();
            if (pastaDestino != null) {
                PREFERENCIAS.put(CHAVE_PASTA_DESTINO, pastaDestino.getAbsolutePath());
            }
            view.atualizarStatus("S-140 gerado com dados persistidos: " + arquivo);
        } catch (Exception e) {
            view.atualizarStatus(
                    "Não foi possível gerar o S-140: " + e.getMessage()
            );
        }
    }

    private static String nome(br.com.geradordesignacoes.model.Pessoa pessoa) {
        return pessoa == null ? "não definido" : pessoa.getNome();
    }

    private static File arquivoExistente(String chave) {
        String caminho = PREFERENCIAS.get(chave, "");
        if (caminho.isBlank()) {
            return null;
        }
        File arquivo = new File(caminho);
        return arquivo.isFile() ? arquivo : null;
    }

    private static void definirPastaInicial(FileChooser seletor, String chave) {
        String caminho = PREFERENCIAS.get(chave, "");
        File pasta = caminho.isBlank() ? null : new File(caminho);
        if (pasta != null && pasta.isDirectory()) {
            seletor.setInitialDirectory(pasta);
        }
    }
}
