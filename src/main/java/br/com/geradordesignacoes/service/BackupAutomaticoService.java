package br.com.geradordesignacoes.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupAutomaticoService {

    private static final long INTERVALO_HORAS = 24;

    private final BackupService backupService;

    private final ScheduledExecutorService scheduler;

    public BackupAutomaticoService() {

        this.backupService =
                new BackupService();

        this.scheduler =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {
                            Thread thread =
                                    new Thread(
                                            runnable,
                                            "backup-automatico"
                                    );

                            thread.setDaemon(true);

                            return thread;
                        }
                );
    }

    public void iniciar() {

        criarBackup();

        scheduler.scheduleAtFixedRate(
                this::criarBackup,
                INTERVALO_HORAS,
                INTERVALO_HORAS,
                TimeUnit.HOURS
        );
    }

    private void criarBackup() {

        try {

            backupService.criarBackup();

            System.out.println(
                    "Backup automático realizado com sucesso."
            );

        } catch (Exception e) {

            System.err.println(
                    "Erro ao realizar backup automático: "
                            + e.getMessage()
            );
        }
    }

    public void encerrar() {

        scheduler.shutdownNow();
    }
}