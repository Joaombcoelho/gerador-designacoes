package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorS140WordServiceTest {

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
    void deveExigirDestinoComExtensaoDocx() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.exportar(
                        YearMonth.of(2026, 10),
                        Path.of("modelo-s140-inexistente.docx"),
                        Path.of("saida-s140.txt")
                )
        );

        // A validação do modelo precede a validação do destino.
        assertTrue(erro.getMessage().contains("modelo oficial S-140"));
    }

}
