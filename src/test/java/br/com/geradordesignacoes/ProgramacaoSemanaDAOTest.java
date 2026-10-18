package br.com.geradordesignacoes;


import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
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
    void devePersistirCopiaDasSalasNaProgramacao() {
        LocalDate data = LocalDate.of(2026, 10, 4);
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                null,
                data,
                List.of(),
                true,
                false
        );

        programacaoSemanaDAO.salvar(programacao);

        ProgramacaoSemana encontrada =
                programacaoSemanaDAO.buscarPorData(data);

        assertTrue(encontrada.salaBHabilitada());
        assertFalse(encontrada.salaCHabilitada());
    }


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
                        .getFirst()
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
                partes.getFirst().getOrdem()
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
                partes.getFirst().getTema()
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


    @Test
    void devePersistirCanticosEConselheiroDaSalaB() {
        LocalDate data = LocalDate.of(2026, 10, 7);
        int pessoaId = new br.com.geradordesignacoes.dao.PessoaDAO()
                .listarTodos().getFirst().getId();
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                null,
                data,
                List.of(),
                true,
                false,
                pessoaId,
                "103",
                "12",
                "45"
        );

        programacaoSemanaDAO.salvar(programacao);

        ProgramacaoSemana encontrada = programacaoSemanaDAO.buscarPorData(data);

        assertNotNull(encontrada);
        assertEquals(pessoaId, encontrada.conselheiroSalaBId());
        assertEquals("103", encontrada.canticoInicial());
        assertEquals("12", encontrada.canticoMeio());
        assertEquals("45", encontrada.canticoFinal());
        assertTrue(encontrada.salaBHabilitada());
        assertFalse(encontrada.salaCHabilitada());
    }

    @Test
    void importaSobreRascunhoVazioPreservandoIdDaData() {
        LocalDate data = LocalDate.of(2026, 12, 9);
        ProgramacaoSemana rascunho = new ProgramacaoSemanaService().obterOuCriar(data);
        Parte parteVariavel = obterParteInicial("DISCURSO_TESOUROS");
        ProgramacaoSemana importada = new ProgramacaoSemana(
                null, data,
                List.of(new ProgramacaoParte(parteVariavel, 1, "Tema importado")),
                false, false, null, "101", "102", "103");

        List<ProgramacaoSemana> salvas =
                programacaoSemanaDAO.salvarTodasNovas(List.of(importada));

        assertEquals(rascunho.id(), salvas.getFirst().id());
        ProgramacaoSemana recarregada = programacaoSemanaDAO.buscarPorData(data);
        assertEquals("101", recarregada.canticoInicial());
        assertEquals(1, recarregada.partes().size());
        assertEquals("Tema importado", recarregada.partes().getFirst().getTema());
        assertFalse(programacaoSemanaDAO.podeReutilizarRascunhoVazio(data));
    }

    @Test
    void reconheceRascunhoVazioCriadoAoAbrirProgramacao() {
        LocalDate data = LocalDate.of(2026, 12, 2);
        new ProgramacaoSemanaService().obterOuCriar(data);

        assertTrue(programacaoSemanaDAO.podeReutilizarRascunhoVazio(data));

        programacaoSemanaDAO.atualizarDadosS140(data, null, "103", null, null);
        assertFalse(programacaoSemanaDAO.podeReutilizarRascunhoVazio(data));
    }

    @Test
    void devePermitirCamposS140VaziosEmProgramacaoExistente() {
        LocalDate data = LocalDate.of(2026, 10, 14);
        ProgramacaoSemana programacao = new ProgramacaoSemana(data);

        programacaoSemanaDAO.salvar(programacao);

        ProgramacaoSemana encontrada = programacaoSemanaDAO.buscarPorData(data);

        assertNotNull(encontrada);
        assertNull(encontrada.conselheiroSalaBId());
        assertNull(encontrada.canticoInicial());
        assertNull(encontrada.canticoMeio());
        assertNull(encontrada.canticoFinal());
    }

    @Test
    void deveAtualizarDadosS140SemAlterarPartesDaProgramacao() {
        LocalDate data = LocalDate.of(2026, 10, 28);
        Parte parte = obterParteInicial("DISCURSO_TESOUROS");
        ProgramacaoSemana programacao = new ProgramacaoSemana(data);
        programacao.adicionarParte(new ProgramacaoParte(parte, 1, "Tema preservado"));
        programacaoSemanaDAO.salvar(programacao);

        programacaoSemanaDAO.atualizarDadosS140(data, null, "103", "  ", "45");

        ProgramacaoSemana encontrada = programacaoSemanaDAO.buscarPorData(data);
        assertNotNull(encontrada);
        assertEquals("103", encontrada.canticoInicial());
        assertNull(encontrada.canticoMeio());
        assertEquals("45", encontrada.canticoFinal());
        assertEquals(1, encontrada.partes().size());
        assertEquals("Tema preservado", encontrada.partes().getFirst().getTema());
    }

    @Test
    void deveNormalizarCanticosVaziosComoNulos() {
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                null,
                LocalDate.of(2026, 10, 21),
                List.of(),
                false,
                false,
                null,
                "  ",
                "",
                null
        );

        assertNull(programacao.canticoInicial());
        assertNull(programacao.canticoMeio());
        assertNull(programacao.canticoFinal());
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