package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.ConnectionFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupAutomaticoService {

    private static final long INTERVALO_HORAS = 24;

    private final BackupService backupService;

    private final ScheduledExecutorService scheduler;
    private final Object lifecycleLock = new Object();
    private boolean iniciado;
    private boolean encerrado;

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

        synchronized (lifecycleLock) {
            if (encerrado) {
                throw new IllegalStateException(
                        "O serviço de backup já foi encerrado."
                );
            }
            if (iniciado) {
                throw new IllegalStateException(
                        "O serviço de backup já foi iniciado."
                );
            }
            iniciado = true;
        }

        criarBackup();

        synchronized (lifecycleLock) {
            if (!encerrado) {
                scheduler.scheduleAtFixedRate(
                        this::criarBackupAgendado,
                        INTERVALO_HORAS,
                        INTERVALO_HORAS,
                        TimeUnit.HOURS
                );
            }
        }
    }

    private void criarBackupAgendado() {
        ConnectionFactory.ProductionContext context;
        synchronized (lifecycleLock) {
            if (encerrado) {
                return;
            }
            context = ConnectionFactory.openProductionContext();
        }

        try (context) {
            criarBackup();
        }
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

        synchronized (lifecycleLock) {
            encerrado = true;
            scheduler.shutdownNow();
        }
    }
}