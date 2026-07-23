package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ControleDesignacoesTest {

    @Test
    void deveRegistrarPessoa() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        controle.registrar(pessoa);

        assertEquals(
                1,
                controle.getPessoasDesignadas().size()
        );
    }

    @Test
    void deveIncrementarQuantidadeDaPessoa() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        controle.registrar(pessoa);
        controle.registrar(pessoa);

        assertEquals(
                2,
                controle.quantidadeDe(pessoa)
        );
    }

    @Test
    void naoDeveDuplicarPessoaNaListaDeDesignadas() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        controle.registrar(pessoa);
        controle.registrar(pessoa);
        controle.registrar(pessoa);

        assertEquals(
                1,
                controle.getPessoasDesignadas().size()
        );
    }

    @Test
    void deveRegistrarParticipacao() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        Parte parte = criarParte();

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                );

        controle.registrarParticipacao(participacao);

        assertEquals(
                1,
                controle.getParticipacoes().size()
        );
    }

    @Test
    void deveRetornarParticipacoes() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        criarPessoa(),
                        criarParte(),
                        TipoParticipacao.LEITOR
                );

        controle.registrarParticipacao(participacao);

        assertFalse(
                controle.getParticipacoes().isEmpty()
        );
    }

    @Test
    void deveInformarQuantidadeDeParticipacoes() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        controle.registrar(pessoa);
        controle.registrar(pessoa);

        assertEquals(
                2,
                controle.quantidadeDe(pessoa)
        );
    }

    @Test
    void deveInformarQuePessoaJaFezParte() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        Parte parte = criarParte();

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        assertTrue(
                controle.jaFezParte(
                        pessoa,
                        parte
                )
        );
    }

    @Test
    void deveRetornarQuantidadeVezesNaParte() {

        ControleDesignacoes controle =
                new ControleDesignacoes();

        Pessoa pessoa = criarPessoa();

        Parte parte = criarParte();

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        assertEquals(
                2,
                controle.quantidadeVezesNaParte(
                        pessoa,
                        parte
                )
        );
    }

    @Test
    void deveInicializarComHistorico() {

        Pessoa pessoa = criarPessoa();

        Parte parte = criarParte();

        HistoricoDesignacoes historico =
                new HistoricoDesignacoes();

        historico.adicionar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        ControleDesignacoes controle =
                new ControleDesignacoes(
                        historico
                );

        assertEquals(
                1,
                controle.quantidadeDe(pessoa)
        );

        assertTrue(
                controle.getPessoasDesignadas().isEmpty()
        );
    }

    @Test
    void deveInicializarComListaDeParticipacoes() {

        Pessoa pessoa = criarPessoa();

        Parte parte = criarParte();

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                );

        ControleDesignacoes controle =
                new ControleDesignacoes(
                        List.of(participacao)
                );

        assertEquals(
                1,
                controle.quantidadeDe(pessoa)
        );

        assertEquals(
                1,
                controle.getParticipacoes().size()
        );
    }

    private Pessoa criarPessoa() {

        return new Pessoa(
                "Pessoa Teste",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.PUBLICADOR
        );
    }

    private Parte criarParte() {

        return new Parte(
                "Parte Teste",
                TipoParte.LEITURA,
                Privilegio.PUBLICADOR,
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