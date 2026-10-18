package br.com.geradordesignacoes.model;

import java.util.Objects;

/**
 * Evento especial armazenado no banco, identificado por um ID estável.
 * A identidade do registro fica separada dos dados de domínio do evento.
 */
public record EventoEspecialRegistrado(long id, EventoEspecial evento) {
    public EventoEspecialRegistrado {
        if (id <= 0) {
            throw new IllegalArgumentException("O ID do evento deve ser positivo.");
        }
        Objects.requireNonNull(evento, "O evento é obrigatório.");
    }
}
