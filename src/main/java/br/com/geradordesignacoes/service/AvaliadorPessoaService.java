package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.ResultadoAvaliacaoPessoa;

public class AvaliadorPessoaService {

    public ResultadoAvaliacaoPessoa avaliar(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle
    ) {

        int pontosParticipacoes =
                pontuarQuantidadeParticipacoes(
                        controle.quantidadeDe(pessoa)
                );

        int pontosPrivilegio =
                pontuarPrivilegio(
                        pessoa,
                        parte
                );

        int penalidadeRepeticao =
                pontuarRepeticaoParte(
                        pessoa,
                        parte,
                        controle
                );

        return new ResultadoAvaliacaoPessoa(
                pessoa,
                pontosParticipacoes,
                pontosPrivilegio,
                penalidadeRepeticao
        );
    }


    private int pontuarQuantidadeParticipacoes(int quantidade) {

        return switch (quantidade) {
            case 0 -> 50;
            case 1 -> 40;
            case 2 -> 30;
            case 3 -> 20;
            default -> 10;
        };
    }


    private int pontuarPrivilegio(
            Pessoa pessoa,
            Parte parte
    ) {

        int diferencaNivel =
                pessoa.getPrivilegio().getNivel()
                        - parte.getPrivilegioMinimo().getNivel();

        if (diferencaNivel <= 0) {
            return 0;
        }

        return diferencaNivel * 10;
    }


    private int pontuarRepeticaoParte(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle
    ) {

        long repeticoes =
                controle.quantidadeVezesNaParte(
                        pessoa,
                        parte
                );

        return (int) repeticoes * 15;
    }
}