package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.AvaliadorPessoaService;
import br.com.geradordesignacoes.service.ControleDesignacoes;

import java.time.LocalDate;
import java.util.List;

public final class TesteAvaliadorPessoa {

    private TesteAvaliadorPessoa() {
    }

    public static void executar() {

        testarAvaliador();

    }

    private static void testarAvaliador() {

        System.out.println("\n===== TESTE AVALIADOR =====");

        Pessoa joao = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                true,
                Privilegio.SERVO_MINISTERIAL
        );

        Pessoa lucas = new Pessoa(
                "Lucas",
                Sexo.MASCULINO,
                true,
                true,
                true,
                false,
                false,
                Privilegio.BATIZADO
        );

        Parte leitura = new Parte(
                "Leitura da Bíblia",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        AvaliadorPessoaService avaliador =
                new AvaliadorPessoaService();

        System.out.println(
                "João: "
                        + avaliador.avaliar(
                        joao,
                        leitura,
                        controle
                )
        );

        System.out.println(
                "Lucas: "
                        + avaliador.avaliar(
                        lucas,
                        leitura,
                        controle
                )
        );

        controle.registrar(joao);
        controle.registrar(joao);

        System.out.println("\nApós João ter 2 participações:");

        System.out.println(
                "João: "
                        + avaliador.avaliar(
                        joao,
                        leitura,
                        controle
                )
        );

        System.out.println(
                "Lucas: "
                        + avaliador.avaliar(
                        lucas,
                        leitura,
                        controle
                )
        );

        System.out.println("\nApós João fazer a mesma parte:");

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        joao,
                        leitura,
                        TipoParticipacao.LEITOR
                )
        );

        System.out.println(
                "João: "
                        + avaliador.avaliar(
                        joao,
                        leitura,
                        controle
                )
        );

        System.out.println(
                "Lucas: "
                        + avaliador.avaliar(
                        lucas,
                        leitura,
                        controle
                )
        );
    }
}