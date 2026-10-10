package br.com.geradordesignacoes.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class ConnectionFactory {

    private static final String TEST_MODE_PROPERTY =
            "gerador.database.testMode";

    private static final ThreadLocal<TestContext> TEST_CONTEXT =
            new ThreadLocal<>();

    private static final ThreadLocal<ProductionContext>
            PRODUCTION_CONTEXT = new ThreadLocal<>();

    private static final Path DATABASE_DIRECTORY =
            Path.of(System.getenv("LOCALAPPDATA"), "GeradorDesignacoes");

    private static final Path DATABASE_PATH =
            DATABASE_DIRECTORY.resolve("gerador-designacoes.db");

    public static Connection getConnection() throws SQLException {

        TestContext context = TEST_CONTEXT.get();
        if (isTestMode() && context == null) {
            throw new IllegalStateException(
                    "Teste sem contexto de banco isolado."
            );
        }
        if (context == null && PRODUCTION_CONTEXT.get() == null) {
            throw new IllegalStateException(
                    "Acesso ao banco exige um contexto explícito."
            );
        }

        Path databasePath =
                context == null
                        ? getProductionDatabasePath()
                        : context.databasePath;
        Path databaseDirectory = databasePath.getParent();

        try {
            if (databaseDirectory != null) {
                Files.createDirectories(databaseDirectory);
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    "Não foi possível criar a pasta de dados.",
                    e
            );
        }

        String url = "jdbc:sqlite:" + databasePath.toAbsolutePath();

        Connection connection = DriverManager.getConnection(url);

        if (context != null) {
            context.register(connection);
        }

        try {
            try (var statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (Throwable failure) {
            try {
                connection.close();
                if (connection.isClosed() && context != null) {
                    context.unregister(connection);
                }
            } catch (Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw asSqlException(failure);
        }

        if (context == null) {
            return connection;
        }

        try {
            return context.proxy(connection);
        } catch (Throwable failure) {
            try {
                connection.close();
                if (connection.isClosed()) {
                    context.unregister(connection);
                }
            } catch (Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
            throw asSqlException(failure);
        }
    }

    public static Path getDatabasePath() {
        TestContext context = TEST_CONTEXT.get();
        if (isTestMode() && context == null) {
            throw new IllegalStateException(
                    "Teste sem contexto de banco isolado."
            );
        }
        if (context != null) {
            return context.databasePath;
        }
        return getProductionDatabasePath();
    }

    public static Path getProductionDatabasePath() {
        return DATABASE_PATH.toAbsolutePath().normalize();
    }

    public static ProductionContext openProductionContext() {
        if (isTestMode() || TEST_CONTEXT.get() != null) {
            throw new IllegalStateException(
                    "O acesso de produção não pode ser habilitado em testes."
            );
        }
        if (PRODUCTION_CONTEXT.get() != null) {
            throw new IllegalStateException(
                        "Já existe um contexto de produção nesta thread."
            );
        }
        ProductionContext context = new ProductionContext();
        PRODUCTION_CONTEXT.set(context);
        return context;
    }

    public static TestContext openTestContext(Path databasePath) {
        if (!isTestMode()) {
            throw new IllegalStateException(
                    "Contexto de banco de teste fora do modo de testes."
            );
        }

        Path normalizedPath = normalize(databasePath);
        Path productionPath = normalize(getProductionDatabasePath());

        if (samePath(normalizedPath, productionPath)
                || isInside(normalizedPath, productionPath.getParent())) {
            throw new IllegalArgumentException(
                    "O banco de teste não pode estar no diretório de produção."
            );
        }

        if (TEST_CONTEXT.get() != null) {
            throw new IllegalStateException(
                    "Já existe um contexto de banco neste thread."
            );
        }

        TestContext context = new TestContext(normalizedPath);
        TEST_CONTEXT.set(context);
        return context;
    }

    private static boolean isTestMode() {
        return Boolean.parseBoolean(
                System.getProperty(TEST_MODE_PROPERTY, "false")
        );
    }

    private static Path normalize(Path path) {
        if (path == null) {
            throw new IllegalArgumentException(
                    "O caminho do banco não pode ser nulo."
            );
        }

        Path absolute = path.toAbsolutePath().normalize();
        try {
            if (Files.exists(absolute)) {
                return absolute.toRealPath();
            }

            Path parent = absolute.getParent();
            if (parent == null) {
                return absolute;
            }

            return parent.toRealPath().resolve(absolute.getFileName())
                    .normalize();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Não foi possível resolver o caminho do banco.",
                    e
            );
        }
    }

    private static boolean samePath(Path first, Path second) {
        return first.toString().equalsIgnoreCase(second.toString());
    }

    private static boolean isInside(Path path, Path directory) {
        if (directory == null) {
            return false;
        }

        String pathText = path.toString();
        String directoryText = directory.toString();

        if (!directoryText.endsWith("\\")
                && !directoryText.endsWith("/")) {
            directoryText +=
                    path.getFileSystem().getSeparator();
        }

        return pathText.regionMatches(
                true,
                0,
                directoryText,
                0,
                directoryText.length()
        );
    }

    private static SQLException asSqlException(Throwable failure) {
        Throwable cause = failure;
        if (cause instanceof InvocationTargetException invocation
                && invocation.getCause() != null) {
            cause = invocation.getCause();
        }
        if (cause instanceof SQLException sqlException) {
            return sqlException;
        }
        if (cause instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        return new SQLException(
                "Falha ao configurar a conexão SQLite.",
                cause
        );
    }

    public static final class TestContext implements AutoCloseable {

        private final Path databasePath;
        private final Set<Connection> openConnections =
                Collections.newSetFromMap(new IdentityHashMap<>());
        private boolean closed;

        private TestContext(Path databasePath) {
            this.databasePath = databasePath;
        }

        private void register(Connection connection) throws SQLException {
            if (closed) {
                try {
                    connection.close();
                } catch (SQLException closeFailure) {
                    throw closeFailure;
                }
                throw new SQLException(
                        "O contexto de teste já foi encerrado."
                );
            }
            openConnections.add(connection);
        }

        private void unregister(Connection connection) {
            openConnections.remove(connection);
        }

        private Connection proxy(Connection connection) {
            InvocationHandler handler = (proxy, method, args) -> {
                String methodName = method.getName();

                if (method.getDeclaringClass() == Object.class) {
                    return switch (methodName) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "toString" ->
                                "TestConnection[" + databasePath + "]";
                        default -> invoke(connection, method, args);
                    };
                }

                if ("close".equals(methodName)) {
                    if (connection.isClosed()) {
                        openConnections.remove(connection);
                        return null;
                    }

                    Object result = invoke(connection, method, args);
                    if (connection.isClosed()) {
                        openConnections.remove(connection);
                        return result;
                    }
                    throw new SQLException(
                            "A conexão não confirmou o fechamento."
                    );
                }

                if ("isClosed".equals(methodName)) {
                    return connection.isClosed();
                }

                if (closed) {
                    throw new SQLException(
                            "A conexão pertence a um contexto encerrado."
                    );
                }

                if ("unwrap".equals(methodName)
                        && args != null
                        && args.length == 1
                        && args[0] instanceof Class<?> type) {
                    if (type.isInstance(proxy)) {
                        return proxy;
                    }
                    if (type.isInstance(connection)) {
                        return type.cast(connection);
                    }
                }

                if ("isWrapperFor".equals(methodName)
                        && args != null
                        && args.length == 1
                        && args[0] instanceof Class<?> type) {
                    return type.isInstance(proxy)
                            || type.isInstance(connection)
                            || (Boolean) invoke(connection, method, args);
                }

                return invoke(connection, method, args);
            };

            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    handler
            );
        }

        private static Object invoke(
                Connection connection,
                java.lang.reflect.Method method,
                Object[] args
        ) throws Throwable {
            try {
                return method.invoke(connection, args);
            } catch (InvocationTargetException e) {
                throw e.getCause() == null ? e : e.getCause();
            }
        }

        @Override
        public void close() throws SQLException {
            if (closed) {
                return;
            }

            SQLException failure = null;
            try {
                if (!openConnections.isEmpty()) {
                    failure = new SQLException(
                            "O contexto de teste ainda possui conexões abertas."
                    );
                }
            } finally {
                closed = true;
                TEST_CONTEXT.remove();
            }

            if (failure != null) {
                throw failure;
            }
        }

    }

    public static final class ProductionContext
            implements AutoCloseable {

        private boolean closed;

        private ProductionContext() {
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            if (PRODUCTION_CONTEXT.get() == this) {
                PRODUCTION_CONTEXT.remove();
            }
        }
    }
}