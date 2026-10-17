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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevisaoMapeamentoApostilaServiceTest {

    private final RevisaoMapeamentoApostilaService service =
            new RevisaoMapeamentoApostilaService();

    @Test
    void deveAplicarEscolhaManualQuandoTipoNaoFoiInferido() {
        var mapeamento = new MapeadorParteApostilaService.ResultadoMapeamento(
                4, "Tema", 5, SecaoParte.MINISTERIO, null,
                MapeadorParteApostilaService.SituacaoMapeamento.REQUER_CONFERENCIA,
                List.of()
        );
        Parte selecionada = parte(12, SecaoParte.MINISTERIO, TipoParte.DEMONSTRACAO);

        var resultado = service.aplicarSelecaoManual(mapeamento, selecionada);

        assertEquals(
                MapeadorParteApostilaService.SituacaoMapeamento.CORRESPONDENCIA_UNICA,
                resultado.situacao()
        );
        assertEquals(selecionada, resultado.candidatos().getFirst());
        assertEquals(TipoParte.DEMONSTRACAO, resultado.tipoInferido());
    }

    @Test
    void deveRejeitarParteDeOutraSecao() {
        var mapeamento = new MapeadorParteApostilaService.ResultadoMapeamento(
                4, "Tema", 5, SecaoParte.MINISTERIO, null,
                MapeadorParteApostilaService.SituacaoMapeamento.REQUER_CONFERENCIA,
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.aplicarSelecaoManual(
                        mapeamento,
                        parte(12, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                                TipoParte.DEMONSTRACAO)
                )
        );
    }

    @Test
    void deveRejeitarTipoDiferenteDoTipoInferido() {
        var mapeamento = new MapeadorParteApostilaService.ResultadoMapeamento(
                4, "Tema", 5, SecaoParte.MINISTERIO, TipoParte.INICIANDO_CONVERSAS,
                MapeadorParteApostilaService.SituacaoMapeamento.AMBIGUO,
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.aplicarSelecaoManual(
                        mapeamento,
                        parte(12, SecaoParte.MINISTERIO, TipoParte.DEMONSTRACAO)
                )
        );
    }

    @Test
    void deveRejeitarParteSemIdDeCatalogo() {
        var mapeamento = new MapeadorParteApostilaService.ResultadoMapeamento(
                4, "Tema", 5, SecaoParte.MINISTERIO, null,
                MapeadorParteApostilaService.SituacaoMapeamento.REQUER_CONFERENCIA,
                List.of()
        );

        assertThrows(IllegalArgumentException.class, () ->
                service.aplicarSelecaoManual(
                        mapeamento,
                        parte(null, SecaoParte.MINISTERIO, TipoParte.DEMONSTRACAO)
                )
        );
    }

    private static Parte parte(Integer id, SecaoParte secao, TipoParte tipo) {
        return new Parte(
                id,
                "Parte de teste",
                tipo,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                secao,
                TipoVariacaoParte.VARIAVEL,
                true,
                1,
                List.of()
        );
    }
}
