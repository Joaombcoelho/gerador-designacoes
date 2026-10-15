package br.com.geradordesignacoes.view.pessoa;

import br.com.geradordesignacoes.model.Privilegio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PessoaFormularioViewTest {

    @Test
    void anciaoPodeSerDirigenteDoEstudoBiblico() {
        assertTrue(PessoaFormularioView.privilegioPodeDirigirEstudo(Privilegio.ANCIAO));
    }

    @Test
    void servoMinisterialPodeSerDirigenteDoEstudoBiblico() {
        assertTrue(PessoaFormularioView.privilegioPodeDirigirEstudo(Privilegio.SERVO_MINISTERIAL));
    }

    @Test
    void batizadoComumNaoPodeSerDirigenteDoEstudoBiblico() {
        assertFalse(PessoaFormularioView.privilegioPodeDirigirEstudo(Privilegio.BATIZADO));
    }

    @Test
    void privilegioNuloNaoPodeSerDirigenteDoEstudoBiblico() {
        assertFalse(PessoaFormularioView.privilegioPodeDirigirEstudo(null));
    }
}
