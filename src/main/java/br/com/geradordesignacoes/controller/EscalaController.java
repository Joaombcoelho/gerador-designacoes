package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.HistoricoDesignacoesService;
import br.com.geradordesignacoes.service.ParteService;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.escala.ItemEscala;

import java.time.LocalDate;
import java.util.List;


public class EscalaController {

    private final EscalaView view;

    private final ParteService parteService;

    private final GeradorEscala geradorEscala;

    private final HistoricoDesignacoesService historicoService;

    private ResultadoGeracaoEscala ultimoResultado;

    private boolean escalaSalva;

    public EscalaController(EscalaView view) {

        this.view = view;

        parteService =
                new ParteService(
                        new ParteDAO()
                );


        geradorEscala =
                new GeradorEscala(
                        new RegrasService()
                );

        historicoService =
                new HistoricoDesignacoesService();


        registrarEventos();


        view.atualizarStatus(
                "Aguardando geração da escala..."
        );

        view.atualizarResumo("");
    }


    private void registrarEventos() {

        view.getBotaoGerar()
                .setOnAction(event -> gerarEscala());


        view.getBotaoGerarNovamente()
                .setOnAction(event -> gerarNovamente());


        view.getBotaoSalvar()
                .setOnAction(event -> salvarEscala());
    }


    private void gerarEscala() {

        try {

            LocalDate data =
                    view.getCampoData()
                            .getValue();


            if (data == null) {

                view.atualizarStatus(
                        "Informe a data da reunião."
                );

                view.atualizarResumo("");

                return;
            }


            view.atualizarStatus(
                    "Gerando escala..."
            );


            List<Parte> partes =
                    parteService.listarTodas();


            ultimoResultado =
                    geradorEscala.gerarEscala(
                            data,
                            partes
                    );

            escalaSalva = false;


            preencherTabela(
                    ultimoResultado
                            .getEscala()
                            .getDesignacoes()
            );


            atualizarResumo();


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao gerar escala."
            );


            view.atualizarResumo(
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }


    private void preencherTabela(
            List<Designacao> designacoes
    ) {

        view.getTabela()
                .getItems()
                .clear();


        for (Designacao designacao : designacoes) {


            String ajudante = "";


            if (designacao.getAjudante() != null) {

                ajudante =
                        designacao
                                .getAjudante()
                                .getNome();
            }


            ItemEscala item =
                    new ItemEscala(
                            designacao
                                    .getParte()
                                    .getNome(),

                            designacao
                                    .getResponsavel()
                                    .getNome(),

                            ajudante
                    );


            view.getTabela()
                    .getItems()
                    .add(item);
        }
    }


    private void atualizarResumo() {

        int quantidade =
                ultimoResultado
                        .getDesignacoes()
                        .size();

        StringBuilder resumo = new StringBuilder();

        resumo.append("Designações: ")
                .append(quantidade);

        if (ultimoResultado.possuiErros()) {

            view.atualizarStatus(
                    "Escala gerada com pendências."
            );

            resumo.append("\n\nPartes não geradas:\n");

            for (String erro : ultimoResultado.getErros()) {

                resumo.append("• ")
                        .append(erro)
                        .append("\n");
            }

        } else {

            view.atualizarStatus(
                    "Escala gerada com sucesso."
            );

            resumo.append("\n\nNenhuma pendência encontrada.");
        }

        view.atualizarResumo(resumo.toString());
    }


    private void gerarNovamente() {

        view.getTabela()
                .getItems()
                .clear();


        ultimoResultado = null;


        view.atualizarStatus(
                "Aguardando geração da escala..."
        );


        view.atualizarResumo("");
    }


    private void salvarEscala() {

        if (escalaSalva) {

            view.atualizarStatus(
                    "Esta escala já foi salva."
            );

            return;
        }

        if (ultimoResultado == null) {

            view.atualizarStatus(
                    "Nenhuma escala gerada."
            );

            view.atualizarResumo(
                    "Gere uma escala antes de salvar."
            );

            return;
        }

        try {

            historicoService.salvarGeracao(
                    ultimoResultado.getParticipacoes()
            );

            escalaSalva = true;

            view.atualizarStatus(
                    "Escala salva com sucesso."
            );

        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao salvar a escala."
            );

            view.atualizarResumo(
                    e.getMessage()
            );

            e.printStackTrace();
        }
    }
}