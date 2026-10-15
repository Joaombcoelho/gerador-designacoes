package br.com.geradordesignacoes.model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Item cronológico do S-140.
 * Uma duração nula indica que o horário dos itens seguintes
 * não pode ser inferido automaticamente até existir um ajuste manual.
 */
public record HorarioS140Item(
        String chave,
        String descricao,
        Integer duracaoMinutos,
        LocalTime horarioInicioManual
) {
    public HorarioS140Item {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("A chave do item é obrigatória.");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do item é obrigatória.");
        }
        Objects.requireNonNull(descricao);
        if (duracaoMinutos != null && duracaoMinutos <= 0) {
            throw new IllegalArgumentException("A duração deve ser maior que zero.");
        }
    }
}
