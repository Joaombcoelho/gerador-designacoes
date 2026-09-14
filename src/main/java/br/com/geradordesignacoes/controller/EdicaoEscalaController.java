package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

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

        validarAjudanteObrigatorio(
                designacao.parte(),
                novoAjudante
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
        validarAjudanteObrigatorio(
                parte,
                ajudante
        );
        validarAjudantePodeRealizarParte(
                parte,
                ajudante
        );
        validarPessoasDiferentes(
                responsavel,
                ajudante
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