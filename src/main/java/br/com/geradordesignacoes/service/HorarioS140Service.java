package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.HorarioS140Item;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.TipoParte;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Orquestra o cálculo dos horários e a persistência dos ajustes manuais.
 * A lista de itens representa a ordem e as durações da pauta semanal.
 */
public class HorarioS140Service {

    private final ConfiguracaoGeralService configuracaoGeralService;
    private final HorarioManualS140DAO horarioManualDAO;
    private final CalculadoraHorarioS140 calculadora;

    public HorarioS140Service() {
        this(new ConfiguracaoGeralService(), new HorarioManualS140DAO(),
                new CalculadoraHorarioS140());
    }

    public HorarioS140Service(
            ConfiguracaoGeralService configuracaoGeralService,
            HorarioManualS140DAO horarioManualDAO,
            CalculadoraHorarioS140 calculadora
    ) {
        this.configuracaoGeralService = Objects.requireNonNull(configuracaoGeralService);
        this.horarioManualDAO = Objects.requireNonNull(horarioManualDAO);
        this.calculadora = Objects.requireNonNull(calculadora);
    }

    public List<HorarioS140Calculado> calcular(
            LocalDate data,
            List<HorarioS140Item> itens
    ) {
        if (data == null) {
            throw new IllegalArgumentException("A data da programação é obrigatória.");
        }
        Objects.requireNonNull(itens, "A lista de itens é obrigatória.");

        ConfiguracaoGeral configuracao = configuracaoGeralService.buscar();
        Map<String, LocalTime> horariosManuais = horarioManualDAO.listarPorData(data);

        List<HorarioS140Item> itensComAjustes = itens.stream()
                .map(item -> new HorarioS140Item(
                        item.chave(),
                        item.descricao(),
                        item.duracaoMinutos(),
                        horariosManuais.getOrDefault(item.chave(), item.horarioInicioManual())
                ))
                .toList();

        return calculadora.calcular(configuracao.horarioInicio(), itensComAjustes);
    }

    /**
     * Monta a pauta cronológica usando as partes selecionadas na semana.
     * Os 18 minutos do Ministério são uma referência, não um bloco artificial
     * que deva ser somado quando as partes reais ocuparem menos tempo.
     */
    public List<HorarioS140Calculado> calcular(ProgramacaoSemana programacao) {
        Objects.requireNonNull(programacao, "A programação semanal é obrigatória.");
        return calcular(programacao.data(), criarItens(programacao));
    }

    public List<HorarioS140Item> criarItens(ProgramacaoSemana programacao) {
        Objects.requireNonNull(programacao, "A programação semanal é obrigatória.");
        ConfiguracaoGeral configuracao = configuracaoGeralService.buscar();
        List<HorarioS140Item> itens = new ArrayList<>();

        itens.add(new HorarioS140Item("CANTICO_ORACAO_INICIAL",
                "Cântico e oração iniciais",
                configuracao.duracaoCanticoOracaoInicialMinutos(), null));
        itens.add(new HorarioS140Item("COMENTARIOS_INICIAIS",
                "Comentários iniciais",
                configuracao.duracaoComentariosIniciaisMinutos(), null));

        adicionarPartesDaSecao(
                itens, programacao, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS, false);

        itens.add(new HorarioS140Item("CANTICO_MEIO", "Cântico do meio",
                configuracao.duracaoCanticoMeioMinutos(), null));

        // Não se cria um bloco fixo de 18 minutos: cada parte do Ministério
        // contribui com sua própria duração cadastrada ou ajustada na semana.
        adicionarPartesDaSecao(itens, programacao, SecaoParte.MINISTERIO, true);

        // As partes de Nossa Vida Cristã também são itens individuais.
        // O total de referência de 15 minutos não é aplicado a cada parte.
        adicionarPartesDaSecao(itens, programacao, SecaoParte.NOSSA_VIDA_CRISTA, true);

        ProgramacaoParte estudo = programacao.partes().stream()
                .filter(item -> item.getParte().getTipo() == TipoParte.DIRIGENTE_ESTUDO)
                .min(Comparator.comparingInt(ProgramacaoParte::getOrdem))
                .orElse(null);

        Integer duracaoEstudo = estudo == null ? null : obterDuracao(estudo);
        if (duracaoEstudo == null) {
            duracaoEstudo = 30;
        }
        itens.add(new HorarioS140Item("ESTUDO_BIBLICO", "Estudo Bíblico",
                duracaoEstudo, null));

        itens.add(new HorarioS140Item("COMENTARIOS_FINAIS",
                "Comentários finais",
                configuracao.duracaoComentariosFinaisMinutos(), null));
        itens.add(new HorarioS140Item("CANTICO_ORACAO_FINAL",
                "Cântico e oração finais",
                configuracao.duracaoCanticoOracaoFinalMinutos(), null));

        return List.copyOf(itens);
    }

    private void adicionarPartesDaSecao(
            List<HorarioS140Item> itens,
            ProgramacaoSemana programacao,
            SecaoParte secao,
            boolean excluirEstudoBiblico
    ) {
        programacao.partes().stream()
                .filter(item -> item.getParte().getSecao() == secao)
                .filter(item -> !excluirEstudoBiblico
                        || item.getParte().getTipo() != TipoParte.DIRIGENTE_ESTUDO)
                .sorted(Comparator.comparingInt(ProgramacaoParte::getOrdem))
                .forEach(item -> {
                    Integer duracao = obterDuracao(item);
                    itens.add(new HorarioS140Item(
                            "PARTE_" + item.getParte().getId(),
                            item.getParte().getNome(),
                            duracao,
                            null));
                });
    }

    private Integer obterDuracao(ProgramacaoParte item) {
        Integer duracao = item.getDuracaoMinutos();
        if (duracao == null) {
            duracao = item.getParte().getDuracaoPadraoMinutos();
        }
        if (duracao == null) {
            duracao = switch (item.getParte().getTipo()) {
                case DISCURSO_TESOUROS -> 10;
                case JOIAS_ESPIRITUAIS -> 10;
                case LEITURA -> 4;
                default -> null;
            };
        }
        return duracao;
    }

    public void ajustarHorario(LocalDate data, String chaveItem, LocalTime horarioInicio) {
        horarioManualDAO.salvar(data, chaveItem, horarioInicio);
    }

    public void removerAjusteManual(LocalDate data, String chaveItem) {
        horarioManualDAO.remover(data, chaveItem);
    }

    public Map<String, LocalTime> listarAjustesManuais(LocalDate data) {
        return horarioManualDAO.listarPorData(data);
    }
}
