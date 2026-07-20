package br.com.geradordesignacoes.model;

import java.util.ArrayList;
import java.util.List;

public class HistoricoDesignacoes {

    private final List<ParticipacaoDesignacao> participacoes;


    public HistoricoDesignacoes() {

        this.participacoes = new ArrayList<>();
    }


    public HistoricoDesignacoes(
            List<ParticipacaoDesignacao> participacoes
    ) {

        this.participacoes =
                new ArrayList<>(participacoes);
    }


    public void adicionar(
            ParticipacaoDesignacao participacao
    ) {

        participacoes.add(
                participacao
        );
    }


    public List<ParticipacaoDesignacao> getParticipacoes() {

        return new ArrayList<>(
                participacoes
        );
    }


    public long quantidadeVezesNaParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .filter(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                && participacao.getParte().equals(parte)
                )
                .count();
    }

    public boolean jaParticipou(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .anyMatch(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                && participacao.getParte().equals(parte)
                );
    }

}