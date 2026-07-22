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
    public final void carregar() {

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
     * Adiciona uma participação:
     * 1 - salva no banco
     * 2 - atualiza memória
     */
    public void adicionar(
            ParticipacaoDesignacao participacao
    ) {


        validarParticipacao(
                participacao
        );


        historicoDAO.salvar(
                participacao
        );


        historico.adicionar(
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



    /**
     * Registra somente participações válidas
     * vindas de uma geração.
     */
    public void registrarGeracao(
            List<ParticipacaoDesignacao> participacoes
    ) {


        for (ParticipacaoDesignacao participacao :
                participacoes) {


            if (participacao.getPessoa().getId() == null) {
                continue;
            }


            if (participacao.getParte().getId() == null) {
                continue;
            }


            adicionar(
                    participacao
            );
        }
    }



    private void validarParticipacao(
            ParticipacaoDesignacao participacao
    ) {


        if (participacao == null) {

            throw new IllegalArgumentException(
                    "Participação não pode ser nula."
            );
        }


        if (participacao.getPessoa() == null) {

            throw new IllegalArgumentException(
                    "Participação sem pessoa."
            );
        }


        if (participacao.getParte() == null) {

            throw new IllegalArgumentException(
                    "Participação sem parte."
            );
        }


        if (participacao.getPessoa().getId() == null) {

            throw new IllegalArgumentException(
                    "Pessoa sem ID para persistência."
            );
        }


        if (participacao.getParte().getId() == null) {

            throw new IllegalArgumentException(
                    "Parte sem ID para persistência."
            );
        }
    }
}