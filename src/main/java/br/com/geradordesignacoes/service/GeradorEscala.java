package br.com.geradordesignacoes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
    private final HistoricoDesignacoesService historicoService;


    public GeradorEscala(RegrasService regrasService) {

        this.regrasService = regrasService;
        this.avaliadorPessoaService = new AvaliadorPessoaService();

        this.seletorPessoaService =
                new SeletorPessoaService(
                        regrasService,
                        avaliadorPessoaService
                );

        this.historicoService =
                new HistoricoDesignacoesService();
    }


    public ResultadoGeracaoEscala gerarEscala(
            LocalDate data,
            List<Parte> partes
    ) {

        return gerarEscala(
                data,
                partes,
                new PessoaDAO().listarTodos()
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

        List<Designacao> designacoes = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        List<DiagnosticoSelecaoPessoa> diagnosticos =
                new ArrayList<>();

        ControleDesignacoes controleDesignacoes =
                new ControleDesignacoes(
                        historico
                );

        int quantidadeParticipacoesHistoricas =
                controleDesignacoes.getParticipacoes().size();


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
                                + parte
                );
            }
        }

        List<ParticipacaoDesignacao> participacoesAtualizadas =
                controleDesignacoes.getParticipacoes();

        List<ParticipacaoDesignacao> participacoesGeradas =
                participacoesAtualizadas.subList(
                        quantidadeParticipacoesHistoricas,
                        participacoesAtualizadas.size()
                );

        historicoService.registrarGeracao(
                participacoesGeradas
        );

        return new ResultadoGeracaoEscala(
                designacoes,
                participacoesAtualizadas,
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

        MelhorDuplaDemonstracao melhorDupla =
                selecionarMelhorDuplaDemonstracao(
                        parte,
                        pessoas,
                        controleDesignacoes
                );

        if (melhorDupla == null) {
            return false;
        }


        designacoes.add(
                new Designacao(
                        data,
                        parte,
                        melhorDupla.responsavel(),
                        melhorDupla.ajudante()
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        melhorDupla.responsavel(),
                        parte,
                        TipoParticipacao.RESPONSAVEL
                )
        );


        controleDesignacoes.registrarParticipacao(
                new ParticipacaoDesignacao(
                        data,
                        melhorDupla.ajudante(),
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
                controleDesignacoes.getPessoasDesignadas();

        MelhorDuplaDemonstracao melhorDupla = null;

        for (Pessoa responsavel : pessoas) {

            for (Pessoa ajudante : pessoas) {

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

                MelhorDuplaDemonstracao dupla =
                        new MelhorDuplaDemonstracao(
                                responsavel,
                                ajudante,
                                avaliacaoResponsavel.getTotal()
                                        + avaliacaoAjudante.getTotal()
                        );

                if (melhorDupla == null
                        || dupla.pontuacaoTotal() > melhorDupla.pontuacaoTotal()) {

                    melhorDupla = dupla;
                }
            }
        }

        return melhorDupla;
    }


    private TipoParticipacao determinarParticipacaoIndividual(
            Parte parte
    ) {

        return parte.getParticipacoesNecessarias()
                .stream()
                .filter(tipoParticipacao ->
                        tipoParticipacao == TipoParticipacao.LEITOR
                                || tipoParticipacao == TipoParticipacao.ORADOR
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Parte individual sem participação compatível: "
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
