package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.Pessoa;

import java.util.List;
import java.util.Optional;

public class PessoaService {

    private final PessoaDAO pessoaDAO;

    public PessoaService(PessoaDAO pessoaDAO) {
        this.pessoaDAO = pessoaDAO;
    }

    public void salvar(Pessoa pessoa) {
        pessoaDAO.salvar(pessoa);
    }

    public List<Pessoa> listarTodas() {
        return pessoaDAO.listarTodos();
    }

    public Optional<Pessoa> buscarPorId(Integer id) {
        return pessoaDAO.buscarPorId(id);
    }

    public void atualizar(Pessoa pessoa) {
        pessoaDAO.atualizar(pessoa);
    }

    public void excluir(Integer id) {
        pessoaDAO.excluir(id);
    }
}