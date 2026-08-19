package br.com.geradordesignacoes.model;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class SemanaProgramacao {

    private final YearMonth mes;
    private final int numeroSemana;
    private final List<Parte> partes;

    public SemanaProgramacao(
            YearMonth mes,
            int numeroSemana
    ) {
        validar(mes, numeroSemana);

        this.mes = mes;
        this.numeroSemana = numeroSemana;
        this.partes = new ArrayList<>();
    }

    public SemanaProgramacao(
            YearMonth mes,
            int numeroSemana,
            List<Parte> partes
    ) {
        validar(mes, numeroSemana);

        this.mes = mes;
        this.numeroSemana = numeroSemana;
        this.partes = new ArrayList<>(partes);
    }

    private void validar(
            YearMonth mes,
            int numeroSemana
    ) {
        if (mes == null) {
            throw new IllegalArgumentException("O mês não pode ser nulo.");
        }

        if (numeroSemana < 1 || numeroSemana > 5) {
            throw new IllegalArgumentException(
                    "O número da semana deve estar entre 1 e 5."
            );
        }
    }

    public YearMonth getMes() {
        return mes;
    }

    public int getNumeroSemana() {
        return numeroSemana;
    }

    public List<Parte> getPartes() {
        return new ArrayList<>(partes);
    }

    public void adicionarParte(Parte parte) {
        if (parte == null) {
            throw new IllegalArgumentException("A parte não pode ser nula.");
        }

        if (!partes.contains(parte)) {
            partes.add(parte);
        }
    }

    public void removerParte(Parte parte) {
        partes.remove(parte);
    }

    public boolean estaConfigurada() {
        return !partes.isEmpty();
    }
}