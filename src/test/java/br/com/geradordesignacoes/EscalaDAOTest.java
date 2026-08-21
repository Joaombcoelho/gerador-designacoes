package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EscalaDAOTest extends BaseDAOTest {


    private final EscalaDAO escalaDAO = new EscalaDAO();


    @Test
    void deveSalvarUmaEscala() {

        Escala escala = criarEscalaSemDesignacoes();


        Escala salva = escalaDAO.salvar(escala);


        assertNotNull(salva.getId());
        assertTrue(salva.getId() > 0);
    }


    @Test
    void deveBuscarEscalaPorId() {

        Escala escala = criarEscalaSemDesignacoes();

        Escala salva = escalaDAO.salvar(escala);


        Escala encontrada =
                escalaDAO.buscarPorId(salva.getId())
                        .orElseThrow();


        assertEquals(
                salva.getId(),
                encontrada.getId()
        );

        assertEquals(
                salva.getData(),
                encontrada.getData()
        );
    }


    @Test
    void deveRetornarVazioQuandoEscalaNaoExiste() {

        assertTrue(
                escalaDAO.buscarPorId(999)
                        .isEmpty()
        );
    }


    @Test
    void deveListarTodasAsEscalas() {


        escalaDAO.salvar(
                criarEscalaSemDesignacoes()
        );


        escalaDAO.salvar(
                criarEscalaSemDesignacoes()
        );


        List<Escala> escalas =
                escalaDAO.listarTodas();


        assertEquals(
                2,
                escalas.size()
        );
    }


    @Test
    void deveExcluirUmaEscala() {


        Escala escala =
                escalaDAO.salvar(
                        criarEscalaSemDesignacoes()
                );


        escalaDAO.excluir(
                escala.getId()
        );


        assertTrue(
                escalaDAO.buscarPorId(
                        escala.getId()
                ).isEmpty()
        );
    }


    @Test
    void deveSalvarDesignacoesDaEscala() {


        Pessoa pessoa =
                criarPessoa();


        Parte parte =
                criarParte();


        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );


        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );


        Escala salva =
                escalaDAO.salvar(escala);


        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();


        assertEquals(
                1,
                encontrada.getDesignacoes().size()
        );


        assertEquals(
                pessoa.getId(),
                encontrada.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveAtualizarResponsavelDaDesignacao() {

        Pessoa responsavelOriginal =
                criarPessoa();

        Pessoa novoResponsavel =
                new Pessoa(
                        "Novo Responsavel",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novoResponsavel =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novoResponsavel);

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavelOriginal,
                        null
                );

        Escala escala =
                escalaDAO.salvar(
                        new Escala(
                                LocalDate.now(),
                                List.of(designacao)
                        )
                );

        Escala escalaSalva =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                escalaSalva.getDesignacoes()
                        .get(0);

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                novoResponsavel.getId(),
                null
        );

        Escala encontrada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                novoResponsavel.getId(),
                encontrada.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveAtualizarAjudanteDaDesignacao() {

        Pessoa responsavel =
                criarPessoa();

        Pessoa ajudanteOriginal =
                new Pessoa(
                        "Ajudante Original",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        ajudanteOriginal =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(ajudanteOriginal);

        Pessoa novoAjudante =
                new Pessoa(
                        "Novo Ajudante",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novoAjudante =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novoAjudante);

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

        parte =
                new br.com.geradordesignacoes.dao.ParteDAO()
                        .salvar(parte);

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavel,
                        ajudanteOriginal
                );

        Escala escala =
                escalaDAO.salvar(
                        new Escala(
                                LocalDate.now(),
                                List.of(designacao)
                        )
                );

        Escala escalaSalva =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                escalaSalva.getDesignacoes()
                        .get(0);

        assertEquals(
                ajudanteOriginal.getId(),
                designacaoSalva.ajudante().getId()
        );

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                responsavel.getId(),
                novoAjudante.getId()
        );

        Escala encontrada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                novoAjudante.getId(),
                encontrada.getDesignacoes()
                        .get(0)
                        .ajudante()
                        .getId()
        );
    }

    private Escala criarEscalaSemDesignacoes() {

        return new Escala(
                LocalDate.now(),
                List.of()
        );
    }


    private Pessoa criarPessoa() {

        Pessoa pessoa =
                new Pessoa(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return new br.com.geradordesignacoes.dao.PessoaDAO()
                .salvar(pessoa);
    }


    private Parte criarParte() {

        Parte parte =
                new Parte(

                        "Leitura",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                );


        return new br.com.geradordesignacoes.dao.ParteDAO()
                .salvar(parte);
    }

    @Test
    void deveCriarDesignacaoSemId() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        assertNull(
                designacao.id()
        );
    }


    @Test
    void deveCriarDesignacaoComId() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        10,
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        assertEquals(
                10,
                designacao.id()
        );
    }
    @Test
    void deveRecuperarIdDaDesignacaoAoBuscarEscala() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );

        Escala salva =
                escalaDAO.salvar(escala);

        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoEncontrada =
                encontrada.getDesignacoes()
                        .get(0);

        assertNotNull(
                designacaoEncontrada.id()
        );

        assertTrue(
                designacaoEncontrada.id() > 0
        );
    }

    @Test
    void deveAtualizarDesignacao() {

        Pessoa responsavelOriginal =
                criarPessoa();

        Pessoa novoResponsavel =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavelOriginal,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );

        Escala salva =
                escalaDAO.salvar(escala);

        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                encontrada.getDesignacoes()
                        .get(0);

        assertNotNull(
                designacaoSalva.id()
        );

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                novoResponsavel.getId(),
                null
        );

        Escala escalaAtualizada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoAtualizada =
                escalaAtualizada.getDesignacoes()
                        .get(0);

        assertEquals(
                novoResponsavel.getId(),
                designacaoAtualizada.responsavel().getId()
        );

        assertNull(
                designacaoAtualizada.ajudante()
        );
    }

    @Test
    void deveAtualizarUmaEscala() {

        Pessoa pessoaOriginal =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacaoOriginal =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoaOriginal,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacaoOriginal)
                );

        Escala salva =
                escalaDAO.salvar(escala);


        Pessoa novaPessoa =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novaPessoa =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novaPessoa);


        Designacao novaDesignacao =
                new Designacao(
                        escala.getData(),
                        parte,
                        novaPessoa,
                        null
                );


        escala.substituirDesignacao(
                0,
                novaDesignacao
        );


        escalaDAO.atualizar(
                escala
        );


        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();


        assertEquals(
                1,
                encontrada.getDesignacoes().size()
        );


        assertEquals(
                novaPessoa.getId(),
                encontrada.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveImpedirAtualizacaoDeEscalaSemId() {

        Escala escala =
                criarEscalaSemDesignacoes();


        assertThrows(
                IllegalArgumentException.class,
                () -> escalaDAO.atualizar(escala)
        );
    }


}