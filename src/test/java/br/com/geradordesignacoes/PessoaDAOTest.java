package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import org.junit.jupiter.api.Test;

import java.util.List;
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
    @Test
    void deveAtualizarPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Original",
                Sexo.MASCULINO,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoaDAO.salvar(pessoa);

        Pessoa atualizada = new Pessoa(
                "Pessoa Atualizada",
                Sexo.FEMININO,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        atualizada.setId(pessoa.getId());

        pessoaDAO.atualizar(atualizada);

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isPresent());

        Pessoa recuperada = encontrada.get();

        assertEquals(pessoa.getId(), recuperada.getId());
        assertEquals("Pessoa Atualizada", recuperada.getNome());
        assertEquals(Sexo.FEMININO, recuperada.getSexo());
        assertFalse(recuperada.isAtivo());

        assertFalse(recuperada.podeSerResponsavel());
        assertTrue(recuperada.podeSerAjudante());
        assertTrue(recuperada.podeFazerLeitura());
        assertTrue(recuperada.podeFazerDiscurso());
        assertTrue(recuperada.podeFazerOracao());
        assertTrue(recuperada.podeSerPresidente());
        assertTrue(recuperada.podeSerDirigente());

        assertEquals(Privilegio.BATIZADO, recuperada.getPrivilegio());
        assertEquals(NivelLeitura.EXPERIENTE, recuperada.getNivelLeitura());

    }

    @Test
    void deveRetornarVazioAoBuscarPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(999999);

        assertTrue(encontrada.isEmpty());

    }

    @Test
    void deveListarTodasAsPessoas() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa1 = new Pessoa(
                "Pessoa Lista 1",
                Sexo.MASCULINO,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        Pessoa pessoa2 = new Pessoa(
                "Pessoa Lista 2",
                Sexo.FEMININO,
                true,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        pessoaDAO.salvar(pessoa1);
        pessoaDAO.salvar(pessoa2);

        List<Pessoa> pessoas = pessoaDAO.listarTodos();

        assertNotNull(pessoas);
        assertTrue(pessoas.size() >= 2);

        assertTrue(
                pessoas.stream()
                        .anyMatch(p -> p.getId().equals(pessoa1.getId()))
        );

        assertTrue(
                pessoas.stream()
                        .anyMatch(p -> p.getId().equals(pessoa2.getId()))
        );

    }

    @Test
    void deveExcluirPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Para Excluir",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoaDAO.salvar(pessoa);

        assertNotNull(pessoa.getId());

        pessoaDAO.excluir(pessoa.getId());

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isEmpty());

    }

    @Test
    void deveLancarExcecaoAoAtualizarPessoaSemId() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Sem ID",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pessoaDAO.atualizar(pessoa)
                );

        assertEquals(
                "A pessoa precisa possuir um ID para ser atualizada.",
                excecao.getMessage()
        );

    }

    @Test
    void deveLancarExcecaoAoExcluirPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        RuntimeException excecao =
                assertThrows(
                        RuntimeException.class,
                        () -> pessoaDAO.excluir(999999)
                );

        assertEquals(
                "Pessoa não encontrada para exclusão.",
                excecao.getMessage()
        );

    }

    @Test
    void deveLancarExcecaoAoAtualizarPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Inexistente",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoa.setId(999999);

        RuntimeException excecao =
                assertThrows(
                        RuntimeException.class,
                        () -> pessoaDAO.atualizar(pessoa)
                );

        assertEquals(
                "Erro ao atualizar pessoa.",
                excecao.getMessage()
        );
    }
}