package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.view.programacao.ProgramacaoView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramacaoController {

    private final ProgramacaoView view;

    private final ProgramacaoSemanaService service;


    /*
     * Mantém as semanas atualmente exibidas.
     */
    private final List<LocalDate> semanas;


    public ProgramacaoController(
            ProgramacaoView view
    ) {

        this.view = view;

        this.service =
                new ProgramacaoSemanaService();

        this.semanas =
                new ArrayList<>();

        registrarEventos();

        view.atualizarStatus(
                "Selecione um mês para configurar a programação."
        );
    }


    private void registrarEventos() {

        /*
         * O DatePicker continua sendo usado,
         * mas agora representa o mês.
         */
        view.getCampoData()
                .setOnAction(
                        event -> carregarMes()
                );


        /*
         * Selecionar uma semana passa a carregar
         * a programação daquela reunião.
         */
        view.getListaSemanas()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, anterior, atual) ->
                                carregarSemana(atual)
                );


        view.getBotaoAdicionarSemana()
                .setOnAction(
                        event -> adicionarSemana()
                );


        view.getBotaoEditarSemana()
                .setOnAction(
                        event -> editarSemanaSelecionada()
                );


        /*
         * Mantemos os eventos antigos.
         */
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


    private void carregarMes() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            return;
        }


        YearMonth mes =
                YearMonth.from(data);


        semanas.clear();

        semanas.addAll(
                obterSemanasIniciais(mes)
        );


        atualizarSemanas();


        if (!semanas.isEmpty()) {

            view.getListaSemanas()
                    .getSelectionModel()
                    .selectFirst();
        }


        view.atualizarStatus(
                "Mês carregado. Selecione uma reunião para configurar."
        );
    }


    private List<LocalDate> obterSemanasIniciais(
            YearMonth mes
    ) {

        List<LocalDate> resultado =
                new ArrayList<>();


        LocalDate data =
                mes.atDay(1);


        /*
         * A reunião semanal atualmente é considerada
         * na quinta-feira.
         *
         * Procuramos as quatro primeiras quintas-feiras
         * do mês.
         */
        while (
                data.getMonth()
                        == mes.getMonth()
                        && resultado.size() < 4
        ) {

            if (data.getDayOfWeek()
                    == DayOfWeek.THURSDAY) {

                resultado.add(data);
            }


            data =
                    data.plusDays(1);
        }


        return resultado;
    }


    private void atualizarSemanas() {

        Map<LocalDate, Boolean> status =
                new HashMap<>();


        for (LocalDate data : semanas) {

            status.put(
                    data,
                    service.estaConfigurada(data)
            );
        }


        view.atualizarSemanas(
                semanas,
                status
        );


        atualizarBotaoGerar();
    }


    private void carregarSemana(
            LocalDate data
    ) {

        if (data == null) {

            return;
        }


        carregarProgramacao(
                data
        );
    }


    private void editarSemanaSelecionada() {

        LocalDate data =
                view.getListaSemanas()
                        .getSelectionModel()
                        .getSelectedItem();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione uma semana para editar."
            );

            return;
        }


        carregarProgramacao(
                data
        );
    }


    private void carregarProgramacao(
            LocalDate data
    ) {

        try {

            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);


            atualizarListas(
                    programacao
            );


            view.atualizarStatus(
                    "Configurando a reunião de "
                            + data
                            .format(
                                    java.time.format.DateTimeFormatter
                                            .ofPattern("dd/MM/yyyy")
                            )
                            + "."
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


        atualizarStatusSemana(
                programacao.data()
        );
    }


    private void adicionarSemana() {

        LocalDate ultimaSemana =
                semanas.isEmpty()
                        ? null
                        : semanas.get(
                        semanas.size() - 1
                );


        if (ultimaSemana == null) {

            LocalDate mes =
                    view.getCampoData()
                            .getValue();


            if (mes == null) {

                view.atualizarStatus(
                        "Selecione primeiro o mês."
                );

                return;
            }


            semanas.addAll(
                    obterSemanasIniciais(
                            YearMonth.from(mes)
                    )
            );

        } else {

            LocalDate novaSemana =
                    ultimaSemana.plusWeeks(1);


            YearMonth mes =
                    YearMonth.from(
                            view.getCampoData()
                                    .getValue()
                    );


            /*
             * A nova semana deve pertencer ao
             * mês selecionado.
             */
            if (!YearMonth.from(novaSemana)
                    .equals(mes)) {

                view.atualizarStatus(
                        "Não há outra quinta-feira neste mês."
                );

                return;
            }


            if (!semanas.contains(novaSemana)) {

                semanas.add(
                        novaSemana
                );
            }
        }


        atualizarSemanas();


        view.getListaSemanas()
                .getSelectionModel()
                .select(
                        semanas.size() - 1
                );


        view.atualizarStatus(
                "Nova semana adicionada."
        );
    }


    private void atualizarStatusSemana(
            LocalDate data
    ) {

        if (!semanas.contains(data)) {

            return;
        }


        boolean configurada =
                service.estaConfigurada(data);


        view.atualizarStatusSemana(
                data,
                configurada
        );


        atualizarBotaoGerar();
    }


    private void atualizarBotaoGerar() {

        if (semanas.isEmpty()) {

            view.atualizarBotaoGerar(
                    false
            );

            return;
        }


        boolean todasConfiguradas =
                semanas.stream()
                        .allMatch(
                                service::estaConfigurada
                        );


        view.atualizarBotaoGerar(
                todasConfiguradas
        );
    }


    private LocalDate obterDataSelecionada() {

        LocalDate data =
                view.getListaSemanas()
                        .getSelectionModel()
                        .getSelectedItem();


        if (data != null) {

            return data;
        }


        return view.getCampoData()
                .getValue();
    }


    private void adicionarParte() {

        LocalDate data =
                obterDataSelecionada();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione uma reunião."
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


            carregarProgramacao(
                    data
            );


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
                obterDataSelecionada();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione uma reunião."
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


            carregarProgramacao(
                    data
            );


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
                obterDataSelecionada();


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
                obterDataSelecionada();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione uma reunião."
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