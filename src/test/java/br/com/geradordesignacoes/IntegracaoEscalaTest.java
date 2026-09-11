package br.com.geradordesignacoes;

import br.com.geradordesignacoes.controller.EscalaController;
import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.view.escala.EscalaView;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class IntegracaoEscalaTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final EscalaDAO escalaDAO = new EscalaDAO();
    private final ProgramacaoSemanaService programacaoService =
            new ProgramacaoSemanaService();

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

        } catch (IllegalStateException e) {
            /*
             * O JavaFX já pode ter sido inicializado
             * por outro teste.
             */
        }

        javafxInicializado = true;
    }


    @Test
    void deveGerarQuatroSemanasConfiguradas() throws Exception {

        criarPessoas(12);

        List<Parte> partesVariaveis =
                criarPartesVariaveis(3);

        YearMonth mes =
                YearMonth.of(2026, 9);

        List<LocalDate> datas =
                List.of(
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 9),
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 23)
                );

        for (LocalDate data : datas) {

            programacaoService.obterOuCriar(data);

            for (Parte parte : partesVariaveis) {

                programacaoService.adicionarParteVariavel(
                        data,
                        parte.getId()
                );
            }

            assertTrue(
                    programacaoService.estaConfigurada(data)
            );
        }

        List<ProgramacaoSemana> semanas =
                programacaoService.listarSemanasDoMes(mes);

        assertEquals(
                4,
                semanas.size()
        );
    }


    @Test
    void deveGerarEExibirQuatroEscalasPeloController()
            throws Exception {

        criarPessoas(12);

        List<Parte> partesVariaveis =
                criarPartesVariaveis(3);

        YearMonth mes =
                YearMonth.of(2026, 9);

        List<LocalDate> datas =
                List.of(
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 9),
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 23)
                );

        for (LocalDate data : datas) {

            programacaoService.obterOuCriar(data);

            for (Parte parte : partesVariaveis) {

                programacaoService.adicionarParteVariavel(
                        data,
                        parte.getId()
                );
            }
        }

        EscalaView view =
                criarEscalaView();

        EscalaController controller =
                obterController(view);

        boolean resultado =
                controller.gerarEscalasDoMes(mes);

        assertTrue(resultado);

        assertTrue(
                controller.possuiEscalasGeradas()
        );

        assertTrue(controller.possuiEscalasGeradas());
        assertFalse(view.getTabela().getItems().isEmpty());
    }


    @Test
    void deveSalvarAsQuatroEscalasNoBanco()
            throws Exception {

        criarPessoas(12);

        List<Parte> partesVariaveis =
                criarPartesVariaveis(3);

        YearMonth mes =
                YearMonth.of(2026, 9);

        configurarQuatroSemanas(
                partesVariaveis
        );

        EscalaView view =
                criarEscalaView();

        EscalaController controller =
                obterController(view);

        assertTrue(
                controller.gerarEscalasDoMes(mes)
        );

        assertTrue(
                controller.salvarEscalasGeradas()
        );

        List<Escala> escalas =
                escalaDAO.listarTodas();

        assertEquals(
                4,
                escalas.size()
        );

        assertEquals(
                List.of(
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 9),
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 23)
                ),
                escalas.stream()
                        .map(Escala::getData)
                        .sorted()
                        .toList()
        );
    }


    @Test
    void deveRecuperarEscalasSalvas()
            throws Exception {

        criarPessoas(12);

        List<Parte> partes =
                criarPartesVariaveis(3);

        configurarQuatroSemanas(partes);

        EscalaView view =
                criarEscalaView();

        EscalaController controller =
                obterController(view);

        YearMonth mes =
                YearMonth.of(2026, 9);

        assertTrue(
                controller.gerarEscalasDoMes(mes)
        );

        assertTrue(
                controller.salvarEscalasGeradas()
        );

        List<Escala> recuperadas =
                escalaDAO.listarTodas();

        assertEquals(
                4,
                recuperadas.size()
        );

        for (Escala escala : recuperadas) {

            assertNotNull(
                    escala.getId()
            );

            assertNotNull(
                    escala.getData()
            );

            assertFalse(
                    escala.getDesignacoes().isEmpty()
            );
        }
    }


    @Test
    void devePermitirEditarDesignacaoAntesDeSalvar()
            throws Exception {

        Pessoa pessoa1 =
                criarPessoa(
                        "Pessoa 1"
                );

        Pessoa pessoa2 =
                criarPessoa(
                        "Pessoa 2"
                );

        Parte parte =
                criarParteVariavel(
                        "Parte 1"
                );

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        ResultadoGeracaoEscala resultado =
                gerador.gerarEscala(
                        LocalDate.of(2026, 9, 2),
                        List.of(parte),
                        List.of(pessoa1, pessoa2)
                );

        assertTrue(
                resultado.erros().isEmpty()
        );

        Escala escala =
                resultado.escala();

        assertEquals(
                1,
                escala.getDesignacoes().size()
        );

        Designacao original =
                escala.getDesignacoes().get(0);

        Designacao nova =
                new Designacao(
                        original.data(),
                        original.parte(),
                        pessoa2,
                        null
                );

        escala.substituirDesignacao(
                0,
                nova
        );

        assertEquals(
                pessoa2.getId(),
                escala.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }


    @Test
    void deveRegenerarUmaSemanaSemAlterarAsOutras()
            throws Exception {

        List<Pessoa> pessoas =
                criarPessoas(12);

        List<Parte> partes =
                criarPartesVariaveis(3);

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        LocalDate semana1 =
                LocalDate.of(2026, 9, 2);

        LocalDate semana2 =
                LocalDate.of(2026, 9, 9);

        LocalDate semana3 =
                LocalDate.of(2026, 9, 16);

        LocalDate semana4 =
                LocalDate.of(2026, 9, 23);

        List<LocalDate> datas =
                List.of(
                        semana1,
                        semana2,
                        semana3,
                        semana4
                );

        for (LocalDate data : datas) {

            ResultadoGeracaoEscala resultado =
                    gerador.gerarEscala(
                            data,
                            partes,
                            pessoas
                    );

            assertTrue(
                    resultado.erros().isEmpty()
            );

            escalaDAO.salvar(
                    resultado.escala()
            );
        }

        List<Escala> antes =
                escalaDAO.listarTodas();

        assertEquals(
                4,
                antes.size()
        );

        List<Integer> idsAntes =
                antes.stream()
                        .filter(
                                escala ->
                                        !escala.getData()
                                                .equals(semana2)
                        )
                        .map(Escala::getId)
                        .sorted()
                        .toList();

        /*
         * Regenera somente a semana 2.
         */
        ResultadoGeracaoEscala novaSemana =
                gerador.gerarEscala(
                        semana2,
                        partes,
                        pessoas
                );

        assertTrue(
                novaSemana.erros().isEmpty()
        );

        escalaDAO.salvar(
                novaSemana.escala()
        );

        List<Escala> depois =
                escalaDAO.listarTodas();

        assertEquals(
                4,
                depois.size()
        );

        List<Integer> idsDepois =
                depois.stream()
                        .filter(
                                escala ->
                                        !escala.getData()
                                                .equals(semana2)
                        )
                        .map(Escala::getId)
                        .sorted()
                        .toList();

        assertEquals(
                idsAntes,
                idsDepois,
                "As outras três semanas não deveriam ser alteradas."
        );

        assertTrue(
                depois.stream()
                        .anyMatch(
                                escala ->
                                        escala.getData()
                                                .equals(semana2)
                        )
        );
    }


    @Test
    void deveManterHistoricoDasQuatroGeracoes()
            throws Exception {

        List<Pessoa> pessoas =
                criarPessoas(12);

        List<Parte> partes =
                criarPartesVariaveis(3);

        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );

        List<LocalDate> datas =
                List.of(
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 9),
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 23)
                );

        for (LocalDate data : datas) {

            ResultadoGeracaoEscala resultado =
                    gerador.gerarEscala(
                            data,
                            partes,
                            pessoas
                    );

            assertTrue(
                    resultado.erros().isEmpty()
            );
        }

        /*
         * O GeradorEscala registra automaticamente
         * as participações no histórico.
         */
        br.com.geradordesignacoes.service.HistoricoDesignacoesService
                historicoService =
                new br.com.geradordesignacoes.service.HistoricoDesignacoesService();

        var participacoes =
                historicoService.getHistorico()
                        .participacoes();

        assertFalse(
                participacoes.isEmpty()
        );

        for (LocalDate data : datas) {

            assertTrue(
                    participacoes.stream()
                            .anyMatch(
                                    participacao ->
                                            participacao.data()
                                                    .equals(data)
                            ),
                    "Não existe histórico para "
                            + data
            );
        }
    }


    private void configurarQuatroSemanas(
            List<Parte> partesVariaveis
    ) {

        List<LocalDate> datas =
                List.of(
                        LocalDate.of(2026, 9, 2),
                        LocalDate.of(2026, 9, 9),
                        LocalDate.of(2026, 9, 16),
                        LocalDate.of(2026, 9, 23)
                );

        for (LocalDate data : datas) {

            programacaoService.obterOuCriar(data);

            for (Parte parte : partesVariaveis) {

                programacaoService.adicionarParteVariavel(
                        data,
                        parte.getId()
                );
            }
        }
    }


    private EscalaView criarEscalaView()
            throws Exception {

        CountDownLatch latch =
                new CountDownLatch(1);

        final EscalaView[] resultado =
                new EscalaView[1];

        Platform.runLater(
                () -> {

                    resultado[0] =
                            new EscalaView();

                    latch.countDown();
                }
        );

        assertTrue(
                latch.await(
                        10,
                        TimeUnit.SECONDS
                ),
                "Não foi possível criar a EscalaView."
        );

        return resultado[0];
    }


    private EscalaController obterController(
            EscalaView view
    ) {
        return view.getController();
    }


    private Pessoa criarPessoa(
            String nome
    ) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        true,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                );

        return pessoaDAO.salvar(pessoa);
    }


    private List<Pessoa> criarPessoas(
            int quantidade
    ) {

        List<Pessoa> pessoas =
                new ArrayList<>();

        for (int i = 1; i <= quantidade; i++) {

            pessoas.add(
                    criarPessoa(
                            "Pessoa " + i
                    )
            );
        }

        return pessoas;
    }


    private Parte criarParteVariavel(
            String nome
    ) {

        Parte parte =
                new Parte(
                        nome,
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        SecaoParte.MINISTERIO,
                        TipoVariacaoParte.VARIAVEL,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL
                        )
                );

        return parteDAO.salvar(parte);
    }


    private List<Parte> criarPartesVariaveis(
            int quantidade
    ) {

        List<Parte> partes =
                new ArrayList<>();

        for (int i = 1; i <= quantidade; i++) {

            partes.add(
                    criarParteVariavel(
                            "Parte " + i
                    )
            );
        }

        return partes;
    }

}