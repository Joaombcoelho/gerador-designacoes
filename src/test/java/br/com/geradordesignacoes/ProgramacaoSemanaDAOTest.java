package br.com.geradordesignacoes;


import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProgramacaoSemanaDAOTest extends BaseDAOTest {

    private final ProgramacaoSemanaDAO programacaoSemanaDAO =
            new ProgramacaoSemanaDAO();

    private final ParteDAO parteDAO =
            new ParteDAO();


    @Test
    void deveSalvarEBuscarProgramacaoPorData() {

        LocalDate data =
                LocalDate.of(
                        2026,
                        8,
                        23
                );


        Parte parte =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );


        ProgramacaoSemana programacao =
                new ProgramacaoSemana(
                        data
                );


        programacao.adicionarParte(
                new ProgramacaoParte(
                        parte,
                        1,
                        "Tema de teste"
                )
        );


        ProgramacaoSemana salva =
                programacaoSemanaDAO.salvar(
                        programacao
                );


        assertNotNull(
                salva.id()
        );


        ProgramacaoSemana encontrada =
                programacaoSemanaDAO.buscarPorData(
                        data
                );


        assertNotNull(
                encontrada
        );


        assertEquals(
                salva.id(),
                encontrada.id()
        );


        assertEquals(
                data,
                encontrada.data()
        );


        assertEquals(
                1,
                encontrada.partes().size()
        );


        assertEquals(
                "Tema de teste",
                encontrada.partes()
                        .get(0)
                        .getTema()
        );
    }


    @Test
    void deveSalvarProgramacaoComVariasPartesNaOrdem() {

        LocalDate data =
                LocalDate.of(
                        2026,
                        8,
                        30
                );


        Parte parte1 =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );

        Parte parte2 =
                obterParteInicial(
                        "JOIAS_ESPIRITUAIS"
                );

        Parte parte3 =
                obterParteInicial(
                        "LEITURA"
                );


        ProgramacaoSemana programacao =
                new ProgramacaoSemana(
                        data
                );


        programacao.adicionarParte(
                new ProgramacaoParte(
                        parte1,
                        1,
                        "Tema 1"
                )
        );


        programacao.adicionarParte(
                new ProgramacaoParte(
                        parte2,
                        2,
                        null
                )
        );


        programacao.adicionarParte(
                new ProgramacaoParte(
                        parte3,
                        3,
                        null
                )
        );


        programacaoSemanaDAO.salvar(
                programacao
        );


        ProgramacaoSemana encontrada =
                programacaoSemanaDAO.buscarPorData(
                        data
                );


        assertNotNull(
                encontrada
        );


        List<ProgramacaoParte> partes =
                encontrada.partes();


        assertEquals(
                3,
                partes.size()
        );


        assertEquals(
                1,
                partes.get(0).getOrdem()
        );

        assertEquals(
                2,
                partes.get(1).getOrdem()
        );

        assertEquals(
                3,
                partes.get(2).getOrdem()
        );


        assertEquals(
                "Tema 1",
                partes.get(0).getTema()
        );


        assertNull(
                partes.get(1).getTema()
        );


        assertNull(
                partes.get(2).getTema()
        );
    }


    @Test
    void deveRetornarNullQuandoNaoExistirProgramacaoNaData() {

        LocalDate data =
                LocalDate.of(
                        2099,
                        1,
                        1
                );


        ProgramacaoSemana encontrada =
                programacaoSemanaDAO.buscarPorData(
                        data
                );


        assertNull(
                encontrada
        );
    }


    @Test
    void deveExcluirProgramacaoESuasPartes() {

        LocalDate data =
                LocalDate.of(
                        2026,
                        9,
                        6
                );


        Parte parte =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );


        ProgramacaoSemana programacao =
                new ProgramacaoSemana(
                        data
                );


        programacao.adicionarParte(
                new ProgramacaoParte(
                        parte,
                        1,
                        "Tema de teste"
                )
        );


        ProgramacaoSemana salva =
                programacaoSemanaDAO.salvar(
                        programacao
                );


        programacaoSemanaDAO.excluir(
                salva.id()
        );


        assertNull(
                programacaoSemanaDAO.buscarPorData(
                        data
                )
        );


        ProgramacaoParteDAO parteDAO =
                new ProgramacaoParteDAO();


        assertTrue(
                parteDAO.listarPorSemana(
                        salva.id()
                ).isEmpty()
        );
    }


    private Parte obterParteInicial(
            String tipo
    ) {

        Optional<Parte> parte =
                parteDAO.listarTodos()
                        .stream()
                        .filter(p ->
                                p.getTipo()
                                        .name()
                                        .equals(tipo)
                        )
                        .findFirst();


        assertTrue(
                parte.isPresent(),
                "Parte inicial não encontrada: " + tipo
        );


        return parte.get();
    }
}