
package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorSalasAuxiliaresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorSalasAuxiliaresServiceTest {

    private GeradorSalasAuxiliaresService gerador;

    @BeforeEach
    void setUp() {
        gerador = new GeradorSalasAuxiliaresService();
    }

    @Test
    void deveFiltrarSomentePartesElegiveis() {
        Parte leitura = criarParte(
                "Leitura",
                TipoParte.LEITURA,
                TipoParticipacao.LEITOR
        );

        Parte demonstracao = criarParte(
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                TipoParticipacao.RESPONSAVEL,
                TipoParticipacao.AJUDANTE
        );

        Parte discursoTesouros = criarParte(
                "Discurso — Tesouros",
                TipoParte.DISCURSO_TESOUROS,
                TipoParticipacao.ORADOR
        );

        List<Parte> resultado = gerador.filtrarPartesElegiveis(
                List.of(leitura, demonstracao, discursoTesouros)
        );

        assertTrue(resultado.contains(leitura));
        assertTrue(resultado.contains(demonstracao));
        assertFalse(resultado.contains(discursoTesouros));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPartes() {
        assertTrue(gerador.filtrarPartesElegiveis(null).isEmpty());
        assertTrue(gerador.filtrarPartesElegiveis(List.of()).isEmpty());
    }

    @Test
    void deveFiltrarCandidatosPelaPermissaoDaSala() {
        Pessoa autorizadaSalaB = criarPessoa(
                "Pessoa Sala B", true, false
        );

        Pessoa autorizadaSalaC = criarPessoa(
                "Pessoa Sala C", false, true
        );

        Parte leitura = criarParte(
                "Leitura",
                TipoParte.LEITURA,
                TipoParticipacao.LEITOR
        );

        List<Pessoa> candidatosB = gerador.filtrarCandidatos(
                List.of(autorizadaSalaB, autorizadaSalaC),
                leitura,
                SalaAuxiliar.B,
                TipoParticipacao.LEITOR
        );

        assertEquals(1, candidatosB.size());
        assertTrue(candidatosB.contains(autorizadaSalaB));
        assertFalse(candidatosB.contains(autorizadaSalaC));
    }

    @Test
    void naoDeveSelecionarPessoaSemQualificacao() {
        Pessoa pessoa = new Pessoa(
                "Sem leitura",
                Sexo.MASCULINO,
                true,
                true,
                true,
                false,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO,
                NivelLeitura.BASICO,
                true,
                true,
                true
        );

        Parte leitura = criarParte(
                "Leitura",
                TipoParte.LEITURA,
                TipoParticipacao.LEITOR
        );

        List<Pessoa> candidatos = gerador.filtrarCandidatos(
                List.of(pessoa),
                leitura,
                SalaAuxiliar.B,
                TipoParticipacao.LEITOR
        );

        assertTrue(candidatos.isEmpty());
    }

    @Test
    void deveConverterSalasAuxiliaresCorretamente() {
        assertEquals(
                SalaDesignacao.B,
                gerador.converterSala(SalaAuxiliar.B)
        );

        assertEquals(
                SalaDesignacao.C,
                gerador.converterSala(SalaAuxiliar.C)
        );
    }

    @Test
    void naoDeveConverterSalaNula() {
        assertThrows(
                NullPointerException.class,
                () -> gerador.converterSala(null)
        );
    }

    private Pessoa criarPessoa(
            String nome,
            boolean salaB,
            boolean salaC
    ) {
        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.ANCIAO,
                NivelLeitura.BASICO,
                true,
                salaB,
                salaC
        );
    }

    private Parte criarParte(
            String nome,
            TipoParte tipo,
            TipoParticipacao... participacoes
    ) {
        return new Parte(
                nome,
                tipo,
                Privilegio.PUBLICADOR,
                tipo == TipoParte.DEMONSTRACAO,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(participacoes)
        );
    }
}