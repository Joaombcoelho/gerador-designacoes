package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

public class ParticipacaoDesignacao {

    private final LocalDate data;
    private final Pessoa pessoa;
    private final Parte parte;
    private final TipoParticipacao tipoParticipacao;


    public ParticipacaoDesignacao(
            LocalDate data,
            Pessoa pessoa,
            Parte parte,
            TipoParticipacao tipoParticipacao
    ) {

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


    public LocalDate getData() {
        return data;
    }


    public Pessoa getPessoa() {
        return pessoa;
    }


    public Parte getParte() {
        return parte;
    }


    public TipoParticipacao getTipoParticipacao() {
        return tipoParticipacao;
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
}