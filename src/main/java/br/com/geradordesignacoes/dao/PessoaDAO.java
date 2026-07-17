package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PessoaDAO {

    public void salvar(Pessoa pessoa) {

        String sql = """
            INSERT INTO pessoa (
                nome,
                sexo,
                ativo,
                pode_ser_responsavel,
                pode_ser_ajudante,
                pode_fazer_leitura,
                pode_fazer_discurso,
                privilegio
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        PreparedStatement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setString(1, pessoa.getNome());
            statement.setString(2, pessoa.getSexo().name());
            statement.setBoolean(3, pessoa.isAtivo());
            statement.setBoolean(4, pessoa.podeSerResponsavel());
            statement.setBoolean(5, pessoa.podeSerAjudante());
            statement.setBoolean(6, pessoa.podeFazerLeitura());
            statement.setBoolean(7, pessoa.podeFazerDiscurso());
            statement.setString(8, pessoa.getPrivilegio().name());

            int linhasAfetadas = statement.executeUpdate();

            if (linhasAfetadas != 1) {
                throw new RuntimeException("Erro ao salvar a pessoa.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (generatedKeys.next()) {
                    pessoa.setId(generatedKeys.getInt(1));
                } else {
                    throw new RuntimeException("Não foi possível obter o ID gerado.");
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pessoa.", e);
        }
    }

    public List<Pessoa> listarTodos() {

        List<Pessoa> pessoas = new ArrayList<>();

        String sql = """
            SELECT *
            FROM pessoa
            """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()
        ) {

            while (resultSet.next()) {
                pessoas.add(mapearPessoa(resultSet));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pessoas.", e);
        }

        return pessoas;
    }

    public Optional<Pessoa> buscarPorId(Integer id) {

        String sql = """
            SELECT *
            FROM pessoa
            WHERE id = ?
            """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return Optional.of(mapearPessoa(resultSet));
                }

            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pessoa por ID.", e);
        }

        return Optional.empty();
    }

    public void atualizar(Pessoa pessoa) {

        if (pessoa.getId() == null) {
            throw new IllegalArgumentException(
                    "A pessoa precisa possuir um ID para ser atualizada."
            );
        }

        String sql = """
            UPDATE pessoa
            SET
                nome = ?,
                sexo = ?,
                ativo = ?,
                pode_ser_responsavel = ?,
                pode_ser_ajudante = ?,
                pode_fazer_leitura = ?,
                pode_fazer_discurso = ?,
                privilegio = ?
            WHERE id = ?
            """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setString(1, pessoa.getNome());
            statement.setString(2, pessoa.getSexo().name());
            statement.setBoolean(3, pessoa.isAtivo());
            statement.setBoolean(4, pessoa.podeSerResponsavel());
            statement.setBoolean(5, pessoa.podeSerAjudante());
            statement.setBoolean(6, pessoa.podeFazerLeitura());
            statement.setBoolean(7, pessoa.podeFazerDiscurso());
            statement.setString(8, pessoa.getPrivilegio().name());
            statement.setInt(9, pessoa.getId());

            int linhasAfetadas = statement.executeUpdate();

            if (linhasAfetadas != 1) {
                throw new RuntimeException("Erro ao atualizar pessoa.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pessoa.", e);
        }
    }

    public void excluir(Integer id) {

        String sql = """
            DELETE FROM pessoa
            WHERE id = ?
            """;

        try (
                Connection connection = ConnectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            int linhasAfetadas = statement.executeUpdate();

            if (linhasAfetadas != 1) {
                throw new RuntimeException("Pessoa não encontrada para exclusão.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir pessoa.", e);
        }
    }

    private Pessoa mapearPessoa(ResultSet resultSet) throws SQLException {

        Pessoa pessoa = new Pessoa(
                resultSet.getString("nome"),
                Sexo.valueOf(resultSet.getString("sexo")),
                resultSet.getInt("ativo") == 1,
                resultSet.getInt("pode_ser_responsavel") == 1,
                resultSet.getInt("pode_ser_ajudante") == 1,
                resultSet.getInt("pode_fazer_leitura") == 1,
                resultSet.getInt("pode_fazer_discurso") == 1,
                Privilegio.valueOf(resultSet.getString("privilegio"))
        );

        pessoa.setId(resultSet.getInt("id"));

        return pessoa;
    }
}