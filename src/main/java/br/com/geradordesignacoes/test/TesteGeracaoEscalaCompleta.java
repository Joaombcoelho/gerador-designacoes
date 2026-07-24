package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TesteGeracaoEscalaCompleta {

    public static void executar() {

        System.out.println(
                "\n===== TESTE GERAÇÃO DE ESCALA COMPLETA ====="
        );

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        GeradorEscala geradorEscala =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala primeiraGeracao =
                geradorEscala.gerar(
                        LocalDate.of(2026, 7, 28),
                        partes,
                        pessoas,
                        new HistoricoDesignacoes()
                );

        imprimirEscala(primeiraGeracao);

        validarGeracaoCompleta(
                primeiraGeracao,
                partes.size(),
                4
        );

        ResultadoGeracaoEscala segundaGeracao =
                geradorEscala.gerar(
                        LocalDate.of(2026, 8, 4),
                        partes,
                        pessoas,
                        new HistoricoDesignacoes(
                                primeiraGeracao.getParticipacoes()
                        )
                );
        System.out.println(
                "TOTAL PRIMEIRA GERAÇÃO: "
                        + primeiraGeracao.getParticipacoes().size()
        );

        System.out.println(
                "TOTAL SEGUNDA GERAÇÃO: "
                        + segundaGeracao.getParticipacoes().size()
        );

        for (ParticipacaoDesignacao p :
                segundaGeracao.getParticipacoes()) {

            System.out.println(
                    p.getPessoa().getNome()
                            + " - "
                            + p.getParte().getNome()
                            + " - "
                            + p.getTipoParticipacao()
            );
        }

        validarSegundaGeracaoConsideraPrimeira(
                primeiraGeracao,
                segundaGeracao
        );

        System.out.println(
                "===== FIM TESTE GERAÇÃO DE ESCALA COMPLETA ====="
        );
    }


    private static List<Pessoa> criarPessoas() {

        Pessoa joao =
                new Pessoa(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        true,
                        Privilegio.ANCIAO
                );

        Pessoa carlos =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        false,
                        false,
                        true,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Pessoa lucas =
                new Pessoa(
                        "Lucas",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Pessoa pedro =
                new Pessoa(
                        "Pedro",
                        Sexo.MASCULINO,
                        true,
                        false,
                        true,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        return List.of(
                joao,
                carlos,
                lucas,
                pedro
        );
    }


    private static List<Parte> criarPartes() {

        Parte leitura =
                new Parte(
                        "Leitura",
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

        Parte discurso =
                new Parte(
                        "Discurso",
                        TipoParte.DISCURSO,
                        Privilegio.ANCIAO,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.ORADOR
                        )
                );

        Parte demonstracao =
                new Parte(
                        "Demonstração",
                        TipoParte.DEMONSTRACAO,
                        Privilegio.SERVO_MINISTERIAL,
                        true,
                        SexoPermitido.MASCULINO,
                        2,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                );

        return List.of(
                leitura,
                discurso,
                demonstracao
        );
    }


    private static void imprimirEscala(
            ResultadoGeracaoEscala resultado
    ) {

        System.out.println(
                "\n===== ESCALA GERADA ====="
        );

        for (Designacao designacao : resultado.getDesignacoes()) {

            System.out.println(
                    "\nParte: "
                            + designacao.getParte().getNome()
            );

            if (designacao.getParte().getTipo() == TipoParte.DEMONSTRACAO) {

                System.out.println(
                        "Responsável: "
                                + designacao.getResponsavel().getNome()
                );

                System.out.println(
                        "Ajudante: "
                                + designacao.getAjudante().getNome()
                );

            } else {

                System.out.println(
                        "Pessoa: "
                                + designacao.getResponsavel().getNome()
                );
            }
        }
    }


    private static void validarGeracaoCompleta(
            ResultadoGeracaoEscala resultado,
            int quantidadePartesEsperadas,
            int quantidadeParticipacoesEsperadas
    ) {

        if (resultado.possuiErros()) {
            throw new RuntimeException(
                    "A escala completa não deveria possuir erros: "
                            + resultado.getErros()
            );
        }

        if (resultado.getDesignacoes().size() != quantidadePartesEsperadas) {
            throw new RuntimeException(
                    "Nem todas as partes solicitadas foram geradas."
            );
        }

        if (resultado.getParticipacoes().size()
                != quantidadeParticipacoesEsperadas) {
            throw new RuntimeException(
                    "O histórico da geração não recebeu todas as participações."
            );
        }

        Set<Pessoa> pessoasDesignadas = new HashSet<>();

        for (Designacao designacao : resultado.getDesignacoes()) {

            if (!designacao.getParte().podeSerRealizadaPor(
                    designacao.getResponsavel()
            )) {
                throw new RuntimeException(
                        "Responsável inválido para a parte: "
                                + designacao.getParte().getNome()
                );
            }

            if (!pessoasDesignadas.add(designacao.getResponsavel())) {
                throw new RuntimeException(
                        "Pessoa repetida na mesma escala: "
                                + designacao.getResponsavel().getNome()
                );
            }

            if (designacao.getParte().getTipo() == TipoParte.DEMONSTRACAO) {

                if (designacao.getAjudante() == null) {
                    throw new RuntimeException(
                            "Demonstração gerada sem ajudante."
                    );
                }

                if (!designacao.getParte().pessoaPodeExercerParticipacao(
                        designacao.getResponsavel(),
                        TipoParticipacao.RESPONSAVEL
                )) {
                    throw new RuntimeException(
                            "Responsável da demonstração não pode exercer a participação."
                    );
                }

                if (!designacao.getParte().pessoaPodeExercerParticipacao(
                        designacao.getAjudante(),
                        TipoParticipacao.AJUDANTE
                )) {
                    throw new RuntimeException(
                            "Ajudante da demonstração não pode exercer a participação."
                    );
                }

                if (!pessoasDesignadas.add(designacao.getAjudante())) {
                    throw new RuntimeException(
                            "Pessoa repetida na mesma escala: "
                                    + designacao.getAjudante().getNome()
                    );
                }
            }
        }
    }


    private static void validarSegundaGeracaoConsideraPrimeira(
            ResultadoGeracaoEscala primeiraGeracao,
            ResultadoGeracaoEscala segundaGeracao
    ) {

        if (segundaGeracao.getParticipacoes().size()
                <= primeiraGeracao.getParticipacoes().size()) {
            throw new RuntimeException(
                    "A segunda geração não considerou/acumulou o histórico da primeira."
            );
        }

        List<ParticipacaoDesignacao> participacoesPrimeiraGeracao =
                primeiraGeracao.getParticipacoes();

        boolean manteveHistorico =
                segundaGeracao.getParticipacoes()
                        .containsAll(participacoesPrimeiraGeracao);

        if (!manteveHistorico) {
            throw new RuntimeException(
                    "A segunda geração não manteve as participações da primeira no histórico."
            );
        }
    }
}
