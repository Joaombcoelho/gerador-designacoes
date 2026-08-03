package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.*;

public class RestaurarDatabase {

    private static final Path BANCO_DESTINO =
            Path.of("data", "gerador-designacoes.db");


    public static void restaurar(Path origem)
            throws IOException {


        if (!Files.exists(origem)) {

            throw new IOException(
                    "Arquivo de backup não encontrado."
            );
        }


        Files.copy(
                origem,
                BANCO_DESTINO,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}