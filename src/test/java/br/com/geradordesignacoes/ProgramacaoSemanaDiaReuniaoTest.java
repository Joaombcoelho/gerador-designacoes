package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoGeralDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramacaoSemanaDiaReuniaoTest extends BaseDAOTest {

    private final ConfiguracaoGeralDAO configuracaoDAO = new ConfiguracaoGeralDAO();
    private final ProgramacaoSemanaDAO programacaoDAO = new ProgramacaoSemanaDAO();
    private final ProgramacaoSemanaService service = new ProgramacaoSemanaService();

    @Test
    void deveManterQuartaFeiraComoDiaPadraoDaReuniao() {
        YearMonth mes = YearMonth.of(2026, 9);
        configuracaoDAO.salvar(configuracao(DayOfWeek.WEDNESDAY));

        List<ProgramacaoSemana> semanas = service.listarSemanasDoMes(mes);

        assertEquals(5, semanas.size());
        assertTrue(semanas.stream()
                .allMatch(semana -> semana.data().getDayOfWeek() == DayOfWeek.WEDNESDAY));
        assertEquals(
                List.of(2, 9, 16, 23, 30),
                semanas.stream().map(semana -> semana.data().getDayOfMonth()).toList()
        );
        assertTrue(semanas.stream().noneMatch(semana ->
                programacaoDAO.buscarPorData(semana.data()) != null));
    }

    @Test
    void deveListarReunioesNoDiaHabitualConfigurado() {
        YearMonth mes = YearMonth.of(2026, 9);
        configuracaoDAO.salvar(configuracao(DayOfWeek.TUESDAY));

        List<ProgramacaoSemana> semanas = service.listarSemanasDoMes(mes);

        assertEquals(5, semanas.size());
        assertTrue(semanas.stream()
                .allMatch(semana -> semana.data().getDayOfWeek() == DayOfWeek.TUESDAY));
        assertEquals(
                List.of(1, 8, 15, 22, 29),
                semanas.stream().map(semana -> semana.data().getDayOfMonth()).toList()
        );

        // Apenas listar as reuniões não deve criar registros no banco.
        assertTrue(semanas.stream()
                .allMatch(semana -> semana.id() == null));
        assertTrue(semanas.stream()
                .noneMatch(semana -> programacaoDAO.buscarPorData(semana.data()) != null));
    }

    private ConfiguracaoGeral configuracao(DayOfWeek dia) {
        return new ConfiguracaoGeral(
                "Congregação de teste",
                dia,
                LocalTime.of(19, 30),
                5,
                1,
                4,
                3,
                5
        );
    }
}
