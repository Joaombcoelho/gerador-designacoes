package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.HabilidadeDesignacaoDAO;
import br.com.geradordesignacoes.model.Pessoa;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PessoaService {

    private final PessoaDAO pessoaDAO;
    private final HabilidadeDesignacaoDAO habilidadeDAO = new HabilidadeDesignacaoDAO();


    public PessoaService(PessoaDAO pessoaDAO) {
        this.pessoaDAO = pessoaDAO;
    }


    public Pessoa salvar(Pessoa pessoa) {

        validarPessoa(pessoa);

        Pessoa salva = pessoaDAO.salvar(pessoa);
        habilidadeDAO.substituirCodigos(salva.getId(), pessoa.getHabilidadesDesignacao());
        return salva;
    }


    public List<Pessoa> listarTodas() {
        return pessoaDAO.listarTodos().stream().map(this::carregarHabilidades).toList();
    }


    public List<Pessoa> listarAtivos() {
        return pessoaDAO.listarAtivos().stream().map(this::carregarHabilidades).toList();
    }

    public List<Pessoa> listarArquivados() {
        return pessoaDAO.listarArquivados().stream().map(this::carregarHabilidades).toList();
    }

    public void arquivar(Integer id) {
        if (id == null) throw new IllegalArgumentException("O ID não pode ser nulo.");
        pessoaDAO.arquivar(id);
    }

    public void restaurar(Integer id) {
        if (id == null) throw new IllegalArgumentException("O ID não pode ser nulo.");
        pessoaDAO.restaurar(id);
    }


    public Optional<Pessoa> buscarPorId(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da pessoa não pode ser nulo."
            );
        }

        return pessoaDAO.buscarPorId(id).map(this::carregarHabilidades);
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
        habilidadeDAO.substituirCodigos(pessoa.getId(), pessoa.getHabilidadesDesignacao());
    }


    public void excluir(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "O ID da pessoa não pode ser nulo."
            );
        }

        pessoaDAO.arquivar(id);
    }


    private Pessoa carregarHabilidades(Pessoa pessoa) {
        if (pessoa.getId() != null) {
            pessoa.setHabilidadesDesignacao(Set.copyOf(habilidadeDAO.listarCodigosPorPessoa(pessoa.getId())));
        }
        return pessoa;
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