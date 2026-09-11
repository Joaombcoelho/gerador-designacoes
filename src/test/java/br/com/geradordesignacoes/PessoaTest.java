package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.model.TipoParticipacao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PessoaTest {

    @Test
    void deveCriarPessoaComConstrutorAntigo() {

        Pessoa pessoa = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO
        );

        assertNull(pessoa.getId());
        assertEquals("João", pessoa.getNome());
        assertEquals(Sexo.MASCULINO, pessoa.getSexo());
        assertTrue(pessoa.isAtivo());

        assertTrue(pessoa.podeSerResponsavel());
        assertTrue(pessoa.podeSerAjudante());
        assertTrue(pessoa.podeFazerLeitura());
        assertTrue(pessoa.podeFazerDiscurso());

        assertFalse(pessoa.podeFazerOracao());
        assertFalse(pessoa.podeSerPresidente());
        assertFalse(pessoa.podeSerDirigente());

        assertEquals(Privilegio.BATIZADO, pessoa.getPrivilegio());
        assertEquals(NivelLeitura.BASICO, pessoa.getNivelLeitura());
    }


    @Test
    void deveCriarPessoaComConstrutorCompleto() {

        Pessoa pessoa = new Pessoa(
                "Maria",
                Sexo.FEMININO,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.SERVO_MINISTERIAL,
                NivelLeitura.EXPERIENTE
        );

        assertEquals("Maria", pessoa.getNome());
        assertEquals(Sexo.FEMININO, pessoa.getSexo());
        assertTrue(pessoa.isAtivo());

        assertTrue(pessoa.podeSerResponsavel());
        assertTrue(pessoa.podeSerAjudante());
        assertTrue(pessoa.podeFazerLeitura());
        assertTrue(pessoa.podeFazerDiscurso());
        assertTrue(pessoa.podeFazerOracao());
        assertTrue(pessoa.podeSerPresidente());
        assertTrue(pessoa.podeSerDirigente());

        assertEquals(Privilegio.SERVO_MINISTERIAL, pessoa.getPrivilegio());
        assertEquals(NivelLeitura.EXPERIENTE, pessoa.getNivelLeitura());
    }


    @Test
    void deveDefinirId() {

        Pessoa pessoa = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoa.setId(123);

        assertEquals(123, pessoa.getId());
    }


    @Test
    void devePermitirExercicioDeResponsavel() {

        Pessoa pessoa = criarPessoa(
                true, false, false, false,
                false, false, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.RESPONSAVEL)
        );

        assertFalse(
                pessoa.podeExercer(TipoParticipacao.AJUDANTE)
        );
    }


    @Test
    void devePermitirExercicioDeAjudante() {

        Pessoa pessoa = criarPessoa(
                false, true, false, false,
                false, false, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.AJUDANTE)
        );

        assertFalse(
                pessoa.podeExercer(TipoParticipacao.RESPONSAVEL)
        );
    }


    @Test
    void devePermitirExercicioDeLeitor() {

        Pessoa pessoa = criarPessoa(
                false, false, true, false,
                false, false, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.LEITOR)
        );

        assertFalse(
                pessoa.podeExercer(TipoParticipacao.ORADOR)
        );
    }


    @Test
    void devePermitirExercicioDeOrador() {

        Pessoa pessoa = criarPessoa(
                false, false, false, true,
                false, false, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.ORADOR)
        );
    }


    @Test
    void devePermitirExercicioDeOracaoInicial() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                true, false, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.ORACAO_INICIAL)
        );

        assertFalse(
                pessoa.podeExercer(TipoParticipacao.ORACAO_FINAL)
        );
    }


    @Test
    void devePermitirExercicioDePresidenteEOracaoFinal() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                false, true, false
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.PRESIDENTE)
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.ORACAO_FINAL)
        );
    }


    @Test
    void devePermitirExercicioDeDirigente() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                false, false, true
        );

        assertTrue(
                pessoa.podeExercer(TipoParticipacao.DIRIGENTE)
        );
    }


    @Test
    void deveRetornarNomeNoToString() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        assertEquals(
                "Pessoa Teste",
                pessoa.toString()
        );
    }


    @Test
    void deveConsiderarPessoasComMesmoIdComoIguais() {

        Pessoa pessoa1 = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        Pessoa pessoa2 = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        pessoa1.setId(10);
        pessoa2.setId(10);

        assertEquals(pessoa1, pessoa2);
        assertEquals(pessoa1.hashCode(), pessoa2.hashCode());
    }


    @Test
    void naoDeveConsiderarPessoasSemIdComoIguais() {

        Pessoa pessoa1 = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        Pessoa pessoa2 = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        assertNotEquals(pessoa1, pessoa2);
    }


    @Test
    void deveConsiderarPessoaIgualAPropriaInstancia() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        assertEquals(pessoa, pessoa);
    }


    @Test
    void deveRetornarFalsoAoCompararComObjetoDeOutroTipo() {

        Pessoa pessoa = criarPessoa(
                false, false, false, false,
                false, false, false
        );

        assertNotEquals(pessoa, "Pessoa Teste");
    }


    @Test
    void deveLancarExcecaoParaNomeNulo() {

        assertThrows(
                NullPointerException.class,
                () -> new Pessoa(
                        null,
                        Sexo.MASCULINO,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                )
        );
    }


    @Test
    void deveLancarExcecaoParaSexoNulo() {

        assertThrows(
                NullPointerException.class,
                () -> new Pessoa(
                        "Pessoa",
                        null,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        NivelLeitura.BASICO
                )
        );
    }


    @Test
    void deveLancarExcecaoParaPrivilegioNulo() {

        assertThrows(
                NullPointerException.class,
                () -> new Pessoa(
                        "Pessoa",
                        Sexo.MASCULINO,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        null,
                        NivelLeitura.BASICO
                )
        );
    }


    @Test
    void deveLancarExcecaoParaNivelLeituraNulo() {

        assertThrows(
                NullPointerException.class,
                () -> new Pessoa(
                        "Pessoa",
                        Sexo.MASCULINO,
                        true,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.PUBLICADOR,
                        null
                )
        );
    }


    private Pessoa criarPessoa(
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            boolean oracao,
            boolean presidente,
            boolean dirigente
    ) {

        return new Pessoa(
                "Pessoa Teste",
                Sexo.MASCULINO,
                true,
                responsavel,
                ajudante,
                leitura,
                discurso,
                oracao,
                presidente,
                dirigente,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );
    }

}