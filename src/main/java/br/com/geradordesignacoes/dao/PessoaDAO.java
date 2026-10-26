package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.NivelLeitura;
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
import java.time.LocalDate;


public class PessoaDAO {


    public Pessoa salvar(Pessoa pessoa) {

        String sql = """
                INSERT INTO pessoa (
                    nome,
                    sexo,
                    ativo,
                    pode_ser_responsavel,
                    pode_ser_ajudante,
                    pode_fazer_leitura,
                    pode_fazer_discurso,
                    pode_fazer_oracao,
                    pode_ser_presidente,
                    pode_ser_dirigente,
                    privilegio,
                    nivel_leitura,
                    pode_participar_sala_principal,
                    pode_participar_sala_b,
                    pode_participar_sala_c,
                    habilidades_configuradas
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                PreparedStatement.RETURN_GENERATED_KEYS
                        )
        ) {


            preencherStatement(
                    statement,
                    pessoa
            );


            int linhasAfetadas =
                    statement.executeUpdate();


            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Erro ao salvar a pessoa."
                );
            }


            try (ResultSet generatedKeys =
                         statement.getGeneratedKeys()) {


                if (generatedKeys.next()) {

                    pessoa.setId(
                            generatedKeys.getInt(1)
                    );

                } else {

                    throw new RuntimeException(
                            "Não foi possível obter o ID gerado."
                    );
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar pessoa.",
                    e
            );
        }


        return pessoa;
    }


    public List<Pessoa> listarTodos() {

        List<Pessoa> pessoas =
                new ArrayList<>();


        String sql = """
                SELECT *
                FROM pessoa
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

                pessoas.add(
                        mapearPessoa(resultSet)
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar pessoas.",
                    e
            );
        }


