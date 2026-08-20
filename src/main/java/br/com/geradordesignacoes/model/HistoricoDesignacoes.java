package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public record HistoricoDesignacoes(List<ParticipacaoDesignacao> participacoes) {

    public HistoricoDesignacoes() {

        this(new ArrayList<>());
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


    @Override
    public List<ParticipacaoDesignacao> participacoes() {

        return new ArrayList<>(
                participacoes
        );
    }


    /**
     * Retorna um novo histórico sem as participações
     * realizadas na data informada.
     * O histórico original não é alterado.
     */
    public HistoricoDesignacoes semParticipacoesDaData(
            LocalDate data
    ) {

        HistoricoDesignacoes historicoFiltrado =
                new HistoricoDesignacoes();

        participacoes.stream()
                .filter(participacao ->
                        !participacao.data().equals(data)
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
                        participacao.pessoa().equals(pessoa)
                                &&
                                participacao.parte().equals(parte)
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
                        participacao.pessoa().equals(pessoa)
                                &&
                                participacao.parte().equals(parte)
                                &&
                                participacao.tipoParticipacao() == tipoParticipacao
                )
                .count();
    }


    public boolean jaParticipou(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .anyMatch(participacao ->
                        participacao.pessoa().equals(pessoa)
                                &&
                                participacao.parte().equals(parte)
                );
    }


    public LocalDate ultimaParticipacaoNaParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .filter(participacao ->
                        participacao.pessoa().equals(pessoa)
                                &&
                                participacao.parte().equals(parte)
                )
                .map(ParticipacaoDesignacao::data)
                .max(LocalDate::compareTo)
                .orElse(null);
    }
}