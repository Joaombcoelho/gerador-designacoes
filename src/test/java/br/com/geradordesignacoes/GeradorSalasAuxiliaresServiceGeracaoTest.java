package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.ResultadoGeracaoSalasAuxiliares;
import br.com.geradordesignacoes.model.SalaDesignacao;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.service.GeradorSalasAuxiliaresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeradorSalasAuxiliaresServiceGeracaoTest {

    private final GeradorSalasAuxiliaresService gerador =
            new GeradorSalasAuxiliaresService();
    private GeradorSalasAuxiliaresService service;
    private LocalDate data;
    private static final LocalDate DATA = LocalDate.of(2026, 10, 12);

    @BeforeEach
    void setUp() {
        service = new GeradorSalasAuxiliaresService();
        data = LocalDate.of(2026, 10, 12);
    }

    @Test
    void deveRetornarResultadoVazioQuandoAmbasAsSalasEstiveremDesabilitadas() {
        Parte leitura = parteLeitura("Leitura");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(leitor("Leitor")),
                List.of(),
                false,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.erros().isEmpty());
        assertTrue(resultado.foiGeradaComSucesso());
    }

    @Test
    void deveGerarLeituraSomenteNaSalaBQuandoSomenteBEstaHabilitada() {
        Parte leitura = parteLeitura("Leitura");
        Pessoa pessoa = leitor("Leitor B");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(pessoa),
                List.of(),
                true,
                false
        );

        assertEquals(1, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());

        Designacao designacao = resultado.designacoes().getFirst();

        assertEquals(SalaDesignacao.B, designacao.sala());
        assertEquals(leitura, designacao.parte());
        assertSame(pessoa, designacao.responsavel());
        assertNull(designacao.ajudante());
        assertEquals(data, designacao.data());
    }

    @Test
    void deveGerarLeituraSomenteNaSalaCQuandoSomenteCEstaHabilitada() {
        Parte leitura = parteLeitura("Leitura");
        Pessoa pessoa = leitor("Leitor C");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(pessoa),
                List.of(),
                false,
                true
        );

        assertEquals(1, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());
        assertEquals(SalaDesignacao.C, resultado.designacoes().getFirst().sala());
        assertSame(pessoa, resultado.designacoes().getFirst().responsavel());
    }

    @Test
    void deveGerarDemonstracaoComResponsavelEAjudanteDiferentes() {
        Parte demonstracao = parteDemonstracao("Demonstração");

        Pessoa responsavel = responsavel("Responsável");
        Pessoa ajudante = ajudante("Ajudante");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(demonstracao),
                List.of(responsavel, ajudante),
                List.of(),
                true,
                false
        );

        assertEquals(1, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());

        Designacao designacao = resultado.designacoes().getFirst();

        assertEquals(SalaDesignacao.B, designacao.sala());
        assertSame(responsavel, designacao.responsavel());
        assertSame(ajudante, designacao.ajudante());
        assertNotSame(designacao.responsavel(), designacao.ajudante());
    }

    @Test
    void naoDeveReutilizarParticipanteDaSalaPrincipalNaMesmaParte() {
        Parte leitura = parteLeitura("Leitura");

        Pessoa participantePrincipal = leitor("Participante Principal");

        Designacao principal = new Designacao(
                data,
                leitura,
                participantePrincipal,
                null,
                SalaDesignacao.PRINCIPAL
        );

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(participantePrincipal),
                List.of(principal),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveReutilizarParticipantesEntreSalasNaMesmaParte() {
        Parte leitura = parteLeitura("Leitura");

        Pessoa primeiroLeitor = leitor("Primeiro leitor");
        Pessoa segundoLeitor = leitor("Segundo leitor");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(primeiroLeitor, segundoLeitor),
                List.of(),
                true,
                true
        );

        assertEquals(2, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());

        Designacao designacaoB = resultado.designacoes().stream()
                .filter(d -> d.sala() == SalaDesignacao.B)
                .findFirst()
                .orElseThrow();

        Designacao designacaoC = resultado.designacoes().stream()
                .filter(d -> d.sala() == SalaDesignacao.C)
                .findFirst()
                .orElseThrow();

        assertNotSame(
                designacaoB.responsavel(),
                designacaoC.responsavel()
        );
    }

    @Test
    void naoDeveSelecionarPessoaInativa() {
        Parte leitura = parteLeitura("Leitura");

        Pessoa inativo = criarPessoa(
                "Inativo", false, false, false, true,
                Privilegio.BATIZADO, true, true
        );

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(inativo),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveSelecionarPessoaSemPermissaoParaSalaB() {
        Parte leitura = parteLeitura("Leitura");

        Pessoa somenteSalaC = criarPessoa(
                "Somente C", true, false, false, true,
                Privilegio.BATIZADO, false, true
        );

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(somenteSalaC),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveSelecionarResponsavelSemPrivilegioAdequado() {
        Parte demonstracao = parteDemonstracao("Demonstração");

        Pessoa publicador = criarPessoa(
                "Publicador", true, true, false, false,
                Privilegio.BATIZADO, true, true
        );

        Pessoa servoAjudante = ajudante("Ajudante");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(demonstracao),
                List.of(publicador, servoAjudante),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveCriarDesignacaoParcialQuandoNaoExisteAjudanteElegivel() {
        Parte demonstracao = parteDemonstracao("Demonstração");

        Pessoa responsavel = responsavel("Responsável");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(demonstracao),
                List.of(responsavel),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void deveRegistrarErroQuandoNaoExisteLeitorElegivel() {
        Parte leitura = parteLeitura("Leitura");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertEquals(1, resultado.erros().size());
        assertTrue(resultado.erros().getFirst().contains("leitor"));
    }

    @Test
    void deveIgnorarPartesNaoElegiveis() {
        Parte discurso = new Parte(
                "Discurso",
                TipoParte.DISCURSO_TESOUROS,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.ORADOR)
        );

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(discurso),
                List.of(responsavel("Responsável")),
                List.of(),
                true,
                true
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.erros().isEmpty());
    }

    @Test
    void deveRetornarListasImutaveisNoResultado() {
        Parte leitura = parteLeitura("Leitura");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura),
                List.of(leitor("Leitor")),
                List.of(),
                true,
                false
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.designacoes().clear()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> resultado.erros().clear()
        );
    }

    @Test
    void deveRejeitarDataNula() {
        assertThrows(
                NullPointerException.class,
                () -> service.gerar(
                        null, List.of(), List.of(), List.of(), true, true
                )
        );
    }

    @Test
    void deveRejeitarListaDePartesNula() {
        assertThrows(
                NullPointerException.class,
                () -> service.gerar(
                        data, null, List.of(), List.of(), true, true
                )
        );
    }

    @Test
    void deveRejeitarListaDePessoasNula() {
        assertThrows(
                NullPointerException.class,
                () -> service.gerar(
                        data, List.of(), null, List.of(), true, true
                )
        );
    }

    @Test
    void deveRejeitarListaDeDesignacoesPrincipaisNula() {
        assertThrows(
                NullPointerException.class,
                () -> service.gerar(
                        data, List.of(), List.of(), null, true, true
                )
        );
    }


    @Test
    void deveGerarDiscursoNaSalaBQuandoOradorEstaHabilitado() {
        Parte parte = criarParteDiscurso("Discurso de teste");
        Pessoa orador = criarPessoa(
                "Orador B", true, true, true, false, Privilegio.BATIZADO, true, false
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(orador),
                List.of(),
                true,
                false
        );


        assertEquals(1, resultado.designacoes().size());
        assertEquals(SalaDesignacao.B, resultado.designacoes().getFirst().sala());
        assertEquals(orador, resultado.designacoes().getFirst().responsavel());
        assertNull(resultado.designacoes().getFirst().ajudante());
        assertTrue(resultado.erros().isEmpty());
    }

    @Test
    void deveGerarDiscursoNaSalaCQuandoOradorEstaHabilitado() {
        Parte parte = criarParteDiscurso("Discurso de teste");
        Pessoa orador = criarPessoa(
                "Orador C", true, true, false, true, Privilegio.BATIZADO, true, true
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(orador),
                List.of(),
                false,
                true
        );


        assertEquals(1, resultado.designacoes().size());
        assertEquals(SalaDesignacao.C, resultado.designacoes().getFirst().sala());
        assertEquals(orador, resultado.designacoes().getFirst().responsavel());
        assertNull(resultado.designacoes().getFirst().ajudante());
        assertTrue(resultado.erros().isEmpty());
    }

    @Test
    void naoDeveGerarDiscursoQuandoPessoaNaoEstaHabilitada() {
        Parte parte = criarParteDiscurso("Discurso de teste");
        Pessoa pessoa = criarPessoa(
                "Pessoa sem habilitação", true, true, true, false, Privilegio.BATIZADO, false, false
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(pessoa),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveGerarDiscursoQuandoPessoaNaoTemPermissaoParaSala() {
        Parte parte = criarParteDiscurso("Discurso de teste");
        Pessoa pessoa = criarPessoa(
                "Orador sem permissão para B", true, true, false, true, Privilegio.BATIZADO, false, false
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(pessoa),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void naoDeveReutilizarOradorDaSalaPrincipalNaMesmaParte() {
        Parte parte = criarParteDiscurso("Discurso de teste");
        Pessoa oradorPrincipal = criarPessoa(
                "Orador principal", true, true, true, false, Privilegio.BATIZADO, true, false
        );

        Designacao designacaoPrincipal = new Designacao(
                DATA,
                parte,
                oradorPrincipal,
                null,
                SalaDesignacao.PRINCIPAL
        );

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(oradorPrincipal),
                List.of(designacaoPrincipal),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
    }

    @Test
    void deveRegistrarErroQuandoNaoExisteOradorDisponivel() {
        Parte parte = criarParteDiscurso("Discurso de teste");

        ResultadoGeracaoSalasAuxiliares resultado = gerador.gerar(
                DATA,
                List.of(parte),
                List.of(),
                List.of(),
                true,
                false
        );

        assertTrue(resultado.designacoes().isEmpty());
        assertTrue(resultado.possuiErros());
        assertTrue(resultado.erros().stream()
                .anyMatch(erro -> erro.contains("orador")));
    }

    @Test
    void deveAlternarLeitoresEntrePartesElegiveis() {
        Parte leitura1 = parteLeitura("Leitura 1");
        Parte leitura2 = parteLeitura("Leitura 2");
        Parte leitura3 = parteLeitura("Leitura 3");

        Pessoa pessoa1 = leitor("Leitor 1");
        Pessoa pessoa2 = leitor("Leitor 2");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(leitura1, leitura2, leitura3),
                List.of(pessoa1, pessoa2),
                List.of(),
                true,
                false
        );

        assertEquals(3, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());

        assertSame(pessoa1, resultado.designacoes().get(0).responsavel());
        assertSame(pessoa2, resultado.designacoes().get(1).responsavel());
        assertSame(pessoa1, resultado.designacoes().get(2).responsavel());
    }

    @Test
    void deveAlternarResponsaveisEmDemonstracoes() {
        Parte demonstracao1 = parteDemonstracao("Demonstração 1");
        Parte demonstracao2 = parteDemonstracao("Demonstração 2");

        Pessoa responsavel1 = responsavel("Responsável 1");
        Pessoa responsavel2 = responsavel("Responsável 2");
        Pessoa ajudante1 = ajudante("Ajudante 1");
        Pessoa ajudante2 = ajudante("Ajudante 2");

        ResultadoGeracaoSalasAuxiliares resultado = service.gerar(
                data,
                List.of(demonstracao1, demonstracao2),
                List.of(responsavel1, responsavel2, ajudante1, ajudante2),
                List.of(),
                true,
                false
        );

        assertEquals(2, resultado.designacoes().size());
        assertTrue(resultado.erros().isEmpty());

        assertSame(
                responsavel1,
                resultado.designacoes().get(0).responsavel()
        );
        assertSame(
                responsavel2,
                resultado.designacoes().get(1).responsavel()
        );

        assertNotSame(
                resultado.designacoes().get(0).responsavel(),
                resultado.designacoes().get(0).ajudante()
        );
        assertNotSame(
                resultado.designacoes().get(1).responsavel(),
                resultado.designacoes().get(1).ajudante()
        );
    }

    private Parte criarParteDiscurso(String nome) {
        return new Parte(
                nome,
                TipoParte.DISCURSO,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.ORADOR)
        );
    }

    private Parte parteLeitura(String nome) {
        return new Parte(
                nome,
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.LEITOR)
        );
    }

    private Parte parteDemonstracao(String nome) {
        return new Parte(
                nome,
                TipoParte.DEMONSTRACAO,
                Privilegio.BATIZADO,
                true,
                SexoPermitido.MASCULINO,
                2,
                false,
                NivelLeitura.BASICO,
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
        );
    }

    private Pessoa leitor(String nome) {
        return criarPessoa(
                nome, true, false, false, true,
                Privilegio.BATIZADO, true, true
        );
    }

    private Pessoa responsavel(String nome) {
        return criarPessoa(
                nome, true, true, false, false,
                Privilegio.ANCIAO, true, true
        );
    }

    private Pessoa ajudante(String nome) {
        return criarPessoa(
                nome, true, false, true, false,
                Privilegio.BATIZADO, true, true
        );
    }

    private Pessoa criarPessoa(
            String nome,
            boolean ativo,
            boolean podeSerResponsavel,
            boolean podeSerAjudante,
            boolean podeFazerLeitura,
            Privilegio privilegio,
            boolean podeSalaB,
            boolean podeSalaC
    ) {
        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                ativo,
                podeSerResponsavel,
                podeSerAjudante,
                podeFazerLeitura,
                true,
                false,
                false,
                false,
                privilegio,
                NivelLeitura.BASICO,
                true,
                podeSalaB,
                podeSalaC
        );
    }
}