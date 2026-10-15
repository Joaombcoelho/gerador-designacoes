package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.HorarioS140Calculado;
import br.com.geradordesignacoes.model.HorarioS140Item;

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
