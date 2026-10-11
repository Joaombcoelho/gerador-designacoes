package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoSalasDAO;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfiguracaoSalasDAOTest extends BaseDAOTest {

    private final ConfiguracaoSalasDAO dao =
            new ConfiguracaoSalasDAO();

    @Test
    void deveCriarConfiguracaoComSalasDesabilitadas() {
        ConfiguracaoSalas configuracao = dao.buscar();

        assertFalse(configuracao.salaBHabilitada());
        assertFalse(configuracao.salaCHabilitada());
    }

    @Test
    void deveSalvarERecarregarConfiguracao() {
        dao.salvar(new ConfiguracaoSalas(true, false));

        ConfiguracaoSalas encontrada = dao.buscar();

        assertTrue(encontrada.salaBHabilitada());
        assertFalse(encontrada.salaCHabilitada());
    }
}