        return pessoas;
    }


    public List<Pessoa> listarAtivos() {
        return listarTodos().stream().filter(p -> !p.isArquivado()).toList();
    }

    public List<Pessoa> listarArquivados() {
        return listarTodos().stream().filter(Pessoa::isArquivado).toList();
    }

    public void arquivar(Integer id) {
        if (id == null) throw new IllegalArgumentException("O ID da pessoa não pode ser nulo.");
        String sql = """
                SELECT origem, data_participacao
                FROM (
                    SELECT 'Histórico de designações' AS origem, data AS data_participacao
                    FROM historico_designacoes
                    WHERE pessoa_id = ? AND date(data) >= date(?)
                    UNION ALL
                    SELECT 'Escala salva' AS origem, e.data AS data_participacao
                    FROM designacao d
                    JOIN escala e ON e.id = d.escala_id
                    WHERE (d.responsavel_id = ? OR d.ajudante_id = ?)
                      AND e.status = 'SALVA'
                      AND date(e.data) >= date(?)
                    UNION ALL
                    SELECT 'Programação semanal — conselheiro da Sala B' AS origem,
                           ps.data AS data_participacao
                    FROM programacao_semana ps
                    WHERE ps.conselheiro_sala_b_id = ? AND date(ps.data) >= date(?)
                )
                ORDER BY date(data_participacao)
                LIMIT 1
                """;
        LocalDate hoje = LocalDate.now();
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, id); ps.setString(2, hoje.toString());
                    ps.setInt(3, id); ps.setInt(4, id); ps.setString(5, hoje.toString());
                    ps.setInt(6, id); ps.setString(7, hoje.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String origem = rs.getString("origem");
                            String data = rs.getString("data_participacao");
                            throw new IllegalStateException(
                                    "Não é possível arquivar a pessoa: foi encontrada uma participação em "
                                            + data + " na fonte \"" + origem + "\". "
                                            + "Verifique esse registro antes de tentar novamente.");
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement("UPDATE pessoa SET arquivado = 1 WHERE id = ?")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Pessoa não encontrada.");
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback(); throw e;
            }
        } catch (SQLException e) { throw new RuntimeException("Erro ao arquivar pessoa.", e); }
    }

    public void restaurar(Integer id) {
        atualizarEstadoArquivamento(id, false);
    }

    private void atualizarEstadoArquivamento(Integer id, boolean arquivado) {
        if (id == null) throw new IllegalArgumentException("O ID da pessoa não pode ser nulo.");
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE pessoa SET arquivado = ? WHERE id = ?")) {
            ps.setInt(1, arquivado ? 1 : 0); ps.setInt(2, id);
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Pessoa não encontrada.");
        } catch (SQLException e) { throw new RuntimeException("Erro ao atualizar arquivamento da pessoa.", e); }
    }

    public Optional<Pessoa> buscarPorId(Integer id) {


        String sql = """
                SELECT *
                FROM pessoa
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


            try (ResultSet resultSet =
                         statement.executeQuery()) {


                if (resultSet.next()) {

                    return Optional.of(
                            mapearPessoa(resultSet)
                    );
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar pessoa por ID.",
                    e
            );
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
                    pode_fazer_oracao = ?,
                    pode_ser_presidente = ?,
                    pode_ser_dirigente = ?,
                    privilegio = ?,
                    nivel_leitura = ?,
                    pode_participar_sala_principal = ?,
                    pode_participar_sala_b = ?,
                    pode_participar_sala_c = ?,
                    habilidades_configuradas = ?
                WHERE id = ?
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {


            preencherStatement(
                    statement,
                    pessoa
            );

            statement.setBoolean(
                    13,
                    pessoa.podeParticiparSalaPrincipal()
            );

            statement.setBoolean(
                    14,
                    pessoa.podeParticiparSalaB()
            );

            statement.setBoolean(
                    15,
                    pessoa.podeParticiparSalaC()
            );

            statement.setInt(
                    17,
                    pessoa.getId()
            );


            int linhasAfetadas =
                    statement.executeUpdate();


            if (linhasAfetadas != 1) {

                throw new RuntimeException(
                        "Erro ao atualizar pessoa."
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar pessoa.",
                    e
            );
        }
    }


    public void excluir(Integer id) {


        String sql = """
                DELETE FROM pessoa
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
                        "Pessoa não encontrada para exclusão."
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao excluir pessoa.",
                    e
            );
        }
    }


    private void preencherStatement(
            PreparedStatement statement,
            Pessoa pessoa
    ) throws SQLException {


        statement.setString(
                1,
                pessoa.getNome()
        );

        statement.setString(
                2,
                pessoa.getSexo().name()
        );

        statement.setBoolean(
                3,
                pessoa.isAtivo()
        );

        statement.setBoolean(
                4,
                pessoa.podeSerResponsavel()
        );

        statement.setBoolean(
                5,
                pessoa.podeSerAjudante()
        );

        statement.setBoolean(
                6,
                pessoa.podeFazerLeitura()
        );

        statement.setBoolean(
                7,
                pessoa.podeFazerDiscurso()
        );

        statement.setBoolean(
                8,
                pessoa.podeFazerOracao()
        );

        statement.setBoolean(
                9,
                pessoa.podeSerPresidente()
        );

        statement.setBoolean(
                10,
                pessoa.podeSerDirigente()
        );

        statement.setString(
                11,
                pessoa.getPrivilegio().name()
        );

        statement.setString(
                12,
                pessoa.getNivelLeitura().name()
        );
        statement.setBoolean(
                13,
                pessoa.podeParticiparSalaPrincipal()
        );

        statement.setBoolean(
                14,
                pessoa.podeParticiparSalaB()
        );

        statement.setBoolean(
                15,
                pessoa.podeParticiparSalaC()
        );

        statement.setBoolean(
                16,
                pessoa.isHabilidadesConfiguradas()
        );
    }


    private Pessoa mapearPessoa(
            ResultSet resultSet
    ) throws SQLException {


        Pessoa pessoa =
                new Pessoa(

                        resultSet.getString("nome"),

                        Sexo.valueOf(
                                resultSet.getString("sexo")
                        ),

                        resultSet.getInt("ativo") == 1,

                        resultSet.getInt("pode_ser_responsavel") == 1,

                        resultSet.getInt("pode_ser_ajudante") == 1,

                        resultSet.getInt("pode_fazer_leitura") == 1,

                        resultSet.getInt("pode_fazer_discurso") == 1,

                        resultSet.getInt("pode_fazer_oracao") == 1,

                        resultSet.getInt("pode_ser_presidente") == 1,

                        resultSet.getInt("pode_ser_dirigente") == 1,

                        Privilegio.valueOf(
                                resultSet.getString("privilegio")
                        ),

                        NivelLeitura.valueOf(
                                resultSet.getString("nivel_leitura")
                        ),

                        resultSet.getInt("pode_participar_sala_principal") == 1,

                        resultSet.getInt("pode_participar_sala_b") == 1,

                        resultSet.getInt("pode_participar_sala_c") == 1
                );


        pessoa.setId(
                resultSet.getInt("id")
        );
        pessoa.setArquivado(resultSet.getInt("arquivado") == 1);
        pessoa.definirHabilidadesCarregadas(
                java.util.Set.copyOf(
                        new HabilidadeDesignacaoDAO().listarCodigosPorPessoa(pessoa.getId())
                ),
                resultSet.getInt("habilidades_configuradas") == 1
        );

        return pessoa;
    }
}