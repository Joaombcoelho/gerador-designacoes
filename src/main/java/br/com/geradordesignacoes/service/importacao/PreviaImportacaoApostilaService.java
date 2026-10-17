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

    public PreviaImportacaoApostilaService() {
        this(new ImportacaoApostilaJsonService(), new MapeadorParteApostilaService());
    }

    PreviaImportacaoApostilaService(
            ImportacaoApostilaJsonService validador,
            MapeadorParteApostilaService mapeador
    ) {
        this.validador = Objects.requireNonNull(validador);
        this.mapeador = Objects.requireNonNull(mapeador);
    }

    public PreviaImportacao preparar(
            Path arquivoJson,
            Map<LocalDate, LocalDate> datasReuniaoPorInicioDaSemana,
            List<Parte> catalogo,
            Set<LocalDate> datasComProgramacaoExistente
    ) throws IOException {
        Objects.requireNonNull(datasReuniaoPorInicioDaSemana,
                "Informe explicitamente a data da reunião para cada semana.");
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
            for (var parte : semana.partes()) {
                var resultado = mapeador.mapear(parte, catalogo);
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
            return !conflitoProgramacaoExistente
                    && partes.size() == 8
                    && partes.stream().allMatch(
                            MapeadorParteApostilaService.ResultadoMapeamento::possuiCorrespondenciaUnica
                    );
        }
    }
}
