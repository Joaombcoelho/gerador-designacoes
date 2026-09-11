package br.com.geradordesignacoes;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EdicaoEscalaControllerTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final EscalaDAO escalaDAO = new EscalaDAO();

    private final EdicaoEscalaController controller =
            new EdicaoEscalaController();


    @Test
    void deveAlterarResponsavelDaDesignacao() {

        Pessoa responsavelOriginal = criarPessoa("Responsavel Original");

        Pessoa novoResponsavel = criarPessoa("Novo Responsavel");

        Parte parte = criarParteSemAjudante();

        Escala escala = criarEscala(
                LocalDate.of(2026, 9, 3),
                new Designacao(
                        LocalDate.of(2026, 9, 3),
                        parte,
                        responsavelOriginal,
                        null
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(escala.getId());

        controller.salvarAlteracoes(
                designacao.id(),
                novoResponsavel,
                null
        );

        Escala atualizada =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow();

        assertEquals(
                novoResponsavel.getId(),
                atualizada.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }


    @Test
    void deveAlterarAjudanteDaDesignacao() {

        Pessoa responsavel = criarPessoa("Responsavel");

        Pessoa ajudanteOriginal = criarPessoa("Ajudante Original");

        Pessoa novoAjudante = criarPessoa("Novo Ajudante");

        Parte parte = criarParteComAjudante();

        Escala escala = criarEscala(
                LocalDate.of(2026, 9, 3),
                new Designacao(
                        LocalDate.of(2026, 9, 3),
                        parte,
                        responsavel,
                        ajudanteOriginal
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(escala.getId());

        controller.salvarAlteracoes(
                designacao.id(),
                responsavel,
                novoAjudante
        );

        Escala atualizada =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow();

        assertEquals(
                novoAjudante.getId(),
                atualizada.getDesignacoes()
                        .get(0)
                        .ajudante()
                        .getId()
        );
    }


    @Test
    void deveDetectarConflitoDePessoaNoMesmoMes() {

        Pessoa pessoa = criarPessoa("Pessoa em Conflito");

        Parte parte1 = criarParteSemAjudante("Parte 1");

        Parte parte2 = criarParteSemAjudante("Parte 2");

        LocalDate primeiraData =
                LocalDate.of(2026, 9, 3);

        LocalDate segundaData =
                LocalDate.of(2026, 9, 10);

        criarEscala(
                primeiraData,
                new Designacao(
                        primeiraData,
                        parte1,
                        pessoa,
                        null
                )
        );

        Escala segundaEscala = criarEscala(
                segundaData,
                new Designacao(
                        segundaData,
                        parte2,
                        pessoa,
                        null
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(segundaEscala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(
                segundaEscala.getId()
        );

        List<LocalDate> conflitos =
                controller.verificarConflitos(
                        designacao.id(),
                        pessoa,
                        null
                );

        assertEquals(
                List.of(primeiraData),
                conflitos
        );
    }


    @Test
    void deveAdicionarNovaParteNaEscala() {

        Pessoa responsavelOriginal =
                criarPessoa("Responsavel Original");

        Pessoa novoResponsavel =
                criarPessoa("Novo Responsavel");

        Parte parteOriginal =
                criarParteSemAjudante("Parte Original");

        Parte novaParte =
                criarParteSemAjudante("Nova Parte");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscala(
                        data,
                        new Designacao(
                                data,
                                parteOriginal,
                                responsavelOriginal,
                                null
                        )
                );

        controller.selecionarEscala(
                escala.getId()
        );

        controller.adicionarParte(
                novaParte,
                novoResponsavel,
                null
        );

        Escala atualizada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                2,
                atualizada.getDesignacoes().size()
        );

        assertTrue(
                atualizada.getDesignacoes()
                        .stream()
                        .anyMatch(d ->
                                d.parte().equals(novaParte)
                                        &&
                                        d.responsavel().getId()
                                                .equals(novoResponsavel.getId())
                        )
        );
    }


    @Test
    void deveImpedirAdicionarParteDuplicada() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteSemAjudante("Parte Única");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscala(
                        data,
                        new Designacao(
                                data,
                                parte,
                                responsavel,
                                null
                        )
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                null
                        )
                );

        assertEquals(
                "Esta parte já está adicionada nesta escala.",
                excecao.getMessage()
        );
    }


    @Test
    void deveImpedirResponsavelInvalido() {

        Pessoa responsavelValido =
                criarPessoa("Responsavel Válido");

        Pessoa responsavelInvalido =
                criarPessoaInativo("Responsavel Inativo");

        Parte parte =
                criarParteSemAjudante("Parte");

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavelInvalido,
                                null
                        )
                );

        assertEquals(
                "O responsável selecionado não pode realizar esta parte.",
                excecao.getMessage()
        );

        assertNotNull(responsavelValido);
    }


    @Test
    void deveImpedirAjudanteInvalido() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Pessoa ajudanteInvalido =
                criarPessoaInativo("Ajudante Inativo");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                ajudanteInvalido
                        )
                );

        assertEquals(
                "O ajudante selecionado não pode realizar esta parte.",
                excecao.getMessage()
        );
    }


    @Test
    void deveExigirAjudanteQuandoParteNecessita() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                null
                        )
                );

        assertEquals(
                "Esta parte exige um ajudante.",
                excecao.getMessage()
        );
    }


    @Test
    void deveImpedirResponsavelIgualAjudante() {

        Pessoa pessoa =
                criarPessoa("Mesma Pessoa");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                pessoa,
                                pessoa
                        )
                );

        assertEquals(
                "O responsável e o ajudante devem ser pessoas diferentes.",
                excecao.getMessage()
        );
    }


    @Test
    void devePersistirNovaDesignacaoAoAdicionarParte() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteSemAjudante("Nova Parte");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscalaVazia(data);

        controller.selecionarEscala(
                escala.getId()
        );

        controller.adicionarParte(
                parte,
                responsavel,
                null
        );

        Escala recuperada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                1,
                recuperada.getDesignacoes().size()
        );

        Designacao designacao =
                recuperada.getDesignacoes()
                        .get(0);

        assertNotNull(
                designacao.id()
        );

        assertEquals(
                data,
                designacao.data()
        );

        assertEquals(
                parte.getId(),
                designacao.parte().getId()
        );

        assertEquals(
                responsavel.getId(),
                designacao.responsavel().getId()
        );

        assertNull(
                designacao.ajudante()
        );
    }


    private Pessoa criarPessoa(String nome) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return pessoaDAO.salvar(pessoa);
    }


    private Pessoa criarPessoaInativo(String nome) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        false,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return pessoaDAO.salvar(pessoa);
    }


    private Parte criarParteSemAjudante() {

        return criarParteSemAjudante("Parte");
    }


    private Parte criarParteSemAjudante(String nome) {

        Parte parte =
                new Parte(
                        nome,
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL
                        )
                );

        return parteDAO.salvar(parte);
    }


    private Parte criarParteComAjudante() {

        Parte parte =
                new Parte(
                        "Demonstracao",
                        TipoParte.DEMONSTRACAO,
                        Privilegio.PUBLICADOR,
                        true,
                        SexoPermitido.AMBOS,
                        2,
                        true,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                );

        return parteDAO.salvar(parte);
    }


    private Escala criarEscala(
            LocalDate data,
            Designacao designacao
    ) {

        return escalaDAO.salvar(
                new Escala(
                        data,
                        List.of(designacao)
                )
        );
    }


    private Escala criarEscalaVazia(
            LocalDate data
    ) {

        return escalaDAO.salvar(
                new Escala(
                        data,
                        List.of()
                )
        );
    }

}