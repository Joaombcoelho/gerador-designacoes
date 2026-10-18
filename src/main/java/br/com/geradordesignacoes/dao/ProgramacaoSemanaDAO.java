package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgramacaoSemanaDAO {

    private final ProgramacaoParteDAO programacaoParteDAO;

    public ProgramacaoSemanaDAO() {
        this.programacaoParteDAO = new ProgramacaoParteDAO();
    }

    public ProgramacaoSemana salvar(ProgramacaoSemana programacaoSemana) {
        String sql = """
                INSERT INTO programacao_semana (
                    data, sala_b_habilitada, sala_c_habilitada,
                    conselheiro_sala_b_id, cantico_inicial, cantico_meio, cantico_final
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            connection.setAutoCommit(false);

            try {
                statement.setDate(1, Date.valueOf(programacaoSemana.data()));
                statement.setBoolean(2, programacaoSemana.salaBHabilitada());
                statement.setBoolean(3, programacaoSemana.salaCHabilitada());

                if (programacaoSemana.conselheiroSalaBId() == null) {
                    statement.setNull(4, Types.INTEGER);
                } else {
                    statement.setInt(4, programacaoSemana.conselheiroSalaBId());
                }
                statement.setString(5, programacaoSemana.canticoInicial());
                statement.setString(6, programacaoSemana.canticoMeio());
                statement.setString(7, programacaoSemana.canticoFinal());

                statement.executeUpdate();

                int id;
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (!generatedKeys.next()) {
                        throw new SQLException(
                                "Não foi possível obter o ID da programação semanal.");
                    }
                    id = generatedKeys.getInt(1);
                }

                for (ProgramacaoParte programacaoParte : programacaoSemana.partes()) {
                    programacaoParteDAO.salvar(connection, id, programacaoParte);
                }

                connection.commit();

                return new ProgramacaoSemana(
                        id,
                        programacaoSemana.data(),
                        programacaoSemana.partes(),
                        programacaoSemana.salaBHabilitada(),
                        programacaoSemana.salaCHabilitada(),
                        programacaoSemana.conselheiroSalaBId(),
                        programacaoSemana.canticoInicial(),
                        programacaoSemana.canticoMeio(),
                        programacaoSemana.canticoFinal()
                );
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar programação semanal.", e);
        }
    }

    /**
     * Persiste várias programações novas numa única transação.
     * Recusa programações existentes já configuradas. Reutiliza somente rascunhos
     * vazios criados pela tela, preservando a transação atômica.
     */
    public List<ProgramacaoSemana> salvarTodasNovas(List<ProgramacaoSemana> programacoes) {
        if (programacoes == null || programacoes.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma programação para importar.");
        }

        Set<LocalDate> datas = new HashSet<>();
        for (ProgramacaoSemana programacao : programacoes) {
            if (programacao == null || programacao.data() == null) {
                throw new IllegalArgumentException("Todas as programações precisam ter uma data.");
            }
            if (!datas.add(programacao.data())) {
                throw new IllegalArgumentException(
                        "A data " + programacao.data() + " aparece mais de uma vez na importação.");
            }
            if (programacao.id() != null) {
                throw new IllegalArgumentException(
                        "A importação aceita somente programações novas, sem ID persistido.");
            }
        }

        String insert = """
                INSERT INTO programacao_semana (
                    data, sala_b_habilitada, sala_c_habilitada,
                    conselheiro_sala_b_id, cantico_inicial, cantico_meio, cantico_final
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (ProgramacaoSemana programacao : programacoes) {
                    Integer existenteId = buscarIdPorData(connection, programacao.data());
                    if (existenteId != null && !ehRascunhoVazio(connection, existenteId)) {
                        throw new IllegalArgumentException(
                                "Já existe uma programação configurada para " + programacao.data()
                                        + ". Nenhuma alteração foi gravada.");
                    }
                }

                List<ProgramacaoSemana> salvas = new ArrayList<>();
                for (ProgramacaoSemana programacao : programacoes) {
                    Integer existenteId = buscarIdPorData(connection, programacao.data());
                    int id;
                    if (existenteId != null) {
                        id = existenteId;
                        try (PreparedStatement statement = connection.prepareStatement("""
                                UPDATE programacao_semana
                                SET sala_b_habilitada = ?, sala_c_habilitada = ?,
                                    conselheiro_sala_b_id = ?, cantico_inicial = ?,
                                    cantico_meio = ?, cantico_final = ?
                                WHERE id = ?
                                """)) {
                            statement.setBoolean(1, programacao.salaBHabilitada());
                            statement.setBoolean(2, programacao.salaCHabilitada());
                            if (programacao.conselheiroSalaBId() == null) {
                                statement.setNull(3, Types.INTEGER);
                            } else {
                                statement.setInt(3, programacao.conselheiroSalaBId());
                            }
                            statement.setString(4, programacao.canticoInicial());
                            statement.setString(5, programacao.canticoMeio());
                            statement.setString(6, programacao.canticoFinal());
                            statement.setInt(7, id);
                            statement.executeUpdate();
                        }
                        try (PreparedStatement statement = connection.prepareStatement(
                                "DELETE FROM programacao_parte WHERE programacao_semana_id = ?")) {
                            statement.setInt(1, id);
                            statement.executeUpdate();
                        }
                    } else {
                        try (PreparedStatement statement = connection.prepareStatement(
                                insert, Statement.RETURN_GENERATED_KEYS)) {
                            statement.setDate(1, Date.valueOf(programacao.data()));
                            statement.setBoolean(2, programacao.salaBHabilitada());
                            statement.setBoolean(3, programacao.salaCHabilitada());
                            if (programacao.conselheiroSalaBId() == null) {
                                statement.setNull(4, Types.INTEGER);
                            } else {
                                statement.setInt(4, programacao.conselheiroSalaBId());
                            }
                            statement.setString(5, programacao.canticoInicial());
                            statement.setString(6, programacao.canticoMeio());
                            statement.setString(7, programacao.canticoFinal());
                            statement.executeUpdate();
                            try (ResultSet keys = statement.getGeneratedKeys()) {
                                if (!keys.next()) {
                                    throw new SQLException(
                                            "Não foi possível obter o ID da programação semanal.");
                                }
                                id = keys.getInt(1);
                            }
                        }
                    }

                    for (ProgramacaoParte parte : programacao.partes()) {
                        programacaoParteDAO.salvar(connection, id, parte);
                    }
                    salvas.add(new ProgramacaoSemana(
                            id, programacao.data(), programacao.partes(),
                            programacao.salaBHabilitada(), programacao.salaCHabilitada(),
                            programacao.conselheiroSalaBId(), programacao.canticoInicial(),
                            programacao.canticoMeio(), programacao.canticoFinal()));
                }

                connection.commit();
                return List.copyOf(salvas);
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Falha ao importar as programações; a transação foi cancelada.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao importar as programações semanais.", e);
        }
    }

    /**
     * Retorna true apenas para uma programação vazia criada como estrutura
     * inicial: sem partes variáveis, metadados manuais ou alterações nas partes fixas.
     */
    public boolean podeReutilizarRascunhoVazio(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data não pode ser nula.");
        }
        try (Connection connection = ConnectionFactory.getConnection()) {
            Integer id = buscarIdPorData(connection, data);
            return id != null && ehRascunhoVazio(connection, id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar programação vazia.", e);
        }
    }

    private Integer buscarIdPorData(Connection connection, LocalDate data) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM programacao_semana WHERE data = ?")) {
            statement.setDate(1, Date.valueOf(data));
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("id") : null;
            }
        }
    }

    private boolean ehRascunhoVazio(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT conselheiro_sala_b_id, cantico_inicial, cantico_meio, cantico_final
                FROM programacao_semana WHERE id = ?
                """)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()
                        || resultSet.getObject("conselheiro_sala_b_id") != null
                        || naoVazio(resultSet.getString("cantico_inicial"))
                        || naoVazio(resultSet.getString("cantico_meio"))
                        || naoVazio(resultSet.getString("cantico_final"))) {
                    return false;
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM programacao_parte pp
                JOIN parte p ON p.id = pp.parte_id
                WHERE pp.programacao_semana_id = ?
                  AND (
                    COALESCE(p.tipo_variacao, 'FIXA') = 'VARIAVEL'
                    OR TRIM(COALESCE(pp.tema, '')) <> ''
                    OR pp.duracao_minutos IS NOT p.duracao_padrao_minutos
                    OR (
                        pp.numero_oficial IS NOT NULL
                        AND NOT (p.tipo = 'LEITURA' AND pp.numero_oficial = 3)
                    )
                  )
                """)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 0;
            }
        }
    }

    private static boolean naoVazio(String valor) {
        return valor != null && !valor.isBlank();
    }

    /**
     * Lista as datas de programações já persistidas dentro de um intervalo,
     * incluindo reuniões em dias diferentes do dia habitual configurado.
     */
    public List<LocalDate> listarDatasEntre(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null || fim.isBefore(inicio)) {
            throw new IllegalArgumentException("Informe um intervalo de datas válido.");
        }

        String sql = """
                SELECT data
                FROM programacao_semana
                WHERE data >= ? AND data <= ?
                ORDER BY data
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(inicio));
            statement.setDate(2, Date.valueOf(fim));

            List<LocalDate> datas = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    datas.add(resultSet.getDate("data").toLocalDate());
                }
            }
            return List.copyOf(datas);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar datas de programações semanais.", e);
        }
    }

    public ProgramacaoSemana buscarPorData(LocalDate data) {
        String sql = """
                SELECT
                    id,
                    data,
                    sala_b_habilitada,
                    sala_c_habilitada,
                    conselheiro_sala_b_id,
                    cantico_inicial,
                    cantico_meio,
                    cantico_final
                FROM programacao_semana
                WHERE data = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(data));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                int id = resultSet.getInt("id");
                int conselheiroIdValor = resultSet.getInt("conselheiro_sala_b_id");
                Integer conselheiroId = resultSet.wasNull() ? null : conselheiroIdValor;

                List<ProgramacaoParte> partes =
                        programacaoParteDAO.listarPorSemana(connection, id);

                return new ProgramacaoSemana(
                        id,
                        resultSet.getDate("data").toLocalDate(),
                        partes,
                        resultSet.getBoolean("sala_b_habilitada"),
                        resultSet.getBoolean("sala_c_habilitada"),
                        conselheiroId,
                        resultSet.getString("cantico_inicial"),
                        resultSet.getString("cantico_meio"),
                        resultSet.getString("cantico_final")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar programação semanal.", e);
        }
    }

    public void atualizarDadosS140(
            LocalDate data,
            Integer conselheiroSalaBId,
            String canticoInicial,
            String canticoMeio,
            String canticoFinal
    ) {
        String sql = """
                UPDATE programacao_semana
                SET conselheiro_sala_b_id = ?,
                    cantico_inicial = ?,
                    cantico_meio = ?,
                    cantico_final = ?
                WHERE data = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (conselheiroSalaBId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, conselheiroSalaBId);
            }
            statement.setString(2, normalizar(canticoInicial));
            statement.setString(3, normalizar(canticoMeio));
            statement.setString(4, normalizar(canticoFinal));
            statement.setDate(5, Date.valueOf(data));

            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "Programação semanal não encontrada para atualizar os dados do S-140."
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro ao atualizar os dados do S-140.", e
            );
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    public void excluir(Integer id) {
        String sql = """
                DELETE FROM programacao_semana
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            int linhasAfetadas = statement.executeUpdate();

            if (linhasAfetadas != 1) {
                throw new RuntimeException("Programação semanal não encontrada.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir programação semanal.", e);
        }
    }
}