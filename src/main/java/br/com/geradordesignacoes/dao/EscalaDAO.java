package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;

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

        if (escala == null) {
            throw new IllegalArgumentException(
                    "A escala não pode ser nula."
            );
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                Escala salva = salvar(connection, escala);
                connection.commit();
                return salva;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            } finally {
                if (connection.getAutoCommit() != autoCommitOriginal) {
                    connection.setAutoCommit(autoCommitOriginal);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar escala.", e);
        }
    }

    /**
     * Salva uma escala na conexão fornecida, sem confirmar nem encerrar
     * a transação. Permite coordenar a persistência de várias escalas
     * com o histórico em uma única transação.
     */
    public Escala salvar(Connection connection, Escala escala)
            throws SQLException {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "A conexão não pode ser nula."
            );
        }
        if (escala == null) {
            throw new IllegalArgumentException(
                    "A escala não pode ser nula."
            );
        }

        String buscarExistenteSql = """
                SELECT id FROM escala WHERE data = ?
                """;
        String inserirSql = """
                INSERT INTO escala
                    (data, status, data_geracao, data_salvamento)
                VALUES (?, ?, ?, ?)
                """;

        Integer escalaExistenteId = null;
        try (PreparedStatement statement =
                     connection.prepareStatement(buscarExistenteSql)) {
            statement.setString(1, escala.getData().toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    escalaExistenteId = resultSet.getInt("id");
                }
            }
        }

        if (escalaExistenteId != null) {
            escala.setId(escalaExistenteId);
            String atualizarSql = """
                    UPDATE escala
                    SET status = ?, data_geracao = ?, data_salvamento = ?
                    WHERE id = ?
                    """;
            try (PreparedStatement statement =
                         connection.prepareStatement(atualizarSql)) {
                statement.setString(1, escala.getStatus().name());
                statement.setString(2, escala.getDataGeracao().toString());
                if (escala.getDataSalvamento() == null) {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                } else {
                    statement.setString(3, escala.getDataSalvamento().toString());
                }
                statement.setInt(4, escala.getId());
                if (statement.executeUpdate() != 1) {
                    throw new SQLException("Erro ao atualizar escala.");
                }
            }
            salvarPapeisEspeciais(connection, escala);
            excluirDesignacoes(connection, escala.getId());
            salvarDesignacoes(connection, escala);
            return escala;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                inserirSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, escala.getData().toString());
            statement.setString(2, escala.getStatus().name());
            statement.setString(3, escala.getDataGeracao().toString());
            if (escala.getDataSalvamento() == null) {
                statement.setNull(4, java.sql.Types.VARCHAR);
            } else {
                statement.setString(4, escala.getDataSalvamento().toString());
            }
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Erro ao salvar escala.");
            }
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    escala.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException(
                            "Não foi possível obter o ID da escala."
                    );
                }
            }
        }

        salvarPapeisEspeciais(connection, escala);
        salvarDesignacoes(connection, escala);
        return escala;
    }

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

                salvarPapeisEspeciais(connection, escala);

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

                PessoaDAO pessoaDAO = new PessoaDAO();
                // As designações salvas são a fonte atual dos responsáveis.
                // As colunas especiais são mantidas por compatibilidade com escalas
                // antigas, mas não devem sobrescrever uma edição manual mais recente.
                escala.setPresidente(responsavelAtualOuLegado(
                        escala.getDesignacoes(), TipoParte.PRESIDENTE_REUNIAO,
                        pessoaDAO, resultSet, "presidente_id"));
                escala.setDirigente(responsavelAtualOuLegado(
                        escala.getDesignacoes(), TipoParte.DIRIGENTE_ESTUDO,
                        pessoaDAO, resultSet, "dirigente_id"));
                escala.setOracaoInicial(responsavelAtualOuLegado(
                        escala.getDesignacoes(), TipoParte.ORACAO_INICIAL,
                        pessoaDAO, resultSet, "oracao_inicial_id"));

                return Optional.of(escala);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar escala.",
                    e
            );
        }
    }

    public Optional<Escala> buscarPorData(LocalDate data) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data da escala não pode ser nula."
            );
        }

        String sql = """
                SELECT id
                FROM escala
                WHERE data = ?
                """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, data.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return buscarPorId(resultSet.getInt("id"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao buscar escala pela data.",
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

        try (Connection connection = ConnectionFactory.getConnection()) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                String tipoParte;
                int escalaId;
                String buscarContextoSql = """
                        SELECT d.escala_id, p.tipo
                        FROM designacao d
                        JOIN parte p ON p.id = d.parte_id
                        WHERE d.id = ?
                        """;
                try (PreparedStatement statement =
                             connection.prepareStatement(buscarContextoSql)) {
                    statement.setInt(1, designacaoId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new IllegalArgumentException(
                                    "Designação não encontrada."
                            );
                        }
                        escalaId = resultSet.getInt("escala_id");
                        tipoParte = resultSet.getString("tipo");
                    }
                }

                String atualizarSql = """
                        UPDATE designacao
                        SET responsavel_id = ?,
                            ajudante_id = ?
                        WHERE id = ?
                        """;
                try (PreparedStatement statement =
                             connection.prepareStatement(atualizarSql)) {
                    if (responsavelId == null) {
                        statement.setNull(1, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(1, responsavelId);
                    }

                    if (ajudanteId == null) {
                        statement.setNull(2, java.sql.Types.INTEGER);
                    } else {
                        statement.setInt(2, ajudanteId);
                    }
                    statement.setInt(3, designacaoId);

                    if (statement.executeUpdate() != 1) {
                        throw new IllegalArgumentException(
                                "Designação não encontrada."
                        );
                    }
                }

                // Esses papéis também são armazenados em colunas próprias de escala.
                // Mantê-los sincronizados evita que o S-140 leia o responsável antigo.
                String colunaPapelEspecial = switch (TipoParte.valueOf(tipoParte)) {
                    case PRESIDENTE_REUNIAO -> "presidente_id";
                    case DIRIGENTE_ESTUDO -> "dirigente_id";
                    case ORACAO_INICIAL -> "oracao_inicial_id";
                    default -> null;
                };
                if (colunaPapelEspecial != null) {
                    String atualizarPapelSql = "UPDATE escala SET "
                            + colunaPapelEspecial + " = ? WHERE id = ?";
                    try (PreparedStatement statement =
                                 connection.prepareStatement(atualizarPapelSql)) {
                        if (responsavelId == null) {
                            statement.setNull(1, java.sql.Types.INTEGER);
                        } else {
                            statement.setInt(1, responsavelId);
                        }
                        statement.setInt(2, escalaId);
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Não foi possível sincronizar o papel especial da escala."
                            );
                        }
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommitOriginal);
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
        adicionarDesignacao(
                escalaId,
                parteId,
                responsavelId,
                ajudanteId,
                SalaDesignacao.PRINCIPAL
        );
    }

    public void adicionarDesignacao(
            Integer escalaId,
            Integer parteId,
            Integer responsavelId,
            Integer ajudanteId,
            SalaDesignacao sala
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

        if (sala == null) {
            throw new IllegalArgumentException(
                    "A sala não pode ser nula."
            );
        }

        String sql = """
            INSERT INTO designacao (
                escala_id,
                parte_id,
                responsavel_id,
                ajudante_id,
                sala
            )
            VALUES (?, ?, ?, ?, ?)
            """;

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, escalaId);
            statement.setInt(2, parteId);

            if (responsavelId == null) {
                statement.setNull(3, java.sql.Types.INTEGER);
            } else {
                statement.setInt(3, responsavelId);
            }

            if (ajudanteId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, ajudanteId);
            }

            statement.setString(5, sala.name());

            int linhasAfetadas = statement.executeUpdate();

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


    private void salvarPapeisEspeciais(
            Connection connection,
            Escala escala
    ) throws SQLException {
        String sql = """
                UPDATE escala
                SET presidente_id = ?,
                    dirigente_id = ?,
                    oracao_inicial_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            definirPessoaOpcional(statement, 1, escala.getPresidente());
            definirPessoaOpcional(statement, 2, escala.getDirigente());
            definirPessoaOpcional(statement, 3, escala.getOracaoInicial());
            statement.setInt(4, escala.getId());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Não foi possível salvar os papéis especiais da escala.");
            }
        }
    }

    private void definirPessoaOpcional(
            PreparedStatement statement,
            int indice,
            Pessoa pessoa
    ) throws SQLException {
        if (pessoa == null || pessoa.getId() == null) {
            statement.setNull(indice, java.sql.Types.INTEGER);
        } else {
            statement.setInt(indice, pessoa.getId());
        }
    }


    private Pessoa responsavelAtualOuLegado(
            List<Designacao> designacoes,
            TipoParte tipo,
            PessoaDAO pessoaDAO,
            ResultSet resultSet,
            String colunaLegada
    ) throws SQLException {
        List<Designacao> designacoesDoPapel = designacoes.stream()
                .filter(d -> d.parte().getTipo() == tipo)
                .toList();
        if (!designacoesDoPapel.isEmpty()) {
            return designacoesDoPapel.stream()
                    .map(Designacao::responsavel)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }
        return buscarPessoaOpcional(pessoaDAO, resultSet, colunaLegada);
    }

    private Pessoa buscarPessoaOpcional(
            PessoaDAO pessoaDAO,
            ResultSet resultSet,
            String coluna
    ) throws SQLException {
        int pessoaId = resultSet.getInt(coluna);
        if (resultSet.wasNull()) {
            return null;
        }
        return pessoaDAO.buscarPorId(pessoaId).orElseThrow(() ->
                new RuntimeException("Pessoa referenciada em " + coluna + " não encontrada."));
    }

    private void salvarDesignacoes(Connection connection, Escala escala) throws SQLException {
        String sql =""" 
            INSERT INTO designacao ( 
            escala_id, 
            parte_id, 
            responsavel_id, 
            ajudante_id, 
            sala 
            ) 
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Designacao designacao : escala.getDesignacoes()) {
                statement.setInt(1, escala.getId());
                statement.setInt(2, designacao.parte().getId());
                if (designacao.responsavel() == null) {
                    statement.setNull(3, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(3, designacao.responsavel().getId());
                }
                if (designacao.ajudante() == null) {
                    statement.setNull(4, java.sql.Types.INTEGER);
                } else {
                    statement.setInt(4, designacao.ajudante().getId());
                }
                statement.setString(5, designacao.sala().name());
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
            d.id,
            d.parte_id,
            d.responsavel_id,
            d.ajudante_id,
            d.sala
        FROM designacao d
        LEFT JOIN programacao_semana ps
            ON ps.data = ?
        LEFT JOIN programacao_parte pp
            ON pp.programacao_semana_id = ps.id
           AND pp.parte_id = d.parte_id
        WHERE d.escala_id = ?
        ORDER BY
            CASE
                WHEN pp.ordem IS NULL THEN 1
                ELSE 0
            END,
            pp.ordem,
            d.id
        """;

        List<Designacao> designacoes = new ArrayList<>();

        ParteDAO parteDAO = new ParteDAO();
        PessoaDAO pessoaDAO = new PessoaDAO();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    data.toString()
            );

            statement.setInt(
                    2,
                    escalaId
            );

            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                while (resultSet.next()) {

                    Parte parte =
                            parteDAO.buscarPorId(
                                    resultSet.getInt("parte_id")
                            ).orElseThrow(() ->
                                    new RuntimeException(
                                            "Parte não encontrada."
                                    )
                            );

                    Pessoa responsavel = null;
                    int responsavelId = resultSet.getInt("responsavel_id");
                    if (!resultSet.wasNull()) {
                        responsavel = pessoaDAO.buscarPorId(responsavelId)
                                .orElseThrow(() -> new RuntimeException(
                                        "Responsável não encontrado."
                                ));
                    }

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
                                    ajudante,
                                    SalaDesignacao.valueOf(resultSet.getString("sala"))
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
                    e.data, 
                    d.sala
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

                Pessoa responsavel = null;
                int responsavelId = resultSet.getInt("responsavel_id");
                if (!resultSet.wasNull()) {
                    responsavel = pessoaDAO.buscarPorId(responsavelId)
                            .orElseThrow(() -> new RuntimeException(
                                    "Responsável não encontrado."
                            ));
                }

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
                        ajudante,
                        SalaDesignacao.valueOf(resultSet.getString("sala"))
                );
            }
        }
    }
}