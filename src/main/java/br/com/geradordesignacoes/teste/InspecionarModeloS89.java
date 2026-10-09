
package br.com.geradordesignacoes.teste;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.rendering.PDFRenderer;

public class InspecionarModeloS89 {

    public static void main(String[] args) {
        File arquivo = new File("modelos/S-89_T.pdf");

        if (!arquivo.exists()) {
            System.err.println("PDF não encontrado: " + arquivo.getAbsolutePath());
            return;
        }

        try (PDDocument documento = Loader.loadPDF(arquivo)) {
            System.out.println("=== MODELO S-89 ===");
            System.out.println("Páginas: " + documento.getNumberOfPages());
            PDFRenderer renderer = new PDFRenderer(documento);

            BufferedImage imagem = renderer.renderImageWithDPI(0, 150);

            File imagemSaida = new File("modelos/S-89_visualizacao.png");
            ImageIO.write(imagem, "PNG", imagemSaida);

            System.out.println("Imagem gerada: " + imagemSaida.getAbsolutePath());

            PDAcroForm formulario =
                    documento.getDocumentCatalog().getAcroForm();

            if (formulario == null) {
                System.out.println("O PDF não possui AcroForm.");
                return;
            }

            for (PDField campo : formulario.getFieldTree()) {
                System.out.println("\n--------------------------------");
                System.out.println("Nome: " + campo.getFullyQualifiedName());
                System.out.println("Tipo: " + campo.getFieldType());
                System.out.println("Valor: " + campo.getValueAsString());

                for (PDAnnotationWidget widget : campo.getWidgets()) {
                    if (widget.getRectangle() != null) {
                        var r = widget.getRectangle();
                        System.out.printf(
                                "Posição: x=%.2f, y=%.2f, largura=%.2f, altura=%.2f%n",
                                r.getLowerLeftX(),
                                r.getLowerLeftY(),
                                r.getWidth(),
                                r.getHeight()
                        );
                    }

                    if (widget.getPage() != null) {
                        System.out.println(
                                "Página: " +
                                        (documento.getPages().indexOf(widget.getPage()) + 1)
                        );
                    }
                }

                if ("Btn".equals(campo.getFieldType())) {
                    System.out.println(
                            "Opções disponíveis: " +
                                    campo.getCOSObject().getDictionaryObject(
                                            org.apache.pdfbox.cos.COSName.OPT
                                    )
                    );
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao inspecionar o PDF:");
            e.printStackTrace();
        }
    }
}