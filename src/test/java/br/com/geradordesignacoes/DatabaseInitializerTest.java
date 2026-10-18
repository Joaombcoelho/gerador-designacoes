
package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseInitializerTest {

    @TempDir
    Path diretorioTemporario;

    @Test
    void devePreservarDesignacoesAntigasSemColunaSala()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("antigo.db");

        try (Connection connection = criarBanco(
                arquivo,
                false,
                true,
                List.of("PRINCIPAL")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals(
                    "PRINCIPAL",
                    buscarSala(connection, 1)
            );

            assertEquals(
                    1,
                    contarDesignacoes(connection)
            );

            assertEquals(
                    10,
                    buscarValor(connection,
                            "SELECT responsavel_id FROM designacao WHERE id = 1")
            );

            assertEquals(
                    20,
                    buscarValor(connection,
                            "SELECT ajudante_id FROM designacao WHERE id = 1")
            );

            assertTrue(colunaExiste(connection, "sala"));
            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void devePreservarTodasAsSalasDuranteAMigracao()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("salas.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                true,
                List.of("PRINCIPAL", "B", "C")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals("PRINCIPAL", buscarSala(connection, 1));
            assertEquals("B", buscarSala(connection, 2));
            assertEquals("C", buscarSala(connection, 3));

            assertEquals(3, contarDesignacoes(connection));

            assertEquals(
                    10,
                    buscarValor(connection,
                            "SELECT responsavel_id FROM designacao WHERE id = 2")
            );

            assertEquals(
                    20,
                    buscarValor(connection,
                            "SELECT ajudante_id FROM designacao WHERE id = 2")
            );

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void naoDeveAlterarDesignacoesQuandoResponsavelJaPermiteNulo()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("atualizado.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                false,
                List.of("PRINCIPAL", "B", "C")
        )) {
            DatabaseInitializer.migrarResponsavelDesignacaoNullable(
                    connection
            );

            assertEquals("PRINCIPAL", buscarSala(connection, 1));
            assertEquals("B", buscarSala(connection, 2));
            assertEquals("C", buscarSala(connection, 3));

            assertEquals(3, contarDesignacoes(connection));

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));
        }
    }

    @Test
    void deveReverterMigracaoQuandoEncontrarSalaInvalida()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("invalido.db");

        try (Connection connection = criarBanco(
                arquivo,
                true,
                true,
                List.of("SALA_INVALIDA")
        )) {
            SQLException erro = assertThrows(
                    SQLException.class,
                    () -> DatabaseInitializer
                            .migrarResponsavelDesignacaoNullable(
                                    connection
                            )
            );

            assertNotNull(erro);

            // A tabela original e seus dados devem continuar existindo.
            assertEquals(
                    "SALA_INVALIDA",
                    buscarSala(connection, 1)
            );

            assertEquals(1, contarDesignacoes(connection));

            assertFalse(tabelaExiste(
                    connection,
                    "designacao_migracao"
            ));

            assertTrue(foreignKeysAtivos(connection));
        }
    }

    @Test
    void devePreservarHistoricoAntigoAoAdicionarColunaSala()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-antigo.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    id, data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    7, '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT id, data, pessoa_id, parte_id,
                        tipo_participacao, sala
                 FROM historico_designacoes
                 WHERE id = 7
                 """)) {

                assertTrue(resultado.next());
                assertEquals(7, resultado.getInt("id"));
                assertEquals("2026-10-01", resultado.getString("data"));
                assertEquals(10, resultado.getInt("pessoa_id"));
                assertEquals(30, resultado.getInt("parte_id"));
                assertEquals(
                        "DISCURSO",
                        resultado.getString("tipo_participacao")
                );
                assertEquals("PRINCIPAL", resultado.getString("sala"));
                assertFalse(resultado.next());
            }
        }
    }

    @Test
    void devePermitirMesmaParticipacaoEmSalasDiferentes()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-salas.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao, sala
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO', 'B'
                )
                """);
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT COUNT(*)
                 FROM historico_designacoes
                 WHERE data = '2026-10-01'
                   AND pessoa_id = 10
                   AND parte_id = 30
                   AND tipo_participacao = 'DISCURSO'
                 """)) {

                assertTrue(resultado.next());
                assertEquals(2, resultado.getInt(1));
            }
        }
    }


    @Test
    void naoDeveAlterarHistoricoAoExecutarMigracaoNovamente()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("historico-repetido.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                CREATE TABLE pessoa (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("""
                CREATE TABLE parte (
                    id INTEGER PRIMARY KEY
                )
                """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");

                statement.execute("""
                CREATE TABLE historico_designacoes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    data DATE NOT NULL,
                    pessoa_id INTEGER NOT NULL,
                    parte_id INTEGER NOT NULL,
                    tipo_participacao TEXT NOT NULL,
                    UNIQUE (
                        data,
                        pessoa_id,
                        parte_id,
                        tipo_participacao
                    ),
                    FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                    FOREIGN KEY (parte_id) REFERENCES parte(id)
                )
                """);

                statement.execute("""
                INSERT INTO historico_designacoes (
                    data, pessoa_id, parte_id, tipo_participacao
                )
                VALUES (
                    '2026-10-01', 10, 30, 'DISCURSO'
                )
                """);
            }

            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);
            DatabaseInitializer.migrarHistoricoDesignacoesSala(connection);

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT COUNT(*)
                 FROM historico_designacoes
                 """)) {

                assertTrue(resultado.next());
                assertEquals(1, resultado.getInt(1));
            }

            try (Statement statement = connection.createStatement();
                 ResultSet resultado = statement.executeQuery("""
                 SELECT sala
                 FROM historico_designacoes
                 """)) {

                assertTrue(resultado.next());
                assertEquals("PRINCIPAL", resultado.getString("sala"));
                assertFalse(resultado.next());
            }
        }
    }

    @Test
    void devePreservarOrdemPersonalizadaAoAtualizarOrdensPadrao()
            throws Exception {

        Path arquivo = diretorioTemporario.resolve("ordem-personalizada.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {

            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        CREATE TABLE parte (
                            id INTEGER PRIMARY KEY,
                            nome TEXT NOT NULL,
                            tipo TEXT NOT NULL,
                            ordem INTEGER NOT NULL DEFAULT 999
                        )
                        """);

                statement.execute("""
                        INSERT INTO parte (id, nome, tipo, ordem)
                        VALUES
                            (1, 'Presidente', 'PRESIDENTE_REUNIAO', 42),
                            (2, 'Oração inicial', 'ORACAO_INICIAL', 999)
                        """);
            }

            DatabaseInitializer.atualizarOrdensPadraoPartes(connection);

            assertEquals(
                    42,
                    buscarValor(connection,
                            "SELECT ordem FROM parte WHERE id = 1")
            );
            assertEquals(
                    2,
                    buscarValor(connection,
                            "SELECT ordem FROM parte WHERE id = 2")
            );

            // A segunda execução não deve modificar novamente a ordem salva.
            DatabaseInitializer.atualizarOrdensPadraoPartes(connection);

            assertEquals(
                    42,
                    buscarValor(connection,
                            "SELECT ordem FROM parte WHERE id = 1")
            );
            assertEquals(
                    2,
                    buscarValor(connection,
                            "SELECT ordem FROM parte WHERE id = 2")
            );
        }
    }

    private Connection criarBanco(
            Path arquivo,
            boolean possuiSala,
            boolean responsavelObrigatorio,
            List<String> salas
    ) throws SQLException {

        Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath()
        );

        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");

                statement.execute("""
                        CREATE TABLE pessoa (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("""
                        CREATE TABLE parte (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("""
                        CREATE TABLE escala (
                            id INTEGER PRIMARY KEY
                        )
                        """);

                statement.execute("INSERT INTO pessoa (id) VALUES (10)");
                statement.execute("INSERT INTO pessoa (id) VALUES (20)");
                statement.execute("INSERT INTO parte (id) VALUES (30)");
                statement.execute("INSERT INTO escala (id) VALUES (40)");

                String colunaResponsavel = responsavelObrigatorio
                        ? "responsavel_id INTEGER NOT NULL"
                        : "responsavel_id INTEGER";

                String colunaSala = possuiSala
                        ? ", sala TEXT NOT NULL DEFAULT 'PRINCIPAL'"
                        : "";

                statement.execute("""
                        CREATE TABLE designacao (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            escala_id INTEGER NOT NULL,
                            parte_id INTEGER NOT NULL,
                            %s,
                            ajudante_id INTEGER
                            %s,
                            FOREIGN KEY (escala_id)
                                REFERENCES escala(id) ON DELETE CASCADE,
                            FOREIGN KEY (parte_id)
                                REFERENCES parte(id),
                            FOREIGN KEY (responsavel_id)
                                REFERENCES pessoa(id),
                            FOREIGN KEY (ajudante_id)
                                REFERENCES pessoa(id)
                        )
                        """.formatted(colunaResponsavel, colunaSala));
            }

            for (int i = 0; i < salas.size(); i++) {
                int id = i + 1;

                String sql = possuiSala
                        ? """
                          INSERT INTO designacao (
                              id, escala_id, parte_id,
                              responsavel_id, ajudante_id, sala
                          )
                          VALUES (%d, 40, 30, 10, 20, '%s')
                          """.formatted(id, salas.get(i))
                        : """
                          INSERT INTO designacao (
                              id, escala_id, parte_id,
                              responsavel_id, ajudante_id
                          )
                          VALUES (%d, 40, 30, 10, 20)
                          """.formatted(id);

                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                }
            }

            return connection;

        } catch (SQLException | RuntimeException e) {
            try {
                connection.close();
            } catch (SQLException erroFechamento) {
                e.addSuppressed(erroFechamento);
            }
            throw e;
        }
    }

    private String buscarSala(
            Connection connection,
            int id
    ) throws SQLException {

        try (var statement = connection.prepareStatement("""
                SELECT sala
                FROM designacao
                WHERE id = ?
                """)) {

            statement.setInt(1, id);

            try (ResultSet resultado = statement.executeQuery()) {
                assertTrue(resultado.next());
                return resultado.getString("sala");
            }
        }
    }

    private int contarDesignacoes(
            Connection connection
    ) throws SQLException {

        return buscarValor(
                connection,
                "SELECT COUNT(*) FROM designacao"
        );
    }

    private int buscarValor(
            Connection connection,
            String sql
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(sql)) {

            assertTrue(resultado.next());
            return resultado.getInt(1);
        }
    }

    private boolean colunaExiste(
            Connection connection,
            String nomeColuna
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "PRAGMA table_info(designacao)"
             )) {

            while (resultado.next()) {
                if (nomeColuna.equals(resultado.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean tabelaExiste(
            Connection connection,
            String nomeTabela
    ) throws SQLException {

        try (var statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM sqlite_master
                WHERE type = 'table'
                  AND name = ?
                """)) {

            statement.setString(1, nomeTabela);

            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() && resultado.getInt(1) > 0;
            }
        }
    }

    private boolean foreignKeysAtivos(
            Connection connection
    ) throws SQLException {

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(
                     "PRAGMA foreign_keys"
             )) {

            return resultado.next() && resultado.getInt(1) == 1;
        }
    }


    private boolean colunaExiste(
            Connection connection,
            String coluna,
            String tabela
    ) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "PRAGMA table_info(" + tabela + ")")) {
            while (resultSet.next()) {
                if (coluna.equals(resultSet.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    void deveAdicionarColunasDosPapeisEspeciaisDeFormaIdempotente()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("papeis-especiais.db");

        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath());
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE pessoa (id INTEGER PRIMARY KEY)");
            statement.execute("""
                    CREATE TABLE escala (
                        id INTEGER PRIMARY KEY,
                        data TEXT NOT NULL,
                        status TEXT NOT NULL,
                        data_geracao TEXT NOT NULL,
                        data_salvamento TEXT
                    )
                    """);
            statement.execute("""
                    INSERT INTO escala (id, data, status, data_geracao)
                    VALUES (1, '2026-10-21', 'GERADA', '2026-10-15T10:00:00')
                    """);

            DatabaseInitializer.adicionarColunasPapeisEspeciaisEscala(connection);
            DatabaseInitializer.adicionarColunasPapeisEspeciaisEscala(connection);

            assertTrue(colunaExiste(connection, "presidente_id", "escala"));
            assertTrue(colunaExiste(connection, "dirigente_id", "escala"));
            assertTrue(colunaExiste(connection, "oracao_inicial_id", "escala"));

            assertEquals(1, buscarValor(connection,
                    "SELECT COUNT(*) FROM escala WHERE id = 1"));
        }
    }


    @Test
    void deveRenomearParteLegadaPreservandoIdEPermitirReexecucao()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-legada.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 41, "Iniciando Conversas_De casa em casa", "CONFIG");
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(41, buscarValor(connection,
                    "SELECT id FROM parte WHERE nome = 'Iniciando Conversas _ De casa em casa'"));
            assertEquals(1, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte WHERE nome LIKE 'Iniciando Conversas%'"));
        }
    }

    @Test
    void deveRemapearDesignacaoAoConsolidarParteDuplicada()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-referenciada.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO escala (id) VALUES (1)");
                st.execute("INSERT INTO designacao (id, escala_id, parte_id) VALUES (7, 1, 10)");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(20, buscarValor(connection,
                    "SELECT parte_id FROM designacao WHERE id = 7"));
            assertEquals(0, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte WHERE id = 10"));
            assertEquals(1, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte WHERE id = 20"));
            assertTrue(foreignKeysAtivos(connection));
        }
    }

    @Test
    void deveUnirParticipacoesNecessariasSemDuplicarChaves()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-participacoes.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO parte_participacao_necessaria VALUES (10, 'RESPONSAVEL', 1)");
                st.execute("INSERT INTO parte_participacao_necessaria VALUES (20, 'RESPONSAVEL', 1)");
                st.execute("INSERT INTO parte_participacao_necessaria VALUES (10, 'AJUDANTE', 2)");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(2, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte_participacao_necessaria WHERE parte_id = 20"));
            assertEquals(0, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte_participacao_necessaria WHERE parte_id = 10"));
        }
    }

    @Test
    void deveReverterQuandoPartesColidemNaMesmaProgramacao()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-programacao-conflito.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO programacao_semana (id, data) VALUES (1, '2026-10-21')");
                st.execute("INSERT INTO programacao_parte (id, programacao_semana_id, parte_id, ordem) VALUES (1, 1, 10, 1)");
                st.execute("INSERT INTO programacao_parte (id, programacao_semana_id, parte_id, ordem) VALUES (2, 1, 20, 2)");
            }
            assertThrows(SQLException.class,
                    () -> DatabaseInitializer.normalizarPartesDuplicadas(connection));
            assertEquals("Iniciando Conversas_De casa em casa",
                    buscarTexto(connection, "SELECT nome FROM parte WHERE id = 10"));
            assertEquals(10, buscarValor(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 1"));
            assertEquals(20, buscarValor(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 2"));
        }
    }

    @Test
    void deveReverterQuandoHistoricoColidirComRestricaoUnique()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-historico-conflito.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO pessoa (id) VALUES (3)");
                st.execute("INSERT INTO historico_designacoes (id, data, pessoa_id, parte_id, tipo_participacao) VALUES (1, '2026-10-01', 3, 10, 'RESPONSAVEL')");
                st.execute("INSERT INTO historico_designacoes (id, data, pessoa_id, parte_id, tipo_participacao) VALUES (2, '2026-10-01', 3, 20, 'RESPONSAVEL')");
            }
            assertThrows(SQLException.class,
                    () -> DatabaseInitializer.normalizarPartesDuplicadas(connection));
            assertEquals(2, buscarValor(connection,
                    "SELECT COUNT(*) FROM historico_designacoes"));
            assertEquals("Iniciando Conversas_De casa em casa",
                    buscarTexto(connection, "SELECT nome FROM parte WHERE id = 10"));
        }
    }

    @Test
    void deveReverterQuandoConfiguracoesDasPartesForemDiferentes()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-config-conflito.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG-A");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG-B");
            assertThrows(SQLException.class,
                    () -> DatabaseInitializer.normalizarPartesDuplicadas(connection));
            assertEquals(2, buscarValor(connection, "SELECT COUNT(*) FROM parte"));
            assertEquals("Iniciando Conversas_De casa em casa",
                    buscarTexto(connection, "SELECT nome FROM parte WHERE id = 10"));
        }
    }

    @Test
    void deveNormalizarTodosOsSeisNomesLegados()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("seis-partes-legadas.db");
        String[] antigos = {
                "Iniciando Conversas_De casa em casa",
                "Iniciando Conversas_Testemunho informal",
                "Iniciando Conversas_Testemunho público",
                "Cultivando Interesse_De casa em casa",
                "Cultivando Interesse_Testemunho informal",
                "Cultivando Interesse_Testemunho público"
        };
        String[] novos = {
                "Iniciando Conversas _ De casa em casa",
                "Iniciando Conversas _ Testemunho informal",
                "Iniciando Conversas _ Testemunho público",
                "Cultivando Interesse _ De casa em casa",
                "Cultivando Interesse _ Testemunho informal",
                "Cultivando Interesse _ Testemunho público"
        };
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            for (int i = 0; i < antigos.length; i++) {
                inserirParte(connection, 100 + i, antigos[i], "CONFIG");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            for (String nome : novos) {
                assertEquals(1, contarPorNome(connection, nome));
            }
            assertEquals(6, buscarValor(connection, "SELECT COUNT(*) FROM parte"));
        }
    }


    @Test
    void deveRemapearProgramacaoSemColisaoPreservandoDados()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-programacao-remapeada.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO programacao_semana (id, data) VALUES (1, '2026-10-21')");
                st.execute("INSERT INTO programacao_semana (id, data) VALUES (2, '2026-10-28')");
                st.execute("INSERT INTO programacao_parte (id, programacao_semana_id, parte_id, ordem, tema, duracao_minutos) VALUES (1, 1, 10, 3, 'Tema preservado', 5)");
                st.execute("INSERT INTO programacao_parte (id, programacao_semana_id, parte_id, ordem) VALUES (2, 2, 20, 4)");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(20, buscarValor(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 1"));
            assertEquals("Tema preservado", buscarTexto(connection,
                    "SELECT tema FROM programacao_parte WHERE id = 1"));
            assertEquals(5, buscarValor(connection,
                    "SELECT duracao_minutos FROM programacao_parte WHERE id = 1"));
            assertEquals(20, buscarValor(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 2"));
        }
    }

    @Test
    void deveRemapearHistoricoSemColisaoPreservandoIdentificador()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-historico-remapeado.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("INSERT INTO pessoa (id) VALUES (3)");
                st.execute("INSERT INTO historico_designacoes (id, data, pessoa_id, parte_id, tipo_participacao) VALUES (77, '2026-10-01', 3, 10, 'RESPONSAVEL')");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(77, buscarValor(connection,
                    "SELECT id FROM historico_designacoes WHERE parte_id = 20"));
            assertEquals(0, buscarValor(connection,
                    "SELECT COUNT(*) FROM historico_designacoes WHERE parte_id = 10"));
        }
    }

    @Test
    void deveIgnorarQuandoNaoExistemNomesLegados()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("sem-partes-legadas.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(1, buscarValor(connection, "SELECT COUNT(*) FROM parte"));
            assertEquals("Iniciando Conversas _ De casa em casa",
                    buscarTexto(connection, "SELECT nome FROM parte WHERE id = 20"));
        }
    }


    @Test
    void devePreservarOrdemDaVarianteCanonicaAoConsolidar()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("parte-ordem.db");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath())) {
            criarEsquemaPartes(connection);
            inserirParte(connection, 10, "Iniciando Conversas_De casa em casa", "CONFIG");
            inserirParte(connection, 20, "Iniciando Conversas _ De casa em casa", "CONFIG");
            try (Statement st = connection.createStatement()) {
                st.execute("UPDATE parte SET ordem = 999 WHERE id = 10");
                st.execute("UPDATE parte SET ordem = 7 WHERE id = 20");
            }
            DatabaseInitializer.normalizarPartesDuplicadas(connection);
            assertEquals(7, buscarValor(connection,
                    "SELECT ordem FROM parte WHERE id = 20"));
            assertEquals(0, buscarValor(connection,
                    "SELECT COUNT(*) FROM parte WHERE id = 10"));
        }
    }

    private void criarEsquemaPartes(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
            st.execute("""
                    CREATE TABLE parte (
                        id INTEGER PRIMARY KEY,
                        nome TEXT NOT NULL,
                        tipo TEXT NOT NULL,
                        ordem INTEGER NOT NULL DEFAULT 999
                    )
                    """);
            st.execute("CREATE TABLE pessoa (id INTEGER PRIMARY KEY)");
            st.execute("CREATE TABLE escala (id INTEGER PRIMARY KEY)");
            st.execute("""
                    CREATE TABLE designacao (
                        id INTEGER PRIMARY KEY,
                        escala_id INTEGER NOT NULL REFERENCES escala(id),
                        parte_id INTEGER NOT NULL REFERENCES parte(id)
                    )
                    """);
            st.execute("""
                    CREATE TABLE parte_participacao_necessaria (
                        parte_id INTEGER NOT NULL REFERENCES parte(id) ON DELETE CASCADE,
                        tipo_participacao TEXT NOT NULL,
                        ordem INTEGER NOT NULL,
                        PRIMARY KEY (parte_id, tipo_participacao)
                    )
                    """);
            st.execute("""
                    CREATE TABLE historico_designacoes (
                        id INTEGER PRIMARY KEY,
                        data TEXT NOT NULL,
                        pessoa_id INTEGER NOT NULL REFERENCES pessoa(id),
                        parte_id INTEGER NOT NULL REFERENCES parte(id),
                        tipo_participacao TEXT NOT NULL,
                        UNIQUE (data, pessoa_id, parte_id, tipo_participacao)
                    )
                    """);
            st.execute("""
                    CREATE TABLE programacao_semana (
                        id INTEGER PRIMARY KEY,
                        data TEXT NOT NULL
                    )
                    """);
            st.execute("""
                    CREATE TABLE programacao_parte (
                        id INTEGER PRIMARY KEY,
                        programacao_semana_id INTEGER NOT NULL REFERENCES programacao_semana(id),
                        parte_id INTEGER NOT NULL REFERENCES parte(id),
                        ordem INTEGER NOT NULL,
                        tema TEXT,
                        duracao_minutos INTEGER,
                        UNIQUE (programacao_semana_id, parte_id),
                        UNIQUE (programacao_semana_id, ordem)
                    )
                    """);
        }
    }

    private void inserirParte(
            Connection connection, int id, String nome, String tipo)
            throws SQLException {
        try (var ps = connection.prepareStatement(
                "INSERT INTO parte (id, nome, tipo) VALUES (?, ?, ?)")) {
            ps.setInt(1, id);
            ps.setString(2, nome);
            ps.setString(3, tipo);
            ps.executeUpdate();
        }
    }

    private int contarPorNome(Connection connection, String nome)
            throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM parte WHERE nome = ?")) {
            ps.setString(1, nome);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private String buscarTexto(Connection connection, String sql)
            throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (!rs.next()) throw new SQLException("Consulta sem resultado.");
            return rs.getString(1);
        }
    }


}
