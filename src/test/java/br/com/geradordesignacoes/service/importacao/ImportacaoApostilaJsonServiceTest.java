package br.com.geradordesignacoes.service.importacao;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImportacaoApostilaJsonServiceTest {

    @TempDir
    Path pastaTemporaria;

    @Test
    void validaJsonCompletoSemAcessarBanco() throws Exception {
        Path arquivo = escreverJson(jsonValido());
        var resultado = new ImportacaoApostilaJsonService().validar(arquivo);

        assertTrue(resultado.validoParaPrevia(), resultado.erros().toString());
        assertEquals(1, resultado.semanas().size());
        var semana = resultado.semanas().getFirst();
        assertEquals("2026-11-02", semana.dataInicio().toString());
        assertEquals("2026-11-08", semana.dataFim().toString());
        assertEquals(3, semana.canticos().quantidadePreenchida());
        assertEquals(8, semana.partes().size());

        var parte4 = semana.partes().get(3);
        assertEquals(4, parte4.numero());
        assertEquals("De casa em casa.", parte4.descricaoDesignacao());
        assertEquals("De casa em casa", parte4.subtitulo());
        assertEquals(br.com.geradordesignacoes.model.SecaoParte.MINISTERIO, parte4.secao());
    }

    @Test
    void bloqueiaPreviaQuandoDatasEstaoInvalidas() throws Exception {
        String json = jsonValido().replace("2026-11-02", "2026-02-30");
        var resultado = new ImportacaoApostilaJsonService().validar(escreverJson(json));

        assertFalse(resultado.validoParaPrevia());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("data inválida")));
    }

    @Test
    void bloqueiaPreviaQuandoParteEstaDuplicadaOuSecaoNaoReconhecida() throws Exception {
        String json = jsonValido()
                .replace("\\"secao\\": \\"ministerio\\"", "\\"secao\\": \\"desconhecida\\"")
                .replace("\\"numero\\": 8", "\\"numero\\": 7");
        var resultado = new ImportacaoApostilaJsonService().validar(escreverJson(json));

        assertFalse(resultado.validoParaPrevia());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("seção ausente ou não reconhecida")));
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("duplicado")));
    }

    @Test
    void rejeitaArquivoSemListaDeSemanas() throws Exception {
        var resultado = new ImportacaoApostilaJsonService()
                .validar(escreverJson("{\\"schema_version\\":\\"1.0\\",\\"semanas\\":[]}"));

        assertFalse(resultado.validoParaPrevia());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("lista 'semanas'")));
    }

    private Path escreverJson(String json) throws Exception {
        Path arquivo = pastaTemporaria.resolve("apostila.json");
        Files.writeString(arquivo, json);
        return arquivo;
    }

    private String jsonValido() {
        StringBuilder partes = new StringBuilder();
        for (int numero = 1; numero <= 8; numero++) {
            if (!partes.isEmpty()) partes.append(",");
            String secao = numero <= 3 ? "tesouros" : numero <= 6 ? "ministerio" : "vida_crista";
            partes.append("""
                    {
                      "numero": %d,
                      "tema": "Tema da parte %d",
                      "duracao_minutos": 5,
                      "secao": "%s",
                      "secao_titulo": "Seção de teste"%s
                    }
                    """.formatted(numero, numero, secao, numero == 4
                    ? ", \"subtitulo\": \"De casa em casa\", \"descricao_designacao\": \"De casa em casa.\"" : ""));
        }
        return """
                {
                  "schema_version": "1.0",
                  "status_conferencia": "pendente",
                  "semanas": [
                    {
                      "arquivo_origem": "mwb_T_202611_00.rtf",
                      "data_inicio": "2026-11-02",
                      "data_fim": "2026-11-08",
                      "leitura_biblica": "Leitura de teste",
                      "canticos": {"abertura": 1, "intermediario": 2, "encerramento": 3},
                      "partes": [%s]
                    }
                  ],
                  "alertas": []
                }
                """.formatted(partes);
    }
}
