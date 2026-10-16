package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import br.com.geradordesignacoes.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorS140WordServiceTest {

    @TempDir
    Path diretorioTemporario;

    private final ExportadorS140WordService service =
            new ExportadorS140WordService();

    @Test
    void deveIdentificarQuatroReunioesEmMesComQuatroQuartasFeiras() {
        assertEquals(
                List.of(
                        LocalDate.of(2026, 10, 7),
                        LocalDate.of(2026, 10, 14),
                        LocalDate.of(2026, 10, 21),
                        LocalDate.of(2026, 10, 28)
                ),
                service.datasReunioes(YearMonth.of(2026, 10), DayOfWeek.WEDNESDAY)
        );
    }

    @Test
    void deveIdentificarCincoReunioesEmMesComCincoQuartasFeiras() {
        assertEquals(
                List.of(
                        LocalDate.of(2026, 12, 2),
                        LocalDate.of(2026, 12, 9),
                        LocalDate.of(2026, 12, 16),
                        LocalDate.of(2026, 12, 23),
                        LocalDate.of(2026, 12, 30)
                ),
                service.datasReunioes(YearMonth.of(2026, 12), DayOfWeek.WEDNESDAY)
        );
    }

    @Test
    void deveRespeitarDiaHabitualDiferenteDeQuartaFeira() {
        assertEquals(
                List.of(
                        LocalDate.of(2026, 10, 4),
                        LocalDate.of(2026, 10, 11),
                        LocalDate.of(2026, 10, 18),
                        LocalDate.of(2026, 10, 25)
                ),
                service.datasReunioes(YearMonth.of(2026, 10), DayOfWeek.SUNDAY)
        );
    }
    @Test
    void deveSubstituirMarcadorDivididoEntreRunsSemPerderTextoAoRedor() throws Exception {
        try (XWPFDocument documento = new XWPFDocument()) {
            var paragrafo = documento.createParagraph();
            paragrafo.createRun().setText("Congregação: [");
            paragrafo.createRun().setText("NOME DA CONGREGAÇÃO");
            paragrafo.createRun().setText("] - mês");

            var metodo = ExportadorS140WordService.class.getDeclaredMethod(
                    "substituirNaParagrafo",
                    org.apache.poi.xwpf.usermodel.XWPFParagraph.class,
                    String.class,
                    String.class);
            metodo.setAccessible(true);
            metodo.invoke(service, paragrafo, "[NOME DA CONGREGAÇÃO]", "Central");

            assertEquals("Congregação: Central - mês", paragrafo.getText());
        }
    }

    @Test
    void deveSubstituirMaisDeUmaOcorrenciaDoMesmoMarcadorDividido() throws Exception {
        try (XWPFDocument documento = new XWPFDocument()) {
            var paragrafo = documento.createParagraph();
            paragrafo.createRun().setText("[");
            paragrafo.createRun().setText("DATA");
            paragrafo.createRun().setText("] e [DATA]");

            var metodo = ExportadorS140WordService.class.getDeclaredMethod(
                    "substituirNaParagrafo",
                    org.apache.poi.xwpf.usermodel.XWPFParagraph.class,
                    String.class,
                    String.class);
            metodo.setAccessible(true);
            metodo.invoke(service, paragrafo, "[DATA]", "15/10/2026");

            assertEquals("15/10/2026 e 15/10/2026", paragrafo.getText());
        }
    }

    @Test
    void deveExibirNomeDaParteQuandoTemaNaoFoiInformado() {
        Parte parte = new Parte(
                1, "Iniciando conversas", TipoParte.INICIANDO_CONVERSAS,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS,
                1, false, NivelLeitura.BASICO, SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL, true, 1, List.of());
        ProgramacaoParte programacaoParte = new ProgramacaoParte(parte, 1, null);

        assertEquals(
                "Iniciando conversas",
                ExportadorS140WordService.textoParteExportacao(programacaoParte));
    }

    @Test
    void deveManterNomeDaParteEAcrescentarTemaQuandoInformado() {
        Parte parte = new Parte(
                1, "Iniciando conversas", TipoParte.INICIANDO_CONVERSAS,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS,
                1, false, NivelLeitura.BASICO, SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL, true, 1, List.of());
        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(parte, 1, "Abordagem informal");

        assertEquals(
                "Iniciando conversas: Abordagem informal",
                ExportadorS140WordService.textoParteExportacao(programacaoParte));
    }

    @Test
    void deveExibirDirigenteELeitorDoEstudoBiblico() {
        Parte estudo = new Parte(
                8, "Estudo Bíblico", TipoParte.DIRIGENTE_ESTUDO,
                Privilegio.SERVO_MINISTERIAL, false, SexoPermitido.MASCULINO,
                1, false, NivelLeitura.BASICO, SecaoParte.NOSSA_VIDA_CRISTA,
                TipoVariacaoParte.FIXA, true, 1, List.of());
        Pessoa dirigente = pessoaTeste("Dirigente", 1);
        Pessoa leitor = pessoaTeste("Leitor", 2);
        List<Designacao> designacoes = List.of(
                new Designacao(LocalDate.of(2026, 10, 7), estudo, dirigente, null),
                new Designacao(LocalDate.of(2026, 10, 7), estudo, null, leitor));

        assertEquals(
                "Dirigente/Leitor",
                ExportadorS140WordService.textoParticipantesEstudoBiblico(designacoes, 8));
    }

    @Test
    void deveIdentificarLinhasOpcionaisSemPartesProgramadasParaRemocao() {
        Parte ministerio = criarParteTeste(
                10, "Iniciando conversas", TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO, 1);
        Parte ministerio2 = criarParteTeste(
                11, "Cultivando interesse", TipoParte.CULTIVANDO_INTERESSE,
                SecaoParte.MINISTERIO, 2);
        ProgramacaoSemana programacao = new ProgramacaoSemana(LocalDate.of(2026, 10, 7));
        programacao.adicionarParte(new ProgramacaoParte(ministerio, 1, null));
        programacao.adicionarParte(new ProgramacaoParte(ministerio2, 2, null));

        assertEquals(
                List.of(116, 117, 121, 122),
                ExportadorS140WordService.linhasPartesNaoUtilizadas(programacao, 102));
    }

    @Test
    void deveRejeitarParteProgramadaSemDesignacaoNaSalaPrincipal() {
        LocalDate data = LocalDate.of(2026, 10, 7);
        Parte ministerio = criarParteTeste(
                30, "Iniciando conversas", TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO, 1);
        ProgramacaoSemana programacao = new ProgramacaoSemana(data);
        programacao.adicionarParte(new ProgramacaoParte(ministerio, 1, null));
        Escala escala = escalaBasica(data);
        List<String> problemas = ExportadorS140WordService.validarSemana(
                new DadosExportacaoS140Semana(data, programacao, escala));

        assertTrue(problemas.stream().anyMatch(p ->
                p.contains("Iniciando conversas") && p.contains("Sala Principal")));
    }

    @Test
    void deveExigirDesignacaoNaSalaBQuandoElaEstiverHabilitada() {
        LocalDate data = LocalDate.of(2026, 10, 7);
        Parte ministerio = criarParteTeste(
                31, "Cultivando interesse", TipoParte.CULTIVANDO_INTERESSE,
                SecaoParte.MINISTERIO, 1);
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                null, data, List.of(new ProgramacaoParte(ministerio, 1, null)),
                true, false, null, null, null, null);
        Escala escala = escalaBasica(data);
        escala.adicionarDesignacao(new Designacao(
                data, ministerio, pessoaTeste("Responsável", 10), null,
                SalaDesignacao.PRINCIPAL));

        List<String> problemas = ExportadorS140WordService.validarSemana(
                new DadosExportacaoS140Semana(data, programacao, escala));

        assertTrue(problemas.stream().anyMatch(p ->
                p.contains("Cultivando interesse") && p.contains("Sala B")));
    }

    @Test
    void deveApontarDirigenteELeitorAusentesNoEstudoBiblico() {
        LocalDate data = LocalDate.of(2026, 10, 7);
        Parte estudo = criarParteTeste(
                32, "Estudo Bíblico", TipoParte.DIRIGENTE_ESTUDO,
                SecaoParte.NOSSA_VIDA_CRISTA, 1);
        ProgramacaoSemana programacao = new ProgramacaoSemana(data);
        programacao.adicionarParte(new ProgramacaoParte(estudo, 1, null));
        Escala escala = escalaBasica(data);

        List<String> problemas = ExportadorS140WordService.validarSemana(
                new DadosExportacaoS140Semana(data, programacao, escala));

        assertTrue(problemas.contains("Estudo Bíblico — dirigente não definido"));
        assertTrue(problemas.contains("Estudo Bíblico — leitor não definido"));
    }

    @Test
    void naoDeveApontarPendenciasQuandoDesignacoesObrigatoriasEstaoPreenchidas() {
        LocalDate data = LocalDate.of(2026, 10, 7);
        Parte estudo = criarParteTeste(
                33, "Estudo Bíblico", TipoParte.DIRIGENTE_ESTUDO,
                SecaoParte.NOSSA_VIDA_CRISTA, 1);
        ProgramacaoSemana programacao = new ProgramacaoSemana(data);
        programacao.adicionarParte(new ProgramacaoParte(estudo, 1, null));
        Escala escala = escalaBasica(data);
        escala.adicionarDesignacao(new Designacao(
                data, estudo, pessoaTeste("Dirigente", 11), null));
        escala.adicionarDesignacao(new Designacao(
                data, estudo, null, pessoaTeste("Leitor", 12)));

        List<String> problemas = ExportadorS140WordService.validarSemana(
                new DadosExportacaoS140Semana(data, programacao, escala));

        assertTrue(problemas.isEmpty(), () -> String.join("; ", problemas));
    }

    @Test
    void deveExportarDocxCompletoComQuatroSemanasERemoverLinhasOpcionais() throws Exception {
        ConfiguracaoGeral configuracao = new ConfiguracaoGeral(
                "Congregação de Teste", DayOfWeek.WEDNESDAY,
                java.time.LocalTime.of(19, 30), 5, 1, 4, 3, 5);
        ConfiguracaoGeralService configuracaoService = new ConfiguracaoGeralService() {
            @Override
            public ConfiguracaoGeral buscar() {
                return configuracao;
            }
        };

        Parte estudo = criarParteTeste(
                40, "Estudo Bíblico", TipoParte.DIRIGENTE_ESTUDO,
                SecaoParte.NOSSA_VIDA_CRISTA, 1);
        Map<LocalDate, DadosExportacaoS140Semana> dadosPorData = new HashMap<>();
        for (LocalDate data : service.datasReunioes(
                YearMonth.of(2026, 10), DayOfWeek.WEDNESDAY)) {
            ProgramacaoSemana programacao = new ProgramacaoSemana(data);
            programacao.adicionarParte(new ProgramacaoParte(estudo, 1, null));
            Escala escala = escalaBasica(data);
            escala.adicionarDesignacao(new Designacao(
                    data, estudo, pessoaTeste("Dirigente", 11), null));
            escala.adicionarDesignacao(new Designacao(
                    data, estudo, null, pessoaTeste("Leitor", 12)));
            dadosPorData.put(data, new DadosExportacaoS140Semana(data, programacao, escala));
        }

        CarregadorDadosExportacaoS140Service carregador =
                new CarregadorDadosExportacaoS140Service() {
                    @Override
                    public DadosExportacaoS140Semana carregar(LocalDate data) {
                        return dadosPorData.get(data);
                    }
                };
        HorarioS140Service horarios = new HorarioS140Service() {
            @Override
            public List<HorarioS140Calculado> calcular(ProgramacaoSemana programacao) {
                return List.of();
            }
        };
        ExportadorS140WordService exportador =
                new ExportadorS140WordService(configuracaoService, carregador, horarios);

        Path modelo = diretorioTemporario.resolve("modelo-teste.docx");
        Path destino = diretorioTemporario.resolve("saida.docx");
        criarModeloDocxTeste(modelo, 4);

        Path resultado = exportador.exportar(YearMonth.of(2026, 10), modelo, destino);

        assertEquals(destino, resultado);
        assertTrue(Files.isRegularFile(destino));
        try (XWPFDocument documento = new XWPFDocument(Files.newInputStream(destino))) {
            var tabela = documento.getTables().getFirst();
            String texto = tabela.getText();
            assertEquals(72, tabela.getNumberOfRows(),
                    "As quatro semanas devem manter apenas as linhas fixas e do Estudo Bíblico");
            assertTrue(texto.contains("07/10/2026"));
            assertTrue(texto.contains("14/10/2026"));
            assertTrue(texto.contains("21/10/2026"));
            assertTrue(texto.contains("28/10/2026"));
            assertTrue(texto.contains("Dirigente/Leitor"));
            assertTrue(!texto.contains("[DATA]"));
            assertTrue(!texto.contains("[Nome]"));
            assertTrue(!texto.contains("[Tema]"));
            assertTrue(!texto.contains("[NOME DA CONGREGAÇÃO]"));
        }
    }

    private void criarModeloDocxTeste(Path caminho, int quantidadeSemanas) throws IOException {
        try (XWPFDocument documento = new XWPFDocument()) {
            var tabela = documento.createTable();
            int totalLinhas = quantidadeSemanas * 24;
            for (int indice = 0; indice < totalLinhas; indice++) {
                if (indice > 0) {
                    tabela.createRow();
                }
                int deslocamento = indice % 24;
                String texto = switch (deslocamento) {
                    case 0 -> "[DATA] [NOME DA CONGREGAÇÃO] [Nome]";
                    case 1 -> "[Nome]";
                    case 3 -> "Cântico [número] [Nome]";
                    case 4 -> "Comentários iniciais (1 min)";
                    case 7, 8, 9, 12, 13, 14, 15, 19, 20 -> "[Tema] (X min) [Nome/Nome]";
                    case 18 -> "Cântico [número]";
                    case 21 -> "[Nome/Nome]";
                    case 22 -> "Comentários finais (3 min)";
                    case 23 -> "Cântico [número] [Nome]";
                    default -> "";
                };
                tabela.getRow(indice).getCell(0).setText(texto);
            }
            try (OutputStream saida = Files.newOutputStream(caminho)) {
                documento.write(saida);
            }
        }
    }

    private Escala escalaBasica(LocalDate data) {
        Parte oracaoFinal = criarParteTeste(
                90, "Oração final", TipoParte.ORACAO_FINAL,
                SecaoParte.NOSSA_VIDA_CRISTA, 90);
        Escala escala = new Escala(data, List.of());
        escala.setPresidente(pessoaTeste("Presidente", 1));
        escala.setOracaoInicial(pessoaTeste("Oração inicial", 2));
        escala.adicionarDesignacao(new Designacao(
                data, oracaoFinal, pessoaTeste("Oração final", 3), null));
        return escala;
    }

    private Pessoa pessoaTeste(String nome, int id) {
        Pessoa pessoa = new Pessoa(
                nome, Sexo.MASCULINO, true, true, true,
                true, true, true, false, true,
                Privilegio.SERVO_MINISTERIAL, NivelLeitura.BASICO);
        pessoa.setId(id);
        return pessoa;
    }

    private Parte criarParteTeste(
            int id, String nome, TipoParte tipo, SecaoParte secao, int ordem) {
        return new Parte(
                id, nome, tipo, Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS,
                1, false, NivelLeitura.BASICO, secao,
                TipoVariacaoParte.VARIAVEL, true, ordem, List.of());
    }

    @Test
    void deveGerarNomePadraoDoS140ComMesEAno() {
        assertEquals(
                "Programacao_reuniao_meio_de_semana_Outubro_2026.docx",
                br.com.geradordesignacoes.controller.ProgramacaoController
                        .nomeArquivoS140(YearMonth.of(2026, 10)));
    }

    @Test
    void deveRejeitarMesNuloAntesDeAcessarArquivosOuBanco() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportar(null, null, null)
        );

        assertTrue(erro.getMessage().contains("Selecione o mês"));
    }

    @Test
    void deveRejeitarModeloInexistenteAntesDeCarregarDados() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportar(
                        YearMonth.of(2026, 10),
                        Path.of("modelo-s140-inexistente.docx"),
                        Path.of("saida-s140.docx")
                )
        );

        assertTrue(erro.getMessage().contains("modelo oficial S-140"));
    }

    @Test
    void deveExigirDestinoComExtensaoDocx() throws IOException {
        Path modelo = diretorioTemporario.resolve("modelo.docx");
        try (XWPFDocument documento = new XWPFDocument()) {
            documento.createParagraph().createRun().setText("Modelo de teste");
            try (var saida = Files.newOutputStream(modelo)) {
                documento.write(saida);
            }
        }

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportar(
                        YearMonth.of(2026, 10),
                        modelo,
                        diretorioTemporario.resolve("saida-s140.txt")
                )
        );

        assertTrue(erro.getMessage().contains("extensão .docx"));
    }

}
