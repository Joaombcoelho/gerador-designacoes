package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

public record Designacao(
        Integer id,
        LocalDate data,
        Parte parte,
        Pessoa responsavel,
        Pessoa ajudante
) {

    public Designacao(
            LocalDate data,
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante
    ) {

        this(
                null,
                data,
                parte,
                responsavel,
                ajudante
        );
    }


    public Designacao(
            Integer id,
            LocalDate data,
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante
    ) {

        this.id = id;

        this.data = Objects.requireNonNull(
                data,
                "A data da designação não pode ser nula."
        );

        this.parte = Objects.requireNonNull(
                parte,
                "A parte não pode ser nula."
        );

        if (responsavel == null && ajudante == null) {
            throw new IllegalArgumentException(
                    "A designação deve possuir um responsável ou um ajudante."
            );
        }

        this.responsavel = responsavel;

        if (parte.getExigeAjudante()
                && ajudante == null
                && parte.getTipo() != TipoParte.DIRIGENTE_ESTUDO) {

            throw new IllegalArgumentException(
                    "Esta parte exige um ajudante."
            );
        }

        this.ajudante = ajudante;
    }


    public Pessoa getParticipante(TipoParticipacao tipoParticipacao) {

        Objects.requireNonNull(
                tipoParticipacao,
                "O tipo de participação não pode ser nulo."
        );


        return switch (tipoParticipacao) {

            case RESPONSAVEL -> responsavel;

            case AJUDANTE -> ajudante;

            default -> throw new IllegalArgumentException(
                    "Tipo de participação não encontrado na designação: "
                            + tipoParticipacao
            );
        };
    }


    @Override
    public String toString() {

        StringBuilder texto = new StringBuilder();

        texto.append("Data: ")
                .append(data)
                .append("\n");

        texto.append("Parte: ")
                .append(parte.getNome())
                .append("\n");

        if (ajudante != null) {

            texto.append("Ajudante: ")
                    .append(ajudante.getNome())
                    .append("\n");
        }

        if (responsavel != null) {
            texto.append("Responsável: ")
                    .append(responsavel.getNome())
                    .append("\n");
        }


        return texto.toString();
    }
}