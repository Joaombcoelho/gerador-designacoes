package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.TipoParticipacao;
import java.util.List;
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
    void deveReconhecerTipoPeloTemaQuandoSubtituloEDescricaoNaoExistem() {
        assertEquals(TipoParte.JOIAS_ESPIRITUAIS,
                service.inferirTipo(parte(2, "Joias espirituais", SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS, null, null)));
        assertEquals(TipoParte.LEITURA,
                service.inferirTipo(parte(3, "Leitura da Bíblia", SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS, null, null)));
    }

    @Test
    void deveReconhecerPartesNumeradasEEstudoBiblicoNaUltimaSecao() {
        assertEquals(TipoParte.DISCURSO,
                service.inferirTipo(parte(7, "Nunca se Esqueça do Que Jeová se Lembra",
                        SecaoParte.NOSSA_VIDA_CRISTA, null, null)));
        assertEquals(TipoParte.DIRIGENTE_ESTUDO,
                service.inferirTipo(parte(8, "Estudo bíblico de congregação",
                        SecaoParte.NOSSA_VIDA_CRISTA, null, null)));
    }

    @Test
    void deveDeixarTipoSemInferenciaQuandoTextoNaoForConhecido() {
        var parte = parte("Discurso", "Considere como aplicar o texto bíblico.");

        assertNull(service.inferirTipo(parte));
    }


    @Test
    void deveManterCompatibilidadeComCatalogoDeDiscursosLegadosSemEscolherArbitrariamente() {
        List<Parte> catalogo = List.of(
                parteCatalogo("Parte 1", TipoParte.PARTE_1, 18),
                parteCatalogo("Parte 2", TipoParte.PARTE_2, 19),
                parteCatalogo("Parte 3", TipoParte.PARTE_3, 20));

        var resultado = service.mapear(
                parte(7, "Nunca se Esqueça do Que Jeová se Lembra",
                        SecaoParte.NOSSA_VIDA_CRISTA, null, null),
                catalogo);

        assertEquals(TipoParte.DISCURSO, resultado.tipoInferido());
        assertEquals(MapeadorParteApostilaService.SituacaoMapeamento.AMBIGUO,
                resultado.situacao());
        assertEquals(3, resultado.candidatos().size());
    }

    private static Parte parteCatalogo(String nome, TipoParte tipo, int ordem) {
        return new Parte(
                nome, tipo, Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS,
                1, false, NivelLeitura.BASICO, SecaoParte.NOSSA_VIDA_CRISTA,
                null, false, ordem, List.of(TipoParticipacao.ORADOR));
    }

    private static ImportacaoApostilaJsonService.ParteApostila parte(
            String subtitulo,
            String descricao
    ) {
        return parte(4, "Tema de teste", SecaoParte.MINISTERIO, subtitulo, descricao);
    }

    private static ImportacaoApostilaJsonService.ParteApostila parte(
            int numero,
            String tema,
            SecaoParte secao,
            String subtitulo,
            String descricao
    ) {
        return new ImportacaoApostilaJsonService.ParteApostila(
                numero, tema, 5, secao, "Seção de teste", subtitulo, descricao
        );
    }
}
