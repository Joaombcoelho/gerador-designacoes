package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.service.ExportadorS89Service;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportadorS89ServiceTest extends BaseDAOTest {

    private final ParteDAO parteDAO = new ParteDAO();
    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final EscalaDAO escalaDAO = new EscalaDAO();

    @Test
    void deveExportarDesignacaoSalvaComNumeroOficialSemAlterarModelo()
            throws Exception {

        LocalDate data = LocalDate.of(2027, 10, 6);
        Parte leitura = parteDAO.listarTodos()
                .stream()
                .filter(parte -> parte.getTipo() == TipoParte.LEITURA)
                .findFirst()
                .orElseThrow();
        Pessoa pessoa = pessoaDAO.listarTodos()
                .stream()
                .findFirst()
                .orElseThrow();

        new ProgramacaoSemanaService().obterOuCriar(data);

        Escala escala = new Escala(
                data,
                List.of(
                        new Designacao(
                                data,
                                leitura,
                                pessoa,
                                null
                        )
                )
        );
        escalaDAO.salvar(escala);

        Path modelo = Path.of("modelos", "S-89_T.pdf");
        byte[] modeloOriginal = Files.readAllBytes(modelo);
        Path saida = Files.createTempDirectory("s89-teste");

        List<Path> arquivos =
                new ExportadorS89Service(
                        escalaDAO,
                        new br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO(),
                        modelo
                ).exportarSemana(data, saida);

        assertEquals(1, arquivos.size());
        assertTrue(Files.exists(arquivos.get(0)));
        assertArrayEquals(modeloOriginal, Files.readAllBytes(modelo));
        assertTrue(
                arquivos.get(0).toString().contains(
                        "Outubro 2027"
                )
        );
        assertTrue(
                arquivos.get(0).toString().contains(
                        "Semana 1 - 06-10-2027"
                )
        );

        try (PDDocument documento =
                     Loader.loadPDF(arquivos.get(0).toFile())) {
            assertEquals(
                    pessoa.getNome(),
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_1_Text_SanSerif")
                            .getValueAsString()
            );
            assertEquals(
                    "04 - 10 de Outubro",
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_3_Text_SanSerif")
                            .getValueAsString()
            );
            assertEquals(
                    "3 — Leitura",
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_4_Text_SanSerif")
                            .getValueAsString()
            );
            assertEquals(
                    "Yes",
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_5_CheckBox")
                            .getValueAsString()
            );
            assertEquals(
                    "Off",
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_6_CheckBox")
                            .getValueAsString()
            );
            assertEquals(
                    "Off",
                    documento.getDocumentCatalog()
                            .getAcroForm()
                            .getField("900_7_CheckBox")
                            .getValueAsString()
            );
        }
    }

    @Test
    void deveFalharQuandoNaoHouverEscalaSalva() throws Exception {

        Path saida = Files.createTempDirectory("s89-sem-escala");

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new ExportadorS89Service(
                                        escalaDAO,
                                        new br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO(),
                                        Path.of("modelos", "S-89_T.pdf")
                                ).exportarSemana(
                                        LocalDate.of(2027, 11, 3),
                                        saida
                                )
                );

        assertTrue(
                excecao.getMessage().contains(
                        "Não existe uma escala salva"
                )
        );
    }

    @Test
    void deveFormatarIntervalosEntreMesesEErrosDeAno() throws Exception {

        assertEquals(
                "27 de Setembro - 03 de Outubro",
                exportarEObterData(
                        LocalDate.of(2027, 9, 29)
                )
        );

        assertEquals(
                "28 de Dezembro de 2026 - 03 de Janeiro de 2027",
                exportarEObterData(
                        LocalDate.of(2026, 12, 30)
                )
        );
    }

    private String exportarEObterData(
            LocalDate data
    ) throws Exception {
        Parte leitura = parteDAO.listarTodos()
                .stream()
                .filter(parte -> parte.getTipo() == TipoParte.LEITURA)
                .findFirst()
                .orElseThrow();
        Pessoa pessoa = pessoaDAO.listarTodos()
                .stream()
                .findFirst()
                .orElseThrow();

        new ProgramacaoSemanaService().obterOuCriar(data);

        escalaDAO.salvar(
                new Escala(
                        data,
                        List.of(
                                new Designacao(
                                        data,
                                        leitura,
                                        pessoa,
                                        null
                                )
                        )
                )
        );

        Path saida = Files.createTempDirectory("s89-intervalo");
        Path arquivo =
                new ExportadorS89Service(
                        escalaDAO,
                        new br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO(),
                        Path.of("modelos", "S-89_T.pdf")
                ).exportarSemana(data, saida).get(0);

        try (PDDocument documento =
                     Loader.loadPDF(arquivo.toFile())) {
            return documento.getDocumentCatalog()
                    .getAcroForm()
                    .getField("900_3_Text_SanSerif")
                    .getValueAsString();
        }
    }
}
