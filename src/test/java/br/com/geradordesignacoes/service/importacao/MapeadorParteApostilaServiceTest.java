package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.TipoParte;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MapeadorParteApostilaServiceTest {

    private final MapeadorParteApostilaService service =
            new MapeadorParteApostilaService();

    @Test
    void deveReconhecerTipoPeloSubtituloSemDependerDeAcentos() {
        var parte = parte("Iniciando conversas", null);

        assertEquals(TipoParte.INICIANDO_CONVERSAS, service.inferirTipo(parte));
    }

    @Test
    void deveReconhecerTipoPelaDescricaoQuandoSubtituloNaoExiste() {
        var parte = parte(null, "Demonstração: Cultivando interesse.");

        assertEquals(TipoParte.CULTIVANDO_INTERESSE, service.inferirTipo(parte));
    }

    @Test
    void deveDeixarTipoSemInferenciaQuandoTextoNaoForConhecido() {
        var parte = parte("Discurso", "Considere como aplicar o texto bíblico.");

        assertNull(service.inferirTipo(parte));
    }

    private static ImportacaoApostilaJsonService.ParteApostila parte(
            String subtitulo,
            String descricao
    ) {
        return new ImportacaoApostilaJsonService.ParteApostila(
                4,
                "Tema de teste",
                5,
                SecaoParte.MINISTERIO,
                "Nossa Vida e Ministério Cristão",
                subtitulo,
                descricao
        );
    }
}
