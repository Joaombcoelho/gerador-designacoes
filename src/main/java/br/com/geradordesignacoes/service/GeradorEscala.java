package br.com.geradordesignacoes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.geradordesignacoes.model.DiagnosticoSelecaoPessoa;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;

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

        return gerar(
                data,
                partes,
                pessoas,
                new HistoricoDesignacoes()
        );
    }


    public ResultadoGeracaoEscala gerar(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas,
            List<ParticipacaoDesignacao> historico
    ) {

        return gerar(
                data,
                partes,
                pessoas,
                new HistoricoDesignacoes(historico)
        );
    }


    public ResultadoGeracaoEscala gerar(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas,
            HistoricoDesignacoes historico
    ) {

        List<Designacao> designacoes = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        List<DiagnosticoSelecaoPessoa> diagnosticos =
                new ArrayList<>();

        ControleDesignacoes controleDesignacoes =
                new ControleDesignacoes(
                        historico
                );


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
                                controleDesignacoes,
                                diagnosticos
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
                controleDesignacoes.getParticipacoes(),
                erros,
                diagnosticos
        );
    }


    private boolean designarParteIndividual(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            ControleDesignacoes controleDesignacoes,
            List<DiagnosticoSelecaoPessoa> diagnosticos
    ) {

        DiagnosticoSelecaoPessoa diagnostico =
                seletorPessoaService.selecionarComDiagnostico(
                        parte,
                        pessoas,
                        controleDesignacoes
                );


        diagnosticos.add(diagnostico);


        if (diagnostico.getEscolhido() == null) {

            return false;
        }


        Pessoa participante =
                diagnostico.getEscolhido()
                        .getPessoa();


        designacoes.add(
                new Designacao(
                        data,
                        parte,
                        participante,
                        null
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        participante,
                        parte,
                        TipoParticipacao.LEITOR
                )
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


                    controleDesignacoes.registrarParticipacao(
                            new ParticipacaoDesignacao(
                                    data,
                                    responsavel,
                                    parte,
                                    TipoParticipacao.RESPONSAVEL
                            )
                    );


                    controleDesignacoes.registrarParticipacao(
                            new ParticipacaoDesignacao(
                                    data,
                                    ajudante,
                                    parte,
                                    TipoParticipacao.AJUDANTE
                            )
                    );


                    return true;
                }
            }
        }

        return false;
    }
}