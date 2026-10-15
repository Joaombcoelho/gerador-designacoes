package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoParteDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.ConfiguracaoSalas;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.TipoParte;
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
    private final ConfiguracaoSalasService configuracaoSalasService;
    private final PessoaDAO pessoaDAO;
    private final ConfiguracaoGeralService configuracaoGeralService;

    public ProgramacaoSemanaService() {
        this.parteDAO = new ParteDAO();
        this.programacaoSemanaDAO = new ProgramacaoSemanaDAO();
        this.programacaoParteDAO = new ProgramacaoParteDAO();
        this.configuracaoSalasService = new ConfiguracaoSalasService();
        this.pessoaDAO = new PessoaDAO();
        this.configuracaoGeralService = new ConfiguracaoGeralService();
    }

    /**
     * Lista todas as partes cadastradas no sistema.
     *
     * Inclui partes FIXAS e VARIÁVEIS.
     *
     * A ordenação segue a ordem padrão das partes
     * da reunião.
     */
    public List<Parte> listarTodas() {
        return ordenarPartes(
                parteDAO.listarTodos()
        );
    }

    /**
     * Lista somente as partes variáveis.
     */
    public List<Parte> listarPartesVariaveis() {
        return parteDAO.listarTodos()
                .stream()
                .filter(this::ehParteVariavel)
                .sorted(comparadorDeOrdem())
                .toList();
    }

    /**
     * Busca a programação de uma semana já cadastrada.
     *
     * Caso não exista, cria uma nova programação contendo
     * todas as partes fixas.
     *
     * Caso já exista, garante que todas as partes fixas
     * atualmente cadastradas no sistema também estejam
     * presentes na programação.
     */
    public ProgramacaoSemana obterOuCriar(
            LocalDate data
    ) {
        validarData(data);

        ProgramacaoSemana existente =
                programacaoSemanaDAO.buscarPorData(data);

        if (existente != null) {
            sincronizarPartesFixas(existente);
            atribuirNumeroPadraoLeitura(existente);

            return programacaoSemanaDAO.buscarPorData(data);
        }

        ProgramacaoSemana nova =
                criarProgramacao(data);

        return programacaoSemanaDAO.salvar(nova);
    }

    /**
     * Garante que todas as partes fixas cadastradas
     * no sistema estejam presentes na programação.
     *
     * Partes variáveis não são alteradas.
     */
    private void sincronizarPartesFixas(
            ProgramacaoSemana programacao
    ) {
        List<Parte> partesFixas =
                listarPartesFixasOrdenadas();

        for (Parte parteFixa : partesFixas) {

            if (possuiParte(
                    programacao,
                    parteFixa
            )) {
                continue;
            }

            int novaOrdem =
                    calcularNovaOrdem(
                            programacao,
                            parteFixa
                    );

            ProgramacaoParte programacaoParte = criarProgramacaoParte(
                    parteFixa,
                    novaOrdem
            );

            atribuirNumeroPadraoLeitura(programacaoParte);

            programacaoParteDAO.salvar(
                    programacao.id(),
                    programacaoParte
            );
        }
    }

    /**
     * Calcula a ordem que uma nova parte deve receber.
     */
    private int calcularNovaOrdem(
            ProgramacaoSemana programacao,
            Parte novaParte
    ) {
        List<Parte> partes =
                programacao.partes()
                        .stream()
                        .map(ProgramacaoParte::getParte)
                        .collect(
                                java.util.stream.Collectors.toCollection(
                                        ArrayList::new
                                )
                        );

        if (!possuiParte(
                programacao,
                novaParte
        )) {
            partes.add(novaParte);
        }

        partes.sort(comparadorDeOrdem());

        for (int i = 0; i < partes.size(); i++) {
            if (partes.get(i)
                    .getId()
                    .equals(novaParte.getId())) {
                return i + 1;
            }
        }

        return obterProximaOrdem(programacao);
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
        validarData(data);

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

        String temaNormalizado =
                        normalizarTema(tema);

        programacaoParte.setTema(temaNormalizado);

        programacaoParteDAO.atualizarTema(
                        programacaoParte.getId(),
                        temaNormalizado
        );
    }

    /**
     * Define ou remove a duração de uma ocorrência semanal.
     * Um valor nulo representa uma duração ainda pendente.
     */
    public void definirDuracaoMinutos(
            LocalDate data,
            int ordem,
            Integer duracaoMinutos
    ) {
        validarData(data);

        if (ordem <= 0) {
            throw new IllegalArgumentException(
                    "A ordem deve ser maior que zero."
            );
        }

        if (duracaoMinutos != null && duracaoMinutos <= 0) {
            throw new IllegalArgumentException(
                    "A duração deve ser maior que zero."
            );
        }

        ProgramacaoSemana programacao = obterOuCriar(data);
        ProgramacaoParte programacaoParte = programacao.partes()
                .stream()
                .filter(parte -> parte.getOrdem() == ordem)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parte da programação não encontrada."
                ));

        programacaoParteDAO.atualizarDuracaoMinutos(
                programacaoParte.getId(),
                duracaoMinutos
        );
    }

    private String normalizarTema(String tema) {
        if (tema == null) {
            return null;
        }

        String temaNormalizado =
                        tema.trim();

        return temaNormalizado.isEmpty()
                        ? null
                        : temaNormalizado;
    }

    /**
     * Define ou altera o número oficial de uma parte
     * de uma programação semanal.
     */
    public void definirNumeroOficial(
            LocalDate data,
            int ordem,
            Integer numeroOficial
    ) {
        validarData(data);

        if (ordem <= 0) {
            throw new IllegalArgumentException(
                    "A ordem deve ser maior que zero."
            );
        }

        if (numeroOficial != null && numeroOficial < 1) {
            throw new IllegalArgumentException(
                    "O número oficial deve ser maior que zero."
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

        programacaoParteDAO.atualizarNumeroOficial(
                programacaoParte.getId(),
                numeroOficial
        );
    }

    /**
     * Cria uma nova programação semanal.
     *
     * Uma nova programação recebe automaticamente
     * todas as partes fixas.
     */
    private ProgramacaoSemana criarProgramacao(
            LocalDate data
    ) {
        List<Parte> partesFixas =
                listarPartesFixasOrdenadas();

        ProgramacaoSemana programacao =
                novaProgramacao(data);

        int ordem = 1;

        for (Parte parte : partesFixas) {
            programacao.adicionarParte(
                    criarProgramacaoParte(
                            parte,
                            ordem++
                    )
            );
        }

        return programacao;
    }

    private ProgramacaoParte criarProgramacaoParte(
            Parte parte,
            int ordem
    ) {
        ProgramacaoParte programacaoParte =
                new ProgramacaoParte(
                        parte,
                        ordem
                );
        programacaoParte.setDuracaoMinutos(
                parte.getDuracaoPadraoMinutos()
        );
        atribuirNumeroPadraoLeitura(programacaoParte);

        return programacaoParte;
    }

    private void atribuirNumeroPadraoLeitura(
            ProgramacaoParte programacaoParte
    ) {
        if (programacaoParte.getParte().getTipo()
                != TipoParte.LEITURA
                || programacaoParte.getNumeroOficial() != null) {
            return;
        }

        programacaoParte.setNumeroOficial(3);
    }

    private void atribuirNumeroPadraoLeitura(
            ProgramacaoSemana programacao
    ) {
        programacao.partes()
                .forEach(
                        programacaoParte -> {
                            if (
                                    programacaoParte.getParte().getTipo()
                                            == TipoParte.LEITURA
                                            && programacaoParte
                                            .getNumeroOficial() == null
                            ) {
                                programacaoParte.setNumeroOficial(3);
                                programacaoParteDAO
                                        .atualizarNumeroOficial(
                                                programacaoParte.getId(),
                                                3
                                        );
                            }
                        }
                );
    }

    /**
     * Lista todas as partes fixas em sua ordem padrão.
     */
    private List<Parte> listarPartesFixasOrdenadas() {
        return parteDAO.listarTodos()
                .stream()
                .filter(this::ehParteFixa)
                .sorted(comparadorDeOrdem())
                .toList();
    }

    /**
     * Ordena as partes pela ordem padrão da reunião.
     */
    private List<Parte> ordenarPartes(
            List<Parte> partes
    ) {
        List<Parte> ordenadas =
                new ArrayList<>(partes);

        ordenadas.sort(comparadorDeOrdem());

        return ordenadas;
    }

    /**
     * Comparador padrão das partes da reunião.
     */
    private Comparator<Parte> comparadorDeOrdem() {
        return Comparator.comparingInt(
                this::obterOrdemParte
        );
    }

    /**
     * Verifica se uma parte é fixa.
     */
    private boolean ehParteFixa(
            Parte parte
    ) {
        return parte.getTipoVariacao()
                == TipoVariacaoParte.FIXA;
    }

    /**
     * Verifica se uma parte é variável.
     */
    private boolean ehParteVariavel(
            Parte parte
    ) {
        return parte.getTipoVariacao()
                == TipoVariacaoParte.VARIAVEL;
    }

    /**
     * Verifica se uma parte já está presente
     * na programação.
     */
    private boolean possuiParte(
            ProgramacaoSemana programacao,
            Parte parte
    ) {
        return programacao.partes()
                .stream()
                .anyMatch(
                        programacaoParte ->
                                programacaoParte
                                        .getParte()
                                        .getId()
                                        .equals(
                                                parte.getId()
                                        )
                );
    }

    /**
     * Obtém a próxima ordem disponível.
     */
    private int obterProximaOrdem(
            ProgramacaoSemana programacao
    ) {
        return programacao.partes()
                .stream()
                .mapToInt(
                        ProgramacaoParte::getOrdem
                )
                .max()
                .orElse(0) + 1;
    }

    /* Ordem padrão das partes da reunião.
     */
    /**
     * Ordem padrão da parte da reunião.
     * A ordem é definida no cadastro da própria parte.
     */
    private int obterOrdemParte(Parte parte) {
        return parte.getOrdem();
    }

    /**
     * Valida a quantidade de partes variáveis.
     */
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
                        .anyMatch(
                                parte ->
                                        !ehParteVariavel(parte)
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
                    "Não é permitido selecionar a mesma "
                            + "parte variável mais de uma vez."
            );
        }
    }

    /**
     * Monta uma programação completa em memória.
     */
    public ProgramacaoSemana montarProgramacao(
            LocalDate data,
            List<Parte> partesVariaveisSelecionadas
    ) {
        validarData(data);

        validarPartesVariaveis(
                partesVariaveisSelecionadas
        );

        List<Parte> partesSelecionadas =
                new ArrayList<>(
                        listarPartesFixasOrdenadas()
                );

        partesSelecionadas.addAll(
                partesVariaveisSelecionadas
        );

        List<Parte> partesOrdenadas =
                ordenarPartes(
                        partesSelecionadas
                );

        ProgramacaoSemana programacao =
                novaProgramacao(data);

        int ordem = 1;

        for (Parte parte : partesOrdenadas) {
            programacao.adicionarParte(
                    criarProgramacaoParte(
                            parte,
                            ordem++
                    )
            );
        }

        return programacao;
    }

    private ProgramacaoSemana novaProgramacao(LocalDate data) {
        ConfiguracaoSalas configuracao =
                configuracaoSalasService.buscar();

        return new ProgramacaoSemana(data, configuracao);
    }

    /**
     * Lista candidatos elegíveis para aconselhar na Sala B.
     * Exige pessoa ativa, permissão para a sala e privilégio de servo ministerial ou ancião.
     */
    public List<Pessoa> listarConselheirosSalaB() {
        return pessoaDAO.listarTodos().stream()
                .filter(Pessoa::isAtivo)
                .filter(Pessoa::podeParticiparSalaB)
                .filter(pessoa -> pessoa.getPrivilegio().atende(Privilegio.SERVO_MINISTERIAL))
                .sorted(Comparator.comparing(Pessoa::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    /**
     * Salva os dados complementares da programação semanal usados no S-140.
     * Campos vazios são persistidos como nulos, sem alterar as partes da semana.
     */
    public void salvarDadosS140(
            LocalDate data,
            Integer conselheiroSalaBId,
            String canticoInicial,
            String canticoMeio,
            String canticoFinal
    ) {
        validarData(data);
        obterOuCriar(data);

        if (conselheiroSalaBId != null) {
            boolean pessoaExiste = pessoaDAO.buscarPorId(conselheiroSalaBId)
                    .filter(pessoa -> pessoa.isAtivo()
                            && pessoa.podeParticiparSalaB()
                            && pessoa.getPrivilegio().atende(Privilegio.SERVO_MINISTERIAL))
                    .isPresent();
            if (!pessoaExiste) {
                throw new IllegalArgumentException(
                        "O conselheiro da Sala B precisa ser ativo, habilitado para a Sala B "
                                + "e servo ministerial ou ancião."
                );
            }
        }

        programacaoSemanaDAO.atualizarDadosS140(
                data,
                conselheiroSalaBId,
                canticoInicial,
                canticoMeio,
                canticoFinal
        );
    }

    /**
     * Adiciona uma parte variável à semana.
     */
    public void adicionarParteVariavel(
            LocalDate data,
            Integer parteId
    ) {
        validarData(data);

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

        if (!ehParteVariavel(parte)) {
            throw new IllegalArgumentException(
                    "Somente partes variáveis podem ser adicionadas."
            );
        }

        if (possuiParte(
                programacao,
                parte
        )) {
            throw new IllegalArgumentException(
                    "Esta parte já está adicionada à semana."
            );
        }

        long quantidadeVariaveis =
                contarPartesVariaveis(programacao);

        if (quantidadeVariaveis >= 6) {
            throw new IllegalArgumentException(
                    "A semana já possui o máximo de 6 partes variáveis."
            );
        }

        int novaOrdem =
                obterProximaOrdem(programacao);

        ProgramacaoParte programacaoParte = criarProgramacaoParte(
                parte,
                novaOrdem
        );

        atribuirNumeroPadraoLeitura(programacaoParte);

        programacaoParteDAO.salvar(
                programacao.id(),
                programacaoParte
        );

        ProgramacaoSemana atualizada =
                programacaoSemanaDAO.buscarPorData(data);

        if (atualizada == null) {
            throw new IllegalStateException(
                    "A programação não foi encontrada após "
                            + "adicionar a parte variável."
            );
        }
    }

    /**
     * Conta quantas partes variáveis existem
     * na programação.
     */
    private long contarPartesVariaveis(
            ProgramacaoSemana programacao
    ) {
        return programacao.partes()
                .stream()
                .filter(
                        programacaoParte ->
                                ehParteVariavel(
                                        programacaoParte.getParte()
                                )
                )
                .count();
    }

    /**
     * Remove uma parte variável da semana.
     *
     * Partes fixas nunca podem ser removidas.
     */
    public void removerParteVariavel(
            LocalDate data,
            Integer parteId
    ) {
        validarData(data);

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

        if (!ehParteVariavel(
                programacaoParte.getParte()
        )) {
            throw new IllegalArgumentException(
                    "Partes fixas não podem ser removidas."
            );
        }

        programacaoParteDAO.excluir(
                programacao.id(),
                parteId
        );
    }

    /**
     * Verifica se a programação possui entre 3 e 6
     * partes variáveis.
     */
    public boolean estaConfigurada(
            LocalDate data
    ) {
        validarData(data);

        ProgramacaoSemana programacao =
                obterOuCriar(data);

        return programacao
                .possuiQuantidadeValidaDePartesVariaveis();
    }

    /**
     * Lista todas as reuniões do mês.
     *
     * Um mês pode possuir 4 ou 5 reuniões, conforme o dia habitual configurado.
     */
    public List<ProgramacaoSemana> listarSemanasDoMes(
            YearMonth mes
    ) {
        if (mes == null) {
            throw new IllegalArgumentException(
                    "O mês não pode ser nulo."
            );
        }

        DayOfWeek diaReuniao = configuracaoGeralService.buscar()
                .diaReuniaoHabitual();

        List<ProgramacaoSemana> semanas =
                new ArrayList<>();

        LocalDate data =
                mes.atDay(1);

        while (data.getMonth() == mes.getMonth()) {

            if (data.getDayOfWeek()
                    == diaReuniao) {

                ProgramacaoSemana programacao =
                        obterOuCriar(data);

                if (programacao != null) {
                    semanas.add(programacao);
                }
            }

            data = data.plusDays(1);
        }

        return semanas;
    }

    /**
     * Valida uma data obrigatória.
     */
    private void validarData(
            LocalDate data
    ) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "A data não pode ser nula."
            );
        }
    }
}