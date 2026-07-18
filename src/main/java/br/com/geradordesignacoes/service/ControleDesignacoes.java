package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Pessoa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControleDesignacoes {

    private final Map<Pessoa, Integer> quantidadePorPessoa;

    private final List<Pessoa> pessoasDesignadas;

    private final List<ParticipacaoDesignacao> participacoes;


    public ControleDesignacoes() {

        this.quantidadePorPessoa = new HashMap<>();

        this.pessoasDesignadas = new ArrayList<>();

        this.participacoes = new ArrayList<>();
    }


    public ControleDesignacoes(
            List<ParticipacaoDesignacao> historico
    ) {

        this();

        if (historico != null) {

            historico.forEach(
                    this::registrarParticipacao
            );
        }
    }


    public void registrar(Pessoa pessoa) {

        quantidadePorPessoa.merge(
                pessoa,
                1,
                Integer::sum
        );

        pessoasDesignadas.add(pessoa);
    }


    public void registrarParticipacao(
            ParticipacaoDesignacao participacao
    ) {

        participacoes.add(
                participacao
        );

        registrar(
                participacao.getPessoa()
        );
    }


    public int quantidadeDe(Pessoa pessoa) {

        return quantidadePorPessoa.getOrDefault(
                pessoa,
                0
        );
    }


    public List<Pessoa> getPessoasDesignadas() {

        return new ArrayList<>(
                pessoasDesignadas
        );
    }


    public List<ParticipacaoDesignacao> getParticipacoes() {

        return new ArrayList<>(
                participacoes
        );
    }


    public boolean jaFezParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return participacoes.stream()
                .anyMatch(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                && participacao.getParte().equals(parte)
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
}