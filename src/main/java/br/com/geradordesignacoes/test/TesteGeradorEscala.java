package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;

import java.time.LocalDate;
import java.util.List;

public class TesteGeradorEscala {


    public static void executar() {

        System.out.println("\n=== TESTE GERADOR DE ESCALA ===\n");


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
    }



    private static List<Pessoa> criarPessoasTeste() {


        Pessoa joao =
                new Pessoa(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        true,
                        true,
                        Privilegio.BATIZADO
                );


        Pessoa pedro =
                new Pessoa(
                        "Pedro",
                        Sexo.MASCULINO,
                        true,
                        false,
                        true,
                        true,
                        false,
                        Privilegio.BATIZADO
                );


        Pessoa carlos =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        true,
                        true,
                        Privilegio.BATIZADO
                );


        Pessoa maria =
                new Pessoa(
                        "Maria",
                        Sexo.FEMININO,
                        true,
                        false,
                        false,
                        true,
                        false,
                        Privilegio.BATIZADO
                );


        return List.of(
                joao,
                pedro,
                carlos,
                maria
        );
    }



    private static List<Parte> criarPartesTeste() {


        Parte leitura =
                new Parte(
                        "Leitura Bíblica",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.LEITOR
                        )
                );



        Parte demonstracao =
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
                );


        return List.of(
                leitura,
                demonstracao
        );
    }
}