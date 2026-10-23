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
    void deveRestaurarDadosRelacionadosDaAplicacaoEInicializarBancoRestaurado()
            throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO pessoa
                        (id, nome, sexo, ativo, pode_ser_responsavel, pode_ser_ajudante,
                         pode_fazer_leitura, pode_fazer_discurso, pode_fazer_oracao,
                         pode_ser_presidente, pode_ser_dirigente, privilegio)
                    VALUES (901, 'Pessoa do backup', 'M', 1, 1, 1, 1, 1, 1, 1, 1, 'ANCIÃO')
                    """);
            statement.executeUpdate("""
                    INSERT INTO parte
                        (id, nome, tipo, privilegio_minimo, exige_ajudante, sexo_permitido,
                         quantidade_minima_participantes, gera_formulario)
                    VALUES (902, 'Parte do backup', 'FIXA', 'PUBLICADOR', 0, 'TODOS', 1, 0)
                    """);
            statement.executeUpdate("""
                    INSERT INTO escala (id, data, status, data_geracao)
                    VALUES (903, '2099-02-04', 'GERADA', '2099-02-01T10:00:00')
                    """);
            statement.executeUpdate("""
                    INSERT INTO designacao (id, escala_id, parte_id, responsavel_id)
                    VALUES (904, 903, 902, 901)
                    """);
            statement.executeUpdate("""
                    INSERT INTO historico_designacoes
                        (id, data, pessoa_id, parte_id, tipo_participacao)
                    VALUES (905, '2099-02-04', 901, 902, 'RESPONSAVEL')
                    """);
            statement.executeUpdate("""
                    INSERT INTO programacao_semana
                        (id, data, sala_b_habilitada, sala_c_habilitada,
                         cantico_inicial, cantico_meio, cantico_final)
                    VALUES (906, '2099-02-04', 1, 0, '12', '34', '56')
                    """);
            statement.executeUpdate("""
                    INSERT INTO programacao_parte
                        (id, programacao_semana_id, parte_id, ordem, tema, numero_oficial, duracao_minutos)
                    VALUES (907, 906, 902, 1, 'Tema do backup', 10, 8)
                    """);
            statement.executeUpdate("""
                    UPDATE configuracoes_gerais
                    SET nome_congregacao = 'Congregação do backup',
                        horario_inicio = '18:45'
                    WHERE id = 1
                    """);
            statement.executeUpdate("""
                    UPDATE configuracao_salas
                    SET sala_b_habilitada = 1, sala_c_habilitada = 0
                    WHERE id = 1
                    """);
        }

        Path backup = directory.resolve("backup-dados-relacionados.db");
        BackupDatabase.criarBackup(backup);
        DatabaseFileSupport.validateDatabase(backup);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("UPDATE pessoa SET nome = 'Pessoa alterada' WHERE id = 901");
            statement.executeUpdate("UPDATE parte SET nome = 'Parte alterada' WHERE id = 902");
            statement.executeUpdate("UPDATE escala SET status = 'ALTERADA' WHERE id = 903");
            statement.executeUpdate("UPDATE programacao_parte SET tema = 'Tema alterado' WHERE id = 907");
            statement.executeUpdate("""
                    UPDATE configuracoes_gerais
                    SET nome_congregacao = 'Congregação alterada', horario_inicio = '20:00'
                    WHERE id = 1
                    """);
        }
        fecharConexao();

        RestaurarDatabase.restaurar(backup);
        DatabaseInitializer.initialize();
        connection = ConnectionFactory.getConnection();

        assertEquals("Pessoa do backup", texto(connection,
                "SELECT nome FROM pessoa WHERE id = 901"));
        assertEquals("Parte do backup", texto(connection,
                "SELECT nome FROM parte WHERE id = 902"));
        assertEquals("GERADA", texto(connection,
                "SELECT status FROM escala WHERE id = 903"));
        assertEquals(901, contarValor(connection,
                "SELECT responsavel_id FROM designacao WHERE id = 904"));
        assertEquals(901, contarValor(connection,
                "SELECT pessoa_id FROM historico_designacoes WHERE id = 905"));
        assertEquals("Tema do backup", texto(connection,
                "SELECT tema FROM programacao_parte WHERE id = 907"));
        assertEquals("Congregação do backup", texto(connection,
                "SELECT nome_congregacao FROM configuracoes_gerais WHERE id = 1"));
        assertEquals("18:45", texto(connection,
                "SELECT horario_inicio FROM configuracoes_gerais WHERE id = 1"));
        assertEquals(1, contarValor(connection,
                "SELECT sala_b_habilitada FROM configuracao_salas WHERE id = 1"));
        assertIntegridadeValida(connection);
        assertForeignKeysValidas(connection);
    }

    @Test
    void deveRejeitarArquivoInexistenteOuVazioSemAlterarBancoAtivo()
            throws Exception {
        Path inexistente = directory.resolve("nao-existe.db");
        assertThrows(IOException.class, () -> RestaurarDatabase.restaurar(inexistente));

        Path vazio = directory.resolve("vazio.db");
        Files.createFile(vazio);
        assertThrows(IOException.class, () -> RestaurarDatabase.restaurar(vazio));

        assertEquals("original", texto(connection,
                "SELECT valor FROM backup_probe WHERE id = 1"));
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
            DatabaseFileSupport.moveReplacing(origem, destino);
            if (falharPrimeiraMovimentacao.compareAndSet(true, false)) {
                throw new IOException("Falha simulada depois da substituição");
            }
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

    private int contarValor(Connection leitura, String sql) throws Exception {
        try (Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery(sql)) {
            assertTrue(result.next());
            return result.getInt(1);
        }
    }

    private void assertForeignKeysValidas(Connection leitura) throws Exception {
        try (Statement statement = leitura.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA foreign_key_check")) {
            assertFalse(result.next(), "O banco restaurado não deve conter violações de chaves estrangeiras.");
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
