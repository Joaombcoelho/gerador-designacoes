package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerSemPessoasNominaisTest {

    private TestDatabase banco;
    private PessoaDAO pessoaDAO;

    @BeforeEach
    void prepararBancoTemporario() throws Exception {
        banco = TestDatabase.create();
        pessoaDAO = new PessoaDAO();
    }

    @AfterEach
    void fecharBancoTemporario() throws Exception {
        if (banco != null) {
            banco.close();
        }
    }

    @Test
    void instalacaoNovaCriaEstruturaSemCadastrarPessoasEComIntegridadeValida()
            throws Exception {
        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals(0, contar(connection, "SELECT COUNT(*) FROM pessoa"));
            assertTrue(tabelaExiste(connection, "pessoa"));
            assertTrue(tabelaExiste(connection, "parte"));
            assertTrue(tabelaExiste(connection, "historico_designacoes"));
            assertTrue(tabelaExiste(connection, "escala"));
            assertTrue(tabelaExiste(connection, "designacao"));
            assertTrue(tabelaExiste(connection, "programacao_semana"));
            assertTrue(tabelaExiste(connection, "configuracoes_gerais"));
            assertTrue(tabelaExiste(connection, "evento_especial"));
            assertEquals(1, contar(connection, "PRAGMA foreign_keys"));
            assertFalse(temViolacaoChaveEstrangeira(connection));
        }
    }

    @Test
    void reinicializacaoPreservaPessoaCadastradaManualmenteSemDuplicarOuAlterar()
            throws Exception {
        DatabaseInitializer.initialize();

        Pessoa pessoa = pessoaDAO.salvar(criarPessoaSintetica());
        int idOriginal = pessoa.getId();

        DatabaseInitializer.initialize();
        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection();
             var statement = connection.prepareStatement("""
                     SELECT id, nome, ativo, privilegio
                     FROM pessoa
                     WHERE id = ?
                     """)) {
            statement.setInt(1, idOriginal);
            try (ResultSet result = statement.executeQuery()) {
                assertTrue(result.next());
                assertEquals(idOriginal, result.getInt("id"));
                assertEquals("Pessoa Sintética de Teste", result.getString("nome"));
                assertEquals(1, result.getInt("ativo"));
                assertEquals(Privilegio.BATIZADO.name(), result.getString("privilegio"));
                assertFalse(result.next());
            }
            assertEquals(1, contar(connection, "SELECT COUNT(*) FROM pessoa"));
            assertFalse(temViolacaoChaveEstrangeira(connection));
        }
    }

    @Test
    void pessoaExcluidaLegitimamenteNaoReapareceAposReinicializacao()
            throws Exception {
        DatabaseInitializer.initialize();
        assertEquals(0, pessoaDAO.listarTodos().size());

        Pessoa pessoa = pessoaDAO.salvar(criarPessoaSintetica());
        pessoaDAO.excluir(pessoa.getId());

        assertEquals(0, pessoaDAO.listarTodos().size());

        DatabaseInitializer.initialize();

        assertEquals(0, pessoaDAO.listarTodos().size());
        try (Connection connection = ConnectionFactory.getConnection()) {
            assertFalse(temViolacaoChaveEstrangeira(connection));
        }
    }

    private static Pessoa criarPessoaSintetica() {
        return new Pessoa(
                "Pessoa Sintética de Teste",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO
        );
    }

    private static int contar(Connection connection, String sql) throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private static boolean tabelaExiste(Connection connection, String nome)
            throws Exception {
        try (var statement = connection.prepareStatement("""
                SELECT 1 FROM sqlite_master
                WHERE type = 'table' AND name = ?
                """)) {
            statement.setString(1, nome);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean temViolacaoChaveEstrangeira(Connection connection)
            throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA foreign_key_check")) {
            return result.next();
        }
    }
}
