package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Exporta as programações e escalas já salvas para o modelo Word oficial S-140.
 * A preparação é somente de leitura: nenhuma programação ou escala é criada/salva.
 */
public class ExportadorS140WordService {

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ConfiguracaoGeralService configuracaoService;
    private final CarregadorDadosExportacaoS140Service carregador;
    private final HorarioS140Service horarioService;
    private final PessoaDAO pessoaDAO = new PessoaDAO();

    public ExportadorS140WordService() {
        this(new ConfiguracaoGeralService(),
                new CarregadorDadosExportacaoS140Service(),
                new HorarioS140Service());
    }

    public ExportadorS140WordService(
            ConfiguracaoGeralService configuracaoService,
            CarregadorDadosExportacaoS140Service carregador,
            HorarioS140Service horarioService
    ) {
        this.configuracaoService = Objects.requireNonNull(configuracaoService);
        this.carregador = Objects.requireNonNull(carregador);
        this.horarioService = Objects.requireNonNull(horarioService);
    }

    public List<LocalDate> datasReunioes(YearMonth mes, DayOfWeek dia) {
        Objects.requireNonNull(mes, "Selecione o mês para exportação.");
        Objects.requireNonNull(dia, "O dia habitual da reunião é obrigatório.");
        List<LocalDate> datas = new ArrayList<>();
        for (LocalDate data = mes.atDay(1); data.getMonth() == mes.getMonth(); data = data.plusDays(1)) {
            if (data.getDayOfWeek() == dia) {
                datas.add(data);
            }
        }
        return List.copyOf(datas);
    }

    public Path exportar(YearMonth mes, Path modelo, Path destino) throws IOException {
        if (mes == null) {
            throw new IllegalArgumentException("Selecione o mês que deseja exportar.");
        }
        validarArquivo(modelo, "Selecione o modelo oficial S-140 em Word.");
        if (destino == null || !destino.getFileName().toString().toLowerCase().endsWith(".docx")) {
            throw new IllegalArgumentException("Escolha um destino com extensão .docx.");
        }

        ConfiguracaoGeral configuracao = configuracaoService.buscar();
        List<LocalDate> datas = datasReunioes(mes, configuracao.diaReuniaoHabitual());
        if (datas.isEmpty() || datas.size() > 5) {
            throw new IllegalArgumentException("O mês selecionado não possui uma quantidade válida de reuniões.");
        }

        List<DadosExportacaoS140Semana> semanas = new ArrayList<>();
        List<String> pendencias = new ArrayList<>();
        for (LocalDate data : datas) {
            try {
                semanas.add(carregador.carregar(data));
            } catch (RuntimeException e) {
                pendencias.add(data.format(DATA) + ": " + e.getMessage());
            }
        }
        if (!pendencias.isEmpty()) {
            throw new IllegalArgumentException("Não foi possível exportar o S-140. Corrija estas semanas antes de gerar:\n- "
                    + String.join("\n- ", pendencias));
        }

        Path pastaDestino = destino.toAbsolutePath().getParent();
        if (pastaDestino != null) {
            Files.createDirectories(pastaDestino);
        }
        try (InputStream entrada = Files.newInputStream(modelo);
             XWPFDocument documento = new XWPFDocument(entrada)) {
            if (documento.getTables().isEmpty()) {
                throw new IllegalArgumentException("O modelo Word selecionado não contém a tabela esperada do S-140.");
            }
            XWPFTable tabela = documento.getTables().getFirst();
            List<Integer> linhasData = encontrarLinhasData(tabela);
            if (linhasData.size() < semanas.size()) {
                throw new IllegalArgumentException("O modelo S-140 não contém blocos suficientes para todas as reuniões do mês.");
            }

            substituirEmTodoDocumento(documento, "[NOME DA CONGREGAÇÃO]", configuracao.nomeCongregacao());
            for (int i = 0; i < semanas.size(); i++) {
                preencherSemana(tabela, linhasData.get(i), semanas.get(i));
            }
            // Blocos restantes ficam vazios para não exibir informações de outro mês.
            for (int i = semanas.size(); i < linhasData.size(); i++) {
                limparBloco(tabela, linhasData.get(i));
            }
            try (OutputStream saida = Files.newOutputStream(destino)) {
                documento.write(saida);
            }
        }
        return destino;
    }

