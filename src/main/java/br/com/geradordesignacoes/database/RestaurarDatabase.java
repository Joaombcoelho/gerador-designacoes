package br.com.geradordesignacoes.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RestaurarDatabase {

    @FunctionalInterface
    interface FileMover {
        void move(Path source, Path destination) throws IOException;
    }

    private RestaurarDatabase() {
    }

    public static void restaurar(Path origem) throws IOException {
        restaurar(origem, DatabaseFileSupport::moveReplacing);
    }

    // Sobrecarga package-private para permitir testar falhas de substituição
    // sem tocar no banco real ou depender de permissões do sistema operacional.
    static void restaurar(Path origem, FileMover fileMover)
            throws IOException {
        if (origem == null) {
            throw new IllegalArgumentException(
                    "O arquivo de origem não pode ser nulo."
            );
        }
        if (fileMover == null) {
            throw new IllegalArgumentException(
                    "O mecanismo de substituição não pode ser nulo."
            );
        }

        DatabaseFileSupport.FILE_OPERATION_LOCK.lock();
        Path bancoDestino = null;
        Path temporario = null;
        Path recuperacao = null;
        boolean haviaBancoOriginal = false;
        boolean substituicaoIniciada = false;
        boolean preservarRecuperacao = false;

        try {
            Path arquivoOrigem = origem.toAbsolutePath().normalize();
            bancoDestino = ConnectionFactory.getDatabasePath()
                    .toAbsolutePath()
                    .normalize();

            if (!Files.isRegularFile(arquivoOrigem)) {
                throw new IOException(
                        "Arquivo de backup não encontrado ou inválido."
                );
            }
            if (DatabaseFileSupport.samePath(arquivoOrigem, bancoDestino)) {
                throw new IOException(
                        "Não é possível restaurar o banco ativo sobre ele mesmo."
                );
            }

            // Validar a origem antes de alterar qualquer arquivo ativo.
            DatabaseFileSupport.validateDatabase(arquivoOrigem);

            Path pastaDestino = bancoDestino.getParent();
            if (pastaDestino == null) {
                throw new IOException(
                        "Não foi possível determinar a pasta do banco ativo."
                );
            }
            Files.createDirectories(pastaDestino);

            temporario = DatabaseFileSupport.createTemporaryPath(
                    pastaDestino,
                    ".gerador-restauracao-"
            );
            Files.copy(arquivoOrigem, temporario);
            DatabaseFileSupport.validateDatabase(temporario);

            haviaBancoOriginal = Files.exists(bancoDestino);
            if (haviaBancoOriginal) {
                if (!Files.isRegularFile(bancoDestino)) {
                    throw new IOException(
                            "O caminho do banco ativo não é um arquivo regular."
                    );
                }

                recuperacao = DatabaseFileSupport.createTemporaryPath(
                        pastaDestino,
                        ".gerador-recuperacao-"
                );
                DatabaseFileSupport.createSnapshot(bancoDestino, recuperacao);
                DatabaseFileSupport.validateDatabase(recuperacao);
            }

            // A partir daqui, qualquer falha tenta restaurar a fotografia
            // validada do banco anterior antes de propagar o erro.
            substituicaoIniciada = true;
            DatabaseFileSupport.deleteSidecars(bancoDestino);
            fileMover.move(temporario, bancoDestino);
            temporario = null;
            DatabaseFileSupport.validateDatabase(bancoDestino);

        } catch (IOException | RuntimeException failure) {
            if (substituicaoIniciada && bancoDestino != null) {
                try {
                    DatabaseFileSupport.deleteSidecars(bancoDestino);
                    if (haviaBancoOriginal && recuperacao != null) {
                        fileMover.move(recuperacao, bancoDestino);
                        recuperacao = null;
                        DatabaseFileSupport.validateDatabase(bancoDestino);
                    } else {
                        Files.deleteIfExists(bancoDestino);
                    }
                } catch (Throwable rollbackFailure) {
                    preservarRecuperacao = recuperacao != null
                            && Files.exists(recuperacao);
                    IOException recoveryNotice = new IOException(
                            preservarRecuperacao
                                    ? "A restauração falhou e a cópia de recuperação foi preservada em: "
                                            + recuperacao
                                    : "A restauração falhou e não foi possível recuperar automaticamente o banco original.",
                            rollbackFailure
                    );
                    failure.addSuppressed(recoveryNotice);
                }
            }

            if (failure instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException(
                    "Não foi possível restaurar o backup com segurança.",
                    failure
            );
        } finally {
            try {
                DatabaseFileSupport.deleteTemporaryFile(temporario);
                if (!preservarRecuperacao) {
                    DatabaseFileSupport.deleteTemporaryFile(recuperacao);
                }
            } finally {
                DatabaseFileSupport.FILE_OPERATION_LOCK.unlock();
            }
        }
    }
}
