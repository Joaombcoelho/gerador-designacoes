package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

public record ParticipacaoDesignacao(Integer id, LocalDate data, Pessoa pessoa, Parte parte,
                                     TipoParticipacao tipoParticipacao) {

    public ParticipacaoDesignacao(
            Integer id,
            LocalDate data,
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {

        this.id = id;

        this.data = Objects.requireNonNull(
                data,
                "A data não pode ser nula."
        );

        this.pessoa = Objects.requireNonNull(
                pessoa,
                "A pessoa não pode ser nula."
        );

        this.parte = Objects.requireNonNull(
                parte,
                "A parte não pode ser nula."
        );

        this.tipoParticipacao = Objects.requireNonNull(
                tipoParticipacao,
                "O tipo de participação não pode ser nulo."
        );
    }


    public ParticipacaoDesignacao(
            LocalDate data,
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {

        this(
                null,
                data,
                pessoa,
                parte,
                tipoParticipacao
        );
    }


    @Override
    public String toString() {

        return """
                Data: %s
                Pessoa: %s
                Parte: %s
                Participação: %s
                """.formatted(
                data,
                pessoa,
                parte,
                tipoParticipacao
        );
    }


    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }

        if (!(o instanceof ParticipacaoDesignacao outra)) {
            return false;
        }

        if (id == null || outra.id == null) {
            return false;
        }

        return id.equals(outra.id);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}