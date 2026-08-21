package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.service.BackupService;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.HistoricoDesignacoesService;
import br.com.geradordesignacoes.service.ParteService;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import br.com.geradordesignacoes.service.RegrasService;
import br.com.geradordesignacoes.view.escala.EscalaView;
import br.com.geradordesignacoes.view.escala.ItemEscala;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EscalaController {

    private final EscalaView view;

    private final ParteService parteService;

    private final GeradorEscala geradorEscala;

    private final HistoricoDesignacoesService historicoService;

    private ResultadoGeracaoEscala ultimoResultado;

    private boolean escalaSalva;

    private final EscalaDAO escalaDAO;

    private final BackupService backupService;

    private final ProgramacaoSemanaService programacaoSemanaService;

    private final Map<LocalDate, ResultadoGeracaoEscala> resultadosGeracao;


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


        escalaDAO =
                new EscalaDAO();


        backupService =
                new BackupService();


        programacaoSemanaService =
                new ProgramacaoSemanaService();


        resultadosGeracao =
                new LinkedHashMap<>();


        registrarEventos();


        view.atualizarStatus(
                "Aguardando geração da escala..."
        );


        view.atualizarResumo("");
    }


    private void registrarEventos() {

        view.getBotaoGerar()
                .setOnAction(
                        event -> gerarEscala()
                );


        view.getBotaoGerarNovamente()
                .setOnAction(
                        event -> gerarNovamente()
                );


        view.getBotaoSalvar()
                .setOnAction(
                        event -> salvarEscala()
                );
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


            resultadosGeracao.clear();


            resultadosGeracao.put(
                    data,
                    ultimoResultado
            );


            escalaSalva = false;


            preencherTabela(
                    ultimoResultado
                            .escala()
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


            if (designacao.ajudante() != null) {

                ajudante =
                        designacao
                                .ajudante()
                                .getNome();
            }


            ItemEscala item =
                    new ItemEscala(
                            designacao
                                    .parte()
                                    .getNome(),

                            designacao
                                    .responsavel()
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


        StringBuilder resumo =
                new StringBuilder();


        resumo.append("Designações: ")
                .append(quantidade);


        if (ultimoResultado.possuiErros()) {

            view.atualizarStatus(
                    "Escala gerada com pendências."
            );


            resumo.append(
                    "\n\nPartes não geradas:\n"
            );


            for (String erro : ultimoResultado.erros()) {

                resumo.append("• ")
                        .append(erro)
                        .append("\n");
            }

        } else {

            view.atualizarStatus(
                    "Escala gerada com sucesso."
            );


            resumo.append(
                    "\n\nNenhuma pendência encontrada."
            );
        }


        view.atualizarResumo(
                resumo.toString()
        );
    }


    private void gerarNovamente() {

        view.getTabela()
                .getItems()
                .clear();


        ultimoResultado = null;

        resultadosGeracao.clear();

        escalaSalva = false;


        view.atualizarStatus(
                "Aguardando geração da escala..."
        );


        view.atualizarResumo("");
    }


    private boolean salvarEscala() {

        return salvarEscalasGeradas();
    }


    /**
     * Salva no banco todas as escalas atualmente geradas.
     *
     * Este métodoo é utilizado tanto pela tela Escala
     * quanto pela tela Programação.
     *
     * @return true se o salvamento foi realizado com sucesso.
     */
    public boolean salvarEscalasGeradas() {

        if (escalaSalva) {

            view.atualizarStatus(
                    "As escalas já foram salvas."
            );

            return true;
        }


        if (resultadosGeracao.isEmpty()) {

            view.atualizarStatus(
                    "Nenhuma escala gerada."
            );


            view.atualizarResumo(
                    "Gere uma escala antes de salvar."
            );


            return false;
        }


        try {

            int quantidadeSalva = 0;


            for (
                    ResultadoGeracaoEscala resultado
                    :
                    resultadosGeracao.values()
            ) {

                if (resultado == null) {
                    continue;
                }


                Escala escala =
                        resultado.escala();


                escalaDAO.salvar(
                        escala
                );


                quantidadeSalva++;
            }


            if (quantidadeSalva == 0) {

                throw new IllegalStateException(
                        "Nenhuma escala válida para salvar."
                );
            }


            backupService.criarBackup();


            escalaSalva = true;


            view.atualizarStatus(
                    quantidadeSalva
                            + " escala(s) salva(s) com sucesso."
            );


            view.atualizarResumo(
                    "As designações foram salvas no histórico."
            );


            return true;


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao salvar as escalas."
            );


            view.atualizarResumo(
                    e.getMessage()
            );


            e.printStackTrace();


            return false;
        }
    }


    public void gerarEscalasDoMes(
            YearMonth mes
    ) {

        if (mes == null) {

            view.atualizarStatus(
                    "Selecione um mês."
            );

            return;
        }


        try {

            List<ProgramacaoSemana> semanas =
                    programacaoSemanaService
                            .listarSemanasDoMes(mes);


            if (semanas.size() != 4) {

                view.atualizarStatus(
                        "É necessário possuir 4 programações configuradas."
                );

                return;
            }


            resultadosGeracao.clear();


            for (ProgramacaoSemana semana : semanas) {

                List<Parte> partes =
                        semana.partes()
                                .stream()
                                .map(
                                        ProgramacaoParte::getParte
                                )
                                .toList();


                ResultadoGeracaoEscala resultado =
                        geradorEscala.gerarEscala(
                                semana.data(),
                                partes
                        );


                resultadosGeracao.put(
                        semana.data(),
                        resultado
                );
            }


            escalaSalva = false;


            view.exibirEscalas(
                    resultadosGeracao
            );


            view.atualizarStatus(
                    "4 escalas geradas com sucesso."
            );


        } catch (Exception e) {

            view.atualizarStatus(
                    "Erro ao gerar as escalas."
            );


            view.atualizarResumo(
                    e.getMessage()
            );


            e.printStackTrace();
        }
    }
}