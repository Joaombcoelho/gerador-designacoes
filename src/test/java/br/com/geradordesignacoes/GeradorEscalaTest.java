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

