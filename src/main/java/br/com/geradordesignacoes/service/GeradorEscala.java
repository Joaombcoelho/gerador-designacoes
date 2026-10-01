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
        this.avaliadorPessoaService = new AvaliadorPessoaService();
        this.seletorPessoaService = new SeletorPessoaService(regrasService, avaliadorPessoaService);
        this.historicoService = new HistoricoDesignacoesService();
        this.pessoaService = new PessoaService(new PessoaDAO());
    }

    public ResultadoGeracaoEscala gerarEscala(LocalDate data, List<Parte> partes) {
        return gerarEscala(data, partes, pessoaService.listarTodas());
    }

    public ResultadoGeracaoEscala gerarEscala(LocalDate data, List<Parte> partes, List<Pessoa> pessoas) {
        return gerar(data, partes, pessoas);
    }

    public ResultadoGeracaoEscala gerar(LocalDate data, List<Parte> partes, List<Pessoa> pessoas) {
        return gerar(data, partes, pessoas, historicoService.getHistorico());
    }

    public ResultadoGeracaoEscala gerar(LocalDate data, List<Parte> partes, List<Pessoa> pessoas, List<ParticipacaoDesignacao> historico) {
        return gerar(data, partes, pessoas, new HistoricoDesignacoes(historico));
    }

    public ResultadoGeracaoEscala gerar(LocalDate data, List<Parte> partes, List<Pessoa> pessoas, HistoricoDesignacoes historico) {
        List<Designacao> designacoes = new ArrayList<>();
        List<String> erros = new ArrayList<>();
        List<DiagnosticoSelecaoPessoa> diagnosticos = new ArrayList<>();
        List<Parte> partesOrdenadas = ordenarPartesComPresidentePrimeiro(partes);
        ControleDesignacoes controleDesignacoes = criarControleDesignacoes(historico);
        Pessoa presidenteDaReuniao = processarPartes(data, partesOrdenadas, pessoas, designacoes, controleDesignacoes, diagnosticos, erros);
        salvarNovasParticipacoes(controleDesignacoes, historico);
        Escala escala = criarEscala(data, designacoes, presidenteDaReuniao);
        return new ResultadoGeracaoEscala(escala, controleDesignacoes.getParticipacoes(), erros, diagnosticos);
    }

    private ControleDesignacoes criarControleDesignacoes(HistoricoDesignacoes historico) {
        HistoricoDesignacoes historicoControle = new HistoricoDesignacoes(historico.participacoes());
        return new ControleDesignacoes(historicoControle);
    }

    private Pessoa processarPartes(LocalDate data, List<Parte> partesOrdenadas, List<Pessoa> pessoas, List<Designacao> designacoes, ControleDesignacoes controleDesignacoes, List<DiagnosticoSelecaoPessoa> diagnosticos, List<String> erros) {
        Pessoa presidenteDaReuniao = null;
        for (Parte parte : partesOrdenadas) {
            boolean gerou = processarParte(data, parte, pessoas, designacoes, controleDesignacoes, diagnosticos, erros, presidenteDaReuniao);
            if (gerou && parte.getTipo() == TipoParte.PRESIDENTE_REUNIAO) {
                presidenteDaReuniao = designacoes.get(designacoes.size() - 1).responsavel();
                controleDesignacoes.definirPresidente(presidenteDaReuniao);
            }
            if (!gerou && parte.getTipo() != TipoParte.DIRIGENTE_ESTUDO) {
                erros.add("Não foi possível gerar a parte: " + parte.getNome());
            }
        }
        return presidenteDaReuniao;
    }

    private boolean processarParte(LocalDate data, Parte parte, List<Pessoa> pessoas, List<Designacao> designacoes, ControleDesignacoes controleDesignacoes, List<DiagnosticoSelecaoPessoa> diagnosticos, List<String> erros, Pessoa presidenteDaReuniao) {
        if (parte.getTipo() == TipoParte.DEMONSTRACAO) {
            return designarDemonstracao(data, parte, pessoas, designacoes, controleDesignacoes);
        }
        if (parte.getTipo() == TipoParte.DIRIGENTE_ESTUDO) {
            return designarDirigenteEstudo(data, parte, pessoas, designacoes, controleDesignacoes, diagnosticos, erros);
        }
        return designarParteIndividual(data, parte, pessoas, designacoes, controleDesignacoes, diagnosticos, presidenteDaReuniao);
    }

    private boolean designarParteIndividual(LocalDate data, Parte parte, List<Pessoa> pessoas, List<Designacao> designacoes, ControleDesignacoes controleDesignacoes, List<DiagnosticoSelecaoPessoa> diagnosticos, Pessoa presidenteDaReuniao) {
        Pessoa participante;
        if (parte.necessitaParticipacao(TipoParticipacao.ORACAO_FINAL)) {
            if (presidenteDaReuniao == null) {
                return false;
            }
            participante = presidenteDaReuniao;
        } else {
            DiagnosticoSelecaoPessoa diagnostico = seletorPessoaService.selecionarComDiagnostico(parte, pessoas, controleDesignacoes, data);
            diagnosticos.add(diagnostico);
            if (diagnostico.escolhido() == null) {
                return false;
            }
            participante = diagnostico.escolhido().getPessoa();
        }
        TipoParticipacao tipoParticipacao = determinarParticipacaoIndividual(parte);
        designacoes.add(new Designacao(data, parte, participante, null));
        controleDesignacoes.registrarParticipacao(new ParticipacaoDesignacao(data, participante, parte, tipoParticipacao));
        return true;
    }

    private boolean designarDemonstracao(LocalDate data, Parte parte, List<Pessoa> pessoas, List<Designacao> designacoes, ControleDesignacoes controleDesignacoes) {
        MelhorDuplaDemonstracao dupla = selecionarMelhorDuplaDemonstracao(parte, pessoas, controleDesignacoes);
        if (dupla == null) {
            return false;
        }
        designacoes.add(new Designacao(data, parte, dupla.responsavel(), dupla.ajudante()));
        controleDesignacoes.registrarParticipacao(new ParticipacaoDesignacao(data, dupla.responsavel(), parte, encontrarParticipacao(parte, TipoParticipacao.RESPONSAVEL)));
        controleDesignacoes.registrarParticipacao(new ParticipacaoDesignacao(data, dupla.ajudante(), parte, encontrarParticipacao(parte, TipoParticipacao.AJUDANTE)));
        return true;
    }

    private boolean designarDirigenteEstudo(LocalDate data, Parte parte, List<Pessoa> pessoas, List<Designacao> designacoes, ControleDesignacoes controleDesignacoes, List<DiagnosticoSelecaoPessoa> diagnosticos, List<String> erros) {
        DiagnosticoSelecaoPessoa diagnosticoDirigente =
                seletorPessoaService.selecionarComDiagnostico(
                        parte,
                        pessoas,
                        controleDesignacoes,
                        data,
                        TipoParticipacao.DIRIGENTE
                );
        diagnosticos.add(diagnosticoDirigente);

        Pessoa dirigente = diagnosticoDirigente.escolhido() == null
                ? null
                : diagnosticoDirigente.escolhido().getPessoa();

        if (dirigente != null) {
            designacoes.add(new Designacao(data, parte, dirigente, null));
            controleDesignacoes.registrarParticipacao(
                    new ParticipacaoDesignacao(
                            data,
                            dirigente,
                            parte,
                            TipoParticipacao.DIRIGENTE
                    )
            );
        } else {
            erros.add("Não foi possível designar o Dirigente do Estudo Bíblico.");
        }

        List<Pessoa> pessoasSemDirigente = pessoas.stream()
                .filter(pessoa -> dirigente == null || !pessoa.equals(dirigente))
                .toList();
        DiagnosticoSelecaoPessoa diagnosticoLeitor =
                seletorPessoaService.selecionarComDiagnostico(
                        parte,
                        pessoasSemDirigente,
                        controleDesignacoes,
                        data,
                        TipoParticipacao.AJUDANTE
                );
        diagnosticos.add(diagnosticoLeitor);

        Pessoa leitor = diagnosticoLeitor.escolhido() == null
                ? null
                : diagnosticoLeitor.escolhido().getPessoa();

        if (leitor != null) {
            designacoes.add(new Designacao(data, parte, null, leitor));
            controleDesignacoes.registrarParticipacao(
                    new ParticipacaoDesignacao(
                            data,
                            leitor,
                            parte,
                            TipoParticipacao.AJUDANTE
                    )
            );
        } else {
            erros.add("Não foi possível designar o Leitor do Estudo Bíblico.");
        }

        return dirigente != null || leitor != null;
    }

    private MelhorDuplaDemonstracao selecionarMelhorDuplaDemonstracao(Parte parte, List<Pessoa> pessoas, ControleDesignacoes controleDesignacoes) {
        List<Pessoa> pessoasJaDesignadas = controleDesignacoes.getPessoasDesignadas();
        MelhorDuplaDemonstracao melhor = null;
        for (Pessoa responsavel : pessoas) {
            for (Pessoa ajudante : pessoas) {
                if (responsavel.equals(ajudante)) {
                    continue;
                }
                if (!regrasService.podeFormarDemonstracao(parte, responsavel, ajudante, pessoasJaDesignadas)) {
                    continue;
                }
                ResultadoAvaliacaoPessoa avaliacaoResponsavel = avaliadorPessoaService.avaliar(responsavel, parte, controleDesignacoes);
                ResultadoAvaliacaoPessoa avaliacaoAjudante = avaliadorPessoaService.avaliar(ajudante, parte, controleDesignacoes);
                MelhorDuplaDemonstracao candidata = new MelhorDuplaDemonstracao(responsavel, ajudante, avaliacaoResponsavel.getTotal() + avaliacaoAjudante.getTotal());
                if (melhor == null || candidata.pontuacaoTotal() > melhor.pontuacaoTotal()) {
                    melhor = candidata;
                }
            }
        }
        return melhor;
    }

    private TipoParticipacao encontrarParticipacao(Parte parte, TipoParticipacao esperada) {
        return parte.getParticipacoesNecessarias().stream().filter(tipo -> tipo == esperada).findFirst().orElseThrow(() -> new IllegalStateException("A parte " + parte.getNome() + " não possui a participação " + esperada));
    }

    private TipoParticipacao determinarParticipacaoIndividual(Parte parte) {
        return parte.getParticipacoesNecessarias().stream().findFirst().orElseThrow(() -> new IllegalStateException("Parte sem participação definida: " + parte.getNome()));
    }

    private record MelhorDuplaDemonstracao(Pessoa responsavel, Pessoa ajudante, int pontuacaoTotal) {
    }

    private void salvarHistorico(List<ParticipacaoDesignacao> participacoes) {
        historicoService.salvarGeracao(participacoes);
    }

    private void salvarNovasParticipacoes(ControleDesignacoes controleDesignacoes, HistoricoDesignacoes historico) {
        int quantidadeHistorica = historico.participacoes().size();
        List<ParticipacaoDesignacao> todasParticipacoes = controleDesignacoes.getParticipacoes();
        List<ParticipacaoDesignacao> novasParticipacoes = todasParticipacoes.subList(quantidadeHistorica, todasParticipacoes.size());
        salvarHistorico(novasParticipacoes);
    }

    private Escala criarEscala(LocalDate data, List<Designacao> designacoes, Pessoa presidenteDaReuniao) {
        Escala escala = new Escala(data, designacoes);
        escala.setPresidente(presidenteDaReuniao);
        return escala;
    }

    private List<Parte> ordenarPartesComPresidentePrimeiro(List<Parte> partes) {
        List<Parte> partesOrdenadas = new ArrayList<>();
        partes.stream().filter(parte -> parte.getTipo() == TipoParte.PRESIDENTE_REUNIAO).findFirst().ifPresent(partesOrdenadas::add);
        partes.stream().filter(parte -> parte.getTipo() != TipoParte.PRESIDENTE_REUNIAO).forEach(partesOrdenadas::add);
        return partesOrdenadas;
    }
}