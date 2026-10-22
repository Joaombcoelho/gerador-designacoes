package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.BackupDatabase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupService {

    private static final DateTimeFormatter FORMATO_NOME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private final Path pastaBackup;
    private final BackupRetentionService retencaoBackups;

    public BackupService() {
        this(pastaBackupPadrao());
    }

    BackupService(Path pastaBackup) {
        this(pastaBackup, new BackupRetentionService());
    }

    BackupService(Path pastaBackup, BackupRetentionService retencaoBackups) {
        if (pastaBackup == null || retencaoBackups == null) {
            throw new IllegalArgumentException(
                    "A pasta de backup e o serviço de retenção são obrigatórios."
            );
        }
        this.pastaBackup = pastaBackup.toAbsolutePath().normalize();
        this.retencaoBackups = retencaoBackups;
    }

    private static Path pastaBackupPadrao() {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null || localAppData.isBlank()) {
            throw new IllegalStateException(
                    "A variável de ambiente LOCALAPPDATA não está disponível."
            );
        }
        return Path.of(
                localAppData,
                "GeradorDesignacoes",
                "backups"
        );
    }

    public void criarBackup() {
        try {
            // O mesmo bloqueio protege o arquivo contra restauração, gravação
            // manual ou outra execução de retenção durante todo o ciclo.
            BackupDatabase.comBloqueioDeArquivos(() -> {
                Files.createDirectories(pastaBackup);

                String dataHora = LocalDateTime.now().format(FORMATO_NOME);
                Path candidato = pastaBackup.resolve(
                        "gerador-designacoes_" + dataHora + ".db"
                );

                int sufixo = 1;
                while (Files.exists(candidato)) {
                    candidato = pastaBackup.resolve(
                            "gerador-designacoes_" + dataHora + "-" + sufixo + ".db"
                    );
                    sufixo++;
                }

                BackupDatabase.criarBackup(candidato);

                // Retenção é manutenção secundária: falhas nela não invalidam
                // nem removem o backup recém-criado ou alteram o banco principal.
                try {
                    retencaoBackups.registrarBackupAutomatico(candidato, Instant.now());
                    retencaoBackups.aplicarRetencao(pastaBackup);
                } catch (Exception e) {
                    System.err.println(
                            "Backup criado, mas a manutenção da retenção falhou; "
                                    + "os arquivos existentes foram preservados quando possível: "
                                    + e.getMessage()
                    );
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao criar backup do banco.",
                    e
            );
        }
    }
}
