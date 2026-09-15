package br.com.geradordesignacoes.controller;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;
import br.com.geradordesignacoes.service.BackupService;
import br.com.geradordesignacoes.service.GeradorEscala;
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
    private final EscalaDAO escalaDAO;
    private final BackupService backupService;
    private final ProgramacaoSemanaService programacaoSemanaService;
    private final Map<LocalDate, ResultadoGeracaoEscala> resultadosGeracao;

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
                    obterDataSelecionada();

            if (data == null) {
                informarDataObrigatoria();
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

            registrarResultado(
                    data,
                    ultimoResultado
            );

            preencherTabela(
                    ultimoResultado
                            .escala()
                            .getDesignacoes()
            );

            atualizarResumo();

        } catch (Exception e) {

            tratarErroGeracao(
                    "Erro ao gerar escala.",
                    e
            );
        }
    }

    private LocalDate obterDataSelecionada() {

        return view.getCampoData()
                .getValue();
    }

    private void informarDataObrigatoria() {

        view.atualizarStatus(
                "Informe a data da reunião."
        );

        view.atualizarResumo("");
    }

    private void registrarResultado(
            LocalDate data,
            ResultadoGeracaoEscala resultado
    ) {
        resultadosGeracao.clear();

        resultadosGeracao.put(
                data,
                resultado
        );

        escalaSalva = false;
    }

    private void preencherTabela(
            List<Designacao> designacoes
    ) {

        view.getTabela()
                .getItems()
                .clear();

        for (
                int indice = 0;
                indice < designacoes.size();
                indice++
        ) {

            Designacao designacao =
                    designacoes.get(indice);

            String ajudante =
                    obterNomeAjudante(
                            designacao
                    );

            ItemEscala item =
                    new ItemEscala(
                            indice,
                            designacao.parte().getNome(),
                            designacao.responsavel().getNome(),
                            ajudante
                    );

            view.getTabela()
                    .getItems()
                    .add(item);
        }
    }

    private String obterNomeAjudante(
            Designacao designacao
    ) {

        if (designacao.ajudante() == null) {
            return "";
        }

        return designacao.ajudante()
                .getNome();
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

            adicionarErrosAoResumo(
                    resumo
            );

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

    private void adicionarErrosAoResumo(
            StringBuilder resumo
    ) {

        resumo.append(
                "\n\nPartes não geradas:\n"
        );

        for (String erro : ultimoResultado.erros()) {

            resumo.append("• ")
                    .append(erro)
                    .append("\n");
        }
    }

    public void substituirDesignacao(
            int indice,
            Pessoa novoResponsavel,
            Pessoa novoAjudante
    ) {

        if (ultimoResultado == null) {
            throw new IllegalStateException(
                    "Nenhuma escala foi gerada."
            );
        }

        Escala escala =
                ultimoResultado.escala();

        validarIndiceDesignacao(
                indice,
                escala
        );

        Designacao designacaoAtual =
                escala.getDesignacoes()
                        .get(indice);

        Designacao novaDesignacao =
                new Designacao(
                        designacaoAtual.data(),
                        designacaoAtual.parte(),
                        novoResponsavel,
                        novoAjudante
                );

        escala.substituirDesignacao(
                indice,
                novaDesignacao
        );

        escalaSalva = false;

        preencherTabela(
                escala.getDesignacoes()
        );

        atualizarResumo();
    }

    private void validarIndiceDesignacao(
            int indice,
            Escala escala
    ) {

        if (
                indice < 0
                        || indice >= escala.getDesignacoes().size()
        ) {
            throw new IndexOutOfBoundsException(
                    "Índice da designação inválido."
            );
        }
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

            informarNenhumaEscalaGerada();

            return false;
        }

        try {

            int quantidadeSalva =
                    salvarResultadosGeracao();

            if (quantidadeSalva == 0) {
                throw new IllegalStateException(
                        "Nenhuma escala válida para salvar."
                );
            }

            backupService.criarBackup();

            escalaSalva = true;

            informarSalvamentoSucesso(
                    quantidadeSalva
            );

            return true;

        } catch (Exception e) {

            informarErroSalvamento(e);

            return false;
        }
    }

    private int salvarResultadosGeracao() {

        int quantidadeSalva = 0;

        for (
                ResultadoGeracaoEscala resultado
                :
                resultadosGeracao.values()
        ) {

            if (resultado == null) {
                continue;
            }

            escalaDAO.salvar(
                    resultado.escala()
            );

            quantidadeSalva++;
        }

        return quantidadeSalva;
    }

    private void informarNenhumaEscalaGerada() {

        view.atualizarStatus(
                "Nenhuma escala gerada."
        );

        view.atualizarResumo(
                "Gere uma escala antes de salvar."
        );
    }

    private void informarSalvamentoSucesso(
            int quantidadeSalva
    ) {

        view.atualizarStatus(
                quantidadeSalva
                        + " escala(s) salva(s) com sucesso."
        );

        view.atualizarResumo(
                "As designações foram salvas no histórico."
        );
    }

    private void informarErroSalvamento(
            Exception e
    ) {

        view.atualizarStatus(
                "Erro ao salvar as escalas."
        );

        view.atualizarResumo(
                e.getMessage()
        );

        e.printStackTrace();
    }

    public boolean gerarEscalasDoMes(
            YearMonth mes
    ) {

        if (mes == null) {

            view.atualizarStatus(
                    "Selecione um mês."
            );

            return false;
        }

        try {

            resultadosGeracao.clear();

            List<ProgramacaoSemana> semanas =
                    programacaoSemanaService
                            .listarSemanasDoMes(mes);

            if (semanas.size() < 4 || semanas.size() > 5) {

                view.atualizarStatus(
                        "O mês deve possuir 4 ou 5 programações."
                );

                return false;
            }

            gerarEscalasDasSemanas(
                    semanas
            );

            validarQuantidadeEscalasGeradas();

            escalaSalva = false;

            view.exibirEscalas(
                    resultadosGeracao
            );

            view.atualizarStatus(
                    resultadosGeracao.size()
                            + " escalas geradas com sucesso."
            );

            return true;

        } catch (Exception e) {

            tratarErroGeracaoMensal(e);

            return false;
        }
    }

    private void gerarEscalasDasSemanas(
            List<ProgramacaoSemana> semanas
    ) {

        for (ProgramacaoSemana semana : semanas) {

            ResultadoGeracaoEscala resultado =
                    gerarEscalaDaSemana(
                            semana
                    );

            resultadosGeracao.put(
                    semana.data(),
                    resultado
            );
        }
    }

    private ResultadoGeracaoEscala gerarEscalaDaSemana(
            ProgramacaoSemana semana
    ) {

        List<Parte> partes =
                obterPartesDaSemana(
                        semana
                );

        ResultadoGeracaoEscala resultado =
                geradorEscala.gerarEscala(
                        semana.data(),
                        partes
                );

        if (
                resultado == null
                        || resultado.escala() == null
        ) {
            throw new IllegalStateException(
                    "Não foi possível gerar a escala de "
                            + semana.data()
            );
        }

        return resultado;
    }

    private List<Parte> obterPartesDaSemana(
            ProgramacaoSemana semana
    ) {

        return semana.partes()
                .stream()
                .map(ProgramacaoParte::getParte)
                .toList();
    }

    private void validarQuantidadeEscalasGeradas() {

        int quantidade =
                resultadosGeracao.size();

        if (quantidade < 4 || quantidade > 5) {

            throw new IllegalStateException(
                    "A geração mensal deveria produzir "
                            + "4 ou 5 escalas, mas produziu "
                            + quantidade
                            + "."
            );
        }
    }

    private void tratarErroGeracaoMensal(
            Exception e
    ) {

        view.atualizarStatus(
                "Erro ao gerar as escalas."
        );

        view.atualizarResumo(
                e.getMessage()
        );

        e.printStackTrace();

        resultadosGeracao.clear();
    }

    private void tratarErroGeracao(
            String mensagem,
            Exception e
    ) {

        view.atualizarStatus(
                mensagem
        );

        view.atualizarResumo(
                e.getMessage()
        );

        e.printStackTrace();
    }

    public boolean possuiEscalasGeradas() {

        int quantidade =
                resultadosGeracao.size();

        return quantidade >= 4
                && quantidade <= 5;
    }
}