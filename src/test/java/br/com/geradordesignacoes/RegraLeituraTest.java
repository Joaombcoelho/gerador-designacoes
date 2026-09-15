package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegraLeituraTest {

    @Test
    void leituraDeveAceitarSomenteNivelBasico() {

        Parte leitura = criarParteLeitura();

        Pessoa leitorBasico =
                criarPessoa(
                        "Leitor Básico",
                        NivelLeitura.BASICO
                );

        Pessoa leitorExperiente =
                criarPessoa(
                        "Leitor Experiente",
                        NivelLeitura.EXPERIENTE
                );

        assertTrue(
                leitura.pessoaPodeExercerParticipacao(
                        leitorBasico,
                        TipoParticipacao.LEITOR
                ),
                "Leitura deve aceitar leitor com nível BASICO."
        );

        assertFalse(
                leitura.pessoaPodeExercerParticipacao(
                        leitorExperiente,
                        TipoParticipacao.LEITOR
                ),
                "Leitura não deve aceitar leitor com nível EXPERIENTE."
        );
    }

    @Test
    void leituraDeveRejeitarExplicitamenteNivelExperiente() {

        Parte leitura = criarParteLeitura();

        Pessoa leitorExperiente =
                criarPessoa(
                        "Leitor Experiente",
                        NivelLeitura.EXPERIENTE
                );

        assertFalse(
                leitura.pessoaPodeExercerParticipacao(
                        leitorExperiente,
                        TipoParticipacao.LEITOR
                ),
                "Um leitor EXPERIENTE não pode ser designado para a parte Leitura."
        );
    }

    private Pessoa criarPessoa(
            String nome,
            NivelLeitura nivelLeitura
    ) {

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
                nivelLeitura
        );
    }

    private Parte criarParteLeitura() {

        return new Parte(
                null,
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                NivelLeitura.BASICO,
                null,
                TipoVariacaoParte.FIXA,
                false,
                List.of(TipoParticipacao.LEITOR)
        );
    }
}
