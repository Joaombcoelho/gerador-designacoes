package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CoordenadorGeracaoSalasService {

    private final GeradorEscala geradorEscala;
    private final GeradorSalasAuxiliaresService geradorSalasAuxiliares;
    private final ConfiguracaoSalasService configuracaoSalasService;
    private final HistoricoDesignacoesService historicoService;

    public CoordenadorGeracaoSalasService(
            GeradorEscala geradorEscala,
            GeradorSalasAuxiliaresService geradorSalasAuxiliares,
            ConfiguracaoSalasService configuracaoSalasService,
            HistoricoDesignacoesService historicoService
    ) {
        this.geradorEscala = Objects.requireNonNull(geradorEscala);
        this.geradorSalasAuxiliares =
                Objects.requireNonNull(geradorSalasAuxiliares);
        this.configuracaoSalasService =
                Objects.requireNonNull(configuracaoSalasService);
        this.historicoService =
                Objects.requireNonNull(historicoService);
    }

    /**
     * Gera as designações e persiste o histórico da data.
     * Mantém o comportamento utilizado pela geração individual.
     */
    public ResultadoGeracaoSalasCoordenadas gerar(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas
    ) {
        return gerarInterno(
                data,
                partes,
                pessoas,
                historicoService.getHistorico(),
                true
        );
    }

    /**
     * Gera as designações sem persistir o histórico.
     *
     * Permite que outra camada, como a geração mensal,
     * controle quando as participações serão salvas.
     *
     * O histórico recebido pode incluir participações
     * temporárias das semanas já geradas no mesmo mês.
     */
    public ResultadoGeracaoSalasCoordenadas gerarSemPersistir(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas,
            HistoricoDesignacoes historico
    ) {
        return gerarInterno(
                data,
                partes,
                pessoas,
                historico,
                false
        );
    }

    private ResultadoGeracaoSalasCoordenadas gerarInterno(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas,
            HistoricoDesignacoes historico,
            boolean persistirHistorico
    ) {
        Objects.requireNonNull(
                data,
                "A data não pode ser nula."
        );
        Objects.requireNonNull(
                partes,
                "As partes não podem ser nulas."
        );
        Objects.requireNonNull(
                pessoas,
                "As pessoas não podem ser nulas."
        );
        Objects.requireNonNull(
                historico,
                "O histórico não pode ser nulo."
        );

        /*
         * Remove do histórico usado na seleção somente
         * as participações da data que está sendo gerada.
         *
         * As demais datas são preservadas.
         */
        HistoricoDesignacoes historicoSemData =
                historico.semParticipacoesDaData(data);

        // Gera a Sala Principal sem persistência antecipada.
        ResultadoGeracaoEscala resultadoPrincipal =
                geradorEscala.gerarSemPersistir(
                        data,
                        partes,
                        pessoas,
                        historicoSemData
                );

        ConfiguracaoSalas configuracao =
                Objects.requireNonNull(
                        configuracaoSalasService.buscar(),
                        "A configuração das salas não pode ser nula."
                );

        // Gera somente as salas auxiliares habilitadas.
        ResultadoGeracaoSalasAuxiliares resultadoAuxiliar =
                geradorSalasAuxiliares.gerar(
                        data,
                        partes,
                        pessoas,
                        resultadoPrincipal.escala().getDesignacoes(),
                        configuracao.salaBHabilitada(),
                        configuracao.salaCHabilitada()
                );

        List<ParticipacaoDesignacao> participacoes =
                new ArrayList<>();

        /*
         * Mantém somente as novas participações da
         * Sala Principal correspondentes à data gerada.
         */
        resultadoPrincipal.participacoes().stream()
                .filter(p -> p.data().equals(data))
                .filter(p -> p.sala() == SalaDesignacao.PRINCIPAL)
                .forEach(participacoes::add);

        // Converte as designações auxiliares para o histórico.
        for (Designacao designacao :
                resultadoAuxiliar.designacoes()) {
            adicionarParticipacoesAuxiliares(
                    designacao,
                    participacoes
            );
        }

        List<String> erros = new ArrayList<>();
        erros.addAll(resultadoPrincipal.erros());
        erros.addAll(resultadoAuxiliar.erros());

        /*
         * Persiste somente quando o fluxo solicitou.
         * Na geração mensal, as participações permanecem
         * em memória até a confirmação do salvamento.
         */
        if (persistirHistorico) {
            historicoService.salvarGeracao(
                    data,
                    participacoes
            );
        }

        return new ResultadoGeracaoSalasCoordenadas(
                resultadoPrincipal,
                resultadoAuxiliar.designacoes(),
                participacoes,
                erros
        );
    }

    private void adicionarParticipacoesAuxiliares(
            Designacao designacao,
            List<ParticipacaoDesignacao> destino
    ) {
        TipoParte tipoParte = designacao.parte().getTipo();

        switch (tipoParte) {
            case LEITURA -> adicionarSeExistir(
                    designacao,
                    designacao.responsavel(),
                    TipoParticipacao.LEITOR,
                    destino
            );

            case DISCURSO -> adicionarSeExistir(
                    designacao,
                    designacao.responsavel(),
                    TipoParticipacao.ORADOR,
                    destino
            );

            case DEMONSTRACAO -> {
                adicionarSeExistir(
                        designacao,
                        designacao.responsavel(),
                        TipoParticipacao.RESPONSAVEL,
                        destino
                );

                adicionarSeExistir(
                        designacao,
                        designacao.ajudante(),
                        TipoParticipacao.AJUDANTE,
                        destino
                );
            }

            default -> {
                // O gerador auxiliar não cria outras modalidades.
            }
        }
    }

    private void adicionarSeExistir(
            Designacao designacao,
            Pessoa pessoa,
            TipoParticipacao tipo,
            List<ParticipacaoDesignacao> destino
    ) {
        if (pessoa == null) {
            return;
        }

        destino.add(
                new ParticipacaoDesignacao(
                        designacao.data(),
                        pessoa,
                        designacao.parte(),
                        tipo,
                        designacao.sala()
                )
        );
    }
}