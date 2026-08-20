package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;


public class TesteCarregarHistorico {


    public static void executar() {


        System.out.println(
                "\n===== TESTE CARREGAMENTO HISTÓRICO ====="
        );


        HistoricoDesignacoesDAO dao =
                new HistoricoDesignacoesDAO();


        HistoricoDesignacoes historico =
                dao.carregarHistorico();


        System.out.println(
                "Quantidade de participações carregadas: "
                        + historico.participacoes().size()
        );


        for (ParticipacaoDesignacao p :
                historico.participacoes()) {


            System.out.println("-----------------------");

            System.out.println(
                    "Data: "
                            + p.data()
            );


            System.out.println(
                    "Pessoa: "
                            + p.pessoa().getNome()
            );


            System.out.println(
                    "Parte: "
                            + p.parte().getNome()
            );


            System.out.println(
                    "Tipo: "
                            + p.tipoParticipacao()
            );
        }


        System.out.println(
                "===== FIM TESTE HISTÓRICO ====="
        );
    }
}