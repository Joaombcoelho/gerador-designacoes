package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EscalaDAO {

    public Escala salvar(Escala escala) {

        String sql = """
                INSERT INTO escala (
                    data,
                    status,
                    data_geracao,
                    data_salvamento
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement statement = connection.prepareStatement(
                            sql,
                            PreparedStatement.RETURN_GENERATED_KEYS
                    )
            ) {

                statement.setString(1, escala.getData().toString());
                statement.setString(2, escala.getStatus().name());
                statement.setString(3, escala.getDataGeracao().toString());

                if (escala.getDataSalvamento() == null) {
                    statement.setNull(4, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(
                            4,
                            escala.getDataSalvamento().toString()
                    );
                }

                int linhasAfetadas = statement.executeUpdate();

                if (linhasAfetadas != 1) {
                    throw new RuntimeException("Erro ao salvar escala.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                    if (generatedKeys.next()) {
                        escala.setId(generatedKeys.getInt(1));
                    } else {
                        throw new RuntimeException(
                                "Não foi possível obter o ID da escala."
                        );
                    }
                }

                salvarDesignacoes(connection, escala);

                connection.commit();

                return escala;

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar escala.",
                    e
            );
        }
    }

    public Optional<Escala> buscarPorId(Integer id) {

        String sql = """
                SELECT *
                FROM escala
                WHERE id = ?
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next()) {
                    return Optional.empty();
                }

                LocalDate data =
                        LocalDate.parse(
                                resultSet.getString("data")
                        );


                Escala escala = new Escala(
                        data,
                        buscarDesignacoes(
                                connection,
                                id,
                                data
                        )
                );

                escala.setId(
                        resultSet.getInt("id")
                );

                escala.setStatus(
                        br.com.geradordesignacoes.model.StatusEscala.valueOf(
                                resultSet.getString("status")
                        )
                );

                escala.setDataGeracao(
                        java.time.LocalDateTime.parse(
                                resultSet.getString("data_geracao")
                        )
                );

                if (resultSet.getTimestamp("data_salvamento") != null) {

                    escala.setDataSalvamento(
                            java.time.LocalDateTime.parse(
                                    resultSet.getString("data_salvamento")
                            )
                    );
                }

                return Optional.of(escala);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar escala.",
                    e
            );
        }
    }

    public List<Escala> listarTodas() {

        String sql = """
                SELECT id
                FROM escala
                ORDER BY data DESC
                """;

        List<Escala> escalas = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {

                buscarPorId(resultSet.getInt("id"))
                        .ifPresent(escalas::add);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar escalas.",
                    e
            );
        }

        return escalas;
    }

    public void excluir(Integer escalaId) {

        if (escalaId == null) {
            throw new IllegalArgumentException(
                    "ID da escala não pode ser nulo."
            );
        }

        String sql = """
                DELETE FROM escala
                WHERE id = ?
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, escalaId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao excluir escala.",
                    e
            );
        }
    }

    private void salvarDesignacoes(
            Connection connection,
            Escala escala
    ) throws SQLException {

        String sql = """
                INSERT INTO designacao (
                    escala_id,
                    parte_id,
                    responsavel_id,
                    ajudante_id
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Designacao designacao : escala.getDesignacoes()) {

                statement.setInt(1, escala.getId());
                statement.setInt(2, designacao.getParte().getId());
                statement.setInt(3, designacao.getResponsavel().getId());

                if (designacao.getAjudante() == null) {
                    statement.setNull(4, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(
                            4,
                            designacao.getAjudante().getId()
                    );
                }

                statement.executeUpdate();
            }
        }
    }

    private List<Designacao> buscarDesignacoes(
            Connection connection,
            Integer escalaId,
            LocalDate data
    ) throws SQLException {

        String sql = """
                SELECT
                    parte_id,
                    responsavel_id,
                    ajudante_id
                FROM designacao
                WHERE escala_id = ?
                ORDER BY id
                """;

        List<Designacao> designacoes = new ArrayList<>();

        ParteDAO parteDAO = new ParteDAO();
        PessoaDAO pessoaDAO = new PessoaDAO();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, escalaId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Parte parte = parteDAO.buscarPorId(
                            resultSet.getInt("parte_id")
                    ).orElseThrow(() ->
                            new RuntimeException("Parte não encontrada.")
                    );

                    Pessoa responsavel = pessoaDAO.buscarPorId(
                            resultSet.getInt("responsavel_id")
                    ).orElseThrow(() ->
                            new RuntimeException("Responsável não encontrado.")
                    );

                    Pessoa ajudante = null;

                    int ajudanteId =
                            resultSet.getInt("ajudante_id");

                    if (!resultSet.wasNull()) {

                        ajudante = pessoaDAO.buscarPorId(
                                ajudanteId
                        ).orElseThrow(() ->
                                new RuntimeException("Ajudante não encontrado.")
                        );
                    }

                    designacoes.add(
                            new Designacao(
                                    data,
                                    parte,
                                    responsavel,
                                    ajudante
                            )
                    );
                }
            }
        }

        return designacoes;
    }

}