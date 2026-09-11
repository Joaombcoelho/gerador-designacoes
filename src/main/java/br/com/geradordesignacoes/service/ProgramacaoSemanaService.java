package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProgramacaoSemanaService {

    private final ParteDAO parteDAO;

    private final ProgramacaoSemanaDAO programacaoSemanaDAO;

    private final ProgramacaoParteDAO programacaoParteDAO;


    public ProgramacaoSemanaService() {

        this.parteDAO =
                new ParteDAO();

        this.programacaoSemanaDAO =
                new ProgramacaoSemanaDAO();

        this.programacaoParteDAO =
                new ProgramacaoParteDAO();
    }


    /**
     * Busca a programação de uma semana já cadastrada.
     * Caso não exista, cria uma nova programação
     * contendo as partes cadastradas no sistema.
     */
    public ProgramacaoSemana obterOuCriar(
            LocalDate data
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        ProgramacaoSemana existente =
                programacaoSemanaDAO.buscarPorData(data);


        if (existente != null) {
            return existente;
        }


        ProgramacaoSemana nova =
                criarProgramacao(data);


        return programacaoSemanaDAO.salvar(nova);
    }


    /**
     * Define ou altera o tema de uma parte
     * de uma programação semanal.
     */
    public void definirTema(
            LocalDate data,
            int ordem,
            String tema
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        if (ordem <= 0) {
            throw new IllegalArgumentException(
                    "A ordem deve ser maior que zero."
            );
        }


        ProgramacaoSemana programacao =
                obterOuCriar(data);


        ProgramacaoParte programacaoParte =
                programacao.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getOrdem() == ordem
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Parte da programação não encontrada."
                                        )
                        );


        programacaoParte.setTema(tema);


        programacaoParteDAO.atualizarTema(
                programacaoParte.getId(),
                tema
        );
    }


    /**
     * Cria a programação semanal em memória.
     */
    private ProgramacaoSemana criarProgramacao(
            LocalDate data
    ) {

        List<Parte> partes =
                parteDAO.listarTodos();


        List<Parte> partesFixas =
                partes.stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA
                        )
                        .sorted(
                                Comparator.comparingInt(
                                        this::obterOrdemParte
                                )
                        )
                        .toList();


        ProgramacaoSemana programacao =
                new ProgramacaoSemana(data);


        int ordem = 1;


        for (Parte parte : partesFixas) {

            programacao.adicionarParte(
                    new ProgramacaoParte(
                            parte,
                            ordem++
                    )
            );
        }


        return programacao;
    }


    /**
     * Define a ordem padrão das partes da reunião.
     */
    private List<Parte> ordenarPartes(
            List<Parte> partes
    ) {

        List<Parte> ordenadas =
                new ArrayList<>(partes);


        ordenadas.sort(
                Comparator.comparingInt(
                        this::obterOrdemParte
                )
        );


        return ordenadas;
    }


    /**
     * Ordem padrão das partes da reunião
     * A ordem é baseada no nome da parte porque,
     * neste momento, a tabela parte ainda não possui
     * uma coluna específica para armazenar a ordem.
     */
    private int obterOrdemParte(
            Parte parte
    ) {

        return switch (parte.getNome()) {

            case "Presidente" ->
                    1;

            case "Oração inicial" ->
                    2;

            case "Discurso — Tesouros" ->
                    3;

            case "Joias Espirituais" ->
                    4;

            case "Leitura" ->
                    5;

            case "Iniciando Conversas" ->
                    6;

            case "Cultivando Interesse" ->
                    7;

            case "O Que Você Diria?" ->
                    8;

            case "Fazendo Discípulos" ->
                    9;

            case "Explicando suas crenças" ->
                    10;

            case "Discurso — Ministério" ->
                    11;

            case "Parte 1" ->
                    12;

            case "Parte 2" ->
                    13;

            case "Parte 3" ->
                    14;

            case "Necessidades Locais" ->
                    15;

            case "Estudo Bíblico" ->
                    16;

            case "Oração final" ->
                    17;

            default ->
                    999;
        };
    }


    public List<Parte> listarPartesVariaveis() {

        return parteDAO.listarTodos()
                .stream()
                .filter(parte ->
                        parte.getTipoVariacao()
                                == TipoVariacaoParte.VARIAVEL
                )
                .sorted(
                        Comparator.comparing(
                                Parte::getSecao,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();
    }


    public void validarPartesVariaveis(
            List<Parte> partesSelecionadas
    ) {

        if (partesSelecionadas == null) {
            throw new IllegalArgumentException(
                    "A lista de partes selecionadas não pode ser nula."
            );
        }


        int quantidade =
                partesSelecionadas.size();


        if (quantidade < 3 || quantidade > 6) {

            throw new IllegalArgumentException(
                    "A programação deve possuir entre "
                            + "3 e 6 partes variáveis."
            );
        }


        boolean possuiParteNaoVariavel =
                partesSelecionadas.stream()
                        .anyMatch(parte ->
                                parte.getTipoVariacao()
                                        != TipoVariacaoParte.VARIAVEL
                        );


        if (possuiParteNaoVariavel) {

            throw new IllegalArgumentException(
                    "Somente partes variáveis podem ser selecionadas."
            );
        }


        boolean possuiParteDuplicada =
                partesSelecionadas.stream()
                        .map(Parte::getId)
                        .distinct()
                        .count()
                        != partesSelecionadas.size();


        if (possuiParteDuplicada) {

            throw new IllegalArgumentException(
                    "Não é permitido selecionar a mesma parte variável mais de uma vez."
            );
        }
    }


    public ProgramacaoSemana montarProgramacao(
            LocalDate data,
            List<Parte> partesVariaveisSelecionadas
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        validarPartesVariaveis(
                partesVariaveisSelecionadas
        );


        List<Parte> todasPartes =
                parteDAO.listarTodos();


        List<Parte> partesFixas =
                todasPartes.stream()
                        .filter(parte ->
                                parte.getTipoVariacao()
                                        == TipoVariacaoParte.FIXA
                        )
                        .toList();


        List<Parte> partesSelecionadas =
                new ArrayList<>();


        partesSelecionadas.addAll(
                partesFixas
        );

        partesSelecionadas.addAll(
                partesVariaveisSelecionadas
        );


        List<Parte> partesOrdenadas =
                ordenarPartes(
                        partesSelecionadas
                );


        ProgramacaoSemana programacao =
                new ProgramacaoSemana(data);


        int ordem = 1;


        for (Parte parte : partesOrdenadas) {

            programacao.adicionarParte(
                    new ProgramacaoParte(
                            parte,
                            ordem++
                    )
            );
        }


        return programacao;
    }


    public void adicionarParteVariavel(
            LocalDate data,
            Integer parteId
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        if (parteId == null) {
            throw new IllegalArgumentException(
                    "O ID da parte não pode ser nulo."
            );
        }


        ProgramacaoSemana programacao =
                obterOuCriar(data);


        Parte parte =
                parteDAO.buscarPorId(parteId)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Parte não encontrada."
                                        )
                        );


        if (parte.getTipoVariacao()
                != TipoVariacaoParte.VARIAVEL) {

            throw new IllegalArgumentException(
                    "Somente partes variáveis podem ser adicionadas."
            );
        }


        boolean jaExiste =
                programacao.partes()
                        .stream()
                        .anyMatch(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getId()
                                                .equals(parte.getId())
                        );


        if (jaExiste) {

            throw new IllegalArgumentException(
                    "Esta parte já está adicionada à semana."
            );
        }


        long quantidadeVariaveis =
                programacao.partes()
                        .stream()
                        .filter(
                                programacaoParte ->
                                        programacaoParte
                                                .getParte()
                                                .getTipoVariacao()
                                                == TipoVariacaoParte.VARIAVEL
                        )
                        .count();


        if (quantidadeVariaveis >= 6) {

            throw new IllegalArgumentException(
                    "A semana já possui o máximo de 6 partes variáveis."
            );
        }


        int novaOrdem =
                programacao.partes()
                        .stream()
                        .mapToInt(
                                ProgramacaoParte::getOrdem
                        )
                        .max()
                        .orElse(0)
                        + 1;


        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(
                        parte,
                        novaOrdem
                );


        programacaoParteDAO.salvar(
                programacao.id(),
                programacaoParte
        );
        ProgramacaoSemana atualizada =
                programacaoSemanaDAO.buscarPorData(data);

        if (atualizada == null) {
            throw new IllegalStateException(
                    "A programação não foi encontrada após adicionar a parte variável."
            );
        }
    }


    public void removerParteVariavel(
            LocalDate data,
            Integer parteId
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        if (parteId == null) {
            throw new IllegalArgumentException(
                    "O ID da parte não pode ser nulo."
            );
        }


        ProgramacaoSemana programacao =
                programacaoSemanaDAO.buscarPorData(data);


        if (programacao == null) {

            throw new IllegalArgumentException(
                    "Não existe programação para esta semana."
            );
        }


        ProgramacaoParte programacaoParte =
                programacao.partes()
                        .stream()
                        .filter(
                                parte ->
                                        parte.getParte()
                                                .getId()
                                                .equals(parteId)
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Esta parte não está configurada para a semana."
                                        )
                        );


        if (programacaoParte
                .getParte()
                .getTipoVariacao()
                != TipoVariacaoParte.VARIAVEL) {

            throw new IllegalArgumentException(
                    "Partes fixas não podem ser removidas."
            );
        }


        programacaoParteDAO.excluir(
                programacao.id(),
                parteId
        );
    }


    public boolean estaConfigurada(
            LocalDate data
    ) {

        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }


        ProgramacaoSemana programacao =
                programacaoSemanaDAO.buscarPorData(data);


        if (programacao == null) {
            return false;
        }


        return programacao
                .possuiQuantidadeValidaDePartesVariaveis();
    }
    public List<ProgramacaoSemana> listarSemanasDoMes(
            YearMonth mes
    ) {

        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        List<ProgramacaoSemana> semanas =
                new ArrayList<>();

        LocalDate data = mes.atDay(1);

        while (
                data.getMonth() == mes.getMonth()
                        && semanas.size() < 4
        ) {

            if (data.getDayOfWeek() == DayOfWeek.THURSDAY) {

                ProgramacaoSemana programacao =
                        programacaoSemanaDAO.buscarPorData(data);

                if (programacao != null) {
                    semanas.add(programacao);
                }
            }

            data = data.plusDays(1);
        }

        return semanas;
    }
}