package br.com.geradordesignacoes.service.eventos;

import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.TipoEventoEspecial;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolve as regras de eventos em memória, sem acessar DAOs, serviços de
 * programação, escalas, histórico ou ajustes manuais.
 *
 * A data recebida identifica a semana a analisar. A reunião habitual é
 * considerada na quarta-feira da semana civil (segunda a domingo).
 */
public class ResolvedorEventosEspeciais {

    public ResultadoResolucaoEventos resolverSemana(
            LocalDate dataReferencia,
            List<EventoEspecial> eventos
    ) {
        Objects.requireNonNull(dataReferencia, "A data de referência é obrigatória.");
        Objects.requireNonNull(eventos, "A lista de eventos é obrigatória.");
        if (eventos.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A lista não pode conter eventos nulos.");
        }

        LocalDate inicioSemana = dataReferencia.with(
                TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate fimSemana = inicioSemana.plusDays(6);
        LocalDate quartaFeira = inicioSemana.plusDays(2);

        List<EventoEspecial> considerados = eventos.stream()
                .filter(evento -> aplicaNaSemana(evento, inicioSemana, fimSemana))
                .sorted(Comparator.comparing(EventoEspecial::dataInicio)
                        .thenComparing(evento -> evento.tipo().name()))
                .toList();

        if (considerados.isEmpty()) {
            return resultado(quartaFeira, AcaoResolucaoEvento.MANTER_PROGRAMACAO,
                    null, null, List.of(), List.of(),
                    "Nenhum evento aplicável altera a reunião nesta semana.");
        }

        if (possuiConflitoNaoResolvido(considerados)) {
            List<AcaoResolucaoEvento> alternativas = considerados.stream()
                    .map(ResolvedorEventosEspeciais::acaoParaTipo)
                    .filter(acao -> acao != AcaoResolucaoEvento.MANTER_PROGRAMACAO)
                    .distinct()
                    .toList();

            // A decisão manual será feita pela interface em etapa posterior.
            // Não há escolha padrão nem aplicação automática neste serviço.
            return resultado(quartaFeira, AcaoResolucaoEvento.EXIGE_DECISAO,
                    null, null, considerados, alternativas,
                    "Há eventos incompatíveis nesta semana. Escolha explicitamente como resolver o conflito.");
        }

        if (considerados.stream().anyMatch(evento ->
                evento.tipo() == TipoEventoEspecial.CANCELAMENTO
                        || evento.tipo() == TipoEventoEspecial.ASSEMBLEIA
                        || evento.tipo() == TipoEventoEspecial.CONGRESSO)) {
            return resultado(quartaFeira, AcaoResolucaoEvento.CANCELAR_REUNIAO,
                    null, null, considerados, List.of(),
                    "A reunião deve ser marcada como cancelada, preservando a programação cadastrada.");
        }

        EventoEspecial visita = considerados.stream()
                .filter(evento -> evento.tipo() == TipoEventoEspecial.VISITA_SUPERINTENDENTE)
                .findFirst()
                .orElse(null);
        if (visita != null) {
            return resultado(quartaFeira, AcaoResolucaoEvento.MOVER_REUNIAO_PARA_TERCA,
                    quartaFeira.minusDays(1), visita.temaDiscurso(), considerados, List.of(),
                    "A reunião passa para terça-feira; o Estudo Bíblico de Congregação será substituído por discurso de 30 minutos.");
        }

        return resultado(quartaFeira, AcaoResolucaoEvento.SUBSTITUIR_POR_CELEBRACAO,
                null, null, considerados, List.of(),
                "A celebração substitui a reunião habitual desta semana.");
    }

    private static boolean aplicaNaSemana(
            EventoEspecial evento,
            LocalDate inicioSemana,
            LocalDate fimSemana
    ) {
        LocalDate inicioIntersecao = evento.dataInicio().isAfter(inicioSemana)
                ? evento.dataInicio() : inicioSemana;
        LocalDate fimIntersecao = evento.dataFim().isBefore(fimSemana)
                ? evento.dataFim() : fimSemana;
        if (fimIntersecao.isBefore(inicioIntersecao)) {
            return false;
        }

        if (evento.tipo() != TipoEventoEspecial.CELEBRACAO) {
            return true;
        }

        // A celebração só substitui a reunião se ocorrer de segunda a sexta.
        for (LocalDate data = inicioIntersecao;
             !data.isAfter(fimIntersecao);
             data = data.plusDays(1)) {
            DayOfWeek dia = data.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                return true;
            }
        }
        return false;
    }

    private static boolean possuiConflitoNaoResolvido(List<EventoEspecial> eventos) {
        for (int i = 0; i < eventos.size(); i++) {
            EventoEspecial primeiro = eventos.get(i);
            for (int j = i + 1; j < eventos.size(); j++) {
                EventoEspecial segundo = eventos.get(j);
                if (primeiro.tipo() == segundo.tipo()) {
                    if (primeiro.tipo() == TipoEventoEspecial.VISITA_SUPERINTENDENTE
                            && !Objects.equals(primeiro.temaDiscurso(), segundo.temaDiscurso())) {
                        return true;
                    }
                    continue;
                }

                if (visitaComCancelamentoNaMesmaData(primeiro, segundo)) {
                    // Regra acordada: o cancelamento prevalece no dia coincidente.
                    continue;
                }

                // Tipos diferentes na mesma semana não recebem precedência implícita,
                // mesmo quando um resultado aparente seria igual.
                return true;
            }
        }
        return false;
    }

    private static boolean visitaComCancelamentoNaMesmaData(
            EventoEspecial primeiro,
            EventoEspecial segundo
    ) {
        EventoEspecial visita;
        EventoEspecial cancelamento;
        if (primeiro.tipo() == TipoEventoEspecial.VISITA_SUPERINTENDENTE
                && segundo.tipo() == TipoEventoEspecial.CANCELAMENTO) {
            visita = primeiro;
            cancelamento = segundo;
        } else if (segundo.tipo() == TipoEventoEspecial.VISITA_SUPERINTENDENTE
                && primeiro.tipo() == TipoEventoEspecial.CANCELAMENTO) {
            visita = segundo;
            cancelamento = primeiro;
        } else {
            return false;
        }

        LocalDate inicioSobreposicao = visita.dataInicio().isAfter(cancelamento.dataInicio())
                ? visita.dataInicio() : cancelamento.dataInicio();
        LocalDate fimSobreposicao = visita.dataFim().isBefore(cancelamento.dataFim())
                ? visita.dataFim() : cancelamento.dataFim();
        return !fimSobreposicao.isBefore(inicioSobreposicao);
    }

    private static AcaoResolucaoEvento acaoParaTipo(TipoEventoEspecial tipo) {
        return switch (tipo) {
            case VISITA_SUPERINTENDENTE -> AcaoResolucaoEvento.MOVER_REUNIAO_PARA_TERCA;
            case ASSEMBLEIA, CONGRESSO, CANCELAMENTO -> AcaoResolucaoEvento.CANCELAR_REUNIAO;
            case CELEBRACAO -> AcaoResolucaoEvento.SUBSTITUIR_POR_CELEBRACAO;
        };
    }

    private static ResultadoResolucaoEventos resultado(
            LocalDate dataOriginal,
            AcaoResolucaoEvento acao,
            LocalDate dataProposta,
            String tema,
            List<EventoEspecial> eventos,
            List<AcaoResolucaoEvento> alternativas,
            String mensagem
    ) {
        return new ResultadoResolucaoEventos(
                dataOriginal, acao, dataProposta, tema, eventos, alternativas, mensagem);
    }
}
