package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.SalaAuxiliar;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.TipoParte;

import java.util.Objects;

public class RegrasSalasAuxiliaresService {

    private final RegrasService regrasService = new RegrasService();

    public boolean parteElegivel(Parte parte) {
        if (parte == null
                || parte.getTipo() == null
                || parte.getSecao() == null) {
            return false;
        }

        if (parte.getTipo() == TipoParte.LEITURA) {
            return true;
        }

        return parte.getSecao() == SecaoParte.MINISTERIO
                && parte.getTipo() != TipoParte.O_QUE_VOCE_DIRIA;
    }

    public boolean podeParticipar(
            Pessoa pessoa,
            Parte parte,
            SalaAuxiliar sala
    ) {
        if (pessoa == null
                || parte == null
                || sala == null
                || !pessoa.isAtivo()
                || !parteElegivel(parte)
                || !temPermissaoParaSala(pessoa, sala)) {
            return false;
        }

        if (parte.getParticipacoesNecessarias() == null
                || parte.getParticipacoesNecessarias().isEmpty()) {
            return false;
        }

        return parte.getParticipacoesNecessarias()
                .stream()
                .anyMatch(tipo ->
                        podeExercerParticipacao(pessoa, parte, tipo)
                );
    }
    private boolean temPermissaoParaSala(
            Pessoa pessoa,
            SalaAuxiliar sala
    ) {
        return switch (sala) {
            case B -> pessoa.podeParticiparSalaB();
            case C -> pessoa.podeParticiparSalaC();
        };
    }

    private boolean podeExercerParticipacao(
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipo
    ) {
        if (tipo == TipoParticipacao.RESPONSAVEL
                && !privilegioPermiteSerResponsavel(pessoa)) {
            return false;
        }

        return regrasService.podeExercerParticipacao(
                pessoa,
                parte,
                tipo
        );
    }

    private boolean privilegioPermiteSerResponsavel(Pessoa pessoa) {
        return Objects.equals(
                pessoa.getPrivilegio(),
                Privilegio.ANCIAO
        ) || Objects.equals(
                pessoa.getPrivilegio(),
                Privilegio.SERVO_MINISTERIAL
        );
    }
}