package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.apache.poi.xwpf.usermodel.XWPFDocument;\nimport br.com.geradordesignacoes.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.List;

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
