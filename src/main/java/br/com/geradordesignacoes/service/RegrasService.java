package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;

import java.util.List;

import static br.com.geradordesignacoes.model.TipoParticipacao.*;

public class RegrasService {
    public boolean podeDesignar(
            Pessoa pessoa,
            Parte parte,
            List<Pessoa> pessoasJaDesignadas
    ) {

        if (pessoa == null
                || parte == null
                || pessoasJaDesignadas.contains(pessoa)
                || !pessoa.isAtivo()) {

            return false;
        }


        for (TipoParticipacao tipo :
                parte.getParticipacoesNecessarias()) {


            if (tipo == PRESIDENTE
                    && !podePresidirReuniao(pessoa)) {

                continue;
            }


            if (parte.pessoaPodeExercerParticipacao(
                    pessoa,
                    tipo
            )) {

                return true;
            }
        }


        return false;
    }

    public boolean podePresidirReuniao(Pessoa pessoa) {

        return pessoa != null
                && pessoa.isAtivo()
                && pessoa.getSexo() == Sexo.MASCULINO
                && pessoa.getPrivilegio().atende(Privilegio.BATIZADO)
                && pessoa.podeSerPresidente();
    }

    public boolean podeFormarDemonstracao(Pessoa responsavel, Pessoa ajudante, List<Pessoa> pessoasJaDesignadas) {
        return responsavel != null
                && ajudante != null
                && responsavel != ajudante
                && !pessoasJaDesignadas.contains(responsavel)
                && !pessoasJaDesignadas.contains(ajudante)
                && responsavel.isAtivo()
                && ajudante.isAtivo()
                && responsavel.getSexo() == ajudante.getSexo()
                && responsavel.podeExercer(TipoParticipacao.RESPONSAVEL)
                && ajudante.podeExercer(TipoParticipacao.AJUDANTE);
    }

    public boolean podeFormarDemonstracao(
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante,
            List<Pessoa> pessoasJaDesignadas
    ) {

        return podeFormarDemonstracao(
                responsavel,
                ajudante,
                pessoasJaDesignadas
        )
                && parte != null
                && parte.pessoaPodeExercerParticipacao(
                responsavel,
                TipoParticipacao.RESPONSAVEL
        )
                && parte.pessoaPodeExercerParticipacao(
                ajudante,
                TipoParticipacao.AJUDANTE
        );
    }
}
