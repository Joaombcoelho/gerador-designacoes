package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProgramacaoParteDAO {

    private final ParteDAO parteDAO;


    public ProgramacaoParteDAO() {

        this.parteDAO =
                new ParteDAO();
    }


    public void salvar(
            int programacaoSemanaId,
            ProgramacaoParte programacaoParte
    ) {

        try (
                Connection connection =
                        ConnectionFactory.getConnection()
        ) {

            salvar(
                    connection,
                    programacaoSemanaId,
                    programacaoParte
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar parte da programação.",
                    e
            );
        }
    }


    public void salvar(
            Connection connection,
            int programacaoSemanaId,
            ProgramacaoParte programacaoParte
    ) throws SQLException {

        String sql = """
            INSERT INTO programacao_parte (
                programacao_semana_id,
                parte_id,
                ordem,
                tema
            )
            VALUES (?, ?, ?, ?)
            """;


        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    programacaoSemanaId
            );

            statement.setInt(
                    2,
                    programacaoParte
                            .getParte()
                            .getId()
            );

            statement.setInt(
                    3,
                    programacaoParte.getOrdem()
            );

            statement.setString(
                    4,
                    programacaoParte.getTema()
            );


            statement.executeUpdate();
        }
    }


    public void atualizarTema(
            Integer id,
            String tema
    ) {

        String sql = """
            UPDATE programacao_parte
            SET tema = ?
            WHERE id = ?
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    tema
            );

            statement.setInt(
                    2,
                    id
            );


            int linhasAfetadas =
                    statement.executeUpdate();


            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Parte da programação não encontrada."
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar tema da parte da programação.",
                    e
            );
        }
    }


    public List<ProgramacaoParte> listarPorSemana(
            int programacaoSemanaId
    ) {

        try (
                Connection connection =
                        ConnectionFactory.getConnection()
        ) {

            return listarPorSemana(
                    connection,
                    programacaoSemanaId
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar partes da programação.",
                    e
            );
        }
    }


    public List<ProgramacaoParte> listarPorSemana(
            Connection connection,
            int programacaoSemanaId
    ) throws SQLException {

        String sql = """
            SELECT
                id,
                parte_id,
                ordem,
                tema
            FROM programacao_parte
            WHERE programacao_semana_id = ?
            ORDER BY ordem
            """;


        List<ProgramacaoParte> partes =
                new ArrayList<>();


        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    programacaoSemanaId
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Integer parteId =
                            resultSet.getInt(
                                    "parte_id"
                            );


                    Optional<Parte> parte =
                            parteDAO.buscarPorId(
                                    parteId
                            );


                    if (parte.isEmpty()) {
                        continue;
                    }


                    ProgramacaoParte programacaoParte =
                            new ProgramacaoParte(
                                    resultSet.getInt("id"),
                                    parte.get(),
                                    resultSet.getInt("ordem"),
                                    resultSet.getString("tema")
                            );


                    partes.add(
                            programacaoParte
                    );
                }
            }
        }


        return partes;
    }


    public void excluir(
            int programacaoSemanaId,
            int parteId
    ) {

        String sql = """
            DELETE FROM programacao_parte
            WHERE programacao_semana_id = ?
              AND parte_id = ?
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    programacaoSemanaId
            );

            statement.setInt(
                    2,
                    parteId
            );


            int linhasAfetadas =
                    statement.executeUpdate();


            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Parte não encontrada na programação semanal."
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao excluir parte da programação.",
                    e
            );
        }
    }


    public void excluirPorSemana(
            int programacaoSemanaId
    ) {

        String sql = """
            DELETE FROM programacao_parte
            WHERE programacao_semana_id = ?
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    programacaoSemanaId
            );


            statement.executeUpdate();


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao excluir partes da programação.",
                    e
            );
        }
    }
}