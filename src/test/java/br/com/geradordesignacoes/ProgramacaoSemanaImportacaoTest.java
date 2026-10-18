package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramacaoSemanaImportacaoTest extends BaseDAOTest {

    @Test
    void permiteRepetirParteNoFluxoDeImportacaoMasNaoNaSelecaoManualNormal() {
        ProgramacaoSemanaService service = new ProgramacaoSemanaService();
        List<Parte> variaveis = service.listarPartesVariaveis();
        assertTrue(variaveis.size() >= 2);

        Parte repetida = variaveis.getFirst();
        Parte outra = variaveis.get(1);
        List<Parte> selecao = List.of(repetida, repetida, outra);

        assertThrows(IllegalArgumentException.class,
                () -> service.validarPartesVariaveis(selecao));

        ProgramacaoSemana programacao = service.montarProgramacaoImportada(
                LocalDate.of(2026, 11, 4), selecao);

        List<Integer> ordens = programacao.partes().stream()
                .filter(item -> item.getParte().getId().equals(repetida.getId()))
                .map(item -> item.getOrdem())
                .toList();

        assertEquals(2, ordens.size());
        assertNotEquals(ordens.get(0), ordens.get(1));
    }
}
