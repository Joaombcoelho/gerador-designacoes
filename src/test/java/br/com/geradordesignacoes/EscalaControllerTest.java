package br.com.geradordesignacoes;

import br.com.geradordesignacoes.controller.EscalaController;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.view.escala.EscalaView;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EscalaControllerTest {

    @BeforeAll
    static void inicializarJavaFX() {

        Platform.startup(() -> {
        });
    }


    @Test
    void deveRejeitarResponsavelNulo() {

        EscalaView view =
                new EscalaView();

        EscalaController controller =
                new EscalaController(view);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.substituirDesignacao(
                                0,
                                null,
                                null
                        )
                );

        assertEquals(
                "O novo responsável não pode ser nulo.",
                excecao.getMessage()
        );
    }
    @Test
    void deveRejeitarSubstituicaoSemEscalaGerada() {

        EscalaView view =
                new EscalaView();

        EscalaController controller =
                new EscalaController(view);

        Pessoa responsavel =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        false,
                        false,
                        false,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        IllegalStateException excecao =
                assertThrows(
                        IllegalStateException.class,
                        () -> controller.substituirDesignacao(
                                0,
                                responsavel,
                                null
                        )
                );

        assertEquals(
                "Nenhuma escala foi gerada.",
                excecao.getMessage()
        );
    }
}