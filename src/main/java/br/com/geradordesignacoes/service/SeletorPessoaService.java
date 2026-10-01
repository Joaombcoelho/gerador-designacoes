package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.DiagnosticoSelecaoPessoa;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoAvaliacaoPessoa;
import br.com.geradordesignacoes.model.TipoParticipacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SeletorPessoaService {

    private final RegrasService regrasService;
    private final AvaliadorPessoaService avaliadorPessoaService;

    public SeletorPessoaService(
            RegrasService regrasService,
            AvaliadorPessoaService avaliadorPessoaService
    ) {
        this.regrasService = regrasService;
        this.avaliadorPessoaService = avaliadorPessoaService;
    }

    public Optional<Pessoa> selecionarMelhorPessoa(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        return selecionarMelhorPessoa(
                parte,
                pessoas,
                controle,
                null
        );
    }

    public Optional<Pessoa> selecionarMelhorPessoa(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle,
            LocalDate data
    ) {

        List<ResultadoAvaliacaoPessoa> candidatos =
                avaliarPessoasElegiveis(
                        parte,
                        pessoas,
                        controle,
                        data
                );

        return candidatos.stream()
                .max(
                        Comparator.comparingInt(
                                ResultadoAvaliacaoPessoa::getTotal
                        )
                )
                .map(
                        ResultadoAvaliacaoPessoa::getPessoa
                );
    }

    public List<ResultadoAvaliacaoPessoa> avaliarCandidatos(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        return avaliarCandidatos(
                parte,
                pessoas,
                controle,
                null
        );
    }

    public List<ResultadoAvaliacaoPessoa> avaliarCandidatos(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle,
            LocalDate data
    ) {

        return avaliarPessoasElegiveis(
                parte,
                pessoas,
                controle,
                data
        );
    }

    public DiagnosticoSelecaoPessoa selecionarComDiagnostico(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        return selecionarComDiagnostico(
                parte,
                pessoas,
                controle,
                null
        );
    }

    public DiagnosticoSelecaoPessoa selecionarComDiagnostico(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle,
            LocalDate data
    ) {

        List<ResultadoAvaliacaoPessoa> candidatos =
                avaliarCandidatos(
                        parte,
                        pessoas,
                        controle,
                        data
                );

        ResultadoAvaliacaoPessoa escolhido =
                candidatos.stream()
                        .max(
                                Comparator.comparingInt(
                                        ResultadoAvaliacaoPessoa::getTotal
                                )
                        )
                        .orElse(null);

        return new DiagnosticoSelecaoPessoa(
                parte,
                candidatos,
                escolhido
        );
    }

    public DiagnosticoSelecaoPessoa selecionarComDiagnostico(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle,
            LocalDate data,
            TipoParticipacao tipo
    ) {
        List<ResultadoAvaliacaoPessoa> candidatos = pessoas.stream()
                .filter(pessoa -> parte.pessoaPodeExercerParticipacao(pessoa, tipo))
                .filter(pessoa -> regrasService.podeExercerParticipacao(pessoa, parte, tipo))
                .filter(pessoa -> !possuiConflitoTesourosJoias(pessoa, parte, controle, data))
                .map(pessoa -> avaliadorPessoaService.avaliar(
                        pessoa,
                        parte,
                        controle,
                        tipo
                ))
                .toList();

        ResultadoAvaliacaoPessoa escolhido = candidatos.stream()
                .max(Comparator.comparingInt(ResultadoAvaliacaoPessoa::getTotal))
                .orElse(null);

        return new DiagnosticoSelecaoPessoa(parte, candidatos, escolhido);
    }

    private List<ResultadoAvaliacaoPessoa> avaliarPessoasElegiveis(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle,
            LocalDate data
    ) {

        List<ResultadoAvaliacaoPessoa> resultados =
                new ArrayList<>();

        for (Pessoa pessoa : pessoas) {

            if (!regrasService.podeDesignar(
                    pessoa,
                    parte,
                    controle
            )) {
                continue;
            }

            if (possuiConflitoTesourosJoias(
                    pessoa,
                    parte,
                    controle,
                    data
            )) {
                continue;
            }

            resultados.add(
                    avaliadorPessoaService.avaliar(
                            pessoa,
                            parte,
                            controle
                    )
            );
        }

        return resultados;
    }

    /**
     * Verifica se a pessoa já recebeu, na mesma semana,
     * a parte que entra em conflito com a parte atual.
     * A regra é:
     * DISCURSO_TESOUROS
     *      ↕
     * JOIAS_ESPIRITUAIS
     * O bloqueio ocorre somente quando a data é a mesma.
     */
    private boolean possuiConflitoTesourosJoias(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle,
            LocalDate data
    ) {
        if (data == null) {
            return false;
        }

        return controle.possuiConflitoTesourosJoias(
                pessoa,
                parte,
                data
        );
    }
}