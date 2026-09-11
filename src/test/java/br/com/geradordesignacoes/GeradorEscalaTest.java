package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GeradorEscalaTest extends BaseDAOTest {

    private static final LocalDate DATA =
            LocalDate.of(2026, 9, 10);

    @Test
    void deveGerarEscalaCompleta() {

        List<Pessoa> pessoas = criarPessoasParaGeracao();

        Parte leitura = criarLeitura();
        Parte discurso = criarDiscurso();
        Parte demonstracao = criarDemonstracao();

        List<Parte> partes = List.of(
                leitura,
                discurso,
                demonstracao
        );

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        partes,
                        pessoas
                );

        assertFalse(
                resultado.possuiErros(),
                "A geração não deveria apresentar erros: "
                        + resultado.erros()
        );

        assertEquals(
                3,
                resultado.escala().getDesignacoes().size(),
                "As três partes deveriam gerar três designações."
        );

        assertEquals(
                4,
                resultado.participacoes().size(),
                "Leitura + discurso + responsável/ajudante da demonstração = 4 participações."
        );
    }


    @Test
    void deveGerarDemonstracaoComResponsavelEAjudante() {

        Pessoa responsavel =
                criarPessoa(
                        "Responsável",
                        true,
                        true,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Pessoa ajudante =
                criarPessoa(
                        "Ajudante",
                        false,
                        true,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Parte demonstracao =
                criarDemonstracao();

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(demonstracao),
                        List.of(
                                responsavel,
                                ajudante
                        )
                );

        assertFalse(
                resultado.possuiErros(),
                "A demonstração deveria ser gerada."
        );

        assertEquals(
                1,
                resultado.escala().getDesignacoes().size()
        );

        Designacao designacao =
                resultado.escala()
                        .getDesignacoes()
                        .get(0);

        assertNotNull(
                designacao.responsavel(),
                "A demonstração deve possuir responsável."
        );

        assertNotNull(
                designacao.ajudante(),
                "A demonstração deve possuir ajudante."
        );

        assertNotEquals(
                designacao.responsavel(),
                designacao.ajudante(),
                "Responsável e ajudante devem ser pessoas diferentes."
        );

        assertEquals(
                2,
                resultado.participacoes().size(),
                "A demonstração deve registrar duas participações."
        );
    }


    @Test
    void naoDeveDesignarAMesmaPessoaDuasVezesNaMesmaEscala() {

        List<Pessoa> pessoas = List.of(
                criarPessoa(
                        "João",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                ),
                criarPessoa(
                        "Carlos",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                ),
                criarPessoa(
                        "Pedro",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                ),
                criarPessoa(
                        "Lucas",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                )
        );

        Parte leitura =
                criarLeitura();

        Parte discurso =
                criarDiscurso();

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(
                                leitura,
                                discurso
                        ),
                        pessoas
                );

        assertFalse(
                resultado.possuiErros(),
                "A geração deveria ser concluída."
        );

        Set<Pessoa> participantes =
                new HashSet<>();

        resultado.escala()
                .getDesignacoes()
                .forEach(designacao -> {

                    assertTrue(
                            participantes.add(
                                    designacao.responsavel()
                            ),
                            "A mesma pessoa não pode ser responsável por duas partes."
                    );

                    if (designacao.ajudante() != null) {

                        assertTrue(
                                participantes.add(
                                        designacao.ajudante()
                                ),
                                "A mesma pessoa não pode aparecer novamente como ajudante."
                        );
                    }
                });
    }


    @Test
    void naoDeveDesignarPresidenteEmOutraParte() {

        Pessoa presidente =
                criarPessoaCompleta(
                        "Ancião",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        true,
                        false,
                        Privilegio.ANCIAO
                );

        Pessoa outroAnciao =
                criarPessoaCompleta(
                        "Outro Ancião",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        true,
                        false,
                        Privilegio.ANCIAO
                );

        Pessoa leitor =
                criarPessoaCompleta(
                        "Leitor",
                        Sexo.MASCULINO,
                        true,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR
                );

        Parte presidenteParte =
                criarPresidente();

        Parte leitura =
                criarLeitura();

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(
                                presidenteParte,
                                leitura
                        ),
                        List.of(
                                presidente,
                                outroAnciao,
                                leitor
                        )
                );

        assertFalse(
                resultado.possuiErros(),
                "A escala deveria ser gerada."
        );

        Designacao designacaoPresidente =
                resultado.escala()
                        .getDesignacoes()
                        .stream()
                        .filter(d ->
                                d.parte()
                                        .getTipo()
                                        == TipoParte.PRESIDENTE_REUNIAO
                        )
                        .findFirst()
                        .orElseThrow();

        Pessoa pessoaPresidente =
                designacaoPresidente.responsavel();

        long quantidadeOutrasPartes =
                resultado.escala()
                        .getDesignacoes()
                        .stream()
                        .filter(d ->
                                d.parte()
                                        .getTipo()
                                        != TipoParte.PRESIDENTE_REUNIAO
                        )
                        .filter(d ->
                                d.responsavel()
                                        .equals(pessoaPresidente)
                                        ||
                                        (d.ajudante() != null
                                                && d.ajudante()
                                                .equals(pessoaPresidente))
                        )
                        .count();

        assertEquals(
                0,
                quantidadeOutrasPartes,
                "O presidente não deve receber outra função na mesma geração."
        );
    }


    @Test
    void deveRetornarErroQuandoNaoExistePessoaElegivel() {

        Pessoa inativa =
                criarPessoaCompleta(
                        "Pessoa Inativa",
                        Sexo.MASCULINO,
                        false,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR
                );

        Parte leitura =
                criarLeitura();

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(leitura),
                        List.of(inativa)
                );

        assertTrue(
                resultado.possuiErros(),
                "Deveria existir erro quando não há pessoa elegível."
        );

        assertEquals(
                1,
                resultado.erros().size()
        );

        assertEquals(
                0,
                resultado.escala()
                        .getDesignacoes()
                        .size(),
                "Nenhuma designação deveria ser criada."
        );
    }


    @Test
    void deveRetornarErroQuandoNaoExisteDuplaValidaParaDemonstracao() {

        Pessoa responsavel =
                criarPessoa(
                        "Responsável",
                        true,
                        true,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Parte demonstracao =
                criarDemonstracao();

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(demonstracao),
                        List.of(responsavel)
                );

        assertTrue(
                resultado.possuiErros(),
                "Deveria existir erro quando não há duas pessoas para a demonstração."
        );

        assertEquals(
                1,
                resultado.erros().size()
        );

        assertEquals(
                0,
                resultado.escala()
                        .getDesignacoes()
                        .size()
        );

        assertEquals(
                0,
                resultado.participacoes().size()
        );
    }


    @Test
    void deveConsiderarHistoricoAnterior() {

        Pessoa pessoaComHistorico =
                criarPessoa(
                        "Pessoa com histórico",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                );

        Pessoa pessoaSemHistorico =
                criarPessoa(
                        "Pessoa sem histórico",
                        true,
                        false,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                );

        Parte leitura =
                criarLeitura();

        LocalDate dataAnterior =
                DATA.minusWeeks(1);

        ParticipacaoDesignacao historico =
                new ParticipacaoDesignacao(
                        dataAnterior,
                        pessoaComHistorico,
                        leitura,
                        TipoParticipacao.LEITOR
                );

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        DATA,
                        List.of(leitura),
                        List.of(
                                pessoaComHistorico,
                                pessoaSemHistorico
                        ),
                        List.of(historico)
                );

        assertFalse(
                resultado.possuiErros(),
                "A geração deveria ocorrer normalmente."
        );

        assertEquals(
                1,
                resultado.escala()
                        .getDesignacoes()
                        .size()
        );

        Pessoa escolhida =
                resultado.escala()
                        .getDesignacoes()
                        .get(0)
                        .responsavel();

        assertEquals(
                pessoaSemHistorico,
                escolhida,
                "Entre pessoas equivalentes, a pessoa sem histórico deve receber prioridade."
        );
    }


    private List<Pessoa> criarPessoasParaGeracao() {

        Pessoa joao =
                criarPessoaCompleta(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        true,
                        false,
                        Privilegio.ANCIAO
                );

        Pessoa carlos =
                criarPessoaCompleta(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        true,
                        false,
                        false,
                        true,
                        false,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Pessoa lucas =
                criarPessoaCompleta(
                        "Lucas",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.SERVO_MINISTERIAL
                );

        Pessoa pedro =
                criarPessoaCompleta(
                        "Pedro",
                        Sexo.MASCULINO,
                        true,
                        false,
                        true,
                        true,
                        false,
                        false,
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


    private Pessoa criarPessoa(
            String nome,
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            Privilegio privilegio
    ) {

        return criarPessoaCompleta(
                nome,
                Sexo.MASCULINO,
                true,
                responsavel,
                ajudante,
                leitura,
                discurso,
                false,
                false,
                false,
                privilegio
        );
    }


    private Pessoa criarPessoaCompleta(
            String nome,
            Sexo sexo,
            boolean ativo,
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            boolean oracao,
            boolean presidente,
            boolean dirigente,
            Privilegio privilegio
    ) {

        return new Pessoa(
                nome,
                sexo,
                ativo,
                responsavel,
                ajudante,
                leitura,
                discurso,
                oracao,
                presidente,
                dirigente,
                privilegio,
                NivelLeitura.BASICO
        );
    }


    private Parte criarLeitura() {

        return new Parte(
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
    }


    private Parte criarDiscurso() {

        return new Parte(
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
    }


    private Parte criarDemonstracao() {

        return new Parte(
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
    }


    private Parte criarPresidente() {

        return new Parte(
                "Presidente",
                TipoParte.PRESIDENTE_REUNIAO,
                Privilegio.ANCIAO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(
                        TipoParticipacao.PRESIDENTE
                )
        );
    }
}

