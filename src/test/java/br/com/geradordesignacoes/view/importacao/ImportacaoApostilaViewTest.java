package br.com.geradordesignacoes.view.importacao;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImportacaoApostilaViewTest {

    @Test
    void sugereDiaHabitualQuandoEstaDentroDoIntervaloDaApostila() {
        LocalDate data = ImportacaoApostilaView.sugerirDataReuniao(
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 4),
                DayOfWeek.WEDNESDAY);

        assertEquals(LocalDate.of(2026, 11, 4), data);
    }

    @Test
    void naoSugereDataForaDoIntervaloDaApostila() {
        LocalDate data = ImportacaoApostilaView.sugerirDataReuniao(
                LocalDate.of(2026, 11, 2),
                LocalDate.of(2026, 11, 3),
                DayOfWeek.WEDNESDAY);

        assertNull(data);
    }
}
