package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.BackupDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private final Path pastaBackup;

    public BackupService() {

        String localAppData =
                System.getenv("LOCALAPPDATA");

        if (localAppData == null || localAppData.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente LOCALAPPDATA não está disponível."
            );
        }

        this.pastaBackup =
                Path.of(
                        localAppData,
                        "GeradorDesignacoes",
                        "backups"
                );
    }


    /**
     * Cria um backup do banco de dados.
     *
     * O arquivo recebe data e hora no nome para
     * evitar que backups anteriores sejam sobrescritos.
     */
    public void criarBackup() {

        try {

            Files.createDirectories(
                    pastaBackup
            );


            String dataHora =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd_HH-mm-ss"
                                    )
                            );


            Path arquivoBackup =
                    pastaBackup.resolve(
                            "gerador-designacoes_"
                                    + dataHora
                                    + ".db"
                    );


            BackupDatabase.criarBackup(
                    arquivoBackup
            );


        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao criar backup do banco.",
                    e
            );
        }
    }
}