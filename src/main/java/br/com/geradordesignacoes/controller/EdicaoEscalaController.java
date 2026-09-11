package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public class EdicaoEscalaController {

    private final EscalaDAO escalaDAO;

    private Escala escalaSelecionada;


    public EdicaoEscalaController() {
        this.escalaDAO = new EscalaDAO();
    }


    /**
     * Seleciona uma escala pelo ID e mantém a escala carregada
     * para as demais operações da tela.
     */
    public Escala selecionarEscala(Integer escalaId) {

        if (escalaId == null) {
            throw new IllegalArgumentException(
                    "O ID da escala não pode ser nulo."
            );
        }

        Optional<Escala> escala =
                escalaDAO.buscarPorId(escalaId);

        if (escala.isEmpty()) {
            throw new RuntimeException(
                    "Escala não encontrada."
            );
        }

        escalaSelecionada = escala.get();

        return escalaSelecionada;
    }


    /**
     * Retorna as designações da escala atualmente selecionada.
     */
    public List<Designacao> listarDesignacoesSelecionadas() {

        if (escalaSelecionada == null) {
            return List.of();
        }

        return escalaSelecionada.getDesignacoes();
    }


    /**
     * Lista todas as escalas existentes no mês informado.
     */
    public List<Escala> listarEscalasDoMes(YearMonth mes) {

        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        return escalaDAO.listarTodas()
                .stream()
                .filter(escala ->
                        YearMonth.from(
                                escala.getData()
                        ).equals(mes)
                )
                .sorted(
                        java.util.Comparator.comparing(
                                Escala::getData
                        )
                )
                .toList();
    }


    /**
     * Adiciona uma nova parte à escala atualmente selecionada.
     */
    public void adicionarParte(
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante
    ) {

        if (escalaSelecionada == null) {
            throw new IllegalStateException(
                    "Nenhuma escala foi selecionada."
            );
        }

        if (parte == null) {
            throw new IllegalArgumentException(
                    "A parte não pode ser nula."
            );
        }

        if (responsavel == null) {
            throw new IllegalArgumentException(
                    "O responsável não pode ser nulo."
            );
        }

        if (parte.getExigeAjudante()
                && ajudante == null) {

            throw new IllegalArgumentException(
                    "Esta parte exige um ajudante."
            );
        }

        if (ajudante != null
                && responsavel.getId() != null
                && responsavel.getId().equals(ajudante.getId())) {

            throw new IllegalArgumentException(
                    "O responsável e o ajudante não podem ser a mesma pessoa."
            );
        }

        Designacao novaDesignacao =
                new Designacao(
                        null,
                        escalaSelecionada.getData(),
                        parte,
                        responsavel,
                        ajudante
                );

        escalaSelecionada.adicionarDesignacao(
                novaDesignacao
        );

        escalaDAO.atualizar(
                escalaSelecionada
        );
    }


    /**
     * Verifica conflitos da nova combinação de responsável e ajudante
     * dentro do mês da escala selecionada.
     */
    public List<LocalDate> verificarConflitos(
            Integer designacaoId,
            Pessoa responsavel,
            Pessoa ajudante
    ) {

        if (escalaSelecionada == null) {
            throw new IllegalStateException(
                    "Nenhuma escala foi selecionada."
            );
        }

        if (responsavel == null) {
            throw new IllegalArgumentException(
                    "O responsável não pode ser nulo."
            );
        }

        YearMonth mes =
                YearMonth.from(
                        escalaSelecionada.getData()
                );

        LinkedHashSet<LocalDate> conflitos =
                new LinkedHashSet<>();

        conflitos.addAll(
                escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                        responsavel.getId(),
                        mes,
                        designacaoId
                )
        );

        if (ajudante != null) {

            conflitos.addAll(
                    escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                            ajudante.getId(),
                            mes,
                            designacaoId
                    )
            );
        }

        return conflitos.stream()
                .sorted()
                .toList();
    }


    /**
     * Salva a alteração de responsável e ajudante de uma designação.
     */
    public void salvarAlteracoes(
            Integer designacaoId,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {

        if (escalaSelecionada == null) {
            throw new IllegalStateException(
                    "Nenhuma escala foi selecionada."
            );
        }

        if (designacaoId == null) {
            throw new IllegalArgumentException(
                    "O ID da designação não pode ser nulo."
            );
        }

        if (novoResponsavel == null) {
            throw new IllegalArgumentException(
                    "O responsável não pode ser nulo."
            );
        }

        Designacao designacaoEncontrada = null;
        int indice = -1;

        List<Designacao> designacoes =
                escalaSelecionada.getDesignacoes();

        for (int i = 0; i < designacoes.size(); i++) {

            Designacao designacao =
                    designacoes.get(i);

            if (designacao.id() != null
                    && designacao.id().equals(designacaoId)) {

                designacaoEncontrada = designacao;
                indice = i;
                break;
            }
        }

        if (designacaoEncontrada == null) {
            throw new RuntimeException(
                    "Designação não encontrada na escala."
            );
        }

        if (designacaoEncontrada.parte().getExigeAjudante()
                && novoAjudante == null) {

            throw new IllegalArgumentException(
                    "Esta parte exige um ajudante."
            );
        }

        if (novoAjudante != null
                && novoResponsavel.getId() != null
                && novoResponsavel.getId().equals(
                novoAjudante.getId()
        )) {

            throw new IllegalArgumentException(
                    "O responsável e o ajudante não podem ser a mesma pessoa."
            );
        }

        escalaDAO.atualizarDesignacao(
                designacaoId,
                novoResponsavel.getId(),
                novoAjudante == null
                        ? null
                        : novoAjudante.getId()
        );

        Designacao novaDesignacao =
                new Designacao(
                        designacaoEncontrada.id(),
                        designacaoEncontrada.data(),
                        designacaoEncontrada.parte(),
                        novoResponsavel,
                        novoAjudante
                );

        escalaSelecionada.substituirDesignacao(
                indice,
                novaDesignacao
        );
    }
}