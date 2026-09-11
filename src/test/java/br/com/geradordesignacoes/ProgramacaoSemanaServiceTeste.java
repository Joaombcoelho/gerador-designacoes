package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
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
}