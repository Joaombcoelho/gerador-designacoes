package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import br.com.geradordesignacoes.dao.ConfiguracaoGeralDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.HorarioS140Item;
import br.com.geradordesignacoes.service.HorarioS140Service;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import java.time.DayOfWeek;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorarioManualS140DAOTest extends BaseDAOTest {

    @Test
    void deveSalvarAtualizarListarERemoverAjusteManual() {
        LocalDate data = LocalDate.of(2027, 5, 5);
        HorarioManualS140DAO dao = new HorarioManualS140DAO();

        // A tabela exige uma programação semanal existente.
        new br.com.geradordesignacoes.service.ProgramacaoSemanaService().obterOuCriar(data);

        dao.salvar(data, "BLOCO_TESOUROS", LocalTime.of(19, 40));
        assertEquals(LocalTime.of(19, 40), dao.listarPorData(data).get("BLOCO_TESOUROS"));

        dao.salvar(data, "BLOCO_TESOUROS", LocalTime.of(19, 45));
        assertEquals(LocalTime.of(19, 45), dao.listarPorData(data).get("BLOCO_TESOUROS"));

        dao.salvar(data, "BLOCO_MINISTERIO", LocalTime.of(20, 15));
        assertEquals(2, dao.listarPorData(data).size());

        dao.remover(data, "BLOCO_TESOUROS");
        assertFalse(dao.listarPorData(data).containsKey("BLOCO_TESOUROS"));
        assertTrue(dao.listarPorData(data).containsKey("BLOCO_MINISTERIO"));
    }

    @Test
    void deveUsarHorarioConfiguradoEManterAjusteManualAoRecalcular() {
        LocalDate data = LocalDate.of(2027, 6, 2);
        new ConfiguracaoGeralDAO().salvar(new ConfiguracaoGeral(
                "Teste", DayOfWeek.WEDNESDAY, LocalTime.of(19, 0),
                5, 1, 4, 3, 5
        ));
        new ProgramacaoSemanaService().obterOuCriar(data);
        HorarioS140Service service = new HorarioS140Service();

        // Usa uma parte identificada como PARTE_ID, em vez do bloco artificial
        // BLOCO_MINISTERIO, que deixou de existir na implementação do S-140.
        List<HorarioS140Item> itens = List.of(
                new HorarioS140Item(
                        "PARTE_999",
                        "Parte do Ministério",
                        18,
                        null
                )
        );

        List<HorarioS140Calculado> antes = service.calcular(data, itens);
        assertEquals(LocalTime.of(19, 0), antes.getFirst().horarioInicio());

        service.ajustarHorario(data, "PARTE_999", LocalTime.of(20, 15));
        List<HorarioS140Calculado> depois = service.calcular(data, itens);
        HorarioS140Calculado ministerio = depois.stream()
                .filter(item -> item.chave().equals("PARTE_999"))
                .findFirst()
                .orElseThrow();

        assertEquals(LocalTime.of(20, 15), ministerio.horarioInicio());
        assertTrue(ministerio.horarioManual());
        assertEquals(LocalTime.of(20, 33), ministerio.horarioFim());
    }

    @Test
    void deveRejeitarChaveVazia() {
        HorarioManualS140DAO dao = new HorarioManualS140DAO();
        assertThrows(IllegalArgumentException.class,
                () -> dao.salvar(LocalDate.of(2027, 5, 5), " ", LocalTime.NOON));
    }
}
