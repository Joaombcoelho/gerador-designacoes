package br.com.geradordesignacoes.database;

import br.com.geradordesignacoes.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
            pode_ser_presidente INTEGER NOT NULL DEFAULT 0,
            pode_ser_dirigente INTEGER NOT NULL DEFAULT 0,
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
            nivel_leitura_minimo TEXT NOT NULL DEFAULT 'BASICO',
            secao TEXT,
            tipo_variacao TEXT,
            possui_tema INTEGER NOT NULL DEFAULT 0
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
            responsavel_id INTEGER NOT NULL,
            ajudante_id INTEGER,

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
            data DATE NOT NULL UNIQUE
        );

        """;


    private static final String CREATE_TABLE_PROGRAMACAO_PARTE = """

        CREATE TABLE IF NOT EXISTS programacao_parte (
            id INTEGER PRIMARY KEY AUTOINCREMENT,

            programacao_semana_id INTEGER NOT NULL,
            parte_id INTEGER NOT NULL,

            ordem INTEGER NOT NULL,
            tema TEXT,

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


            /*
             * ----------------------------------------------------
             * PARTE
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PARTE
            );


            /*
             * ----------------------------------------------------
             * PARTICIPAÇÕES NECESSÁRIAS
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PARTE_PARTICIPACAO_NECESSARIA
            );


            /*
             * ----------------------------------------------------
             * HISTÓRICO
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_HISTORICO_DESIGNACOES
            );


            /*
             * ----------------------------------------------------
             * ESCALA
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_ESCALA
            );


            /*
             * ----------------------------------------------------
             * DESIGNAÇÃO
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_DESIGNACAO
            );


            /*
             * ----------------------------------------------------
             * PROGRAMAÇÃO SEMANAL
             * ----------------------------------------------------
             */

            statement.execute(
                    CREATE_TABLE_PROGRAMACAO_SEMANA
            );


            statement.execute(
                    CREATE_TABLE_PROGRAMACAO_PARTE
            );


            /*
             * ----------------------------------------------------
             * DADOS INICIAIS
             * ----------------------------------------------------
             */

            cadastrarPartesIniciais(
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


    /*
     * ============================================================
     * PARTES INICIAIS
     * ============================================================
     */

    private static void cadastrarPartesIniciais(
            Connection connection
    ) throws SQLException {

        String sqlVerificar = """

            SELECT id
            FROM parte
            WHERE tipo = ?

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

                    TipoParticipacao.DIRIGENTE,
                    TipoParticipacao.LEITOR
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
}