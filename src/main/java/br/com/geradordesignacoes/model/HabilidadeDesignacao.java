package br.com.geradordesignacoes.model;

import java.util.Objects;

/**
 * Habilidade configurável usada como requisito adicional de elegibilidade.
 * Não substitui sexo, privilégio, nível de leitura ou regras específicas da parte.
 */
public record HabilidadeDesignacao(
        Integer id,
        String codigo,
        String nome,
        boolean ativa
) {
    public HabilidadeDesignacao {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("O ID da habilidade deve ser positivo.");
        }
        codigo = Objects.requireNonNull(codigo, "código não pode ser nulo").trim();
        nome = Objects.requireNonNull(nome, "nome não pode ser nulo").trim();
        if (codigo.isEmpty()) {
            throw new IllegalArgumentException("O código da habilidade não pode ser vazio.");
        }
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome da habilidade não pode ser vazio.");
        }
    }
}
