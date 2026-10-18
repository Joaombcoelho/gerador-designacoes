package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EventoEspecialDAO;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.EventoEspecialRegistrado;
import br.com.geradordesignacoes.model.TipoEventoEspecial;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventosEspeciaisDAOTest {

    private TestDatabase banco;
    private EventoEspecialDAO dao;

    @BeforeEach
    void prepararBanco() throws Exception {
        banco = TestDatabase.create();
        DatabaseInitializer.initialize();
        dao = new EventoEspecialDAO();
    }

    @AfterEach
    void fecharBanco() throws Exception {
        if (banco != null) {
            banco.close();
        }
    }

    @Test
    void salvaBuscaEListaEventoPreservandoOsDados() {
        EventoEspecial evento = visita("Tema da visita", "Descrição opcional");
        EventoEspecialRegistrado salvo = dao.salvar(evento);

        assertTrue(salvo.id() > 0);
        assertEquals(evento, salvo.evento());
        assertEquals(salvo, dao.buscarPorId(salvo.id()).orElseThrow());
        assertEquals(List.of(salvo), dao.listarTodos());
    }

    @Test
    void atualizaEventoSemAlterarSeuIdentificador() {
        EventoEspecialRegistrado salvo = dao.salvar(
                visita("Tema original", "Descrição original"));
        EventoEspecial atualizado = visita("Tema atualizado", "Descrição atualizada");

        EventoEspecialRegistrado resultado = dao.atualizar(salvo.id(), atualizado);

        assertEquals(salvo.id(), resultado.id());
        assertEquals(atualizado, dao.buscarPorId(salvo.id()).orElseThrow().evento());
        assertEquals(1, dao.listarTodos().size());
    }

    @Test
    void excluiSomenteRegistroSolicitado() {
        EventoEspecialRegistrado primeiro = dao.salvar(
                visita("Tema 1", null));
        EventoEspecialRegistrado segundo = dao.salvar(
                new EventoEspecial(TipoEventoEspecial.ASSEMBLEIA,
                        java.time.LocalDate.of(2026, 11, 1),
                        java.time.LocalDate.of(2026, 11, 1), null, null));

        assertTrue(dao.excluir(primeiro.id()));
        assertFalse(dao.excluir(primeiro.id()));
        assertTrue(dao.buscarPorId(primeiro.id()).isEmpty());
        assertEquals(List.of(segundo), dao.listarTodos());
    }

    @Test
    void rejeitaAtualizacaoDeEventoInexistente() {
        assertThrows(IllegalArgumentException.class,
                () -> dao.atualizar(999, visita("Tema", null)));
    }

    @Test
    void inicializacaoRepetidaPreservaEventosPersistidos() {
        EventoEspecialRegistrado salvo = dao.salvar(visita("Tema", null));

        assertDoesNotThrow(DatabaseInitializer::initialize);

        assertEquals(salvo, dao.buscarPorId(salvo.id()).orElseThrow());
        assertEquals(1, dao.listarTodos().size());
    }

    private static EventoEspecial visita(String tema, String descricao) {
        return new EventoEspecial(
                TipoEventoEspecial.VISITA_SUPERINTENDENTE,
                java.time.LocalDate.of(2026, 10, 13),
                java.time.LocalDate.of(2026, 10, 13),
                tema,
                descricao);
    }
}
