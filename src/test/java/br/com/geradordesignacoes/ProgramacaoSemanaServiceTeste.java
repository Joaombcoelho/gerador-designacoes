package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramacaoSemanaServiceTest {

    private ProgramacaoSemanaService service;


    @BeforeEach
    void prepararBanco() {

        service =
                new ProgramacaoSemanaService();
    }

    @Test
    void deveCriarProgramacaoParaNovaData() {

        LocalDate data =
                LocalDate.of(2026, 8, 24);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        assertNotNull(programacao);

        assertEquals(
                data,
                programacao.getData()
        );

        assertNotNull(
                programacao.getId()
        );

        assertFalse(
                programacao.getPartes().isEmpty()
        );
    }


    @Test
    void deveCriarPartesNaOrdemCorreta() {

        LocalDate data =
                LocalDate.of(2026, 8, 25);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        List<ProgramacaoParte> partes =
                programacao.getPartes();


        assertFalse(partes.isEmpty());


        for (int i = 0; i < partes.size(); i++) {

            assertEquals(
                    i + 1,
                    partes.get(i).getOrdem()
            );
        }


        assertEquals(
                "Presidente",
                partes.get(0)
                        .getParte()
                        .getNome()
        );


        assertEquals(
                "Oração inicial",
                partes.get(1)
                        .getParte()
                        .getNome()
        );


        assertEquals(
                "Discurso — Tesouros",
                partes.get(2)
                        .getParte()
                        .getNome()
        );


        assertEquals(
                "Oração final",
                partes.get(partes.size() - 1)
                        .getParte()
                        .getNome()
        );
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
                primeira.getId(),
                segunda.getId()
        );


        assertEquals(
                primeira.getData(),
                segunda.getData()
        );


        assertEquals(
                primeira.getPartes().size(),
                segunda.getPartes().size()
        );
    }


    @Test
    void partesDevemSerCriadasSemTemaInicialmente() {

        LocalDate data =
                LocalDate.of(2026, 8, 27);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        for (
                ProgramacaoParte parte :
                programacao.getPartes()
        ) {

            assertFalse(
                    parte.possuiTema()
            );
        }
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
    void deveDefinirTemaDaParte() {

        LocalDate data =
                LocalDate.of(2026, 8, 28);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        service.definirTema(
                data,
                3,
                "Tesouros da Palavra de Deus"
        );


        ProgramacaoSemana atualizada =
                service.obterOuCriar(data);


        ProgramacaoParte parte =
                atualizada.getPartes()
                        .stream()
                        .filter(
                                p ->
                                        p.getOrdem() == 3
                        )
                        .findFirst()
                        .orElseThrow();


        assertEquals(
                "Tesouros da Palavra de Deus",
                parte.getTema()
        );
    }


    @Test
    void deveAlterarTemaDaParte() {

        LocalDate data =
                LocalDate.of(2026, 8, 29);


        service.obterOuCriar(data);


        service.definirTema(
                data,
                3,
                "Primeiro tema"
        );


        service.definirTema(
                data,
                3,
                "Segundo tema"
        );


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        ProgramacaoParte parte =
                programacao.getPartes()
                        .stream()
                        .filter(
                                p ->
                                        p.getOrdem() == 3
                        )
                        .findFirst()
                        .orElseThrow();


        assertEquals(
                "Segundo tema",
                parte.getTema()
        );
    }
    @Test
    void deveAdicionarParteVariavelNaSemana() {

        LocalDate data =
                LocalDate.of(2026, 9, 1);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        assertTrue(
                programacao.getPartesVariaveis()
                        .isEmpty()
        );


        ParteDAO parteDAO =
                new ParteDAO();


        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Nenhuma parte variável cadastrada."
                                        )
                        );


        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );


        ProgramacaoSemana atualizada =
                service.obterOuCriar(data);


        assertTrue(
                atualizada.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getId()
                                                .equals(parteVariavel.getId())
                        )
        );
    }

    @Test
    void deveRemoverParteVariavelDaSemana() {

        LocalDate data =
                LocalDate.of(2026, 9, 2);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        ParteDAO parteDAO =
                new ParteDAO();


        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Nenhuma parte variável cadastrada."
                                        )
                        );


        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );


        ProgramacaoSemana antesDaRemocao =
                service.obterOuCriar(data);


        assertTrue(
                antesDaRemocao.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );


        service.removerParteVariavel(
                data,
                parteVariavel.getId()
        );


        ProgramacaoSemana depoisDaRemocao =
                service.obterOuCriar(data);


        assertFalse(
                depoisDaRemocao.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );
    }
    @Test
    void deveImpedirMaisDeSeisPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 3);


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        ParteDAO parteDAO =
                new ParteDAO();


        List<Parte> partesVariaveis =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .limit(7)
                        .toList();


        assertTrue(
                partesVariaveis.size() >= 7
        );


        for (int i = 0; i < 6; i++) {

            service.adicionarParteVariavel(
                    data,
                    partesVariaveis.get(i).getId()
            );
        }


        assertEquals(
                6,
                service.obterOuCriar(data)
                        .getPartesVariaveis()
                        .size()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                partesVariaveis.get(6).getId()
                        )
        );
    }

    @Test
    void deveImpedirAdicionarParteFixaComoVariavel() {

        LocalDate data =
                LocalDate.of(2026, 9, 4);


        service.obterOuCriar(data);


        ParteDAO parteDAO =
                new ParteDAO();


        Parte parteFixa =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Nenhuma parte fixa cadastrada."
                                        )
                        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                parteFixa.getId()
                        )
        );
    }

    @Test
    void deveImpedirAdicionarParteVariavelDuplicada() {

        LocalDate data =
                LocalDate.of(2026, 9, 5);


        service.obterOuCriar(data);


        ParteDAO parteDAO =
                new ParteDAO();


        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Nenhuma parte variável cadastrada."
                                        )
                        );


        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                parteVariavel.getId()
                        )
        );
    }
    @Test
    void deveImpedirRemoverParteFixa() {

        LocalDate data =
                LocalDate.of(2026, 9, 6);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        Parte parteFixa =
                programacao.getPartes()
                        .stream()
                        .map(ProgramacaoParte::getParte)
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA
                        )
                        .findFirst()
                        .orElseThrow();


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                data,
                                parteFixa.getId()
                        )
        );
    }
    @Test
    void deveImpedirRemoverParteNaoConfigurada() {

        LocalDate data =
                LocalDate.of(2026, 9, 7);

        service.obterOuCriar(data);

        ParteDAO parteDAO =
                new ParteDAO();

        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                data,
                                parteVariavel.getId()
                        )
        );
    }
    @Test
    void deveRetornarFalsoQuandoProgramacaoPossuiMenosDeTresPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 8);

        service.obterOuCriar(data);

        ParteDAO parteDAO =
                new ParteDAO();

        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow();

        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );

        assertFalse(
                service.estaConfigurada(data)
        );
    }
    @Test
    void deveRetornarVerdadeiroComTresPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 9);

        service.obterOuCriar(data);

        ParteDAO parteDAO =
                new ParteDAO();

        List<Parte> partesVariaveis =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .limit(3)
                        .toList();

        assertEquals(
                3,
                partesVariaveis.size()
        );

        for (Parte parte : partesVariaveis) {

            service.adicionarParteVariavel(
                    data,
                    parte.getId()
            );
        }

        assertTrue(
                service.estaConfigurada(data)
        );
    }
    @Test
    void deveImpedirAdicionarParteComDataNula() {

        ParteDAO parteDAO =
                new ParteDAO();

        Parte parteVariavel =
                parteDAO.listarTodos()
                        .stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.VARIAVEL
                        )
                        .findFirst()
                        .orElseThrow();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                null,
                                parteVariavel.getId()
                        )
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
    void naoDevePermitirAdicionarParteVariavelDuplicada() {

        LocalDate data =
                LocalDate.of(2026, 9, 22);

        Parte parteVariavel =
                service.listarPartesVariaveis()
                        .stream()
                        .findFirst()
                        .orElseThrow();


        ProgramacaoSemana programacao =
                service.obterOuCriar(data);


        boolean jaExiste =
                programacao.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        );


        if (jaExiste) {

            service.removerParteVariavel(
                    data,
                    parteVariavel.getId()
            );
        }


        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                parteVariavel.getId()
                        )
        );
    }
    @Test
    void naoDevePermitirAdicionarParteFixa() {

        LocalDate data =
                LocalDate.of(2026, 9, 9);

        ProgramacaoSemana programacao =
                service.obterOuCriar(data);

        Parte parteFixa =
                programacao.getPartes()
                        .stream()
                        .map(ProgramacaoParte::getParte)
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA
                        )
                        .findFirst()
                        .orElseThrow();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                parteFixa.getId()
                        )
        );
    }
    @Test
    void naoDevePermitirMaisDeSeisPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 10);

        List<Parte> partesVariaveis =
                service.listarPartesVariaveis();

        assertTrue(
                partesVariaveis.size() >= 7,
                "É necessário possuir pelo menos 7 partes variáveis cadastradas."
        );

        for (int i = 0; i < 6; i++) {

            service.adicionarParteVariavel(
                    data,
                    partesVariaveis.get(i).getId()
            );
        }

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.adicionarParteVariavel(
                                data,
                                partesVariaveis.get(6).getId()
                        )
        );
    }
    @Test
    void naoDevePermitirRemoverParteQueNaoEstaNaProgramacao() {

        LocalDate data =
                LocalDate.of(2026, 9, 11);

        Parte parteVariavel =
                service.listarPartesVariaveis()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        service.obterOuCriar(data);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.removerParteVariavel(
                                data,
                                parteVariavel.getId()
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
    void deveRetornarTrueComExatamenteTresPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 14);

        List<Parte> partesVariaveis =
                service.listarPartesVariaveis();

        for (int i = 0; i < 3; i++) {

            service.adicionarParteVariavel(
                    data,
                    partesVariaveis.get(i).getId()
            );
        }

        assertTrue(
                service.estaConfigurada(data)
        );
    }
    @Test
    void deveRetornarTrueComExatamenteSeisPartesVariaveis() {

        LocalDate data =
                LocalDate.of(2026, 9, 15);

        List<Parte> partesVariaveis =
                service.listarPartesVariaveis();

        assertTrue(
                partesVariaveis.size() >= 6,
                "É necessário possuir pelo menos 6 partes variáveis cadastradas."
        );

        for (int i = 0; i < 6; i++) {

            service.adicionarParteVariavel(
                    data,
                    partesVariaveis.get(i).getId()
            );
        }

        assertTrue(
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
    void deveRemoverParteVariavel() {

        LocalDate data =
                LocalDate.of(2026, 9, 19);

        Parte parteVariavel =
                service.listarPartesVariaveis()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );

        ProgramacaoSemana antes =
                service.obterOuCriar(data);

        assertTrue(
                antes.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );

        service.removerParteVariavel(
                data,
                parteVariavel.getId()
        );

        ProgramacaoSemana depois =
                service.obterOuCriar(data);

        assertFalse(
                depois.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );
    }
    @Test
    void devePersistirParteVariavelAdicionada() {

        LocalDate data =
                LocalDate.of(2026, 9, 20);

        Parte parteVariavel =
                service.listarPartesVariaveis()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );

        ProgramacaoSemana novaInstancia =
                new ProgramacaoSemanaService()
                        .obterOuCriar(data);

        assertTrue(
                novaInstancia.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );
    }
    @Test
    void devePersistirRemocaoDaParteVariavel() {

        LocalDate data =
                LocalDate.of(2026, 9, 21);

        Parte parteVariavel =
                service.listarPartesVariaveis()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        service.adicionarParteVariavel(
                data,
                parteVariavel.getId()
        );

        service.removerParteVariavel(
                data,
                parteVariavel.getId()
        );

        ProgramacaoSemana novaInstancia =
                new ProgramacaoSemanaService()
                        .obterOuCriar(data);

        assertFalse(
                novaInstancia.getPartesVariaveis()
                        .stream()
                        .anyMatch(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );
    }
}