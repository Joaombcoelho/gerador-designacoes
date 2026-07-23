package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.AvaliadorPessoaService;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AvaliadorPessoaServiceTest {

    private AvaliadorPessoaService avaliador;

    @BeforeEach
    void setUp() {

        avaliador = new AvaliadorPessoaService();
    }

    @Test
    void devePontuarZeroParticipacoes() {

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        criarPessoa(Privilegio.PUBLICADOR),
                        criarParte(Privilegio.PUBLICADOR),
                        new ControleDesignacoes()
                );

        assertEquals(50, resultado.getPontosParticipacoes());
    }

    @Test
    void devePontuarUmaParticipacao() {

        Pessoa pessoa = criarPessoa(Privilegio.PUBLICADOR);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrar(pessoa);

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        criarParte(Privilegio.PUBLICADOR),
                        controle
                );

        assertEquals(40, resultado.getPontosParticipacoes());
    }

    @Test
    void devePontuarDuasParticipacoes() {

        Pessoa pessoa = criarPessoa(Privilegio.PUBLICADOR);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrar(pessoa);
        controle.registrar(pessoa);

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        criarParte(Privilegio.PUBLICADOR),
                        controle
                );

        assertEquals(30, resultado.getPontosParticipacoes());
    }

    @Test
    void devePontuarTresParticipacoes() {

        Pessoa pessoa = criarPessoa(Privilegio.PUBLICADOR);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrar(pessoa);
        controle.registrar(pessoa);
        controle.registrar(pessoa);

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        criarParte(Privilegio.PUBLICADOR),
                        controle
                );

        assertEquals(20, resultado.getPontosParticipacoes());
    }

    @Test
    void devePontuarQuatroOuMaisParticipacoes() {

        Pessoa pessoa = criarPessoa(Privilegio.PUBLICADOR);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrar(pessoa);
        controle.registrar(pessoa);
        controle.registrar(pessoa);
        controle.registrar(pessoa);

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        criarParte(Privilegio.PUBLICADOR),
                        controle
                );

        assertEquals(10, resultado.getPontosParticipacoes());
    }

    @Test
    void devePontuarPrivilegioSuperior() {

        Pessoa pessoa =
                criarPessoa(
                        Privilegio.ANCIAO
                );

        Parte parte =
                criarParte(
                        Privilegio.PUBLICADOR
                );

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        parte,
                        new ControleDesignacoes()
                );

        int esperado =
                (pessoa.getPrivilegio().getNivel()
                        - parte.getPrivilegioMinimo().getNivel())
                        * 10;

        assertEquals(
                esperado,
                resultado.getPontosPrivilegio()
        );
    }

    @Test
    void naoDevePontuarPrivilegioIgual() {

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        criarPessoa(Privilegio.PUBLICADOR),
                        criarParte(Privilegio.PUBLICADOR),
                        new ControleDesignacoes()
                );

        assertEquals(0, resultado.getPontosPrivilegio());
    }

    @Test
    void naoDevePontuarPrivilegioInferior() {

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        criarPessoa(Privilegio.PUBLICADOR),
                        criarParte(Privilegio.ANCIAO),
                        new ControleDesignacoes()
                );

        assertEquals(0, resultado.getPontosPrivilegio());
    }

    @Test
    void deveAplicarPenalidadePorRepeticao() {

        Pessoa pessoa =
                criarPessoa(Privilegio.PUBLICADOR);

        Parte parte =
                criarParte(Privilegio.PUBLICADOR);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        pessoa,
                        parte,
                        controle
                );

        assertEquals(
                15,
                resultado.getPenalidadeRepeticao()
        );
    }

    @Test
    void naoDeveAplicarPenalidadeSemHistorico() {

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        criarPessoa(Privilegio.PUBLICADOR),
                        criarParte(Privilegio.PUBLICADOR),
                        new ControleDesignacoes()
                );

        assertEquals(
                0,
                resultado.getPenalidadeRepeticao()
        );
    }

    @Test
    void deveCalcularPontuacaoTotal() {

        ResultadoAvaliacaoPessoa resultado =
                avaliador.avaliar(
                        criarPessoa(Privilegio.ANCIAO),
                        criarParte(Privilegio.PUBLICADOR),
                        new ControleDesignacoes()
                );

        assertEquals(
                resultado.getPontosParticipacoes()
                        + resultado.getPontosPrivilegio()
                        - resultado.getPenalidadeRepeticao(),
                resultado.getTotal()
        );
    }

    private Pessoa criarPessoa(
            Privilegio privilegio
    ) {

        return new Pessoa(
                "Pessoa Teste",
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