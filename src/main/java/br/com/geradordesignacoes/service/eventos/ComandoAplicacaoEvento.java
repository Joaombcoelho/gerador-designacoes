package br.com.geradordesignacoes.service.eventos;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Solicitação explícita de aplicação. A chave de idempotência identifica
 * uma confirmação única; uma nova confirmação deve usar outra chave.
 */
public record ComandoAplicacaoEvento(
        LocalDate dataReferencia,
        List<Long> idsEventos,
        AcaoResolucaoEvento acaoConfirmada,
        String chaveIdempotencia,
        boolean confirmacaoExplicita,
        boolean confirmacaoRetroativa
) {
    public ComandoAplicacaoEvento {
        Objects.requireNonNull(dataReferencia, "A data de referência é obrigatória.");
        Objects.requireNonNull(idsEventos, "Os eventos selecionados são obrigatórios.");
        idsEventos = List.copyOf(idsEventos);
        Objects.requireNonNull(acaoConfirmada, "A ação confirmada é obrigatória.");
        chaveIdempotencia = chaveIdempotencia == null ? "" : chaveIdempotencia.trim();

        if (idsEventos.isEmpty() || idsEventos.stream().anyMatch(id -> id == null || id <= 0)
                || idsEventos.stream().distinct().count() != idsEventos.size()) {
            throw new IllegalArgumentException("Informe IDs de eventos positivos, únicos e não vazios.");
        }
        if (chaveIdempotencia.isBlank() || chaveIdempotencia.length() > 200) {
            throw new IllegalArgumentException("A chave de idempotência é obrigatória e deve ter até 200 caracteres.");
        }
    }
}