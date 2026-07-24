package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.dao.PessoaDAO;

import java.time.LocalDate;
import java.util.List;

public class TesteGeradorEscala {


    public static void executar() {

        System.out.println("\n=== TESTE GERADOR DE ESCALA ===\n");

        new HistoricoDesignacoesDAO().limpar();

        List<Pessoa> pessoas = criarPessoasTeste();

        List<Parte> partes = criarPartesTeste();


        GeradorEscala geradorEscala =
                new GeradorEscala(
                        new RegrasService()
                );


        ResultadoGeracaoEscala resultado =
                geradorEscala.gerar(
                        LocalDate.now(),
                        partes,
                        pessoas
                );


        System.out.println("=== DESIGNAÇÕES GERADAS ===\n");

        resultado.getDesignacoes()
                .forEach(System.out::println);


        System.out.println("\n=== PARTICIPAÇÕES ===\n");

        resultado.getParticipacoes()
                .forEach(System.out::println);


        System.out.println("\n=== ERROS ===\n");

        if (resultado.possuiErros()) {

            resultado.getErros()
                    .forEach(System.out::println);

        } else {

            System.out.println("Nenhum erro encontrado.");
        }
        System.out.println("\n=== DIAGNÓSTICOS ===\n");

        resultado.getDiagnosticos()
                .forEach(System.out::println);
    }


    private static List<Pessoa> criarPessoasTeste() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa joao =
                pessoaDAO.salvar(
                        new Pessoa(
                                "João",
                                Sexo.MASCULINO,
                                true,
                                true,
                                true,
                                true,
                                true,
                                Privilegio.BATIZADO
                        )
                );

        Pessoa pedro =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Pedro",
                                Sexo.MASCULINO,
                                true,
                                false,
                                true,
                                true,
                                false,
                                Privilegio.BATIZADO
                        )
                );

        Pessoa carlos =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Carlos",
                                Sexo.MASCULINO,
                                true,
                                true,
                                true,
                                true,
                                true,
                                Privilegio.BATIZADO
                        )
                );

        Pessoa maria =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Maria",
                                Sexo.FEMININO,
                                true,
                                false,
                                false,
                                true,
                                false,
                                Privilegio.BATIZADO
                        )
                );

        return List.of(
                joao,
                pedro,
                carlos,
                maria
        );
    }

    private static List<Parte> criarPartesTeste() {

        ParteDAO parteDAO = new ParteDAO();

        Parte leitura = parteDAO.salvar(
                new Parte(
                        "Leitura Bíblica",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        Parte demonstracao = parteDAO.salvar(
                new Parte(
                        "Demonstração",
                        TipoParte.DEMONSTRACAO,
                        Privilegio.BATIZADO,
                        true,
                        SexoPermitido.AMBOS,
                        2,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                )
        );
        System.out.println("ID leitura: " + leitura.getId());
        System.out.println("ID demonstração: " + demonstracao.getId());

        return List.of(
                leitura,
                demonstracao
        );

    }

}