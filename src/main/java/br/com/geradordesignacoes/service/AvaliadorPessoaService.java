package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;

public class AvaliadorPessoaService {


    public int avaliar(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle
    ) {

        int pontos = 0;


        int quantidade = controle.quantidadeDe(pessoa);

        pontos += pontuarQuantidadeParticipacoes(quantidade);

        pontos += pontuarPrivilegio(
                pessoa,
                parte
        );


        return pontos;
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
                        -
                        parte.getPrivilegioMinimo().getNivel();


        if (diferencaNivel <= 0) {
            return 0;
        }


        return diferencaNivel * 10;
    }
}