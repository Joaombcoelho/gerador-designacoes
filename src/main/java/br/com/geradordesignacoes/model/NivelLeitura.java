package br.com.geradordesignacoes.model;

public enum NivelLeitura {

    BASICO,
    EXPERIENTE;


    public boolean atende(NivelLeitura exigido) {

        return this == exigido;
    }
}