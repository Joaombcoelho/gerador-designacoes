package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.Objects;

public class Designacao {

    private final LocalDate data;
    private final Parte parte;
    private final Pessoa responsavel;
    private final Pessoa ajudante;


    public Designacao(
            LocalDate data,
            Parte parte,
            Pessoa responsavel,
            Pessoa ajudante
    ) {

        this.data = Objects.requireNonNull(
                data,
                "A data da designação não pode ser nula."
        );

        this.parte = Objects.requireNonNull(
                parte,
                "A parte não pode ser nula."
        );

        this.responsavel = Objects.requireNonNull(
                responsavel,
                "O responsável não pode ser nulo."
        );


        if (parte.getExigeAjudante()
                && ajudante == null) {

            throw new IllegalArgumentException(
                    "Esta parte exige um ajudante."
            );
        }

        this.ajudante = ajudante;
    }


    public LocalDate getData() {
        return data;
    }


    public Parte getParte() {
        return parte;
    }


    public Pessoa getResponsavel() {
        return responsavel;
    }


    public Pessoa getAjudante() {
        return ajudante;
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

        texto.append("Responsável: ")
                .append(responsavel.getNome())
                .append("\n");


        if (ajudante != null) {

            texto.append("Ajudante: ")
                    .append(ajudante.getNome())
                    .append("\n");
        }


        return texto.toString();
    }
}