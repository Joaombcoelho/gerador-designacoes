package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class BackupRestoreTest {

    private Path directory;
    private Path databasePath;
    private ConnectionFactory.TestContext context;
    private Connection connection;

    @BeforeEach
    void prepararBancoTemporario() throws Exception {
        directory = Files.createTempDirectory("gerador-backup-test-");
        databasePath = directory.resolve("active.db");
        context = ConnectionFactory.openTestContext(databasePath);
        DatabaseInitializer.initialize();
        connection = ConnectionFactory.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE backup_probe (
                        id INTEGER PRIMARY KEY,
                        valor TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO backup_probe (id, valor)
                    VALUES (1, 'original')
                    """);
        }
    }

    @AfterEach
    void limparBancoTemporario() throws Exception {
        Exception failure = null;
        try {
            fecharConexao();
        } catch (Exception e) {
            failure = e;
        }
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        try {
            if (directory != null && Files.exists(directory)) {
                try (var paths = Files.walk(directory)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(path -> {
                                try {
                                    Files.deleteIfExists(path);
                                } catch (IOException e) {
                                    throw new CleanupException(e);
                                }
                            });
                }
            }
        } catch (CleanupException e) {
            if (failure == null) {
                failure = e.ioException;
            } else {
                failure.addSuppressed(e.ioException);
            }
        } catch (IOException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Test
    void deveCriarBackupConsistenteMesmoComWALERestaurarOsDados()
            throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet journalMode = statement.executeQuery(
                     "PRAGMA journal_mode = WAL")) {
            assertTrue(journalMode.next());
            assertEquals("wal", journalMode.getString(1).toLowerCase());
        }

        Path backup = directory.resolve("backup-valido.db");
        BackupDatabase.criarBackup(backup);

        assertTrue(Files.isRegularFile(backup));
        DatabaseFileSupport.validateDatabase(backup);
        assertEquals(1, contar(backup, "SELECT COUNT(*) FROM backup_probe"));

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE backup_probe SET valor = 'alterado' WHERE id = 1"
            );
        }

        fecharConexao();
        RestaurarDatabase.restaurar(backup);

        connection = ConnectionFactory.getConnection();
        assertEquals(
                "original",
                texto(connection,
                        "SELECT valor FROM backup_probe WHERE id = 1")
        );
        assertEquals(1, contar(connection, "SELECT COUNT(*) FROM backup_probe"));
        assertIntegridadeValida(connection);
    }

    @Test
    void deveRejeitarArquivoInvalidoSemAlterarBancoAtivo()
            throws Exception {
        Path backupInvalido = directory.resolve("arquivo-corrompido.db");
        Files.writeString(
                backupInvalido,
                "isto não é um banco SQLite",
                StandardCharsets.UTF_8
        );

        IOException erro = assertThrows(
                IOException.class,
                () -> RestaurarDatabase.restaurar(backupInvalido)
        );

        assertTrue(erro.getMessage().contains("SQLite")
                        || erro.getMessage().contains("banco"));
        assertEquals(
                "original",
                texto(connection,
                        "SELECT valor FROM backup_probe WHERE id = 1")
        );
    }

    @Test
    void devePreservarBancoOriginalQuandoSubstituicaoFalhar()
            throws Exception {
        Path backup = directory.resolve("backup-anterior.db");
        BackupDatabase.criarBackup(backup);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE backup_probe SET valor = 'estado-atual' WHERE id = 1"
            );
        }
        fecharConexao();

        AtomicBoolean falharPrimeiraMovimentacao = new AtomicBoolean(true);
        RestaurarDatabase.FileMover moverComFalha = (origem, destino) -> {
            if (falharPrimeiraMovimentacao.compareAndSet(true, false)) {
                throw new IOException("Falha simulada durante a substituição");
            }
            DatabaseFileSupport.moveReplacing(origem, destino);
        };

        IOException erro = assertThrows(
                IOException.class,
                () -> RestaurarDatabase.restaurar(backup, moverComFalha)
        );
        assertTrue(erro.getMessage().contains("Falha simulada"));

        connection = ConnectionFactory.getConnection();
        assertEquals(
                "estado-atual",
                texto(connection,
                        "SELECT valor FROM backup_probe WHERE id = 1")
        );
        assertIntegridadeValida(connection);
    }

    @Test
    void deveRejeitarBancoSqliteValidoMasSemEsquemaDoAplicativo()
            throws Exception {
        Path bancoEstranho = directory.resolve("outro-banco.db");
        try (Connection outro = java.sql.DriverManager.getConnection(
                "jdbc:sqlite:" + bancoEstranho.toAbsolutePath());
             Statement statement = outro.createStatement()) {
            statement.execute("CREATE TABLE outra_tabela (id INTEGER PRIMARY KEY)");
        }

        assertThrows(
                IOException.class,
                () -> RestaurarDatabase.restaurar(bancoEstranho)
        );
        assertEquals(
                "original",
                texto(connection,
                        "SELECT valor FROM backup_probe WHERE id = 1")
        );
    }

    private void fecharConexao() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
        connection = null;
    }

    private int contar(Path database, String sql) throws Exception {
        try (Connection leitura = java.sql.DriverManager.getConnection(
                "jdbc:sqlite:" + database.toAbsolutePath());
             Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private int contar(Connection leitura, String sql) throws Exception {
        try (Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private String texto(Connection leitura, String sql) throws Exception {
        try (Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private void assertIntegridadeValida(Connection leitura) throws Exception {
        try (Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA integrity_check")) {
            assertTrue(result.next());
            assertEquals("ok", result.getString(1));
            assertFalse(result.next());
        }
    }

    private static final class CleanupException extends RuntimeException {
        private final IOException ioException;

        private CleanupException(IOException ioException) {
            super(ioException);
            this.ioException = ioException;
        }
    }
}
