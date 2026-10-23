package br.com.geradordesignacoes.database;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConnectionFactoryDatabasePathTest {

    @Test
    void preservaDiretorioLocalAppDataQuandoDisponivel() {
        Path localAppData = Path.of(
                System.getProperty("java.io.tmpdir"),
                "dados-locais-windows"
        );

        Path actual = ConnectionFactory.resolveProductionDatabasePath(
                localAppData.toString(),
                null,
                "Windows 11",
                System.getProperty("user.home")
        );

        assertEquals(
                localAppData.resolve("GeradorDesignacoes")
                        .resolve("gerador-designacoes.db")
                        .toAbsolutePath()
                        .normalize(),
                actual
        );
    }

    @Test
    void usaXdgDataHomeQuandoLocalAppDataNaoExiste() {
        Path xdgDataHome = Path.of(
                System.getProperty("java.io.tmpdir"),
                "xdg-data"
        );

        Path actual = ConnectionFactory.resolveProductionDatabasePath(
                null,
                xdgDataHome.toString(),
                "Linux",
                System.getProperty("user.home")
        );

        assertEquals(
                xdgDataHome.resolve("GeradorDesignacoes")
                        .resolve("gerador-designacoes.db")
                        .toAbsolutePath()
                        .normalize(),
                actual
        );
    }

    @Test
    void usaLocalShareQuandoVariaveisDeDadosNaoExistem() {
        Path userHome = Path.of(System.getProperty("user.home"));

        Path actual = ConnectionFactory.resolveProductionDatabasePath(
                null,
                null,
                "Linux",
                userHome.toString()
        );

        assertEquals(
                userHome.resolve(".local")
                        .resolve("share")
                        .resolve("GeradorDesignacoes")
                        .resolve("gerador-designacoes.db")
                        .toAbsolutePath()
                        .normalize(),
                actual
        );
    }

    @Test
    void usaFallbackDoWindowsQuandoLocalAppDataNaoExiste() {
        Path userHome = Path.of(System.getProperty("java.io.tmpdir"), "perfil");

        Path actual = ConnectionFactory.resolveProductionDatabasePath(
                null,
                null,
                "Windows 11",
                userHome.toString()
        );

        assertEquals(
                userHome.resolve("AppData")
                        .resolve("Local")
                        .resolve("GeradorDesignacoes")
                        .resolve("gerador-designacoes.db")
                        .toAbsolutePath()
                        .normalize(),
                actual
        );
    }
}
