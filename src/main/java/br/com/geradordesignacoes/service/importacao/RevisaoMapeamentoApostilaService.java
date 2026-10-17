package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.Parte;

import java.util.List;
import java.util.Objects;

/**
 * Aplica uma escolha manual do catálogo a uma sugestão de mapeamento.
 * Não persiste a escolha nem altera a parte cadastrada.
 */
public class RevisaoMapeamentoApostilaService {

    /**
     * Confirma a escolha feita pelo usuário para uma parte da apostila.
     *
     * A parte escolhida precisa pertencer à mesma seção. Quando o extrator
     * identificou o tipo da parte, a escolha também precisa respeitar esse
     * tipo. Quando o tipo não foi identificado, a seleção manual pode resolver
     * a incerteza, desde que a seção seja compatível.
     */
    public MapeadorParteApostilaService.ResultadoMapeamento aplicarSelecaoManual(
            MapeadorParteApostilaService.ResultadoMapeamento mapeamento,
            Parte selecionada
    ) {
        Objects.requireNonNull(mapeamento, "O mapeamento é obrigatório.");
        Objects.requireNonNull(selecionada, "Selecione uma parte do catálogo.");

        if (selecionada.getId() == null) {
            throw new IllegalArgumentException(
                    "A parte selecionada precisa existir no catálogo."
            );
        }

        if (selecionada.getSecao() != mapeamento.secao()) {
            throw new IllegalArgumentException(
                    "A parte selecionada precisa pertencer à mesma seção da apostila."
            );
        }

        if (mapeamento.tipoInferido() != null
                && selecionada.getTipo() != mapeamento.tipoInferido()) {
            throw new IllegalArgumentException(
                    "A parte selecionada não corresponde ao tipo identificado na apostila."
            );
        }

        return new MapeadorParteApostilaService.ResultadoMapeamento(
                mapeamento.numeroParteApostila(),
                mapeamento.tema(),
                mapeamento.duracaoMinutos(),
                mapeamento.secao(),
                selecionada.getTipo(),
                MapeadorParteApostilaService.SituacaoMapeamento.CORRESPONDENCIA_UNICA,
                List.of(selecionada)
        );
    }
}
