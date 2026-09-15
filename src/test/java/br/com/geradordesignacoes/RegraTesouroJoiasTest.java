package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegraTesourosJoiasTest {

    @Test
    void mesmaPessoaNaoPodeFazerTesourosEJoiasNaMesmaSemana() {

        Pessoa pessoa = criarPessoa("João");

        Parte tesouros = criarParte(
                TipoParte.DISCURSO_TESOUROS,
                "Discurso - Tesouros da Palavra de Deus"
        );

        Parte joias = criarParte(
                TipoParte.JOIAS_ESPIRITUAIS,
                "Joias espirituais"
        );

        LocalDate data = LocalDate.of(2026, 9, 16);

        /*
         * Teste 1:
         * A pessoa fez Tesouros e tenta receber Joias
         * na mesma semana.
         */
        ControleDesignacoes controleTesouros =
                new ControleDesignacoes();

        controleTesouros.registrarParticipacao(
                new ParticipacaoDesignacao(
                        null,
                        data,
                        pessoa,
                        tesouros,
                        TipoParticipacao.RESPONSAVEL
                )
        );

        assertTrue(
                controleTesouros.possuiConflitoTesourosJoias(
                        pessoa,
                        joias,
                        data
                ),
                "A pessoa não deve poder fazer Joias na mesma semana em que fez Tesouros."
        );

        /*
         * Teste 2:
         * A pessoa fez Joias e tenta receber Tesouros
         * na mesma semana.
         */
        ControleDesignacoes controleJoias =
                new ControleDesignacoes();

        controleJoias.registrarParticipacao(
                new ParticipacaoDesignacao(
                        null,
                        data,
                        pessoa,
                        joias,
                        TipoParticipacao.RESPONSAVEL
                )
        );

        assertTrue(
                controleJoias.possuiConflitoTesourosJoias(
                        pessoa,
                        tesouros,
                        data
                ),
                "A pessoa não deve poder fazer Tesouros na mesma semana em que fez Joias."
        );
    }

    @Test
    void mesmaPessoaPodeFazerTesourosEJoiasEmSemanasDiferentes() {

        Pessoa pessoa = criarPessoa("João");

        Parte tesouros = criarParte(
                TipoParte.DISCURSO_TESOUROS,
                "Discurso - Tesouros da Palavra de Deus"
        );

        Parte joias = criarParte(
                TipoParte.JOIAS_ESPIRITUAIS,
                "Joias espirituais"
        );

        LocalDate semana1 = LocalDate.of(2026, 9, 9);
        LocalDate semana2 = LocalDate.of(2026, 9, 16);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        null,
                        semana1,
                        pessoa,
                        tesouros,
                        TipoParticipacao.RESPONSAVEL
                )
        );

        assertFalse(
                controle.possuiConflitoTesourosJoias(
                        pessoa,
                        joias,
                        semana2
                ),
                "A pessoa deve poder fazer Joias em uma semana diferente de Tesouros."
        );
    }

    @Test
    void conflitoNaoDeveBloquearOutrasPartes() {

        Pessoa pessoa = criarPessoa("João");

        Parte tesouros = criarParte(
                TipoParte.DISCURSO_TESOUROS,
                "Discurso - Tesouros da Palavra de Deus"
        );

        Parte outraParte = criarParte(
                TipoParte.DISCURSO,
                "Outra parte"
        );

        LocalDate data = LocalDate.of(2026, 9, 16);

        ControleDesignacoes controle =
                new ControleDesignacoes();

        controle.registrarParticipacao(
                new ParticipacaoDesignacao(
                        null,
                        data,
                        pessoa,
                        tesouros,
                        TipoParticipacao.RESPONSAVEL
                )
        );

        assertFalse(
                controle.possuiConflitoTesourosJoias(
                        pessoa,
                        outraParte,
                        data
                ),
                "A regra Tesouros × Joias não deve bloquear outras partes."
        );
    }

    private Pessoa criarPessoa(String nome) {

        return new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                true,
                false,
                true,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );
    }

    private Parte criarParte(
            TipoParte tipo,
            String nome
    ) {

        return new Parte(
                null,
                nome,
                tipo,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoVariacaoParte.FIXA,
                false,
                List.of(TipoParticipacao.RESPONSAVEL)
        );
    }
}
