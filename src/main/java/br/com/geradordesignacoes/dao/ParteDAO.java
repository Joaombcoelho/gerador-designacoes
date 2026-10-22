package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

public class ParteDAO {


    public Parte salvar(Parte parte) {

        String sql = """
                INSERT INTO parte (
                    nome,
                    tipo,
                    privilegio_minimo,
                    exige_ajudante,
                    sexo_permitido,
                    quantidade_minima_participantes,
                    gera_formulario,
                    nivel_leitura_minimo,
                    secao,
                    tipo_variacao,
                    possui_tema,
                    ordem,
                    duracao_padrao_minutos
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection()
        ) {

            connection.setAutoCommit(false);


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {


                preencherStatement(
                        statement,
                        parte
                );


                int linhas =
                        statement.executeUpdate();


                if (linhas != 1) {
                    throw new RuntimeException(
                            "Erro ao salvar parte."
                    );
                }


                try (ResultSet generatedKeys =
                             statement.getGeneratedKeys()) {


                    if (!generatedKeys.next()) {

                        throw new RuntimeException(
                                "Não foi possível obter o ID gerado."
                        );
                    }


                    Parte parteSalva =
                            new Parte(
                                    generatedKeys.getInt(1),
                                    parte.getNome(),
                                    parte.getTipo(),
                                    parte.getPrivilegioMinimo(),
                                    parte.getExigeAjudante(),
                                    parte.getSexoPermitido(),
                                    parte.getQuantidadeMinimaParticipantes(),
                                    parte.geraFormulario(),
                                    parte.getNivelLeituraMinimo(),
                                    parte.getSecao(),
                                    parte.getTipoVariacao(),
                                    parte.possuiTema(),
                                    parte.getOrdem(),
                                    parte.getParticipacoesNecessarias()
                            );
                    parteSalva.setDuracaoPadraoMinutos(
                            parte.getDuracaoPadraoMinutos()
                    );


                    salvarParticipacoesNecessarias(
                            connection,
                            parteSalva
                    );


                    connection.commit();


                    return parteSalva;
                }


            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao salvar parte.",
                    e
            );
        }
    }


    public List<Parte> listarTodos() {

        List<Parte> partes =
                new ArrayList<>();


        String sql = """
                SELECT *
                FROM parte
                ORDER BY nome
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

                partes.add(
                        mapearParte(
                                connection,
                                resultSet
                        )
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao listar partes.",
                    e
            );
        }


