package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class BackupDatabase {

    public static void criarBackup(Path destino)
            throws IOException {

        if (destino == null) {
            throw new IllegalArgumentException(
                    "O destino do backup não pode ser nulo."
            );
        }

        Path bancoOrigem =
                ConnectionFactory.getDatabasePath();

        if (!Files.exists(bancoOrigem)) {
            throw new IOException(
                    "Banco de dados não encontrado: "
                            + bancoOrigem.toAbsolutePath()
            );
        }

        Path pastaDestino = destino.getParent();

        if (pastaDestino != null) {
            Files.createDirectories(pastaDestino);
        }

        Files.copy(
                bancoOrigem,
                destino,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}