    private void preencherSemana(XWPFTable tabela, int linhaData, DadosExportacaoS140Semana dados) {
        ProgramacaoSemana programacao = dados.programacao();
        Escala escala = dados.escala();
        XWPFTableRow cabecalho = tabela.getRow(linhaData);
        substituirMarcador(cabecalho, "[DATA]", dados.data().format(DATA));

        setPlaceholder(tabela.getRow(linhaData), "[Nome]", 0,
                nome(escala.getPresidente()));
        setPlaceholder(tabela.getRow(linhaData + 1), "[Nome]", 0,
                nomePorId(programacao.conselheiroSalaBId()));

        setTextoContendo(tabela.getRow(linhaData + 3), "Cântico [número]",
                rotuloCantico(programacao.canticoInicial()));
        setPlaceholder(tabela.getRow(linhaData + 3), "[Nome]", 0,
                nome(escala.getOracaoInicial()));
        setTextoContendo(tabela.getRow(linhaData + 4), "Comentários iniciais (1 min)",
                temaComDuracao("Comentários iniciais", configuracaoService.buscar().duracaoComentariosIniciaisMinutos()));

        preencherPartePorTipo(tabela, linhaData + 7, programacao, escala, TipoParte.DISCURSO_TESOUROS, SalaDesignacao.PRINCIPAL);
        preencherPartePorTipo(tabela, linhaData + 8, programacao, escala, TipoParte.JOIAS_ESPIRITUAIS, SalaDesignacao.PRINCIPAL);
        preencherPartePorTipo(tabela, linhaData + 9, programacao, escala, TipoParte.LEITURA, SalaDesignacao.PRINCIPAL);

        List<ProgramacaoParte> ministerio = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.MINISTERIO)
                .filter(p -> p.getParte().getTipo() != TipoParte.O_QUE_VOCE_DIRIA)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem)).toList();
        for (int i = 0; i < 4; i++) {
            int linha = linhaData + 12 + i;
            if (i < ministerio.size()) {
                ProgramacaoParte parte = ministerio.get(i);
                preencherTema(tabela.getRow(linha), parte);
                preencherNomesSala(tabela.getRow(linha), escala, parte.getParte().getId());
            } else {
                limparMarcadoresParte(tabela.getRow(linha));
            }
        }

        setTextoContendo(tabela.getRow(linhaData + 18), "Cântico [número]",
                rotuloCantico(programacao.canticoMeio()));

        List<ProgramacaoParte> vidaCrista = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.NOSSA_VIDA_CRISTA)
                .filter(p -> p.getParte().getTipo() != TipoParte.DIRIGENTE_ESTUDO)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem)).toList();
        for (int i = 0; i < 2; i++) {
            XWPFTableRow linha = tabela.getRow(linhaData + 19 + i);
            if (i < vidaCrista.size()) {
                preencherTema(linha, vidaCrista.get(i));
                Designacao d = designacao(escala, vidaCrista.get(i).getParte().getId(), SalaDesignacao.PRINCIPAL);
                setPlaceholder(linha, "[Nome]", 0, nome(d == null ? null : d.responsavel()));
            } else {
                limparMarcadoresParte(linha);
            }
        }

        ProgramacaoParte estudo = programacao.partes().stream()
                .filter(p -> p.getParte().getTipo() == TipoParte.DIRIGENTE_ESTUDO)
                .findFirst().orElse(null);
        Designacao designacaoEstudo = estudo == null ? null
                : designacao(escala, estudo.getParte().getId(), SalaDesignacao.PRINCIPAL);
        setPlaceholder(tabela.getRow(linhaData + 21), "[Nome/Nome]", 0,
                designacaoEstudo == null ? "" : nome(designacaoEstudo.responsavel()) + "/" + nome(designacaoEstudo.ajudante()));

        setTextoContendo(tabela.getRow(linhaData + 22), "Comentários finais (3 min)",
                temaComDuracao("Comentários finais", configuracaoService.buscar().duracaoComentariosFinaisMinutos()));
        setTextoContendo(tabela.getRow(linhaData + 23), "Cântico [número]",
                rotuloCantico(programacao.canticoFinal()));
        Designacao oracaoFinal = escala.getDesignacoes().stream()
                .filter(d -> d.parte().getTipo() == TipoParte.ORACAO_FINAL)
                .filter(d -> d.sala() == SalaDesignacao.PRINCIPAL)
                .findFirst().orElse(null);
        setPlaceholder(tabela.getRow(linhaData + 23), "[Nome]", 0,
                oracaoFinal == null ? "" : nome(oracaoFinal.responsavel()));

        preencherHorarios(tabela, linhaData, programacao);
    }

    private void preencherPartePorTipo(XWPFTable tabela, int linha, ProgramacaoSemana programacao,
                                       Escala escala, TipoParte tipo, SalaDesignacao sala) {
        ProgramacaoParte parte = programacao.partes().stream()
                .filter(p -> p.getParte().getTipo() == tipo)
                .findFirst().orElse(null);
        if (parte == null) {
            limparMarcadoresParte(tabela.getRow(linha));
            return;
        }
        preencherTema(tabela.getRow(linha), parte);
        Designacao designacao = designacao(escala, parte.getParte().getId(), sala);
        setPlaceholder(tabela.getRow(linha), "[Nome]", 0,
                designacao == null ? "" : nome(designacao.responsavel()));
    }

    private void preencherNomesSala(XWPFTableRow linha, Escala escala, Integer parteId) {
        List<XWPFTableCell> marcadores = celulasComMarcador(linha, "[Nome/Nome]");
        Designacao salaB = designacao(escala, parteId, SalaDesignacao.B);
        Designacao principal = designacao(escala, parteId, SalaDesignacao.PRINCIPAL);
        if (!marcadores.isEmpty()) {
            definirTexto(marcadores.get(0), salaB == null ? "" : nome(salaB.responsavel()) + "/" + nome(salaB.ajudante()));
        }
        if (marcadores.size() > 1) {
            definirTexto(marcadores.get(marcadores.size() - 1),
                    principal == null ? "" : nome(principal.responsavel()) + "/" + nome(principal.ajudante()));
        }
    }

    private void preencherTema(XWPFTableRow linha, ProgramacaoParte parte) {
        for (XWPFTableCell celula : linha.getTableCells()) {
            String texto = celula.getText();
            if (texto.contains("[Tema]")) {
                substituirNaCelula(celula, "[Tema]", textoSeguro(parte.getTema()));
                Integer duracao = parte.getDuracaoMinutos() != null
                        ? parte.getDuracaoMinutos() : parte.getParte().getDuracaoPadraoMinutos();
                substituirNaCelula(celula, "(X min)", duracao == null ? "(duração pendente)" : "(" + duracao + " min)");
                substituirNaCelula(celula, "(XX min)", duracao == null ? "(duração pendente)" : "(" + duracao + " min)");
                break;
            }
        }
    }

    private void preencherHorarios(XWPFTable tabela, int linhaData, ProgramacaoSemana programacao) {
        List<HorarioS140Calculado> horarios = horarioService.calcular(programacao);
        definirHorario(tabela.getRow(linhaData + 3), horarios, "CANTICO_ORACAO_INICIAL");
        definirHorario(tabela.getRow(linhaData + 4), horarios, "COMENTARIOS_INICIAIS");
        for (ProgramacaoParte parte : programacao.partes()) {
            definirHorarioPorChave(tabela, linhaData, horarios, "PARTE_" + parte.getParte().getId(), parte);
        }
        definirHorario(tabela.getRow(linhaData + 18), horarios, "CANTICO_MEIO");
        definirHorario(tabela.getRow(linhaData + 21), horarios, "ESTUDO_BIBLICO");
        definirHorario(tabela.getRow(linhaData + 22), horarios, "COMENTARIOS_FINAIS");
        definirHorario(tabela.getRow(linhaData + 23), horarios, "CANTICO_ORACAO_FINAL");
    }

    private void definirHorarioPorChave(XWPFTable tabela, int linhaData,
                                        List<HorarioS140Calculado> horarios, String chave,
                                        ProgramacaoParte parte) {
        int linha = -1;
        TipoParte tipo = parte.getParte().getTipo();
        if (tipo == TipoParte.DISCURSO_TESOUROS) linha = linhaData + 7;
        else if (tipo == TipoParte.JOIAS_ESPIRITUAIS) linha = linhaData + 8;
        else if (tipo == TipoParte.LEITURA) linha = linhaData + 9;
        else if (parte.getParte().getSecao() == SecaoParte.MINISTERIO) {
            List<ProgramacaoParte> itens = parte.getParte().getSecao() == SecaoParte.MINISTERIO
                    ? List.of(parte) : List.of();
            // A posição de cada parte do Ministério é resolvida pela ordem salva abaixo.
            linha = linhaData + 12;
        } else if (parte.getParte().getSecao() == SecaoParte.NOSSA_VIDA_CRISTA) {
            linha = linhaData + 19;
        }
        if (linha >= 0) definirHorario(tabela.getRow(linha), horarios, chave);
    }

    private void definirHorario(XWPFTableRow linha, List<HorarioS140Calculado> horarios, String chave) {
        Optional<HorarioS140Calculado> item = horarios.stream().filter(h -> h.chave().equals(chave)).findFirst();
        if (item.isEmpty()) return;
        List<XWPFTableCell> tempos = celulasComMarcador(linha, "0:00");
        if (!tempos.isEmpty()) {
            definirTexto(tempos.get(0), item.get().horarioInicio() == null ? "Pendente" : item.get().horarioInicio().toString());
        }
        if (tempos.size() > 1) {
            definirTexto(tempos.get(1), item.get().horarioFim() == null ? "Pendente" : item.get().horarioFim().toString());
        }
    }

    private List<Integer> encontrarLinhasData(XWPFTable tabela) {
        List<Integer> linhas = new ArrayList<>();
        for (int i = 0; i < tabela.getNumberOfRows(); i++) {
            if (tabela.getRow(i).getTableCells().stream().anyMatch(c -> c.getText().contains("[DATA]"))) {
                linhas.add(i);
            }
        }
        return linhas;
    }

    private void limparBloco(XWPFTable tabela, int linhaData) {
        for (int deslocamento = 0; deslocamento <= 23 && linhaData + deslocamento < tabela.getNumberOfRows(); deslocamento++) {
            XWPFTableRow linha = tabela.getRow(linhaData + deslocamento);
            for (XWPFTableCell celula : linha.getTableCells()) {
                if (celula.getText().contains("[Tema]") || celula.getText().contains("[Nome]") ||
                        celula.getText().contains("[Nome/Nome]") || celula.getText().contains("Cântico [número]")) {
                    substituirNaCelula(celula, "[Tema]", "");
                    substituirNaCelula(celula, "[Nome/Nome]", "");
                    substituirNaCelula(celula, "[Nome]", "");
                    substituirNaCelula(celula, "Cântico [número]", "Cântico");
                }
            }
        }
    }

    private void limparMarcadoresParte(XWPFTableRow linha) {
        for (XWPFTableCell celula : linha.getTableCells()) {
            substituirNaCelula(celula, "[Tema]", "");
            substituirNaCelula(celula, "(X min)", "");
            substituirNaCelula(celula, "(XX min)", "");
            substituirNaCelula(celula, "[Nome/Nome]", "");
            substituirNaCelula(celula, "[Nome]", "");
        }
    }

    private void substituirEmTodoDocumento(XWPFDocument documento, String marcador, String valor) {
        for (XWPFTable tabela : documento.getTables()) {
            for (XWPFTableRow linha : tabela.getRows()) {
                for (XWPFTableCell celula : linha.getTableCells()) substituirNaCelula(celula, marcador, valor);
            }
        }
        for (XWPFParagraph paragrafo : documento.getParagraphs()) substituirNaParagrafo(paragrafo, marcador, valor);
    }

    private void substituirMarcador(XWPFTableRow linha, String marcador, String valor) {
        for (XWPFTableCell celula : linha.getTableCells()) substituirNaCelula(celula, marcador, valor);
    }

    private void setTextoContendo(XWPFTableRow linha, String marcador, String texto) {
        for (XWPFTableCell celula : linha.getTableCells()) {
            if (celula.getText().contains(marcador)) {
                substituirNaCelula(celula, marcador, texto);
                return;
            }
        }
    }

    private void setPlaceholder(XWPFTableRow linha, String marcador, int ocorrencia, String valor) {
        List<XWPFTableCell> celulas = celulasComMarcador(linha, marcador);
        if (ocorrencia < celulas.size()) substituirNaCelula(celulas.get(ocorrencia), marcador, valor == null ? "" : valor);
    }

    private List<XWPFTableCell> celulasComMarcador(XWPFTableRow linha, String marcador) {
        return linha.getTableCells().stream().filter(c -> c.getText().contains(marcador)).toList();
    }

    private void substituirNaCelula(XWPFTableCell celula, String marcador, String valor) {
        for (XWPFParagraph paragrafo : celula.getParagraphs()) substituirNaParagrafo(paragrafo, marcador, valor);
    }

    private void substituirNaParagrafo(XWPFParagraph paragrafo, String marcador, String valor) {
        for (XWPFRun run : paragrafo.getRuns()) {
            String texto = run.getText(0);
            if (texto != null && texto.contains(marcador)) run.setText(texto.replace(marcador, valor == null ? "" : valor), 0);
        }
    }

    private void definirTexto(XWPFTableCell celula, String valor) {
        List<XWPFParagraph> paragrafos = celula.getParagraphs();
        if (paragrafos.isEmpty()) {
            celula.setText(valor == null ? "" : valor);
            return;
        }
        XWPFParagraph primeiro = paragrafos.getFirst();
        List<XWPFRun> runs = primeiro.getRuns();
        if (runs.isEmpty()) primeiro.createRun().setText(valor == null ? "" : valor);
        else {
            runs.getFirst().setText(valor == null ? "" : valor, 0);
            for (int i = 1; i < runs.size(); i++) runs.get(i).setText("", 0);
        }
    }

    private Designacao designacao(Escala escala, Integer parteId, SalaDesignacao sala) {
        return escala.getDesignacoes().stream()
                .filter(d -> d.parte().getId().equals(parteId))
                .filter(d -> d.sala() == sala)
                .findFirst().orElse(null);
    }

    private String nomePorId(Integer id) {
        if (id == null) return "";
        return pessoaDAO.buscarPorId(id).map(Pessoa::getNome).orElse("");
    }

    private static String nome(Pessoa pessoa) {
        return pessoa == null ? "" : pessoa.getNome();
    }

    private static String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private static String rotuloCantico(String cantico) {
        return cantico == null || cantico.isBlank() ? "Cântico" : "Cântico " + cantico.trim();
    }

    private static String temaComDuracao(String tema, int minutos) {
        return tema + " (" + minutos + " min)";
    }

    private static void validarArquivo(Path arquivo, String mensagem) {
        if (arquivo == null || !Files.isRegularFile(arquivo) || !Files.isReadable(arquivo)) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
