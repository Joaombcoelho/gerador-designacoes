package br.com.geradordesignacoes.database;

public class DatabaseInitializer {

    private static final String CREATE_TABLE_PESSOA = """
    CREATE TABLE IF NOT EXISTS pessoa (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT NOT NULL,
        sexo TEXT NOT NULL
    );
    """;

    public static void initialize() {

    }

}