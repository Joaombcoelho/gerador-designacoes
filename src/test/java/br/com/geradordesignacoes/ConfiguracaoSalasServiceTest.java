
package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoSalasDAO;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;
import br.com.geradordesignacoes.service.ConfiguracaoSalasService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguracaoSalasServiceTest {

    @Test
    void deveManterSalaCDesabilitadaQuandoSalaBEstiverDesabilitada() {
        DaoEmMemoria dao = new DaoEmMemoria();
        ConfiguracaoSalasService service =
                new ConfiguracaoSalasService(dao);

        service.salvar(new ConfiguracaoSalas(false, true));

        assertEquals(
                new ConfiguracaoSalas(false, false),
                dao.configuracaoSalva
        );
    }

    @Test
    void devePermitirSalaBAtivaSemSalaC() {
        DaoEmMemoria dao = new DaoEmMemoria();
        ConfiguracaoSalasService service =
                new ConfiguracaoSalasService(dao);

        service.salvar(new ConfiguracaoSalas(true, false));

        assertEquals(
                new ConfiguracaoSalas(true, false),
                dao.configuracaoSalva
        );
    }

    @Test
    void devePermitirSalaBESalaCAtivas() {
        DaoEmMemoria dao = new DaoEmMemoria();
        ConfiguracaoSalasService service =
                new ConfiguracaoSalasService(dao);

        service.salvar(new ConfiguracaoSalas(true, true));

        assertEquals(
                new ConfiguracaoSalas(true, true),
                dao.configuracaoSalva
        );
    }

    @Test
    void deveRejeitarConfiguracaoNula() {
        DaoEmMemoria dao = new DaoEmMemoria();
        ConfiguracaoSalasService service =
                new ConfiguracaoSalasService(dao);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.salvar(null)
        );
    }

    private static class DaoEmMemoria extends ConfiguracaoSalasDAO {

        private ConfiguracaoSalas configuracaoSalva;

        @Override
        public void salvar(ConfiguracaoSalas configuracao) {
            configuracaoSalva = configuracao;
        }
    }
}
