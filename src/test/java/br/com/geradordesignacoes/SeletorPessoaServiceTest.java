package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SeletorPessoaServiceTest {

    private SeletorPessoaService seletor;

    @BeforeEach
    void setUp() {

        seletor = new SeletorPessoaService(
                new RegrasService(),
                new AvaliadorPessoaService()
        );
    }

    @Test
    void deveSelecionarMelhorPessoa() {

        Pessoa publicador = criarPessoa(
                "Publicador",
                Privilegio.PUBLICADOR
        );

        Pessoa anciao = criarPessoa(
                "Ancião",
                Privilegio.ANCIAO
        );

        Parte parte = criarParte(
                Privilegio.PUBLICADOR
        );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Optional<Pessoa> resultado =
                seletor.selecionarMelhorPessoa(
                        parte,
                        List.of(publicador, anciao),
                        controle
                );

        assertTrue(resultado.isPresent());

        assertEquals(
                anciao,
                resultado.get()
        );
    }

    @Test
    void naoDeveSelecionarQuandoNaoHaCandidatosValidos() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR
        );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Optional<Pessoa> resultado =
                seletor.selecionarMelhorPessoa(
                        parte,
                        List.of(),
                        controle
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveAvaliarTodosOsCandidatosValidos() {

        Pessoa p1 =
                criarPessoa(
                        "João",
                        Privilegio.PUBLICADOR
                );

        Pessoa p2 =
                criarPessoa(
                        "Pedro",
                        Privilegio.BATIZADO
                );

        Parte parte =
                criarParte(
                        Privilegio.PUBLICADOR
                );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        List<ResultadoAvaliacaoPessoa> resultados =
                seletor.avaliarCandidatos(
                        parte,
                        List.of(p1, p2),
                        controle
                );

        assertEquals(
                2,
                resultados.size()
        );
    }

    @Test
    void deveIgnorarPessoaInativa() {

        Pessoa ativa =
                criarPessoa(
                        "Ativa",
                        Privilegio.PUBLICADOR
                );

        Pessoa inativa =
                new Pessoa(
                        "Inativa",
                        Sexo.MASCULINO,
                        false,
                        true,
                        true,
                        true,
                        false,
                        Privilegio.PUBLICADOR
                );

        Parte parte =
                criarParte(
                        Privilegio.PUBLICADOR
                );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        List<ResultadoAvaliacaoPessoa> resultados =
                seletor.avaliarCandidatos(
                        parte,
                        List.of(
                                ativa,
                                inativa
                        ),
                        controle
                );

        assertEquals(
                1,
                resultados.size()
        );
    }

    @Test
    void deveGerarDiagnostico() {

        Pessoa publicador =
                criarPessoa(
                        "Publicador",
                        Privilegio.PUBLICADOR
                );

        Pessoa anciao =
                criarPessoa(
                        "Ancião",
                        Privilegio.ANCIAO
                );

        Parte parte =
                criarParte(
                        Privilegio.PUBLICADOR
                );

        ControleDesignacoes controle =
                new ControleDesignacoes();

        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        parte,
                        List.of(
                                publicador,
                                anciao
                        ),
                        controle
                );

        assertNotNull(diagnostico);

        assertEquals(
                parte,
                diagnostico.parte()
        );

        assertEquals(
                2,
                diagnostico.candidatos().size()
        );

        assertNotNull(
                diagnostico.escolhido()
        );

        assertEquals(
                anciao,
                diagnostico.escolhido().getPessoa()
        );
    }

    @Test
    void deveRetornarDiagnosticoSemEscolhidoQuandoNaoHaCandidatos() {

        Parte parte =
                criarParte(
                        Privilegio.PUBLICADOR
                );

        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        parte,
                        List.of(),
                        new ControleDesignacoes()
                );

        assertNotNull(diagnostico);

        assertTrue(
                diagnostico.candidatos().isEmpty()
        );

        assertNull(
                diagnostico.escolhido()
        );
    }

    @Test
    void naoDeveSelecionarPresidenteComoDirigente() {
        Pessoa presidente = criarPessoaComParticipacao(
                "Presidente",
                true,
                true
        );
        Pessoa candidato = criarPessoaComParticipacao(
                "Candidato",
                true,
                true
        );
        Parte parte = criarParteComParticipacao(
                TipoParticipacao.DIRIGENTE
        );
        ControleDesignacoes controle = new ControleDesignacoes();
        controle.definirPresidente(presidente);

        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        parte,
                        List.of(presidente, candidato),
                        controle,
                        LocalDate.of(2026, 8, 4),
                        TipoParticipacao.DIRIGENTE
                );

        assertFalse(
                diagnostico.candidatos().stream()
                        .anyMatch(resultado ->
                                resultado.getPessoa().equals(presidente))
        );
        assertEquals(candidato, diagnostico.escolhido().getPessoa());
    }

    @Test
    void naoDeveSelecionarPresidenteComoAjudante() {
        Pessoa presidente = criarPessoaComParticipacao(
                "Presidente",
                true,
                true
        );
        Parte parte = criarParteComParticipacao(
                TipoParticipacao.AJUDANTE
        );
        ControleDesignacoes controle = new ControleDesignacoes();
        controle.definirPresidente(presidente);

        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        parte,
                        List.of(presidente),
                        controle,
                        LocalDate.of(2026, 8, 4),
                        TipoParticipacao.AJUDANTE
                );

        assertTrue(diagnostico.candidatos().isEmpty());
        assertNull(diagnostico.escolhido());
    }

    @Test
    void pessoaQueNaoEPresidenteContinuaElegivel() {
        Pessoa candidato = criarPessoaComParticipacao(
                "Candidato",
                true,
                true
        );
        Parte parte = criarParteComParticipacao(
                TipoParticipacao.DIRIGENTE
        );
        ControleDesignacoes controle = new ControleDesignacoes();

        DiagnosticoSelecaoPessoa diagnostico =
                seletor.selecionarComDiagnostico(
                        parte,
                        List.of(candidato),
                        controle,
                        LocalDate.of(2026, 8, 4),
                        TipoParticipacao.DIRIGENTE
                );

        assertEquals(candidato, diagnostico.escolhido().getPessoa());
    }

    private Pessoa criarPessoa(
            String nome,
            Privilegio privilegio
    ) {

        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                privilegio
        );
    }

    private Parte criarParte(
            Privilegio privilegio
    ) {

        return new Parte(
                "Parte Teste",
                TipoParte.LEITURA,
                privilegio,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(
                        TipoParticipacao.LEITOR
                )
        );
    }

    private Pessoa criarPessoaComParticipacao(
            String nome,
            boolean podeSerAjudante,
            boolean podeSerDirigente
    ) {
        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                podeSerAjudante,
                true,
                true,
                true,
                true,
                podeSerDirigente,
                Privilegio.ANCIAO,
                NivelLeitura.EXPERIENTE
        );
    }

    private Parte criarParteComParticipacao(
            TipoParticipacao participacao
    ) {
        return new Parte(
                "Parte " + participacao,
                TipoParte.PARTE_1,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                List.of(participacao)
        );
    }
}