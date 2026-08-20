package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class ProgramacaoSemanaDAO {

    private final ProgramacaoParteDAO programacaoParteDAO;


    public ProgramacaoSemanaDAO() {

        this.programacaoParteDAO =
                new ProgramacaoParteDAO();
    }


    public ProgramacaoSemana salvar(
            ProgramacaoSemana programacaoSemana
    ) {

        String sql = """
            INSERT INTO programacao_semana (
                data
            )
            VALUES (?)
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            statement.setDate(
                    1,
                    Date.valueOf(
                            programacaoSemana.getData()
                    )
            );


            statement.executeUpdate();


            Integer id;


            try (
                    ResultSet generatedKeys =
                            statement.getGeneratedKeys()
            ) {

                if (!generatedKeys.next()) {

                    throw new RuntimeException(
                            "Não foi possível obter o ID " +
                                    "da programação semanal."
                    );
                }


                id =
                        generatedKeys.getInt(1);
            }


            for (
                    ProgramacaoParte programacaoParte :
                    programacaoSemana.getPartes()
            ) {

                programacaoParteDAO.salvar(
                        id,
                        programacaoParte
                );
            }


            return new ProgramacaoSemana(
                    id,
                    programacaoSemana.getData(),
                    programacaoSemana.getPartes()
            );


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar programação semanal.",
                    e
            );
        }
    }


    public ProgramacaoSemana buscarPorData(
            LocalDate data
    ) {

        String sql = """
            SELECT
                id,
                data
            FROM programacao_semana
            WHERE data = ?
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setDate(
                    1,
                    Date.valueOf(data)
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (!resultSet.next()) {

                    return null;
                }


                Integer id =
                        resultSet.getInt("id");


                List<ProgramacaoParte> partes =
                        programacaoParteDAO.listarPorSemana(
                                id
                        );


                return new ProgramacaoSemana(
                        id,
                        resultSet.getDate("data")
                                .toLocalDate(),
                        partes
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar programação semanal.",
                    e
            );
        }
    }


    public void excluir(
            Integer id
    ) {

        String sql = """
            DELETE FROM programacao_semana
            WHERE id = ?
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    id
            );


            int linhasAfetadas =
                    statement.executeUpdate();


            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Programação semanal não encontrada."
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao excluir programação semanal.",
                    e
            );
        }
    }
}