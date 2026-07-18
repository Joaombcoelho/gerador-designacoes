package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SeletorPessoaService {

    private final RegrasService regrasService;
    private final AvaliadorPessoaService avaliadorPessoaService;


    public SeletorPessoaService(
            RegrasService regrasService,
            AvaliadorPessoaService avaliadorPessoaService
    ) {

        this.regrasService = regrasService;
        this.avaliadorPessoaService = avaliadorPessoaService;
    }


    public Optional<Pessoa> selecionarMelhorPessoa(
            Parte parte,
            List<Pessoa> pessoas,
            ControleDesignacoes controle
    ) {

        return pessoas.stream()

                .filter(pessoa ->
                        regrasService.podeDesignar(
                                pessoa,
                                parte,
                                List.of()
                        )
                )

                .max(
                        Comparator.comparingInt(
                                pessoa -> avaliadorPessoaService.avaliar(
                                        pessoa,
                                        parte,
                                        controle
                                )
                        )
                );
    }
}