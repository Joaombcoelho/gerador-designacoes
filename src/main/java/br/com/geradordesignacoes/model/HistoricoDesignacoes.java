package br.com.geradordesignacoes.model;

import java.util.ArrayList;
import java.time.LocalDate;
import java.util.List;
import java.time.LocalDate;

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


    public HistoricoDesignacoes semParticipacoesDaData(
            LocalDate data
    ) {

        HistoricoDesignacoes historicoFiltrado =
                new HistoricoDesignacoes();

        participacoes.stream()
                .filter(participacao ->
                        !participacao.getData().equals(data)
                )
                .forEach(historicoFiltrado::adicionar);

        return historicoFiltrado;
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

    public long quantidadeVezesNaParticipacao(
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {

        return participacoes.stream()
                .filter(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                &&
                                participacao.getParte().equals(parte)
                                &&
                                participacao.getTipoParticipacao() == tipoParticipacao
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
    public LocalDate ultimaParticipacaoNaParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .filter(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                &&
                                participacao.getParte().equals(parte)
                )
                .map(ParticipacaoDesignacao::getData)
                .max(LocalDate::compareTo)
                .orElse(null);
    }
}
