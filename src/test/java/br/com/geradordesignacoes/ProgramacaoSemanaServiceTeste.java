package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.ConfiguracaoSalasDAO;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramacaoSemanaServiceTest extends BaseDAOTest {

    private ProgramacaoSemanaService service;

    private final ConfiguracaoSalasDAO configuracaoSalasDAO =
            new ConfiguracaoSalasDAO();


    @BeforeEach
    void prepararServico() {
        service =
                new ProgramacaoSemanaService();
    }


    @Test
    void deveRecuperarProgramacaoExistente() {

        LocalDate data =
                LocalDate.of(2026, 8, 26);


        ProgramacaoSemana primeira =
                service.obterOuCriar(data);


        ProgramacaoSemana segunda =
                service.obterOuCriar(data);


        assertNotNull(primeira);
        assertNotNull(segunda);


        assertEquals(
                primeira.id(),
                segunda.id()
        );


        assertEquals(
                primeira.data(),
                segunda.data()
        );


        assertEquals(
                primeira.partes().size(),
                segunda.partes().size()
        );
    }

    @Test
    void deveCopiarConfiguracaoGlobalAoCriarSemana() {
        configuracaoSalasDAO.salvar(new ConfiguracaoSalas(true, true));

        LocalDate data = LocalDate.of(2026, 11, 1);
        ProgramacaoSemana criada = service.obterOuCriar(data);

        configuracaoSalasDAO.salvar(new ConfiguracaoSalas(false, false));

        ProgramacaoSemana recarregada = service.obterOuCriar(data);

        assertTrue(criada.salaBHabilitada());
        assertTrue(criada.salaCHabilitada());
        assertTrue(recarregada.salaBHabilitada());
        assertTrue(recarregada.salaCHabilitada());
    }


    @Test
    void partesDevemSerCriadasSemTemaInicialmente() {

        LocalDate data =
                LocalDate.of(2026, 8, 27);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        for (
                ProgramacaoParte parte :
                programacao.partes()
        ) {

            assertFalse(
                    parte.possuiTema()
            );
        }
    }

    @Test
    void deveManterTemasDaMesmaParteIsoladosEntreSemanas() {

        LocalDate semanaA =
                LocalDate.of(2026, 12, 2);

        LocalDate semanaB =
                LocalDate.of(2026, 12, 9);

        ProgramacaoSemana programacaoA =
                service.obterOuCriar(semanaA);

        ProgramacaoSemana programacaoB =
                service.obterOuCriar(semanaB);

        ProgramacaoParte parteA =
                programacaoA.partes()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        ProgramacaoParte parteB =
                programacaoB.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteA.getParte()
                                                                .getId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                parteA.getParte().getId(),
                parteB.getParte().getId()
        );

        service.definirTema(
                semanaA,
                parteA.getOrdem(),
                "Tema da primeira semana"
        );

        service.definirTema(
                semanaB,
                parteB.getOrdem(),
                "Tema da segunda semana"
        );

        ProgramacaoSemana recarregadaA =
                service.obterOuCriar(semanaA);

        ProgramacaoSemana recarregadaB =
                service.obterOuCriar(semanaB);

        assertEquals(
                "Tema da primeira semana",
                recarregadaA.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteA.getParte()
                                                                .getId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow()
                        .getTema()
        );

        assertEquals(
                "Tema da segunda semana",
                recarregadaB.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteA.getParte()
                                                                .getId()
                                                )
                        )
                        .findFirst()
                        .orElseThrow()
                        .getTema()
        );
    }

    @Test
    void devePreservarTemaNormalAoSalvar() {

        LocalDate data =
                LocalDate.of(2027, 1, 6);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        int ordem =
                programacao.partes()
                        .get(0)
                        .getOrdem();

        service.definirTema(
                data,
                ordem,
                "Tema normal"
        );

        assertEquals(
                "Tema normal",
                service.obterOuCriar(data)
                        .partes()
                        .get(0)
                        .getTema()
        );
    }

    @Test
    void deveRemoverEspacosNasExtremidadesDoTema() {

        LocalDate data =
                LocalDate.of(2027, 1, 13);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        service.definirTema(
                data,
                programacao.partes().get(0).getOrdem(),
                "  Tema  com  espacamento  "
        );

        assertEquals(
                "Tema  com  espacamento",
                service.obterOuCriar(data)
                        .partes()
                        .get(0)
                        .getTema()
        );
    }

    @Test
    void deveNormalizarTemaVazioParaAusencia() {

        LocalDate data =
                LocalDate.of(2027, 1, 20);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        service.definirTema(
                data,
                programacao.partes().get(0).getOrdem(),
                ""
        );

        assertNull(
                service.obterOuCriar(data)
                        .partes()
                        .get(0)
                        .getTema()
        );
    }

    @Test
    void deveNormalizarTemaComApenasEspacosParaAusencia() {

        LocalDate data =
                LocalDate.of(2027, 1, 27);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        service.definirTema(
                data,
                programacao.partes().get(0).getOrdem(),
                "   "
        );

        assertNull(
                service.obterOuCriar(data)
                        .partes()
                        .get(0)
                        .getTema()
        );
    }


    @Test
    void deveRejeitarDataNula() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.obterOuCriar(null)
        );
    }




    @Test
    void deveImpedirAdicionarParteComIdNulo() {

        LocalDate data =
                LocalDate.of(2026, 9, 10);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                null
                        )
        );
    }




    @Test
    void naoDevePermitirRemoverParteComIdInexistente() {

        LocalDate data =
                LocalDate.of(2026, 9, 12);

        service.obterOuCriar(data);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                data,
                                Integer.MAX_VALUE
                        )
        );
    }
    @Test
    void deveRetornarFalseQuandoPossuirMenosDeTresPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 13);

        assertFalse(
                service.estaConfigurada(data)
        );
    }


    @Test
    void deveRetornarFalseQuandoNaoExistirProgramacao() {

        LocalDate data =
                LocalDate.of(2026, 9, 16);

        assertFalse(
                service.estaConfigurada(data)
        );
    }
    @Test
    void naoDevePermitirAdicionarParteComIdNulo() {

        LocalDate data =
                LocalDate.of(2026, 9, 17);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                null
                        )
        );
    }
    @Test
    void naoDevePermitirAdicionarParteComDataNula() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                null,
                                1
                        )
        );
    }
    @Test
    void naoDevePermitirRemoverParteComDataNula() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                null,
                                1
                        )
        );
    }
    @Test
    void naoDevePermitirRemoverParteComIdNulo() {

        LocalDate data =
                LocalDate.of(2026, 9, 18);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                data,
                                null
                        )
        );
    }

    @Test
    void deveCopiarDuracaoPadraoEManterOcorrenciasExistentesIndependentes() {
        ParteDAO parteDAO = new ParteDAO();
        Parte parte = service.listarTodas().stream()
                .filter(p -> p.getTipoVariacao() == TipoVariacaoParte.FIXA)
                .findFirst().orElseThrow();
        parte.setDuracaoPadraoMinutos(12);
        parteDAO.atualizar(parte);

        LocalDate semanaAntiga = LocalDate.of(2027, 3, 3);
        ProgramacaoSemana criada = service.obterOuCriar(semanaAntiga);
        ProgramacaoParte ocorrenciaAntiga = criada.partes().stream()
                .filter(p -> p.getParte().getId().equals(parte.getId()))
                .findFirst().orElseThrow();
        assertEquals(12, ocorrenciaAntiga.getDuracaoMinutos());

        parte.setDuracaoPadraoMinutos(18);
        parteDAO.atualizar(parte);
        assertEquals(12, service.obterOuCriar(semanaAntiga).partes().stream()
                .filter(p -> p.getParte().getId().equals(parte.getId()))
                .findFirst().orElseThrow().getDuracaoMinutos());

        LocalDate semanaNova = LocalDate.of(2027, 3, 10);
        assertEquals(18, service.obterOuCriar(semanaNova).partes().stream()
                .filter(p -> p.getParte().getId().equals(parte.getId()))
                .findFirst().orElseThrow().getDuracaoMinutos());

        service.definirDuracaoMinutos(semanaAntiga, ocorrenciaAntiga.getOrdem(), 20);
        assertEquals(20, service.obterOuCriar(semanaAntiga).partes().stream()
                .filter(p -> p.getParte().getId().equals(parte.getId()))
                .findFirst().orElseThrow().getDuracaoMinutos());
        assertEquals(18, service.obterOuCriar(semanaNova).partes().stream()
                .filter(p -> p.getParte().getId().equals(parte.getId()))
                .findFirst().orElseThrow().getDuracaoMinutos());
    }

    @Test
    void deveRejeitarDuracaoSemanalNaoPositiva() {
        ProgramacaoSemana programacao = service.obterOuCriar(LocalDate.of(2027, 4, 7));
        assertThrows(IllegalArgumentException.class, () ->
                service.definirDuracaoMinutos(
                        programacao.data(),
                        programacao.partes().getFirst().getOrdem(),
                        0
                ));
    }

    @Test
    void deveDefinirNumeroTresParaLeituraEPreservarNumeroEditado() {

        LocalDate data =
                LocalDate.of(2026, 11, 4);

        ProgramacaoSemana criada =
                service.obterOuCriar(data);

        ProgramacaoParte leitura =
                criada.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte().getTipo()
                                                == TipoParte.LEITURA
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                3,
                leitura.getNumeroOficial()
        );

        service.definirNumeroOficial(
                data,
                leitura.getOrdem(),
                8
        );

        ProgramacaoSemana recarregada =
                service.obterOuCriar(data);

        assertEquals(
                8,
                recarregada.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte().getTipo()
                                                == TipoParte.LEITURA
                        )
                        .findFirst()
                        .orElseThrow()
                        .getNumeroOficial()
        );
    }


}