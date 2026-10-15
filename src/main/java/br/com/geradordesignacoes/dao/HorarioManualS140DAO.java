package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Persiste somente os horários ajustados manualmente.
 * Os horários calculados são sempre derivados dos dados da programação.
 */
public class HorarioManualS140DAO {

    public Map<String, LocalTime> listarPorData(LocalDate data) {
        validarData(data);
        String sql = """
                SELECT chave_item, horario_inicio
                FROM horario_manual_s140
                WHERE data = ?
                """;
        Map<String, LocalTime> horarios = new LinkedHashMap<>();

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(data));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    horarios.put(
                            resultSet.getString("chave_item"),
                            LocalTime.parse(resultSet.getString("horario_inicio"))
                    );
                }
            }
            return Map.copyOf(horarios);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao consultar horários manuais do S-140.", e);
        }
    }

    public void salvar(LocalDate data, String chaveItem, LocalTime horarioInicio) {
        validarData(data);
        if (chaveItem == null || chaveItem.isBlank()) {
            throw new IllegalArgumentException("A chave do item é obrigatória.");
        }
        if (horarioInicio == null) {
            throw new IllegalArgumentException("O horário manual não pode ser nulo.");
        }

        String sql = """
                INSERT INTO horario_manual_s140 (data, chave_item, horario_inicio)
                VALUES (?, ?, ?)
                ON CONFLICT(data, chave_item)
                DO UPDATE SET horario_inicio = excluded.horario_inicio
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(data));
            statement.setString(2, chaveItem.trim());
            statement.setString(3, horarioInicio.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar horário manual do S-140.", e);
        }
    }

    public void remover(LocalDate data, String chaveItem) {
        validarData(data);
        if (chaveItem == null || chaveItem.isBlank()) {
            throw new IllegalArgumentException("A chave do item é obrigatória.");
        }

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     DELETE FROM horario_manual_s140
                     WHERE data = ? AND chave_item = ?
                     """)) {
            statement.setDate(1, Date.valueOf(data));
            statement.setString(2, chaveItem.trim());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao remover horário manual do S-140.", e);
        }
    }

    private void validarData(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException("A data da programação é obrigatória.");
        }
    }
}
