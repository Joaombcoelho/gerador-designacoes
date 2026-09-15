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

class RegraEstudoBiblicoTest {

    @Test
    void batizadoExperientePodeSerLeitorDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa batizadoExperiente = criarPessoa(
                "Batizado Experiente",
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        assertTrue(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        batizadoExperiente,
                        TipoParticipacao.AJUDANTE
                ),
                "Batizado experiente deve poder ser leitor do Estudo Bíblico."
        );
    }

    @Test
    void servoMinisterialNaoDeveSerLeitorDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa servoMinisterial = criarPessoa(
                "Servo Ministerial",
                Privilegio.SERVO_MINISTERIAL,
                NivelLeitura.EXPERIENTE
        );

        assertFalse(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        servoMinisterial,
                        TipoParticipacao.AJUDANTE
                ),
                "Servo Ministerial deve ficar reservado para outras designações."
        );
    }

    @Test
    void anciaoNaoDeveSerLeitorDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa anciao = criarPessoa(
                "Ancião",
                Privilegio.ANCIAO,
                NivelLeitura.EXPERIENTE
        );

        assertFalse(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        anciao,
                        TipoParticipacao.AJUDANTE
                ),
                "Ancião deve ficar reservado para outras designações."
        );
    }

    @Test
    void batizadoBasicoNaoDeveSerLeitorDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa batizadoBasico = criarPessoa(
                "Batizado Básico",
                Privilegio.BATIZADO,
                NivelLeitura.BASICO
        );

        assertFalse(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        batizadoBasico,
                        TipoParticipacao.AJUDANTE
                ),
                "Leitor do Estudo Bíblico deve ter nível EXPERIENTE."
        );
    }

    @Test
    void publicadorNaoBatizadoNaoDeveSerLeitorDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa publicador = criarPessoa(
                "Publicador",
                Privilegio.PUBLICADOR,
                NivelLeitura.EXPERIENTE
        );

        assertFalse(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        publicador,
                        TipoParticipacao.AJUDANTE
                ),
                "Publicador não batizado não deve ser leitor do Estudo Bíblico."
        );
    }

    @Test
    void servoMinisterialPodeSerDirigenteDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa servoMinisterial = criarPessoa(
                "Servo Ministerial",
                Privilegio.SERVO_MINISTERIAL,
                NivelLeitura.EXPERIENTE
        );

        assertTrue(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        servoMinisterial,
                        TipoParticipacao.DIRIGENTE
                ),
                "Servo Ministerial deve poder dirigir o Estudo Bíblico."
        );
    }

    @Test
    void anciaoPodeSerDirigenteDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa anciao = criarPessoa(
                "Ancião",
                Privilegio.ANCIAO,
                NivelLeitura.EXPERIENTE
        );

        assertTrue(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        anciao,
                        TipoParticipacao.DIRIGENTE
                ),
                "Ancião deve poder dirigir o Estudo Bíblico."
        );
    }

    @Test
    void batizadoComumNaoPodeSerDirigenteDoEstudoBiblico() {

        Parte estudoBiblico = criarParteEstudoBiblico();

        Pessoa batizado = criarPessoa(
                "Batizado",
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        assertFalse(
                estudoBiblico.pessoaPodeExercerParticipacao(
                        batizado,
                        TipoParticipacao.DIRIGENTE
                ),
                "Batizado comum não deve poder dirigir o Estudo Bíblico."
        );
    }

    private Pessoa criarPessoa(
            String nome,
            Privilegio privilegio,
            NivelLeitura nivelLeitura
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
                false,
                true,
                privilegio,
                nivelLeitura
        );
    }

    private Parte criarParteEstudoBiblico() {
        return new Parte(
                null,
                "Estudo Bíblico de Congregação",
                TipoParte.DIRIGENTE_ESTUDO,
                Privilegio.SERVO_MINISTERIAL,
                true,
                SexoPermitido.MASCULINO,
                2,
                false,
                NivelLeitura.BASICO,
                null,
                TipoVariacaoParte.FIXA,
                false,
                List.of(
                        TipoParticipacao.DIRIGENTE,
                        TipoParticipacao.AJUDANTE
                )
        );
    }
}