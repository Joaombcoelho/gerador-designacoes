package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.EventoEspecialRegistrado;
import br.com.geradordesignacoes.model.TipoEventoEspecial;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persistência isolada de eventos especiais.
 *
 * Este DAO não aplica eventos à programação e não acessa escalas,
 * designações, histórico ou ajustes manuais do S-140.
 */
public class EventoEspecialDAO {

    public EventoEspecialRegistrado salvar(EventoEspecial evento) {
        validarEvento(evento);
        String sql = """
                INSERT INTO evento_especial (
                    tipo, data_inicio, data_fim, tema_discurso, descricao
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            preencher(statement, evento);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Não foi possível obter o ID do evento.");
                }
                return new EventoEspecialRegistrado(keys.getLong(1), evento);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar evento especial.", e);
        }
    }

    public List<EventoEspecialRegistrado> listarTodos() {
        String sql = """
                SELECT id, tipo, data_inicio, data_fim, tema_discurso, descricao
                FROM evento_especial
                ORDER BY data_inicio, data_fim, id
                """;
        List<EventoEspecialRegistrado> eventos = new ArrayList<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                eventos.add(mapear(resultSet));
            }
            return List.copyOf(eventos);
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Erro ao listar eventos especiais.", e);
        }
    }

    public Optional<EventoEspecialRegistrado> buscarPorId(long id) {
        if (id <= 0) {
            return Optional.empty();
        }
        String sql = """
                SELECT id, tipo, data_inicio, data_fim, tema_discurso, descricao
                FROM evento_especial
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(mapear(resultSet))
                        : Optional.empty();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Erro ao buscar evento especial.", e);
        }
    }

    public EventoEspecialRegistrado atualizar(long id, EventoEspecial evento) {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID do evento deve ser positivo.");
        }
        validarEvento(evento);

        String sql = """
                UPDATE evento_especial
                SET tipo = ?, data_inicio = ?, data_fim = ?,
                    tema_discurso = ?, descricao = ?
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            preencher(statement, evento);
            statement.setLong(6, id);
            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "Evento especial não encontrado para atualização: " + id);
            }
            return new EventoEspecialRegistrado(id, evento);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar evento especial.", e);
        }
    }

    public boolean excluir(long id) {
        if (id <= 0) {
            return false;
        }
        String sql = "DELETE FROM evento_especial WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir evento especial.", e);
        }
    }

    private static void validarEvento(EventoEspecial evento) {
        if (evento == null) {
            throw new IllegalArgumentException("O evento especial é obrigatório.");
        }
    }

    private static void preencher(
            PreparedStatement statement,
            EventoEspecial evento
    ) throws SQLException {
        statement.setString(1, evento.tipo().name());
        statement.setDate(2, Date.valueOf(evento.dataInicio()));
        statement.setDate(3, Date.valueOf(evento.dataFim()));
        statement.setString(4, evento.temaDiscurso());
        statement.setString(5, evento.descricao());
    }

    private static EventoEspecialRegistrado mapear(ResultSet resultSet)
            throws SQLException {
        TipoEventoEspecial tipo = TipoEventoEspecial.valueOf(
                resultSet.getString("tipo"));
        LocalDate inicio = LocalDate.parse(resultSet.getString("data_inicio"));
        LocalDate fim = LocalDate.parse(resultSet.getString("data_fim"));
        EventoEspecial evento = new EventoEspecial(
                tipo,
                inicio,
                fim,
                resultSet.getString("tema_discurso"),
                resultSet.getString("descricao"));
        return new EventoEspecialRegistrado(resultSet.getLong("id"), evento);
    }
}
