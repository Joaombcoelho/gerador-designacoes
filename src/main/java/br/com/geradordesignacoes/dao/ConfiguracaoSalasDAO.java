package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracaoSalasDAO {

    public ConfiguracaoSalas buscar() {
        String sql = """
                SELECT sala_b_habilitada, sala_c_habilitada
                FROM configuracao_salas
                WHERE id = 1
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {
            if (!resultSet.next()) {
                return ConfiguracaoSalas.desabilitadas();
            }

            return new ConfiguracaoSalas(
                    resultSet.getBoolean("sala_b_habilitada"),
                    resultSet.getBoolean("sala_c_habilitada")
            );
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar configuração das salas.",
                    e
            );
        }
    }

    public void salvar(ConfiguracaoSalas configuracao) {
        if (configuracao == null) {
            throw new IllegalArgumentException(
                    "A configuração das salas não pode ser nula."
            );
        }

        String sql = """
                INSERT INTO configuracao_salas (
                    id, sala_b_habilitada, sala_c_habilitada
                )
                VALUES (1, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    sala_b_habilitada = excluded.sala_b_habilitada,
                    sala_c_habilitada = excluded.sala_c_habilitada
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setBoolean(1, configuracao.salaBHabilitada());
            statement.setBoolean(2, configuracao.salaCHabilitada());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao salvar configuração das salas.",
                    e
            );
        }
    }
}
