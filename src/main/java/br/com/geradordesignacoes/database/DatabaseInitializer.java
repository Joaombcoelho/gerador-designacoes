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
                pode_participar_sala_c INTEGER NOT NULL DEFAULT 1,
                arquivado INTEGER NOT NULL DEFAULT 0
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
                duracao_padrao_minutos INTEGER,
                arquivado INTEGER NOT NULL DEFAULT 0
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
                numero_oficial INTEGER,
                duracao_minutos INTEGER,
            
                FOREIGN KEY (programacao_semana_id)
                    REFERENCES programacao_semana(id)
                    ON DELETE CASCADE,
            
                FOREIGN KEY (parte_id)
                    REFERENCES parte(id),
            
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

    private static void adicionarColunaArquivado(Connection connection, String tabela) throws SQLException {
        if (!tabela.equals("pessoa") && !tabela.equals("parte")) {
            throw new IllegalArgumentException("Tabela não permitida para migração de arquivamento.");
        }
        if (!colunaExiste(connection, tabela, "arquivado")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE " + tabela + " ADD COLUMN arquivado INTEGER NOT NULL DEFAULT 0");
            }
        }
    }


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
            adicionarColunaArquivado(connection, "pessoa");

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
            adicionarColunaArquivado(connection, "parte");

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
            migrarPermissaoPartesRepetidasNaProgramacao(connection);


            /*
             * ----------------------------------------------------
             * DADOS INICIAIS
             * ----------------------------------------------------
             */

            normalizarPartesDuplicadas(connection);

            cadastrarPartesIniciais(
                    connection
            );

            atualizarOrdensPadraoPartes(
                    connection
            );

            // Não declarar sucesso se a conexão estiver sem a proteção de
            // chaves estrangeiras ou se alguma migração deixou referências órfãs.
            validarIntegridadeReferencial(connection);

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
     * Valida a conexão usada pela inicialização antes de declarar sucesso.
     * A inicialização contém unidades transacionais locais; esta verificação
     * detecta violações remanescentes, mas não transforma todas as migrações
     * anteriores em uma única transação global.
     */
    static void validarIntegridadeReferencial(Connection connection)
            throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA foreign_keys")) {
            if (!result.next() || result.getInt(1) != 1) {
                throw new SQLException(
                        "A inicialização não pode ser concluída: chaves estrangeiras estão desativadas na conexão.");
            }
        }

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery("PRAGMA foreign_key_check")) {
            if (result.next()) {
                throw new SQLException("A inicialização não pode ser concluída: "
                        + "violação de chave estrangeira na tabela '"
                        + result.getString(1) + "' (rowid="
                        + result.getString(2) + ", tabela referenciada='"
                        + result.getString(3) + "', restrição="
                        + result.getInt(4) + ").");
            }
        }
    }


    /**
     * Corrige os seis nomes legados antes do cadastro inicial. A migração
     * preserva os IDs quando existe apenas o registro antigo e remapeia todas
     * as chaves estrangeiras quando já existem as duas variantes.
     *
     * Em caso de colisão que não possa ser resolvida sem perder informação,
     * toda a operação é revertida e a inicialização falha explicitamente.
     */
    static void normalizarPartesDuplicadas(Connection connection)
            throws SQLException {
        String[][] nomes = {
                {"Iniciando Conversas_De casa em casa",
                        "Iniciando Conversas _ De casa em casa"},
                {"Iniciando Conversas_Testemunho informal",
                        "Iniciando Conversas _ Testemunho informal"},
                {"Iniciando Conversas_Testemunho público",
                        "Iniciando Conversas _ Testemunho público"},
                {"Cultivando Interesse_De casa em casa",
                        "Cultivando Interesse _ De casa em casa"},
                {"Cultivando Interesse_Testemunho informal",
                        "Cultivando Interesse _ Testemunho informal"},
                {"Cultivando Interesse_Testemunho público",
                        "Cultivando Interesse _ Testemunho público"}
        };

        boolean autoCommitAnterior = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try {
            for (String[] par : nomes) {
                Integer antigoId = buscarIdPartePorNome(connection, par[0]);
                Integer novoId = buscarIdPartePorNome(connection, par[1]);

                if (antigoId == null) {
                    continue;
                }
                if (novoId == null) {
                    try (PreparedStatement ps = connection.prepareStatement(
                            "UPDATE parte SET nome = ? WHERE id = ?")) {
                        ps.setString(1, par[1]);
                        ps.setInt(2, antigoId);
                        ps.executeUpdate();
                    }
                    continue;
                }
                if (antigoId.equals(novoId)) {
                    continue;
                }

                validarPartesEquivalentes(connection, antigoId, novoId,
                        par[0], par[1]);
                remapearReferenciasParte(connection, antigoId, novoId);
                try (PreparedStatement ps = connection.prepareStatement(
                        "DELETE FROM parte WHERE id = ?")) {
                    ps.setInt(1, antigoId);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Não foi possível consolidar a parte legada: " + par[0]);
                    }
                }
            }

            try (Statement st = connection.createStatement();
                 ResultSet rs = st.executeQuery("PRAGMA foreign_key_check")) {
                if (rs.next()) {
                    throw new SQLException("A normalização das partes deixou uma referência estrangeira inválida na tabela "
                            + rs.getString(1) + ".");
                }
            }
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            if (e instanceof SQLException sqlException) {
                throw sqlException;
            }
            throw e;
        } finally {
            connection.setAutoCommit(autoCommitAnterior);
        }
    }

    private static Integer buscarIdPartePorNome(
            Connection connection, String nome) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT id FROM parte WHERE nome = ? ORDER BY id")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                int id = rs.getInt(1);
                if (rs.next()) {
                    throw new SQLException("Há mais de uma parte com o nome '" + nome
                            + "'. A migração foi cancelada para evitar perda de dados.");
                }
                return id;
            }
        }
    }

    private static void validarPartesEquivalentes(
            Connection connection, int antigoId, int novoId,
            String nomeAntigo, String nomeNovo) throws SQLException {
        List<String> colunas = new java.util.ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info(parte)")) {
            while (rs.next()) {
                String coluna = rs.getString("name");
                // A ordem padrão da variante canônica é recalculada pelo
                // inicializador; registros legados podem continuar com 999.
                if (!"id".equals(coluna)
                        && !"nome".equals(coluna)
                        && !"ordem".equals(coluna)) {
                    colunas.add(coluna);
                }
            }
        }

        String selecao = String.join(", ", colunas);
        try (PreparedStatement psAntigo = connection.prepareStatement(
                    "SELECT " + selecao + " FROM parte WHERE id = ?");
             PreparedStatement psNovo = connection.prepareStatement(
                    "SELECT " + selecao + " FROM parte WHERE id = ?")) {
            psAntigo.setInt(1, antigoId);
            psNovo.setInt(1, novoId);
            try (ResultSet antigo = psAntigo.executeQuery();
                 ResultSet novo = psNovo.executeQuery()) {
                if (!antigo.next() || !novo.next()) {
                    throw new SQLException("Não foi possível validar os registros de parte.");
                }
                for (int i = 1; i <= colunas.size(); i++) {
                    Object valorAntigo = antigo.getObject(i);
                    Object valorNovo = novo.getObject(i);
                    if (!java.util.Objects.equals(valorAntigo, valorNovo)) {
                        throw new SQLException("As partes '" + nomeAntigo + "' e '"
                                + nomeNovo + "' possuem configurações diferentes (coluna "
                                + colunas.get(i - 1) + "). A migração foi revertida para preservar os dados.");
                    }
                }
            }
        }
    }

    private static void remapearReferenciasParte(
            Connection connection, int antigoId, int novoId)
            throws SQLException {
        List<String[]> referencias = new java.util.ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet tabelas = st.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type = 'table' "
                             + "AND name NOT LIKE 'sqlite_%'")) {
            while (tabelas.next()) {
                String tabela = tabelas.getString(1);
                try (Statement fkStatement = connection.createStatement();
                     ResultSet fks = fkStatement.executeQuery(
                             "PRAGMA foreign_key_list('" + tabela.replace("'", "''") + "')")) {
                    while (fks.next()) {
                        if ("parte".equalsIgnoreCase(fks.getString("table"))
                                && "id".equalsIgnoreCase(fks.getString("to"))) {
                            referencias.add(new String[]{tabela, fks.getString("from")});
                        }
                    }
                }
            }
        }

        for (String[] referencia : referencias) {
            String tabela = referencia[0];
            String coluna = referencia[1];
            if ("parte".equalsIgnoreCase(tabela)) {
                continue;
            }
            if ("parte_participacao_necessaria".equalsIgnoreCase(tabela)) {
                consolidarParticipacoesNecessarias(
                        connection, antigoId, novoId);
                continue;
            }
            if ("programacao_parte".equalsIgnoreCase(tabela)) {
                consolidarProgramacaoParte(connection, antigoId, novoId);
                continue;
            }
            if ("historico_designacoes".equalsIgnoreCase(tabela)) {
                validarSemColisaoHistorico(connection, antigoId, novoId);
            }
            String sql = "UPDATE \"" + tabela.replace("\"", "\"\"")
                    + "\" SET \"" + coluna.replace("\"", "\"\"")
                    + "\" = ? WHERE \"" + coluna.replace("\"", "\"\"") + "\" = ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setInt(1, novoId);
                ps.setInt(2, antigoId);
                ps.executeUpdate();
            }
        }
    }

    private static void consolidarParticipacoesNecessarias(
            Connection connection, int antigoId, int novoId)
            throws SQLException {
        try (PreparedStatement conflitos = connection.prepareStatement("""
                SELECT antiga.tipo_participacao
                FROM parte_participacao_necessaria antiga
                JOIN parte_participacao_necessaria nova
                  ON nova.parte_id = ? 
                 AND nova.tipo_participacao = antiga.tipo_participacao
                WHERE antiga.parte_id = ? AND nova.ordem <> antiga.ordem
                """)) {
            conflitos.setInt(1, novoId);
            conflitos.setInt(2, antigoId);
            try (ResultSet rs = conflitos.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("As partes duplicadas possuem ordenações diferentes para a participação "
                            + rs.getString(1) + ". A migração foi revertida.");
                }
            }
        }
        try (PreparedStatement apagarDuplicadas = connection.prepareStatement("""
                DELETE FROM parte_participacao_necessaria
                WHERE parte_id = ?
                  AND tipo_participacao IN (
                    SELECT tipo_participacao
                    FROM parte_participacao_necessaria WHERE parte_id = ?
                  )
                """)) {
            apagarDuplicadas.setInt(1, antigoId);
            apagarDuplicadas.setInt(2, novoId);
            apagarDuplicadas.executeUpdate();
        }
        try (PreparedStatement atualizar = connection.prepareStatement(
                "UPDATE parte_participacao_necessaria SET parte_id = ? WHERE parte_id = ?")) {
            atualizar.setInt(1, novoId);
            atualizar.setInt(2, antigoId);
            atualizar.executeUpdate();
        }
    }

    private static void consolidarProgramacaoParte(
            Connection connection, int antigoId, int novoId)
            throws SQLException {
        try (PreparedStatement conflitos = connection.prepareStatement("""
                SELECT antiga.id
                FROM programacao_parte antiga
                JOIN programacao_parte nova
                  ON nova.programacao_semana_id = antiga.programacao_semana_id
                 AND nova.parte_id = ?
                WHERE antiga.parte_id = ?
                """)) {
            conflitos.setInt(1, novoId);
            conflitos.setInt(2, antigoId);
            try (ResultSet rs = conflitos.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("As duas variantes de uma parte aparecem na mesma programação semanal. "
                            + "A migração foi revertida para que nenhuma entrada da programação seja descartada.");
                }
            }
        }
        try (PreparedStatement atualizar = connection.prepareStatement(
                "UPDATE programacao_parte SET parte_id = ? WHERE parte_id = ?")) {
            atualizar.setInt(1, novoId);
            atualizar.setInt(2, antigoId);
            atualizar.executeUpdate();
        }
    }

    private static void validarSemColisaoHistorico(
            Connection connection, int antigoId, int novoId)
            throws SQLException {
        // Confere exatamente as colunas que compõem a restrição UNIQUE
        // (data, pessoa_id, parte_id, tipo_participacao), sem depender
        // de colunas opcionais que variam entre versões do banco.
        String sql = """
                SELECT h_antigo.id
                FROM historico_designacoes AS h_antigo
                INNER JOIN historico_designacoes AS h_novo
                  ON h_novo.data = h_antigo.data
                 AND h_novo.pessoa_id = h_antigo.pessoa_id
                 AND h_novo.tipo_participacao = h_antigo.tipo_participacao
                WHERE h_antigo.parte_id = ? AND h_novo.parte_id = ?
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, antigoId);
            ps.setInt(2, novoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    throw new SQLException("As partes duplicadas possuem registros de histórico que colidiriam "
                            + "com a restrição UNIQUE. A migração foi revertida para preservar o histórico.");
                }
            }
        }
    }

    private static boolean colunaExiste(
            Connection connection, String tabela, String coluna)
            throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(
                     "PRAGMA table_info('" + tabela.replace("'", "''") + "')")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
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

    /**
     * Remove a restrição histórica que impedia repetir a mesma parte do
     * catálogo na programação semanal. Cada ocorrência continua protegida
     * por um ID próprio e por uma ordem única dentro da semana.
     */
    private static void migrarPermissaoPartesRepetidasNaProgramacao(
            Connection connection
    ) throws SQLException {
        String definicao = null;
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'programacao_parte'")) {
            if (resultSet.next()) {
                definicao = resultSet.getString("sql");
            }
        }
        if (definicao == null) {
            return;
        }

        String normalizada = definicao.replaceAll("\\s+", " ").toUpperCase(java.util.Locale.ROOT);
        if (!normalizada.matches("(?s).*UNIQUE\\s*\\(\\s*PROGRAMACAO_SEMANA_ID\\s*,\\s*PARTE_ID\\s*\\).*")) {
            return;
        }

        boolean autoCommitAnterior = connection.getAutoCommit();
        connection.setAutoCommit(false);
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE programacao_parte RENAME TO programacao_parte_migracao_repeticao");
            statement.execute(CREATE_TABLE_PROGRAMACAO_PARTE);
            statement.execute("""
                    INSERT INTO programacao_parte (
                        id, programacao_semana_id, parte_id, ordem, tema,
                        numero_oficial, duracao_minutos
                    )
                    SELECT
                        id, programacao_semana_id, parte_id, ordem, tema,
                        numero_oficial, duracao_minutos
                    FROM programacao_parte_migracao_repeticao
                    """);
            statement.execute("DROP TABLE programacao_parte_migracao_repeticao");
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(autoCommitAnterior);
        }

        System.out.println("Migração concluída: a programação aceita ocorrências repetidas de uma parte.");
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
            atualizarOrdemPorNome(statement, 7, "Iniciando conversas_De casa em casa");
            atualizarOrdemPorNome(statement, 8, "Iniciando conversas_Testemunho informal");
            atualizarOrdemPorNome(statement, 9, "Iniciando conversas_Testemunho público");
            atualizarOrdemPorNome(statement, 10, "Cultivando Interesse");
            atualizarOrdemPorNome(statement, 11, "Cultivando interesse_De casa em casa");
            atualizarOrdemPorNome(statement, 12, "Cultivando interesse_Testemunho informal");
            atualizarOrdemPorNome(statement, 13, "Cultivando interesse_Testemunho público");
            atualizarOrdem(statement, 14, TipoParte.O_QUE_VOCE_DIRIA);
            atualizarOrdemPorNome(statement, 15, "Fazendo discípulos");
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