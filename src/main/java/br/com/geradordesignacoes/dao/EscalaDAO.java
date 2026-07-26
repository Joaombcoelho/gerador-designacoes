package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.Escala;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EscalaDAO {

    public Escala salvar(Escala escala) {

        throw new UnsupportedOperationException(
                "Ainda não implementado."
        );
    }

    public Optional<Escala> buscarPorId(Integer id) {

        throw new UnsupportedOperationException(
                "Ainda não implementado."
        );
    }

    public List<Escala> listarTodas() {

        throw new UnsupportedOperationException(
                "Ainda não implementado."
        );
    }

    public void excluir(Integer id) {

        throw new UnsupportedOperationException(
                "Ainda não implementado."
        );
    }

    protected Connection getConnection() throws SQLException {

        return ConnectionFactory.getConnection();
    }
}