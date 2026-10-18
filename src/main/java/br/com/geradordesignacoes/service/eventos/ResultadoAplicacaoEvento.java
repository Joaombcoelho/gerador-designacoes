package br.com.geradordesignacoes.service.eventos;

import java.time.LocalDate;
import java.util.Objects;

public record ResultadoAplicacaoEvento(
        Situacao situacao,
        long idAplicacao,
        LocalDate dataReuniao,
        EstadoAplicacaoEvento estado
) {
    public ResultadoAplicacaoEvento {
        Objects.requireNonNull(situacao);
        Objects.requireNonNull(dataReuniao);
        Objects.requireNonNull(estado);
        if (idAplicacao <= 0) {
            throw new IllegalArgumentException("O ID da aplicação deve ser positivo.");
        }
    }

    public enum Situacao {
        APLICADA,
        JA_APLICADA
    }
}