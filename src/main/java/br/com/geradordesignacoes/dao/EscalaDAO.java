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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EscalaDAO {

    public Escala salvar(Escala escala) {

        if (escala == null)
        { throw new IllegalArgumentException(
                "A escala não pode ser nula."
        );
        }

        String buscarExistenteSql = """ 
SELECT id FROM escala WHERE data = ? 
""";
        String inserirSql = """ 
INSERT INTO escala 
    ( data, 
     status, 
     data_geracao, 
     data_salvamento ) 
VALUES (?, ?, ?, ?) 
""";
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                /* * Verifica se já existe uma escala para esta data.
                * */ Integer escalaExistenteId = null;
                try ( PreparedStatement statement = connection.prepareStatement(
                        buscarExistenteSql )
                )
                { statement.setString( 1, escala.getData().toString() );
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            escalaExistenteId = resultSet.getInt("id");
                        }
                    }
                }

                /* * Se já existe uma escala para a data, * reutiliza o ID e atualiza a escala. */
                if (escalaExistenteId != null)
                { escala.setId( escalaExistenteId );
                    String atualizarSql = """ 
UPDATE escala SET status = ?, data_geracao = ?, data_salvamento = ? WHERE id = ? """; try ( PreparedStatement statement = connection.prepareStatement( atualizarSql ) ) { statement.setString( 1, escala.getStatus().name() ); statement.setString( 2, escala.getDataGeracao().toString() ); if (escala.getDataSalvamento() == null) { statement.setNull( 3, java.sql.Types.VARCHAR ); } else { statement.setString( 3, escala.getDataSalvamento().toString() ); } statement.setInt( 4, escala.getId() ); int linhasAfetadas = statement.executeUpdate(); if (linhasAfetadas != 1) { throw new RuntimeException( "Erro ao atualizar escala." ); } } /* * Remove as designações antigas da escala * e grava as novas. */ excluirDesignacoes( connection, escala.getId() ); salvarDesignacoes( connection, escala ); connection.commit(); return escala; } /* * Não existe escala para esta data. * Cria uma nova. */ try ( PreparedStatement statement = connection.prepareStatement( inserirSql, PreparedStatement.RETURN_GENERATED_KEYS ) ) { statement.setString( 1, escala.getData().toString() ); statement.setString( 2, escala.getStatus().name() ); statement.setString( 3, escala.getDataGeracao().toString() ); if (escala.getDataSalvamento() == null) { statement.setNull( 4, java.sql.Types.VARCHAR ); } else { statement.setString( 4, escala.getDataSalvamento().toString() ); } int linhasAfetadas = statement.executeUpdate(); if (linhasAfetadas != 1) { throw new RuntimeException( "Erro ao salvar escala." ); } try ( ResultSet generatedKeys = statement.getGeneratedKeys() ) { if (generatedKeys.next()) { escala.setId( generatedKeys.getInt(1) ); } else { throw new RuntimeException( "Não foi possível obter o ID da escala." ); } } salvarDesignacoes( connection, escala ); connection.commit(); return escala; } } catch (SQLException | RuntimeException e) { connection.rollback(); throw e; } } catch (SQLException e) { throw new RuntimeException( "Erro ao salvar escala.", e ); } }

    public void atualizar(Escala escala) {

        if (escala == null) {
            throw new IllegalArgumentException(
                    "A escala não pode ser nula."
            );
        }

        if (escala.getId() == null) {
            throw new IllegalArgumentException(
                    "A escala precisa possuir ID para atualização."
            );
        }

        String sql = """
            UPDATE escala
            SET
                status = ?,
                data_salvamento = ?
            WHERE id = ?
            """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        escala.getStatus().name()
                );

                if (escala.getDataSalvamento() == null) {
                    statement.setNull(
                            2,
                            java.sql.Types.VARCHAR
                    );
                } else {
                    statement.setString(
                            2,
                            escala.getDataSalvamento().toString()
                    );
                }

                statement.setInt(
                        3,
                        escala.getId()
                );

                int linhasAfetadas =
                        statement.executeUpdate();

                if (linhasAfetadas != 1) {
                    throw new RuntimeException(
                            "Escala não encontrada para atualização."
                    );
                }

                excluirDesignacoes(
                        connection,
                        escala.getId()
                );

                salvarDesignacoes(
                        connection,
                        escala
                );

                connection.commit();

            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar escala.",
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


    public void atualizarDesignacao(
            Integer designacaoId,
            Integer responsavelId,
            Integer ajudanteId
    ) {

        if (designacaoId == null) {
            throw new IllegalArgumentException(
                    "ID da designação não pode ser nulo."
            );
        }

        if (responsavelId == null) {
            throw new IllegalArgumentException(
                    "ID do responsável não pode ser nulo."
            );
        }

        String sql = """
                UPDATE designacao
                SET responsavel_id = ?,
                    ajudante_id = ?
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
                    responsavelId
            );

            if (ajudanteId == null) {

                statement.setNull(
                        2,
                        java.sql.Types.INTEGER
                );

            } else {

                statement.setInt(
                        2,
                        ajudanteId
                );
            }

            statement.setInt(
                    3,
                    designacaoId
            );

            int linhasAfetadas =
                    statement.executeUpdate();

            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Designação não encontrada."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar designação.",
                    e
            );
        }
    }

    public void adicionarDesignacao(
            Integer escalaId,
            Integer parteId,
            Integer responsavelId,
            Integer ajudanteId
    ) {

        if (escalaId == null) {
            throw new IllegalArgumentException(
                    "ID da escala não pode ser nulo."
            );
        }

        if (parteId == null) {
            throw new IllegalArgumentException(
                    "ID da parte não pode ser nulo."
            );
        }

        if (responsavelId == null) {
            throw new IllegalArgumentException(
                    "ID do responsável não pode ser nulo."
            );
        }

        String sql = """
            INSERT INTO designacao (
                escala_id,
                parte_id,
                responsavel_id,
                ajudante_id
            )
            VALUES (?, ?, ?, ?)
            """;

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    escalaId
            );

            statement.setInt(
                    2,
                    parteId
            );

            statement.setInt(
                    3,
                    responsavelId
            );

            if (ajudanteId == null) {

                statement.setNull(
                        4,
                        java.sql.Types.INTEGER
                );

            } else {

                statement.setInt(
                        4,
                        ajudanteId
                );
            }

            int linhasAfetadas =
                    statement.executeUpdate();

            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Não foi possível adicionar a designação."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao adicionar designação.",
                    e
            );
        }
    }

    /**
     * Lista as datas de outras designações da pessoa no mês informado,
     * considerando tanto responsável quanto ajudante.
     */
    public List<LocalDate> listarDatasDeOutrasDesignacoesNoMes(
            Integer pessoaId,
            YearMonth mes,
            Integer designacaoIgnoradaId
    ) {

        if (pessoaId == null) {
            throw new IllegalArgumentException(
                    "ID da pessoa não pode ser nulo."
            );
        }

        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        String sql = """
                SELECT DISTINCT e.data
                FROM designacao d
                JOIN escala e ON e.id = d.escala_id
                WHERE (d.responsavel_id = ? OR d.ajudante_id = ?)
                  AND e.data >= ?
                  AND e.data < ?
                  AND (? IS NULL OR d.id <> ?)
                ORDER BY e.data
                """;

        List<LocalDate> datas = new ArrayList<>();

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, pessoaId);
            statement.setInt(2, pessoaId);
            statement.setString(3, mes.atDay(1).toString());
            statement.setString(4, mes.plusMonths(1).atDay(1).toString());

            if (designacaoIgnoradaId == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
                statement.setNull(6, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, designacaoIgnoradaId);
                statement.setInt(6, designacaoIgnoradaId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    datas.add(LocalDate.parse(resultSet.getString("data")));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao consultar designações da pessoa no mês.",
                    e
            );
        }

        return datas;
    }


    private void excluirDesignacoes(
            Connection connection,
            Integer escalaId
    ) throws SQLException {

        String sql = """
            DELETE FROM designacao
            WHERE escala_id = ?
            """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    escalaId
            );

            statement.executeUpdate();
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

                statement.setInt(
                        1,
                        escala.getId()
                );

                statement.setInt(
                        2,
                        designacao.parte().getId()
                );

                statement.setInt(
                        3,
                        designacao.responsavel().getId()
                );

                if (designacao.ajudante() == null) {

                    statement.setNull(
                            4,
                            java.sql.Types.INTEGER
                    );

                } else {

                    statement.setInt(
                            4,
                            designacao.ajudante().getId()
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
                    id,
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

            statement.setInt(
                    1,
                    escalaId
            );

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    Parte parte =
                            parteDAO.buscarPorId(
                                    resultSet.getInt("parte_id")
                            ).orElseThrow(() ->
                                    new RuntimeException(
                                            "Parte não encontrada."
                                    )
                            );

                    Pessoa responsavel =
                            pessoaDAO.buscarPorId(
                                    resultSet.getInt("responsavel_id")
                            ).orElseThrow(() ->
                                    new RuntimeException(
                                            "Responsável não encontrado."
                                    )
                            );

                    Pessoa ajudante = null;

                    int ajudanteId =
                            resultSet.getInt("ajudante_id");

                    if (!resultSet.wasNull()) {

                        ajudante =
                                pessoaDAO.buscarPorId(
                                        ajudanteId
                                ).orElseThrow(() ->
                                        new RuntimeException(
                                                "Ajudante não encontrado."
                                        )
                                );
                    }

                    designacoes.add(
                            new Designacao(
                                    resultSet.getInt("id"),
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
    private Designacao buscarDesignacaoPorId(
            Connection connection,
            Integer designacaoId
    ) throws SQLException {

        String sql = """
            SELECT
                d.id,
                d.escala_id,
                d.parte_id,
                d.responsavel_id,
                d.ajudante_id,
                e.data
            FROM designacao d
            JOIN escala e
                ON e.id = d.escala_id
            WHERE d.id = ?
            """;

        ParteDAO parteDAO = new ParteDAO();
        PessoaDAO pessoaDAO = new PessoaDAO();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    designacaoId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                if (!resultSet.next()) {

                    throw new RuntimeException(
                            "Designação não encontrada após inclusão."
                    );
                }

                Parte parte =
                        parteDAO.buscarPorId(
                                resultSet.getInt("parte_id")
                        ).orElseThrow(() ->
                                new RuntimeException(
                                        "Parte não encontrada."
                                )
                        );

                Pessoa responsavel =
                        pessoaDAO.buscarPorId(
                                resultSet.getInt("responsavel_id")
                        ).orElseThrow(() ->
                                new RuntimeException(
                                        "Responsável não encontrado."
                                )
                        );

                Pessoa ajudante = null;

                int ajudanteId =
                        resultSet.getInt("ajudante_id");

                if (!resultSet.wasNull()) {

                    ajudante =
                            pessoaDAO.buscarPorId(
                                    ajudanteId
                            ).orElseThrow(() ->
                                    new RuntimeException(
                                            "Ajudante não encontrado."
                                    )
                            );
                }

                return new Designacao(
                        resultSet.getInt("id"),
                        LocalDate.parse(
                                resultSet.getString("data")
                        ),
                        parte,
                        responsavel,
                        ajudante
                );
            }
        }
    }
}