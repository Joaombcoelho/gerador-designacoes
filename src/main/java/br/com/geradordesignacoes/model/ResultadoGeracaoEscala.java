package br.com.geradordesignacoes.model;

import java.util.List;

public class ResultadoGeracaoEscala {

    private final List<Designacao> designacoes;
    private final List<ParticipacaoDesignacao> participacoes;
    private final List<String> erros;
    private final List<DiagnosticoSelecaoPessoa> diagnosticos;


    public ResultadoGeracaoEscala(
            List<Designacao> designacoes,
            List<ParticipacaoDesignacao> participacoes,
            List<String> erros,
            List<DiagnosticoSelecaoPessoa> diagnosticos
    ) {

        this.designacoes = designacoes;
        this.participacoes = participacoes;
        this.erros = erros;
        this.diagnosticos = diagnosticos;
    }


    public List<Designacao> getDesignacoes() {

        return designacoes;
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