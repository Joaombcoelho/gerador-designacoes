package br.com.geradordesignacoes.model;

import java.util.List;
import java.util.Objects;

public record ResultadoGeracaoSalasAuxiliares(
        List<Designacao> designacoes,
        List<String> erros
) {

    public ResultadoGeracaoSalasAuxiliares {
        designacoes = List.copyOf(
                Objects.requireNonNull(
                        designacoes,
                        "A lista de designações não pode ser nula."
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

    public boolean foiGeradaComSucesso() {
        return erros.isEmpty();
    }
}