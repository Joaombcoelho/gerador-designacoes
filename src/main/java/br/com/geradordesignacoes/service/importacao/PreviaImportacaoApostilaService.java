package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.Parte;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Monta uma prévia de importação sem persistir alterações.
 *
 * As datas da apostila representam um intervalo de publicação/semana e não
 * são tratadas implicitamente como a data efetiva da reunião. O chamador
 * deve fornecer explicitamente a data da reunião para cada semana importada.
 */
public class PreviaImportacaoApostilaService {

    private final ImportacaoApostilaJsonService validador;
    private final MapeadorParteApostilaService mapeador;
    private final RevisaoMapeamentoApostilaService revisaoService;

    public PreviaImportacaoApostilaService() {
        this(new ImportacaoApostilaJsonService(), new MapeadorParteApostilaService(),
                new RevisaoMapeamentoApostilaService());
    }

    PreviaImportacaoApostilaService(
            ImportacaoApostilaJsonService validador,
            MapeadorParteApostilaService mapeador
    ) {
        this(validador, mapeador, new RevisaoMapeamentoApostilaService());
    }

    PreviaImportacaoApostilaService(
            ImportacaoApostilaJsonService validador,
            MapeadorParteApostilaService mapeador,
            RevisaoMapeamentoApostilaService revisaoService
    ) {
        this.validador = Objects.requireNonNull(validador);
        this.mapeador = Objects.requireNonNull(mapeador);
        this.revisaoService = Objects.requireNonNull(revisaoService);
    }

    public PreviaImportacao preparar(
            Path arquivoJson,
            Map<LocalDate, LocalDate> datasReuniaoPorInicioDaSemana,
            List<Parte> catalogo,
            Set<LocalDate> datasComProgramacaoExistente
    ) throws IOException {
        return preparar(arquivoJson, datasReuniaoPorInicioDaSemana, catalogo,
                datasComProgramacaoExistente, Map.of());
    }

    /**
     * Monta a prévia usando também as escolhas manuais feitas na interface.
     * A chave externa é a data inicial da semana da apostila e a interna é
     * o número da parte na apostila. Nenhuma escolha é persistida.
     */
    public PreviaImportacao preparar(
            Path arquivoJson,
            Map<LocalDate, LocalDate> datasReuniaoPorInicioDaSemana,
            List<Parte> catalogo,
            Set<LocalDate> datasComProgramacaoExistente,
            Map<LocalDate, Map<Integer, Parte>> escolhasManuais
    ) throws IOException {
        Objects.requireNonNull(datasReuniaoPorInicioDaSemana,
                "Informe explicitamente a data da reunião para cada semana.");
        Objects.requireNonNull(escolhasManuais, "As escolhas manuais não podem ser nulas.");
        Objects.requireNonNull(catalogo, "O catálogo de partes é obrigatório.");
        Objects.requireNonNull(datasComProgramacaoExistente,
                "Informe as datas que já possuem programação.");

        var validacao = validador.validar(arquivoJson);
        List<String> erros = new ArrayList<>(validacao.erros());
        List<String> avisos = new ArrayList<>(validacao.avisos());
        List<SemanaPrevia> semanas = new ArrayList<>();
        Set<LocalDate> datasEscolhidas = new HashSet<>();

        for (var semana : validacao.semanas()) {
            LocalDate dataReuniao = datasReuniaoPorInicioDaSemana.get(semana.dataInicio());
            if (dataReuniao == null) {
                erros.add("Semana iniciada em " + semana.dataInicio()
                        + ": selecione a data efetiva da reunião.");
                continue;
            }

            if (!datasEscolhidas.add(dataReuniao)) {
                erros.add("A data de reunião " + dataReuniao
                        + " foi selecionada para mais de uma semana da apostila.");
            }

            boolean conflitoExistente = datasComProgramacaoExistente.contains(dataReuniao);
            if (conflitoExistente) {
                avisos.add("Já existe uma programação para " + dataReuniao
                        + "; essa semana não pode ser gravada automaticamente.");
            }

            List<MapeadorParteApostilaService.ResultadoMapeamento> partes = new ArrayList<>();

            Map<Integer, Parte> escolhasDaSemana =
                    escolhasManuais.getOrDefault(semana.dataInicio(), Map.of());

// Impede que a mesma parte do catálogo seja escolhida
// para dois números diferentes da mesma semana.
            Set<Integer> idsPartesManuais = new HashSet<>();

            for (Parte selecionada : escolhasDaSemana.values()) {
                if (selecionada == null || selecionada.getId() == null) {
                    continue;
                }

                if (!idsPartesManuais.add(selecionada.getId())) {
                    erros.add(
                            "Semana " + semana.dataInicio()
                                    + ": a mesma parte do catálogo foi selecionada "
                                    + "mais de uma vez."
                    );
                }
            }

            for (var parte : semana.partes()) {
                var resultado = mapeador.mapear(parte, catalogo);
                Parte selecionada = escolhasDaSemana.get(parte.numero());
                if (selecionada != null) {
                    try {
                        resultado = revisaoService.aplicarSelecaoManual(resultado, selecionada);
                    } catch (IllegalArgumentException e) {
                        erros.add("Semana " + semana.dataInicio() + ", parte " + parte.numero()
                                + ": escolha manual inválida — " + e.getMessage());
                    }
                }
                partes.add(resultado);
                if (!resultado.possuiCorrespondenciaUnica()) {
                    avisos.add("Semana " + semana.dataInicio() + ", parte "
                            + parte.numero() + ": mapeamento " + rotulo(resultado.situacao())
                            + "; exige conferência.");
                }
            }

            semanas.add(new SemanaPrevia(
                    semana.dataInicio(),
                    semana.dataFim(),
                    dataReuniao,
                    semana.leituraBiblica(),
                    semana.canticos(),
                    partes,
                    conflitoExistente
            ));
        }

        return new PreviaImportacao(semanas, erros, avisos);
    }

