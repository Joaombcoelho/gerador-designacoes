package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.view.historico.HistoricoView;

import java.time.YearMonth;
import java.util.List;


public class HistoricoController {

    private final HistoricoView view;

    private final EscalaDAO escalaDAO;

    private List<Escala> escalas;


    public HistoricoController(
            HistoricoView view
    ) {

        this.view = view;

        this.escalaDAO =
                new EscalaDAO();

        carregarHistorico();
    }


    private void carregarHistorico() {

        escalas =
                escalaDAO.listarTodas();

        view.carregarEscalas(
                escalas
        );
    }


    public void atualizarHistorico() {

        escalas =
                escalaDAO.listarTodas();

        view.atualizarMeses(
                escalas
        );

        view.carregarEscalas(
                escalas
        );
    }


    public void filtrarPorMes(
            YearMonth mes
    ) {

        if (mes == null) {

            view.carregarEscalas(
                    escalas
            );

            return;
        }

        List<Escala> escalasFiltradas =
                escalas.stream()
                        .filter(
                                escala ->
                                        YearMonth.from(
                                                escala.getData()
                                        ).equals(mes)
                        )
                        .toList();

        view.carregarEscalas(
                escalasFiltradas
        );
    }


    public void carregarDetalhes(
            Escala escala
    ) {

        if (escala == null) {
            return;
        }

        view.carregarDetalhes(
                escala.getDesignacoes()
        );
    }


    public void excluirEscala(
            Escala escala
    ) {

        if (escala == null) {
            return;
        }

        escalaDAO.excluir(
                escala.getId()
        );

        atualizarHistorico();
    }
}