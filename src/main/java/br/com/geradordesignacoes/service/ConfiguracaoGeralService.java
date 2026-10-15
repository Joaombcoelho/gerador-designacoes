package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ConfiguracaoGeralDAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;

public class ConfiguracaoGeralService {

    private final ConfiguracaoGeralDAO dao;

    public ConfiguracaoGeralService() {
        this(new ConfiguracaoGeralDAO());
    }

    public ConfiguracaoGeralService(ConfiguracaoGeralDAO dao) {
        if (dao == null) {
            throw new IllegalArgumentException("O DAO não pode ser nulo.");
        }
        this.dao = dao;
    }

    public ConfiguracaoGeral buscar() {
        return dao.buscar();
    }

    public void salvar(ConfiguracaoGeral configuracao) {
        if (configuracao == null) {
            throw new IllegalArgumentException(
                    "A configuração geral não pode ser nula."
            );
        }
        dao.salvar(configuracao);
    }
}
