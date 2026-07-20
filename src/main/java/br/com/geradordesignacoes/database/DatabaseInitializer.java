package br.com.geradordesignacoes.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String CREATE_TABLE_PESSOA = """

            CREATE TABLE IF NOT EXISTS pessoa (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT NOT NULL,
                sexo TEXT NOT NULL,
                ativo INTEGER NOT NULL,
                pode_ser_responsavel INTEGER NOT NULL,
                pode_ser_ajudante INTEGER NOT NULL,
                pode_fazer_leitura INTEGER NOT NULL,
                pode_fazer_discurso INTEGER NOT NULL,
                privilegio TEXT NOT NULL
            );

            """;

    private static final String CREATE_TABLE_PARTE = """

        CREATE TABLE IF NOT EXISTS parte (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            tipo TEXT NOT NULL,
            privilegio_minimo TEXT NOT NULL,
            exige_ajudante INTEGER NOT NULL,
            sexo_permitido TEXT NOT NULL,
            quantidade_minima_participantes INTEGER NOT NULL,
            gera_formulario INTEGER NOT NULL
        );

        """;

    private static final String CREATE_TABLE_HISTORICO_DESIGNACOES = """

            CREATE TABLE IF NOT EXISTS historico_designacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data DATE NOT NULL,
                pessoa_id INTEGER NOT NULL,
                parte_id INTEGER NOT NULL,
                tipo_participacao TEXT NOT NULL,

                FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                FOREIGN KEY (parte_id) REFERENCES parte(id)
            );

            """;

    public static void initialize() {

        try (
                Connection connection = ConnectionFactory.getConnection();
                Statement statement = connection.createStatement()
        ) {

            statement.execute(CREATE_TABLE_PESSOA);
            System.out.println("Tabela 'pessoa' criada ou já existente.");

            statement.execute(CREATE_TABLE_PARTE);
            System.out.println("Tabela 'parte' criada ou já existente.");

            statement.execute(CREATE_TABLE_HISTORICO_DESIGNACOES);
            System.out.println("Tabela 'historico_designacoes' criada ou já existente.");

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inicializar o banco de dados.", e);
        }
    }
}