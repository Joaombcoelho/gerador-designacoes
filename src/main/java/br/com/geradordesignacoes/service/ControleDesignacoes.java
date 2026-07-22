package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.HistoricoDesignacoes;
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

    private final HistoricoDesignacoes historico;


    public ControleDesignacoes() {

        this.quantidadePorPessoa =
                new HashMap<>();

        this.pessoasDesignadas =
                new ArrayList<>();

        this.historico =
                new HistoricoDesignacoes();
    }


    public ControleDesignacoes(
            HistoricoDesignacoes historico
    ) {

        this.quantidadePorPessoa =
                new HashMap<>();

        this.pessoasDesignadas =
                new ArrayList<>();

        this.historico =
                historico != null
                        ? historico
                        : new HistoricoDesignacoes();


        /*
         * Carrega somente dados estatísticos
         * do histórico.
         *
         * Não adiciona pessoas em
         * pessoasDesignadas, pois elas
         * pertencem apenas à geração atual.
         */
        for (ParticipacaoDesignacao participacao :
                this.historico.getParticipacoes()) {

            registrarHistorico(
                    participacao
            );
        }
    }


    public ControleDesignacoes(
            List<ParticipacaoDesignacao> historico
    ) {

        this(
                new HistoricoDesignacoes(
                        historico
                )
        );
    }


    /**
     * Registra uma pessoa na geração atual.
     */
    public void registrar(
            Pessoa pessoa
    ) {

        quantidadePorPessoa.merge(
                pessoa,
                1,
                Integer::sum
        );


        if (!pessoasDesignadas.contains(pessoa)) {

            pessoasDesignadas.add(
                    pessoa
            );
        }
    }


    /**
     * Registra uma nova participação gerada.
     */
    public void registrarParticipacao(
            ParticipacaoDesignacao participacao
    ) {

        historico.adicionar(
                participacao
        );


        registrar(
                participacao.getPessoa()
        );
    }


    /**
     * Carrega participações antigas somente
     * para cálculo e histórico.
     *
     * Não bloqueia a pessoa na escala atual.
     */
    private void registrarHistorico(
            ParticipacaoDesignacao participacao
    ) {

        quantidadePorPessoa.merge(
                participacao.getPessoa(),
                1,
                Integer::sum
        );
    }


    public int quantidadeDe(
            Pessoa pessoa
    ) {

        return quantidadePorPessoa.getOrDefault(
                pessoa,
                0
        );
    }


    /**
     * Pessoas que já receberam alguma parte
     * nesta geração atual.
     */
    public List<Pessoa> getPessoasDesignadas() {

        return new ArrayList<>(
                pessoasDesignadas
        );
    }


    /**
     * Histórico completo:
     * histórico recebido + novas participações.
     */
    public List<ParticipacaoDesignacao> getParticipacoes() {

        return historico.getParticipacoes();
    }


    public boolean jaFezParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return historico.jaParticipou(
                pessoa,
                parte
        );
    }


    public long quantidadeVezesNaParte(
            Pessoa pessoa,
            Parte parte
    ) {

        return historico.getParticipacoes()
                .stream()
                .filter(participacao ->
                        participacao.getPessoa().equals(pessoa)
                                &&
                                participacao.getParte().equals(parte)
                )
                .count();
    }
}