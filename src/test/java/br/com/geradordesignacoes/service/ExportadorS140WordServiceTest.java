package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
