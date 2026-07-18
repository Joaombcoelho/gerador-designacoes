package br.com.geradordesignacoes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.model.TipoParte;

public class GeradorEscala {

    private final RegrasService regrasService;
    private final SeletorPessoaService seletorPessoaService;


    public GeradorEscala(RegrasService regrasService) {

        this.regrasService = regrasService;

        this.seletorPessoaService =
                new SeletorPessoaService(
                        regrasService,
                        new AvaliadorPessoaService()
                );
    }


    public ResultadoGeracaoEscala gerar(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas
    ) {

        List<Designacao> designacoes = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        ControleDesignacoes controleDesignacoes =
                new ControleDesignacoes();


        for (Parte parte : partes) {

            if (parte.getTipo() == TipoParte.DEMONSTRACAO) {

                boolean gerou =
                        designarDemonstracao(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes
                        );


                if (!gerou) {

                    erros.add(
                            "Não foi possível gerar a parte: "
                                    + parte
                    );
                }


            } else {

                boolean gerou =
                        designarParteIndividual(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes
                        );


                if (!gerou) {

                    erros.add(
                            "Não foi possível gerar a parte: "
                                    + parte
                    );
                }
            }
        }


        return new ResultadoGeracaoEscala(
                designacoes,
                erros
        );
    }



    private boolean designarParteIndividual(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            ControleDesignacoes controleDesignacoes
    ) {


        Optional<Pessoa> pessoa =
                seletorPessoaService.selecionarMelhorPessoa(
                        parte,
                        pessoas,
                        controleDesignacoes
                );


        if (pessoa.isEmpty()) {

            return false;
        }


        Pessoa participante = pessoa.get();


        designacoes.add(
                new Designacao(
                        data,
                        parte,
                        participante,
                        null
                )
        );


        controleDesignacoes.registrar(
                participante
        );


        return true;
    }



    private boolean designarDemonstracao(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            ControleDesignacoes controleDesignacoes
    ) {


        for (Pessoa responsavel : pessoas) {

            for (Pessoa ajudante : pessoas) {


                if (regrasService.podeFormarDemonstracao(
                        responsavel,
                        ajudante,
                        controleDesignacoes.getPessoasDesignadas()
                )) {


                    designacoes.add(
                            new Designacao(
                                    data,
                                    parte,
                                    responsavel,
                                    ajudante
                            )
                    );


                    controleDesignacoes.registrar(
                            responsavel
                    );

                    controleDesignacoes.registrar(
                            ajudante
                    );


                    return true;
                }
            }
        }

        return false;
    }
}