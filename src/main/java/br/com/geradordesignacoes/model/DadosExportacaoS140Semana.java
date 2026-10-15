package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Snapshot somente de leitura dos dados já persistidos que serão usados
 * para compor uma semana do S-140.
 */
public record DadosExportacaoS140Semana(
        LocalDate data,
        ProgramacaoSemana programacao,
        Escala escala
) {
    public DadosExportacaoS140Semana {
        Objects.requireNonNull(data, "A data é obrigatória.");
        Objects.requireNonNull(programacao, "A programação é obrigatória.");
        Objects.requireNonNull(escala, "A escala é obrigatória.");

        if (!data.equals(programacao.data()) || !data.equals(escala.getData())) {
            throw new IllegalArgumentException(
                    "A programação e a escala devem corresponder à mesma data.");
        }
    }
}
