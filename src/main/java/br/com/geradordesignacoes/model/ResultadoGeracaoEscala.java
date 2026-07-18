package br.com.geradordesignacoes.model;

import java.util.List;

public class ResultadoGeracaoEscala {

    private final List<Designacao> designacoes;
    private final List<ParticipacaoDesignacao> participacoes;
    private final List<String> erros;


    public ResultadoGeracaoEscala(
            List<Designacao> designacoes,
            List<ParticipacaoDesignacao> participacoes,
            List<String> erros
    ) {

        this.designacoes = designacoes;
        this.participacoes = participacoes;
        this.erros = erros;
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


    public boolean possuiErros() {

        return !erros.isEmpty();
    }
}