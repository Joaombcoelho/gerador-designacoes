package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

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
    void deveRejeitarChaveVazia() {
        HorarioManualS140DAO dao = new HorarioManualS140DAO();
        assertThrows(IllegalArgumentException.class,
                () -> dao.salvar(LocalDate.of(2027, 5, 5), " ", LocalTime.NOON));
    }
}
