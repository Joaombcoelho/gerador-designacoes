package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.database.ConnectionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;

public abstract class BaseDAOTest {

    protected Connection connection;
    private TestDatabase testDatabase;

    protected final ConnectionFactory.TestContext
    abrirContextoNaThreadAtual() {
        return testDatabase.openContextOnCurrentThread();
    }


    @BeforeEach
    void prepararBanco() throws Exception {

        testDatabase =
                TestDatabase.create();

        DatabaseInitializer.initialize();

        connection =
                br.com.geradordesignacoes.database.ConnectionFactory
                        .getConnection();
    }


    @AfterEach
    void finalizarBanco() throws Exception {

        Exception failure = null;
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            failure = e;
        }

        try {
            testDatabase.close();
        } catch (Exception e) {
            if (failure != null) {
                failure.addSuppressed(e);
            } else {
                failure = e;
            }
        }

        if (failure != null) {
            throw failure;
        }
    }
}