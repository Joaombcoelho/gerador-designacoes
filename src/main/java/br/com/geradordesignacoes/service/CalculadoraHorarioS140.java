package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.HorarioS140Item;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Calcula os horários em ordem cronológica.
 *
 * Um horário manual é tratado como ponto de referência: ele prevalece
 * sobre o cálculo anterior e o cálculo prossegue a partir dele.
 * Assim, ajustes manuais posteriores são preservados quando um item
 * anterior muda de duração.
 */
public class CalculadoraHorarioS140 {

    public List<HorarioS140Calculado> calcular(
            LocalTime horarioInicioReuniao,
            List<HorarioS140Item> itens
    ) {
        Objects.requireNonNull(horarioInicioReuniao,
                "O horário de início da reunião é obrigatório.");
        Objects.requireNonNull(itens, "A lista de itens é obrigatória.");

        List<HorarioS140Calculado> resultado = new ArrayList<>(itens.size());
        LocalTime proximoInicio = horarioInicioReuniao;

        for (HorarioS140Item item : itens) {
            Objects.requireNonNull(item, "A lista não pode conter itens nulos.");

            boolean manual = item.horarioInicioManual() != null;
            LocalTime inicio = manual ? item.horarioInicioManual() : proximoInicio;
            LocalTime fim = inicio != null && item.duracaoMinutos() != null
                    ? inicio.plusMinutes(item.duracaoMinutos())
                    : null;

            resultado.add(new HorarioS140Calculado(
                    item.chave(),
                    item.descricao(),
                    item.duracaoMinutos(),
                    inicio,
                    fim,
                    manual
            ));

            // Uma duração desconhecida interrompe a sequência automática.
            // O cálculo só é retomado se um item posterior tiver horário manual.
            proximoInicio = fim;
        }

        return List.copyOf(resultado);
    }
}
