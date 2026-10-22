package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class BackupServiceRetentionFailureTest {

    private Path directory;
    private Path backupDirectory;
    private ConnectionFactory.TestContext context;
    private Connection connection;

    @BeforeEach
    void prepararBancoDeTeste() throws Exception {
        directory = Files.createTempDirectory("gerador-backup-retencao-falha-");
        backupDirectory = directory.resolve("backups");
        Files.createDirectories(backupDirectory);

        context = ConnectionFactory.openTestContext(directory.resolve("active.db"));
        DatabaseInitializer.initialize();
        connection = ConnectionFactory.getConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE retention_probe (
                        id INTEGER PRIMARY KEY,
                        valor TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO retention_probe (id, valor)
                    VALUES (1, 'banco-original')
                    """);
        }
    }

    @AfterEach
    void limpar() throws Exception {
        Exception falha = null;
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            falha = e;
        }
        try {
            if (context != null) {
                context.close();
            }
        } catch (Exception e) {
            if (falha == null) {
                falha = e;
            } else {
                falha.addSuppressed(e);
            }
        }
        try {
            if (directory != null && Files.exists(directory)) {
                try (var caminhos = Files.walk(directory)) {
                    for (Path caminho : caminhos.sorted(Comparator.reverseOrder()).toList()) {
                        Files.deleteIfExists(caminho);
                    }
                }
            }
        } catch (Exception e) {
            if (falha == null) {
                falha = e;
            } else {
                falha.addSuppressed(e);
            }
        }
        if (falha != null) {
            throw falha;
        }
    }

    @Test
    void falhaNaRetencaoNaoInvalidaNovoBackupNemAlteraBancoPrincipal()
            throws Exception {
        Path backupManual = backupDirectory.resolve("backup-manual.db");
        Files.writeString(backupManual, "backup manual preexistente");

        Path legado = backupDirectory.resolve(
                "gerador-designacoes_2020-01-01_00-00-00-000.db");
        Files.writeString(legado, "backup legado preexistente");

        BackupRetentionService retencaoComFalha = new BackupRetentionService() {
            @Override
            void registrarBackupAutomatico(Path backup, Instant criadoEm)
                    throws IOException {
                throw new IOException("Falha simulada ao registrar metadados");
            }
        };
        BackupService service = new BackupService(backupDirectory, retencaoComFalha);

        assertDoesNotThrow(service::criarBackup);

        assertTrue(Files.isRegularFile(backupManual));
        assertTrue(Files.isRegularFile(legado));

        try (var arquivos = Files.list(backupDirectory)) {
            long backupsCriados = arquivos
                    .filter(path -> path.getFileName().toString().matches(
                            "gerador-designacoes_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}(?:-\\d+)?\\.db"))
                    .count();
            assertEquals(2, backupsCriados,
                    "O backup novo deve existir, assim como o arquivo legado preservado.");
        }

        assertEquals("banco-original", consultarValorPrincipal());
    }

    private String consultarValorPrincipal() throws Exception {
        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "SELECT valor FROM retention_probe WHERE id = 1")) {
            assertTrue(resultado.next());
            return resultado.getString(1);
        }
    }
}
