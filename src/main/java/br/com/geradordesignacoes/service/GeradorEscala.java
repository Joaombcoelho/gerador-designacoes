package br.com.geradordesignacoes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.DiagnosticoSelecaoPessoa;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.HistoricoDesignacoes;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoAvaliacaoPessoa;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;


public class GeradorEscala {

    private final RegrasService regrasService;
    private final SeletorPessoaService seletorPessoaService;
    private final AvaliadorPessoaService avaliadorPessoaService;
    private final HistoricoDesignacoesDAO historicoDAO;
    private final PessoaDAO pessoaDAO;


    public GeradorEscala(RegrasService regrasService) {

        this.regrasService = regrasService;

        this.avaliadorPessoaService =
                new AvaliadorPessoaService();


        this.seletorPessoaService =
                new SeletorPessoaService(
                        regrasService,
                        avaliadorPessoaService
                );


        this.historicoDAO =
                new HistoricoDesignacoesDAO();

        this.pessoaDAO =
                new PessoaDAO();
    }


    public ResultadoGeracaoEscala gerarEscala(
            LocalDate data,
            List<Parte> partes
    ) {

        return gerarEscala(
                data,
                partes,
                pessoaDAO.listarTodos()
        );
    }


    public ResultadoGeracaoEscala gerarEscala(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas
    ) {

        return gerar(
                data,
                partes,
                pessoas
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
                historicoDAO.carregarHistorico()
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


        List<Designacao> designacoes =
                new ArrayList<>();

        List<String> erros =
                new ArrayList<>();

        List<DiagnosticoSelecaoPessoa> diagnosticos =
                new ArrayList<>();


        HistoricoDesignacoes historicoControle =
                new HistoricoDesignacoes(
                        historico.getParticipacoes()
                );


        ControleDesignacoes controleDesignacoes =
                new ControleDesignacoes(
                        historicoControle
                );


        int quantidadeHistorica =
                controleDesignacoes
                        .getParticipacoes()
                        .size();

        for (Parte parte : partes) {

            boolean gerou;


            if (parte.getTipo() == TipoParte.DEMONSTRACAO) {

                gerou =
                        designarDemonstracao(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes
                        );

            } else {

                gerou =
                        designarParteIndividual(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes,
                                diagnosticos
                        );
            }



            if (!gerou) {

                erros.add(
                        "Não foi possível gerar a parte: "
                                + parte.getNome()
                );
            }
        }


        List<ParticipacaoDesignacao> todasParticipacoes =
                controleDesignacoes.getParticipacoes();


        List<ParticipacaoDesignacao> novasParticipacoes =
                todasParticipacoes
                        .subList(
                                quantidadeHistorica,
                                todasParticipacoes.size()
                        );



        historicoDAO.salvarTodos(
                novasParticipacoes
        );

        return new ResultadoGeracaoEscala(
                designacoes,
                todasParticipacoes,
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


        diagnosticos.add(
                diagnostico
        );


        if (diagnostico.getEscolhido() == null) {

            return false;
        }



        Pessoa participante =
                diagnostico
                        .getEscolhido()
                        .getPessoa();



        TipoParticipacao tipoParticipacao =
                determinarParticipacaoIndividual(
                        parte
                );



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
                        tipoParticipacao
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


        MelhorDuplaDemonstracao dupla =
                selecionarMelhorDuplaDemonstracao(
                        parte,
                        pessoas,
                        controleDesignacoes
                );



        if (dupla == null) {

            return false;
        }



        designacoes.add(
                new Designacao(
                        data,
                        parte,
                        dupla.responsavel(),
                        dupla.ajudante()
                )
        );



        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        dupla.responsavel(),
                        parte,
                        TipoParticipacao.RESPONSAVEL
                )
        );



        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        dupla.ajudante(),
                        parte,
                        TipoParticipacao.AJUDANTE
                )
        );


        return true;
    }




    private MelhorDuplaDemonstracao selecionarMelhorDuplaDemonstracao(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controleDesignacoes
    ) {


        List<Pessoa> pessoasJaDesignadas =
                controleDesignacoes
                        .getPessoasDesignadas();



        MelhorDuplaDemonstracao melhor =
                null;



        for (Pessoa responsavel : pessoas) {


            for (Pessoa ajudante : pessoas) {



                if (responsavel.equals(ajudante)) {

                    continue;
                }



                if (!regrasService.podeFormarDemonstracao(
                        parte,
                        responsavel,
                        ajudante,
                        pessoasJaDesignadas
                )) {

                    continue;
                }



                ResultadoAvaliacaoPessoa avaliacaoResponsavel =
                        avaliadorPessoaService.avaliar(
                                responsavel,
                                parte,
                                controleDesignacoes
                        );



                ResultadoAvaliacaoPessoa avaliacaoAjudante =
                        avaliadorPessoaService.avaliar(
                                ajudante,
                                parte,
                                controleDesignacoes
                        );



                MelhorDuplaDemonstracao candidata =
                        new MelhorDuplaDemonstracao(
                                responsavel,
                                ajudante,
                                avaliacaoResponsavel.getTotal()
                                        +
                                        avaliacaoAjudante.getTotal()
                        );



                if (melhor == null
                        ||
                        candidata.pontuacaoTotal()
                                >
                                melhor.pontuacaoTotal()) {


                    melhor =
                            candidata;
                }
            }
        }


        return melhor;
    }





    private TipoParticipacao determinarParticipacaoIndividual(
            Parte parte
    ) {


        return parte.getParticipacoesNecessarias()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Parte sem participação definida: "
                                        + parte.getNome()
                        )
                );
    }




    private record MelhorDuplaDemonstracao(
            Pessoa responsavel,
            Pessoa ajudante,
            int pontuacaoTotal
    ) {
    }
}