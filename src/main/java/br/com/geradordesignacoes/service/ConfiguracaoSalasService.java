package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ConfiguracaoSalasDAO;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;

public class ConfiguracaoSalasService {

    private final ConfiguracaoSalasDAO dao;

    public ConfiguracaoSalasService() {
        this(new ConfiguracaoSalasDAO());
    }

    public ConfiguracaoSalasService(ConfiguracaoSalasDAO dao) {
        this.dao = dao;
    }

    public ConfiguracaoSalas buscar() {
        return dao.buscar();
    }

    public void salvar(ConfiguracaoSalas configuracao) {
        dao.salvar(configuracao);
    }
}
