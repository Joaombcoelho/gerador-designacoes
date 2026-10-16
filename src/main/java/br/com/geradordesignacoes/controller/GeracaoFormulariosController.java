package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import br.com.geradordesignacoes.service.ExportadorS140WordService;
import br.com.geradordesignacoes.view.formularios.GeracaoFormulariosView;
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

    private final GeracaoFormulariosView view;
    private final EscalaDAO escalaDAO;
    private final ProgramacaoSemanaDAO programacaoDAO;
    private final ConfiguracaoGeralService configuracaoService;
    private final ExportadorS140WordService exportador;

    public GeracaoFormulariosController(GeracaoFormulariosView view) {
        this(
                view,
                new EscalaDAO(),
                new ProgramacaoSemanaDAO(),
                new ConfiguracaoGeralService(),
                new ExportadorS140WordService()
        );
    }

    GeracaoFormulariosController(
            GeracaoFormulariosView view,
            EscalaDAO escalaDAO,
            ProgramacaoSemanaDAO programacaoDAO,
            ConfiguracaoGeralService configuracaoService,
            ExportadorS140WordService exportador
    ) {
        this.view = view;
        this.escalaDAO = escalaDAO;
        this.programacaoDAO = programacaoDAO;
        this.configuracaoService = configuracaoService;
        this.exportador = exportador;

        view.getBotaoAtualizar().setOnAction(_ -> atualizarDadosSalvos());
        view.getBotaoExportarS140().setOnAction(_ -> exportarS140());
        atualizarDadosSalvos();
    }

    private boolean atualizarDadosSalvos() {
        LocalDate selecionada = view.getCampoMes().getValue();
        if (selecionada == null) {
            view.atualizarDados(List.of());
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

            for (LocalDate data : datas) {
                Escala escala = escalaDAO.buscarPorData(data).orElse(null);
                ProgramacaoSemana programacao = programacaoDAO.buscarPorData(data);

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
            view.atualizarStatus(
                    "Consulta concluída: " + linhas.size()
                            + " reunião(ões). A geração consulta novamente o banco."
            );
            return true;
        } catch (Exception e) {
            view.atualizarDados(List.of());
            view.atualizarStatus(
                    "Não foi possível carregar os dados salvos: " + e.getMessage()
            );
            return false;
        }
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
