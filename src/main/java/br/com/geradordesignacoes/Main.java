package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.database.ConnectionFactory;

public class Main {

    public static void main(String[] args) {

        try (ConnectionFactory.ProductionContext ignored =
                     ConnectionFactory.openProductionContext()) {
            DatabaseInitializer.initialize();
        }

    }
}