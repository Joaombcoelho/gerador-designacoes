package br.com.geradordesignacoes.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String CREATE_TABLE_PESSOA = """

        CREATE TABLE IF NOT EXISTS pessoa (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            sexo TEXT NOT NULL,
            ativo INTEGER NOT NULL,
            pode_ser_responsavel INTEGER NOT NULL,
            pode_ser_ajudante INTEGER NOT NULL,
            pode_fazer_leitura INTEGER NOT NULL,
            pode_fazer_discurso INTEGER NOT NULL,
            pode_fazer_oracao INTEGER NOT NULL DEFAULT 0,
            privilegio TEXT NOT NULL,
            nivel_leitura TEXT NOT NULL DEFAULT 'BASICO'
        );

        """;


    private static final String CREATE_TABLE_PARTE = """

        CREATE TABLE IF NOT EXISTS parte (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            tipo TEXT NOT NULL,
            privilegio_minimo TEXT NOT NULL,
            exige_ajudante INTEGER NOT NULL,
            sexo_permitido TEXT NOT NULL,
            quantidade_minima_participantes INTEGER NOT NULL,
            gera_formulario INTEGER NOT NULL,
            nivel_leitura_minimo TEXT NOT NULL DEFAULT 'BASICO'
        );

        """;


    private static final String CREATE_TABLE_PARTE_PARTICIPACAO_NECESSARIA = """

            CREATE TABLE IF NOT EXISTS parte_participacao_necessaria (
                parte_id INTEGER NOT NULL,
                tipo_participacao TEXT NOT NULL,
                ordem INTEGER NOT NULL,

                PRIMARY KEY (parte_id, tipo_participacao),
                FOREIGN KEY (parte_id) REFERENCES parte(id) ON DELETE CASCADE
            );

            """;


    private static final String BACKFILL_PARTICIPACOES_LEITURA = """

            INSERT OR IGNORE INTO parte_participacao_necessaria (
                parte_id,
                tipo_participacao,
                ordem
            )
            SELECT id, 'LEITOR', 0
            FROM parte
            WHERE tipo = 'LEITURA';

            """;


    private static final String BACKFILL_PARTICIPACOES_DISCURSO = """

            INSERT OR IGNORE INTO parte_participacao_necessaria (
                parte_id,
                tipo_participacao,
                ordem
            )
            SELECT id, 'ORADOR', 0
            FROM parte
            WHERE tipo = 'DISCURSO';

            """;


    private static final String BACKFILL_PARTICIPACOES_DEMONSTRACAO_RESPONSAVEL = """

            INSERT OR IGNORE INTO parte_participacao_necessaria (
                parte_id,
                tipo_participacao,
                ordem
            )
            SELECT id, 'RESPONSAVEL', 0
            FROM parte
            WHERE tipo = 'DEMONSTRACAO';

            """;


    private static final String BACKFILL_PARTICIPACOES_DEMONSTRACAO_AJUDANTE = """

            INSERT OR IGNORE INTO parte_participacao_necessaria (
                parte_id,
                tipo_participacao,
                ordem
            )
            SELECT id, 'AJUDANTE', 1
            FROM parte
            WHERE tipo = 'DEMONSTRACAO';

            """;


    private static final String CREATE_TABLE_HISTORICO_DESIGNACOES = """

            CREATE TABLE IF NOT EXISTS historico_designacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data DATE NOT NULL,
                pessoa_id INTEGER NOT NULL,
                parte_id INTEGER NOT NULL,
                tipo_participacao TEXT NOT NULL,

                UNIQUE(
                    data,
                    pessoa_id,
                    parte_id,
                    tipo_participacao
                ),

                FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                FOREIGN KEY (parte_id) REFERENCES parte(id)
            );

            """;


    private static final String CREATE_TABLE_ESCALA = """
            
            CREATE TABLE IF NOT EXISTS escala (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data DATE NOT NULL,
                status TEXT NOT NULL,
                data_geracao TEXT NOT NULL,
                data_salvamento TEXT
            );

            """;


    private static final String CREATE_TABLE_DESIGNACAO = """

            CREATE TABLE IF NOT EXISTS designacao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                escala_id INTEGER NOT NULL,
                parte_id INTEGER NOT NULL,
                responsavel_id INTEGER NOT NULL,
                ajudante_id INTEGER,

                FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE,
                FOREIGN KEY (parte_id) REFERENCES parte(id),
                FOREIGN KEY (responsavel_id) REFERENCES pessoa(id),
                FOREIGN KEY (ajudante_id) REFERENCES pessoa(id)
            );

            """;


    private static void adicionarColunaNivelLeitura(
            Connection connection
    ) throws SQLException {

        String sql = "PRAGMA table_info(pessoa);";

        boolean existe = false;

        try (
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {

                if ("nivel_leitura".equals(
                        resultSet.getString("name")
                )) {

                    existe = true;
                    break;
                }
            }
        }


        if (!existe) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                    ALTER TABLE pessoa
                    ADD COLUMN nivel_leitura
                    TEXT NOT NULL DEFAULT 'BASICO';
                    """);

                System.out.println(
                        "Coluna 'nivel_leitura' adicionada."
                );
            }
        }
    }


    private static void adicionarColunaNivelLeituraParte(
            Connection connection
    ) throws SQLException {

        String sql = "PRAGMA table_info(parte);";

        boolean existe = false;


        try (
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {

                if ("nivel_leitura_minimo".equals(
                        resultSet.getString("name")
                )) {

                    existe = true;
                    break;
                }
            }
        }


        if (!existe) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                    ALTER TABLE parte
                    ADD COLUMN nivel_leitura_minimo
                    TEXT NOT NULL DEFAULT 'BASICO';
                    """);


                System.out.println(
                        "Coluna 'nivel_leitura_minimo' adicionada na tabela parte."
                );
            }
        }
    }

    private static boolean colunaExiste(
            Connection connection,
            String nomeColuna
    ) throws SQLException {

        String sql = "PRAGMA table_info(pessoa);";

        try (
                Statement statement = connection.createStatement();
                var resultSet = statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {

                if (nomeColuna.equals(
                        resultSet.getString("name")
                )) {

                    return true;
                }
            }
        }

        return false;
    }



    private static void adicionarColunaPodeFazerOracao(
            Connection connection
    ) throws SQLException {

        if (!colunaExiste(
                connection,
                "pode_fazer_oracao"
        )) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                    ALTER TABLE pessoa
                    ADD COLUMN pode_fazer_oracao
                    INTEGER NOT NULL DEFAULT 0;
                    """);

                System.out.println(
                        "Coluna 'pode_fazer_oracao' adicionada."
                );
            }
        }
    }



    private static void adicionarColunaPresidenteDirigente(
            Connection connection
    ) throws SQLException {


        if (!colunaExiste(
                connection,
                "pode_ser_presidente"
        )) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                    ALTER TABLE pessoa
                    ADD COLUMN pode_ser_presidente
                    INTEGER NOT NULL DEFAULT 0;
                    """);

                System.out.println(
                        "Coluna 'pode_ser_presidente' adicionada."
                );
            }
        }


        if (!colunaExiste(
                connection,
                "pode_ser_dirigente"
        )) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                    ALTER TABLE pessoa
                    ADD COLUMN pode_ser_dirigente
                    INTEGER NOT NULL DEFAULT 0;
                    """);

                System.out.println(
                        "Coluna 'pode_ser_dirigente' adicionada."
                );
            }
        }
    }

    private static final String CRIAR_PARTE_PRESIDENTE = """

        INSERT OR IGNORE INTO parte (
            nome,
            tipo,
            privilegio_minimo,
            exige_ajudante,
            sexo_permitido,
            quantidade_minima_participantes,
            gera_formulario,
            nivel_leitura_minimo
        )
        VALUES (
            'Presidente da reunião',
            'PRESIDENTE_REUNIAO',
            'ANCIAO',
            0,
            'MASCULINO',
            1,
            0,
            'BASICO'
        );

        """;


    private static final String CRIAR_PARTE_ORACAO = """

        INSERT OR IGNORE INTO parte (
            nome,
            tipo,
            privilegio_minimo,
            exige_ajudante,
            sexo_permitido,
            quantidade_minima_participantes,
            gera_formulario,
            nivel_leitura_minimo
        )
        VALUES (
            'Oração',
            'ORACAO',
            'BATIZADO',
            0,
            'MASCULINO',
            1,
            0,
            'BASICO'
        );

        """;


    private static final String CRIAR_PARTE_DIRIGENTE_ESTUDO = """

        INSERT OR IGNORE INTO parte (
            nome,
            tipo,
            privilegio_minimo,
            exige_ajudante,
            sexo_permitido,
            quantidade_minima_participantes,
            gera_formulario,
            nivel_leitura_minimo
        )
        VALUES (
            'Dirigente do estudo bíblico',
            'DIRIGENTE_ESTUDO',
            'ANCIAO',
            0,
            'MASCULINO',
            2,
            0,
            'BASICO'
        );

        """;

    private static final String BACKFILL_PARTICIPACAO_PRESIDENTE = """

        INSERT OR IGNORE INTO parte_participacao_necessaria (
            parte_id,
            tipo_participacao,
            ordem
        )
        SELECT id, 'PRESIDENTE', 0
        FROM parte
        WHERE tipo = 'PRESIDENTE_REUNIAO';

        """;


    private static final String BACKFILL_PARTICIPACAO_ORACAO = """

        INSERT OR IGNORE INTO parte_participacao_necessaria (
            parte_id,
            tipo_participacao,
            ordem
        )
        SELECT id, 'ORACAO', 0
        FROM parte
        WHERE tipo = 'ORACAO';

        """;


    private static final String BACKFILL_PARTICIPACOES_DIRIGENTE_ESTUDO = """

        INSERT OR IGNORE INTO parte_participacao_necessaria (
            parte_id,
            tipo_participacao,
            ordem
        )
        SELECT id, 'DIRIGENTE', 0
        FROM parte
        WHERE tipo = 'DIRIGENTE_ESTUDO';


        INSERT OR IGNORE INTO parte_participacao_necessaria (
            parte_id,
            tipo_participacao,
            ordem
        )
        SELECT id, 'LEITOR', 1
        FROM parte
        WHERE tipo = 'DIRIGENTE_ESTUDO';

        """;
    public static void initialize() {

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                Statement statement =
                        connection.createStatement()
        ) {

            // Ativa integridade das chaves estrangeiras no SQLite
            statement.execute(
                    "PRAGMA foreign_keys = ON;"
            );


            statement.execute(CREATE_TABLE_PESSOA);

            System.out.println(
                    "Tabela 'pessoa' criada ou já existente."
            );


            adicionarColunaNivelLeitura(connection);
            adicionarColunaPodeFazerOracao(connection);
            adicionarColunaPresidenteDirigente(connection);


            statement.execute(CREATE_TABLE_PARTE);

            System.out.println(
                    "Tabela 'parte' criada ou já existente."
            );


            adicionarColunaNivelLeituraParte(connection);


            statement.execute(
                    CREATE_TABLE_PARTE_PARTICIPACAO_NECESSARIA
            );

            System.out.println(
                    "Tabela 'parte_participacao_necessaria' criada ou já existente."
            );

            statement.execute(
                    CRIAR_PARTE_PRESIDENTE
            );

            statement.execute(
                    CRIAR_PARTE_ORACAO
            );

            statement.execute(
                    CRIAR_PARTE_DIRIGENTE_ESTUDO
            );


            statement.execute(
                    BACKFILL_PARTICIPACAO_PRESIDENTE
            );

            statement.execute(
                    BACKFILL_PARTICIPACAO_ORACAO
            );

            statement.execute(
                    BACKFILL_PARTICIPACOES_DIRIGENTE_ESTUDO
            );


            statement.execute(
                    BACKFILL_PARTICIPACOES_LEITURA
            );

            statement.execute(
                    BACKFILL_PARTICIPACOES_DISCURSO
            );

            statement.execute(
                    BACKFILL_PARTICIPACOES_DEMONSTRACAO_RESPONSAVEL
            );

            statement.execute(
                    BACKFILL_PARTICIPACOES_DEMONSTRACAO_AJUDANTE
            );


            statement.execute(
                    CREATE_TABLE_HISTORICO_DESIGNACOES
            );

            System.out.println(
                    "Tabela 'historico_designacoes' criada ou já existente."
            );


            statement.execute(
                    CREATE_TABLE_ESCALA
            );

            System.out.println(
                    "Tabela 'escala' criada ou já existente."
            );


            statement.execute(
                    CREATE_TABLE_DESIGNACAO
            );

            System.out.println(
                    "Tabela 'designacao' criada ou já existente."
            );


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao inicializar o banco de dados.",
                    e
            );
        }
    }


}