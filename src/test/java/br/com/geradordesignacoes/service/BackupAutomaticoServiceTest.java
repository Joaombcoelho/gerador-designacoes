package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BackupAutomaticoServiceTest {

    @Test
    void deveExecutarBackupInicialAgendarIntervaloEEncerrarUmaUnicaVez()
            throws Exception {
        AtomicInteger backupsExecutados = new AtomicInteger();
        BackupService backupFalso = new BackupService(
                Path.of(System.getProperty("java.io.tmpdir"))
        ) {
            @Override
            public void criarBackup() {
                backupsExecutados.incrementAndGet();
            }
        };

        AgendadorObservavel scheduler = new AgendadorObservavel();
        BackupAutomaticoService service = new BackupAutomaticoService(
                backupFalso,
                scheduler,
                24,
                TimeUnit.HOURS
        );

        service.iniciar();

        assertEquals(1, backupsExecutados.get(),
                "O backup inicial deve ser tentado ao iniciar o serviço.");
        assertEquals(24, scheduler.initialDelay);
        assertEquals(24, scheduler.period);
        assertEquals(TimeUnit.HOURS, scheduler.unit);
        assertNotNull(scheduler.future);

        assertThrows(
                IllegalStateException.class,
                service::iniciar,
                "O serviço não deve aceitar inicialização duplicada."
        );

        service.encerrar();
        assertTrue(scheduler.isShutdown());

        service.encerrar();
        assertThrows(
                IllegalStateException.class,
                service::iniciar,
                "Um serviço encerrado não deve ser reiniciado."
        );
    }

    private static final class AgendadorObservavel
            extends ScheduledThreadPoolExecutor {

        private long initialDelay;
        private long period;
        private TimeUnit unit;
        private ScheduledFuture<?> future;

        private AgendadorObservavel() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(
                Runnable command,
                long initialDelay,
                long period,
                TimeUnit unit
        ) {
            this.initialDelay = initialDelay;
            this.period = period;
            this.unit = unit;
            future = super.scheduleAtFixedRate(
                    command,
                    initialDelay,
                    period,
                    unit
            );
            return future;
        }
    }
}
