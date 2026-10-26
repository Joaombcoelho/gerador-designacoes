package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.HabilidadeDesignacaoCodigo;
import br.com.geradordesignacoes.model.TipoParte;

import java.util.Optional;

/**
 * Mapeia tipos de parte para habilidades individuais.
 *
 * Este componente apenas descreve os requisitos. A aplicação efetiva do
 * requisito é integrada separadamente para que categorias, sexo, nível de
 * leitura, participações e conflitos existentes continuem sendo avaliados.
 */
public class RequisitoHabilidadeParteService {

    public Optional<HabilidadeDesignacaoCodigo> habilidadeExigida(TipoParte tipo) {
        if (tipo == null) {
            return Optional.empty();
        }

        return switch (tipo) {
            case PRESIDENTE_REUNIAO ->
                    Optional.of(HabilidadeDesignacaoCodigo.PRESIDENCIA);
            case JOIAS_ESPIRITUAIS ->
                    Optional.of(HabilidadeDesignacaoCodigo.JOIAS_ESPIRITUAIS);
            case DISCURSO_TESOUROS ->
                    Optional.of(HabilidadeDesignacaoCodigo.TESOUROS);
            case NECESSIDADES_LOCAIS ->
                    Optional.of(HabilidadeDesignacaoCodigo.NECESSIDADES_LOCAIS);
            case DISCURSO, PARTE_1, PARTE_2, PARTE_3 ->
                    Optional.of(HabilidadeDesignacaoCodigo.DISCURSO);
            default -> Optional.empty();
        };
    }
}
