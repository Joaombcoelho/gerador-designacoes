package br.com.geradordesignacoes.model;

import java.time.LocalTime;
import java.util.Objects;

public record HorarioS140Calculado(
        String chave,
        String descricao,
        Integer duracaoMinutos,
        LocalTime horarioInicio,
        LocalTime horarioFim,
        boolean horarioManual
) {
    public HorarioS140Calculado {
        Objects.requireNonNull(chave);
        Objects.requireNonNull(descricao);
        if (duracaoMinutos != null && duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duração deve ser maior que zero.");
        }
        if (horarioFim != null && horarioInicio == null) {
            throw new IllegalArgumentException("Não pode haver horário final sem horário inicial.");
        }
    }

    public boolean possuiHorarioCalculado() {
        return horarioInicio != null && horarioFim != null;
    }
}
