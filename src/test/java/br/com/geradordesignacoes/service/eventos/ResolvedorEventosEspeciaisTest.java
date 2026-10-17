package br.com.geradordesignacoes.service.eventos;

import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.TipoEventoEspecial;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResolvedorEventosEspeciaisTest {

    private final ResolvedorEventosEspeciais resolvedor = new ResolvedorEventosEspeciais();

    @Test
    void semEventosMantemReuniaoNaQuarta() {
        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14), List.of());

        assertEquals(AcaoResolucaoEvento.MANTER_PROGRAMACAO, resultado.acao());
        assertEquals(LocalDate.of(2026, 10, 14), resultado.dataReuniaoOriginal());
        assertFalse(resultado.possuiAlteracaoProposta());
    }

    @Test
    void visitaMoveReuniaoParaTercaEPreservaTemaInformado() {
        EventoEspecial visita = evento(TipoEventoEspecial.VISITA_SUPERINTENDENTE,
                "2026-10-12", "2026-10-12", "Tema da visita");

        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14), List.of(visita));

        assertEquals(AcaoResolucaoEvento.MOVER_REUNIAO_PARA_TERCA, resultado.acao());
        assertEquals(LocalDate.of(2026, 10, 13), resultado.dataReuniaoProposta());
        assertEquals("Tema da visita", resultado.temaDiscurso());
        assertTrue(resultado.mensagem().contains("30 minutos"));
    }

    @Test
    void assembleiaCancelaSemApagarProgramacao() {
        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14),
                List.of(evento(TipoEventoEspecial.ASSEMBLEIA, "2026-10-17", "2026-10-17", null)));

        assertEquals(AcaoResolucaoEvento.CANCELAR_REUNIAO, resultado.acao());
        assertNull(resultado.dataReuniaoProposta());
    }

    @Test
    void congressoCancelaReuniao() {
        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14),
                List.of(evento(TipoEventoEspecial.CONGRESSO, "2026-10-12", "2026-10-12", null)));

        assertEquals(AcaoResolucaoEvento.CANCELAR_REUNIAO, resultado.acao());
    }

    @Test
    void celebracaoNaSegundaAQuartaOuSextaSubstituiReuniao() {
        for (String data : List.of("2026-10-12", "2026-10-14", "2026-10-16")) {
            ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                    LocalDate.of(2026, 10, 14),
                    List.of(evento(TipoEventoEspecial.CELEBRACAO, data, data, null)));
            assertEquals(AcaoResolucaoEvento.SUBSTITUIR_POR_CELEBRACAO, resultado.acao(), data);
        }
    }

    @Test
    void celebracaoNoFimDeSemanaNaoAlteraReuniao() {
        for (String data : List.of("2026-10-17", "2026-10-18")) {
            ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                    LocalDate.of(2026, 10, 14),
                    List.of(evento(TipoEventoEspecial.CELEBRACAO, data, data, null)));
            assertEquals(AcaoResolucaoEvento.MANTER_PROGRAMACAO, resultado.acao(), data);
        }
    }

    @Test
    void cancelamentoGenericoMantemDadosDaProgramacaoEMarcaCancelamento() {
        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14),
                List.of(evento(TipoEventoEspecial.CANCELAMENTO, "2026-10-14", "2026-10-14", null)));

        assertEquals(AcaoResolucaoEvento.CANCELAR_REUNIAO, resultado.acao());
        assertTrue(resultado.mensagem().contains("preservando a programação"));
    }

    @Test
    void visitaECancelamentoComDataCoincidenteResolveParaCancelamento() {
        EventoEspecial visita = evento(TipoEventoEspecial.VISITA_SUPERINTENDENTE,
                "2026-10-14", "2026-10-14", "Tema");
        EventoEspecial cancelamento = evento(TipoEventoEspecial.CANCELAMENTO,
                "2026-10-14", "2026-10-14", null);

        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14), List.of(visita, cancelamento));

        assertEquals(AcaoResolucaoEvento.CANCELAR_REUNIAO, resultado.acao());
        assertFalse(resultado.exigeDecisaoDoUsuario());
    }

    @Test
    void visitaECancelamentoNaMesmaSemanaMasEmDatasDiferentesExigeDecisao() {
        EventoEspecial visita = evento(TipoEventoEspecial.VISITA_SUPERINTENDENTE,
                "2026-10-13", "2026-10-13", "Tema");
        EventoEspecial cancelamento = evento(TipoEventoEspecial.CANCELAMENTO,
                "2026-10-16", "2026-10-16", null);

        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14), List.of(visita, cancelamento));

        assertEquals(AcaoResolucaoEvento.EXIGE_DECISAO, resultado.acao());
        assertTrue(resultado.exigeDecisaoDoUsuario());
        assertTrue(resultado.alternativas().contains(AcaoResolucaoEvento.MOVER_REUNIAO_PARA_TERCA));
        assertTrue(resultado.alternativas().contains(AcaoResolucaoEvento.CANCELAR_REUNIAO));
    }

    @Test
    void combinacaoSemPrecedenciaExigeEscolhaEListaAlternativas() {
        EventoEspecial visita = evento(TipoEventoEspecial.VISITA_SUPERINTENDENTE,
                "2026-10-13", "2026-10-13", "Tema");
        EventoEspecial assembleia = evento(TipoEventoEspecial.ASSEMBLEIA,
                "2026-10-17", "2026-10-17", null);

        ResultadoResolucaoEventos resultado = resolvedor.resolverSemana(
                LocalDate.of(2026, 10, 14), List.of(visita, assembleia));

        assertEquals(AcaoResolucaoEvento.EXIGE_DECISAO, resultado.acao());
        assertFalse(resultado.alternativas().isEmpty());
        assertTrue(resultado.mensagem().contains("Escolha explicitamente"));
    }

    @Test
    void intervaloInclusivoEConsideradoNasDuasPontas() {
        EventoEspecial evento = evento(TipoEventoEspecial.ASSEMBLEIA,
                "2026-10-12", "2026-10-18", null);

        assertTrue(evento.ocorreEm(LocalDate.of(2026, 10, 12)));
        assertTrue(evento.ocorreEm(LocalDate.of(2026, 10, 18)));
        assertFalse(evento.ocorreEm(LocalDate.of(2026, 10, 19)));
    }

    @Test
    void rejeitaIntervaloInvertido() {
        assertThrows(IllegalArgumentException.class, () ->
                evento(TipoEventoEspecial.CONGRESSO, "2026-10-18", "2026-10-12", null));
    }

    @Test
    void rejeitaTemaDeDiscursoEmEventoQueNaoEVisita() {
        assertThrows(IllegalArgumentException.class, () ->
                evento(TipoEventoEspecial.CELEBRACAO, "2026-10-14", "2026-10-14", "Tema indevido"));
    }

    private static EventoEspecial evento(
            TipoEventoEspecial tipo, String inicio, String fim, String tema
    ) {
        return new EventoEspecial(tipo, LocalDate.parse(inicio), LocalDate.parse(fim), tema, null);
    }
}
