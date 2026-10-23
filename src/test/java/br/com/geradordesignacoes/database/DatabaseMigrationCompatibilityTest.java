package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseMigrationCompatibilityTest {

    @TempDir
    Path diretorioTemporario;

    private ConnectionFactory.TestContext contexto;

    @AfterEach
    void fecharContexto() throws Exception {
        if (contexto != null) {
            contexto.close();
        }
    }

    @Test
    void deveAtualizarEsquemaHistoricoPreservandoDadosERelacoes() throws Exception {
        Path arquivo = diretorioTemporario.resolve("esquema-legado.db");
        criarEsquemaHistorico(arquivo, false);
        contexto = ConnectionFactory.openTestContext(arquivo);

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals("Pessoa histórica", texto(connection,
                    "SELECT nome FROM pessoa WHERE id = 500"));
            assertEquals(1, inteiro(connection,
                    "SELECT pode_participar_sala_principal FROM pessoa WHERE id = 500"));
            assertEquals(0, inteiro(connection,
                    "SELECT arquivado FROM pessoa WHERE id = 500"));

            assertEquals("Parte histórica A", texto(connection,
                    "SELECT nome FROM parte WHERE id = 600"));
            assertEquals(0, inteiro(connection,
                    "SELECT arquivado FROM parte WHERE id = 600"));
            assertEquals(5, inteiro(connection,
                    "SELECT duracao_minutos FROM programacao_parte WHERE id = 1100"));
            assertEquals("Tema histórico", texto(connection,
                    "SELECT tema FROM programacao_parte WHERE id = 1100"));

            assertEquals("2026-01-07", texto(connection,
                    "SELECT data FROM escala WHERE id = 700"));
            assertEquals(500, inteiro(connection,
                    "SELECT responsavel_id FROM designacao WHERE id = 800"));
            assertEquals("PRINCIPAL", texto(connection,
                    "SELECT sala FROM designacao WHERE id = 800"));

            assertEquals(900, inteiro(connection,
                    "SELECT id FROM historico_designacoes WHERE id = 900"));
            assertEquals("PRINCIPAL", texto(connection,
                    "SELECT sala FROM historico_designacoes WHERE id = 900"));
            assertNull(texto(connection,
                    "SELECT cantico_inicial FROM programacao_semana WHERE id = 1000"),
                    "Colunas adicionadas na migração devem iniciar sem inventar valores históricos.");

            assertColunaExiste(connection, "escala", "presidente_id");
            assertColunaExiste(connection, "escala", "dirigente_id");
            assertColunaExiste(connection, "escala", "oracao_inicial_id");
            assertColunaExiste(connection, "programacao_semana", "sala_b_habilitada");
            assertColunaExiste(connection, "programacao_semana", "cantico_inicial");
            assertColunaExiste(connection, "programacao_parte", "numero_oficial");
            assertColunaExiste(connection, "programacao_parte", "duracao_minutos");

            assertEquals(1, pragmaInteiro(connection, "PRAGMA foreign_keys"));
            assertForeignKeysValidas(connection);
        }
    }

    @Test
    void deveSerIdempotenteAoReexecutarInicializacaoSemRedefinirDados() throws Exception {
        Path arquivo = diretorioTemporario.resolve("reexecucao.db");
        criarEsquemaHistorico(arquivo, false);
        contexto = ConnectionFactory.openTestContext(arquivo);

        DatabaseInitializer.initialize();
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    UPDATE configuracoes_gerais
                    SET nome_congregacao = 'Congregação personalizada'
                    WHERE id = 1
                    """);
            statement.executeUpdate("""
                    UPDATE pessoa SET ativo = 0 WHERE id = 500
                    """);
        }

        int pessoasAntes;
        int partesAntes;
        int historicoAntes;
        try (Connection connection = ConnectionFactory.getConnection()) {
            pessoasAntes = inteiro(connection, "SELECT COUNT(*) FROM pessoa");
            partesAntes = inteiro(connection, "SELECT COUNT(*) FROM parte");
            historicoAntes = inteiro(connection, "SELECT COUNT(*) FROM historico_designacoes");
        }

        DatabaseInitializer.initialize();
        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertEquals(pessoasAntes, inteiro(connection, "SELECT COUNT(*) FROM pessoa"));
            assertEquals(partesAntes, inteiro(connection, "SELECT COUNT(*) FROM parte"));
            assertEquals(historicoAntes, inteiro(connection, "SELECT COUNT(*) FROM historico_designacoes"));
            assertEquals("Congregação personalizada", texto(connection,
                    "SELECT nome_congregacao FROM configuracoes_gerais WHERE id = 1"));
            assertEquals(0, inteiro(connection,
                    "SELECT ativo FROM pessoa WHERE id = 500"));
            assertEquals(900, inteiro(connection,
                    "SELECT id FROM historico_designacoes WHERE id = 900"));
            assertForeignKeysValidas(connection);
        }

        // Confirma que conexões distintas também recebem foreign_keys=ON.
        try (Connection primeira = ConnectionFactory.getConnection();
             Connection segunda = ConnectionFactory.getConnection()) {
            assertEquals(1, pragmaInteiro(primeira, "PRAGMA foreign_keys"));
            assertEquals(1, pragmaInteiro(segunda, "PRAGMA foreign_keys"));
        }
    }

    @Test
    void deveReverterMigracaoDeProgramacaoComDadosIncompativeisEPermitirRecuperacao()
            throws Exception {
        Path arquivo = diretorioTemporario.resolve("falha-migracao.db");
        criarEsquemaHistorico(arquivo, true);
        contexto = ConnectionFactory.openTestContext(arquivo);

        RuntimeException falha = assertThrows(
                RuntimeException.class,
                DatabaseInitializer::initialize);
        assertTrue(falha.getMessage().contains("inicializar"));

        try (Connection connection = ConnectionFactory.getConnection()) {
            assertTrue(tabelaExiste(connection, "programacao_parte"));
            assertFalse(tabelaExiste(connection, "programacao_parte_migracao_repeticao"));
            assertEquals(2, inteiro(connection, "SELECT COUNT(*) FROM programacao_parte"));
            assertEquals(600, inteiro(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 1100"));
            assertEquals(601, inteiro(connection,
                    "SELECT parte_id FROM programacao_parte WHERE id = 1101"));
        }

        // Corrige somente a estrutura de dados incompatível e tenta inicializar
        // novamente, comprovando que a tabela original continua utilizável.
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                    "UPDATE programacao_parte SET ordem = 2 WHERE id = 1101");
        }

        DatabaseInitializer.initialize();

        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            assertFalse(tabelaExiste(connection, "programacao_parte_migracao_repeticao"));
            statement.executeUpdate("""
                    INSERT INTO programacao_parte
                        (programacao_semana_id, parte_id, ordem, tema, numero_oficial, duracao_minutos)
                    VALUES (1000, 600, 3, 'Parte repetida após migração', NULL, 4)
                    """);
            assertEquals(3, inteiro(connection,
                    "SELECT COUNT(*) FROM programacao_parte WHERE programacao_semana_id = 1000"));
            assertForeignKeysValidas(connection);
        }
    }

    private void criarEsquemaHistorico(Path arquivo, boolean ordensDuplicadas)
            throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                "jdbc:sqlite:" + arquivo.toAbsolutePath());
             Statement st = connection.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");

            st.execute("""
                    CREATE TABLE pessoa (
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
                        nivel_leitura TEXT NOT NULL DEFAULT 'BASICO'
                    )
                    """);
            st.execute("""
                    CREATE TABLE parte (
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
                        possui_tema INTEGER NOT NULL DEFAULT 0
                    )
                    """);
            st.execute("""
                    CREATE TABLE escala (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        data DATE NOT NULL,
                        status TEXT NOT NULL,
                        data_geracao TEXT NOT NULL,
                        data_salvamento TEXT
                    )
                    """);
            st.execute("""
                    CREATE TABLE designacao (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        escala_id INTEGER NOT NULL,
                        parte_id INTEGER NOT NULL,
                        responsavel_id INTEGER NOT NULL,
                        ajudante_id INTEGER,
                        FOREIGN KEY (escala_id) REFERENCES escala(id) ON DELETE CASCADE,
                        FOREIGN KEY (parte_id) REFERENCES parte(id),
                        FOREIGN KEY (responsavel_id) REFERENCES pessoa(id),
                        FOREIGN KEY (ajudante_id) REFERENCES pessoa(id)
                    )
                    """);
            st.execute("""
                    CREATE TABLE historico_designacoes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        data DATE NOT NULL,
                        pessoa_id INTEGER NOT NULL,
                        parte_id INTEGER NOT NULL,
                        tipo_participacao TEXT NOT NULL,
                        UNIQUE (data, pessoa_id, parte_id, tipo_participacao),
                        FOREIGN KEY (pessoa_id) REFERENCES pessoa(id),
                        FOREIGN KEY (parte_id) REFERENCES parte(id)
                    )
                    """);
            st.execute("""
                    CREATE TABLE programacao_semana (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        data DATE NOT NULL UNIQUE
                    )
                    """);
            st.execute("""
                    CREATE TABLE programacao_parte (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        programacao_semana_id INTEGER NOT NULL,
                        parte_id INTEGER NOT NULL,
                        ordem INTEGER NOT NULL,
                        tema TEXT,
                        UNIQUE (programacao_semana_id, parte_id),
                        FOREIGN KEY (programacao_semana_id) REFERENCES programacao_semana(id) ON DELETE CASCADE,
                        FOREIGN KEY (parte_id) REFERENCES parte(id)
                    )
                    """);

            st.executeUpdate("""
                    INSERT INTO pessoa
                        (id, nome, sexo, ativo, pode_ser_responsavel, pode_ser_ajudante,
                         pode_fazer_leitura, pode_fazer_discurso, privilegio)
                    VALUES (500, 'Pessoa histórica', 'MASCULINO', 1, 1, 1, 1, 1, 'ANCIÃO')
                    """);
            st.executeUpdate("""
                    INSERT INTO parte
                        (id, nome, tipo, privilegio_minimo, exige_ajudante, sexo_permitido,
                         quantidade_minima_participantes, gera_formulario, secao, tipo_variacao, possui_tema)
                    VALUES
                        (600, 'Parte histórica A', 'DEMONSTRACAO', 'PUBLICADOR', 0, 'AMBOS', 2, 1, 'MINISTERIO', 'VARIAVEL', 1),
                        (601, 'Parte histórica B', 'DEMONSTRACAO', 'PUBLICADOR', 0, 'AMBOS', 2, 1, 'MINISTERIO', 'VARIAVEL', 1)
                    """);
            st.executeUpdate("""
                    INSERT INTO escala (id, data, status, data_geracao)
                    VALUES (700, '2026-01-07', 'SALVA', '2026-01-01T10:00:00')
                    """);
            st.executeUpdate("""
                    INSERT INTO designacao (id, escala_id, parte_id, responsavel_id)
                    VALUES (800, 700, 600, 500)
                    """);
            st.executeUpdate("""
                    INSERT INTO historico_designacoes
                        (id, data, pessoa_id, parte_id, tipo_participacao)
                    VALUES (900, '2026-01-07', 500, 600, 'RESPONSAVEL')
                    """);
            st.executeUpdate("""
                    INSERT INTO programacao_semana (id, data)
                    VALUES (1000, '2026-01-07')
                    """);

            st.executeUpdate("""
                    INSERT INTO programacao_parte
                        (id, programacao_semana_id, parte_id, ordem, tema)
                    VALUES (1100, 1000, 600, 1, 'Tema histórico')
                    """);
            if (ordensDuplicadas) {
                st.executeUpdate("""
                        INSERT INTO programacao_parte
                            (id, programacao_semana_id, parte_id, ordem, tema)
                        VALUES (1101, 1000, 601, 1, 'Tema incompatível')
                        """);
            }
        }
    }

    private static int inteiro(Connection connection, String sql) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Consulta sem resultado: " + sql);
            return rs.getInt(1);
        }
    }

    private static int pragmaInteiro(Connection connection, String sql) throws SQLException {
        return inteiro(connection, sql);
    }

    private static String texto(Connection connection, String sql) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            assertTrue(rs.next(), "Consulta sem resultado: " + sql);
            return rs.getString(1);
        }
    }

    private static boolean tabelaExiste(Connection connection, String tabela)
            throws SQLException {
        try (var ps = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=?")) {
            ps.setString(1, tabela);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void assertColunaExiste(
            Connection connection, String tabela, String coluna) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA table_info('" + tabela + "')")) {
            while (rs.next()) {
                if (coluna.equalsIgnoreCase(rs.getString("name"))) {
                    return;
                }
            }
        }
        fail("Coluna esperada ausente: " + tabela + "." + coluna);
    }

    private static void assertForeignKeysValidas(Connection connection) throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("PRAGMA foreign_key_check")) {
            assertFalse(rs.next(), () -> "Violação de chave estrangeira na tabela "
                    + rsGetTable(rs));
        }
    }

    private static String rsGetTable(ResultSet rs) {
        try {
            return rs.getString(1);
        } catch (SQLException e) {
            return "desconhecida";
        }
    }
}
