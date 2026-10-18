package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Representação imutável de um evento especial, ainda sem persistência.
 * O intervalo de datas é inclusivo.
 */
public record EventoEspecial(
        TipoEventoEspecial tipo,
        LocalDate dataInicio,
        LocalDate dataFim,
        String temaDiscurso,
        String descricao
) {
    public EventoEspecial {
        Objects.requireNonNull(tipo, "O tipo do evento é obrigatório.");
        Objects.requireNonNull(dataInicio, "A data inicial do evento é obrigatória.");
        Objects.requireNonNull(dataFim, "A data final do evento é obrigatória.");

        if (dataFim.isBefore(dataInicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        temaDiscurso = normalizarOpcional(temaDiscurso);
        descricao = normalizarOpcional(descricao);

        if (temaDiscurso != null && tipo != TipoEventoEspecial.VISITA_SUPERINTENDENTE) {
            throw new IllegalArgumentException(
                    "O tema do discurso só pode ser informado para a visita do superintendente.");
        }
    }

    public boolean ocorreEm(LocalDate data) {
        Objects.requireNonNull(data, "A data consultada é obrigatória.");
        return !data.isBefore(dataInicio) && !data.isAfter(dataFim);
    }

    /**
     * Informa se o intervalo inclusivo do evento toca ao menos um dia
     * do intervalo informado.
     */
    public boolean sobrepoeIntervalo(LocalDate inicio, LocalDate fim) {
        Objects.requireNonNull(inicio, "O início do intervalo é obrigatório.");
        Objects.requireNonNull(fim, "O fim do intervalo é obrigatório.");
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("O fim do intervalo não pode anteceder o início.");
        }
        return !dataFim.isBefore(inicio) && !dataInicio.isAfter(fim);
    }

    private static String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