    private static String rotulo(MapeadorParteApostilaService.SituacaoMapeamento situacao) {
        return switch (situacao) {
            case CORRESPONDENCIA_UNICA -> "correspondência única";
            case AMBIGUO -> "ambíguo";
            case NAO_ENCONTRADO -> "não encontrado";
            case REQUER_CONFERENCIA -> "requer conferência";
        };
    }

    public record PreviaImportacao(
            List<SemanaPrevia> semanas,
            List<String> erros,
            List<String> avisos
    ) {
        public PreviaImportacao {
            semanas = List.copyOf(semanas);
            erros = List.copyOf(erros);
            avisos = List.copyOf(avisos);
        }

        public boolean podeProsseguirParaConfirmacao() {
            return !semanas.isEmpty()
                    && erros.isEmpty()
                    && semanas.stream().allMatch(SemanaPrevia::podeProsseguirParaConfirmacao);
        }
    }

    public record SemanaPrevia(
            LocalDate dataInicioApostila,
            LocalDate dataFimApostila,
            LocalDate dataReuniao,
            String leituraBiblica,
            ImportacaoApostilaJsonService.CanticosApostila canticos,
            List<MapeadorParteApostilaService.ResultadoMapeamento> partes,
            boolean conflitoProgramacaoExistente
    ) {
        public SemanaPrevia {
            partes = List.copyOf(partes);
        }

        public boolean podeProsseguirParaConfirmacao() {
            long quantidadeVariaveis = partes.stream()
                    .filter(MapeadorParteApostilaService.ResultadoMapeamento::possuiCorrespondenciaUnica)
                    .map(parte -> parte.candidatos().getFirst())
                    .filter(parte -> parte.getTipoVariacao()
                            == br.com.geradordesignacoes.model.TipoVariacaoParte.VARIAVEL)
                    .count();
            return !conflitoProgramacaoExistente
                    && partes.size() == 8
                    && quantidadeVariaveis >= 3
                    && quantidadeVariaveis <= 6
                    && partes.stream().allMatch(
                            MapeadorParteApostilaService.ResultadoMapeamento::possuiCorrespondenciaUnica
                    );
        }
    }
}
