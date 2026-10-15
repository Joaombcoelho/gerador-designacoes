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
     * Monta a pauta cronológica com as durações padrão conhecidas e
     * as durações específicas já salvas na programação semanal.
     */
    public List<HorarioS140Calculado> calcular(ProgramacaoSemana programacao) {
        Objects.requireNonNull(programacao, "A programação semanal é obrigatória.");
        return calcular(programacao.data(), criarItens(programacao));
    }

    public List<HorarioS140Item> criarItens(ProgramacaoSemana programacao) {
        Objects.requireNonNull(programacao, "A programação semanal é obrigatória.");
        ConfiguracaoGeral configuracao = configuracaoGeralService.buscar();
        java.util.ArrayList<HorarioS140Item> itens = new java.util.ArrayList<>();

        itens.add(new HorarioS140Item("CANTICO_ORACAO_INICIAL",
                "Cântico e oração iniciais",
                configuracao.duracaoCanticoOracaoInicialMinutos(), null));
        itens.add(new HorarioS140Item("COMENTARIOS_INICIAIS",
                "Comentários iniciais",
                configuracao.duracaoComentariosIniciaisMinutos(), null));

        programacao.partes().stream()
                .filter(item -> item.getParte().getSecao() == SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS)
                .sorted(java.util.Comparator.comparingInt(ProgramacaoParte::getOrdem))
                .forEach(item -> {
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
                    itens.add(new HorarioS140Item(
                            "PARTE_" + item.getParte().getId(),
                            item.getParte().getNome(),
                            duracao,
                            null));
                });

        itens.add(new HorarioS140Item("CANTICO_MEIO", "Cântico do meio",
                configuracao.duracaoCanticoMeioMinutos(), null));

        // O bloco do Ministério tem 18 minutos como referência mínima,
        // não sendo obrigatório preencher esse tempo com designações.
        itens.add(new HorarioS140Item("BLOCO_MINISTERIO", "Ministério",
                18, null));

        itens.add(new HorarioS140Item("BLOCO_NOSSA_VIDA_CRISTA",
                "Nossa Vida Cristã", 15, null));

        Integer duracaoEstudo = programacao.partes().stream()
                .filter(item -> item.getParte().getTipo() == TipoParte.DIRIGENTE_ESTUDO)
                .map(ProgramacaoParte::getDuracaoMinutos)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(30);
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
