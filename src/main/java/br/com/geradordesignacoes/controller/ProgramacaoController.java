package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.view.programacao.ProgramacaoView;

import java.time.LocalDate;
import java.util.List;

public class ProgramacaoController {

    private final ProgramacaoView view;

    private final ProgramacaoSemanaService service;


    public ProgramacaoController(
            ProgramacaoView view
    ) {

        this.view = view;

        this.service =
                new ProgramacaoSemanaService();

        registrarEventos();

        view.atualizarStatus(
                "Selecione uma data para configurar a programação."
        );
    }


    private void registrarEventos() {

        view.getCampoData()
                .setOnAction(
                        event -> carregarProgramacao()
                );


        view.getBotaoAdicionar()
                .setOnAction(
                        event -> adicionarParte()
                );


        view.getBotaoRemover()
                .setOnAction(
                        event -> removerParte()
                );


        view.getListaPartesSelecionadas()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, atual) ->
                                selecionarParte(atual)
                );


        view.getBotaoSalvarTema()
                .setOnAction(
                        event -> salvarTema()
                );
    }


    private void carregarProgramacao() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            return;
        }


        try {

            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);


            atualizarListas(
                    programacao
            );


            view.atualizarStatus(
                    "Programação carregada."
            );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao carregar programação: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    private void atualizarListas(
            ProgramacaoSemana programacao
    ) {

        List<Parte> partesVariaveis =
                service.listarPartesVariaveis();


        List<Parte> partesSelecionadas =
                programacao.partes()
                        .stream()
                        .filter(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getTipoVariacao()
                                                == TipoVariacaoParte.VARIAVEL
                        )
                        .map(
                                ProgramacaoParte::getParte
                        )
                        .toList();


        view.getListaPartesSelecionadas()
                .getItems()
                .setAll(
                        partesSelecionadas
                );


        List<Parte> partesDisponiveis =
                partesVariaveis.stream()
                        .filter(
                                parte ->
                                        !partesSelecionadas
                                                .contains(parte)
                        )
                        .toList();


        view.getListaPartesDisponiveis()
                .getItems()
                .setAll(
                        partesDisponiveis
                );


        view.getCampoTema()
                .clear();
    }


    private void adicionarParte() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            view.atualizarStatus(
                    "Informe a data da reunião."
            );

            return;
        }


        Parte parte =
                view.getListaPartesDisponiveis()
                        .getSelectionModel()
                        .getSelectedItem();


        if (parte == null) {

            view.atualizarStatus(
                    "Selecione uma parte para adicionar."
            );

            return;
        }


        try {

            service.adicionarParteVariavel(
                    data,
                    parte.getId()
            );


            carregarProgramacao();


            view.atualizarStatus(
                    "Parte adicionada à programação."
            );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao adicionar parte: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    private void removerParte() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            view.atualizarStatus(
                    "Informe a data da reunião."
            );

            return;
        }


        Parte parte =
                view.getListaPartesSelecionadas()
                        .getSelectionModel()
                        .getSelectedItem();


        if (parte == null) {

            view.atualizarStatus(
                    "Selecione uma parte para remover."
            );

            return;
        }


        try {

            service.removerParteVariavel(
                    data,
                    parte.getId()
            );


            carregarProgramacao();


            view.atualizarStatus(
                    "Parte removida da programação."
            );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao remover parte: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    private void selecionarParte(
            Parte parte
    ) {

        if (parte == null) {

            view.getCampoTema()
                    .clear();

            return;
        }


        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            return;
        }


        try {

            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);


            programacao.partes()
                    .stream()
                    .filter(
                            programacaoParte ->
                                    programacaoParte
                                            .getParte()
                                            .getId()
                                            .equals(
                                                    parte.getId()
                                            )
                    )
                    .findFirst()
                    .ifPresentOrElse(

                            programacaoParte ->
                                    view.getCampoTema()
                                            .setText(
                                                    programacaoParte
                                                            .getTema()
                                            ),

                            () ->
                                    view.getCampoTema()
                                            .clear()
                    );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao carregar tema."
            );

            e.printStackTrace();
        }
    }


    private void salvarTema() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            view.atualizarStatus(
                    "Informe a data da reunião."
            );

            return;
        }


        Parte parte =
                view.getListaPartesSelecionadas()
                        .getSelectionModel()
                        .getSelectedItem();


        if (parte == null) {

            view.atualizarStatus(
                    "Selecione uma parte para informar o tema."
            );

            return;
        }


        String tema =
                view.getCampoTema()
                        .getText();


        try {

            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);


            ProgramacaoParte programacaoParte =
                    programacao.partes()
                            .stream()
                            .filter(
                                    item ->
                                            item.getParte()
                                                    .getId()
                                                    .equals(
                                                            parte.getId()
                                                    )
                            )
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Parte não encontrada na programação."
                                            )
                            );


            service.definirTema(
                    data,
                    programacaoParte.getOrdem(),
                    tema
            );


            view.atualizarStatus(
                    "Tema salvo com sucesso."
            );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao salvar tema: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}