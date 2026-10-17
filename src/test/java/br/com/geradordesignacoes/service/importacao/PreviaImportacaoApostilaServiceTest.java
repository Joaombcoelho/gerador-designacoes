package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreviaImportacaoApostilaServiceTest {

    @TempDir
    Path pastaTemporaria;

    private final PreviaImportacaoApostilaService service =
            new PreviaImportacaoApostilaService();

    @Test
    void deveExigirDataDaReuniaoInformadaExplicitamente() throws Exception {
        Path json = criarJsonValido();

        var previa = service.preparar(
                json,
                Map.of(),
                List.of(),
                Set.of()
        );

        assertFalse(previa.erros().isEmpty());
        assertTrue(previa.erros().stream()
                .anyMatch(erro -> erro.contains("selecione a data efetiva da reunião")));
        assertFalse(previa.podeProsseguirParaConfirmacao());
    }

    @Test
    void deveBloquearProsseguimentoQuandoDataJaPossuiProgramacao() throws Exception {
        Path json = criarJsonValido();
        LocalDate inicioApostila = LocalDate.of(2026, 11, 2);
        LocalDate dataReuniao = LocalDate.of(2026, 11, 4);

        var previa = service.preparar(
                json,
                Map.of(inicioApostila, dataReuniao),
                List.<Parte>of(),
                Set.of(dataReuniao)
        );

        assertTrue(previa.semanas().getFirst().conflitoProgramacaoExistente());
        assertTrue(previa.avisos().stream()
                .anyMatch(aviso -> aviso.contains("Já existe uma programação")));
        assertFalse(previa.podeProsseguirParaConfirmacao());
    }

    @Test
    void deveDetectarDuasSemanasAssociadasAMesmaDataDeReuniao() throws Exception {
        Path json = criarJsonComDuasSemanas();
        LocalDate dataReuniao = LocalDate.of(2026, 11, 4);

        var previa = service.preparar(
                json,
                Map.of(
                        LocalDate.of(2026, 11, 2), dataReuniao,
                        LocalDate.of(2026, 11, 9), dataReuniao
                ),
                List.<Parte>of(),
                Set.of()
        );

        assertTrue(previa.erros().stream()
                .anyMatch(erro -> erro.contains("selecionada para mais de uma semana")));
        assertFalse(previa.podeProsseguirParaConfirmacao());
    }

    @Test
    void deveUsarEscolhaManualNaPreviaTecnica() throws Exception {
        Path json = criarJsonValido();
        LocalDate inicioApostila = LocalDate.of(2026, 11, 2);
        LocalDate dataReuniao = LocalDate.of(2026, 11, 4);
        Parte selecionada = parte(77, SecaoParte.MINISTERIO, TipoParte.DEMONSTRACAO);

        var previa = service.preparar(
                json,
                Map.of(inicioApostila, dataReuniao),
                List.of(selecionada),
                Set.of(),
                Map.of(inicioApostila, Map.of(4, selecionada))
        );

        var mapeamento = previa.semanas().getFirst().partes().get(3);
        assertEquals(MapeadorParteApostilaService.SituacaoMapeamento.CORRESPONDENCIA_UNICA,
                mapeamento.situacao());
        assertEquals(selecionada, mapeamento.candidatos().getFirst());
        assertEquals(TipoParte.DEMONSTRACAO, mapeamento.tipoInferido());
        assertTrue(previa.avisos().stream().anyMatch(aviso -> aviso.contains("exige conferência")));
        assertFalse(previa.podeProsseguirParaConfirmacao());
    }

    @Test
    void deveSinalizarParteManualDuplicadaNaMesmaSemana() throws Exception {
        Path json = criarJsonValido();
        LocalDate inicioApostila = LocalDate.of(2026, 11, 2);
        Parte selecionada = parte(77, SecaoParte.MINISTERIO, TipoParte.DEMONSTRACAO);

        var previa = service.preparar(
                json,
                Map.of(inicioApostila, LocalDate.of(2026, 11, 4)),
                List.of(selecionada),
                Set.of(),
                Map.of(inicioApostila, Map.of(4, selecionada, 5, selecionada))
        );

        assertTrue(previa.erros().stream()
                .anyMatch(erro -> erro.contains("mesma parte do catálogo")));
        assertFalse(previa.podeProsseguirParaConfirmacao());
    }

    private static Parte parte(Integer id, SecaoParte secao, TipoParte tipo) {
        return new Parte(
                id, "Parte de teste", tipo, Privilegio.PUBLICADOR, false,
                SexoPermitido.AMBOS, 1, false, NivelLeitura.BASICO,
                secao, TipoVariacaoParte.VARIAVEL, true, 1, List.of()
        );
    }

    private Path criarJsonValido() throws Exception {
        Path arquivo = pastaTemporaria.resolve("apostila.json");
        Files.writeString(arquivo, jsonSemana("2026-11-02", "2026-11-08"));
        return arquivo;
    }

    private Path criarJsonComDuasSemanas() throws Exception {
        Path arquivo = pastaTemporaria.resolve("apostila-duas-semanas.json");
        String primeira = jsonSemana("2026-11-02", "2026-11-08");
        String segunda = jsonSemana("2026-11-09", "2026-11-15");
        String conteudo = """
                {
                  "schema_version": "1.0",
                  "status_conferencia": "pendente",
                  "semanas": [%s, %s]
                }
                """.formatted(primeira, segunda);
        Files.writeString(arquivo, conteudo);
        return arquivo;
    }

    private static String jsonSemana(String inicio, String fim) {
        StringBuilder partes = new StringBuilder();
        for (int numero = 1; numero <= 8; numero++) {
            if (numero > 1) {
                partes.append(",");
            }
            String secao = numero <= 3 ? "tesouros"
                    : numero <= 6 ? "ministerio" : "vida_crista";
            partes.append("""
                    {
                      "numero": %d,
                      "tema": "Tema %d",
                      "duracao_minutos": 5,
                      "secao": "%s",
                      "secao_titulo": "Seção",
                      "subtitulo": "Subtítulo",
                      "descricao_designacao": "Descrição."
                    }
                    """.formatted(numero, numero, secao));
        }
        return """
                {
                  "data_inicio": "%s",
                  "data_fim": "%s",
                  "leitura_biblica": "Salmo 1",
                  "canticos": {"abertura": 1, "intermediario": 2, "encerramento": 3},
                  "partes": [%s]
                }
                """.formatted(inicio, fim, partes);
    }
}
