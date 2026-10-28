package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.service.HorarioS140Service;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import br.com.geradordesignacoes.service.ConfiguracaoSalasService;
import br.com.geradordesignacoes.view.programacao.ProgramacaoView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramacaoController {

    private final ProgramacaoView view;

    private final ProgramacaoSemanaService service;

    private final HorarioS140Service horarioS140Service;

    private final EscalaController escalaController;

    private final ConfiguracaoGeralService configuracaoGeralService;

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
        this.horarioS140Service = new HorarioS140Service();

        this.configuracaoGeralService = new ConfiguracaoGeralService();

        ConfiguracaoSalasService configuracaoSalasService = new ConfiguracaoSalasService();
        view.definirVisibilidadeConselheiroSalaB(
                configuracaoSalasService.buscar().salaBHabilitada()
        );

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
                        _ -> carregarMes()
                );


        view.getListaSemanas()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (_, _, atual) ->
                                carregarSemana(atual)
                );


        view.getBotaoAdicionarSemana()
                .setOnAction(
                        _ -> adicionarSemana()
                );


        view.getBotaoEditarSemana()
                .setOnAction(
                        _ -> editarSemanaSelecionada()
                );


        /*
         * Agora a alteração da programação acontece
         * diretamente através dos CheckBoxes.
         */
        view.setOnParteSelecionadaChanged(
                this::alterarParteSelecionada
        );


        /*
         * Clicar numa parte continua permitindo
         * carregar o tema correspondente.
         */
        view.getListaPartes()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (observable, _, atual) ->
                                selecionarParte(atual)
                );


        view.getBotaoSalvarTema()
                .setOnAction(
                        _ -> salvarTema()
                );

        view.getBotaoSalvarNumeroOficial()
                .setOnAction(
                        _ -> salvarNumeroOficial()
                );

        view.getBotaoSalvarDuracao()
                .setOnAction(_ -> salvarDuracao());

        view.getBotaoSalvarDadosS140()
                .setOnAction(_ -> salvarDadosS140());

        view.getBotaoHorariosS140().setOnAction(_ -> abrirHorariosS140());
        view.setOnSalvarAjustesHorarios((chaves, ajustes) -> {
            LocalDate dataSelecionada = obterDataSelecionada();
            if (dataSelecionada == null) {
                view.atualizarStatus("Selecione uma reunião para ajustar os horários.");
                return;
            }
            try {
                for (String chave : chaves) {
                    LocalTime horario = ajustes.get(chave);
                    if (horario == null) {
                        horarioS140Service.removerAjusteManual(dataSelecionada, chave);
                    } else {
                        horarioS140Service.ajustarHorario(dataSelecionada, chave, horario);
                    }
                }
                view.atualizarStatus("Ajustes de horário do S-140 salvos.");
                abrirHorariosS140();
            } catch (Exception e) {
                view.atualizarStatus("Erro ao salvar os horários: " + e.getMessage());
            }
        });


        view.getBotaoGerar()
                .setOnAction(
                        _ -> gerarEscalas()
                );


        view.getBotaoSalvar()
                .setOnAction(
                        _ -> salvarEscalas()
                );
    }


    private void abrirHorariosS140() {
        LocalDate dataSelecionada = obterDataSelecionada();
        if (dataSelecionada == null) {
            view.atualizarStatus("Selecione uma reunião para visualizar os horários.");
            return;
        }

        try {
            ProgramacaoSemana programacao = service.buscarOuPreparar(dataSelecionada);
            List<HorarioS140Calculado> horarios = horarioS140Service.calcular(programacao);
            view.abrirDialogoHorariosS140(
                    horarios,
                    horarioS140Service.listarAjustesManuais(dataSelecionada)
            );
        } catch (Exception e) {
            view.atualizarStatus("Erro ao calcular os horários do S-140: " + e.getMessage());
        }
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

        DayOfWeek diaReuniao = configuracaoGeralService.buscar()
                .diaReuniaoHabitual();

        LocalDate data =
                mes.atDay(1);

        while (data.getMonth()
                == mes.getMonth()) {

            if (data.getDayOfWeek() == diaReuniao) {
                resultado.add(data);
            }

            data = data.plusDays(1);
        }

        // Inclui também datas já importadas que não coincidam com o dia habitual.
        // A consulta é somente de leitura e não cria programações.
        resultado.addAll(service.listarDatasProgramadasNoMes(mes));

        return resultado.stream()
                .distinct()
                .sorted()
                .toList();
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
                    service.buscarOuPreparar(data);


            atualizarListaPartes(programacao);
            view.carregarConselheirosSalaB(service.listarConselheirosSalaB());
            view.carregarDadosS140(
                    programacao.canticoInicial(),
                    programacao.canticoMeio(),
                    programacao.canticoFinal(),
                    programacao.conselheiroSalaBId()
            );

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

        List<Parte> todasAsPartes =
                service.listarTodas();




        /*
         * Primeiro carregamos todas as partes variáveis.
         */
        view.carregarPartes(
                todasAsPartes
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

        Map<Integer, Integer> numerosOficiais =
                new HashMap<>();

        programacao.partes()
                .forEach(
                        programacaoParte ->
                                numerosOficiais.put(
                                        programacaoParte.getParte().getId(),
                                        programacaoParte.getNumeroOficial()
                                )
                );

        view.carregarNumerosOficiais(numerosOficiais);


        view.getCampoTema()
                .clear();

        view.getCampoNumeroOficial().clear();
        view.getCampoDuracao().clear();

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
                        "Não há outra quarta-feira neste mês."
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

            view.getCampoTema().clear();
            view.getCampoNumeroOficial().clear();
            view.getCampoDuracao().clear();
            return;
        }


        LocalDate data =
                obterDataSelecionada();


        if (data == null) {
            return;
        }


        try {

            ProgramacaoSemana programacao =
                    service.buscarOuPreparar(data);


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
                            programacaoParte -> {
                                view.getCampoTema()
                                        .setText(
                                                programacaoParte
                                                        .getTema()
                                        );
                                view.getCampoNumeroOficial()
                                        .setText(
                                                programacaoParte
                                                        .getNumeroOficial()
                                                        == null
                                                        ? ""
                                                        : programacaoParte
                                                        .getNumeroOficial()
                                                        .toString()
                                        );
                                view.getCampoDuracao().setText(
                                        programacaoParte.getDuracaoMinutos() == null
                                                ? ""
                                                : programacaoParte.getDuracaoMinutos().toString()
                                );
                            },
                            () -> {
                                view.getCampoTema().clear();
                                view.getCampoNumeroOficial().clear();
                                view.getCampoDuracao().clear();
                            }
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

    private void salvarNumeroOficial() {

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
                    "Selecione uma parte para informar o número oficial."
            );
            return;
        }

        if (!view.isParteSelecionada(parte.getId())) {
            view.atualizarStatus(
                    "Selecione a parte antes de informar o número oficial."
            );
            return;
        }

        String texto =
                view.getCampoNumeroOficial()
                        .getText()
                        .trim();

        Integer numeroOficial;

        if (texto.isEmpty()) {
            numeroOficial = null;
        } else {
            try {
                numeroOficial = Integer.valueOf(texto);
            } catch (NumberFormatException e) {
                view.atualizarStatus(
                        "Informe um número oficial inteiro positivo ou deixe o campo vazio."
                );
                return;
            }
        }

        try {
            ProgramacaoSemana programacao =
                    service.obterOuCriar(data);

            ProgramacaoParte programacaoParte =
                    programacao.partes()
                            .stream()
                            .filter(
                                    item ->
                                            item.getParte().getId()
                                                    .equals(parte.getId())
                            )
                            .findFirst()
                            .orElseThrow(
                                    () ->
                                            new IllegalArgumentException(
                                                    "Parte não encontrada na programação."
                                            )
                            );

            service.definirNumeroOficial(
                    data,
                    programacaoParte.getOrdem(),
                    numeroOficial
            );

            carregarProgramacao(data);

            view.atualizarStatus(
                    "Número oficial salvo com sucesso."
            );
        } catch (IllegalArgumentException e) {
            view.atualizarStatus(
                    e.getMessage()
            );
        } catch (Exception e) {
            view.atualizarStatus(
                    "Erro ao salvar número oficial: "
                            + e.getMessage()
            );
            e.printStackTrace();
        }
    }


    private void salvarDadosS140() {
        LocalDate data = obterDataSelecionada();
        if (data == null) {
            view.atualizarStatus("Selecione uma reunião.");
            return;
        }

        Pessoa conselheiro = view.getCampoConselheiroSalaB()
                .getSelectionModel().getSelectedItem();
        try {
            service.salvarDadosS140(
                    data,
                    conselheiro == null ? null : conselheiro.getId(),
                    view.getCampoCanticoInicial().getText(),
                    view.getCampoCanticoMeio().getText(),
                    view.getCampoCanticoFinal().getText()
            );
            view.atualizarStatus("Dados do S-140 salvos com sucesso.");
        } catch (IllegalArgumentException e) {
            view.atualizarStatus(e.getMessage());
        } catch (Exception e) {
            view.atualizarStatus("Erro ao salvar os dados do S-140: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void salvarDuracao() {
        LocalDate data = obterDataSelecionada();

        if (data == null) {
            view.atualizarStatus("Selecione uma reunião.");
            return;
        }

        Parte parte = view.getListaPartes()
                .getSelectionModel()
                .getSelectedItem();

        if (parte == null || !view.isParteSelecionada(parte.getId())) {
            view.atualizarStatus(
                    "Selecione uma parte que esteja na programação."
            );
            return;
        }

        String texto = view.getCampoDuracao().getText().trim();
        Integer duracao;

        if (texto.isEmpty()) {
            duracao = null;
        } else {
            try {
                duracao = Integer.valueOf(texto);
                if (duracao <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                view.atualizarStatus(
                        "Informe uma duração inteira positiva em minutos ou deixe o campo vazio."
                );
                return;
            }
        }

        try {
            ProgramacaoSemana programacao = service.obterOuCriar(data);
            ProgramacaoParte ocorrencia = programacao.partes().stream()
                    .filter(item -> item.getParte().getId().equals(parte.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Parte não encontrada na programação."
                    ));

            service.definirDuracaoMinutos(
                    data,
                    ocorrencia.getOrdem(),
                    duracao
            );
            carregarProgramacao(data);
            view.getListaPartes().getSelectionModel().select(parte);
            selecionarParte(parte);
            view.atualizarStatus("Duração salva com sucesso.");
        } catch (Exception e) {
            view.atualizarStatus(
                    "Erro ao salvar duração: " + e.getMessage()
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

    public static String nomeArquivoS140(YearMonth mes) {
        String nomeMes = mes.getMonth().getDisplayName(
                java.time.format.TextStyle.FULL, java.util.Locale.forLanguageTag("pt-BR"));
        nomeMes = nomeMes.substring(0, 1).toUpperCase(java.util.Locale.forLanguageTag("pt-BR"))
                + nomeMes.substring(1);
        return "Programacao_reuniao_meio_de_semana_" + nomeMes + "_" + mes.getYear() + ".docx";
    }


    private void salvarEscalas() {

        if (!escalaController.confirmarCorrecaoRetroativa()) {
            view.atualizarStatus(
                    "Salvamento cancelado. O histórico não foi alterado."
            );
            return;
        }

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