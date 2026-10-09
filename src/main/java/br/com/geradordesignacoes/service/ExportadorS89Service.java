package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoParte;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Month;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ExportadorS89Service {

    private static final String MODELO_PADRAO =
            "modelos/S-89_T.pdf";

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EscalaDAO escalaDAO;
    private final ProgramacaoSemanaDAO programacaoSemanaDAO;
    private final Path caminhoModelo;

    public ExportadorS89Service() {
        this(
                new EscalaDAO(),
                new ProgramacaoSemanaDAO(),
                resolverCaminhoModelo()
        );
    }

    public ExportadorS89Service(
            EscalaDAO escalaDAO,
            ProgramacaoSemanaDAO programacaoSemanaDAO,
            Path caminhoModelo
    ) {
        this.escalaDAO = Objects.requireNonNull(escalaDAO);
        this.programacaoSemanaDAO =
                Objects.requireNonNull(programacaoSemanaDAO);
        this.caminhoModelo = Objects.requireNonNull(caminhoModelo);
    }

    public List<Path> exportarSemana(LocalDate data) {
        Path raizSaida = Path.of(
                System.getProperty(
                        "s89.output.dir",
                        Path.of(
                                System.getProperty("user.home"),
                                        "GeradorDesignacoes"
                                ).toString()
                )
        );

        return exportarSemana(data, raizSaida);
    }

    public List<Path> exportarSemana(
            LocalDate data,
            Path raizSaida
    ) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Selecione uma semana para exportar."
            );
        }

        if (raizSaida == null) {
            throw new IllegalArgumentException(
                    "O diretório de saída não pode ser nulo."
            );
        }

        validarModelo();

        Escala escala =
                escalaDAO.buscarPorData(data)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Não existe uma escala salva para "
                                                + data.format(FORMATO_DATA)
                                                + "."
                                )
                        );

        ProgramacaoSemana programacao =
                programacaoSemanaDAO.buscarPorData(data);

        if (programacao == null) {
            throw new IllegalArgumentException(
                    "Não existe programação salva para "
                            + data.format(FORMATO_DATA)
                            + "."
            );
        }

        List<DesignacaoExportacao> designacoes =
                prepararDesignacoes(escala, programacao);

        if (designacoes.isEmpty()) {
            throw new IllegalArgumentException(
                    "A escala não possui designações elegíveis para o S-89."
            );
        }

        Path diretorio =
                raizSaida
                        .resolve("S-89")
                        .resolve(nomeMes(data.getMonth()) + " "
                                + data.getYear())
                        .resolve(
                                "Semana "
                                        + numeroSemana(data)
                                        + " - "
                                        + data.format(
                                        DateTimeFormatter.ofPattern(
                                                "dd-MM-yyyy"
                                        )
                                )
                        );

        try {
            Files.createDirectories(diretorio);

            List<Path> arquivos = new ArrayList<>();

            for (DesignacaoExportacao designacao : designacoes) {
                Path arquivo =
                        proximoNomeDisponivel(
                                diretorio.resolve(
                                        nomeArquivo(designacao.parte())
                                )
                        );

                preencherModelo(
                        designacao,
                        arquivo
                );

                arquivos.add(arquivo);
            }

            return arquivos;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Falha ao criar os arquivos S-89.",
                    e
            );
        }
    }

    private List<DesignacaoExportacao> prepararDesignacoes(
            Escala escala,
            ProgramacaoSemana programacao
    ) {
        List<DesignacaoExportacao> resultado = new ArrayList<>();

        for (Designacao designacao : escala.getDesignacoes()) {
            if (!elegivel(designacao.parte())) {
                continue;
            }

            ProgramacaoParte programacaoParte =
                    programacao.partes()
                            .stream()
                            .filter(
                                    parte ->
                                            parte.getParte().getId()
                                                    .equals(
                                                            designacao.parte()
                                                                    .getId()
                                                    )
                            )
                            .findFirst()
                            .orElseThrow(
                                    () -> new IllegalArgumentException(
                                            "A parte "
                                                    + designacao.parte()
                                                    .getNome()
                                                    + " não está cadastrada "
                                                    + "na programação da semana."
                                    )
                            );

            if (programacaoParte.getNumeroOficial() == null) {
                throw new IllegalArgumentException(
                        "A parte "
                                + designacao.parte().getNome()
                                + " não possui número oficial definido."
                );
            }

            if (
                    designacao.responsavel() == null
                            && designacao.ajudante() == null
            ) {
                throw new IllegalArgumentException(
                        "A parte "
                                + designacao.parte().getNome()
                                + " não possui participante definido."
                );
            }

            String nome =
                    designacao.responsavel() != null
                            ? designacao.responsavel().getNome()
                            : designacao.ajudante().getNome();

            String ajudante =
                    designacao.responsavel() == null
                            ? ""
                            : designacao.ajudante() == null
                            ? ""
                            : designacao.ajudante().getNome();

            resultado.add(
                    new DesignacaoExportacao(
                            programacaoParte.getParte(),
                            nome,
                            ajudante,
                            escala.getData(),
                            programacaoParte.getNumeroOficial()
                    )
            );
        }

        return resultado.stream()
                .sorted(
                        Comparator.comparingInt(
                                item -> item.numeroOficial()
                        )
                )
                .toList();
    }

    private boolean elegivel(Parte parte) {
        return parte.getTipo() == TipoParte.LEITURA
                || (
                parte.getTipo() == TipoParte.DEMONSTRACAO
                        && parte.getSecao()
                        == br.com.geradordesignacoes.model.SecaoParte.MINISTERIO
        );
    }

    private void preencherModelo(
            DesignacaoExportacao designacao,
            Path arquivo
    ) throws IOException {
        try (
                PDDocument documento =
                        Loader.loadPDF(caminhoModelo.toFile())
        ) {
            PDAcroForm formulario =
                    documento.getDocumentCatalog().getAcroForm();

            if (formulario == null) {
                throw new IllegalArgumentException(
                        "O modelo S-89 não possui formulário editável."
                );
            }

            preencherCampo(formulario, "900_1_Text_SanSerif",
                    designacao.nome());
            preencherCampo(formulario, "900_2_Text_SanSerif",
                    designacao.ajudante());
            preencherCampo(formulario, "900_3_Text_SanSerif",
                    formatarIntervaloSemana(designacao.data()));
            preencherCampo(formulario, "900_4_Text_SanSerif",
                    designacao.numeroOficial()
                            + " — "
                            + designacao.parte().getNome());

            marcarCheckbox(formulario, "900_5_CheckBox", true);
            marcarCheckbox(formulario, "900_6_CheckBox", false);
            marcarCheckbox(formulario, "900_7_CheckBox", false);

            documento.save(arquivo.toFile());
        }
    }

    private String formatarIntervaloSemana(LocalDate data) {
        LocalDate inicio =
                data.with(DayOfWeek.MONDAY);
        LocalDate fim =
                data.with(DayOfWeek.SUNDAY);

        String mesInicio = nomeMes(inicio.getMonth());
        String mesFim = nomeMes(fim.getMonth());

        if (inicio.getYear() != fim.getYear()) {
            return String.format(
                    "%02d de %s de %d - %02d de %s de %d",
                    inicio.getDayOfMonth(),
                    mesInicio,
                    inicio.getYear(),
                    fim.getDayOfMonth(),
                    mesFim,
                    fim.getYear()
            );
        }

        if (inicio.getMonth() != fim.getMonth()) {
            return String.format(
                    "%02d de %s - %02d de %s",
                    inicio.getDayOfMonth(),
                    mesInicio,
                    fim.getDayOfMonth(),
                    mesFim
            );
        }

        return String.format(
                "%02d - %02d de %s",
                inicio.getDayOfMonth(),
                fim.getDayOfMonth(),
                mesInicio
        );
    }

    private void marcarCheckbox(
            PDAcroForm formulario,
            String nome,
            boolean marcado
    ) throws IOException {
        PDField campo = formulario.getField(nome);

        if (!(campo instanceof PDCheckBox checkbox)) {
            throw new IllegalArgumentException(
                    "O campo " + nome + " não é um checkbox válido."
            );
        }

        if (marcado) {
            checkbox.setValue("Yes");
        } else {
            checkbox.unCheck();
        }
    }

    private void preencherCampo(
            PDAcroForm formulario,
            String nome,
            String valor
    ) throws IOException {
        PDField campo = formulario.getField(nome);

        if (campo == null) {
            throw new IllegalArgumentException(
                    "O campo " + nome + " não existe no modelo S-89."
            );
        }

        campo.setValue(valor);
    }

    private void validarModelo() {
        if (!Files.isRegularFile(caminhoModelo)) {
            throw new IllegalStateException(
                    "Modelo S-89 não encontrado: "
                            + caminhoModelo.toAbsolutePath()
            );
        }
    }

    private Path proximoNomeDisponivel(Path arquivo) {
        if (!Files.exists(arquivo)) {
            return arquivo;
        }

        String nome = arquivo.getFileName().toString();
        int extensao = nome.lastIndexOf('.');
        String base = extensao < 0 ? nome : nome.substring(0, extensao);
        String sufixo = extensao < 0 ? "" : nome.substring(extensao);
        int contador = 2;
        Path candidato;

        do {
            candidato = arquivo.resolveSibling(
                    base + "_" + contador + sufixo
            );
            contador++;
        } while (Files.exists(candidato));

        return candidato;
    }

    private String nomeArquivo(Parte parte) {
        String nome =
                parte.getTipo() == TipoParte.LEITURA
                        ? "S89_Leitura_Biblia"
                        : "S89_Faca_Seu_Melhor";

        return nome + ".pdf";
    }

    private int numeroSemana(LocalDate data) {
        int semana = 0;
        LocalDate dia = data.withDayOfMonth(1);

        while (!dia.isAfter(data)) {
            if (dia.getDayOfWeek() == data.getDayOfWeek()) {
                semana++;
            }
            dia = dia.plusDays(1);
        }

        return semana;
    }

    private String nomeMes(Month mes) {
        String nome =
                mes.getDisplayName(
                        TextStyle.FULL,
                        Locale.forLanguageTag("pt-BR")
                );

        return Character.toUpperCase(nome.charAt(0))
                + nome.substring(1);
    }

    private static Path resolverCaminhoModelo() {
        String configurado =
                System.getProperty("s89.template");

        if (configurado != null && !configurado.isBlank()) {
            return Path.of(configurado);
        }

        return Path.of(MODELO_PADRAO);
    }

    private record DesignacaoExportacao(
            Parte parte,
            String nome,
            String ajudante,
            LocalDate data,
            Integer numeroOficial
    ) {
    }
}
