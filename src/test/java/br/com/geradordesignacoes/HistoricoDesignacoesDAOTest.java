package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoricoDesignacoesDAOTest {

    @Test
    void deveSalvarECarregarHistorico() {

        PessoaDAO pessoaDAO = new PessoaDAO();
        ParteDAO parteDAO = new ParteDAO();
        HistoricoDesignacoesDAO historicoDAO =
                new HistoricoDesignacoesDAO();

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
}