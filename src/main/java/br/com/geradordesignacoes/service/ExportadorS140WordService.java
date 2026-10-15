package br.com.geradordesignacoes.service;

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
                DadosExportacaoS140Semana dados = carregador.carregar(data);
                List<String> problemas = validarSemana(dados);
                if (problemas.isEmpty()) {
                    semanas.add(dados);
                } else {
                    pendencias.add(data.format(DATA) + ": " + String.join("; ", problemas));
                }
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

            // Remove apenas linhas opcionais de partes que não foram programadas.
            // Fazemos isso depois de preencher todos os blocos e em ordem decrescente,
            // para que os índices dos blocos seguintes não sejam deslocados antes da hora.
            List<Integer> linhasNaoUtilizadas = new ArrayList<>();
            for (int i = 0; i < semanas.size(); i++) {
                linhasNaoUtilizadas.addAll(
                        linhasPartesNaoUtilizadas(semanas.get(i).programacao(), linhasData.get(i)));
            }
            linhasNaoUtilizadas.stream()
                    .distinct()
                    .sorted(Comparator.reverseOrder())
                    .forEach(tabela::removeRow);

            try (OutputStream saida = Files.newOutputStream(destino)) {
                documento.write(saida);
            }
        }
        return destino;
    }

    private List<String> validarSemana(DadosExportacaoS140Semana dados) {
        List<String> problemas = new ArrayList<>();
        if (dados.escala().getPresidente() == null) {
            problemas.add("presidente não definido na escala salva");
        }
        if (dados.escala().getOracaoInicial() == null) {
            problemas.add("oração inicial não definida na escala salva");
        }
        if (dados.escala().getDesignacoes().stream().noneMatch(d ->
                d.parte().getTipo() == TipoParte.ORACAO_FINAL && d.sala() == SalaDesignacao.PRINCIPAL)) {
            problemas.add("oração final não definida na escala salva");
        }
        if (dados.programacao().partes().isEmpty()) {
            problemas.add("a programação semanal não possui partes");
        }
        return problemas;
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
        setPlaceholder(tabela.getRow(linhaData + 21), "[Nome/Nome]", 0,
                estudo == null ? "" : textoParticipantesEstudoBiblico(
                        escala.getDesignacoes(), estudo.getParte().getId()));

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
        List<XWPFTableCell> nomes = celulasComMarcador(tabela.getRow(linha), "[Nome]");
        if (nomes.size() > 1) {
            Designacao salaB = designacao(escala, parte.getParte().getId(), SalaDesignacao.B);
            Designacao principal = designacao(escala, parte.getParte().getId(), SalaDesignacao.PRINCIPAL);
            substituirNaCelula(nomes.get(0), "[Nome]", salaB == null ? "" : nome(salaB.responsavel()));
            substituirNaCelula(nomes.get(nomes.size() - 1), "[Nome]", principal == null ? "" : nome(principal.responsavel()));
        } else {
            Designacao designacao = designacao(escala, parte.getParte().getId(), sala);
            setPlaceholder(tabela.getRow(linha), "[Nome]", 0,
                    designacao == null ? "" : nome(designacao.responsavel()));
        }
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

    /**
     * O dirigente e o leitor do Estudo Bíblico são persistidos como duas
     * designações da mesma parte: uma tem responsável e a outra ajudante.
     * Não se deve consultar apenas a primeira designação encontrada.
     */
    static String textoParticipantesEstudoBiblico(List<Designacao> designacoes, Integer parteId) {
        List<Designacao> estudo = designacoes.stream()
                .filter(d -> Objects.equals(d.parte().getId(), parteId))
                .filter(d -> d.parte().getTipo() == TipoParte.DIRIGENTE_ESTUDO)
                .toList();
        Pessoa dirigente = estudo.stream()
                .map(Designacao::responsavel)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        Pessoa leitor = estudo.stream()
                .map(Designacao::ajudante)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        return nome(dirigente) + "/" + nome(leitor);
    }

    /**
     * Retorna os índices das linhas de partes opcionais sem programação.
     * As linhas fixas da reunião e o layout dos blocos não são removidos.
     */
    static List<Integer> linhasPartesNaoUtilizadas(ProgramacaoSemana programacao, int linhaData) {
        List<ProgramacaoParte> ministerio = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.MINISTERIO)
                .filter(p -> p.getParte().getTipo() != TipoParte.O_QUE_VOCE_DIRIA)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem))
                .toList();
        List<ProgramacaoParte> vidaCrista = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.NOSSA_VIDA_CRISTA)
                .filter(p -> p.getParte().getTipo() != TipoParte.DIRIGENTE_ESTUDO)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem))
                .toList();

        List<Integer> linhas = new ArrayList<>();
        for (int i = Math.min(ministerio.size(), 4); i < 4; i++) {
            linhas.add(linhaData + 12 + i);
        }
        for (int i = Math.min(vidaCrista.size(), 2); i < 2; i++) {
            linhas.add(linhaData + 19 + i);
        }
        return List.copyOf(linhas);
    }

    private void preencherTema(XWPFTableRow linha, ProgramacaoParte parte) {
        for (XWPFTableCell celula : linha.getTableCells()) {
            String texto = celula.getText();
            if (texto.contains("[Tema]")) {
                substituirNaCelula(celula, "[Tema]", textoParteExportacao(parte));
                Integer duracao = parte.getDuracaoMinutos() != null
                        ? parte.getDuracaoMinutos() : parte.getParte().getDuracaoPadraoMinutos();
                substituirNaCelula(celula, "(X min)", duracao == null ? "(duração pendente)" : "(" + duracao + " min)");
                substituirNaCelula(celula, "(XX min)", duracao == null ? "(duração pendente)" : "(" + duracao + " min)");
                break;
            }
        }
    }

    /**
     * Identifica a parte no formulário mesmo quando a programação não tem tema.
     * Quando há tema, mantém os dois dados para não perder o assunto cadastrado.
     */
    static String textoParteExportacao(ProgramacaoParte parte) {
        String nomeParte = textoSeguro(parte.getParte().getNome());
        String tema = textoSeguro(parte.getTema());
        if (nomeParte.isBlank()) {
            return tema;
        }
        return tema.isBlank() ? nomeParte : nomeParte + ": " + tema;
    }

    private void preencherHorarios(XWPFTable tabela, int linhaData, ProgramacaoSemana programacao) {
        List<HorarioS140Calculado> horarios = horarioService.calcular(programacao);
        definirHorario(tabela.getRow(linhaData + 3), horarios, "CANTICO_ORACAO_INICIAL");
        definirHorario(tabela.getRow(linhaData + 4), horarios, "COMENTARIOS_INICIAIS");
        List<ProgramacaoParte> tesouros = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem)).toList();
        for (ProgramacaoParte parte : tesouros) {
            int linha = switch (parte.getParte().getTipo()) {
                case DISCURSO_TESOUROS -> linhaData + 7;
                case JOIAS_ESPIRITUAIS -> linhaData + 8;
                case LEITURA -> linhaData + 9;
                default -> -1;
            };
            if (linha >= 0) definirHorario(tabela.getRow(linha), horarios, "PARTE_" + parte.getParte().getId());
        }
        List<ProgramacaoParte> ministerio = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.MINISTERIO)
                .filter(p -> p.getParte().getTipo() != TipoParte.O_QUE_VOCE_DIRIA)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem)).toList();
        for (int i = 0; i < ministerio.size() && i < 4; i++) {
            ProgramacaoParte parte = ministerio.get(i);
            definirHorario(tabela.getRow(linhaData + 12 + i), horarios, "PARTE_" + parte.getParte().getId());
        }
        List<ProgramacaoParte> vidaCrista = programacao.partes().stream()
                .filter(p -> p.getParte().getSecao() == SecaoParte.NOSSA_VIDA_CRISTA)
                .filter(p -> p.getParte().getTipo() != TipoParte.DIRIGENTE_ESTUDO)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem)).toList();
        for (int i = 0; i < vidaCrista.size() && i < 2; i++) {
            ProgramacaoParte parte = vidaCrista.get(i);
            definirHorario(tabela.getRow(linhaData + 19 + i), horarios, "PARTE_" + parte.getParte().getId());
        }
        definirHorario(tabela.getRow(linhaData + 18), horarios, "CANTICO_MEIO");
        definirHorario(tabela.getRow(linhaData + 21), horarios, "ESTUDO_BIBLICO");
        definirHorario(tabela.getRow(linhaData + 22), horarios, "COMENTARIOS_FINAIS");
        definirHorario(tabela.getRow(linhaData + 23), horarios, "CANTICO_ORACAO_FINAL");
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
        // Alguns blocos compartilham a página com o anterior. Só limpa o
        // cabeçalho de página quando ele pertence exclusivamente a este bloco.
        int linhaCabecalho = linhaData - 2;
        if (linhaCabecalho >= 0
                && tabela.getRow(linhaCabecalho).getTableCells().stream()
                .anyMatch(celula -> celula.getText().contains("[NOME DA CONGREGAÇÃO]"))) {
            limparLinha(tabela.getRow(linhaCabecalho));
        }

        for (int deslocamento = 0;
             deslocamento <= 23 && linhaData + deslocamento < tabela.getNumberOfRows();
             deslocamento++) {
            limparLinha(tabela.getRow(linhaData + deslocamento));
        }
    }

    private void limparLinha(XWPFTableRow linha) {
        for (XWPFTableCell celula : linha.getTableCells()) {
            for (XWPFParagraph paragrafo : celula.getParagraphs()) {
                for (XWPFRun run : paragrafo.getRuns()) {
                    if (run.getText(0) != null) {
                        run.setText("", 0);
                    }
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

    /**
     * Substitui marcadores mesmo quando o Word os divide entre vários runs.
     * A substituição mantém os runs e seus estilos; o texto substituto herda
     * o estilo do primeiro run que continha o marcador.
     */
    private void substituirNaParagrafo(XWPFParagraph paragrafo, String marcador, String valor) {
        if (marcador == null || marcador.isEmpty()) {
            return;
        }

        List<XWPFRun> runs = paragrafo.getRuns();
        if (runs.isEmpty()) {
            return;
        }

        String substituto = valor == null ? "" : valor;
        while (true) {
            List<String> textos = new ArrayList<>(runs.size());
            StringBuilder textoCompleto = new StringBuilder();
            for (XWPFRun run : runs) {
                String texto = run.getText(0);
                textos.add(texto == null ? "" : texto);
                textoCompleto.append(texto == null ? "" : texto);
            }

            int inicio = textoCompleto.indexOf(marcador);
            if (inicio < 0) {
                return;
            }
            int fim = inicio + marcador.length();

            int indiceRunInicio = -1;
            int indiceRunFim = -1;
            int deslocamentoInicio = 0;
            int deslocamentoFim = 0;
            int acumulado = 0;
            for (int i = 0; i < textos.size(); i++) {
                int proximo = acumulado + textos.get(i).length();
                if (indiceRunInicio < 0 && inicio >= acumulado && inicio < proximo) {
                    indiceRunInicio = i;
                    deslocamentoInicio = inicio - acumulado;
                }
                if (fim > acumulado && fim <= proximo) {
                    indiceRunFim = i;
                    deslocamentoFim = fim - acumulado;
                    break;
                }
                acumulado = proximo;
            }

            if (indiceRunInicio < 0 || indiceRunFim < 0) {
                return;
            }

            String textoInicio = textos.get(indiceRunInicio);
            String prefixo = textoInicio.substring(0, deslocamentoInicio);
            if (indiceRunInicio == indiceRunFim) {
                String sufixo = textoInicio.substring(deslocamentoFim);
                runs.get(indiceRunInicio).setText(prefixo + substituto + sufixo, 0);
            } else {
                String textoFim = textos.get(indiceRunFim);
                runs.get(indiceRunInicio).setText(prefixo + substituto, 0);
                for (int i = indiceRunInicio + 1; i < indiceRunFim; i++) {
                    runs.get(i).setText("", 0);
                }
                runs.get(indiceRunFim).setText(textoFim.substring(deslocamentoFim), 0);
            }
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
