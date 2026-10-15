package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoGeralDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoGeralDAOTest extends BaseDAOTest {

    private final ConfiguracaoGeralDAO dao = new ConfiguracaoGeralDAO();

    @Test
    void deveInicializarConfiguracaoGeralComValoresPadrao() {
        ConfiguracaoGeral configuracao = dao.buscar();

        assertEquals(ConfiguracaoGeral.padrao(), configuracao);
        assertEquals(DayOfWeek.WEDNESDAY, configuracao.diaReuniaoHabitual());
        assertEquals(LocalTime.of(19, 30), configuracao.horarioInicio());
    }

    @Test
    void deveSalvarERecarregarConfiguracaoGeral() {
        ConfiguracaoGeral configuracao = new ConfiguracaoGeral(
                "Congregação Central",
                DayOfWeek.THURSDAY,
                LocalTime.of(19, 0),
                6, 2, 5, 4, 6
        );

        dao.salvar(configuracao);

        assertEquals(configuracao, dao.buscar());
    }

    @Test
    void deveAtualizarConfiguracaoExistenteSemCriarOutraLinha() throws Exception {
        dao.salvar(new ConfiguracaoGeral(
                "Primeiro nome", DayOfWeek.WEDNESDAY, LocalTime.of(19, 30),
                5, 1, 4, 3, 5
        ));
        dao.salvar(new ConfiguracaoGeral(
                "Segundo nome", DayOfWeek.SATURDAY, LocalTime.of(18, 30),
                5, 1, 4, 3, 5
        ));

        try (var statement = connection.createStatement();
             var resultSet = statement.executeQuery(
                     "SELECT COUNT(*) FROM configuracoes_gerais")) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
        assertEquals("Segundo nome", dao.buscar().nomeCongregacao());
    }

    @Test
    void deveRejeitarDuracaoNaoPositiva() {
        assertThrows(IllegalArgumentException.class, () -> new ConfiguracaoGeral(
                "", DayOfWeek.WEDNESDAY, LocalTime.of(19, 30),
                0, 1, 4, 3, 5
        ));
    }

    @Test
    void deveRejeitarConfiguracaoNula() {
        assertThrows(IllegalArgumentException.class, () -> dao.salvar(null));
    }
}
