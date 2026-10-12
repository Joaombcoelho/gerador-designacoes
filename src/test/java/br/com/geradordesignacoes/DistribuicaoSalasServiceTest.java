package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.DistribuicaoSalasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DistribuicaoSalasServiceTest {

    private DistribuicaoSalasService service;

    @BeforeEach
    void setUp() {
        service = new DistribuicaoSalasService();
    }

    @Test
    void deveIncluirLeituraDaBibliaNaDistribuicaoAuxiliar() {
        Parte leitura = criarParte(
                "Leitura da Bíblia",
                TipoParte.LEITURA
        );

        assertTrue(
                service.partePodeSerDistribuida(leitura)
        );
    }

    @Test
    void deveIncluirDemonstracoesNaDistribuicaoAuxiliar() {
        List<Parte> demonstracoes = List.of(
                criarParte("Iniciando Conversas",
                        TipoParte.INICIANDO_CONVERSAS),
                criarParte("Cultivando Interesse",
                        TipoParte.CULTIVANDO_INTERESSE),
                criarParte("Fazendo Discípulos",
                        TipoParte.FAZENDO_DISCIPULOS),
                criarParte("Explicando suas crenças",
                        TipoParte.EXPLICANDO_SUAS_CRENCAS)
        );

        for (Parte parte : demonstracoes) {
            assertTrue(
                    service.partePodeSerDistribuida(parte),
                    "A parte deveria ser elegível: "
                            + parte.getNome()
            );
        }
    }

    @Test
    void naoDeveIncluirOQueVoceDiriaNasSalasAuxiliares() {
        Parte parte = criarParte(
                "O Que Você Diria?",
                TipoParte.O_QUE_VOCE_DIRIA
        );

        assertFalse(
                service.partePodeSerDistribuida(parte)
        );
    }

    @Test
    void naoDeveDistribuirParteNula() {
        assertFalse(
                service.partePodeSerDistribuida(null)
        );
    }

    @Test
    void deveAceitarAnciaoAtivoHabilitadoComoResponsavel() {
        Pessoa pessoa = criarPessoa(
                "Ancião",
                true,
                true,
                Privilegio.ANCIAO
        );

        assertTrue(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void deveAceitarServoMinisterialAtivoHabilitadoComoResponsavel() {
        Pessoa pessoa = criarPessoa(
                "Servo ministerial",
                true,
                true,
                Privilegio.SERVO_MINISTERIAL
        );

        assertTrue(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void naoDeveAceitarPublicadorComoResponsavelDaSala() {
        Pessoa pessoa = criarPessoa(
                "Publicador",
                true,
                true,
                Privilegio.PUBLICADOR
        );

        assertFalse(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void naoDeveAceitarBatizadoComoResponsavelDaSala() {
        Pessoa pessoa = criarPessoa(
                "Batizado",
                true,
                true,
                Privilegio.BATIZADO
        );

        assertFalse(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void naoDeveAceitarResponsavelInativo() {
        Pessoa pessoa = criarPessoa(
                "Ancião inativo",
                false,
                true,
                Privilegio.ANCIAO
        );

        assertFalse(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void naoDeveAceitarPessoaSemHabilitacaoParaResponsavel() {
        Pessoa pessoa = criarPessoa(
                "Ancião sem habilitação",
                true,
                false,
                Privilegio.ANCIAO
        );

        assertFalse(
                service.responsavelElegivel(pessoa)
        );
    }

    @Test
    void naoDeveAceitarResponsavelNulo() {
        assertFalse(
                service.responsavelElegivel(null)
        );
    }

    @Test
    void deveIdentificarParticipantesDiferentesEntreAsSalas() {
        Pessoa participanteB = criarPessoa(
                "Participante B",
                true,
                true,
                Privilegio.SERVO_MINISTERIAL
        );

        Pessoa participanteC = criarPessoa(
                "Participante C",
                true,
                true,
                Privilegio.ANCIAO
        );

        assertTrue(
                service.participantesSaoDiferentes(
                        List.of(participanteB),
                        List.of(participanteC)
                )
        );
    }

    @Test
    void naoDevePermitirMesmoParticipanteNasDuasSalas() {
        Pessoa participante = criarPessoa(
                "Participante repetido",
                true,
                true,
                Privilegio.ANCIAO
        );

        assertFalse(
                service.participantesSaoDiferentes(
                        List.of(participante),
                        List.of(participante)
                )
        );
    }

    @Test
    void deveManterTodasAsPartesDaProgramacaoOriginal() {
        List<ProgramacaoParte> programacaoOriginal =
                new ArrayList<>();

        programacaoOriginal.add(
                new ProgramacaoParte(
                        criarParte(
                                "Leitura da Bíblia",
                                TipoParte.LEITURA
                        ),
                        1
                )
        );

        programacaoOriginal.add(
                new ProgramacaoParte(
                        criarParte(
                                "O Que Você Diria?",
                                TipoParte.O_QUE_VOCE_DIRIA
                        ),
                        2
                )
        );

        programacaoOriginal.add(
                new ProgramacaoParte(
                        criarParte(
                                "Iniciando Conversas",
                                TipoParte.INICIANDO_CONVERSAS
                        ),
                        3
                )
        );

        List<ProgramacaoParte> copiaOriginal =
                new ArrayList<>(programacaoOriginal);

        service.partesElegiveis(programacaoOriginal);

        assertEquals(
                copiaOriginal,
                programacaoOriginal,
                "A distribuição auxiliar não deve remover "
                        + "nem alterar partes do Salão Principal."
        );

        assertEquals(3, programacaoOriginal.size());
    }

    private Parte criarParte(
            String nome,
            TipoParte tipo
    ) {
        boolean demonstracao =
                tipo == TipoParte.INICIANDO_CONVERSAS
                        || tipo == TipoParte.CULTIVANDO_INTERESSE
                        || tipo == TipoParte.FAZENDO_DISCIPULOS
                        || tipo == TipoParte.EXPLICANDO_SUAS_CRENCAS;

        List<TipoParticipacao> participacoes =
                demonstracao
                        ? List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
                        : tipo == TipoParte.LEITURA
                        ? List.of(TipoParticipacao.LEITOR)
                        : List.of(TipoParticipacao.PRESIDENTE);

        return new Parte(
                nome,
                tipo,
                Privilegio.PUBLICADOR,
                demonstracao,
                SexoPermitido.AMBOS,
                demonstracao ? 2 : 1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL,
                false,
                participacoes
        );
    }

    private Pessoa criarPessoa(
            String nome,
            boolean ativo,
            boolean podeSerResponsavel,
            Privilegio privilegio
    ) {
        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                ativo,
                podeSerResponsavel,
                true,
                true,
                true,
                false,
                false,
                false,
                privilegio,
                NivelLeitura.BASICO
        );
    }

    @Test
    void devePreservarOrdemDasPartesElegiveis() {
        List<ProgramacaoParte> programacao = List.of(
                new ProgramacaoParte(
                        criarParte("Leitura da Bíblia", TipoParte.LEITURA),
                        1
                ),
                new ProgramacaoParte(
                        criarParte("O Que Você Diria?", TipoParte.O_QUE_VOCE_DIRIA),
                        2
                ),
                new ProgramacaoParte(
                        criarParte("Iniciando Conversas", TipoParte.INICIANDO_CONVERSAS),
                        3
                ),
                new ProgramacaoParte(
                        criarParte("Discurso", TipoParte.DISCURSO),
                        4
                ),
                new ProgramacaoParte(
                        criarParte("Cultivando Interesse", TipoParte.CULTIVANDO_INTERESSE),
                        5
                )
        );

        List<String> nomesElegiveis = service
                .partesElegiveis(programacao)
                .stream()
                .map(item -> item.getParte().getNome())
                .toList();

        assertEquals(
                List.of(
                        "Leitura da Bíblia",
                        "Iniciando Conversas",
                        "Discurso",
                        "Cultivando Interesse"
                ),
                nomesElegiveis
        );
    }

    @Test
    void deveRetornarMesmasPartesElegiveisParaSalasBEC() {
        List<ProgramacaoParte> programacao = List.of(
                new ProgramacaoParte(
                        criarParte("Leitura da Bíblia", TipoParte.LEITURA),
                        1
                ),
                new ProgramacaoParte(
                        criarParte("O Que Você Diria?", TipoParte.O_QUE_VOCE_DIRIA),
                        2
                ),
                new ProgramacaoParte(
                        criarParte("Fazendo Discípulos", TipoParte.FAZENDO_DISCIPULOS),
                        3
                )
        );

        List<ProgramacaoParte> partesSalaB =
                service.partesElegiveis(programacao);

        List<ProgramacaoParte> partesSalaC =
                service.partesElegiveis(programacao);

        assertEquals(
                partesSalaB.stream()
                        .map(item -> item.getParte().getNome())
                        .toList(),
                partesSalaC.stream()
                        .map(item -> item.getParte().getNome())
                        .toList()
        );
    }
    @Test
    void deveAceitarNovaParteCadastradaNaSecaoMinisterio() {
        Parte novaParte = new Parte(
                "Nova parte do Ministério",
                TipoParte.DISCURSO,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL,
                false,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        assertTrue(
                service.partePodeSerDistribuida(novaParte),
                "Novas partes da seção MINISTERIO devem ser elegíveis."
        );
    }

    @Test
    void naoDeveAceitarParteDeOutraSecaoQueNaoSejaLeitura() {
        Parte parteTesouros = new Parte(
                "Discurso dos Tesouros",
                TipoParte.DISCURSO_TESOUROS,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoVariacaoParte.VARIAVEL,
                false,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        assertFalse(
                service.partePodeSerDistribuida(parteTesouros),
                "Partes da seção TESOUROS não devem ser distribuídas nas salas auxiliares."
        );
    }
}
