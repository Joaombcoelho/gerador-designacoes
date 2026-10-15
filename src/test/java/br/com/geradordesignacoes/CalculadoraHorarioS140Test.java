package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.HorarioS140Item;
import br.com.geradordesignacoes.service.CalculadoraHorarioS140;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraHorarioS140Test {

    private final CalculadoraHorarioS140 calculadora = new CalculadoraHorarioS140();

    @Test
    void deveCalcularInicioEFimEmSequencia() {
        List<HorarioS140Calculado> resultado = calculadora.calcular(
                LocalTime.of(19, 30),
                List.of(
                        new HorarioS140Item("abertura", "Cântico e oração iniciais", 5, null),
                        new HorarioS140Item("comentarios", "Comentários iniciais", 1, null),
                        new HorarioS140Item("tesouros", "Tesouros", 24, null)
                )
        );

        assertEquals(LocalTime.of(19, 30), resultado.get(0).horarioInicio());
        assertEquals(LocalTime.of(19, 35), resultado.get(0).horarioFim());
        assertEquals(LocalTime.of(19, 35), resultado.get(1).horarioInicio());
        assertEquals(LocalTime.of(19, 36), resultado.get(1).horarioFim());
        assertEquals(LocalTime.of(19, 36), resultado.get(2).horarioInicio());
        assertEquals(LocalTime.of(20, 0), resultado.get(2).horarioFim());
        assertFalse(resultado.get(0).horarioManual());
    }

    @Test
    void devePreservarAjusteManualPosteriorAoAlterarDuracaoAnterior() {
        List<HorarioS140Item> itens = List.of(
                new HorarioS140Item("abertura", "Abertura", 5, null),
                new HorarioS140Item("tesouros", "Tesouros", 24, null),
                new HorarioS140Item("ministerio", "Ministério", 18, LocalTime.of(20, 15)),
                new HorarioS140Item("vida", "Nossa Vida Cristã", 15, null)
        );

        List<HorarioS140Calculado> resultado = calculadora.calcular(
                LocalTime.of(19, 30), itens);

        assertEquals(LocalTime.of(19, 35), resultado.get(1).horarioInicio());
        assertEquals(LocalTime.of(20, 15), resultado.get(2).horarioInicio());
        assertEquals(LocalTime.of(20, 33), resultado.get(2).horarioFim());
        assertEquals(LocalTime.of(20, 33), resultado.get(3).horarioInicio());
        assertTrue(resultado.get(2).horarioManual());
    }

    @Test
    void deveRetomarCalculoDepoisDeUmaDuracaoPendenteQuandoHouverAjusteManual() {
        List<HorarioS140Calculado> resultado = calculadora.calcular(
                LocalTime.of(19, 30),
                List.of(
                        new HorarioS140Item("a", "Parte com duração conhecida", 5, null),
                        new HorarioS140Item("b", "Parte pendente", null, null),
                        new HorarioS140Item("c", "Ajuste manual", 10, LocalTime.of(20, 0)),
                        new HorarioS140Item("d", "Parte seguinte", 5, null)
                )
        );

        assertEquals(LocalTime.of(19, 35), resultado.get(1).horarioInicio());
        assertNull(resultado.get(1).horarioFim());
        assertEquals(LocalTime.of(20, 0), resultado.get(2).horarioInicio());
        assertEquals(LocalTime.of(20, 10), resultado.get(3).horarioInicio());
    }

    @Test
    void deveRejeitarDuracaoNaoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new HorarioS140Item("x", "Inválido", 0, null));
    }

    @Test
    void deveRejeitarInicioNulo() {
        assertThrows(NullPointerException.class,
                () -> calculadora.calcular(null, List.of()));
    }
}
