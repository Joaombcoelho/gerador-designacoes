package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import br.com.geradordesignacoes.service.RegrasService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Regras de consulta, validação e persistência da edição de escalas.
 */
public class EdicaoEscalaController {

    private final EscalaDAO escalaDAO =
            new EscalaDAO();

    private final PessoaDAO pessoaDAO =
            new PessoaDAO();

    private final RegrasService regrasService =
            new RegrasService();

    private Escala escalaSelecionada;

    public List<Escala> listarEscalasDoMes(
            YearMonth mes
    ) {
        validarMes(mes);

        return escalaDAO.listarTodas()
                .stream()
                .filter(
                        escala ->
                                YearMonth.from(
                                        escala.getData()
                                ).equals(mes)
                )
                .sorted(
                        Comparator.comparing(
                                Escala::getData
                        )
                )
                .toList();
    }

    public List<YearMonth> listarMesesDisponiveis() {
        return escalaDAO.listarTodas()
                .stream()
                .map(escala -> YearMonth.from(escala.getData()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    public Escala selecionarEscala(
            Integer escalaId
    ) {
        escalaSelecionada =
                escalaDAO.buscarPorId(escalaId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Escala não encontrada."
                                        )
                        );

        return escalaSelecionada;
    }

    public List<Designacao> listarDesignacoesSelecionadas() {
        if (escalaSelecionada == null) {
            return List.of();
        }

        return escalaSelecionada.getDesignacoes();
    }

    public List<Pessoa> listarPessoasParaSelecao(
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {
        return listarPessoasParaSelecao(
                parte,
                tipoParticipacao,
                null
        );
    }

    public List<Pessoa> listarPessoasParaSelecao(
            Parte parte,
            TipoParticipacao tipoParticipacao,
            Pessoa participanteAtual
    ) {
        return listarPessoasParaSelecao(
                parte,
                tipoParticipacao,
                participanteAtual,
                null
        );
    }

    public List<Pessoa> listarPessoasParaSelecao(
            Parte parte,
            TipoParticipacao tipoParticipacao,
            Pessoa participanteAtual,
            Integer designacaoAtualId
    ) {
        if (parte == null || tipoParticipacao == null) {
            return List.of();
        }

        ControleDesignacoes controle =
                criarControleDaReuniao(designacaoAtualId);

        LinkedHashSet<Pessoa> pessoas =
                new LinkedHashSet<>(
                        pessoaDAO.listarAtivos()
                                .stream()
                                .filter(
                                        pessoa ->
                                                pessoaElegivel(
                                                        pessoa,
                                                        parte,
                                                        tipoParticipacao,
                                                        controle
                                                )
                                )
                                .sorted(
                                        Comparator.comparing(
                                                Pessoa::getNome,
                                                String.CASE_INSENSITIVE_ORDER
                                        )
                                )
                                .toList()
                );

        if (participanteAtual != null) {
            pessoas.add(participanteAtual);
        }

        return pessoas.stream().toList();
    }

    private boolean pessoaElegivel(
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao,
            ControleDesignacoes controle
    ) {
        /*
         * Na edição manual, a pessoa responsável pela Leitura Bíblica
         * pode ter nível de leitura experiente. A restrição a BASICO
         * continua aplicada pela regra de geração automática (Parte /
         * RegrasService), mas não deve impedir uma correção manual.
         * Mantemos as demais regras: pessoa ativa, sexo e privilégio
         * permitidos pela parte, e permissão para fazer leitura.
         */
        boolean leituraBiblicaManual =
                parte.getTipo() == TipoParte.LEITURA
                        && tipoParticipacao == TipoParticipacao.LEITOR;

        boolean podeExercer = leituraBiblicaManual
                ? parte.necessitaParticipacao(tipoParticipacao)
                        && parte.podeSerRealizadaPor(pessoa)
                        && pessoa.podeExercer(tipoParticipacao)
                : regrasService.podeExercerParticipacao(
                        pessoa,
                        parte,
                        tipoParticipacao
                );

        if (!podeExercer) {
            return false;
        }

        if (controle.ehPresidente(pessoa)) {
            return false;
        }

        if (controle.getPessoasDesignadas().contains(pessoa)
                && pessoa.getPrivilegio() !=
                Privilegio.ANCIAO
                && pessoa.getPrivilegio() !=
                Privilegio.SERVO_MINISTERIAL) {
            return false;
        }

        return escalaSelecionada == null
                || !controle.possuiConflitoTesourosJoias(
                pessoa,
                parte,
                escalaSelecionada.getData()
        );
    }

    public List<LocalDate> verificarConflitos(
            Integer designacaoId,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {
        Designacao designacao =
                obterDesignacao(designacaoId);

        YearMonth mes =
                YearMonth.from(
                        escalaSelecionada.getData()
                );

        LinkedHashSet<LocalDate> datas =
                new LinkedHashSet<>();

        adicionarDatasDeConflito(
                datas,
                novoResponsavel,
                mes,
                designacao.id()
        );

        adicionarDatasDeConflito(
                datas,
                novoAjudante,
                mes,
                designacao.id()
        );

        return datas.stream()
                .sorted()
                .toList();
    }

    private void adicionarDatasDeConflito(
            LinkedHashSet<LocalDate> datas,
            Pessoa pessoa,
            YearMonth mes,
            Integer designacaoId
    ) {
        if (pessoa == null) {
            return;
        }

        datas.addAll(
                escalaDAO
                        .listarDatasDeOutrasDesignacoesNoMes(
                                pessoa.getId(),
                                mes,
                                designacaoId
                        )
        );
    }

    public void salvarAlteracoes(
            Integer designacaoId,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {
        Designacao designacao =
                obterDesignacao(designacaoId);

        validarResponsavel(novoResponsavel);
        validarParticipante(
                designacao.parte(),
                novoResponsavel,
                tipoParticipacaoDoResponsavel(designacao.parte()),
                designacao.id()
        );

        validarAjudanteObrigatorio(
                designacao.parte(),
                novoAjudante
        );

        if (novoAjudante != null) {
            validarParticipante(
                    designacao.parte(),
                    novoAjudante,
                    TipoParticipacao.AJUDANTE,
                    designacao.id()
            );
        }

        validarCompatibilidadeDaParte(
                designacao.parte(),
                novoResponsavel,
                novoAjudante,
                designacao.id()
        );

        atualizarDesignacao(
                designacao,
                novoResponsavel,
                novoAjudante
        );

        recarregarEscala();
    }

    private void atualizarDesignacao(
            Designacao designacao,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {
        escalaDAO.atualizarDesignacao(
                designacao.id(),
                novoResponsavel.getId(),
                obterIdAjudante(novoAjudante)
        );
    }

    private Integer obterIdAjudante(
            Pessoa ajudante
    ) {
        return ajudante == null
                ? null
                : ajudante.getId();
    }

    public void adicionarParte(
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante
    ) {
        validarEscalaSelecionada();
        validarParte(parte);
        validarResponsavel(responsavel);
        validarParteAindaNaoAdicionada(parte);
        validarResponsavelPodeRealizarParte(
                parte,
                responsavel
        );
        validarParticipante(
                parte,
                responsavel,
                tipoParticipacaoDoResponsavel(parte),
                null
        );
        validarAjudanteObrigatorio(
                parte,
                ajudante
        );
        validarAjudantePodeRealizarParte(
                parte,
                ajudante
        );
        if (ajudante != null) {
            validarParticipante(
                    parte,
                    ajudante,
                    TipoParticipacao.AJUDANTE,
                    null
            );
        }
        validarPessoasDiferentes(
                responsavel,
                ajudante
        );
        validarCompatibilidadeDaParte(
                parte,
                responsavel,
                ajudante,
                null
        );

        escalaDAO.adicionarDesignacao(
                escalaSelecionada.getId(),
                parte.getId(),
                responsavel.getId(),
                obterIdAjudante(ajudante)
        );

        recarregarEscala();
    }

    private void validarEscalaSelecionada() {
        if (escalaSelecionada == null) {
            throw new IllegalStateException(
                    "Selecione uma escala."
            );
        }
    }

    private void validarMes(
            YearMonth mes
    ) {
        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }
    }

    private void validarParte(
            Parte parte
    ) {
        if (parte == null) {
            throw new IllegalArgumentException(
                    "Selecione uma parte."
            );
        }
    }

    private void validarResponsavel(
            Pessoa responsavel
    ) {
        if (responsavel == null) {
            throw new IllegalArgumentException(
                    "Selecione um responsável."
            );
        }
    }

    private void validarAjudanteObrigatorio(
            Parte parte,
            Pessoa ajudante
    ) {
        if (parte.getExigeAjudante()
                && ajudante == null) {

            throw new IllegalArgumentException(
                    "Esta parte exige um ajudante."
            );
        }
    }

    private void validarParteAindaNaoAdicionada(
            Parte parte
    ) {
        boolean parteJaExiste =
                escalaSelecionada
                        .getDesignacoes()
                        .stream()
                        .anyMatch(
                                designacao ->
                                        designacao.parte()
                                                .equals(parte)
                        );

        if (parteJaExiste) {
            throw new IllegalArgumentException(
                    "Esta parte já está adicionada nesta escala."
            );
        }
    }

    private void validarResponsavelPodeRealizarParte(
            Parte parte,
            Pessoa responsavel
    ) {
        if (!parte.podeSerRealizadaPor(responsavel)) {
            throw new IllegalArgumentException(
                    "O responsável selecionado não pode realizar esta parte."
            );
        }
    }

    private void validarParticipante(
                Parte parte,
                Pessoa pessoa,
                TipoParticipacao tipoParticipacao,
                Integer designacaoAtualId
        ) {
            if (!pessoaElegivel(
                    pessoa,
                    parte,
                    tipoParticipacao,
                    criarControleDaReuniao(designacaoAtualId)
            )) {
                throw new IllegalArgumentException(
                        "A pessoa selecionada não pode exercer esta participação."
                );
            }
        }

    private void validarCompatibilidadeDaParte(
                Parte parte,
                Pessoa responsavel,
                Pessoa ajudante,
                Integer designacaoAtualId
        ) {
            if (parte.getTipo() == TipoParte.DEMONSTRACAO
                    && !regrasService.podeFormarDemonstracao(
                    parte,
                    responsavel,
                    ajudante,
                    criarControleDaReuniao(designacaoAtualId)
                            .getPessoasDesignadas()
            )) {
                throw new IllegalArgumentException(
                        "O responsável e o ajudante não são compatíveis para esta demonstração."
                );
            }
        }

    private ControleDesignacoes criarControleDaReuniao(
                Integer designacaoAtualId
        ) {
            ControleDesignacoes controle =
                    new ControleDesignacoes();

            if (escalaSelecionada == null) {
                return controle;
            }

            for (Designacao designacao :
                    escalaSelecionada.getDesignacoes()) {

                if (designacao.id() != null
                        && designacao.id().equals(designacaoAtualId)) {
                    continue;
                }

                TipoParticipacao tipoResponsavel =
                        tipoParticipacaoDoResponsavel(
                                designacao.parte()
                        );

                if (designacao.responsavel() != null) {
                    controle.registrarParticipacao(
                            new ParticipacaoDesignacao(
                                    designacao.data(),
                                    designacao.responsavel(),
                                    designacao.parte(),
                                    tipoResponsavel
                            )
                    );

                    if (tipoResponsavel
                            == TipoParticipacao.PRESIDENTE) {
                        controle.definirPresidente(
                                designacao.responsavel()
                        );
                    }
                }

                if (designacao.ajudante() != null) {
                    controle.registrarParticipacao(
                            new ParticipacaoDesignacao(
                                    designacao.data(),
                                    designacao.ajudante(),
                                    designacao.parte(),
                                    TipoParticipacao.AJUDANTE
                            )
                    );
                }
            }

            return controle;
        }

    private TipoParticipacao tipoParticipacaoDoResponsavel(
                Parte parte
        ) {
            if (parte.getTipo() == TipoParte.DIRIGENTE_ESTUDO) {
                return TipoParticipacao.DIRIGENTE;
            }

            return parte.getParticipacoesNecessarias()
                    .stream()
                    .filter(tipo -> tipo != TipoParticipacao.AJUDANTE)
                    .findFirst()
                    .orElse(TipoParticipacao.RESPONSAVEL);
    }

    private void validarAjudantePodeRealizarParte(
            Parte parte,
            Pessoa ajudante
    ) {
        if (ajudante != null
                && !parte.podeSerRealizadaPor(ajudante)) {

            throw new IllegalArgumentException(
                    "O ajudante selecionado não pode realizar esta parte."
            );
        }
    }

    private void validarPessoasDiferentes(
            Pessoa responsavel,
            Pessoa ajudante
    ) {
        if (ajudante != null
                && responsavel.getId()
                .equals(ajudante.getId())) {

            throw new IllegalArgumentException(
                    "O responsável e o ajudante devem ser pessoas diferentes."
            );
        }
    }

    private void recarregarEscala() {
        selecionarEscala(
                escalaSelecionada.getId()
        );
    }

    private Designacao obterDesignacao(
            Integer designacaoId
    ) {
        validarEscalaSelecionada();

        return escalaSelecionada
                .getDesignacoes()
                .stream()
                .filter(
                        designacao ->
                                designacao.id()
                                        .equals(designacaoId)
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Designação não encontrada."
                                )
                );
    }
}