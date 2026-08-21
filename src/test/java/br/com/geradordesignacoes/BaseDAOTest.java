package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class BaseDAOTest {

    protected Connection connection;


    @BeforeEach
    void prepararBanco() throws SQLException {

        DatabaseInitializer.initialize();

        connection = ConnectionFactory.getConnection();

        limparBanco();

        DatabaseInitializer.initialize();
    }


    @AfterEach
    void finalizarBanco() throws SQLException {

        limparBanco();

        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }


    protected void limparBanco() throws SQLException {

        try (Statement statement = connection.createStatement()) {

            statement.execute(
                    "DELETE FROM designacao"
            );

            statement.execute(
                    "DELETE FROM historico_designacoes"
            );

            statement.execute(
                    "DELETE FROM parte_participacao_necessaria"
            );

            statement.execute(
                    "DELETE FROM programacao_parte"
            );

            statement.execute(
                    "DELETE FROM programacao_semana"
            );

            statement.execute(
                    "DELETE FROM escala"
            );

            statement.execute(
                    "DELETE FROM parte"
            );

            statement.execute(
                    "DELETE FROM pessoa"
            );
        }
    }
}