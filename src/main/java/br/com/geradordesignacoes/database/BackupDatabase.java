package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BackupDatabase {

    private static final Path BANCO_ORIGEM =
            ConnectionFactory.getDatabasePath();

    public static void criarBackup(Path destino)
            throws IOException {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "O destino do backup não pode ser nulo."
            );
        }

        if (!Files.exists(BANCO_ORIGEM)) {
            throw new IOException(
                    "Banco de dados não encontrado: "
                            + BANCO_ORIGEM.toAbsolutePath()
            );
        }

        Path pastaDestino = destino.getParent();

        if (pastaDestino != null) {
            Files.createDirectories(pastaDestino);
        }

        Files.copy(
                BANCO_ORIGEM,
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}