package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProgramacaoSemanaServiceDuracaoTest extends BaseDAOTest {

    @Test
    void deveCopiarPadraoParaNovasOcorrenciasSemAlterarSemanasExistentes() {
        ProgramacaoSemanaService service = new ProgramacaoSemanaService();
        ParteDAO parteDAO = new ParteDAO();

        Parte parte = service.listarTodas().stream()
                .filter(p -> p.getTipoVariacao() == TipoVariacaoParte.FIXA)
                .findFirst()
                .orElseThrow();

        parte.setDuracaoPadraoMinutos(12);
        parteDAO.atualizar(parte);

        LocalDate semanaAntiga = LocalDate.of(2027, 3, 3);
        ProgramacaoSemana programacaoAntiga = service.obterOuCriar(semanaAntiga);
        ProgramacaoParte ocorrenciaAntiga = buscar(programacaoAntiga, parte.getId());
        assertEquals(12, ocorrenciaAntiga.getDuracaoMinutos());

        parte.setDuracaoPadraoMinutos(18);
        parteDAO.atualizar(parte);

        assertEquals(12, buscar(service.obterOuCriar(semanaAntiga), parte.getId())
                .getDuracaoMinutos());

        LocalDate semanaNova = LocalDate.of(2027, 3, 10);
        assertEquals(18, buscar(service.obterOuCriar(semanaNova), parte.getId())
                .getDuracaoMinutos());

        service.definirDuracaoMinutos(semanaAntiga, ocorrenciaAntiga.getOrdem(), 20);
        assertEquals(20, buscar(service.obterOuCriar(semanaAntiga), parte.getId())
                .getDuracaoMinutos());
        assertEquals(18, buscar(service.obterOuCriar(semanaNova), parte.getId())
                .getDuracaoMinutos());
    }

    @Test
    void devePermitirDeixarDuracaoSemanalPendente() {
        ProgramacaoSemanaService service = new ProgramacaoSemanaService();
        ProgramacaoSemana programacao = service.obterOuCriar(LocalDate.of(2027, 4, 7));
        ProgramacaoParte ocorrencia = programacao.partes().getFirst();

        service.definirDuracaoMinutos(programacao.data(), ocorrencia.getOrdem(), null);

        assertNull(buscar(service.obterOuCriar(programacao.data()),
                ocorrencia.getParte().getId()).getDuracaoMinutos());
    }

    private ProgramacaoParte buscar(ProgramacaoSemana programacao, Integer parteId) {
        return programacao.partes().stream()
                .filter(p -> p.getParte().getId().equals(parteId))
                .findFirst()
                .orElseThrow();
    }
}
