package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.AvaliadorPessoaService;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.service.SeletorPessoaService;


public final class TesteDiagnosticoSelecao {


    private TesteDiagnosticoSelecao() {
    }


    public static void executar() {

        System.out.println("\n===== TESTE DIAGNÓSTICO SELEÇÃO =====");


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
                java.util.List.of(TipoParticipacao.LEITOR)
        );


        ControleDesignacoes controle =
                new ControleDesignacoes();


        SeletorPessoaService seletor =
                new SeletorPessoaService(
                        new RegrasService(),
                        new AvaliadorPessoaService()
                );


        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        leitura,
                        java.util.List.of(
                                joao,
                                carlos,
                                lucas
                        ),
                        controle
                );


        System.out.println(diagnostico);

        System.out.println("\n=== DIAGNÓSTICO DA SELEÇÃO ===");


        for (var candidato : diagnostico.getCandidatos()) {

            System.out.println(candidato);
        }


        System.out.println("\n=== ESCOLHIDO ===");

        if (diagnostico.getEscolhido() != null) {

            System.out.println(
                    diagnostico.getEscolhido()
            );

        } else {

            System.out.println(
                    "Nenhum candidato encontrado."
            );
        }
    }
}