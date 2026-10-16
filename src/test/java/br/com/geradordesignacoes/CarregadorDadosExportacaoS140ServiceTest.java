package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.DadosExportacaoS140Semana;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.service.CarregadorDadosExportacaoS140Service;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CarregadorDadosExportacaoS140ServiceTest extends BaseDAOTest {

    private final EscalaDAO escalaDAO = new EscalaDAO();
    private final ProgramacaoSemanaDAO programacaoDAO = new ProgramacaoSemanaDAO();

    @Test
    void deveFalharSemCriarProgramacaoQuandoNaoHouverEscala() {
        LocalDate data = LocalDate.of(2027, 7, 7);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> criarService().carregar(data)
        );

        assertTrue(excecao.getMessage().contains("Não existe uma escala salva"));
        assertNull(programacaoDAO.buscarPorData(data),
                "A preparação para exportação não deve criar uma programação.");
    }

    @Test
    void deveFalharQuandoExistirEscalaMasNaoHouverProgramacao() {
        LocalDate data = LocalDate.of(2027, 7, 14);
        escalaDAO.salvar(new Escala(data, List.of()));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> criarService().carregar(data)
        );

        assertTrue(excecao.getMessage().contains("Não existe programação salva"));
        assertNull(programacaoDAO.buscarPorData(data),
                "O serviço não deve criar registros ausentes.");
    }

    @Test
    void deveRetornarProgramacaoEEscalaDaMesmaDataSemCriarNovaEscala() {
        LocalDate data = LocalDate.of(2027, 7, 21);
        var programacao = new ProgramacaoSemanaService().obterOuCriar(data);
        Escala escalaSalva = escalaDAO.salvar(new Escala(data, List.of()));

        DadosExportacaoS140Semana dados = criarService().carregar(data);

        assertEquals(data, dados.data());
        assertEquals(programacao.data(), dados.programacao().data());
        assertEquals(escalaSalva.getId(), dados.escala().getId());
        assertEquals(1, escalaDAO.listarTodas().size());
        assertEquals(1, programacaoDAO.buscarPorData(data).id());
    }

    @Test
    void deveCarregarNoS140AAlteracaoManualMaisRecenteSalvaNoBanco() {
        LocalDate data = LocalDate.of(2027, 7, 28);
        PessoaDAO pessoaDAO = new PessoaDAO();
        ParteDAO parteDAO = new ParteDAO();

        var pessoaOriginal = pessoaDAO.salvar(new br.com.geradordesignacoes.model.Pessoa(
                "Responsável original", br.com.geradordesignacoes.model.Sexo.MASCULINO,
                true, true, true, true, false,
                br.com.geradordesignacoes.model.Privilegio.ANCIAO
        ));
        var pessoaAtualizada = pessoaDAO.salvar(new br.com.geradordesignacoes.model.Pessoa(
                "Responsável atualizado", br.com.geradordesignacoes.model.Sexo.MASCULINO,
                true, true, true, true, false,
                br.com.geradordesignacoes.model.Privilegio.ANCIAO
        ));
        var parte = parteDAO.salvar(new br.com.geradordesignacoes.model.Parte(
                "Parte de teste",
                br.com.geradordesignacoes.model.TipoParte.DISCURSO_TESOUROS,
                br.com.geradordesignacoes.model.Privilegio.PUBLICADOR,
                false,
                br.com.geradordesignacoes.model.SexoPermitido.MASCULINO,
                1,
                false,
                List.of(br.com.geradordesignacoes.model.TipoParticipacao.RESPONSAVEL)
        ));

        new ProgramacaoSemanaService().obterOuCriar(data);
        escalaDAO.salvar(new Escala(data, List.of(
                new br.com.geradordesignacoes.model.Designacao(
                        data, parte, pessoaOriginal, null
                )
        )));
        var designacao = escalaDAO.buscarPorData(data)
                .orElseThrow()
                .getDesignacoes()
                .getFirst();

        escalaDAO.atualizarDesignacao(
                designacao.id(),
                pessoaAtualizada.getId(),
                null
        );

        DadosExportacaoS140Semana dados = criarService().carregar(data);

        assertEquals(
                pessoaAtualizada.getId(),
                dados.escala().getDesignacoes().getFirst().responsavel().getId()
        );
        assertNotEquals(
                pessoaOriginal.getId(),
                dados.escala().getDesignacoes().getFirst().responsavel().getId()
        );
    }

    @Test
    void deveRejeitarDataNula() {
        assertThrows(IllegalArgumentException.class,
                () -> criarService().carregar(null));
    }

    private CarregadorDadosExportacaoS140Service criarService() {
        return new CarregadorDadosExportacaoS140Service(escalaDAO, programacaoDAO);
    }
}
