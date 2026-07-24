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
                        + historico.getParticipacoes().size()
        );


        for (ParticipacaoDesignacao p :
                historico.getParticipacoes()) {


            System.out.println("-----------------------");

            System.out.println(
                    "Data: "
                            + p.getData()
            );


            System.out.println(
                    "Pessoa: "
                            + p.getPessoa().getNome()
            );


            System.out.println(
                    "Parte: "
                            + p.getParte().getNome()
            );


            System.out.println(
                    "Tipo: "
                            + p.getTipoParticipacao()
            );
        }


        System.out.println(
                "===== FIM TESTE HISTÓRICO ====="
        );
    }
}