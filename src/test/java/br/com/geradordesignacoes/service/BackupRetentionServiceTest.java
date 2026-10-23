package br.com.geradordesignacoes.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BackupRetentionServiceTest {

    private static final DateTimeFormatter FORMATO_NOME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final Instant INICIO = Instant.parse("2026-01-01T00:00:00Z");

    private Path pasta;
    private BackupRetentionService retencao;

    @BeforeEach
    void preparar() throws IOException {
        pasta = Files.createTempDirectory("gerador-retencao-test-");
        retencao = new BackupRetentionService();
    }

    @AfterEach
    void limpar() throws IOException {
        if (pasta != null && Files.exists(pasta)) {
            try (var caminhos = Files.walk(pasta)) {
                for (Path caminho : caminhos.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(caminho);
                }
            }
        }
    }

    @Test
    void deveManterOsTrintaBackupsAutomaticosMaisRecentes() throws Exception {
        List<Path> backups = criarAutomaticos(35, i -> INICIO.plusSeconds(i));

        retencao.aplicarRetencao(pasta);

        for (int i = 0; i < 5; i++) {
            assertFalse(Files.exists(backups.get(i)),
                    "O backup automático excedente mais antigo deveria ser removido.");
            assertFalse(Files.exists(marcador(backups.get(i))));
        }
        for (int i = 5; i < backups.size(); i++) {
            assertTrue(Files.isRegularFile(backups.get(i)),
                    "Os 30 backups automáticos mais recentes devem ser preservados.");
            assertTrue(Files.isRegularFile(marcador(backups.get(i))));
        }
    }

    @Test
    void devePreservarBackupsManuaisEArquivosLegadosSemMetadados() throws Exception {
        List<Path> manuais = new ArrayList<>();
        for (int i = 0; i < 37; i++) {
            Path manual = pasta.resolve("backup-manual-" + i + ".db");
            Files.writeString(manual, "backup manual " + i);
            manuais.add(manual);
        }

        Path desconhecido = pasta.resolve("arquivo-desconhecido.bin");
        Path invalido = pasta.resolve("backup-manual-invalido.db");
        Files.writeString(desconhecido, "conteúdo desconhecido");
        Files.writeString(invalido, "não é um banco SQLite");

        List<Path> legados = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Path legado = caminhoAutomatico(i + 100);
            Files.writeString(legado, "backup legado " + i);
            legados.add(legado);
        }

        criarAutomaticos(32, i -> INICIO.plusSeconds(i));
        retencao.aplicarRetencao(pasta);

        manuais.forEach(path -> assertTrue(Files.isRegularFile(path)));
        legados.forEach(path -> assertTrue(Files.isRegularFile(path),
                "Nome compatível, sem metadados confiáveis, não autoriza exclusão."));
        assertTrue(Files.isRegularFile(desconhecido),
                "Arquivos desconhecidos não devem ser excluídos pela retenção.");
        assertTrue(Files.isRegularFile(invalido),
                "Arquivos inválidos sem classificação automática comprovada devem ser preservados.");
    }

    @Test
    void deveCancelarTodaExclusaoSeOsMetadadosNaoForemValidos() throws Exception {
        List<Path> backups = criarAutomaticos(34, i -> INICIO.plusSeconds(i));
        Files.writeString(marcador(backups.get(33)), "metadados inválidos");

        assertThrows(IOException.class, () -> retencao.aplicarRetencao(pasta));

        backups.forEach(path -> assertTrue(Files.isRegularFile(path),
                "Uma falha de classificação deve impedir a limpeza desta execução."));
    }

    @Test
    void deveCancelarTodaExclusaoSeNaoPuderLerUmMarcadorComSeguranca() throws Exception {
        List<Path> backups = criarAutomaticos(31, i -> INICIO.plusSeconds(i));
        Path marcador = marcador(backups.get(30));
        Files.delete(marcador);
        Files.createDirectory(marcador);

        assertThrows(IOException.class, () -> retencao.aplicarRetencao(pasta));

        backups.forEach(path -> assertTrue(Files.isRegularFile(path)));
    }

    @Test
    void devePreservarEmpatesNaDataDeCorteDaRetencao() throws Exception {
        List<Path> backups = criarAutomaticos(31, i -> {
            if (i <= 1) {
                return INICIO;
            }
            return INICIO.plusSeconds(i);
        });

        retencao.aplicarRetencao(pasta);

        backups.forEach(path -> assertTrue(Files.isRegularFile(path),
                "Se o limite de retenção cair em datas empatadas, os empatados são preservados."));
    }

    @Test
    void deveInterromperExclusoesQuandoUmaExclusaoFalhar() throws Exception {
        List<Path> backups = criarAutomaticos(33, i -> INICIO.plusSeconds(i));
        AtomicInteger chamadas = new AtomicInteger();
        BackupRetentionService retencaoComFalha = new BackupRetentionService(path -> {
            if (chamadas.incrementAndGet() == 3) {
                throw new IOException("Falha de exclusão simulada");
            }
            Files.delete(path);
        });

        assertThrows(IOException.class, () -> retencaoComFalha.aplicarRetencao(pasta));

        assertFalse(Files.exists(backups.get(0)),
                "A primeira exclusão, concluída antes da falha, era o backup mais antigo.");
        assertTrue(Files.exists(backups.get(1)),
                "Depois da falha, nenhum backup excedente posterior deve ser tentado.");
        assertEquals(32, contarBackupsAutomaticosExistentes(backups));
    }

    @Test
    void devePreservarArquivoQuandoSeuConteudoNaoCorresponderAoMarcador() throws Exception {
        List<Path> backups = criarAutomaticos(31, i -> INICIO.plusSeconds(i));
        Files.writeString(backups.get(30), "conteúdo substituído manualmente");

        assertThrows(IOException.class, () -> retencao.aplicarRetencao(pasta));

        backups.forEach(path -> assertTrue(Files.isRegularFile(path),
                "Um hash divergente torna a classificação incerta e cancela a retenção."));
    }

    @Test
    void deveRemoverClassificacaoAutomaticaAntesDeUmDestinoSerUsadoManualmente()
            throws Exception {
        List<Path> backups = criarAutomaticos(31, i -> INICIO.plusSeconds(i));
        Path destinoManual = backups.get(0);

        retencao.invalidarMarcadorParaDestinoManual(destinoManual);
        retencao.aplicarRetencao(pasta);

        assertTrue(Files.isRegularFile(destinoManual),
                "Um arquivo destinado a backup manual deve deixar de ser elegível à retenção.");
        assertFalse(Files.exists(marcador(destinoManual)));
        backups.forEach(path -> assertTrue(Files.isRegularFile(path)));
    }

    private List<Path> criarAutomaticos(int quantidade, CriadorData data) throws Exception {
        List<Path> backups = new ArrayList<>();
        for (int i = 0; i < quantidade; i++) {
            Path arquivo = caminhoAutomatico(i);
            Files.writeString(arquivo, "conteúdo do backup " + i);
            retencao.registrarBackupAutomatico(arquivo, data.criar(i));
            backups.add(arquivo);
        }
        return backups;
    }

    private Path caminhoAutomatico(int indice) {
        LocalDateTime dataNome = LocalDateTime.of(2026, 1, 1, 0, 0)
                .plusSeconds(indice);
        return pasta.resolve("gerador-designacoes_"
                + dataNome.format(FORMATO_NOME) + ".db");
    }

    private static Path marcador(Path arquivo) {
        return arquivo.resolveSibling(arquivo.getFileName() + ".auto.properties");
    }

    private static long contarBackupsAutomaticosExistentes(List<Path> backups) {
        return backups.stream().filter(Files::exists).count();
    }

    @FunctionalInterface
    private interface CriadorData {
        Instant criar(int indice);
    }
}
