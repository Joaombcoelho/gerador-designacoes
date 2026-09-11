package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class RestaurarDatabase {

    private static final Path BANCO_DESTINO =
            ConnectionFactory.getDatabasePath();

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

        Path pastaDestino = BANCO_DESTINO.getParent();

        if (pastaDestino != null) {
            Files.createDirectories(pastaDestino);
        }

        Files.copy(
                origem,
                BANCO_DESTINO,
                StandardCopyOption.REPLACE_EXISTING
        );
    }
}