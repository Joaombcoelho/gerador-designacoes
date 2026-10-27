package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.HabilidadeDesignacaoCodigo;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.service.RequisitoHabilidadeParteService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequisitoHabilidadeParteServiceTest {

    private final RequisitoHabilidadeParteService service =
            new RequisitoHabilidadeParteService();

    @Test
    void deveAssociarPartesDeDiscursoAUmaUnicaHabilidade() {
        for (TipoParte tipo : new TipoParte[]{
                TipoParte.DISCURSO,
                TipoParte.PARTE_1,
                TipoParte.PARTE_2,
                TipoParte.PARTE_3
        }) {
            assertEquals(HabilidadeDesignacaoCodigo.DISCURSO,
                    service.habilidadeExigida(tipo).orElseThrow());
        }
    }

    @Test
    void deveMapearHabilidadesEspecificasDasPartes() {
        assertEquals(HabilidadeDesignacaoCodigo.PRESIDENCIA,
                service.habilidadeExigida(TipoParte.PRESIDENTE_REUNIAO).orElseThrow());
        assertEquals(HabilidadeDesignacaoCodigo.JOIAS_ESPIRITUAIS,
                service.habilidadeExigida(TipoParte.JOIAS_ESPIRITUAIS).orElseThrow());
        assertEquals(HabilidadeDesignacaoCodigo.TESOUROS,
                service.habilidadeExigida(TipoParte.DISCURSO_TESOUROS).orElseThrow());
        assertEquals(HabilidadeDesignacaoCodigo.NECESSIDADES_LOCAIS,
                service.habilidadeExigida(TipoParte.NECESSIDADES_LOCAIS).orElseThrow());
    }

    @Test
    void naoDeveSubstituirRegrasExistentesDeMinisterioELeitura() {
        assertTrue(service.habilidadeExigida(TipoParte.DEMONSTRACAO).isEmpty());
        assertTrue(service.habilidadeExigida(TipoParte.LEITURA).isEmpty());
        assertTrue(service.habilidadeExigida(TipoParte.INICIANDO_CONVERSAS).isEmpty());
        assertTrue(service.habilidadeExigida(TipoParte.DIRIGENTE_ESTUDO).isEmpty());
    }

    @Test
    void tipoNuloNaoPossuiHabilidade() {
        assertTrue(service.habilidadeExigida(null).isEmpty());
    }
}
