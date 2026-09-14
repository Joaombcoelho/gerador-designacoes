package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParticipacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControleDesignacoes {

    private final Map<Pessoa, Integer> quantidadePorPessoa;
    private final List<Pessoa> pessoasDesignadas;
    private final HistoricoDesignacoes historico;

    private Pessoa presidente;

    public ControleDesignacoes() {
        quantidadePorPessoa = new HashMap<>();
        pessoasDesignadas = new ArrayList<>();
        historico = new HistoricoDesignacoes();
    }

    public ControleDesignacoes(
            HistoricoDesignacoes historico
    ) {
        quantidadePorPessoa = new HashMap<>();
        pessoasDesignadas = new ArrayList<>();
        this.historico =
                historico != null
                        ? historico
                        : new HistoricoDesignacoes();

        for (ParticipacaoDesignacao participacao :
                this.historico.participacoes()) {

            registrarHistorico(participacao);
        }
    }

    public ControleDesignacoes(
            List<ParticipacaoDesignacao> historico
    ) {
        this(
                new HistoricoDesignacoes(historico)
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

        adicionarPessoaSeNecessario(pessoa);
    }

    /**
     * Registra uma nova participação gerada.
     */
    public void registrarParticipacao(
            ParticipacaoDesignacao participacao
    ) {
        historico.adicionar(participacao);

        registrar(
                participacao.pessoa()
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
                participacao.pessoa(),
                1,
                Integer::sum
        );
    }

    /**
     * Adiciona uma pessoa à lista da geração atual
     * somente se ela ainda não estiver presente.
     */
    private void adicionarPessoaSeNecessario(
            Pessoa pessoa
    ) {
        if (!pessoasDesignadas.contains(pessoa)) {
            pessoasDesignadas.add(pessoa);
        }
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
        return historico.participacoes();
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
        return participacoesDaParte(
                pessoa,
                parte
        ).count();
    }

    public LocalDate ultimaParticipacaoNaParte(
            Pessoa pessoa,
            Parte parte
    ) {
        return participacoesDaParte(
                pessoa,
                parte
        )
                .map(ParticipacaoDesignacao::data)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * Retorna as participações de uma pessoa
     * em uma determinada parte.
     */
    private java.util.stream.Stream<ParticipacaoDesignacao>
    participacoesDaParte(
            Pessoa pessoa,
            Parte parte
    ) {
        return historico.participacoes()
                .stream()
                .filter(
                        participacao ->
                                participacao.pessoa().equals(pessoa)
                                        && participacao.parte().equals(parte)
                );
    }

    public long quantidadeVezesNaParticipacao(
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {
        return historico.quantidadeVezesNaParticipacao(
                pessoa,
                parte,
                tipoParticipacao
        );
    }

    public void definirPresidente(
            Pessoa presidente
    ) {
        this.presidente = presidente;
    }

    public boolean ehPresidente(
            Pessoa pessoa
    ) {
        return presidente != null
                && presidente.equals(pessoa);
    }
}