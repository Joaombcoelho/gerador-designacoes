package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.*;

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
                pontuarRepeticaoParticipacao(
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

        Privilegio minimo =
                parte.getPrivilegioMinimo();

        if (minimo == Privilegio.SERVO_MINISTERIAL) {

            return pessoa.getPrivilegio()
                    == Privilegio.SERVO_MINISTERIAL
                    ? 10
                    : 0;
        }

        int diferencaNivel =
                pessoa.getPrivilegio().getNivel()
                        - minimo.getNivel();

        if (diferencaNivel <= 0) {
            return 0;
        }

        return diferencaNivel * 10;
    }


    private int pontuarRepeticaoParticipacao(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle
    ) {

        TipoParticipacao tipoParticipacao =
                parte.getParticipacoesNecessarias()
                        .stream()
                        .findFirst()
                        .orElse(null);

        if (tipoParticipacao == null) {
            return 0;
        }

        long repeticoes =
                controle.quantidadeVezesNaParticipacao(
                        pessoa,
                        parte,
                        tipoParticipacao
                );

        return (int) repeticoes * 15;
    }
}