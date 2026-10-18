package br.com.geradordesignacoes.service.importacao;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfirmacaoImportacaoApostilaServiceTest {

    @Test
    void deveRecusarPreviaComPendenciasAntesDeAcessarPersistencia() {
        var previa = new PreviaImportacaoApostilaService.PreviaImportacao(
                List.of(),
                List.of("Mapeamento incompleto"),
                List.of()
        );
        var service = new ConfirmacaoImportacaoApostilaService();

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.importar(previa)
        );

        assertTrue(erro.getMessage().contains("prévia contém erros"));
    }

    @Test
    void deveRecusarPreviaNula() {
        var service = new ConfirmacaoImportacaoApostilaService();

        assertThrows(NullPointerException.class, () -> service.importar(null));
    }
}
