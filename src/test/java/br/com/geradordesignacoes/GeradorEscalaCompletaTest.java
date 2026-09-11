package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorEscalaCompletaTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();

    private final GeradorEscala gerador =
            new GeradorEscala(new RegrasService());


    @Test
    void deveGerarTresPartesVariaveis() {

        List<Pessoa> pessoas = criarPessoas(6);

        List<Parte> partes = criarPartesVariaveis(3);

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        partes,
                        pessoas
                );

        assertTrue(
                resultado.erros().isEmpty(),
                "Não deveria haver erros na geração."
        );

        assertEquals(
                3,
                resultado.escala().getDesignacoes().size()
        );
    }


    @Test
    void deveGerarQuatroPartesVariaveis() {

        List<Pessoa> pessoas = criarPessoas(8);

        List<Parte> partes = criarPartesVariaveis(4);

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        partes,
                        pessoas
                );

        assertTrue(
                resultado.erros().isEmpty(),
                "Não deveria haver erros na geração."
        );

        assertEquals(
                4,
                resultado.escala().getDesignacoes().size()
        );
    }


    @Test
    void deveGerarCincoPartesVariaveis() {

        List<Pessoa> pessoas = criarPessoas(10);

        List<Parte> partes = criarPartesVariaveis(5);

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        partes,
                        pessoas
                );

        assertTrue(
                resultado.erros().isEmpty(),
                "Não deveria haver erros na geração."
        );

        assertEquals(
                5,
                resultado.escala().getDesignacoes().size()
        );
    }


    @Test
    void deveGerarSeisPartesVariaveis() {

        List<Pessoa> pessoas = criarPessoas(12);

        List<Parte> partes = criarPartesVariaveis(6);

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        partes,
                        pessoas
                );

        assertTrue(
                resultado.erros().isEmpty(),
                "Não deveria haver erros na geração."
        );

        assertEquals(
                6,
                resultado.escala().getDesignacoes().size()
        );
    }


    @Test
    void deveGerarCombinacoesDiferentesDePartesPorSemana() {

        List<Pessoa> pessoas = criarPessoas(12);

        List<Parte> partesSemana1 =
                criarPartesVariaveisComPrefixo(3, "Semana 1");

        List<Parte> partesSemana2 =
                criarPartesVariaveisComPrefixo(5, "Semana 2");

        ResultadoGeracaoEscala resultadoSemana1 =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        partesSemana1,
                        pessoas
                );

        ResultadoGeracaoEscala resultadoSemana2 =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 10),
                        partesSemana2,
                        pessoas
                );

        assertTrue(
                resultadoSemana1.erros().isEmpty(),
                "A primeira semana não deveria apresentar erros."
        );

        assertTrue(
                resultadoSemana2.erros().isEmpty(),
                "A segunda semana não deveria apresentar erros."
        );

        assertEquals(
                3,
                resultadoSemana1.escala()
                        .getDesignacoes()
                        .size()
        );

        assertEquals(
                5,
                resultadoSemana2.escala()
                        .getDesignacoes()
                        .size()
        );

        assertNotEquals(
                resultadoSemana1.escala()
                        .getDesignacoes()
                        .size(),
                resultadoSemana2.escala()
                        .getDesignacoes()
                        .size()
        );
    }


    @Test
    void deveAplicarRodizioNasQuatroSemanas() {

        List<Pessoa> pessoas = criarPessoas(8);

        List<Parte> partes =
                criarPartesVariaveisComPrefixo(
                        2,
                        "Parte"
                );

        List<Escala> escalas = new ArrayList<>();

        for (int semana = 0; semana < 4; semana++) {

            LocalDate data =
                    LocalDate.of(2026, 9, 3)
                            .plusWeeks(semana);

            ResultadoGeracaoEscala resultado =
                    gerador.gerarEscala(
                            data,
                            partes,
                            pessoas
                    );

            assertTrue(
                    resultado.erros().isEmpty(),
                    "A semana " + (semana + 1)
                            + " não deveria apresentar erros."
            );

            assertEquals(
                    2,
                    resultado.escala()
                            .getDesignacoes()
                            .size()
            );

            escalas.add(resultado.escala());
        }

        /*
         * Verifica que a geração não utiliza sempre
         * exatamente a mesma pessoa em todas as semanas.
         */
        Pessoa primeiraPessoaSemana1 =
                escalas.get(0)
                        .getDesignacoes()
                        .get(0)
                        .responsavel();

        boolean encontrouOutraPessoa = false;

        for (int i = 1; i < escalas.size(); i++) {

            for (Designacao designacao :
                    escalas.get(i).getDesignacoes()) {

                if (!designacao.responsavel()
                        .equals(primeiraPessoaSemana1)) {

                    encontrouOutraPessoa = true;
                    break;
                }
            }

            if (encontrouOutraPessoa) {
                break;
            }
        }

        assertTrue(
                encontrouOutraPessoa,
                "O rodízio deveria utilizar participantes diferentes ao longo das semanas."
        );

        /*
         * Verifica que cada semana possui designações
         * válidas e que a data da escala corresponde
         * à semana gerada.
         */
        for (int i = 0; i < escalas.size(); i++) {

            LocalDate dataEsperada =
                    LocalDate.of(2026, 9, 3)
                            .plusWeeks(i);

            assertEquals(
                    dataEsperada,
                    escalas.get(i).getData()
            );

            assertFalse(
                    escalas.get(i)
                            .getDesignacoes()
                            .isEmpty()
            );
        }
    }


    private List<Pessoa> criarPessoas(int quantidade) {

        List<Pessoa> pessoas = new ArrayList<>();

        for (int i = 1; i <= quantidade; i++) {

            Pessoa pessoa =
                    new Pessoa(
                            "Pessoa " + i,
                            Sexo.MASCULINO,
                            true,
                            true,
                            true,
                            false,
                            true,
                            false,
                            false,
                            false,
                            Privilegio.PUBLICADOR,
                            NivelLeitura.BASICO
                    );

            pessoas.add(
                    pessoaDAO.salvar(pessoa)
            );
        }

        return pessoas;
    }


    private List<Parte> criarPartesVariaveis(
            int quantidade
    ) {

        return criarPartesVariaveisComPrefixo(
                quantidade,
                "Parte"
        );
    }


    private List<Parte> criarPartesVariaveisComPrefixo(
            int quantidade,
            String prefixo
    ) {

        List<Parte> partes = new ArrayList<>();

        for (int i = 1; i <= quantidade; i++) {

            Parte parte =
                    new Parte(
                            prefixo + " " + i,
                            TipoParte.LEITURA,
                            Privilegio.PUBLICADOR,
                            false,
                            SexoPermitido.AMBOS,
                            1,
                            false,
                            NivelLeitura.BASICO,
                            SecaoParte.MINISTERIO,
                            TipoVariacaoParte.VARIAVEL,
                            false,
                            List.of(
                                    TipoParticipacao.RESPONSAVEL
                            )
                    );

            partes.add(
                    parteDAO.salvar(parte)
            );
        }

        return partes;
    }
}