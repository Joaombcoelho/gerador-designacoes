package br.com.geradordesignacoes.model;

import java.util.List;
import java.util.Objects;

public class ResultadoGeracaoEscala {

    private final Escala escala;

    private final List<ParticipacaoDesignacao> participacoes;

    private final List<String> erros;

    private final List<DiagnosticoSelecaoPessoa> diagnosticos;


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


    public Escala getEscala() {
        return escala;
    }


    /**
     * Mantido temporariamente para compatibilidade.
     * Será removido após a migração completa.
     */
    @Deprecated
    public List<Designacao> getDesignacoes() {
        return escala.getDesignacoes();
    }


    public List<ParticipacaoDesignacao> getParticipacoes() {
        return participacoes;
    }


    public List<String> getErros() {
        return erros;
    }


    public List<DiagnosticoSelecaoPessoa> getDiagnosticos() {
        return diagnosticos;
    }


    public boolean possuiErros() {
        return !erros.isEmpty();
    }
}