
package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    @TempDir
    Path diretorioTemporario;

    @Test
    void devePreservarDesignacoesAntigasSemColunaSala()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("antigo.db");

        try (Connection connection = criarBanco(
                arquivo,
                false,
                true,
                List.of("PRINCIPAL")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals(
                    "PRINCIPAL",
                    buscarSala(connection, 1)
            );

            assertEquals(
                    1,
                    contarDesignacoes(connection)
            );

            assertEquals(
                    10,
                    buscarValor(connection,
                            "SELECT responsavel_id FROM designacao WHERE id = 1")
            );

            assertEquals(
                    20,
                    buscarValor(connection,
                            "SELECT ajudante_id FROM designacao WHERE id = 1")
            );

            assertTrue(colunaExiste(connection, "sala"));
            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void devePreservarTodasAsSalasDuranteAMigracao()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("salas.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                true,
                List.of("PRINCIPAL", "B", "C")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals("PRINCIPAL", buscarSala(connection, 1));
            assertEquals("B", buscarSala(connection, 2));
            assertEquals("C", buscarSala(connection, 3));

            assertEquals(3, contarDesignacoes(connection));

            assertEquals(
                    10,
                    buscarValor(connection,
                            "SELECT responsavel_id FROM designacao WHERE id = 2")
            );

            assertEquals(
                    20,
                    buscarValor(connection,
                            "SELECT ajudante_id FROM designacao WHERE id = 2")
            );

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void naoDeveAlterarDesignacoesQuandoResponsavelJaPermiteNulo()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("atualizado.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                false,
                List.of("PRINCIPAL", "B", "C")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals("PRINCIPAL", buscarSala(connection, 1));
            assertEquals("B", buscarSala(connection, 2));
            assertEquals("C", buscarSala(connection, 3));

            assertEquals(3, contarDesignacoes(connection));

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void deveReverterMigracaoQuandoEncontrarSalaInvalida()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("invalido.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                true,
                List.of("SALA_INVALIDA")
        )) {
            SQLException erro = assertThrows(
                    SQLException.class,
                    () -> DatabaseInitializer
                            .migrarResponsavelDesignacaoNullable(
                                    connection
                            )
            );

            assertNotNull(erro);

            // A tabela original e seus dados devem continuar existindo.
            assertEquals(
                    "SALA_INVALIDA",
                    buscarSala(connection, 1)
            );

            assertEquals(1, contarDesignacoes(connection));

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));

            assertTrue(foreignKeysAtivos(connection));
        }
    }

    @Test
    void devePreservarHistoricoAntigoAoAdicionarColunaSala()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-antigo.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    id, data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    7, '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT id, data, pessoa_id, parte_id,
                        tipo_participacao, sala
                 FROM historico_designacoes
                 WHERE id = 7
                 """)) {

                assertTrue(resultado.next());
                assertEquals(7, resultado.getInt("id"));
                assertEquals("2026-10-01", resultado.getString("data"));
                assertEquals(10, resultado.getInt("pessoa_id"));
                assertEquals(30, resultado.getInt("parte_id"));
                assertEquals(
                        "DISCURSO",
                        resultado.getString("tipo_participacao")
                );
                assertEquals("PRINCIPAL", resultado.getString("sala"));
                assertFalse(resultado.next());
            }
        }
    }

    @Test
    void devePermitirMesmaParticipacaoEmSalasDiferentes()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-salas.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao, sala
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO', 'B'
                )
                """);
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT COUNT(*)
                 FROM historico_designacoes
                 WHERE data = '2026-10-01'
                   AND pessoa_id = 10
                   AND parte_id = 30
                   AND tipo_participacao = 'DISCURSO'
                 """)) {

                assertTrue(resultado.next());
                assertEquals(2, resultado.getInt(1));
            }
        }
    }


    @Test
    void naoDeveAlterarHistoricoAoExecutarMigracaoNovamente()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-repetido.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);
            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT COUNT(*)
                 FROM historico_designacoes
                 """)) {

                assertTrue(resultado.next());
                assertEquals(1, resultado.getInt(1));
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT sala
                 FROM historico_designacoes
                 """)) {

                assertTrue(resultado.next());
                assertEquals("PRINCIPAL", resultado.getString("sala"));
                assertFalse(resultado.next());
            }
        }
    }

    private Connection criarBanco(
            Path arquivo,
            boolean possuiSala,
            boolean responsavelObrigatorio,
            List<String> salas
    ) throws SQLException {

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath()
        );

        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");

                statement.execute("""
                        CREATE TABLE pessoa (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("""
                        CREATE TABLE parte (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("""
                        CREATE TABLE escala (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO pessoa (id) VALUES (20)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");
                statement.execute("INSERT INTO escala (id) VALUES (40)");

                String colunaResponsavel = responsavelObrigatorio
                        ? "responsavel_id INTEGER NOT NULL"
                        : "responsavel_id INTEGER";

                String colunaSala = possuiSala
                        ? ", sala TEXT NOT NULL DEFAULT 'PRINCIPAL'"
                        : "";

                statement.execute("""
                        CREATE TABLE designacao (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            escala_id INTEGER NOT NULL,
                            parte_id INTEGER NOT NULL,
                            %s,
                            ajudante_id INTEGER
                            %s,
                            FOREIGN KEY (escala_id)
                                REFERENCES escala(id) ON DELETE CASCADE,
                            FOREIGN KEY (parte_id)
                                REFERENCES parte(id),
                            FOREIGN KEY (responsavel_id)
                                REFERENCES pessoa(id),
                            FOREIGN KEY (ajudante_id)
                                REFERENCES pessoa(id)
                        )
                        """.formatted(colunaResponsavel, colunaSala));
            }

            for (int i = 0; i < salas.size(); i++) {
                int id = i + 1;

                String sql = possuiSala
                        ? """
                          INSERT INTO designacao (
                              id, escala_id, parte_id,
                              responsavel_id, ajudante_id, sala
                          )
                          VALUES (%d, 40, 30, 10, 20, '%s')
                          """.formatted(id, salas.get(i))
                        : """
                          INSERT INTO designacao (
                              id, escala_id, parte_id,
                              responsavel_id, ajudante_id
                          )
                          VALUES (%d, 40, 30, 10, 20)
                          """.formatted(id);

                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }

            return connection;

        } catch (SQLException | RuntimeException e) {
            try {
                connection.close();
            } catch (SQLException erroFechamento) {
                e.addSuppressed(erroFechamento);
            }
            throw e;
        }
    }

    private String buscarSala(
            Connection connection,
            int id
    ) throws SQLException {

        try (var statement = connection.prepareStatement("""
                SELECT sala
                FROM designacao
                WHERE id = ?
                """)) {

            statement.setInt(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                assertTrue(resultado.next());
                return resultado.getString("sala");
            }
        }
    }

    private int contarDesignacoes(
            Connection connection
    ) throws SQLException {

        return buscarValor(
                connection,
                "SELECT COUNT(*) FROM designacao"
        );
    }

    private int buscarValor(
            Connection connection,
            String sql
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(sql)) {

            assertTrue(resultado.next());
            return resultado.getInt(1);
        }
    }

    private boolean colunaExiste(
            Connection connection,
            String nomeColuna
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "PRAGMA table_info(designacao)"
             )) {

            while (resultado.next()) {
                if (nomeColuna.equals(resultado.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tabelaExiste(
            Connection connection,
            String nomeTabela
    ) throws SQLException {

        try (var statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = ?
                """)) {

            statement.setString(1, nomeTabela);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    private boolean foreignKeysAtivos(
            Connection connection
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "PRAGMA foreign_keys"
             )) {

            return resultado.next() && resultado.getInt(1) == 1;
        }
    }
}