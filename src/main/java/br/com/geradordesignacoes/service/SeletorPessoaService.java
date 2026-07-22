package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.DiagnosticoSelecaoPessoa;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoAvaliacaoPessoa;

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

        Pessoa melhorPessoa = null;
        ResultadoAvaliacaoPessoa melhorResultado = null;


        for (Pessoa pessoa : pessoas) {

            boolean podeSerDesignada =
                    regrasService.podeDesignar(
                            pessoa,
                            parte,
                            controle.getPessoasDesignadas()
                    );


            if (!podeSerDesignada) {
                continue;
            }


            ResultadoAvaliacaoPessoa resultado =
                    avaliadorPessoaService.avaliar(
                            pessoa,
                            parte,
                            controle
                    );


            if (melhorResultado == null ||
                    resultado.getTotal() > melhorResultado.getTotal()) {

                melhorResultado = resultado;
                melhorPessoa = pessoa;
            }
        }


        return Optional.ofNullable(melhorPessoa);
    }



    public List<ResultadoAvaliacaoPessoa> avaliarCandidatos(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        List<ResultadoAvaliacaoPessoa> resultados =
                new ArrayList<>();


        for (Pessoa pessoa : pessoas) {

            boolean podeSerDesignada =
                    regrasService.podeDesignar(
                            pessoa,
                            parte,
                            controle.getPessoasDesignadas()
                    );


            if (!podeSerDesignada) {
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



    public DiagnosticoSelecaoPessoa selecionarComDiagnostico(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        List<ResultadoAvaliacaoPessoa> candidatos =
                avaliarCandidatos(
                        parte,
                        pessoas,
                        controle
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
}