        return partes;
    }


    public List<Parte> listarAtivos() {
        return listarTodos().stream().filter(p -> !p.isArquivado()).toList();
    }

    public List<Parte> listarArquivados() {
        return listarTodos().stream().filter(Parte::isArquivado).toList();
    }

    public void arquivar(Integer id) {
        if (id == null) throw new IllegalArgumentException("O ID da parte não pode ser nulo.");
        String sql = """
                SELECT origem, data_participacao
                FROM (
                    SELECT 'Histórico de designações' AS origem, data AS data_participacao
                    FROM historico_designacoes
                    WHERE parte_id = ? AND date(data) >= date(?)
                    UNION ALL
                    SELECT 'Escala salva' AS origem, e.data AS data_participacao
                    FROM designacao d
                    JOIN escala e ON e.id = d.escala_id
                    WHERE d.parte_id = ? AND date(e.data) >= date(?)
                    UNION ALL
                    SELECT 'Programação semanal' AS origem, ps.data AS data_participacao
                    FROM programacao_parte pp
                    JOIN programacao_semana ps ON ps.id = pp.programacao_semana_id
                    WHERE pp.parte_id = ? AND date(ps.data) >= date(?)
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
                    ps.setInt(3, id); ps.setString(4, hoje.toString());
                    ps.setInt(5, id); ps.setString(6, hoje.toString());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String origem = rs.getString("origem");
                            String data = rs.getString("data_participacao");
                            throw new IllegalStateException(
                                    "Não é possível arquivar a parte: foi encontrada uma participação em "
                                            + data + " na fonte "" + origem + "". "
                                            + "Verifique esse registro antes de tentar novamente.");
                        }
                    }
                }
                try (PreparedStatement ps = connection.prepareStatement("UPDATE parte SET arquivado = 1 WHERE id = ?")) {
                    ps.setInt(1, id);
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Parte não encontrada.");
                }
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback(); throw e;
            }
        } catch (SQLException e) { throw new RuntimeException("Erro ao arquivar parte.", e); }
    }

    public void restaurar(Integer id) {
        atualizarEstadoArquivamento(id, false);
    }

    private void atualizarEstadoArquivamento(Integer id, boolean arquivado) {
        if (id == null) throw new IllegalArgumentException("O ID da parte não pode ser nulo.");
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE parte SET arquivado = ? WHERE id = ?")) {
            ps.setInt(1, arquivado ? 1 : 0); ps.setInt(2, id);
            if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Parte não encontrada.");
        } catch (SQLException e) { throw new RuntimeException("Erro ao atualizar arquivamento da parte.", e); }
    }

    public Optional<Parte> buscarPorId(Integer id) {


        String sql = """
                SELECT *
                FROM parte
                WHERE id = ?
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ) {

            statement.setInt(1, id);


            try (ResultSet rs =
                         statement.executeQuery()) {


                if (rs.next()) {

                    return Optional.of(
                            mapearParte(
                                    connection,
                                    rs
                            )
                    );
                }
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao buscar parte.",
                    e
            );
        }


        return Optional.empty();
    }


    public void atualizar(Parte parte) {


        if (parte.getId() == null) {

            throw new IllegalArgumentException(
                    "Parte sem ID."
            );
        }


        String sql = """
                UPDATE parte
                SET
                    nome = ?,
                    tipo = ?,
                    privilegio_minimo = ?,
                    exige_ajudante = ?,
                    sexo_permitido = ?,
                    quantidade_minima_participantes = ?,
                    gera_formulario = ?,
                    nivel_leitura_minimo = ?,
                    secao = ?,
                    tipo_variacao = ?,
                    possui_tema = ?,
                    ordem = ?,
                    duracao_padrao_minutos = ?
                WHERE id = ?
                """;


        try (
                Connection connection =
                        ConnectionFactory.getConnection()

        ) {

            connection.setAutoCommit(false);


            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)

            ) {


                preencherStatement(
                        statement,
                        parte
                );


                statement.setInt(
                        14,
                        parte.getId()
                );


                statement.executeUpdate();


                excluirParticipacoesNecessarias(
                        connection,
                        parte.getId()
                );


                salvarParticipacoesNecessarias(
                        connection,
                        parte
                );


                connection.commit();


            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao atualizar parte.",
                    e
            );
        }
    }


    private void preencherStatement(
            PreparedStatement statement,
            Parte parte
    ) throws SQLException {


        statement.setString(
                1,
                parte.getNome()
        );


        statement.setString(
                2,
                parte.getTipo().name()
        );


        statement.setString(
                3,
                parte.getPrivilegioMinimo().name()
        );


        statement.setBoolean(
                4,
                parte.getExigeAjudante()
        );


        statement.setString(
                5,
                parte.getSexoPermitido().name()
        );


        statement.setInt(
                6,
                parte.getQuantidadeMinimaParticipantes()
        );


        statement.setBoolean(
                7,
                parte.geraFormulario()
        );


        statement.setString(
                8,
                parte.getNivelLeituraMinimo().name()
        );

        statement.setString(
                9,
                parte.getSecao() == null
                        ? null
                        : parte.getSecao().name()
        );

        statement.setString(
                10,
                parte.getTipoVariacao() == null
                        ? null
                        : parte.getTipoVariacao().name()
        );

        statement.setBoolean(
                11,
                parte.possuiTema()
        );

        statement.setInt(
                12,
                parte.getOrdem()
        );

        statement.setObject(
                13,
                parte.getDuracaoPadraoMinutos()
        );
    }


    private Parte mapearParte(
            Connection connection,
            ResultSet resultSet
    ) throws SQLException {

        Integer id =
                resultSet.getInt("id");


        String nivel =
                resultSet.getString(
                        "nivel_leitura_minimo"
                );


        if (nivel == null) {
            nivel =
                    NivelLeitura.BASICO.name();
        }


        String secao =
                resultSet.getString("secao");


        String tipoVariacao =
                resultSet.getString("tipo_variacao");


        Parte parte = new Parte(
                id,

                resultSet.getString("nome"),

                TipoParte.valueOf(
                        resultSet.getString("tipo")
                ),

                Privilegio.valueOf(
                        resultSet.getString("privilegio_minimo")
                ),

                resultSet.getInt(
                        "exige_ajudante"
                ) == 1,

                SexoPermitido.valueOf(
                        resultSet.getString("sexo_permitido")
                ),

                resultSet.getInt(
                        "quantidade_minima_participantes"
                ),

                resultSet.getInt(
                        "gera_formulario"
                ) == 1,

                NivelLeitura.valueOf(
                        nivel
                ),

                secao == null
                        ? null
                        : SecaoParte.valueOf(secao),

                tipoVariacao == null
                        ? null
                        : TipoVariacaoParte.valueOf(
                        tipoVariacao
                ),

                resultSet.getInt(
                        "possui_tema"
                ) == 1,

                resultSet.getInt(
                        "ordem"
                ),

                buscarParticipacoesNecessarias(
                        connection,
                        id
                )
        );

        parte.setDuracaoPadraoMinutos(
                (Integer) resultSet.getObject("duracao_padrao_minutos")
        );
        parte.setArquivado(resultSet.getInt("arquivado") == 1);
        return parte;
    }

    private void salvarParticipacoesNecessarias(
            Connection connection,
            Parte parte
    ) throws SQLException {


        String sql = """
                INSERT INTO parte_participacao_necessaria
                (
                    parte_id,
                    tipo_participacao,
                    ordem
                )
                VALUES (?, ?, ?)
                """;


        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ) {

            int ordem = 0;


            for (TipoParticipacao participacao :
                    parte.getParticipacoesNecessarias()) {


                statement.setInt(
                        1,
                        parte.getId()
                );


                statement.setString(
                        2,
                        participacao.name()
                );


                statement.setInt(
                        3,
                        ordem++
                );


                statement.addBatch();

            }


            statement.executeBatch();

        }
    }


    private List<TipoParticipacao> buscarParticipacoesNecessarias(
            Connection connection,
            Integer parteId
    ) throws SQLException {


        List<TipoParticipacao> lista =
                new ArrayList<>();


        String sql = """
                SELECT tipo_participacao
                FROM parte_participacao_necessaria
                WHERE parte_id = ?
                ORDER BY ordem
                """;


        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ) {

            statement.setInt(
                    1,
                    parteId
            );


            try (ResultSet rs =
                         statement.executeQuery()) {


                while (rs.next()) {


                    lista.add(
                            TipoParticipacao.valueOf(
                                    rs.getString(
                                            "tipo_participacao"
                                    )
                            )
                    );

                }

            }

        }


        return lista;
    }


    private void excluirParticipacoesNecessarias(
            Connection connection,
            Integer parteId
    ) throws SQLException {


        String sql = """
                DELETE FROM parte_participacao_necessaria
                WHERE parte_id = ?
                """;


        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ) {

            statement.setInt(
                    1,
                    parteId
            );

            statement.executeUpdate();
        }
    }

    public void excluir(Integer id) {

        String sql = """
                DELETE FROM parte
                WHERE id = ?
                """;

        try (Connection connection = ConnectionFactory.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {


                excluirParticipacoesNecessarias(
                        connection,
                        id
                );


                statement.setInt(1, id);


                int linhasAfetadas =
                        statement.executeUpdate();


                if (linhasAfetadas != 1) {

                    throw new RuntimeException(
                            "Parte não encontrada para exclusão."
                    );
                }


                connection.commit();


            } catch (SQLException | RuntimeException e) {

                connection.rollback();
                throw e;
            }


        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase(java.util.Locale.ROOT)
                            .contains("foreign key constraint failed")) {
                throw new RuntimeException(
                        "Não é possível excluir esta parte porque ela está vinculada "
                                + "a designações, ao histórico ou a programações semanais.",
                        e
                );
            }
            throw new RuntimeException("Erro ao excluir parte.", e);
        }
    }
}