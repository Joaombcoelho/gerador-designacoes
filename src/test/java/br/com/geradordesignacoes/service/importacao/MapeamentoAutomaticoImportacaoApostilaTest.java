package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapeamentoAutomaticoImportacaoApostilaTest {

    private final MapeadorParteApostilaService mapeador =
            new MapeadorParteApostilaService();

    @Test
    void identificaCorrespondenciaUnicaSemConfundirNumeroOficialComId() {
        Parte catalogada = parte(734, "Discurso de tesouros", TipoParte.DISCURSO_TESOUROS);
        var json = parteApostila(1, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                "Discurso de tesouros");

        var resultado = mapeador.mapear(json, List.of(catalogada));

        assertTrue(resultado.possuiCorrespondenciaUnica());
        assertEquals(1, resultado.numeroParteApostila());
        assertEquals(734, resultado.candidatos().getFirst().getId());
        assertNotEquals(resultado.numeroParteApostila(),
                resultado.candidatos().getFirst().getId());
    }

    @Test
    void deixaCorrespondenciaAmbiguaSemEscolhaAutomatica() {
        Parte primeira = parte(734, "Discurso de tesouros A", TipoParte.DISCURSO_TESOUROS);
        Parte segunda = parte(735, "Discurso de tesouros B", TipoParte.DISCURSO_TESOUROS);

        var resultado = mapeador.mapear(
                parteApostila(1, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                        "Discurso de tesouros"),
                List.of(primeira, segunda));

        assertEquals(MapeadorParteApostilaService.SituacaoMapeamento.AMBIGUO,
                resultado.situacao());
        assertFalse(resultado.possuiCorrespondenciaUnica());
        assertEquals(List.of(734, 735),
                resultado.candidatos().stream().map(Parte::getId).toList());
    }

    @Test
    void deixaSemCorrespondenciaQuandoCatalogoNaoContemOTipoInferido() {
        Parte catalogada = parte(734, "Joias espirituais", TipoParte.JOIAS_ESPIRITUAIS);

        var resultado = mapeador.mapear(
                parteApostila(1, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                        "Discurso de tesouros"),
                List.of(catalogada));

        assertEquals(MapeadorParteApostilaService.SituacaoMapeamento.NAO_ENCONTRADO,
                resultado.situacao());
        assertTrue(resultado.candidatos().isEmpty());
        assertFalse(resultado.possuiCorrespondenciaUnica());
    }

    private static ImportacaoApostilaJsonService.ParteApostila parteApostila(
            int numero, SecaoParte secao, String subtitulo) {
        return new ImportacaoApostilaJsonService.ParteApostila(
                numero, "Tema de teste", 5, secao, "Tesouros",
                subtitulo, "Discurso de tesouros");
    }

    private static Parte parte(int id, String nome, TipoParte tipo) {
        return new Parte(id, nome, tipo, Privilegio.PUBLICADOR, false,
                SexoPermitido.AMBOS, 1, false, NivelLeitura.BASICO,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoVariacaoParte.VARIAVEL, true, 1, List.of());
    }
}
