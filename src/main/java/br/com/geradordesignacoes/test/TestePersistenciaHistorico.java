package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;

import java.time.LocalDate;
import java.util.List;

public class TestePersistenciaHistorico {


    public static void executar() {

        System.out.println(
                "\n===== TESTE PERSISTÊNCIA HISTÓRICO ====="
        );


        PessoaDAO pessoaDAO = new PessoaDAO();

        ParteDAO parteDAO = new ParteDAO();

        HistoricoDesignacoesDAO historicoDAO =
                new HistoricoDesignacoesDAO();


        Pessoa pessoa =
                pessoaDAO.listarTodos()
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Nenhuma pessoa cadastrada."
                                )
                        );


        Parte parte =
                parteDAO.listarTodos()
                        .stream()
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Nenhuma parte cadastrada."
                                )
                        );


        System.out.println("\nDados utilizados:");

        System.out.println(
                "Pessoa: "
                        + pessoa.getNome()
                        + " ID="
                        + pessoa.getId()
        );


        System.out.println(
                "Parte: "
                        + parte.getNome()
                        + " ID="
                        + parte.getId()
        );


        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        null,
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.RESPONSAVEL
                );


        System.out.println(
                "\nSalvando participação..."
        );


        historicoDAO.salvar(
                participacao
        );


        System.out.println(
                "Registro salvo."
        );


        System.out.println(
                "\nCarregando histórico..."
        );


        HistoricoDesignacoes historico =
                historicoDAO.carregarHistorico();


        List<ParticipacaoDesignacao> lista =
                historico.getParticipacoes();


        System.out.println(
                "\nTotal registros encontrados: "
                        + lista.size()
        );


        for (ParticipacaoDesignacao p : lista) {

            System.out.println("-----------------------");

            System.out.println(
                    "ID: "
                            + p.getId()
            );

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
                "\n===== TESTE FINALIZADO ====="
        );
    }
}