package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.*;

public class BackupDatabase {

    private static final Path BANCO_ORIGEM =
            Path.of("data", "gerador-designacoes.db");


    public static void criarBackup(Path destino)
            throws IOException {

        if (!Files.exists(BANCO_ORIGEM)) {
            throw new IOException(
                    "Banco de dados não encontrado."
            );
        }


        Files.copy(
                BANCO_ORIGEM,
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}