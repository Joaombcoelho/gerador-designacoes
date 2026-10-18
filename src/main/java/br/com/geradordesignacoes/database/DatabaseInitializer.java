package br.com.geradordesignacoes.database;

import br.com.geradordesignacoes.model.*;

import java.sql.*;
import java.util.List;

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
                pode_ser_presidente INTEGER NOT NULL DEFAULT 0,
                pode_ser_dirigente INTEGER NOT NULL DEFAULT 0,
                privilegio TEXT NOT NULL,
                nivel_leitura TEXT NOT NULL DEFAULT 'BASICO',
                pode_participar_sala_principal INTEGER NOT NULL DEFAULT 1,
                pode_participar_sala_b INTEGER NOT NULL DEFAULT 1,
                pode_participar_sala_c INTEGER NOT NULL DEFAULT 1
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
                nivel_leitura_minimo TEXT NOT NULL DEFAULT 'BASICO',
                secao TEXT,
                tipo_variacao TEXT,
                possui_tema INTEGER NOT NULL DEFAULT 0,
                ordem INTEGER NOT NULL DEFAULT 999,
                duracao_padrao_minutos INTEGER
            );
            
            """;


    private static final String CREATE_TABLE_PARTE_PARTICIPACAO_NECESSARIA = """
            
            CREATE TABLE IF NOT EXISTS parte_participacao_necessaria (
                parte_id INTEGER NOT NULL,
                tipo_participacao TEXT NOT NULL,
                ordem INTEGER NOT NULL,
            
                PRIMARY KEY (parte_id, tipo_participacao),
            
                FOREIGN KEY (parte_id)
                    REFERENCES parte(id)
                    ON DELETE CASCADE
            );
            
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
            
                FOREIGN KEY (pessoa_id)
                    REFERENCES pessoa(id),
            
                FOREIGN KEY (parte_id)
                    REFERENCES parte(id)
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
                responsavel_id INTEGER,
                ajudante_id INTEGER,
                sala TEXT NOT NULL DEFAULT 'PRINCIPAL'
                    CHECK (sala IN ('PRINCIPAL', 'B', 'C')),
                
            
                FOREIGN KEY (escala_id)
                    REFERENCES escala(id)
                    ON DELETE CASCADE,
            
                FOREIGN KEY (parte_id)
                    REFERENCES parte(id),
            
                FOREIGN KEY (responsavel_id)
                    REFERENCES pessoa(id),
            
                FOREIGN KEY (ajudante_id)
                    REFERENCES pessoa(id)
            );
            
            """;


    /*
     * ============================================================
     * PROGRAMAÇÃO SEMANAL
     * ============================================================
     */

    private static final String CREATE_TABLE_PROGRAMACAO_SEMANA = """
            
            CREATE TABLE IF NOT EXISTS programacao_semana (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data DATE NOT NULL UNIQUE,
                sala_b_habilitada INTEGER NOT NULL DEFAULT 0,
                sala_c_habilitada INTEGER NOT NULL DEFAULT 0,
                conselheiro_sala_b_id INTEGER REFERENCES pessoa(id) ON DELETE SET NULL,
                cantico_inicial TEXT,
                cantico_meio TEXT,
                cantico_final TEXT
            );
            
            """;

    private static final String CREATE_TABLE_CONFIGURACAO_SALAS = """
            
            CREATE TABLE IF NOT EXISTS configuracao_salas (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                sala_b_habilitada INTEGER NOT NULL DEFAULT 0,
                sala_c_habilitada INTEGER NOT NULL DEFAULT 0
            );
            
            """;

    private static final String CREATE_TABLE_CONFIGURACAO_GERAL = """
            
            CREATE TABLE IF NOT EXISTS configuracoes_gerais (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                nome_congregacao TEXT NOT NULL DEFAULT '',
                dia_reuniao_habitual TEXT NOT NULL DEFAULT 'WEDNESDAY',
                horario_inicio TEXT NOT NULL DEFAULT '19:30',
                duracao_cantico_oracao_inicial_minutos INTEGER NOT NULL DEFAULT 5 CHECK (duracao_cantico_oracao_inicial_minutos > 0),
                duracao_comentarios_iniciais_minutos INTEGER NOT NULL DEFAULT 1 CHECK (duracao_comentarios_iniciais_minutos > 0),
                duracao_cantico_meio_minutos INTEGER NOT NULL DEFAULT 4 CHECK (duracao_cantico_meio_minutos > 0),
                duracao_comentarios_finais_minutos INTEGER NOT NULL DEFAULT 3 CHECK (duracao_comentarios_finais_minutos > 0),
                duracao_cantico_oracao_final_minutos INTEGER NOT NULL DEFAULT 5 CHECK (duracao_cantico_oracao_final_minutos > 0)
            );
            
            """;


    private static final String CREATE_TABLE_PROGRAMACAO_PARTE = """
            
            CREATE TABLE IF NOT EXISTS programacao_parte (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
            
                programacao_semana_id INTEGER NOT NULL,
                parte_id INTEGER NOT NULL,
            
                ordem INTEGER NOT NULL,
                tema TEXT,
                duracao_minutos INTEGER,
            
                FOREIGN KEY (programacao_semana_id)
                    REFERENCES programacao_semana(id)
                    ON DELETE CASCADE,
            
                FOREIGN KEY (parte_id)
                    REFERENCES parte(id),
            
                UNIQUE (
                    programacao_semana_id,
                    parte_id
                ),
            
                UNIQUE (
                    programacao_semana_id,
                    ordem
                )
            );
            
            """;


    /*
     * ============================================================
     * INICIALIZAÇÃO
     * ============================================================
     */

    public static void initialize() {

        try (
                Connection connection =
                        ConnectionFactory.getConnection();

                Statement statement =
                        connection.createStatement()
        ) {

            statement.execute(
                    "PRAGMA foreign_keys = ON;"
            );


            /*
             * ----------------------------------------------------
             * PESSOA
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PESSOA
            );
            adicionarColunasPermissaoSalasPessoa(connection);

            /*
             * ----------------------------------------------------
             * PARTE
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PARTE
            );
            adicionarColunaOrdemParte(connection);
            adicionarColunaDuracaoPadraoParte(connection);

            migrarTipoVariacaoPartes(connection);
            /*
             * ----------------------------------------------------
             * PARTICIPAÇÕES NECESSÁRIAS
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PARTE_PARTICIPACAO_NECESSARIA
            );
            removerAjudanteConfiguradoNoEstudoBiblico(connection);


            /*
             * ----------------------------------------------------
             * HISTÓRICO
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_HISTORICO_DESIGNACOES
            );

            migrarHistoricoDesignacoesSala(connection);


            /*
             * ----------------------------------------------------
             * ESCALA
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_ESCALA
            );

            adicionarColunasPapeisEspeciaisEscala(connection);


            /*
             * ----------------------------------------------------
             * DESIGNAÇÃO
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_DESIGNACAO
            );
            migrarResponsavelDesignacaoNullable(connection);
            adicionarColunaSalaDesignacao(connection);

            /*
             * ----------------------------------------------------
             * PROGRAMAÇÃO SEMANAL
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PROGRAMACAO_SEMANA
            );

            statement.execute("""
                    CREATE TABLE IF NOT EXISTS horario_manual_s140 (
                        data DATE NOT NULL,
                        chave_item TEXT NOT NULL,
                        horario_inicio TEXT NOT NULL,
                        PRIMARY KEY (data, chave_item),
                        FOREIGN KEY (data)
                            REFERENCES programacao_semana(data)
                            ON DELETE CASCADE
                    )
                    """);



            adicionarColunasSalasProgramacaoSemana(connection);
            adicionarColunasDadosS140ProgramacaoSemana(connection);
            statement.execute(CREATE_TABLE_CONFIGURACAO_SALAS);
            statement.execute("""
                    INSERT OR IGNORE INTO configuracao_salas
                        (id, sala_b_habilitada, sala_c_habilitada)
                    VALUES (1, 0, 0)
                    """);

            statement.execute(CREATE_TABLE_CONFIGURACAO_GERAL);
            statement.execute("""
                    INSERT OR IGNORE INTO configuracoes_gerais (id)
                    VALUES (1)
                    """);

            statement.execute(
                    CREATE_TABLE_PROGRAMACAO_PARTE
            );

            /*
             * ----------------------------------------------------
             * EVENTOS ESPECIAIS
             * ----------------------------------------------------
             *
             * Tabela independente: cadastrar/editar/remover eventos
             * não modifica programação, escalas, histórico ou S-140.
             */
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS evento_especial (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tipo TEXT NOT NULL CHECK (
                            tipo IN (
                                'VISITA_SUPERINTENDENTE',
                                'ASSEMBLEIA',
                                'CONGRESSO',
                                'CELEBRACAO',
                                'CANCELAMENTO'
                            )
                        ),
                        data_inicio TEXT NOT NULL,
                        data_fim TEXT NOT NULL,
                        tema_discurso TEXT,
                        descricao TEXT,
                        CHECK (data_fim >= data_inicio),
                        CHECK (
                            tema_discurso IS NULL
                            OR tipo = 'VISITA_SUPERINTENDENTE'
                        )
                    )
                    """);

            adicionarColunaNumeroOficialProgramacaoParte(connection);
            adicionarColunaDuracaoProgramacaoParte(connection);


            /*
             * ----------------------------------------------------
             * DADOS INICIAIS
             * ----------------------------------------------------
             */

            cadastrarPartesIniciais(
                    connection
            );

            atualizarOrdensPadraoPartes(
                    connection
            );

            cadastrarPessoasIniciais(
                    connection
            );


            System.out.println(
                    "Inicialização do banco concluída."
            );


        } catch (SQLException e) {

            throw new RuntimeException(
                    "Erro ao inicializar o banco de dados.",
                    e
            );
        }

    }

    /**
     * Persiste os papéis especiais que pertencem à escala, sem depender
     * da existência de uma parte/designação correspondente.
     * As colunas são opcionais para preservar escalas antigas.
     */
    static void adicionarColunasPapeisEspeciaisEscala(
            Connection connection
    ) throws SQLException {
        adicionarColunaReferenciaPessoaEscala(connection, "presidente_id");
        adicionarColunaReferenciaPessoaEscala(connection, "dirigente_id");
        adicionarColunaReferenciaPessoaEscala(connection, "oracao_inicial_id");
    }

    private static void adicionarColunaReferenciaPessoaEscala(
            Connection connection,
            String coluna
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(escala)")) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE escala ADD COLUMN " + coluna + " INTEGER REFERENCES pessoa(id)");
        }
    }

    private static void adicionarColunaSalaDesignacao(
            Connection connection
    ) throws SQLException {

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "PRAGMA table_info(designacao)"
                )
        ) {
            while (resultSet.next()) {
                if ("sala".equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                ALTER TABLE designacao
                ADD COLUMN sala TEXT NOT NULL DEFAULT 'PRINCIPAL'
                CHECK (sala IN ('PRINCIPAL', 'B', 'C'))
                """);
        }
    }

    private static void adicionarColunasSalasProgramacaoSemana(
            Connection connection
    ) throws SQLException {
        adicionarColunaSeNecessario(connection, "sala_b_habilitada");
        adicionarColunaSeNecessario(connection, "sala_c_habilitada");
    }

    private static void adicionarColunasDadosS140ProgramacaoSemana(
            Connection connection
    ) throws SQLException {
        adicionarColunaProgramacaoSemanaSeNecessario(
                connection, "conselheiro_sala_b_id",
                "INTEGER REFERENCES pessoa(id) ON DELETE SET NULL"
        );
        adicionarColunaProgramacaoSemanaSeNecessario(
                connection, "cantico_inicial", "TEXT"
        );
        adicionarColunaProgramacaoSemanaSeNecessario(
                connection, "cantico_meio", "TEXT"
        );
        adicionarColunaProgramacaoSemanaSeNecessario(
                connection, "cantico_final", "TEXT"
        );
    }

    private static void adicionarColunaProgramacaoSemanaSeNecessario(
            Connection connection,
            String coluna,
            String definicao
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(programacao_semana)")) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE programacao_semana ADD COLUMN "
                            + coluna + " " + definicao
            );
        }
    }
    private static void adicionarColunasPermissaoSalasPessoa(
            Connection connection
    ) throws SQLException {
        adicionarColunaPessoaSeNecessario(
                connection,
                "pode_participar_sala_principal"
        );

        adicionarColunaPessoaSeNecessario(
                connection,
                "pode_participar_sala_b"
        );

        adicionarColunaPessoaSeNecessario(
                connection,
                "pode_participar_sala_c"
        );
    }

    private static void adicionarColunaPessoaSeNecessario(
            Connection connection,
            String coluna
    ) throws SQLException {

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "PRAGMA table_info(pessoa)"
                )
        ) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE pessoa ADD COLUMN "
                            + coluna
                            + " INTEGER NOT NULL DEFAULT 1"
            );
        }
    }
    private static void adicionarColunaSeNecessario(
            Connection connection,
            String coluna
    ) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "PRAGMA table_info(programacao_semana)"
                )
        ) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE programacao_semana ADD COLUMN "
                            + coluna
                            + " INTEGER NOT NULL DEFAULT 0"
            );
        }
    }

    private static void adicionarColunaDuracaoPadraoParte(
            Connection connection
    ) throws SQLException {
        adicionarColunaInteiraOpcional(
                connection,
                "parte",
                "duracao_padrao_minutos"
        );
    }

    private static void adicionarColunaDuracaoProgramacaoParte(
            Connection connection
    ) throws SQLException {
        adicionarColunaInteiraOpcional(
                connection,
                "programacao_parte",
                "duracao_minutos"
        );
    }

    private static void adicionarColunaInteiraOpcional(
            Connection connection,
            String tabela,
            String coluna
    ) throws SQLException {
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "PRAGMA table_info(" + tabela + ")"
                )
        ) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute(
                    "ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " INTEGER"
            );
        }
    }

    private static void adicionarColunaNumeroOficialProgramacaoParte(
            Connection connection
    ) throws SQLException {

        boolean colunaExiste = false;

        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "PRAGMA table_info(programacao_parte)"
                )
        ) {
            while (resultSet.next()) {
                if ("numero_oficial".equals(
                        resultSet.getString("name")
                )) {
                    colunaExiste = true;
                    break;
                }
            }
        }

        if (!colunaExiste) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE programacao_parte " +
                                "ADD COLUMN numero_oficial INTEGER"
                );
            }

            System.out.println(
                    "Coluna numero_oficial adicionada à programação."
            );
        }
    }

    static void migrarResponsavelDesignacaoNullable(
            Connection connection
    ) throws SQLException {

        boolean responsavelObrigatorio = false;
        boolean colunaSalaExiste = false;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(designacao)"
             )) {

            while (resultSet.next()) {
                String nomeColuna = resultSet.getString("name");

                if ("responsavel_id".equals(nomeColuna)) {
                    responsavelObrigatorio =
                            resultSet.getInt("notnull") == 1;
                }

                if ("sala".equals(nomeColuna)) {
                    colunaSalaExiste = true;
                }
            }
        }

        if (!responsavelObrigatorio) {
            return;
        }

        boolean foreignKeysAtivos;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA foreign_keys"
             )) {

            foreignKeysAtivos =
                    resultSet.next() && resultSet.getInt(1) == 1;
        }

        if (foreignKeysAtivos) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = OFF");
            }
        }

        boolean autoCommitAnterior = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE designacao_migracao (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    escala_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    responsavel_id INTEGER,
                    ajudante_id INTEGER,
                    sala TEXT NOT NULL DEFAULT 'PRINCIPAL'
                        CHECK (sala IN ('PRINCIPAL', 'B', 'C')),
                    FOREIGN KEY (escala_id)
                        REFERENCES escala(id) ON DELETE CASCADE,
                    FOREIGN KEY (parte_id)
                        REFERENCES parte(id),
                    FOREIGN KEY (responsavel_id)
                        REFERENCES pessoa(id),
                    FOREIGN KEY (ajudante_id)
                        REFERENCES pessoa(id)
                )
                """);

            String colunasDestino = """
                id, escala_id, parte_id,
                responsavel_id, ajudante_id, sala
                """;

            String colunasOrigem = colunaSalaExiste
                    ? """
                  id, escala_id, parte_id,
                  responsavel_id, ajudante_id,
                  sala
                  """
                    : """
                  id, escala_id, parte_id,
                  responsavel_id, ajudante_id,
                  'PRINCIPAL'
                  """;

            statement.execute("""
                INSERT INTO designacao_migracao (
                    %s
                )
                SELECT %s
                FROM designacao
                """.formatted(colunasDestino, colunasOrigem));

            statement.execute("DROP TABLE designacao");

            statement.execute("""
                ALTER TABLE designacao_migracao
                RENAME TO designacao
                """);

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            try {
                connection.setAutoCommit(autoCommitAnterior);
            } finally {
                if (foreignKeysAtivos) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("PRAGMA foreign_keys = ON");
                    }
                }
            }
        }
    }

    private static void removerAjudanteConfiguradoNoEstudoBiblico(
            Connection connection
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                DELETE FROM parte_participacao_necessaria
                WHERE tipo_participacao = 'AJUDANTE'
                  AND parte_id IN (
                      SELECT id FROM parte
                      WHERE tipo = 'DIRIGENTE_ESTUDO'
                  )
                """)) {
            statement.executeUpdate();
        }
    }


    /*
     * ============================================================
     * PARTES INICIAIS
     * ============================================================
     */
    private static void adicionarColunaOrdemParte(
            Connection connection
    ) throws SQLException {

        String sqlVerificar = """
                PRAGMA table_info(parte)
                """;

        boolean colunaExiste = false;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sqlVerificar);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            while (resultSet.next()) {

                String nomeColuna =
                        resultSet.getString("name");

                if ("ordem".equalsIgnoreCase(nomeColuna)) {
                    colunaExiste = true;
                    break;
                }
            }
        }

        if (!colunaExiste) {

            try (Statement statement =
                         connection.createStatement()) {

                statement.execute("""
                        ALTER TABLE parte
                        ADD COLUMN ordem INTEGER NOT NULL DEFAULT 999
                        """);
            }
        }
    }

    private static void migrarTipoVariacaoPartes(
            Connection connection
    ) throws SQLException {

        /*
         * ------------------------------------------------------------
         * PARTES FIXAS
         * ------------------------------------------------------------
         *
         * Corrige bancos antigos nos quais tipo_variacao
         * ficou NULL.
         */

        String sqlFixas = """
                UPDATE parte
                SET tipo_variacao = 'FIXA'
                WHERE tipo IN (
                    'PRESIDENTE_REUNIAO',
                    'ORACAO_INICIAL',
                    'DISCURSO_TESOUROS',
                    'JOIAS_ESPIRITUAIS',
                    'LEITURA',
                    'DIRIGENTE_ESTUDO',
                    'ORACAO_FINAL'
                )
                AND (
                    tipo_variacao IS NULL
                    OR TRIM(tipo_variacao) = ''
                )
                """;


        /*
         * ------------------------------------------------------------
         * PARTES VARIÁVEIS
         * ------------------------------------------------------------
         */

        String sqlVariaveis = """
                UPDATE parte
                SET tipo_variacao = 'VARIAVEL'
                WHERE (
                    tipo = 'DEMONSTRACAO'
                    OR tipo = 'O_QUE_VOCE_DIRIA'
                )
                AND (
                    tipo_variacao IS NULL
                    OR TRIM(tipo_variacao) = ''
                )
                """;


        try (
                PreparedStatement fixas =
                        connection.prepareStatement(sqlFixas);

                PreparedStatement variaveis =
                        connection.prepareStatement(sqlVariaveis)
        ) {

            int corrigidasFixas =
                    fixas.executeUpdate();

            int corrigidasVariaveis =
                    variaveis.executeUpdate();


            System.out.println(
                    "Migração de tipo_variacao concluída: "
                            + corrigidasFixas
                            + " partes fixas e "
                            + corrigidasVariaveis
                            + " partes variáveis corrigidas."
            );
        }
    }

    static void atualizarOrdensPadraoPartes(
            Connection connection
    ) throws SQLException {

        String sql = """
                UPDATE parte
                SET ordem = ?
                WHERE tipo = ?
                  AND ordem = 999
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            atualizarOrdem(
                    statement,
                    1,
                    TipoParte.PRESIDENTE_REUNIAO
            );

            atualizarOrdem(
                    statement,
                    2,
                    TipoParte.ORACAO_INICIAL
            );

            atualizarOrdem(
                    statement,
                    3,
                    TipoParte.DISCURSO_TESOUROS
            );

            atualizarOrdem(
                    statement,
                    4,
                    TipoParte.JOIAS_ESPIRITUAIS
            );

            atualizarOrdem(
                    statement,
                    5,
                    TipoParte.LEITURA
            );

            /* * Partes do Ministério e as suas variações. * Cada variação possui uma ordem própria. */
            atualizarOrdemPorNome(statement, 6, "Iniciando Conversas");
            atualizarOrdemPorNome(statement, 7, "Iniciando Conversas _ De casa em casa");
            atualizarOrdemPorNome(statement, 8, "Iniciando Conversas _ Testemunho informal");
            atualizarOrdemPorNome(statement, 9, "Iniciando Conversas _ Testemunho público");
            atualizarOrdemPorNome(statement, 10, "Cultivando Interesse");
            atualizarOrdemPorNome(statement, 11, "Cultivando Interesse _ De casa em casa");
            atualizarOrdemPorNome(statement, 12, "Cultivando Interesse _ Testemunho informal");
            atualizarOrdemPorNome(statement, 13, "Cultivando Interesse _ Testemunho público");
            atualizarOrdem(statement, 14, TipoParte.O_QUE_VOCE_DIRIA);
            atualizarOrdemPorNome(statement, 15, "Fazendo Discípulos");
            atualizarOrdemPorNome(statement, 16, "Explicando suas crenças");
            atualizarOrdemPorNome(statement, 17, "Discurso — Ministério");
            atualizarOrdem(statement, 18, TipoParte.PARTE_1);
            atualizarOrdem(statement, 19, TipoParte.PARTE_2);
            atualizarOrdem(statement, 20, TipoParte.PARTE_3);
            atualizarOrdem(statement, 21, TipoParte.NECESSIDADES_LOCAIS);
            atualizarOrdem(statement, 22, TipoParte.DIRIGENTE_ESTUDO);
            atualizarOrdem(statement, 23, TipoParte.ORACAO_FINAL);
        }
    }

    private static void cadastrarPartesIniciais(
            Connection connection
    ) throws SQLException {

        String sqlVerificar = """
                
                SELECT id
                FROM parte
                WHERE tipo = ?
                  AND nome = ?
                
                """;


        String sqlInserir = """
                
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
                    possui_tema
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                
                """;


        String sqlParticipacao = """
                
                INSERT OR IGNORE INTO parte_participacao_necessaria (
                    parte_id,
                    tipo_participacao,
                    ordem
                )
                VALUES (?, ?, ?)
                
                """;


        try (
                PreparedStatement verificar =
                        connection.prepareStatement(sqlVerificar);

                PreparedStatement inserir =
                        connection.prepareStatement(
                                sqlInserir,
                                Statement.RETURN_GENERATED_KEYS
                        );

                PreparedStatement participacao =
                        connection.prepareStatement(
                                sqlParticipacao
                        )
        ) {

            /*
             * PRESIDENTE
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Presidente",
                    TipoParte.PRESIDENTE_REUNIAO,
                    Privilegio.ANCIAO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    null,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.PRESIDENTE
            );


            /*
             * ORAÇÃO INICIAL
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Oração inicial",
                    TipoParte.ORACAO_INICIAL,
                    Privilegio.BATIZADO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    null,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.ORACAO_INICIAL
            );


            /*
             * DISCURSO — TESOUROS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Discurso — Tesouros",
                    TipoParte.DISCURSO_TESOUROS,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                    TipoVariacaoParte.FIXA,
                    true,

                    TipoParticipacao.ORADOR
            );


            /*
             * JOIAS ESPIRITUAIS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Joias Espirituais",
                    TipoParte.JOIAS_ESPIRITUAIS,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.ORADOR
            );


            /*
             * LEITURA
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Leitura",
                    TipoParte.LEITURA,
                    Privilegio.PUBLICADOR,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.LEITOR
            );


            /*
             * INICIANDO CONVERSAS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Iniciando Conversas",
                    TipoParte.DEMONSTRACAO,
                    Privilegio.PUBLICADOR,
                    SexoPermitido.AMBOS,
                    2,
                    true,
                    true,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.RESPONSAVEL,
                    TipoParticipacao.AJUDANTE
            );


            /*
             * CULTIVANDO INTERESSE
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Cultivando Interesse",
                    TipoParte.DEMONSTRACAO,
                    Privilegio.PUBLICADOR,
                    SexoPermitido.AMBOS,
                    2,
                    true,
                    true,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.RESPONSAVEL,
                    TipoParticipacao.AJUDANTE
            );


            /*
             * O QUE VOCÊ DIRIA?
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "O Que Você Diria?",
                    TipoParte.O_QUE_VOCE_DIRIA,
                    Privilegio.ANCIAO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.PRESIDENTE
            );


            /*
             * FAZENDO DISCÍPULOS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Fazendo Discípulos",
                    TipoParte.DEMONSTRACAO,
                    Privilegio.PUBLICADOR,
                    SexoPermitido.AMBOS,
                    2,
                    true,
                    true,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.RESPONSAVEL,
                    TipoParticipacao.AJUDANTE
            );


            /*
             * EXPLICANDO SUAS CRENÇAS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Explicando suas crenças",
                    TipoParte.DEMONSTRACAO,
                    Privilegio.PUBLICADOR,
                    SexoPermitido.AMBOS,
                    2,
                    true,
                    true,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.RESPONSAVEL,
                    TipoParticipacao.AJUDANTE
            );


            /*
             * DISCURSO — MINISTÉRIO
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Discurso — Ministério",
                    TipoParte.DISCURSO,
                    Privilegio.BATIZADO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.MINISTERIO,
                    TipoVariacaoParte.VARIAVEL,
                    false,

                    TipoParticipacao.ORADOR
            );


            /*
             * PARTE 1
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Parte 1",
                    TipoParte.PARTE_1,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.NOSSA_VIDA_CRISTA,
                    TipoVariacaoParte.VARIAVEL,
                    true,

                    TipoParticipacao.ORADOR
            );


            /*
             * PARTE 2
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Parte 2",
                    TipoParte.PARTE_2,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.NOSSA_VIDA_CRISTA,
                    TipoVariacaoParte.VARIAVEL,
                    true,

                    TipoParticipacao.ORADOR
            );


            /*
             * PARTE 3
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Parte 3",
                    TipoParte.PARTE_3,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.NOSSA_VIDA_CRISTA,
                    TipoVariacaoParte.VARIAVEL,
                    true,

                    TipoParticipacao.ORADOR
            );


            /*
             * NECESSIDADES LOCAIS
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Necessidades Locais",
                    TipoParte.NECESSIDADES_LOCAIS,
                    Privilegio.ANCIAO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.NOSSA_VIDA_CRISTA,
                    TipoVariacaoParte.VARIAVEL,
                    true,

                    TipoParticipacao.ORADOR
            );


            /*
             * ESTUDO BÍBLICO
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Estudo Bíblico",
                    TipoParte.DIRIGENTE_ESTUDO,
                    Privilegio.SERVO_MINISTERIAL,
                    SexoPermitido.MASCULINO,
                    2,
                    true,
                    false,
                    NivelLeitura.BASICO,
                    SecaoParte.NOSSA_VIDA_CRISTA,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.DIRIGENTE
            );


            /*
             * ORAÇÃO FINAL
             */

            inserirParte(
                    verificar,
                    inserir,
                    participacao,

                    "Oração final",
                    TipoParte.ORACAO_FINAL,
                    Privilegio.BATIZADO,
                    SexoPermitido.MASCULINO,
                    1,
                    false,
                    false,
                    NivelLeitura.BASICO,
                    null,
                    TipoVariacaoParte.FIXA,
                    false,

                    TipoParticipacao.ORACAO_FINAL
            );

            /* * VARIAÇÕES — INICIANDO CONVERSAS */
            inserirParte(verificar, inserir, participacao, "Iniciando Conversas _ De casa em casa", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
            inserirParte(verificar, inserir, participacao, "Iniciando Conversas _ Testemunho informal", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
            inserirParte(verificar, inserir, participacao, "Iniciando Conversas _ Testemunho público", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
            /* * VARIAÇÕES — CULTIVANDO INTERESSE */
            inserirParte(verificar, inserir, participacao, "Cultivando Interesse _ De casa em casa", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
            inserirParte(verificar, inserir, participacao, "Cultivando Interesse _ Testemunho informal", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
            inserirParte(verificar, inserir, participacao, "Cultivando Interesse _ Testemunho público", TipoParte.DEMONSTRACAO, Privilegio.PUBLICADOR, SexoPermitido.AMBOS, 2, true, true, NivelLeitura.BASICO, SecaoParte.MINISTERIO, TipoVariacaoParte.VARIAVEL, false, TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE);
        }
    }


    private static void inserirParte(
            PreparedStatement verificar,
            PreparedStatement inserir,
            PreparedStatement participacao,

            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            boolean exigeAjudante,
            NivelLeitura nivelLeituraMinimo,
            SecaoParte secao,
            TipoVariacaoParte tipoVariacao,
            boolean possuiTema,

            TipoParticipacao... participacoes
    ) throws SQLException {

        verificar.setString(
                1,
                tipo.name()
        );

        verificar.setString(
                2,
                nome
        );


        try (
                ResultSet resultado =
                        verificar.executeQuery()
        ) {

            if (resultado.next()) {

                int parteId =
                        resultado.getInt("id");

                inserirParticipacoes(
                        participacao,
                        parteId,
                        participacoes
                );

                return;
            }
        }


        inserir.setString(
                1,
                nome
        );

        inserir.setString(
                2,
                tipo.name()
        );

        inserir.setString(
                3,
                privilegioMinimo.name()
        );

        inserir.setBoolean(
                4,
                exigeAjudante
        );

        inserir.setString(
                5,
                sexoPermitido.name()
        );

        inserir.setInt(
                6,
                quantidadeMinimaParticipantes
        );

        inserir.setBoolean(
                7,
                geraFormulario
        );

        inserir.setString(
                8,
                nivelLeituraMinimo.name()
        );

        inserir.setString(
                9,
                secao == null
                        ? null
                        : secao.name()
        );

        inserir.setString(
                10,
                tipoVariacao == null
                        ? null
                        : tipoVariacao.name()
        );

        inserir.setBoolean(
                11,
                possuiTema
        );


        inserir.executeUpdate();


        int parteId;


        try (
                ResultSet chaves =
                        inserir.getGeneratedKeys()
        ) {

            if (!chaves.next()) {

                throw new SQLException(
                        "Não foi possível obter o ID da Parte."
                );
            }

            parteId =
                    chaves.getInt(1);
        }


        inserirParticipacoes(
                participacao,
                parteId,
                participacoes
        );
    }


    private static void inserirParticipacoes(
            PreparedStatement participacao,
            int parteId,
            TipoParticipacao... participacoes
    ) throws SQLException {

        for (
                int ordem = 0;
                ordem < participacoes.length;
                ordem++
        ) {

            participacao.setInt(
                    1,
                    parteId
            );

            participacao.setString(
                    2,
                    participacoes[ordem].name()
            );

            participacao.setInt(
                    3,
                    ordem
            );

            participacao.executeUpdate();
        }
    }


    /*
     * ============================================================
     * PESSOAS INICIAIS
     * ============================================================
     */

    private static void cadastrarPessoasIniciais(
            Connection connection
    ) throws SQLException {

        String sqlVerificar = """
                
                SELECT COUNT(*)
                FROM pessoa
                WHERE nome = ?
                
                """;


        String sqlInserir = """
                
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
                    nivel_leitura
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                
                """;


        try (
                PreparedStatement verificar =
                        connection.prepareStatement(
                                sqlVerificar
                        );

                PreparedStatement inserir =
                        connection.prepareStatement(
                                sqlInserir
                        )
        ) {

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Carlos Kovalski",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.ANCIAO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Marcio Correa",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.ANCIAO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Alef Dias",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.SERVO_MINISTERIAL
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "João Manoel",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.ANCIAO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Rubens Coelho",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.ANCIAO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Adriana",
                    Sexo.FEMININO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Priscila",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Samara Coelho",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Valdecir",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Ângela",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Marisilvia",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Marli",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Heitor",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Luís Cláudio",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Lídia",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Diogo",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Flaviana",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Lana",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Monique",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Dariane",
                    Sexo.FEMININO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Lourenço",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Rhuan",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.PUBLICADOR
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Elisandro",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Mara",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Sabrina",
                    Sexo.FEMININO,
                    true,
                    Privilegio.PUBLICADOR
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Reginaldo",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Loili",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Loreni",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "José de Quadros",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Jéssica",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Soeli",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Mariane",
                    Sexo.FEMININO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "João Vaz",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Nelci",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Jamyle",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Josane",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Simone",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Dieisson",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Anancí",
                    Sexo.FEMININO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Vagner",
                    Sexo.MASCULINO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "João de Deus",
                    Sexo.MASCULINO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Armelindo",
                    Sexo.MASCULINO,
                    false,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Erazi",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );

            inserirPessoaInicial(
                    verificar,
                    inserir,
                    "Cirlene",
                    Sexo.FEMININO,
                    true,
                    Privilegio.BATIZADO
            );
        }
    }


    private static void inserirPessoaInicial(
            PreparedStatement verificar,
            PreparedStatement inserir,
            String nome,
            Sexo sexo,
            boolean ativo,
            Privilegio privilegio
    ) throws SQLException {

        verificar.setString(
                1,
                nome
        );


        try (
                ResultSet resultado =
                        verificar.executeQuery()
        ) {

            if (
                    resultado.next()
                            && resultado.getInt(1) > 0
            ) {

                return;
            }
        }


        boolean responsavel = false;
        boolean ajudante = false;
        boolean leitura = false;
        boolean discurso = false;
        boolean oracao = false;
        boolean presidente = false;
        boolean dirigente = false;


        if (sexo == Sexo.FEMININO) {

            responsavel = true;
            ajudante = true;

        } else {

            if (privilegio == Privilegio.ANCIAO) {

                responsavel = true;
                ajudante = true;
                leitura = true;
                discurso = true;
                oracao = true;
                presidente = true;
                dirigente = true;

            } else if (
                    privilegio == Privilegio.SERVO_MINISTERIAL
                            || privilegio == Privilegio.BATIZADO
            ) {

                responsavel = true;
                ajudante = true;
                leitura = true;
                discurso = true;
                oracao = true;

            } else if (
                    privilegio == Privilegio.PUBLICADOR
            ) {

                responsavel = true;
                ajudante = true;
            }
        }


        inserir.setString(
                1,
                nome
        );

        inserir.setString(
                2,
                sexo.name()
        );

        inserir.setBoolean(
                3,
                ativo
        );

        inserir.setBoolean(
                4,
                responsavel
        );

        inserir.setBoolean(
                5,
                ajudante
        );

        inserir.setBoolean(
                6,
                leitura
        );

        inserir.setBoolean(
                7,
                discurso
        );

        inserir.setBoolean(
                8,
                oracao
        );

        inserir.setBoolean(
                9,
                presidente
        );

        inserir.setBoolean(
                10,
                dirigente
        );

        inserir.setString(
                11,
                privilegio.name()
        );

        inserir.setString(
                12,
                NivelLeitura.BASICO.name()
        );


        inserir.executeUpdate();
    }

    private static void atualizarOrdem(
            PreparedStatement statement,
            int ordem,
            TipoParte tipo
    ) throws SQLException {

        statement.setInt(1, ordem);
        statement.setString(2, tipo.name());
        statement.executeUpdate();
    }

    private static void atualizarOrdemPorNome(
            PreparedStatement statement,
            int ordem,
            String nome
    ) throws SQLException {

        String sql = """
                UPDATE parte
                SET ordem = ?
                WHERE nome = ?
                  AND ordem = 999
                """;

        try (PreparedStatement update =
                     statement.getConnection()
                             .prepareStatement(sql)) {

            update.setInt(1, ordem);
            update.setString(2, nome);
            update.executeUpdate();
        }
    }

    static void migrarHistoricoDesignacoesSala(
            Connection connection
    ) throws SQLException {

        boolean colunaSalaExiste = false;
        boolean unicidadeIncluiSala = false;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(historico_designacoes)"
             )) {

            while (resultSet.next()) {
                if ("sala".equals(resultSet.getString("name"))) {
                    colunaSalaExiste = true;
                    break;
                }
            }
        }

        if (colunaSalaExiste) {
            try (Statement statement = connection.createStatement();
                 ResultSet indices = statement.executeQuery(
                         "PRAGMA index_list(historico_designacoes)"
                 )) {

                while (indices.next()) {
                    if (indices.getInt("unique") != 1) {
                        continue;
                    }

                    String nomeIndice = indices.getString("name");
                    List<String> colunas = new java.util.ArrayList<>();

                    try (Statement detalhes = connection.createStatement();
                         ResultSet colunasIndice = detalhes.executeQuery(
                                 "PRAGMA index_info('" +
                                         nomeIndice.replace("'", "''") + "')"
                         )) {

                        while (colunasIndice.next()) {
                            colunas.add(
                                    colunasIndice.getString("name")
                            );
                        }
                    }

                    if (colunas.size() == 5
                            && colunas.containsAll(List.of(
                            "data",
                            "pessoa_id",
                            "parte_id",
                            "tipo_participacao",
                            "sala"
                    ))) {
                        unicidadeIncluiSala = true;
                        break;
                    }
                }
            }
        }

        if (colunaSalaExiste && unicidadeIncluiSala) {
            return;
        }

        boolean foreignKeysAtivos;

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA foreign_keys"
             )) {

            foreignKeysAtivos =
                    resultSet.next() && resultSet.getInt(1) == 1;
        }

        if (foreignKeysAtivos) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = OFF");
            }
        }

        boolean autoCommitAnterior = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try (Statement statement = connection.createStatement()) {

            statement.execute("""
            CREATE TABLE historico_designacoes_migracao (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                data DATE NOT NULL,
                pessoa_id INTEGER NOT NULL,
                parte_id INTEGER NOT NULL,
                tipo_participacao TEXT NOT NULL,
                sala TEXT NOT NULL DEFAULT 'PRINCIPAL'
                    CHECK (sala IN ('PRINCIPAL', 'B', 'C')),

                UNIQUE (
                    data,
                    pessoa_id,
                    parte_id,
                    tipo_participacao,
                    sala
                ),

                FOREIGN KEY (pessoa_id)
                    REFERENCES pessoa(id),

                FOREIGN KEY (parte_id)
                    REFERENCES parte(id)
            )
            """);

            String colunaSalaOrigem = colunaSalaExiste
                    ? "sala"
                    : "'PRINCIPAL'";

            statement.execute("""
            INSERT INTO historico_designacoes_migracao (
                id,
                data,
                pessoa_id,
                parte_id,
                tipo_participacao,
                sala
            )
            SELECT
                id,
                data,
                pessoa_id,
                parte_id,
                tipo_participacao,
                %s
            FROM historico_designacoes
            """.formatted(colunaSalaOrigem));

            statement.execute("DROP TABLE historico_designacoes");

            statement.execute("""
            ALTER TABLE historico_designacoes_migracao
            RENAME TO historico_designacoes
            """);

            connection.commit();

        } catch (SQLException e) {
            connection.rollback();
            throw e;

        } finally {
            try {
                connection.setAutoCommit(autoCommitAnterior);
            } finally {
                if (foreignKeysAtivos) {
                    try (Statement statement = connection.createStatement()) {
                        statement.execute("PRAGMA foreign_keys = ON");
                    }
                }
            }
        }
    }
}