package br.com.geradordesignacoes.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Escala {

    private Integer id;

    private final LocalDate data;

    private final List<Designacao> designacoes;

    private StatusEscala status;

    private LocalDateTime dataGeracao;

    private LocalDateTime dataSalvamento;

    private Pessoa presidente;

    private Pessoa dirigente;

    private Pessoa oracaoInicial;

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

        this.status = StatusEscala.GERADA;
        this.dataGeracao = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public List<Designacao> getDesignacoes() {
        return Collections.unmodifiableList(designacoes);
    }

    /**
     * Uso interno dos DAOs para adicionar designações
     * durante a reconstrução da escala.
     */
    public void adicionarDesignacao(Designacao designacao) {

        designacoes.add(
                Objects.requireNonNull(designacao)
        );
    }

    public void substituirDesignacao(
            int indice,
            Designacao novaDesignacao
    ) {

        if (indice < 0 || indice >= designacoes.size()) {
            throw new IndexOutOfBoundsException(
                    "Índice da designação inválido."
            );
        }

        designacoes.set(
                indice,
                Objects.requireNonNull(
                        novaDesignacao,
                        "A nova designação não pode ser nula."
                )
        );
    }

    public StatusEscala getStatus() {
        return status;
    }

    public void setStatus(StatusEscala status) {
        this.status = Objects.requireNonNull(status);
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(LocalDateTime dataGeracao) {
        this.dataGeracao = Objects.requireNonNull(dataGeracao);
    }

    public LocalDateTime getDataSalvamento() {
        return dataSalvamento;
    }

    public void setDataSalvamento(LocalDateTime dataSalvamento) {
        this.dataSalvamento = dataSalvamento;
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
                "id=" + id +
                ", data=" + data +
                ", designacoes=" + designacoes.size() +
                ", status=" + status +
                '}';
    }

    public Pessoa getPresidente() {
        return presidente;
    }

    public void setPresidente(Pessoa presidente) {
        this.presidente = presidente;
    }


    public Pessoa getDirigente() {
        return dirigente;
    }

    public void setDirigente(Pessoa dirigente) {
        this.dirigente = dirigente;
    }


    public Pessoa getOracaoInicial() {
        return oracaoInicial;
    }

    public void setOracaoInicial(Pessoa oracaoInicial) {
        this.oracaoInicial = oracaoInicial;
    }
}