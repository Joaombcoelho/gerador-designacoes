package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.ConnectionFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

final class TestDatabase {

    private TestDatabase() {
    }

    static TestDatabase create() throws IOException {
        Path directory = Files.createTempDirectory(
                "gerador-designacoes-test-"
        );
        Path databasePath = directory.resolve(
                "gerador-designacoes-test.db"
        );
        Path realDirectory;
        try {
            realDirectory = directory.toRealPath();
        } catch (IOException e) {
            delete(directory);
            throw e;
        }

        try {
            ConnectionFactory.TestContext context =
                    ConnectionFactory.openTestContext(databasePath);
            return new TestDatabase(directory, realDirectory, context);
        } catch (RuntimeException e) {
            delete(directory);
            throw e;
        }
    }

    private final Path directory;
    private final Path realDirectory;
    private final ConnectionFactory.TestContext context;

    private TestDatabase(
            Path directory,
            Path realDirectory,
            ConnectionFactory.TestContext context
    ) {
        this.directory = directory;
        this.realDirectory = realDirectory;
        this.context = context;
    }

    void close() throws Exception {
        context.close();
        deleteIfUnchanged();
    }

    private void deleteIfUnchanged() throws IOException {
        if (!Files.isDirectory(directory)
                || Files.isSymbolicLink(directory)
                || !directory.toRealPath().equals(realDirectory)) {
            throw new IOException(
                    "O diretório temporário foi alterado; arquivos preservados."
            );
        }

        delete(directory);
    }

    private static void delete(Path directory) throws IOException {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new TestDatabaseCleanupException(e);
                        }
                    });
        } catch (TestDatabaseCleanupException e) {
            throw e.ioException;
        }
    }

    private static final class TestDatabaseCleanupException
            extends RuntimeException {

        private final IOException ioException;

        private TestDatabaseCleanupException(IOException ioException) {
            this.ioException = ioException;
        }
    }
}
