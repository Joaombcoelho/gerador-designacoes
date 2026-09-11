package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorEscalaRegrasTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();

    private final GeradorEscala gerador =
            new GeradorEscala(new RegrasService());


    @Test
    void deveGerarOracaoInicialComPessoaAutorizada() {

        Pessoa pessoa =
                criarPessoa(
                        "Orador",
                        true,
                        false,
                        false,
                        true,
                        false,
                        Privilegio.ANCIAO,
                        NivelLeitura.BASICO
                );

        Parte oracaoInicial =
                criarParte(
                        "Oração Inicial",
                        TipoParte.ORACAO_INICIAL,
                        false,
                        List.of(
                                TipoParticipacao.ORACAO_INICIAL
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(oracaoInicial),
                        List.of(pessoa)
                );

        assertTrue(resultado.erros().isEmpty());

        assertEquals(
                1,
                resultado.escala().getDesignacoes().size()
        );

        Designacao designacao =
                resultado.escala()
                        .getDesignacoes()
                        .get(0);

        assertEquals(
                pessoa.getId(),
                designacao.responsavel().getId()
        );
    }


    @Test
    void deveGerarOracaoFinalComOProprioPresidente() {

        Pessoa presidente =
                criarPresidente("Presidente");

        Parte partePresidente =
                criarPartePresidente();

        Parte oracaoFinal =
                criarParte(
                        "Oração Final",
                        TipoParte.ORACAO_FINAL,
                        false,
                        List.of(
                                TipoParticipacao.ORACAO_FINAL
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(
                                partePresidente,
                                oracaoFinal
                        ),
                        List.of(presidente)
                );

        assertTrue(resultado.erros().isEmpty());

        assertEquals(
                2,
                resultado.escala()
                        .getDesignacoes()
                        .size()
        );

        assertEquals(
                presidente.getId(),
                resultado.escala()
                        .getPresidente()
                        .getId()
        );

        Designacao designacaoOracao =
                resultado.escala()
                        .getDesignacoes()
                        .get(1);

        assertEquals(
                presidente.getId(),
                designacaoOracao.responsavel().getId()
        );
    }


    @Test
    void deveGerarDirigenteDoEstudo() {

        Pessoa dirigente =
                criarPessoa(
                        "Dirigente",
                        true,
                        false,
                        false,
                        false,
                        true,
                        Privilegio.ANCIAO,
                        NivelLeitura.BASICO
                );

        Pessoa leitor =
                criarPessoa(
                        "Leitor",
                        true,
                        false,
                        true,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.EXPERIENTE
                );

        Parte estudo =
                criarParte(
                        "Estudo Bíblico",
                        TipoParte.DIRIGENTE_ESTUDO,
                        false,
                        List.of(
                                TipoParticipacao.DIRIGENTE,
                                TipoParticipacao.LEITOR
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(estudo),
                        List.of(dirigente, leitor)
                );

        assertTrue(resultado.erros().isEmpty());

        assertEquals(
                1,
                resultado.escala()
                        .getDesignacoes()
                        .size()
        );

        Designacao designacao =
                resultado.escala()
                        .getDesignacoes()
                        .get(0);

        assertEquals(
                dirigente.getId(),
                designacao.responsavel().getId()
        );

        assertEquals(
                leitor.getId(),
                designacao.ajudante().getId()
        );
    }


    @Test
    void dirigenteNaoPodeSerOLeitor() {

        Pessoa pessoa =
                criarPessoa(
                        "Pessoa",
                        true,
                        false,
                        true,
                        false,
                        true,
                        Privilegio.ANCIAO,
                        NivelLeitura.EXPERIENTE
                );

        Parte estudo =
                criarParte(
                        "Estudo Bíblico",
                        TipoParte.DIRIGENTE_ESTUDO,
                        false,
                        List.of(
                                TipoParticipacao.DIRIGENTE,
                                TipoParticipacao.LEITOR
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(estudo),
                        List.of(pessoa)
                );

        assertEquals(
                1,
                resultado.erros().size()
        );

        assertTrue(
                resultado.erros()
                        .get(0)
                        .contains("Estudo Bíblico")
        );

        assertTrue(
                resultado.escala()
                        .getDesignacoes()
                        .isEmpty()
        );
    }


    @Test
    void deveManterParticipantesDistintosNaDemonstracao() {

        Pessoa responsavel =
                criarPessoa(
                        "Responsavel",
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        Pessoa ajudante =
                criarPessoa(
                        "Ajudante",
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        Parte demonstracao =
                criarParte(
                        "Demonstração",
                        TipoParte.DEMONSTRACAO,
                        true,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(demonstracao),
                        List.of(responsavel, ajudante)
                );

        assertTrue(resultado.erros().isEmpty());

        Designacao designacao =
                resultado.escala()
                        .getDesignacoes()
                        .get(0);

        assertNotEquals(
                designacao.responsavel().getId(),
                designacao.ajudante().getId()
        );
    }


    @Test
    void deveRespeitarNivelDeLeitura() {

        Pessoa dirigente =
                criarPessoa(
                        "Dirigente",
                        true,
                        false,
                        false,
                        false,
                        true,
                        Privilegio.ANCIAO,
                        NivelLeitura.BASICO
                );

        Pessoa leitorBasico =
                criarPessoa(
                        "Leitor Básico",
                        true,
                        false,
                        true,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        Parte estudo =
                criarParteComNivelLeitura(
                        "Estudo Bíblico",
                        TipoParte.DIRIGENTE_ESTUDO,
                        NivelLeitura.EXPERIENTE,
                        List.of(
                                TipoParticipacao.DIRIGENTE,
                                TipoParticipacao.LEITOR
                        )
                );

        ResultadoGeracaoEscala resultadoSemExperiente =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(estudo),
                        List.of(dirigente, leitorBasico)
                );

        assertEquals(
                1,
                resultadoSemExperiente.erros().size()
        );

        Pessoa leitorExperiente =
                criarPessoa(
                        "Leitor Experiente",
                        true,
                        false,
                        true,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.EXPERIENTE
                );

        ResultadoGeracaoEscala resultadoComExperiente =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 10),
                        List.of(estudo),
                        List.of(dirigente, leitorExperiente)
                );

        assertTrue(
                resultadoComExperiente.erros().isEmpty()
        );

        assertEquals(
                1,
                resultadoComExperiente.escala()
                        .getDesignacoes()
                        .size()
        );

        assertEquals(
                leitorExperiente.getId(),
                resultadoComExperiente.escala()
                        .getDesignacoes()
                        .get(0)
                        .ajudante()
                        .getId()
        );
    }


    @Test
    void deveRetornarErroQuandoNaoHaCandidato() {

        Pessoa pessoaInadequada =
                criarPessoa(
                        "Pessoa Inadequada",
                        true,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        Parte presidente =
                criarPartePresidente();

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(presidente),
                        List.of(pessoaInadequada)
                );

        assertEquals(
                1,
                resultado.erros().size()
        );

        assertTrue(
                resultado.erros()
                        .get(0)
                        .contains("Presidente")
        );

        assertTrue(
                resultado.escala()
                        .getDesignacoes()
                        .isEmpty()
        );
    }


    @Test
    void deveImpedirParticipanteDeReceberDuasParticipacoesIncompativeis() {

        Pessoa publicador =
                criarPessoa(
                        "Publicador",
                        true,
                        true,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        Parte primeiraParte =
                criarParte(
                        "Primeira Parte",
                        TipoParte.LEITURA,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL
                        )
                );

        Parte segundaParte =
                criarParte(
                        "Segunda Parte",
                        TipoParte.LEITURA,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL
                        )
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 3),
                        List.of(
                                primeiraParte,
                                segundaParte
                        ),
                        List.of(publicador)
                );

        assertEquals(
                1,
                resultado.escala()
                        .getDesignacoes()
                        .size()
        );

        assertEquals(
                1,
                resultado.erros().size()
        );
    }


    private Pessoa criarPessoa(
            String nome,
            boolean ativo,
            boolean responsavel,
            boolean leitura,
            boolean oracao,
            boolean dirigente,
            Privilegio privilegio,
            NivelLeitura nivelLeitura
    ) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        ativo,
                        responsavel,
                        true,
                        leitura,
                        true,
                        oracao,
                        false,
                        dirigente,
                        privilegio,
                        nivelLeitura
                );

        return pessoaDAO.salvar(pessoa);
    }


    private Pessoa criarPresidente(
            String nome
    ) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        true,
                        true,
                        true,
                        true,
                        Privilegio.ANCIAO,
                        NivelLeitura.BASICO
                );

        return pessoaDAO.salvar(pessoa);
    }


    private Parte criarParte(
            String nome,
            TipoParte tipo,
            boolean exigeAjudante,
            List<TipoParticipacao> participacoes
    ) {

        Parte parte =
                new Parte(
                        nome,
                        tipo,
                        Privilegio.PUBLICADOR,
                        exigeAjudante,
                        SexoPermitido.AMBOS,
                        exigeAjudante ? 2 : 1,
                        exigeAjudante,
                        NivelLeitura.BASICO,
                        participacoes
                );

        return parteDAO.salvar(parte);
    }


    private Parte criarParteComNivelLeitura(
            String nome,
            TipoParte tipo,
            NivelLeitura nivelLeitura,
            List<TipoParticipacao> participacoes
    ) {

        Parte parte =
                new Parte(
                        null,
                        nome,
                        tipo,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        2,
                        false,
                        nivelLeitura,
                        null,
                        null,
                        false,
                        participacoes
                );

        return parteDAO.salvar(parte);
    }


    private Parte criarPartePresidente() {

        Parte parte =
                new Parte(
                        "Presidente",
                        TipoParte.PRESIDENTE_REUNIAO,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.PRESIDENTE
                        )
                );

        return parteDAO.salvar(parte);
    }

}