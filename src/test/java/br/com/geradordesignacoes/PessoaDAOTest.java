package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PessoaDAOTest {

    @Test
    void deveSalvarEBuscarPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Teste João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        assertNotNull(pessoa.getId());

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isPresent());

        Pessoa recuperada = encontrada.get();

        assertEquals(pessoa.getNome(), recuperada.getNome());
        assertEquals(pessoa.getSexo(), recuperada.getSexo());
        assertEquals(pessoa.getPrivilegio(), recuperada.getPrivilegio());
        assertEquals(pessoa.isAtivo(), recuperada.isAtivo());
        assertEquals(pessoa.podeSerResponsavel(), recuperada.podeSerResponsavel());
        assertEquals(pessoa.podeSerAjudante(), recuperada.podeSerAjudante());
        assertEquals(pessoa.podeFazerLeitura(), recuperada.podeFazerLeitura());
        assertEquals(pessoa.podeFazerDiscurso(), recuperada.podeFazerDiscurso());
    }
}