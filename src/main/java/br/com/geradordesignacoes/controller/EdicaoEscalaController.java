package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.view.escala.EdicaoEscalaView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;

/** Coordena a edição atômica de responsável e ajudante de uma designação. */
public class EdicaoEscalaController {

    private final EdicaoEscalaView view;
    private final Escala escala;
    private final EscalaDAO escalaDAO;

    public EdicaoEscalaController(EdicaoEscalaView view, Escala escala) {
        this.view = view;
        this.escala = escala;
        this.escalaDAO = new EscalaDAO();

        view.carregarPessoas(new PessoaDAO().listarTodos());
        view.carregarDesignacoes(escala.getDesignacoes());
        view.getBotaoSalvar().setOnAction(event -> salvarAlteracoes());
        view.getTabela().getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, atual) -> carregarDesignacao(atual)
        );
    }

    private void carregarDesignacao(Designacao designacao) {
        if (designacao != null) {
            view.selecionarParticipantes(
                    designacao.responsavel(),
                    designacao.ajudante()
            );
        }
    }

    private void salvarAlteracoes() {
        Designacao designacao = view.getTabela().getSelectionModel().getSelectedItem();
        Pessoa novoResponsavel = view.getResponsavelSelecionado();
        Pessoa novoAjudante = view.getAjudanteSelecionado();

        if (designacao == null || novoResponsavel == null) {
            view.exibirAviso("Selecione uma designação e um responsável.");
            return;
        }

        if (designacao.parte().getExigeAjudante() && novoAjudante == null) {
            view.exibirAviso("Esta parte exige um ajudante.");
            return;
        }

        List<LocalDate> conflitos = obterConflitos(
                designacao, novoResponsavel, novoAjudante
        );

        if (!conflitos.isEmpty()
                && !view.confirmarConflitos(novoResponsavel, novoAjudante, conflitos)) {
            carregarDesignacao(designacao);
            return;
        }

        /* Uma única atualização evita persistência parcial dos dois papéis. */
        escalaDAO.atualizarDesignacao(
                designacao.id(),
                novoResponsavel.getId(),
                novoAjudante == null ? null : novoAjudante.getId()
        );

        view.exibirSucesso("Designação atualizada com sucesso.");
    }

    private List<LocalDate> obterConflitos(
            Designacao designacao,
            Pessoa responsavel,
            Pessoa ajudante
    ) {
        YearMonth mes = YearMonth.from(escala.getData());
        LinkedHashSet<LocalDate> datas = new LinkedHashSet<>();
        datas.addAll(escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                responsavel.getId(), mes, designacao.id()
        ));

        if (ajudante != null) {
            datas.addAll(escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                    ajudante.getId(), mes, designacao.id()
            ));
        }

        return datas.stream().sorted().toList();
    }
}
