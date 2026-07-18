package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;

import java.time.LocalDate;
import java.util.List;

public final class TesteHistoricoDesignacoes {

    private TesteHistoricoDesignacoes() {
    }


    public static void executar() {

        System.out.println("\n===== TESTE HISTÓRICO DESIGNAÇÕES =====");


        Pessoa joao =
                new Pessoa(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        true,
                        true,
                        Privilegio.SERVO_MINISTERIAL
                );


        Pessoa carlos =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        true,
                        false,
                        Privilegio.BATIZADO
                );


        Parte leitura =
                new Parte(
                        "Leitura Bíblica",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.LEITOR
                        )
                );


        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );


        ResultadoGeracaoEscala primeiraGeracao =
                gerador.gerar(
                        LocalDate.now(),
                        List.of(leitura),
                        List.of(joao, carlos)
                );


        System.out.println("\n=== PRIMEIRA GERAÇÃO ===");

        primeiraGeracao.getDesignacoes()
                .forEach(System.out::println);


        ResultadoGeracaoEscala segundaGeracao =
                gerador.gerar(
                        LocalDate.now().plusDays(7),
                        List.of(leitura),
                        List.of(joao, carlos),
                        primeiraGeracao.getParticipacoes()
                );


        System.out.println("\n=== SEGUNDA GERAÇÃO COM HISTÓRICO ===");

        segundaGeracao.getDesignacoes()
                .forEach(System.out::println);


        System.out.println("\n=== DIAGNÓSTICO SEGUNDA GERAÇÃO ===");

        segundaGeracao.getDiagnosticos()
                .forEach(System.out::println);
    }
}