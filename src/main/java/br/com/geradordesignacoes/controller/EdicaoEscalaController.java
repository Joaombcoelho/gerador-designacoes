package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Pessoa;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;

/** Regras de consulta, validação e persistência da edição de escalas. */
public class EdicaoEscalaController {

    private final EscalaDAO escalaDAO = new EscalaDAO();
    private Escala escalaSelecionada;

    public List<Escala> listarEscalasDoMes(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException("O mês não pode ser nulo.");
        }

        return escalaDAO.listarTodas().stream()
                .filter(escala -> YearMonth.from(escala.getData()).equals(mes))
                .sorted(java.util.Comparator.comparing(Escala::getData))
                .toList();
    }

    public Escala selecionarEscala(Integer escalaId) {
        escalaSelecionada = escalaDAO.buscarPorId(escalaId)
                .orElseThrow(() -> new IllegalArgumentException("Escala não encontrada."));
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
        Designacao designacao = obterDesignacao(designacaoId);
        YearMonth mes = YearMonth.from(escalaSelecionada.getData());
        LinkedHashSet<LocalDate> datas = new LinkedHashSet<>();
        datas.addAll(escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                novoResponsavel.getId(), mes, designacao.id()
        ));
        if (novoAjudante != null) {
            datas.addAll(escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                    novoAjudante.getId(), mes, designacao.id()
            ));
        }
        return datas.stream().sorted().toList();
    }

    public void salvarAlteracoes(
            Integer designacaoId,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {
        Designacao designacao = obterDesignacao(designacaoId);
        if (novoResponsavel == null) {
            throw new IllegalArgumentException("Selecione um responsável.");
        }
        if (designacao.parte().getExigeAjudante() && novoAjudante == null) {
            throw new IllegalArgumentException("Esta parte exige um ajudante.");
        }

        /* Uma única atualização impede salvar somente um dos dois papéis. */
        escalaDAO.atualizarDesignacao(
                designacao.id(), novoResponsavel.getId(),
                novoAjudante == null ? null : novoAjudante.getId()
        );
        selecionarEscala(escalaSelecionada.getId());
    }

    private Designacao obterDesignacao(Integer designacaoId) {
        if (escalaSelecionada == null) {
            throw new IllegalStateException("Selecione uma escala.");
        }
        return escalaSelecionada.getDesignacoes().stream()
                .filter(designacao -> designacao.id().equals(designacaoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Designação não encontrada."));
    }
}
