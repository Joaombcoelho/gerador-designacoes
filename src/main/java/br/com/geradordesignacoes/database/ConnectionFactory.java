package br.com.geradordesignacoes.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    private static final Path DATABASE_PATH =
            Path.of("data", "gerador-designacoes.db");

    public static Connection getConnection() throws SQLException {

        try {
            Files.createDirectories(DATABASE_PATH.getParent());
        } catch (Exception e) {
            throw new RuntimeException("Não foi possível criar a pasta de dados.", e);
        }

        String url = "jdbc:sqlite:" + DATABASE_PATH.toAbsolutePath();

        return DriverManager.getConnection(url);
    }
}