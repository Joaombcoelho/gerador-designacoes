package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RestaurarDatabase {

    public static void restaurar(Path origem)
            throws IOException {

        if (origem == null) {
            throw new IllegalArgumentException(
                    "O arquivo de origem não pode ser nulo."
            );
        }

        if (!Files.exists(origem)) {
            throw new IOException(
                    "Arquivo de backup não encontrado."
            );
        }

        Path bancoDestino =
                ConnectionFactory.getDatabasePath();
        Path pastaDestino = bancoDestino.getParent();

        if (pastaDestino != null) {
            Files.createDirectories(pastaDestino);
        }

        Files.copy(
                origem,
                bancoDestino,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}