package br.com.geradordesignacoes.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final Path DATABASE_DIRECTORY =
            Path.of(System.getenv("LOCALAPPDATA"), "GeradorDesignacoes");

    private static final Path DATABASE_PATH =
            DATABASE_DIRECTORY.resolve("gerador-designacoes.db");

    public static Connection getConnection() throws SQLException {

        try {
            Files.createDirectories(DATABASE_DIRECTORY);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível criar a pasta de dados.",
                    e
            );
        }

        String url = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath();

        Connection connection = DriverManager.getConnection(url);

        try (var statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON;");
        }

        return connection;
    }

    public static Path getDatabasePath() {
        return DATABASE_PATH;
    }
}