package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class ExportadorEscalaPdf {

    private static final float MARGEM = 50;
    private static final float ALTURA_LINHA = 18;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void exportar(List<Escala> escalas, File arquivo)
            throws IOException {

        if (escalas == null || escalas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem escalas para exportar."
            );
        }

        if (arquivo == null) {
            throw new IllegalArgumentException(
                    "Selecione um arquivo para salvar o PDF."
            );
        }

        List<Escala> escalasOrdenadas = escalas.stream()
                .filter(escala -> escala.getData() != null)
                .sorted(Comparator.comparing(Escala::getData))
                .toList();

        if (escalasOrdenadas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem escalas com datas válidas."
            );
        }

        try (PDDocument documento = new PDDocument()) {

            PDPage pagina = novaPagina(documento);
            PDPageContentStream conteudo =
                    new PDPageContentStream(documento, pagina);

            float y = pagina.getMediaBox().getHeight() - MARGEM;

            y = escrever(
                    conteudo,
                    "ESCALAS DE DESIGNACOES",
                    MARGEM,
                    y,
                    16,
                    true
            );

            y -= ALTURA_LINHA;

            for (Escala escala : escalasOrdenadas) {

                if (y < MARGEM + 100) {
                    conteudo.close();

                    pagina = novaPagina(documento);
                    conteudo = new PDPageContentStream(documento, pagina);
                    y = pagina.getMediaBox().getHeight() - MARGEM;
                }

                y = escrever(
                        conteudo,
                        "Data: " + escala.getData().format(FORMATO_DATA),
                        MARGEM,
                        y,
                        12,
                        true
                );

                y -= 6;

                List<Designacao> designacoes =
                        escala.getDesignacoes() == null
                                ? List.of()
                                : escala.getDesignacoes();

                for (Designacao designacao : designacoes) {

                    if (y < MARGEM + 45) {
                        conteudo.close();

                        pagina = novaPagina(documento);
                        conteudo = new PDPageContentStream(
                                documento, pagina
                        );
                        y = pagina.getMediaBox().getHeight() - MARGEM;
                    }

                    String parte = designacao.parte() != null
                            ? designacao.parte().getNome()
                            : "Parte não informada";

                    String responsavel =
                            designacao.responsavel() != null
                                    ? designacao.responsavel().getNome()
                                    : "Não definido";

                    String ajudante =
                            designacao.ajudante() != null
                                    ? designacao.ajudante().getNome()
                                    : "Não definido";

                    y = escrever(
                            conteudo,
                            "Parte: " + parte,
                            MARGEM + 10,
                            y,
                            10,
                            false
                    );

                    y = escrever(
                            conteudo,
                            "Responsável: " + responsavel,
                            MARGEM + 20,
                            y,
                            10,
                            false
                    );

                    y = escrever(
                            conteudo,
                            "Ajudante: " + ajudante,
                            MARGEM + 20,
                            y,
                            10,
                            false
                    );

                    y -= 6;
                }

                y -= ALTURA_LINHA;
            }

            conteudo.close();
            documento.save(arquivo);
        }
    }

    private PDPage novaPagina(PDDocument documento) {
        PDPage pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);
        return pagina;
    }

    private float escrever(
            PDPageContentStream conteudo,
            String texto,
            float x,
            float y,
            int tamanho,
            boolean negrito
    ) throws IOException {

        PDType1Font fonte = new PDType1Font(
                negrito
                        ? Standard14Fonts.FontName.HELVETICA_BOLD
                        : Standard14Fonts.FontName.HELVETICA
        );

        conteudo.beginText();
        conteudo.setFont(fonte, tamanho);
        conteudo.newLineAtOffset(x, y);
        conteudo.showText(texto);
        conteudo.endText();

        return y - ALTURA_LINHA;
    }
}