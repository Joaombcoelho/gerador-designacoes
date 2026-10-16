package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Operações comuns de arquivo SQLite usadas por backup e restauração.
 */
final class DatabaseFileSupport {

    static final ReentrantLock FILE_OPERATION_LOCK = new ReentrantLock();

    private static final List<String> REQUIRED_TABLES =
            List.of("pessoa", "parte", "escala", "designacao");

    private DatabaseFileSupport() {
    }

    static Path createTemporaryPath(Path directory, String prefix)
            throws IOException {
        Path temporary = Files.createTempFile(directory, prefix, ".db");
        Files.delete(temporary);
        return temporary;
    }

    /**
     * Produz uma fotografia consistente do banco usando o próprio SQLite.
     * VACUUM INTO incorpora dados confirmados mesmo quando a origem usa WAL.
     * O destino precisa não existir.
     */
    static void createSnapshot(Path source, Path destination)
            throws IOException {
        if (Files.exists(destination)) {
            throw new IOException(
                    "O destino temporário da cópia já existe: " + destination
            );
        }

        String destinationLiteral = destination.toAbsolutePath()
                .normalize()
                .toString()
                .replace("'", "''");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + source.toAbsolutePath().normalize());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("VACUUM INTO '" + destinationLiteral + "'");
        } catch (SQLException e) {
            throw new IOException(
                    "Não foi possível criar uma cópia consistente do banco SQLite.",
                    e
            );
        }
    }

    /**
     * Verifica integridade física, integridade referencial e se o arquivo
     * contém as tabelas centrais esperadas pelo aplicativo.
     */
    static void validateDatabase(Path database) throws IOException {
        if (database == null || !Files.isRegularFile(database)) {
            throw new IOException(
                    "O arquivo de banco não existe ou não é um arquivo regular."
            );
        }
        if (Files.size(database) == 0) {
            throw new IOException("O arquivo de banco está vazio.");
        }

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + database.toAbsolutePath().normalize());
             Statement statement = connection.createStatement()) {

            boolean integrityOk = false;
            try (ResultSet result = statement.executeQuery(
                    "PRAGMA integrity_check")) {
                while (result.next()) {
                    String message = result.getString(1);
                    if (!"ok".equalsIgnoreCase(message)) {
                        throw new IOException(
                                "Falha na verificação de integridade SQLite: "
                                        + message
                        );
                    }
                    integrityOk = true;
                }
            }

            if (!integrityOk) {
                throw new IOException(
                        "O SQLite não retornou resultado para a verificação de integridade."
                );
            }

            try (ResultSet result = statement.executeQuery(
                    "PRAGMA foreign_key_check")) {
                if (result.next()) {
                    throw new IOException(
                            "O banco contém referências estrangeiras inválidas."
                    );
                }
            }

            for (String requiredTable : REQUIRED_TABLES) {
                try (var tableQuery = connection.prepareStatement("""
                        SELECT COUNT(*)
                        FROM sqlite_master
                        WHERE type = 'table' AND name = ?
                        """)) {
                    tableQuery.setString(1, requiredTable);
                    try (ResultSet result = tableQuery.executeQuery()) {
                        if (!result.next() || result.getInt(1) != 1) {
                            throw new IOException(
                                    "O arquivo não parece ser um banco válido do Gerador de Designações: "
                                            + "tabela ausente '" + requiredTable + "'."
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new IOException(
                    "O arquivo não é um banco SQLite válido ou não pôde ser lido.",
                    e
            );
        }
    }

    static void moveReplacing(Path source, Path destination)
            throws IOException {
        try {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(
                    source,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    /**
     * Remove arquivos laterais somente durante uma restauração coordenada,
     * depois que já existe uma fotografia de recuperação do banco original.
     */
    static void deleteSidecars(Path database) throws IOException {
        Files.deleteIfExists(Path.of(database + "-wal"));
        Files.deleteIfExists(Path.of(database + "-shm"));
        Files.deleteIfExists(Path.of(database + "-journal"));
    }

    static boolean samePath(Path first, Path second) {
        Path normalizedFirst = first.toAbsolutePath().normalize();
        Path normalizedSecond = second.toAbsolutePath().normalize();
        if (normalizedFirst.equals(normalizedSecond)) {
            return true;
        }
        try {
            return Files.exists(normalizedFirst)
                    && Files.exists(normalizedSecond)
                    && Files.isSameFile(normalizedFirst, normalizedSecond);
        } catch (IOException ignored) {
            return false;
        }
    }

    static void deleteTemporaryFile(Path path) throws IOException {
        if (path != null) {
            Files.deleteIfExists(path);
        }
    }
}
