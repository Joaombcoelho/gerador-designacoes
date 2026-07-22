package br.com.geradordesignacoes.service;

import java.util.List;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParticipacao;

import static br.com.geradordesignacoes.model.TipoParte.*;

public class RegrasService {
    public boolean podeDesignar(Pessoa pessoa, Parte parte, List<Pessoa> pessoasJaDesignadas) {
        if (pessoa == null || parte == null || pessoasJaDesignadas.contains(pessoa) || !pessoa.isAtivo()) {
            return false;
        }

        return switch (parte.getTipo()) {

            case LEITURA ->
                    parte.pessoaPodeExercerParticipacao(
                            pessoa,
                            TipoParticipacao.LEITOR
                    );

            case DISCURSO ->
                    parte.pessoaPodeExercerParticipacao(
                            pessoa,
                            TipoParticipacao.ORADOR
                    );

            case DEMONSTRACAO ->
                    parte.pessoaPodeExercerParticipacao(
                            pessoa,
                            TipoParticipacao.RESPONSAVEL
                    )
                            ||
                            parte.pessoaPodeExercerParticipacao(
                                    pessoa,
                                    TipoParticipacao.AJUDANTE
                            );
        };
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
