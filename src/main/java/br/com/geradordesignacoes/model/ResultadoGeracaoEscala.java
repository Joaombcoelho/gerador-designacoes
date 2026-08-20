package br.com.geradordesignacoes.model;

import java.util.List;
import java.util.Objects;

public record ResultadoGeracaoEscala(Escala escala, List<ParticipacaoDesignacao> participacoes, List<String> erros,
                                     List<DiagnosticoSelecaoPessoa> diagnosticos) {

    public ResultadoGeracaoEscala(
            Escala escala,
            List<ParticipacaoDesignacao> participacoes,
            List<String> erros,
            List<DiagnosticoSelecaoPessoa> diagnosticos
    ) {

        this.escala = Objects.requireNonNull(
                escala,
                "A escala não pode ser nula."
        );

        this.participacoes = List.copyOf(participacoes);
        this.erros = List.copyOf(erros);
        this.diagnosticos = List.copyOf(diagnosticos);
    }


    /**
     * Mantido temporariamente para compatibilidade.
     * Será removido após a migração completa.
     */
    @Deprecated
    public List<Designacao> getDesignacoes() {
        return escala.getDesignacoes();
    }


    public boolean possuiErros() {
        return !erros.isEmpty();
    }
}