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

    private final EscalaController escalaController;

    private final List<LocalDate> semanas;


    public ProgramacaoController(
            ProgramacaoView view,
            EscalaController escalaController
    ) {

        this.view = view;

        this.escalaController =
                escalaController;

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

        view.getCampoData()
                .setOnAction(
                        event -> carregarMes()
                );


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
         * Agora a alteração da programação acontece
         * diretamente através dos CheckBoxes.
         */
        view.setOnParteSelecionadaChanged(
                this::alterarParteSelecionada
        );


        /*
         * Clicar em uma parte continua permitindo
         * carregar o tema correspondente.
         */
        view.getListaPartes()
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


        view.getBotaoGerar()
                .setOnAction(
                        event -> gerarEscalas()
                );


        view.getBotaoSalvar()
                .setOnAction(
                        event -> salvarEscalas()
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


        /*
         * Ao trocar de mês, ainda não existe
         * uma escala gerada para o novo mês.
         */
        view.atualizarBotaoSalvar(false);


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


        carregarProgramacao(data);
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


        carregarProgramacao(data);
    }


    private void carregarProgramacao(
            LocalDate data
    ) {

        try {

            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);


            atualizarListaPartes(programacao);


            /*
             * Ao entrar na configuração de uma semana,
             * a geração anterior deixa de ser considerada
             * como resultado atual.
             */
            view.atualizarBotaoSalvar(false);


            view.atualizarStatus(
                    "Configurando a reunião de "
                            + data.format(
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


    private void atualizarListaPartes(
            ProgramacaoSemana programacao
    ) {

        List<Parte> partesVariaveis =
                service.listarPartesVariaveis();


        /*
         * Primeiro carregamos todas as partes variáveis.
         */
        view.carregarPartes(
                partesVariaveis
        );


        /*
         * Depois marcamos somente as partes que
         * pertencem à programação desta semana.
         */
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
                .forEach(
                        parte ->
                                view.marcarParte(
                                        parte.getId(),
                                        true
                                )
                );


        view.getCampoTema()
                .clear();


        atualizarStatusSemana(
                programacao.data()
        );
    }


    /**
     * Adiciona ou remove uma parte da programação
     * conforme o estado do CheckBox.
     */
    private void alterarParteSelecionada(
            Parte parte
    ) {

        if (parte == null) {
            return;
        }


        LocalDate data =
                obterDataSelecionada();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione uma reunião."
            );

            return;
        }


        boolean selecionada =
                view.isParteSelecionada(
                        parte.getId()
                );


        try {

            if (selecionada) {

                service.adicionarParteVariavel(
                        data,
                        parte.getId()
                );


                view.atualizarStatus(
                        "Parte adicionada à programação."
                );

            } else {

                service.removerParteVariavel(
                        data,
                        parte.getId()
                );


                view.atualizarStatus(
                        "Parte removida da programação."
                );
            }


            atualizarStatusSemana(data);


        } catch (Exception e) {

            /*
             * Se a operação falhar, desfazemos
             * visualmente o CheckBox.
             */
            view.marcarParte(
                    parte.getId(),
                    !selecionada
            );


            view.atualizarStatus(
                    "Não foi possível alterar a parte: "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
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


            LocalDate dataMes =
                    view.getCampoData()
                            .getValue();


            if (dataMes == null) {
                return;
            }


            YearMonth mes =
                    YearMonth.from(dataMes);


            if (!YearMonth.from(novaSemana)
                    .equals(mes)) {

                view.atualizarStatus(
                        "Não há outra quinta-feira neste mês."
                );

                return;
            }


            if (!semanas.contains(novaSemana)) {

                semanas.add(novaSemana);
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

            view.atualizarBotaoGerar(false);

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
                view.getListaPartes()
                        .getSelectionModel()
                        .getSelectedItem();


        if (parte == null) {

            view.atualizarStatus(
                    "Selecione uma parte para informar o tema."
            );

            return;
        }


        /*
         * Não permite salvar tema de uma parte
         * que não esteja selecionada.
         */
        if (!view.isParteSelecionada(
                parte.getId()
        )) {

            view.atualizarStatus(
                    "Selecione a parte antes de informar o tema."
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


    private void gerarEscalas() {

        LocalDate data =
                view.getCampoData()
                        .getValue();


        if (data == null) {

            view.atualizarStatus(
                    "Selecione um mês."
            );

            return;
        }


        escalaController.gerarEscalasDoMes(
                YearMonth.from(data)
        );


        /*
         * Se chegamos até aqui, a geração foi executada.
         * O botão Salvar fica disponível para persistir
         * as escalas geradas.
         */
        view.atualizarBotaoSalvar(true);
    }


    private void salvarEscalas() {

        boolean salvou =
                escalaController.salvarEscalasGeradas();


        if (salvou) {

            view.atualizarStatus(
                    "Escalas salvas com sucesso no banco de dados."
            );


            view.atualizarBotaoSalvar(false);

        } else {

            view.atualizarStatus(
                    "Não foi possível salvar as escalas."
            );
        }
    }
}