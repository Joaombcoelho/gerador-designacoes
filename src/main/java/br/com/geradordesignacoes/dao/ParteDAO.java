package br.com.geradordesignacoes.dao;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                    nivel_leitura_minimo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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



                try(ResultSet generatedKeys =
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
                                    parte.getParticipacoesNecessarias()
                            );


                    salvarParticipacoesNecessarias(
                            connection,
                            parteSalva
                    );


                    connection.commit();


                    return parteSalva;
                }


            } catch(SQLException | RuntimeException e){

                connection.rollback();
                throw e;
            }


        } catch(SQLException e){

            throw new RuntimeException(
                    "Erro ao salvar parte.",
                    e
            );
        }
    }



    public List<Parte> listarTodos(){

        List<Parte> partes =
                new ArrayList<>();


        String sql = """
                SELECT *
                FROM parte
                ORDER BY nome
                """;


        try(
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()

        ){

            while(resultSet.next()){

                partes.add(
                        mapearParte(
                                connection,
                                resultSet
                        )
                );
            }


        }catch(SQLException e){

            throw new RuntimeException(
                    "Erro ao listar partes.",
                    e
            );
        }


        return partes;
    }




    public Optional<Parte> buscarPorId(Integer id){


        String sql = """
                SELECT *
                FROM parte
                WHERE id = ?
                """;


        try(
                Connection connection =
                        ConnectionFactory.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ){

            statement.setInt(1,id);


            try(ResultSet rs =
                        statement.executeQuery()){


                if(rs.next()){

                    return Optional.of(
                            mapearParte(
                                    connection,
                                    rs
                            )
                    );
                }
            }


        }catch(SQLException e){

            throw new RuntimeException(
                    "Erro ao buscar parte.",
                    e
            );
        }


        return Optional.empty();
    }




    public void atualizar(Parte parte){


        if(parte.getId() == null){

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
                    nivel_leitura_minimo = ?
                WHERE id = ?
                """;


        try(
                Connection connection =
                        ConnectionFactory.getConnection()

        ){

            connection.setAutoCommit(false);



            try(
                    PreparedStatement statement =
                            connection.prepareStatement(sql)

            ){


                preencherStatement(
                        statement,
                        parte
                );


                statement.setInt(
                        9,
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


            }catch(SQLException | RuntimeException e){

                connection.rollback();
                throw e;
            }


        }catch(SQLException e){

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


        return new Parte(
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

                buscarParticipacoesNecessarias(
                        connection,
                        id
                )
        );
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


        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ){

            int ordem = 0;


            for(TipoParticipacao participacao :
                    parte.getParticipacoesNecessarias()){


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



        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ){

            statement.setInt(
                    1,
                    parteId
            );


            try(ResultSet rs =
                        statement.executeQuery()){


                while(rs.next()){


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


        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)

        ){

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

            throw new RuntimeException(
                    "Erro ao excluir parte.",
                    e
            );
        }
    }
}