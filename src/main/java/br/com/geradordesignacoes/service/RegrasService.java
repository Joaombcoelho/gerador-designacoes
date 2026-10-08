package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.*;

import java.util.List;
import java.time.LocalDate;

import static br.com.geradordesignacoes.model.TipoParticipacao.*;

public class RegrasService {

    public boolean podeDesignar(
            Pessoa pessoa,
            Parte parte,
            List<Pessoa> pessoasJaDesignadas
    ) {

        if (pessoa == null
                || parte == null
                || !pessoa.isAtivo()) {

            return false;
        }

        /*
         * Presidente não pode receber uma segunda
         * designação na mesma reunião.
         *
         * Esta sobrecarga não possui o ControleDesignacoes,
         * portanto o bloqueio é mantido apenas pelo fato
         * de a pessoa já estar na lista de designados.
         */
        if (pessoasJaDesignadas.contains(pessoa)
                && !podeReceberMaisDeUmaDesignacao(pessoa)) {

            return false;
        }

        return podeExercerAlgumaParticipacao(pessoa, parte);
    }


    public boolean podeDesignar(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle
    ) {
        return podeDesignar(
                pessoa,
                parte,
                controle,
                null
        );
    }


    public boolean podeDesignar(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle,
            LocalDate data
    ) {

        if (pessoa == null
                || parte == null
                || controle == null
                || !pessoa.isAtivo()) {

            return false;
        }

        /*
         * Regra específica:
         *
         * A mesma pessoa não pode fazer simultaneamente
         * Discurso - Tesouros e Joias espirituais na
         * mesma reunião.
         */
        if (data != null
                && possuiConflitoTesourosJoias(
                pessoa,
                parte,
                controle,
                data
        )) {
            return false;
        }

        List<Pessoa> pessoasJaDesignadas =
                controle.getPessoasDesignadas();

        if (pessoasJaDesignadas.contains(pessoa)) {

            /*
             * O presidente nunca pode receber
             * uma segunda designação.
             */
            if (controle.ehPresidente(pessoa)) {
                return false;
            }

            /*
             * Anciãos e servos ministeriais podem
             * receber mais de uma designação.
             */
            if (!podeReceberMaisDeUmaDesignacao(pessoa)) {
                return false;
            }
        }

        return podeExercerAlgumaParticipacao(
                pessoa,
                parte
        );
    }


    /**
     * Impede que a mesma pessoa seja designada
     * para Discurso - Tesouros e Joias espirituais
     * na mesma reunião.
     */
    private boolean possuiConflitoTesourosJoias(
            Pessoa pessoa,
            Parte parte,
            ControleDesignacoes controle,
            LocalDate data
    ) {
        if (pessoa == null
                || parte == null
                || controle == null
                || data == null) {

            return false;
        }

        if (!ehParteTesouros(parte)
                && !ehParteJoias(parte)) {

            return false;
        }

        TipoParte tipoConflitante =
                ehParteTesouros(parte)
                        ? TipoParte.JOIAS_ESPIRITUAIS
                        : TipoParte.DISCURSO_TESOUROS;

        return controle.getParticipacoes()
                .stream()
                .anyMatch(
                        participacao ->
                                participacao.pessoa().equals(pessoa)
                                        && participacao.data().equals(data)
                                        && participacao.parte().getTipo()
                                        == tipoConflitante
                );
    }


    private boolean ehParteTesouros(
            Parte parte
    ) {
        if (parte == null) {
            return false;
        }

        String nome =
                parte.getNome();

        return nome != null
                && (
                nome.equalsIgnoreCase(
                        "Discurso - Tesouros"
                )
                        || nome.equalsIgnoreCase(
                        "Discurso – Tesouros"
                )
                        || nome.equalsIgnoreCase(
                        "Discurso - Tesouros da Palavra de Deus"
                )
                        || nome.equalsIgnoreCase(
                        "Discurso – Tesouros da Palavra de Deus"
                )
        );
    }


    private boolean ehParteJoias(
            Parte parte
    ) {
        if (parte == null) {
            return false;
        }

        String nome =
                parte.getNome();

        return nome != null
                && nome.toLowerCase()
                .contains("joias espirituais");
    }


    /**
     * Verifica se a pessoa pode exercer pelo menos
     * uma das participações necessárias da parte.
     */
    private boolean podeExercerAlgumaParticipacao(
            Pessoa pessoa,
            Parte parte
    ) {

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


    /**
     * Verifica se o privilégio da pessoa permite
     * receber mais de uma designação na mesma reunião.
     */
    private boolean podeReceberMaisDeUmaDesignacao(
            Pessoa pessoa
    ) {

        return pessoa.getPrivilegio() == Privilegio.ANCIAO
                || pessoa.getPrivilegio() == Privilegio.SERVO_MINISTERIAL;
    }


    /**
     * Verifica se uma pessoa pode exercer
     * uma participação específica numa parte.
     * Este método é utilizado quando precisamos
     * validar uma participação individual, como
     * durante a edição manual de uma designação.
     */
    public boolean podeExercerParticipacao(
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {

        if (pessoa == null
                || parte == null
                || tipoParticipacao == null
                || !pessoa.isAtivo()) {

            return false;
        }

        if (tipoParticipacao == PRESIDENTE
                && !podePresidirReuniao(pessoa)) {

            return false;
        }

        return parte.pessoaPodeExercerParticipacao(
                pessoa,
                tipoParticipacao
        );
    }


    public boolean podePresidirReuniao(
            Pessoa pessoa
    ) {

        return pessoa != null
                && pessoa.isAtivo()
                && pessoa.getSexo() == Sexo.MASCULINO
                && pessoa.getPrivilegio().atende(
                Privilegio.BATIZADO
        )
                && pessoa.podeSerPresidente();
    }


    public boolean podeFormarDemonstracao(
            Pessoa responsavel,
            Pessoa ajudante,
            List<Pessoa> pessoasJaDesignadas
    ) {

        return responsavel != null
                && ajudante != null
                && responsavel != ajudante
                && responsavel.isAtivo()
                && ajudante.isAtivo()
                && responsavel.getSexo() == ajudante.getSexo()
                && responsavel.podeExercer(
                TipoParticipacao.RESPONSAVEL
        )
                && ajudante.podeExercer(
                TipoParticipacao.AJUDANTE
        );
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