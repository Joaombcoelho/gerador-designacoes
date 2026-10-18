package br.com.geradordesignacoes;

import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgramacaoSemanaServiceDatasImportadasTest extends BaseDAOTest {

    @Test
    void deveListarProgramacaoPersistidaMesmoForaDoDiaHabitual() {
        ProgramacaoSemanaService service = new ProgramacaoSemanaService();
        LocalDate dataImportada = LocalDate.of(2026, 10, 20); // terça-feira

        service.obterOuCriar(dataImportada);

        assertTrue(
                service.listarDatasProgramadasNoMes(YearMonth.of(2026, 10))
                        .contains(dataImportada)
        );
    }
}
