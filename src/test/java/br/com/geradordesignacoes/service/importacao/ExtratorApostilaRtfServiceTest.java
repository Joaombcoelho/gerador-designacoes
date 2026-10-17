package br.com.geradordesignacoes.service.importacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtratorApostilaRtfServiceTest {

    private final ExtratorApostilaRtfService service = new ExtratorApostilaRtfService();

    @TempDir
    Path pastaTemporaria;

    @Test
    void rejeitaPastaSemArquivosRtf() {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> service.extrair(pastaTemporaria)
        );

        assertTrue(erro.getMessage().contains("Nenhum arquivo .rtf"));
    }

    @Test
    void rejeitaCaminhoQueNaoEhPasta() {
        Path inexistente = pastaTemporaria.resolve("pasta-inexistente");

        assertThrows(IllegalArgumentException.class, () -> service.extrair(inexistente));
    }
}
