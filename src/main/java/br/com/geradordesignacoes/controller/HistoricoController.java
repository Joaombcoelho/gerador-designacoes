package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.view.historico.HistoricoView;

import java.util.List;


public class HistoricoController {

    private final HistoricoView view;

    private final EscalaDAO escalaDAO;


    public HistoricoController(
            HistoricoView view
    ) {

        this.view = view;

        this.escalaDAO =
                new EscalaDAO();


        carregarHistorico();
    }


    private void carregarHistorico() {

        List<Escala> escalas =
                escalaDAO.listarTodas();


        view.carregarEscalas(
                escalas
        );
    }


    public void atualizarHistorico() {

        List<Escala> escalas =
                escalaDAO.listarTodas();

        view.carregarEscalas(escalas);
    }


    public void carregarDetalhes(Escala escala) {

        if (escala == null) {
            return;
        }

        view.carregarDetalhes(
                escala.getDesignacoes()
        );
    }


    public void excluirEscala(Escala escala) {

        if (escala == null) {
            return;
        }

        escalaDAO.excluir(
                escala.getId()
        );

        atualizarHistorico();
    }
}