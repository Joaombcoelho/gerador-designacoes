package br.com.geradordesignacoes.model;

public record ConfiguracaoSalas(
        boolean salaBHabilitada,
        boolean salaCHabilitada
) {

    public static ConfiguracaoSalas desabilitadas() {
        return new ConfiguracaoSalas(false, false);
    }
}
