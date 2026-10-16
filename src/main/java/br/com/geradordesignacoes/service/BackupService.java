package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.BackupDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final DateTimeFormatter FORMATO_NOME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private final Path pastaBackup;

    public BackupService() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente LOCALAPPDATA não está disponível."
            );
        }

        this.pastaBackup = Path.of(
                localAppData,
                "GeradorDesignacoes",
                "backups"
        );
    }

    public void criarBackup() {
        try {
            Files.createDirectories(pastaBackup);

            String dataHora = LocalDateTime.now().format(FORMATO_NOME);
            Path arquivoBackup = pastaBackup.resolve(
                    "gerador-designacoes_" + dataHora + ".db"
            );

            int sufixo = 1;
            while (Files.exists(arquivoBackup)) {
                arquivoBackup = pastaBackup.resolve(
                        "gerador-designacoes_" + dataHora + "-" + sufixo + ".db"
                );
                sufixo++;
            }

            BackupDatabase.criarBackup(arquivoBackup);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao criar backup do banco.",
                    e
            );
        }
    }
}
