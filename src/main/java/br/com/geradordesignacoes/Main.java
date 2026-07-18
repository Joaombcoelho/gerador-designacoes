package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.test.TesteAvaliadorPessoa;
import br.com.geradordesignacoes.test.TesteGeradorEscala;
import br.com.geradordesignacoes.test.TesteRankingAvaliacao;


public class Main {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        TesteAvaliadorPessoa.executar();
        TesteGeradorEscala.executar();
        TesteRankingAvaliacao.executar();
    }
}