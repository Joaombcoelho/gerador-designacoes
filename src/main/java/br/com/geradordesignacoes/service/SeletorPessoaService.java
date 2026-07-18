package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoAvaliacaoPessoa;

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

        Pessoa melhorPessoa = null;
        ResultadoAvaliacaoPessoa melhorResultado = null;


        for (Pessoa pessoa : pessoas) {

            boolean podeSerDesignada =
                    regrasService.podeDesignar(
                            pessoa,
                            parte,
                            List.of()
                    );


            if (!podeSerDesignada) {
                continue;
            }


            ResultadoAvaliacaoPessoa resultado =
                    avaliadorPessoaService.avaliar(
                            pessoa,
                            parte,
                            controle
                    );


            if (melhorResultado == null ||
                    resultado.getTotal() > melhorResultado.getTotal()) {

                melhorResultado = resultado;
                melhorPessoa = pessoa;
            }
        }


        return Optional.ofNullable(melhorPessoa);
    }
}