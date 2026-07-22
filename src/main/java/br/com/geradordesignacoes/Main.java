package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.test.*;


public class Main {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        TesteCadastroBanco.executar();

        TesteAvaliadorPessoa.executar();
        TesteGeradorEscala.executar();
        TesteRankingAvaliacao.executar();
        TesteDiagnosticoSelecao.executar();

        TestePersistenciaHistorico.executar();
        TesteCarregarHistorico.executar();
        TesteHistoricoService.executar();
        TesteHistoricoInfluenciaSelecao.executar();
        TesteGeracaoEscalaCompleta.executar();

    }
}