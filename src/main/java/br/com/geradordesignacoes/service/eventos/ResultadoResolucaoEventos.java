package br.com.geradordesignacoes.service.eventos;

import br.com.geradordesignacoes.model.EventoEspecial;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Resultado descritivo e imutável da resolução de eventos para uma semana.
 * É apenas uma proposta lógica; não representa aplicação nem persistência.
 */
public record ResultadoResolucaoEventos(
        LocalDate dataReuniaoOriginal,
        AcaoResolucaoEvento acao,
        LocalDate dataReuniaoProposta,
        String temaDiscurso,
        List<EventoEspecial> eventosConsiderados,
        List<AcaoResolucaoEvento> alternativas,
        String mensagem
) {
    public ResultadoResolucaoEventos {
        Objects.requireNonNull(dataReuniaoOriginal, "A data da reunião é obrigatória.");
        Objects.requireNonNull(acao, "A ação de resolução é obrigatória.");
        eventosConsiderados = List.copyOf(
                Objects.requireNonNull(eventosConsiderados, "A lista de eventos é obrigatória."));
        alternativas = List.copyOf(
                Objects.requireNonNull(alternativas, "A lista de alternativas é obrigatória."));
        mensagem = mensagem == null ? "" : mensagem;

        if (acao != AcaoResolucaoEvento.EXIGE_DECISAO && !alternativas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Alternativas só podem ser fornecidas quando é necessária uma decisão.");
        }
        if (acao == AcaoResolucaoEvento.EXIGE_DECISAO && alternativas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Um conflito deve apresentar pelo menos uma alternativa.");
        }
    }

    public boolean exigeDecisaoDoUsuario() {
        return acao == AcaoResolucaoEvento.EXIGE_DECISAO;
    }

    public boolean possuiAlteracaoProposta() {
        return acao == AcaoResolucaoEvento.MOVER_REUNIAO_PARA_TERCA
                || acao == AcaoResolucaoEvento.CANCELAR_REUNIAO
                || acao == AcaoResolucaoEvento.SUBSTITUIR_POR_CELEBRACAO;
    }
}
