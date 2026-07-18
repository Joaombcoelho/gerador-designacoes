package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Pessoa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ControleDesignacoes {

    private final Map<Pessoa, Integer> quantidadePorPessoa;

    private final List<Pessoa> pessoasDesignadas;


    public ControleDesignacoes() {

        this.quantidadePorPessoa = new HashMap<>();
        this.pessoasDesignadas = new ArrayList<>();
    }


    public void registrar(Pessoa pessoa) {

        quantidadePorPessoa.merge(
                pessoa,
                1,
                Integer::sum
        );

        pessoasDesignadas.add(pessoa);
    }


    public int quantidadeDe(Pessoa pessoa) {

        return quantidadePorPessoa.getOrDefault(
                pessoa,
                0
        );
    }


    public List<Pessoa> getPessoasDesignadas() {

        return new ArrayList<>(
                pessoasDesignadas
        );
    }
}