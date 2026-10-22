package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.database.BackupDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Retenção conservadora dos backups automáticos.
 *
 * Apenas arquivos criados pelo fluxo automático e acompanhados de metadados
 * íntegros são candidatos à exclusão. Arquivos legados sem marcador, backups
 * manuais e arquivos cuja classificação seja duvidosa são preservados.
 */
public class BackupRetentionService {

    static final int LIMITE_BACKUPS_AUTOMATICOS = 30;

    private static final String TIPO_AUTOMATICO = "automatic";
    private static final String VERSAO_METADADOS = "1";
    private static final String SUFIXO_METADADOS = ".auto.properties";
    private static final Pattern NOME_AUTOMATICO = Pattern.compile(
            "^gerador-designacoes_\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}-\\d{3}(?:-\\d+)?\\.db$"
    );

    private final FileDeleter deleter;

    public BackupRetentionService() {
        this(Files::delete);
    }

    BackupRetentionService(FileDeleter deleter) {
        if (deleter == null) {
            throw new IllegalArgumentException("O mecanismo de exclusão é obrigatório.");
        }
        this.deleter = deleter;
    }

    /**
     * Registra a origem automática depois que o backup já foi criado e validado.
     * O marcador é publicado atomicamente; se falhar, o backup permanece sem
     * classificação e não pode ser excluído pela retenção.
     */
    void registrarBackupAutomatico(Path backup, Instant criadoEm) throws IOException {
        if (backup == null || criadoEm == null) {
            throw new IllegalArgumentException("Backup e data de criação são obrigatórios.");
        }

        Path arquivo = backup.toAbsolutePath().normalize();
        String nome = arquivo.getFileName().toString();
        if (!NOME_AUTOMATICO.matcher(nome).matches()) {
            throw new IOException("O nome do arquivo não corresponde ao padrão automático esperado.");
        }

        BasicFileAttributes atributos = Files.readAttributes(
                arquivo, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        if (!atributos.isRegularFile() || atributos.isSymbolicLink()) {
            throw new IOException("O backup automático precisa ser um arquivo regular.");
        }

        Path marcador = caminhoMarcador(arquivo);
        try {
            Files.readAttributes(marcador, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            throw new IOException("Já existem metadados para este backup; o marcador não será sobrescrito.");
        } catch (NoSuchFileException esperado) {
            // Não há marcador anterior.
        }

        Properties propriedades = new Properties();
        propriedades.setProperty("version", VERSAO_METADADOS);
        propriedades.setProperty("kind", TIPO_AUTOMATICO);
        propriedades.setProperty("backupFile", nome);
        propriedades.setProperty("createdAt", criadoEm.toString());
        propriedades.setProperty("size", Long.toString(Files.size(arquivo)));
        propriedades.setProperty("sha256", calcularSha256(arquivo));

        Path temporario = Files.createTempFile(
                arquivo.getParent(), ".gerador-retencao-", ".tmp");
        try {
            try (OutputStream output = Files.newOutputStream(temporario)) {
                propriedades.store(output, "Metadados de retenção do backup automático");
            }

            try {
                Files.move(temporario, marcador, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, marcador);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    /**
     * Remove o marcador automático antes que o usuário grave manualmente
     * sobre um destino que já foi usado por um backup automático.
     */
    public void invalidarMarcadorParaDestinoManual(Path destino) throws IOException {
        if (destino == null) {
            throw new IllegalArgumentException("O destino do backup é obrigatório.");
        }

        Path marcador = caminhoMarcador(destino.toAbsolutePath().normalize());
        BasicFileAttributes atributos;
        try {
            atributos = Files.readAttributes(
                    marcador, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException ausente) {
            return;
        }

        if (!atributos.isRegularFile() || atributos.isSymbolicLink()) {
            throw new IOException("Não é seguro substituir metadados de backup: " + marcador);
        }
        Files.delete(marcador);
    }

    /**
     * Mantém os 30 backups automáticos mais recentes. A classificação de todos
     * os candidatos termina antes da primeira exclusão; uma falha de leitura
     * ou classificação interrompe a operação sem apagar nenhum arquivo.
     */
    void aplicarRetencao(Path diretorio) throws IOException {
        BackupDatabase.comBloqueioDeArquivos(() -> aplicarRetencaoComBloqueio(diretorio));
    }

    private void aplicarRetencaoComBloqueio(Path diretorio) throws IOException {
        if (diretorio == null) {
            throw new IllegalArgumentException("A pasta de backups é obrigatória.");
        }

        Path pasta = diretorio.toAbsolutePath().normalize();
        BasicFileAttributes atributosPasta;
        try {
            atributosPasta = Files.readAttributes(
                    pasta, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (NoSuchFileException inexistente) {
            return;
        }
        if (!atributosPasta.isDirectory() || atributosPasta.isSymbolicLink()) {
            throw new IOException("A pasta de backups não é um diretório seguro.");
        }

        List<BackupAutomatico> backups = new ArrayList<>();
        try (DirectoryStream<Path> entradas = Files.newDirectoryStream(pasta)) {
            for (Path entrada : entradas) {
                String nome = entrada.getFileName().toString();
                if (!NOME_AUTOMATICO.matcher(nome).matches()) {
                    continue;
                }

                BasicFileAttributes atributosArquivo = Files.readAttributes(
                        entrada, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (!atributosArquivo.isRegularFile() || atributosArquivo.isSymbolicLink()) {
                    // Não se segue nem se exclui um caminho que não seja arquivo regular.
                    continue;
                }

                Path marcador = caminhoMarcador(entrada);
                BasicFileAttributes atributosMarcador;
                try {
                    atributosMarcador = Files.readAttributes(
                            marcador, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                } catch (NoSuchFileException legadoOuManual) {
                    // O nome sozinho não prova que seja um backup automático.
                    continue;
                }

                if (!atributosMarcador.isRegularFile() || atributosMarcador.isSymbolicLink()) {
                    throw new IOException(
                            "Não foi possível classificar com segurança o marcador: " + marcador);
                }

                BackupAutomatico backup = lerEValidarMetadados(entrada, marcador);
                if (backup == null) {
                    throw new IOException(
                            "Os metadados não correspondem ao backup; retenção cancelada: " + entrada);
                }
                backups.add(backup);
            }
        } catch (DirectoryIteratorException e) {
            throw e.getCause();
        }

        if (backups.size() <= LIMITE_BACKUPS_AUTOMATICOS) {
            return;
        }

        backups.sort(Comparator.comparing(BackupAutomatico::criadoEm).reversed()
                .thenComparing(b -> b.arquivo().getFileName().toString()));

        Instant limite = backups.get(LIMITE_BACKUPS_AUTOMATICOS - 1).criadoEm();
        List<BackupAutomatico> excedentes = backups.stream()
                .filter(backup -> backup.criadoEm().isBefore(limite))
                .sorted(Comparator.comparing(BackupAutomatico::criadoEm)
                        .thenComparing(b -> b.arquivo().getFileName().toString()))
                .toList();

        // Para timestamps empatados no limite, preserva todos: não há evidência
        // suficiente para escolher qual deles seria o 30.º.
        for (BackupAutomatico excedente : excedentes) {
            deleter.delete(excedente.arquivo());
            // Se apagar o marcador falhar, a operação para; não tenta outros backups.
            deleter.delete(excedente.marcador());
        }
    }

    private BackupAutomatico lerEValidarMetadados(Path arquivo, Path marcador)
            throws IOException {
        Properties propriedades = new Properties();
        try (InputStream input = Files.newInputStream(marcador)) {
            propriedades.load(input);
        } catch (IllegalArgumentException e) {
            throw new IOException("Metadados de backup malformados: " + marcador, e);
        }

        String nome = arquivo.getFileName().toString();
        if (!VERSAO_METADADOS.equals(propriedades.getProperty("version"))
                || !TIPO_AUTOMATICO.equals(propriedades.getProperty("kind"))
                || !nome.equals(propriedades.getProperty("backupFile"))) {
            return null;
        }

        final Instant criadoEm;
        final long tamanho;
        try {
            criadoEm = Instant.parse(propriedades.getProperty("createdAt"));
            tamanho = Long.parseLong(propriedades.getProperty("size"));
        } catch (RuntimeException e) {
            return null;
        }

        // Um marcador muito adiantado em relação ao relógio atual pode indicar
        // relógio alterado ou metadados indevidos; nesse caso não ordenamos nem apagamos.
        if (criadoEm.isAfter(Instant.now().plusSeconds(300))) {
            return null;
        }

        String hashRegistrado = propriedades.getProperty("sha256");
        if (tamanho < 0 || hashRegistrado == null
                || !hashRegistrado.matches("[0-9a-f]{64}")) {
            return null;
        }

        if (Files.size(arquivo) != tamanho
                || !hashRegistrado.equals(calcularSha256(arquivo))) {
            return null;
        }

        return new BackupAutomatico(arquivo, marcador, criadoEm);
    }

    private static Path caminhoMarcador(Path arquivo) {
        return arquivo.resolveSibling(arquivo.getFileName() + SUFIXO_METADADOS);
    }

    private static String calcularSha256(Path arquivo) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(arquivo)) {
                byte[] buffer = new byte[8192];
                int lidos;
                while ((lidos = input.read(buffer)) != -1) {
                    digest.update(buffer, 0, lidos);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", e);
        }
    }

    private record BackupAutomatico(Path arquivo, Path marcador, Instant criadoEm) {
    }

    @FunctionalInterface
    interface FileDeleter {
        void delete(Path path) throws IOException;
    }
}
