package br.com.geradordesignacoes.service;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final Path BANCO =
            Path.of("data", "gerador-designacoes.db");

    private static final Path PASTA_BACKUP =
            Path.of("backup");

    private static final DateTimeFormatter FORMATADOR =
            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd_HH-mm-ss"
            );


    public void criarBackup() {

        try {

            Files.createDirectories(
                    PASTA_BACKUP
            );

            String nomeArquivo =
                    "gerador-designacoes-"
                            + LocalDateTime.now()
                            .format(FORMATADOR)
                            + ".db";

            Path destino =
                    PASTA_BACKUP.resolve(nomeArquivo);

            Files.copy(
                    BANCO,
                    destino,
                    StandardCopyOption.COPY_ATTRIBUTES
            );

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao criar backup do banco.",
                    e
            );
        }
    }
}