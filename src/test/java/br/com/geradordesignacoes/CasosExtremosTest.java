package br.com.geradordesignacoes;

import br.com.geradordesignacoes.controller.EscalaController;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.view.escala.EscalaView;
import javafx.application.Platform;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CasosExtremosTest extends BaseDAOTest {

    private PessoaDAO pessoaDAO;
    private ParteDAO parteDAO;

    private static boolean javafxInicializado = false;

    @BeforeAll
    static void inicializarJavaFX() throws Exception {

        if (javafxInicializado) {
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);

        try {

            Platform.startup(latch::countDown);

            assertTrue(
                    latch.await(10, TimeUnit.SECONDS),
                    "JavaFX não foi inicializado."
            );

        } catch (IllegalStateException ignored) {
            // JavaFX já foi inicializado por outro teste.
        }

        javafxInicializado = true;
    }

    @BeforeEach
    void preparar() {

        pessoaDAO = new PessoaDAO();
        parteDAO = new ParteDAO();
    }


    // ============================================================
    // 1. PESSOA INATIVA NÃO PODE PRESIDIR
    // ============================================================

    @Test
    void pessoaInativaNaoPodePresidir() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Inativa",
                Sexo.MASCULINO,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.ANCIAO,
                NivelLeitura.EXPERIENTE
        );

        RegrasService regras = new RegrasService();

        assertFalse(
                regras.podePresidirReuniao(pessoa),
                "Pessoa inativa não pode presidir a reunião."
        );
    }

    // ============================================================
    // 2. PARTE SEM CANDIDATO COMPATÍVEL
    // ============================================================

    @Test
    void parteSemCandidatoCompativelDeveSerRejeitada() {

        Pessoa mulher = new Pessoa(
                "Maria",
                Sexo.FEMININO,
                true,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        Parte parte = new Parte(
                "Parte masculina",
                TipoParte.DISCURSO,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoVariacaoParte.FIXA,
                false,
                List.of(TipoParticipacao.ORADOR)
        );

        RegrasService regras = new RegrasService();

        assertFalse(
                regras.podeDesignar(mulher, parte, List.of(mulher)),
                "Uma pessoa incompatível com a parte não deveria ser aceita."
        );
    }

    // ============================================================
    // 3. PARTE QUE EXIGE AJUDANTE
    // ============================================================

    @Test
    void parteQueExigeAjudanteNaoAceitaDesignacaoSemAjudante() {

        Pessoa responsavel = criarPessoa(
                "Responsável",
                Sexo.MASCULINO
        );

        Parte parte = new Parte(
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                Privilegio.PUBLICADOR,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.FIXA,
                false,
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Designacao(
                        java.time.LocalDate.of(2026, 9, 3),
                        parte,
                        responsavel,
                        null
                )
        );
    }

    // ============================================================
    // 4. MENOS DE 6 PARTES VARIÁVEIS
    // ============================================================

    @Test
    void programacaoComMenosDeTresPartesVariaveisDeveSerRejeitada() {

        ProgramacaoSemanaService service =
                new ProgramacaoSemanaService();

        Parte parte1 = criarParteVariavel("Parte variável 1");
        Parte parte2 = criarParteVariavel("Parte variável 2");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.montarProgramacao(
                        java.time.LocalDate.of(2026, 9, 3),
                        List.of(parte1, parte2)
                )
        );
    }

    // ============================================================
    // 5. MAIS DE 6 PARTES VARIÁVEIS
    // ============================================================

    @Test
    void programacaoComMaisDeSeisPartesVariaveisDeveSerRejeitada() {

        ProgramacaoSemanaService service =
                new ProgramacaoSemanaService();

        List<Parte> partes = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            partes.add(
                    criarParteVariavel(
                            "Parte variável " + i
                    )
            );
        }

        assertThrows(
                IllegalArgumentException.class,
                () -> service.montarProgramacao(
                        java.time.LocalDate.of(2026, 9, 3),
                        partes
                )
        );
    }

    // ============================================================
    // 6. PARTE VARIÁVEL DUPLICADA
    // ============================================================

    @Test
    void programacaoComParteVariavelDuplicadaDeveSerRejeitada() {

        ProgramacaoSemanaService service =
                new ProgramacaoSemanaService();

        Parte parte1 = criarParteVariavel("Parte variável 1");
        Parte parte2 = criarParteVariavel("Parte variável 2");
        Parte parte3 = criarParteVariavel("Parte variável 3");

        assertThrows(
                IllegalArgumentException.class,
                () -> service.montarProgramacao(
                        java.time.LocalDate.of(2026, 9, 3),
                        List.of(
                                parte1,
                                parte2,
                                parte3,
                                parte1
                        )
                )
        );
    }

    // ============================================================
    // 7. CONTROLLER SEM MÊS
    // ============================================================

    @Test
    void controllerNaoDeveGerarSemMes() throws Exception {

        EscalaView view = criarEscalaView();

        EscalaController controller =
                view.getController();

        assertFalse(
                controller.gerarEscalasDoMes(null),
                "Não deveria ser possível gerar uma escala sem mês."
        );
    }

    // ============================================================
    // 8. PARTE QUE EXIGE AJUDANTE SEM AJUDANTE
    // ============================================================

    @Test
    void parteComAjudanteObrigatorioDeveRejeitarResponsavelSemAjudante() {

        Pessoa responsavel = criarPessoa(
                "Responsável",
                Sexo.MASCULINO
        );

        Parte parte = new Parte(
                "Demonstração obrigatória",
                TipoParte.DEMONSTRACAO,
                Privilegio.PUBLICADOR,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL,
                false,
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Designacao(
                        java.time.LocalDate.of(2026, 9, 10),
                        parte,
                        responsavel,
                        null
                )
        );
    }

    // ============================================================
    // AUXILIARES
    // ============================================================

    private Pessoa criarPessoa(
            String nome,
            Sexo sexo
    ) {

        Pessoa pessoa = new Pessoa(
                nome,
                sexo,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.ANCIAO,
                NivelLeitura.EXPERIENTE
        );

        pessoaDAO.salvar(pessoa);

        return pessoa;
    }

    private Parte criarParteVariavel(
            String nome
    ) {

        Parte parte = new Parte(
                nome,
                TipoParte.DISCURSO,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL,
                false,
                List.of(TipoParticipacao.ORADOR)
        );

        parteDAO.salvar(parte);

        return parte;
    }

    private EscalaView criarEscalaView()
            throws Exception {

        CountDownLatch latch =
                new CountDownLatch(1);

        final EscalaView[] resultado =
                new EscalaView[1];

        Platform.runLater(() -> {

            resultado[0] =
                    new EscalaView();

            latch.countDown();
        });

        assertTrue(
                latch.await(
                        10,
                        TimeUnit.SECONDS
                ),
                "Não foi possível criar a EscalaView."
        );

        return resultado[0];
    }
}
