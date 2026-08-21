package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EscalaTest {

    @Test
    void deveSubstituirUmaDesignacao() {

        LocalDate data = LocalDate.of(
                2026,
                8,
                21
        );

        Pessoa pessoaOriginal =
                criarPessoa(
                        "João"
                );

        Pessoa novaPessoa =
                criarPessoa(
                        "Carlos"
                );

        Parte parte =
                criarParte();

        Designacao designacaoOriginal =
                new Designacao(
                        data,
                        parte,
                        pessoaOriginal,
                        null
                );

        Escala escala =
                new Escala(
                        data,
                        List.of(
                                designacaoOriginal
                        )
                );

        Designacao novaDesignacao =
                new Designacao(
                        data,
                        parte,
                        novaPessoa,
                        null
                );

        escala.substituirDesignacao(
                0,
                novaDesignacao
        );

        Designacao designacaoAtual =
                escala.getDesignacoes()
                        .get(0);

        assertEquals(
                novaPessoa.getNome(),
                designacaoAtual
                        .responsavel()
                        .getNome()
        );
    }


    @Test
    void deveLancarExcecaoAoSubstituirIndiceInvalido() {

        LocalDate data = LocalDate.of(
                2026,
                8,
                21
        );

        Pessoa pessoa =
                criarPessoa(
                        "João"
                );

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        data,
                        parte,
                        pessoa,
                        null
                );

        Escala escala =
                new Escala(
                        data,
                        List.of(
                                designacao
                        )
                );

        assertThrows(
                IndexOutOfBoundsException.class,
                () -> escala.substituirDesignacao(
                        1,
                        designacao
                )
        );
    }


    private Pessoa criarPessoa(
            String nome
    ) {

        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                true,
                false,
                false,
                Privilegio.ANCIAO
        );
    }


    private Parte criarParte() {

        return new Parte(
                "Leitura",
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