package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BackupDatabase {

    private BackupDatabase() {
    }

    /**
     * Serializa criação, retenção e restauração de arquivos de backup.
     * O bloqueio é reentrante, então a operação pode chamar criarBackup.
     */
    public static void comBloqueioDeArquivos(OperacaoArquivo operacao)
            throws IOException {
        if (operacao == null) {
            throw new IllegalArgumentException("A operação de arquivo é obrigatória.");
        }
        DatabaseFileSupport.FILE_OPERATION_LOCK.lock();
        try {
            operacao.executar();
        } finally {
            DatabaseFileSupport.FILE_OPERATION_LOCK.unlock();
        }
    }

    @FunctionalInterface
    public interface OperacaoArquivo {
        void executar() throws IOException;
    }

    public static void criarBackup(Path destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException(
                    "O destino do backup não pode ser nulo."
            );
        }

        DatabaseFileSupport.FILE_OPERATION_LOCK.lock();
        Path temporario = null;
        try {
            Path bancoOrigem = ConnectionFactory.getDatabasePath()
                    .toAbsolutePath()
                    .normalize();
            Path arquivoDestino = destino.toAbsolutePath().normalize();

            if (!Files.isRegularFile(bancoOrigem)) {
                throw new IOException(
                        "Banco de dados não encontrado: " + bancoOrigem
                );
            }
            if (DatabaseFileSupport.samePath(bancoOrigem, arquivoDestino)) {
                throw new IOException(
                        "O destino do backup não pode ser o próprio banco ativo."
                );
            }

            Path pastaDestino = arquivoDestino.getParent();
            if (pastaDestino == null) {
                throw new IOException(
                        "Não foi possível determinar a pasta do backup."
                );
            }
            Files.createDirectories(pastaDestino);

            temporario = DatabaseFileSupport.createTemporaryPath(
                    pastaDestino,
                    ".gerador-backup-"
            );

            DatabaseFileSupport.createSnapshot(bancoOrigem, temporario);
            DatabaseFileSupport.validateDatabase(temporario);

            // Só substitui o arquivo final depois que a cópia temporária
            // foi criada e passou pelas verificações do SQLite.
            DatabaseFileSupport.moveReplacing(temporario, arquivoDestino);
            temporario = null;
        } finally {
            try {
                DatabaseFileSupport.deleteTemporaryFile(temporario);
            } finally {
                DatabaseFileSupport.FILE_OPERATION_LOCK.unlock();
            }
        }
    }
}
