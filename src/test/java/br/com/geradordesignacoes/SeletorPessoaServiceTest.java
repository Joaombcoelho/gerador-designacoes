package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                diagnostico.getParte()
        );

        assertEquals(
                2,
                diagnostico.getCandidatos().size()
        );

        assertNotNull(
                diagnostico.getEscolhido()
        );

        assertEquals(
                anciao,
                diagnostico.getEscolhido().getPessoa()
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
                diagnostico.getCandidatos().isEmpty()
        );

        assertNull(
                diagnostico.getEscolhido()
        );
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
}