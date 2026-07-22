package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;

import java.util.List;

public class HistoricoDesignacoesService {


    private final HistoricoDesignacoesDAO historicoDAO;

    private HistoricoDesignacoes historico;



    public HistoricoDesignacoesService() {

        this.historicoDAO =
                new HistoricoDesignacoesDAO();

        carregar();
    }



    /**
     * Carrega o histórico persistido no banco.
     */
    public void carregar() {

        this.historico =
                historicoDAO.carregarHistorico();
    }



    /**
     * Retorna o histórico atual em memória.
     */
    public HistoricoDesignacoes getHistorico() {

        return historico;
    }



    /**
     * Adiciona uma participação
     * e persiste no banco.
     */
    public void adicionar(
            ParticipacaoDesignacao participacao
    ) {


        historico.adicionar(
                participacao
        );


        historicoDAO.salvar(
                participacao
        );
    }



    /**
     * Salva várias participações geradas.
     */
    public void adicionarTodos(
            List<ParticipacaoDesignacao> participacoes
    ) {


        for (ParticipacaoDesignacao participacao :
                participacoes) {


            adicionar(
                    participacao
            );
        }
    }
    public void registrarGeracao(
            List<ParticipacaoDesignacao> participacoes
    ) {

        for (ParticipacaoDesignacao participacao : participacoes) {

            if (participacao.getPessoa().getId() == null) {

                continue;
            }

            adicionar(
                    participacao
            );
        }
    }
}