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
                                                .equals(
                                                        parteVariavel.getId()
                                                )
                        )
        );
    }
}