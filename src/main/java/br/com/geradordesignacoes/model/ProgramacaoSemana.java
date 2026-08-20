package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ProgramacaoSemana(Integer id, LocalDate data, List<ProgramacaoParte> partes) {

    public ProgramacaoSemana(
            Integer id,
            LocalDate data,
            List<ProgramacaoParte> partes
    ) {

        this.id = id;

        this.data =
                Objects.requireNonNull(data);

        this.partes =
                new ArrayList<>(
                        Objects.requireNonNull(partes)
                );
    }


    public ProgramacaoSemana(
            LocalDate data
    ) {

        this(
                null,
                data,
                new ArrayList<>()
        );
    }


    @Override
    public List<ProgramacaoParte> partes() {

        return Collections.unmodifiableList(
                partes
        );
    }


    public void adicionarParte(
            ProgramacaoParte programacaoParte
    ) {

        partes.add(
                Objects.requireNonNull(
                        programacaoParte
                )
        );
    }


    public void removerParte(
            ProgramacaoParte programacaoParte
    ) {

        partes.remove(
                programacaoParte
        );
    }

    public void definirPartes(
            List<ProgramacaoParte> partes
    ) {

        Objects.requireNonNull(
                partes,
                "A lista de partes não pode ser nula."
        );

        this.partes.clear();

        this.partes.addAll(
                partes
        );
    }

    public int getQuantidadePartesVariaveis() {

        return (int) partes.stream()
                .filter(programacaoParte ->
                        programacaoParte
                                .getParte()
                                .getTipoVariacao()
                                == TipoVariacaoParte.VARIAVEL
                )
                .count();
    }


    public boolean possuiQuantidadeValidaDePartesVariaveis() {

        int quantidade =
                getQuantidadePartesVariaveis();

        return quantidade >= 3
                && quantidade <= 6;
    }

    public List<ProgramacaoParte> getPartesVariaveis() {

        return partes.stream()
                .filter(programacaoParte ->
                        programacaoParte
                                .getParte()
                                .getTipoVariacao()
                                == TipoVariacaoParte.VARIAVEL
                )
                .toList();
    }

    @Override
    public String toString() {

        return "ProgramacaoSemana{" +
                "id=" + id +
                ", data=" + data +
                ", partes=" + partes.size() +
                '}';
    }
}