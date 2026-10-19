package br.com.geradordesignacoes.database;

import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportacaoProgramacaoTransacionalTest {

    @TempDir
    Path diretorioTemporario;

    private ConnectionFactory.TestContext contexto;
    private ProgramacaoSemanaDAO dao;

    @BeforeEach
    void prepararBanco() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("importacao.db"));
        DatabaseInitializer.initialize();
        dao = new ProgramacaoSemanaDAO();
    }

    @AfterEach
    void fecharContexto() throws Exception {
        if (contexto != null) {
            contexto.close();
        }
    }

    @Test
    void deveSalvarTodasAsProgramacoesNovas() {
        var primeira = new ProgramacaoSemana(LocalDate.of(2027, 1, 6));
        var segunda = new ProgramacaoSemana(LocalDate.of(2027, 1, 13));

        List<ProgramacaoSemana> salvas = dao.salvarTodasNovas(List.of(primeira, segunda));

        assertEquals(2, salvas.size());
        assertNotNull(dao.buscarPorData(primeira.data()));
        assertNotNull(dao.buscarPorData(segunda.data()));
    }

    @Test
    void deveRecusarLoteComDataExistenteSemGravarNenhumaSemanaNova() {
        LocalDate dataExistente = LocalDate.of(2027, 2, 3);
        LocalDate dataNova = LocalDate.of(2027, 2, 10);
        dao.salvar(new ProgramacaoSemana(dataExistente));
        // Torna a programação existente não vazia: o DAO pode reutilizar
        // rascunhos vazios, mas deve recusar datas já configuradas.
        dao.atualizarDadosS140(dataExistente, null, "Cântico 1", null, null);

// Marca a programação como configurada, impedindo a reutilização
// como rascunho vazio durante a importação.
        dao.atualizarDadosS140(
                dataExistente,
                null,
                "1",
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> dao.salvarTodasNovas(List.of(
                new ProgramacaoSemana(dataNova),
                new ProgramacaoSemana(dataExistente)
        )));

        assertNull(dao.buscarPorData(dataNova));
        assertNotNull(dao.buscarPorData(dataExistente));
    }
}
