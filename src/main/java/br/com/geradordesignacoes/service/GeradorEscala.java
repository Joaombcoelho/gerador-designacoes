package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class GeradorEscala {

    private final RegrasService regrasService;
    private final SeletorPessoaService seletorPessoaService;
    private final AvaliadorPessoaService avaliadorPessoaService;
    private final HistoricoDesignacoesService historicoService;
    private final PessoaService pessoaService;


    public GeradorEscala(RegrasService regrasService) {

        this.regrasService = regrasService;

        this.avaliadorPessoaService =
                new AvaliadorPessoaService();


        this.seletorPessoaService =
                new SeletorPessoaService(
                        regrasService,
                        avaliadorPessoaService
                );


        this.historicoService =
                new HistoricoDesignacoesService();

        this.pessoaService =
                new PessoaService(
                        new PessoaDAO()
                );
    }


    public ResultadoGeracaoEscala gerarEscala(
            LocalDate data,
            List<Parte> partes
    ) {

        return gerarEscala(
                data,
                partes,
                pessoaService.listarTodas()
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
                historicoService.getHistorico()
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

        Pessoa presidenteDaReuniao = null;

        List<Parte> partesOrdenadas =
                ordenarPartesComPresidentePrimeiro(partes);

        HistoricoDesignacoes historicoControle =
                new HistoricoDesignacoes(
                        historico.participacoes()
                );


        ControleDesignacoes controleDesignacoes =
                new ControleDesignacoes(
                        historicoControle
                );


        int quantidadeHistorica =
                controleDesignacoes
                        .getParticipacoes()
                        .size();

        for (Parte parte : partesOrdenadas) {

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


            } else if (parte.getTipo() == TipoParte.DIRIGENTE_ESTUDO) {


                gerou =
                        designarDirigenteEstudo(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes,
                                diagnosticos
                        );


            } else {

                gerou =
                        designarParteIndividual(
                                data,
                                parte,
                                pessoas,
                                designacoes,
                                controleDesignacoes,
                                diagnosticos,
                                presidenteDaReuniao
                        );

            }

            if (gerou
                    && parte.getTipo() == TipoParte.PRESIDENTE_REUNIAO) {

                presidenteDaReuniao =
                        designacoes.get(designacoes.size() - 1)
                                .responsavel();

                controleDesignacoes.definirPresidente(
                        presidenteDaReuniao
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


        salvarHistorico(
                novasParticipacoes
        );


        Escala escala = new Escala(
                data,
                designacoes
        );

        escala.setPresidente(presidenteDaReuniao);

        return new ResultadoGeracaoEscala(
                escala,
                todasParticipacoes,
                erros,
                diagnosticos
        );
    }



    private List<Parte> ordenarPartesComPresidentePrimeiro(
            List<Parte> partes
    ) {

        List<Parte> partesOrdenadas =
                new ArrayList<>();


        partes.stream()
                .filter(parte ->
                        parte.getTipo() == TipoParte.PRESIDENTE_REUNIAO
                )
                .findFirst()
                .ifPresent(partesOrdenadas::add);


        partes.stream()
                .filter(parte ->
                        parte.getTipo() != TipoParte.PRESIDENTE_REUNIAO
                )
                .forEach(partesOrdenadas::add);


        return partesOrdenadas;
    }

    private boolean designarParteIndividual(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            ControleDesignacoes controleDesignacoes,
            List<DiagnosticoSelecaoPessoa> diagnosticos,
            Pessoa presidenteDaReuniao
    ) {

        Pessoa participante;


        if (parte.necessitaParticipacao(
                TipoParticipacao.ORACAO_FINAL)) {

            if (presidenteDaReuniao == null) {
                return false;
            }

            participante = presidenteDaReuniao;


        } else {

            DiagnosticoSelecaoPessoa diagnostico =
                    seletorPessoaService.selecionarComDiagnostico(
                            parte,
                            pessoas,
                            controleDesignacoes
                    );


            diagnosticos.add(
                    diagnostico
            );


            if (diagnostico.escolhido() == null) {

                return false;
            }


            participante =
                    diagnostico.escolhido()
                            .getPessoa();
        }


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
                        encontrarParticipacao(parte, TipoParticipacao.RESPONSAVEL)
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        dupla.ajudante(),
                        parte,
                        encontrarParticipacao(parte, TipoParticipacao.AJUDANTE)
                )
        );


        return true;
    }

    private boolean designarDirigenteEstudo(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            ControleDesignacoes controleDesignacoes,
            List<DiagnosticoSelecaoPessoa> diagnosticos
    ) {


        MelhorDuplaDirigenteEstudo dupla =
                selecionarDirigenteELeitor(
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
                        dupla.dirigente(),
                        dupla.leitor()
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        dupla.dirigente(),
                        parte,
                        TipoParticipacao.DIRIGENTE
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        dupla.leitor(),
                        parte,
                        TipoParticipacao.LEITOR
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

    private MelhorDuplaDirigenteEstudo selecionarDirigenteELeitor(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controleDesignacoes

    ) {

        List<Pessoa> pessoasJaDesignadas =
                controleDesignacoes.getPessoasDesignadas();


        MelhorDuplaDirigenteEstudo melhor =
                null;


        for (Pessoa dirigente : pessoas) {


            if (!parte.pessoaPodeExercerParticipacao(
                    dirigente,
                    TipoParticipacao.DIRIGENTE
            )) {
                continue;
            }


            if (pessoasJaDesignadas.contains(dirigente)) {
                continue;
            }



            for (Pessoa leitor : pessoas) {


                if (dirigente.equals(leitor)) {
                    continue;
                }


                if (!parte.pessoaPodeExercerParticipacao(
                        leitor,
                        TipoParticipacao.LEITOR
                )) {
                    continue;
                }


                if (pessoasJaDesignadas.contains(leitor)) {
                    continue;
                }



                ResultadoAvaliacaoPessoa avaliacaoDirigente =
                        avaliadorPessoaService.avaliar(
                                dirigente,
                                parte,
                                controleDesignacoes
                        );


                ResultadoAvaliacaoPessoa avaliacaoLeitor =
                        avaliadorPessoaService.avaliar(
                                leitor,
                                parte,
                                controleDesignacoes
                        );



                MelhorDuplaDirigenteEstudo candidata =
                        new MelhorDuplaDirigenteEstudo(
                                dirigente,
                                leitor,
                                avaliacaoDirigente.getTotal()
                                        +
                                        avaliacaoLeitor.getTotal()
                        );



                if (melhor == null
                        ||
                        candidata.pontuacaoTotal()
                                >
                                melhor.pontuacaoTotal()) {


                    melhor = candidata;
                }
            }
        }


        return melhor;
    }

    private TipoParticipacao encontrarParticipacao(
            Parte parte,
            TipoParticipacao esperada
    ) {

        return parte.getParticipacoesNecessarias()
                .stream()
                .filter(tipo -> tipo == esperada)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "A parte "
                                        + parte.getNome()
                                        + " não possui a participação "
                                        + esperada
                        )
                );
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

    private void salvarHistorico(
            List<ParticipacaoDesignacao> participacoes
    ) {

        historicoService.salvarGeracao(participacoes);

    }
    private record MelhorDuplaDirigenteEstudo(
            Pessoa dirigente,
            Pessoa leitor,
            int pontuacaoTotal
    ) {
    }
}