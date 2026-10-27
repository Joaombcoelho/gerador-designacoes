package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.HabilidadeDesignacaoCodigo;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PessoaHabilidadesTest {

    @Test
    void deveArmazenarHabilidadesIndependentesDasCategorias() {
        Pessoa pessoa = new Pessoa(
                "Pessoa de teste", Sexo.MASCULINO, true,
                true, true, true, true, true, true, true,
                Privilegio.SERVO_MINISTERIAL,
                br.com.geradordesignacoes.model.NivelLeitura.BASICO);

        pessoa.setHabilidadesDesignacao(Set.of(
                HabilidadeDesignacaoCodigo.TESOUROS.codigo(),
                HabilidadeDesignacaoCodigo.DISCURSO.codigo()));

        assertTrue(pessoa.possuiHabilidade("TESOUROS"));
        assertTrue(pessoa.possuiHabilidade("DISCURSO"));
        assertFalse(pessoa.possuiHabilidade("PRESIDENCIA"));
        assertEquals(Privilegio.SERVO_MINISTERIAL, pessoa.getPrivilegio());
    }

    @Test
    void deveManterConjuntoImutavelETratarAusenciaComoSemHabilidades() {
        Pessoa pessoa = new Pessoa(
                "Pessoa de teste", Sexo.MASCULINO, true,
                true, true, true, true, Privilegio.PUBLICADOR);

        assertTrue(pessoa.getHabilidadesDesignacao().isEmpty());
        assertFalse(pessoa.possuiHabilidade("DISCURSO"));
        pessoa.setHabilidadesDesignacao(Set.of("DISCURSO"));
        assertThrows(UnsupportedOperationException.class,
                () -> pessoa.getHabilidadesDesignacao().add("TESOUROS"));
    }
}
