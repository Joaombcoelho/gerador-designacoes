package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.model.Pessoa;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ParteTest {

    private Parte criarParte(
            Privilegio privilegioMinimo,
            SexoPermitido sexoPermitido,
            NivelLeitura nivelLeituraMinimo,
            List<TipoParticipacao> participacoes
    ) {
        return new Parte(
                "Parte de teste",
                TipoParte.DISCURSO,
                privilegioMinimo,
                false,
                sexoPermitido,
                1,
                false,
                nivelLeituraMinimo,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoVariacaoParte.FIXA,
                false,
                participacoes
        );
    }

    private Pessoa criarPessoa(
            Sexo sexo,
            Privilegio privilegio,
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            boolean oracao,
            boolean presidente,
            boolean dirigente,
            NivelLeitura nivelLeitura
    ) {
        return new Pessoa(
                "Pessoa de teste",
                sexo,
                true,
                responsavel,
                ajudante,
                leitura,
                discurso,
                oracao,
                presidente,
                dirigente,
                privilegio,
                nivelLeitura
        );
    }

    @Test
    void devePermitirResponsavelHabilitado() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.MASCULINO,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertTrue(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.RESPONSAVEL
                )
        );
    }

    @Test
    void naoDevePermitirResponsavelSemHabilitacao() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.MASCULINO,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.RESPONSAVEL
                )
        );
    }

    @Test
    void devePermitirAjudanteHabilitado() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.AJUDANTE)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.FEMININO,
                Privilegio.PUBLICADOR,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertTrue(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.AJUDANTE
                )
        );
    }

    @Test
    void naoDevePermitirAjudanteSemHabilitacao() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.AJUDANTE)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.FEMININO,
                Privilegio.PUBLICADOR,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.AJUDANTE
                )
        );
    }

    @Test
    void devePermitirLeitorComNivelSuficiente() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.EXPERIENTE,
                List.of(TipoParticipacao.LEITOR)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                NivelLeitura.EXPERIENTE
        );

        assertTrue(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.LEITOR
                )
        );
    }

    @Test
    void naoDevePermitirLeitorComNivelInsuficiente() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.EXPERIENTE,
                List.of(TipoParticipacao.LEITOR)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.LEITOR
                )
        );
    }

    @Test
    void leitorExperienteDevePoderRealizarParteBasica() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.LEITOR)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                NivelLeitura.EXPERIENTE
        );

        assertTrue(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.LEITOR
                )
        );
    }

    @Test
    void leitorBasicoNaoDeveRealizarParteExperiente() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.EXPERIENTE,
                List.of(TipoParticipacao.LEITOR)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.LEITOR
                )
        );
    }

    @Test
    void naoDevePermitirPessoaComPrivilegioInsuficiente() {

        Parte parte = criarParte(
                Privilegio.SERVO_MINISTERIAL,
                SexoPermitido.MASCULINO,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.RESPONSAVEL
                )
        );
    }

    @Test
    void naoDevePermitirSexoIncompativel() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.MASCULINO,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.FEMININO,
                Privilegio.PUBLICADOR,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.RESPONSAVEL
                )
        );
    }

    @Test
    void naoDevePermitirPessoaInativa() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = new Pessoa(
                "Pessoa inativa",
                Sexo.MASCULINO,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.RESPONSAVEL
                )
        );
    }

    @Test
    void naoDevePermitirParticipacaoNaoNecessaria() {

        Parte parte = criarParte(
                Privilegio.PUBLICADOR,
                SexoPermitido.AMBOS,
                NivelLeitura.BASICO,
                List.of(TipoParticipacao.RESPONSAVEL)
        );

        Pessoa pessoa = criarPessoa(
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );

        assertFalse(
                parte.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.AJUDANTE
                )
        );
    }
}