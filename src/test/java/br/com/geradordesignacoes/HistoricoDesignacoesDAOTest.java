package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoricoDesignacoesDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final HistoricoDesignacoesDAO historicoDAO =
            new HistoricoDesignacoesDAO();


    @BeforeEach
    void limparBanco() {

        historicoDAO.limpar();
    }


    @Test
    void deveSalvarECarregarHistorico() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Teste Histórico",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = new Parte(
                "Leitura Histórico",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        Parte parteSalva = parteDAO.salvar(parte);

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parteSalva,
                        TipoParticipacao.LEITOR
                );

        historicoDAO.salvar(participacao);

        HistoricoDesignacoes historico =
                historicoDAO.carregarHistorico();

        assertNotNull(historico);

        assertFalse(
                historico.getParticipacoes().isEmpty()
        );

        ParticipacaoDesignacao recuperada =
                historico.getParticipacoes()
                        .get(historico.getParticipacoes().size() - 1);

        assertEquals(
                pessoa.getId(),
                recuperada.getPessoa().getId()
        );

        assertEquals(
                parteSalva.getId(),
                recuperada.getParte().getId()
        );

        assertEquals(
                TipoParticipacao.LEITOR,
                recuperada.getTipoParticipacao()
        );

        assertEquals(
                LocalDate.now(),
                recuperada.getData()
        );
    }


    @Test
    void deveBuscarParticipacoesPorPessoa() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Buscar",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Buscar",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.buscarPorPessoa(
                        pessoa.getId()
                );

        assertFalse(resultado.isEmpty());

        assertEquals(
                pessoa.getId(),
                resultado.get(0).getPessoa().getId()
        );
    }


    @Test
    void deveBuscarParticipacoesPorParte() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Parte",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Teste",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.buscarPorParte(
                        parte.getId()
                );

        assertFalse(resultado.isEmpty());

        assertEquals(
                parte.getId(),
                resultado.get(0).getParte().getId()
        );
    }


    @Test
    void deveSalvarVariasParticipacoes() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Lista",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Lista",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        List<ParticipacaoDesignacao> lista = List.of(

                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                ),

                new ParticipacaoDesignacao(
                        LocalDate.now().plusDays(1),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        historicoDAO.salvarTodos(lista);

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        assertEquals(
                2,
                resultado.size()
        );
    }


    @Test
    void deveLimparHistorico() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Limpar",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Limpar",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        assertFalse(
                historicoDAO.listarTodas().isEmpty()
        );

        historicoDAO.limpar();

        assertTrue(
                historicoDAO.listarTodas().isEmpty()
        );
    }
}