
package br.com.geradordesignacoes.model;

import java.util.List;
import java.util.Objects;

public record ResultadoGeracaoSalasCoordenadas(
        ResultadoGeracaoEscala resultadoPrincipal,
        List<Designacao> designacoesAuxiliares,
        List<ParticipacaoDesignacao> participacoesDaData,
        List<String> erros
) {

    public ResultadoGeracaoSalasCoordenadas {
        Objects.requireNonNull(
                resultadoPrincipal,
                "O resultado da Sala Principal não pode ser nulo."
        );

        designacoesAuxiliares = List.copyOf(
                Objects.requireNonNull(
                        designacoesAuxiliares,
                        "As designações auxiliares não podem ser nulas."
                )
        );

        participacoesDaData = List.copyOf(
                Objects.requireNonNull(
                        participacoesDaData,
                        "As participações não podem ser nulas."
                )
        );

        erros = List.copyOf(
                Objects.requireNonNull(
                        erros,
                        "A lista de erros não pode ser nula."
                )
        );
    }

    public boolean possuiErros() {
        return !erros.isEmpty();
    }
}