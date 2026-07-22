package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.HistoricoDesignacoesService;

import java.time.LocalDate;


public class TesteHistoricoService {


    public static void executar() {


        System.out.println(
                "\n===== TESTE HISTÓRICO SERVICE ====="
        );


        HistoricoDesignacoesService service =
                new HistoricoDesignacoesService();



        System.out.println(
                "Histórico carregado:"
        );


        System.out.println(
                "Quantidade inicial: "
                        + service.getHistorico()
                        .getParticipacoes()
                        .size()
        );



        PessoaDAO pessoaDAO =
                new PessoaDAO();

        ParteDAO parteDAO =
                new ParteDAO();



        Pessoa pessoa =
                pessoaDAO.listarTodos()
                        .stream()
                        .findFirst()
                        .orElseThrow();



        Parte parte =
                parteDAO.listarTodos()
                        .stream()
                        .findFirst()
                        .orElseThrow();



        ParticipacaoDesignacao novaParticipacao =
                new ParticipacaoDesignacao(
                        null,
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.AJUDANTE
                );



        System.out.println(
                "\nAdicionando nova participação..."
        );


        service.adicionar(
                novaParticipacao
        );



        System.out.println(
                "Quantidade após adicionar: "
                        + service.getHistorico()
                        .getParticipacoes()
                        .size()
        );



        System.out.println(
                "\nCriando novo Service..."
        );


        HistoricoDesignacoesService novoService =
                new HistoricoDesignacoesService();



        System.out.println(
                "Quantidade recuperada do banco: "
                        + novoService.getHistorico()
                        .getParticipacoes()
                        .size()
        );



        System.out.println(
                "\n===== FIM TESTE SERVICE ====="
        );
    }
}