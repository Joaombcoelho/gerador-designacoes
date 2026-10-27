package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class HabilidadesSchemaMigrationTest {

    @TempDir
    Path diretorioTemporario;

    private ConnectionFactory.TestContext contexto;

    @AfterEach
    void fecharContexto() throws Exception {
        if (contexto != null) {
            contexto.close();
        }
    }

    @Test
    void deveCriarEstruturaDeHabilidadesEPermitirReinicializacao() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("habilidades-novo.db"));

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertTrue(tabelaExiste(connection, "habilidade_designacao"));
            assertTrue(tabelaExiste(connection, "pessoa_habilidade"));
            assertTrue(colunaExiste(connection, "pessoa", "habilidades_configuradas"));
            assertEquals(5, contar(connection, "SELECT COUNT(*) FROM habilidade_designacao"));
            assertEquals(0, contar(connection, "SELECT COUNT(*) FROM pessoa_habilidade"));
        }

        DatabaseInitializer.initialize();
        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertTrue(tabelaExiste(connection, "habilidade_designacao"));
            assertTrue(tabelaExiste(connection, "pessoa_habilidade"));
            assertEquals(1, inteiro(connection, "PRAGMA foreign_keys"));
        }
    }


    @Test
    void deveMigrarBancoExistenteSemColunaHabilidadesConfiguradasPreservandoPessoas() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("habilidades-banco-legado.db"));

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE pessoa (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        sexo TEXT NOT NULL,
                        ativo INTEGER NOT NULL,
                        pode_ser_responsavel INTEGER NOT NULL,
                        pode_ser_ajudante INTEGER NOT NULL,
                        pode_fazer_leitura INTEGER NOT NULL,
                        pode_fazer_discurso INTEGER NOT NULL,
                        privilegio TEXT NOT NULL,
                        nivel_leitura TEXT NOT NULL DEFAULT 'BASICO'
                    )
                    """);
            statement.executeUpdate("""
                    INSERT INTO pessoa
                        (id, nome, sexo, ativo, pode_ser_responsavel,
                         pode_ser_ajudante, pode_fazer_leitura,
                         pode_fazer_discurso, privilegio, nivel_leitura)
                    VALUES (901, 'Pessoa legada', 'MASCULINO', 1, 0,
                            0, 0, 0, 'PUBLICADOR', 'BASICO')
                    """);
        }

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertTrue(colunaExiste(connection, "pessoa", "habilidades_configuradas"));
            assertEquals(0, contar(connection, """
                    SELECT COUNT(*) FROM pessoa
                    WHERE id = 901 AND nome = 'Pessoa legada'
                    """));
            assertEquals(1, contar(connection, """
                    SELECT COUNT(*) FROM pessoa
                    WHERE id = 901 AND nome = 'Pessoa legada'
                    """));
            assertEquals(0, contar(connection, """
                    SELECT habilidades_configuradas FROM pessoa WHERE id = 901
                    """));
        }
    }

    @Test
    void devePreservarVinculoEImpedirDuplicidadeOuReferenciaOrfa() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("habilidades-relacoes.db"));

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO pessoa
                        (id, nome, sexo, ativo, pode_ser_responsavel,
                         pode_ser_ajudante, pode_fazer_leitura,
                         pode_fazer_discurso, privilegio, nivel_leitura)
                    VALUES (501, 'Pessoa de teste', 'MASCULINO', 1, 0,
                            0, 0, 0, 'PUBLICADOR', 'BASICO')
                    """);
            statement.executeUpdate("""
                    INSERT INTO habilidade_designacao (id, codigo, nome)
                    VALUES (701, 'HABILIDADE_TESTE', 'Habilidade de teste')
                    """);
            statement.executeUpdate("""
                    INSERT INTO pessoa_habilidade (pessoa_id, habilidade_id)
                    VALUES (501, 701)
                    """);

            assertThrows(SQLException.class, () -> statement.executeUpdate("""
                    INSERT INTO pessoa_habilidade (pessoa_id, habilidade_id)
                    VALUES (501, 701)
                    """), "A chave composta deve impedir habilidades duplicadas.");

            assertThrows(SQLException.class, () -> statement.executeUpdate("""
                    INSERT INTO pessoa_habilidade (pessoa_id, habilidade_id)
                    VALUES (999999, 701)
                    """), "A chave estrangeira deve impedir pessoa inexistente.");

            assertThrows(SQLException.class, () -> statement.executeUpdate("""
                    INSERT INTO pessoa_habilidade (pessoa_id, habilidade_id)
                    VALUES (501, 999999)
                    """), "A chave estrangeira deve impedir habilidade inexistente.");
        }

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals(1, contar(connection, """
                    SELECT COUNT(*) FROM pessoa_habilidade
                    WHERE pessoa_id = 501 AND habilidade_id = 701
                    """));
            assertEquals(1, contar(connection, """
                    SELECT COUNT(*) FROM pessoa WHERE id = 501
                    """));
            assertEquals("Habilidade de teste", texto(connection, """
                    SELECT nome FROM habilidade_designacao WHERE id = 701
                    """));
        }
    }


    @Test
    void deveNormalizarTiposLegadosDeDiscursoSemExcluirPartesNemAlterarNomes() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("discursos-legados.db"));

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals(0, contar(connection, """
                    SELECT COUNT(*) FROM parte
                    WHERE tipo IN ('PARTE_1', 'PARTE_2', 'PARTE_3')
                    """));
            assertEquals(3, contar(connection, """
                    SELECT COUNT(*) FROM parte
                    WHERE nome IN ('Parte 1', 'Parte 2', 'Parte 3')
                      AND tipo = 'DISCURSO'
                    """));
        }

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals(3, contar(connection, """
                    SELECT COUNT(*) FROM parte
                    WHERE nome IN ('Parte 1', 'Parte 2', 'Parte 3')
                      AND tipo = 'DISCURSO'
                    """));
        }
    }

    private boolean tabelaExiste(Connection connection, String tabela) throws SQLException {
        try (var ps = connection.prepareStatement("""
                SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?
                """)) {
            ps.setString(1, tabela);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean colunaExiste(Connection connection, String tabela, String coluna) throws SQLException {
        try (var statement = connection.createStatement();
             var rs = statement.executeQuery("PRAGMA table_info(" + tabela + ")")) {
            while (rs.next()) {
                if (coluna.equals(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }

    private int contar(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        }
    }

    private int inteiro(Connection connection, String sql) throws SQLException {
        return contar(connection, sql);
    }

    private String texto(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement();
             var rs = statement.executeQuery(sql)) {
            assertTrue(rs.next());
            return rs.getString(1);
        }
    }
}
