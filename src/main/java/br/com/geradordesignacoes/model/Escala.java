package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Escala {

    private final LocalDate data;

    private final List<Designacao> designacoes;

    private Integer id;

    private final LocalDate data;

    private final List<Designacao> designacoes;

    private StatusEscala status;

    private LocalDateTime dataGeracao;

    private LocalDateTime dataSalvamento;

    public Escala(
            LocalDate data,
            List<Designacao> designacoes
    ) {

        this.data = Objects.requireNonNull(
                data,
                "A data da escala não pode ser nula."
        );

        this.designacoes = new ArrayList<>(
                Objects.requireNonNull(
                        designacoes,
                        "A lista de designações não pode ser nula."
                )
        );
    }

    public LocalDate getData() {
        return data;
    }

    public List<Designacao> getDesignacoes() {
        return Collections.unmodifiableList(designacoes);
    }

    public int getQuantidadeDesignacoes() {
        return designacoes.size();
    }

    public boolean estaVazia() {
        return designacoes.isEmpty();
    }

    @Override
    public String toString() {
        return "Escala{" +
                "data=" + data +
                ", designacoes=" + designacoes.size() +
                '}';
    }
}