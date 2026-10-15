package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalTime;

/** Persistência da configuração geral única do aplicativo. */
public class ConfiguracaoGeralDAO {

    public ConfiguracaoGeral buscar() {
        String sql = """
                SELECT nome_congregacao, dia_reuniao_habitual, horario_inicio,
                       duracao_cantico_oracao_inicial_minutos,
                       duracao_comentarios_iniciais_minutos,
                       duracao_cantico_meio_minutos,
                       duracao_comentarios_finais_minutos,
                       duracao_cantico_oracao_final_minutos
                FROM configuracoes_gerais
                WHERE id = 1
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                return ConfiguracaoGeral.padrao();
            }

            return new ConfiguracaoGeral(
                    resultSet.getString("nome_congregacao"),
                    DayOfWeek.valueOf(resultSet.getString("dia_reuniao_habitual")),
                    LocalTime.parse(resultSet.getString("horario_inicio")),
                    resultSet.getInt("duracao_cantico_oracao_inicial_minutos"),
                    resultSet.getInt("duracao_comentarios_iniciais_minutos"),
                    resultSet.getInt("duracao_cantico_meio_minutos"),
                    resultSet.getInt("duracao_comentarios_finais_minutos"),
                    resultSet.getInt("duracao_cantico_oracao_final_minutos")
            );
        } catch (SQLException | IllegalArgumentException e) {
            throw new RuntimeException("Erro ao buscar configurações gerais.", e);
        }
    }

    public void salvar(ConfiguracaoGeral configuracao) {
        if (configuracao == null) {
            throw new IllegalArgumentException(
                    "A configuração geral não pode ser nula."
            );
        }

        String sql = """
                INSERT INTO configuracoes_gerais (
                    id, nome_congregacao, dia_reuniao_habitual, horario_inicio,
                    duracao_cantico_oracao_inicial_minutos,
                    duracao_comentarios_iniciais_minutos,
                    duracao_cantico_meio_minutos,
                    duracao_comentarios_finais_minutos,
                    duracao_cantico_oracao_final_minutos
                )
                VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    nome_congregacao = excluded.nome_congregacao,
                    dia_reuniao_habitual = excluded.dia_reuniao_habitual,
                    horario_inicio = excluded.horario_inicio,
                    duracao_cantico_oracao_inicial_minutos = excluded.duracao_cantico_oracao_inicial_minutos,
                    duracao_comentarios_iniciais_minutos = excluded.duracao_comentarios_iniciais_minutos,
                    duracao_cantico_meio_minutos = excluded.duracao_cantico_meio_minutos,
                    duracao_comentarios_finais_minutos = excluded.duracao_comentarios_finais_minutos,
                    duracao_cantico_oracao_final_minutos = excluded.duracao_cantico_oracao_final_minutos
                """;

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, configuracao.nomeCongregacao());
            statement.setString(2, configuracao.diaReuniaoHabitual().name());
            statement.setString(3, configuracao.horarioInicio().toString());
            statement.setInt(4, configuracao.duracaoCanticoOracaoInicialMinutos());
            statement.setInt(5, configuracao.duracaoComentariosIniciaisMinutos());
            statement.setInt(6, configuracao.duracaoCanticoMeioMinutos());
            statement.setInt(7, configuracao.duracaoComentariosFinaisMinutos());
            statement.setInt(8, configuracao.duracaoCanticoOracaoFinalMinutos());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar configurações gerais.", e);
        }
    }
}
