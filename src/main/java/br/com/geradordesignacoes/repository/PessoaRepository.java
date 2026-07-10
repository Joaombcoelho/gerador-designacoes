package br.com.geradordesignacoes.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import br.com.geradordesignacoes.model.Pessoa;

public class PessoaRepository {
    private final List<Pessoa> pessoas = new ArrayList<>();

    public void adicionar(Pessoa pessoa) {
        pessoas.add(pessoa);
    }

    public List<Pessoa> listarTodos() {
        return Collections.unmodifiableList(pessoas);
    }
}
