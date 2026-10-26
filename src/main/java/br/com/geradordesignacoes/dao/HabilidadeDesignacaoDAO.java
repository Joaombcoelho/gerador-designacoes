package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.HabilidadeDesignacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Persistência do catálogo e dos vínculos de habilidades individuais.
 * Não infere habilidades com base em sexo ou privilégio.
 */
public class HabilidadeDesignacaoDAO {

    public List<HabilidadeDesignacao> listarAtivas() {
        return listar("""
                SELECT id, codigo, nome, ativa
                FROM habilidade_designacao
                WHERE ativa = 1
                ORDER BY nome COLLATE NOCASE, id
                """);
    }

    public List<HabilidadeDesignacao> listarPorPessoa(Integer pessoaId) {
        validarId(pessoaId, "pessoa");
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT h.id, h.codigo, h.nome, h.ativa
                     FROM habilidade_designacao h
                     JOIN pessoa_habilidade ph ON ph.habilidade_id = h.id
                     WHERE ph.pessoa_id = ?
                     ORDER BY h.nome COLLATE NOCASE, h.id
                     """)) {
            statement.setInt(1, pessoaId);
            try (ResultSet rs = statement.executeQuery()) {
                List<HabilidadeDesignacao> resultado = new ArrayList<>();
                while (rs.next()) {
                    resultado.add(mapear(rs));
                }
                return List.copyOf(resultado);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar habilidades da pessoa.", e);
        }
    }

    /**
     * Substitui o conjunto de habilidades de uma pessoa atomicamente.
     * Um ID inexistente faz a transação falhar e restaura os vínculos anteriores.
     */
    public void substituirHabilidades(Integer pessoaId, Collection<Integer> habilidadeIds) {
        validarId(pessoaId, "pessoa");
        Objects.requireNonNull(habilidadeIds, "habilidades não pode ser nulo");
        List<Integer> ids = List.copyOf(habilidadeIds);
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Todos os IDs de habilidade devem ser positivos.");
        }
        if (ids.stream().distinct().count() != ids.size()) {
            throw new IllegalArgumentException("A lista contém habilidades duplicadas.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            boolean autoCommitAnterior = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement pessoa = connection.prepareStatement(
                        "SELECT 1 FROM pessoa WHERE id = ?")) {
                    pessoa.setInt(1, pessoaId);
                    try (ResultSet rs = pessoa.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Pessoa não encontrada: " + pessoaId);
                        }
                    }
                }

                try (PreparedStatement apagar = connection.prepareStatement(
                        "DELETE FROM pessoa_habilidade WHERE pessoa_id = ?")) {
                    apagar.setInt(1, pessoaId);
                    apagar.executeUpdate();
                }

                try (PreparedStatement inserir = connection.prepareStatement("""
                        INSERT INTO pessoa_habilidade (pessoa_id, habilidade_id)
                        VALUES (?, ?)
                        """)) {
                    for (Integer habilidadeId : ids) {
                        inserir.setInt(1, pessoaId);
                        inserir.setInt(2, habilidadeId);
                        inserir.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommitAnterior);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar habilidades da pessoa.", e);
        }
    }

    public HabilidadeDesignacao salvar(HabilidadeDesignacao habilidade) {
        Objects.requireNonNull(habilidade, "habilidade não pode ser nula");
        if (habilidade.id() != null) {
            throw new IllegalArgumentException("Para cadastrar uma habilidade, o ID deve ser nulo.");
        }
        String sql = """
                INSERT INTO habilidade_designacao (codigo, nome, ativa)
                VALUES (?, ?, ?)
                """;
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, habilidade.codigo());
            statement.setString(2, habilidade.nome());
            statement.setBoolean(3, habilidade.ativa());
            if (statement.executeUpdate() != 1) {
                throw new RuntimeException("Não foi possível salvar a habilidade.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new RuntimeException("Não foi possível obter o ID da habilidade.");
                }
                return new HabilidadeDesignacao(
                        keys.getInt(1), habilidade.codigo(), habilidade.nome(), habilidade.ativa());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar habilidade de designação.", e);
        }
    }


    public List<String> listarCodigosPorPessoa(Integer pessoaId) {
        validarId(pessoaId, "pessoa");
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT h.codigo
                     FROM habilidade_designacao h
                     JOIN pessoa_habilidade ph ON ph.habilidade_id = h.id
                     WHERE ph.pessoa_id = ? AND h.ativa = 1
                     ORDER BY h.codigo
                     """)) {
            statement.setInt(1, pessoaId);
            try (ResultSet rs = statement.executeQuery()) {
                List<String> codigos = new ArrayList<>();
                while (rs.next()) {
                    codigos.add(rs.getString("codigo"));
                }
                return List.copyOf(codigos);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar códigos de habilidades da pessoa.", e);
        }
    }

    public void substituirCodigos(Integer pessoaId, Collection<String> codigos) {
        Objects.requireNonNull(codigos, "códigos não pode ser nulo");
        List<String> codigosUnicos = List.copyOf(codigos);
        if (codigosUnicos.stream().anyMatch(codigo -> codigo == null || codigo.isBlank())) {
            throw new IllegalArgumentException("Código de habilidade inválido.");
        }
        if (codigosUnicos.stream().distinct().count() != codigosUnicos.size()) {
            throw new IllegalArgumentException("A lista contém códigos de habilidade duplicados.");
        }

        List<Integer> ids = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT id FROM habilidade_designacao
                     WHERE codigo = ? AND ativa = 1
                     """)) {
            for (String codigo : codigosUnicos) {
                statement.setString(1, codigo);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Habilidade inexistente ou inativa: " + codigo);
                    }
                    ids.add(rs.getInt("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao validar habilidades da pessoa.", e);
        }
        substituirHabilidades(pessoaId, ids);
    }

    private List<HabilidadeDesignacao> listar(String sql) {
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<HabilidadeDesignacao> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(mapear(rs));
            }
            return List.copyOf(resultado);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar habilidades de designação.", e);
        }
    }

    private HabilidadeDesignacao mapear(ResultSet rs) throws SQLException {
        return new HabilidadeDesignacao(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nome"),
                rs.getBoolean("ativa"));
    }

    private void validarId(Integer id, String entidade) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID de " + entidade + " deve ser positivo.");
        }
    }
}
