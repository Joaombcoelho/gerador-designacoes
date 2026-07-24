package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.AvaliadorPessoaService;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.service.SeletorPessoaService;

import java.util.Comparator;
import java.util.List;


public final class TesteRankingAvaliacao {


    private TesteRankingAvaliacao() {
    }


    public static void executar() {

        System.out.println("\n===== TESTE RANKING AVALIAÇÃO =====");


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


        Pessoa carlos = new Pessoa(
                "Carlos",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );


        Pessoa lucas = new Pessoa(
                "Lucas",
                Sexo.MASCULINO,
                true,
                false,
                true,
                true,
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


        SeletorPessoaService seletor =
                new SeletorPessoaService(
                        new RegrasService(),
                        new AvaliadorPessoaService()
                );


        List<ResultadoAvaliacaoPessoa> ranking =
                seletor.avaliarCandidatos(
                        leitura,
                        List.of(
                                joao,
                                carlos,
                                lucas
                        ),
                        controle
                );


        ranking.sort(
                Comparator.comparingInt(
                        ResultadoAvaliacaoPessoa::getTotal
                ).reversed()
        );


        System.out.println("\n=== RANKING DE CANDIDATOS ===");


        for (ResultadoAvaliacaoPessoa resultado : ranking) {

            System.out.println(resultado);
        }
    }
}