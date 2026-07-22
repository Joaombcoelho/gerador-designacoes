package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

public class ParteDAO {

        public Parte salvar(Parte parte) {

        String sql = """
                INSERT INTO parte (
                    nome,
                    tipo,
                    privilegio_minimo,
                    exige_ajudante,
                    sexo_permitido,
                    quantidade_minima_participantes,
                    gera_formulario
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement statement = connection.prepareStatement(
                            sql,
                            PreparedStatement.RETURN_GENERATED_KEYS
                    )
            ) {

                statement.setString(1, parte.getNome());
                statement.setString(2, parte.getTipo().name());
                statement.setString(3, parte.getPrivilegioMinimo().name());
                statement.setBoolean(4, parte.getExigeAjudante());
                statement.setString(5, parte.getSexoPermitido().name());
                statement.setInt(6, parte.getQuantidadeMinimaParticipantes());
                statement.setBoolean(7, parte.geraFormulario());

                int linhasAfetadas = statement.executeUpdate();

                if (linhasAfetadas != 1) {
                    throw new RuntimeException("Erro ao salvar parte.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                    if (generatedKeys.next()) {

                        Parte parteSalva = new Parte(
                                generatedKeys.getInt(1),
                                parte.getNome(),
                                parte.getTipo(),
                                parte.getPrivilegioMinimo(),
                                parte.getExigeAjudante(),
                                parte.getSexoPermitido(),
                                parte.getQuantidadeMinimaParticipantes(),
                                parte.geraFormulario(),
                                parte.getParticipacoesNecessarias()
                        );

                        salvarParticipacoesNecessarias(connection, parteSalva);
                        connection.commit();

                        return parteSalva;

                    } else {
                        throw new RuntimeException("Não foi possível obter o ID gerado.");
                    }
                }
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar parte.", e);
        }
    }

    public List<Parte> listarTodos() {

        List<Parte> partes = new ArrayList<>();

        String sql = """
                SELECT *
                FROM parte
                ORDER BY nome
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                partes.add(mapearParte(connection, resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar partes.", e);
        }

        return partes;
    }

    public Optional<Parte> buscarPorId(Integer id) {

        String sql = """
                SELECT *
                FROM parte
                WHERE id = ?
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearParte(connection, resultSet));
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar parte por ID.", e);
        }

        return Optional.empty();
    }

    public void atualizar(Parte parte) {

        if (parte.getId() == null) {
            throw new IllegalArgumentException(
                    "A parte precisa possuir um ID para ser atualizada."
            );
        }

        String sql = """
                UPDATE parte
                SET
                    nome = ?,
                    tipo = ?,
                    privilegio_minimo = ?,
                    exige_ajudante = ?,
                    sexo_permitido = ?,
                    quantidade_minima_participantes = ?,
                    gera_formulario = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, parte.getNome());
                statement.setString(2, parte.getTipo().name());
                statement.setString(3, parte.getPrivilegioMinimo().name());
                statement.setBoolean(4, parte.getExigeAjudante());
                statement.setString(5, parte.getSexoPermitido().name());
                statement.setInt(6, parte.getQuantidadeMinimaParticipantes());
                statement.setBoolean(7, parte.geraFormulario());
                statement.setInt(8, parte.getId());

                int linhasAfetadas = statement.executeUpdate();

                if (linhasAfetadas != 1) {
                    throw new RuntimeException("Erro ao atualizar parte.");
                }

                excluirParticipacoesNecessarias(connection, parte.getId());
                salvarParticipacoesNecessarias(connection, parte);
                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar parte.", e);
        }
    }

    public void excluir(Integer id) {

        String sql = """
                DELETE FROM parte
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                excluirParticipacoesNecessarias(connection, id);
                statement.setInt(1, id);

                int linhasAfetadas = statement.executeUpdate();

                if (linhasAfetadas != 1) {
                    throw new RuntimeException("Parte não encontrada para exclusão.");
                }

                connection.commit();

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir parte.", e);
        }
    }

    private Parte mapearParte(Connection connection, ResultSet resultSet)
            throws SQLException {

        Integer parteId = resultSet.getInt("id");

        return new Parte(
                parteId,
                resultSet.getString("nome"),
                TipoParte.valueOf(resultSet.getString("tipo")),
                Privilegio.valueOf(resultSet.getString("privilegio_minimo")),
                resultSet.getInt("exige_ajudante") == 1,
                SexoPermitido.valueOf(resultSet.getString("sexo_permitido")),
                resultSet.getInt("quantidade_minima_participantes"),
                resultSet.getInt("gera_formulario") == 1,
                buscarParticipacoesNecessarias(connection, parteId)
        );
    }

    private List<TipoParticipacao> buscarParticipacoesNecessarias(
            Connection connection,
            Integer parteId) throws SQLException {

        String sql = """
                SELECT tipo_participacao
                FROM parte_participacao_necessaria
                WHERE parte_id = ?
                ORDER BY ordem
                """;

        List<TipoParticipacao> participacoes = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parteId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    participacoes.add(
                            TipoParticipacao.valueOf(
                                    resultSet.getString("tipo_participacao")
                            )
                    );
                }
            }
        }

        return participacoes;
    }

    private void salvarParticipacoesNecessarias(
            Connection connection,
            Parte parte) throws SQLException {

        String sql = """
                INSERT INTO parte_participacao_necessaria (
                    parte_id,
                    tipo_participacao,
                    ordem
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            int ordem = 0;

            for (TipoParticipacao participacao : parte.getParticipacoesNecessarias()) {
                statement.setInt(1, parte.getId());
                statement.setString(2, participacao.name());
                statement.setInt(3, ordem++);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void excluirParticipacoesNecessarias(
            Connection connection,
            Integer parteId) throws SQLException {

        String sql = """
                DELETE FROM parte_participacao_necessaria
                WHERE parte_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parteId);
            statement.executeUpdate();
        }
    }
}
