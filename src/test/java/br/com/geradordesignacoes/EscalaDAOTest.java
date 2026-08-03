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
                        .getResponsavel()
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
}