package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorSalasAuxiliaresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GeradorSalasAuxiliaresCasosLimiteTest {

    private GeradorSalasAuxiliaresService gerador;

    @BeforeEach
    void configurar() {
        gerador = new GeradorSalasAuxiliaresService();
    }

    @Test
    void deveUsarPessoasDiferentesComoResponsavelEAjudanteNasDuasSalas() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        Parte demonstracao = criarDemonstracao();

        List<Pessoa> pessoas = List.of(
                criarPessoa("Pessoa 1", true, true),
                criarPessoa("Pessoa 2", true, true),
                criarPessoa("Pessoa 3", true, true),
                criarPessoa("Pessoa 4", true, true)
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                data,
                List.of(demonstracao),
                pessoas,
                List.of(),
                true,
                true
        );

        assertFalse(
                resultado.possuiErros(),
                "A geração não deveria apresentar erros: "
                        + resultado.erros()
        );

        assertEquals(
                2,
                resultado.designacoes().size(),
                "Deveria haver uma demonstração em cada sala auxiliar."
        );

        assertTrue(
                resultado.designacoes().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.B)
        );

        assertTrue(
                resultado.designacoes().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.C)
        );

        assertTrue(
                resultado.designacoes().stream()
                        .allMatch(d ->
                                d.responsavel() != null
                                        && d.ajudante() != null
                                        && !d.responsavel().equals(d.ajudante())
                        ),
                "Responsável e ajudante devem ser pessoas diferentes."
        );

        long pessoasDistintas = resultado.designacoes().stream()
                .flatMap(d -> Stream.of(
                        d.responsavel(),
                        d.ajudante()
                ))
                .distinct()
                .count();

        assertEquals(
                4,
                pessoasDistintas,
                "As duas salas devem utilizar quatro pessoas diferentes."
        );
    }

    @Test
    void naoDeveGerarDemonstracaoQuandoNaoHaAjudanteDisponivel() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        Parte demonstracao = criarDemonstracao();

        Pessoa unicaPessoa = criarPessoa(
                "Única pessoa disponível",
                true,
                false
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                data,
                List.of(demonstracao),
                List.of(unicaPessoa),
                List.of(),
                true,
                false
        );

        assertTrue(
                resultado.designacoes().isEmpty(),
                "Não deve existir demonstração sem ajudante."
        );

        assertTrue(
                resultado.possuiErros(),
                "A ausência de ajudante deve ser informada."
        );

        assertTrue(
                resultado.erros().stream()
                        .anyMatch(erro ->
                                erro.toLowerCase().contains("ajudante")
                        ),
                "O erro deve indicar a indisponibilidade de ajudante: "
                        + resultado.erros()
        );
    }

    @Test
    void naoDeveReutilizarNaSalaAuxiliarPessoaDaSalaPrincipalNaMesmaParte() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        Parte leitura = criarParte(
                "Leitura",
                TipoParte.LEITURA,
                TipoParticipacao.LEITOR
        );

        Pessoa pessoaDaPrincipal = criarPessoa(
                "Pessoa da Principal",
                true,
                false
        );

        Pessoa outraPessoa = criarPessoa(
                "Outra pessoa",
                true,
                false
        );

        Designacao designacaoPrincipal = new Designacao(
                data,
                leitura,
                pessoaDaPrincipal,
                null
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                data,
                List.of(leitura),
                List.of(pessoaDaPrincipal, outraPessoa),
                List.of(designacaoPrincipal),
                true,
                false
        );

        assertFalse(
                resultado.possuiErros(),
                "A outra pessoa deveria poder ser designada: "
                        + resultado.erros()
        );

        assertEquals(
                1,
                resultado.designacoes().size()
        );

        Designacao designacaoAuxiliar =
                resultado.designacoes().getFirst();

        assertEquals(
                SalaDesignacao.B,
                designacaoAuxiliar.sala()
        );

        assertEquals(
                outraPessoa,
                designacaoAuxiliar.responsavel(),
                "A pessoa já designada na Sala Principal não deve ser reutilizada."
        );
    }

    private Parte criarDemonstracao() {
        return new Parte(
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                Privilegio.PUBLICADOR,
                true,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
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
}
