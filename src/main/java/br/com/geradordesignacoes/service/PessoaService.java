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


    public Pessoa salvar(Pessoa pessoa) {

        validarPessoa(pessoa);

        return pessoaDAO.salvar(pessoa);
    }


    public List<Pessoa> listarTodas() {
        return pessoaDAO.listarTodos();
    }


    public Optional<Pessoa> buscarPorId(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da pessoa não pode ser nulo."
            );
        }

        return pessoaDAO.buscarPorId(id);
    }


    public void atualizar(Pessoa pessoa) {

        if (pessoa == null) {
            throw new IllegalArgumentException(
                    "A pessoa não pode ser nula."
            );
        }

        if (pessoa.getId() == null) {
            throw new IllegalArgumentException(
                    "A pessoa precisa possuir ID para atualização."
            );
        }

        validarPessoa(pessoa);

        pessoaDAO.atualizar(pessoa);
    }


    public void excluir(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da pessoa não pode ser nulo."
            );
        }

        pessoaDAO.excluir(id);
    }


    private void validarPessoa(Pessoa pessoa) {

        if (pessoa == null) {
            throw new IllegalArgumentException(
                    "A pessoa não pode ser nula."
            );
        }

        if (pessoa.getNome() == null || pessoa.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O nome da pessoa é obrigatório."
            );
        }

        if (pessoa.getSexo() == null) {
            throw new IllegalArgumentException(
                    "O sexo da pessoa é obrigatório."
            );
        }

        if (pessoa.getPrivilegio() == null) {
            throw new IllegalArgumentException(
                    "O privilégio da pessoa é obrigatório."
            );
        }
    }
}