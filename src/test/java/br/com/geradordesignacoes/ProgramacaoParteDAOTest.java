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

class ProgramacaoParteDAOTest extends BaseDAOTest {

    private final ProgramacaoParteDAO programacaoParteDAO =
            new ProgramacaoParteDAO();

    private final ProgramacaoSemanaDAO programacaoSemanaDAO =
            new ProgramacaoSemanaDAO();

    private final ParteDAO parteDAO =
            new ParteDAO();


    @Test
    void deveSalvarEListarParteDaProgramacao() {

        int semanaId = criarProgramacaoSemana();


        Parte parte =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );


        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(
                        parte,
                        1,
                        "Tema de teste"
                );


        programacaoParteDAO.salvar(
                semanaId,
                programacaoParte
        );


        List<ProgramacaoParte> partes =
                programacaoParteDAO.listarPorSemana(
                        semanaId
                );


        assertEquals(
                1,
                partes.size()
        );


        ProgramacaoParte resultado =
                partes.get(0);


        assertEquals(
                parte.getId(),
                resultado.getParte().getId()
        );

        assertEquals(
                1,
                resultado.getOrdem()
        );

        assertEquals(
                "Tema de teste",
                resultado.getTema()
        );
    }


    @Test
    void deveAtualizarTemaDaParte() {

        int semanaId = criarProgramacaoSemana();


        Parte parte =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );


        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(
                        parte,
                        1,
                        "Tema antigo"
                );


        programacaoParteDAO.salvar(
                semanaId,
                programacaoParte
        );


        List<ProgramacaoParte> partes =
                programacaoParteDAO.listarPorSemana(
                        semanaId
                );


        ProgramacaoParte salva =
                partes.get(0);


        programacaoParteDAO.atualizarTema(
                salva.getId(),
                "Tema novo"
        );


        List<ProgramacaoParte> atualizadas =
                programacaoParteDAO.listarPorSemana(
                        semanaId
                );


        assertEquals(
                "Tema novo",
                atualizadas.get(0).getTema()
        );
    }


    @Test
    void deveExcluirParteDaProgramacao() {

        int semanaId = criarProgramacaoSemana();


        Parte parte =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );


        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(
                        parte,
                        1,
                        "Tema de teste"
                );


        programacaoParteDAO.salvar(
                semanaId,
                programacaoParte
        );


        List<ProgramacaoParte> partes =
                programacaoParteDAO.listarPorSemana(
                        semanaId
                );


        assertEquals(
                1,
                partes.size()
        );


        programacaoParteDAO.excluir(
                semanaId,
                parte.getId()
        );


        List<ProgramacaoParte> restantes =
                programacaoParteDAO.listarPorSemana(
                        semanaId
                );


        assertTrue(
                restantes.isEmpty()
        );
    }


    @Test
    void deveExcluirTodasAsPartesDaSemana() {

        int semanaId = criarProgramacaoSemana();


        Parte parte1 =
                obterParteInicial(
                        "DISCURSO_TESOUROS"
                );

        Parte parte2 =
                obterParteInicial(
                        "JOIAS_ESPIRITUAIS"
                );


        programacaoParteDAO.salvar(
                semanaId,
                new ProgramacaoParte(
                        parte1,
                        1,
                        "Tema 1"
                )
        );


        programacaoParteDAO.salvar(
                semanaId,
                new ProgramacaoParte(
                        parte2,
                        2,
                        null
                )
        );


        assertEquals(
                2,
                programacaoParteDAO
                        .listarPorSemana(semanaId)
                        .size()
        );


        programacaoParteDAO.excluirPorSemana(
                semanaId
        );


        assertTrue(
                programacaoParteDAO
                        .listarPorSemana(semanaId)
                        .isEmpty()
        );
    }


    private int criarProgramacaoSemana() {

        ProgramacaoSemana programacao =
                new ProgramacaoSemana(
                        LocalDate.of(
                                2026,
                                10,
                                1
                        )
                );


        ProgramacaoSemana salva =
                programacaoSemanaDAO.salvar(
                        programacao
                );


        assertNotNull(
                salva.id()
        );


        return salva.id();
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