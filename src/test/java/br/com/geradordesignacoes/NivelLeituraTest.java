package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.NivelLeitura;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class NivelLeituraTest {

    @Test
    void devePermitirNivelBasicoParaExigenciaBasica() {

        assertTrue(
                NivelLeitura.BASICO.atende(
                        NivelLeitura.BASICO
                )
        );
    }

    @Test
    void devePermitirNivelExperienteParaExigenciaExperiente() {

        assertTrue(
                NivelLeitura.EXPERIENTE.atende(
                        NivelLeitura.EXPERIENTE
                )
        );
    }

    @Test
    void devePermitirNivelExperienteParaExigenciaBasica() {

        assertTrue(
                NivelLeitura.EXPERIENTE.atende(
                        NivelLeitura.BASICO
                )
        );
    }

    @Test
    void naoDevePermitirNivelBasicoParaExigenciaExperiente() {

        assertFalse(
                NivelLeitura.BASICO.atende(
                        NivelLeitura.EXPERIENTE
                )
        );
    }

    @Test
    void naoDevePermitirQuandoNivelExigidoForNulo() {

        assertFalse(
                NivelLeitura.BASICO.atende(null)
        );

        assertFalse(
                NivelLeitura.EXPERIENTE.atende(null)
        );
    }
}