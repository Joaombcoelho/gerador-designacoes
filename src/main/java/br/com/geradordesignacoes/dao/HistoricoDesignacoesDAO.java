package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParticipacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDesignacoesDAO {


    private final PessoaDAO pessoaDAO;

    private final ParteDAO parteDAO;



    public HistoricoDesignacoesDAO() {

        this.pessoaDAO =
                new PessoaDAO();

        this.parteDAO =
                new ParteDAO();
    }



    public void salvar(
            ParticipacaoDesignacao participacao
    ) {


        validarParticipacao(
                participacao
        );


        String sql = """
                INSERT OR IGNORE INTO historico_designacoes (
                    data,
                    pessoa_id,
                    parte_id,
                    tipo_participacao
                )
                VALUES (?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {


            statement.setString(
                    1,
                    participacao.getData().toString()
            );


            statement.setInt(
                    2,
                    participacao.getPessoa().getId()
            );


            statement.setInt(
                    3,
                    participacao.getParte().getId()
            );


            statement.setString(
                    4,
                    participacao.getTipoParticipacao().name()
            );


            statement.executeUpdate();


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar histórico de designação.",
                    e
            );
        }
    }



    public void substituirParticipacoesDaData(
            LocalDate data,
            List<ParticipacaoDesignacao> participacoes
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "Data não pode ser nula."
            );
        }

        if (participacoes == null) {
            throw new IllegalArgumentException(
                    "Participações não podem ser nulas."
            );
        }

        participacoes.forEach(participacao -> {
            validarParticipacao(participacao);

            if (!participacao.getData().equals(data)) {
                throw new IllegalArgumentException(
                        "Participação não pertence à data informada."
                );
            }
        });

        String sqlExcluir = """
                DELETE FROM historico_designacoes
                WHERE data = ?
                """;

        String sqlInserir = """
                INSERT INTO historico_designacoes (
                    data,
                    pessoa_id,
                    parte_id,
                    tipo_participacao
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection =
                     ConnectionFactory.getConnection()) {

            boolean autoCommitOriginal =
                    connection.getAutoCommit();

            try {
                connection.setAutoCommit(false);

                try (PreparedStatement excluir =
                             connection.prepareStatement(sqlExcluir)) {

                    excluir.setString(
                            1,
                            data.toString()
                    );

                    excluir.executeUpdate();
                }

                try (PreparedStatement inserir =
                             connection.prepareStatement(sqlInserir)) {

                    for (ParticipacaoDesignacao participacao : participacoes) {

                        inserir.setString(
                                1,
                                participacao.getData().toString()
                        );

                        inserir.setInt(
                                2,
                                participacao.getPessoa().getId()
                        );

                        inserir.setInt(
                                3,
                                participacao.getParte().getId()
                        );

                        inserir.setString(
                                4,
                                participacao.getTipoParticipacao().name()
                        );

                        inserir.addBatch();
                    }

                    inserir.executeBatch();
                }

                connection.commit();
                connection.setAutoCommit(autoCommitOriginal);

            } catch (SQLException e) {

                connection.rollback();
                connection.setAutoCommit(autoCommitOriginal);

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao substituir histórico da data.",
                    e
            );
        }
    }


    public List<ParticipacaoDesignacao> listarTodas() {


        List<ParticipacaoDesignacao> participacoes =
                new ArrayList<>();


        String sql = """
                SELECT *
                FROM historico_designacoes
                ORDER BY data, id
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {


            while (resultSet.next()) {


                participacoes.add(
                        mapearParticipacao(resultSet)
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar histórico.",
                    e
            );
        }


        return participacoes;
    }



    public HistoricoDesignacoes carregarHistorico() {


        HistoricoDesignacoes historico =
                new HistoricoDesignacoes();


        listarTodas()
                .forEach(
                        historico::adicionar
                );


        return historico;
    }



    private ParticipacaoDesignacao mapearParticipacao(
            ResultSet resultSet
    ) throws SQLException {


        Integer id =
                resultSet.getInt("id");


        LocalDate data =
                LocalDate.parse(
                        resultSet.getString("data")
                );


        int pessoaId =
                resultSet.getInt("pessoa_id");


        int parteId =
                resultSet.getInt("parte_id");



        Pessoa pessoa =
                pessoaDAO.buscarPorId(pessoaId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Pessoa não encontrada: "
                                                + pessoaId
                                )
                        );



        Parte parte =
                parteDAO.buscarPorId(parteId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Parte não encontrada: "
                                                + parteId
                                )
                        );



        TipoParticipacao tipoParticipacao =
                TipoParticipacao.valueOf(
                        resultSet.getString(
                                "tipo_participacao"
                        )
                );



        return new ParticipacaoDesignacao(
                id,
                data,
                pessoa,
                parte,
                tipoParticipacao
        );
    }



    private void validarParticipacao(
            ParticipacaoDesignacao participacao
    ) {


        if (participacao == null) {

            throw new IllegalArgumentException(
                    "Participação não pode ser nula."
            );
        }


        if (participacao.getPessoa() == null
                || participacao.getPessoa().getId() == null) {

            throw new IllegalArgumentException(
                    "Pessoa sem ID."
            );
        }


        if (participacao.getParte() == null
                || participacao.getParte().getId() == null) {

            throw new IllegalArgumentException(
                    "Parte sem ID."
            );
        }
    }

    public void limpar() {

        String sql = """
            DELETE FROM historico_designacoes
            """;

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao limpar histórico.",
                    e
            );
        }
    }

    public List<ParticipacaoDesignacao> buscarPorPessoa(
            Integer pessoaId
    ) {

        List<ParticipacaoDesignacao> participacoes =
                new ArrayList<>();


        String sql = """
            SELECT *
            FROM historico_designacoes
            WHERE pessoa_id = ?
            ORDER BY data, id
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    pessoaId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    participacoes.add(
                            mapearParticipacao(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar histórico da pessoa.",
                    e
            );
        }

        return participacoes;
    }

    public List<ParticipacaoDesignacao> buscarPorParte(
            Integer parteId
    ) {

        List<ParticipacaoDesignacao> participacoes =
                new ArrayList<>();


        String sql = """
            SELECT *
            FROM historico_designacoes
            WHERE parte_id = ?
            ORDER BY data, id
            """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    parteId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    participacoes.add(
                            mapearParticipacao(resultSet)
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar histórico da parte.",
                    e
            );
        }

        return participacoes;
    }

    public void salvarTodos(
            List<ParticipacaoDesignacao> participacoes
    ) {

        if (participacoes == null || participacoes.isEmpty()) {
            return;
        }

        for (ParticipacaoDesignacao participacao : participacoes) {
            salvar(participacao);
        }
    }

}