package br.com.geradordesignacoes;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.TipoEventoEspecial;
import br.com.geradordesignacoes.service.eventos.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AplicacaoEventosEspeciaisServiceTest {
    private TestDatabase banco;
    private AplicacaoEventosEspeciaisService service;
    private long idEvento;
    private final LocalDate quarta = LocalDate.now()
            .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
            .plusDays(2).plusWeeks(2);

    @BeforeEach
    void preparar() throws Exception {
        banco = TestDatabase.create();
        DatabaseInitializer.initialize();
        service = new AplicacaoEventosEspeciaisService();
        try (Connection c = ConnectionFactory.getConnection()) {
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO programacao_semana (data) VALUES (?)")) {
                p.setDate(1, Date.valueOf(quarta));
                p.executeUpdate();
            }
            EventoEspecial evento = new EventoEspecial(
                    TipoEventoEspecial.CANCELAMENTO, quarta, quarta, null, "Teste");
            try (PreparedStatement p = c.prepareStatement("""
                    INSERT INTO evento_especial (tipo, data_inicio, data_fim, tema_discurso, descricao)
                    VALUES (?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1, evento.tipo().name());
                p.setString(2, evento.dataInicio().toString());
                p.setString(3, evento.dataFim().toString());
                p.setString(4, null);
                p.setString(5, evento.descricao());
                p.executeUpdate();
                try (ResultSet rs = p.getGeneratedKeys()) {
                    assertTrue(rs.next());
                    idEvento = rs.getLong(1);
                }
            }
        }
    }

    @AfterEach
    void fechar() throws Exception {
        if (banco != null) banco.close();
    }

    @Test
    void aplicaCancelamentoSemApagarProgramacaoERepeticaoEIdempotente() {
        ComandoAplicacaoEvento comando = comando(UUID.randomUUID().toString(), true);
        ResultadoAplicacaoEvento primeiro = service.aplicar(comando);
        ResultadoAplicacaoEvento repetido = service.aplicar(comando);

        assertEquals(ResultadoAplicacaoEvento.Situacao.APLICADA, primeiro.situacao());
        assertEquals(ResultadoAplicacaoEvento.Situacao.JA_APLICADA, repetido.situacao());
        assertEquals(primeiro.idAplicacao(), repetido.idAplicacao());
        assertEquals(EstadoAplicacaoEvento.CANCELADA, repetido.estado());
        assertEquals(1, contar("programacao_semana"));
        assertEquals(1, contar("evento_especial_aplicacao"));
        assertEquals(1, contar("estado_reuniao_evento"));
    }

    @Test
    void exigeConfirmacaoAntesDeGravar() {
        assertThrows(IllegalArgumentException.class,
                () -> service.aplicar(comando(UUID.randomUUID().toString(), false)));
        assertEquals(0, contar("evento_especial_aplicacao"));
        assertEquals(0, contar("estado_reuniao_evento"));
    }

    @Test
    void excluirCadastroDoEventoNaoReverteAplicacaoConfirmada() throws Exception {
        service.aplicar(comando(UUID.randomUUID().toString(), true));
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement p = c.prepareStatement("DELETE FROM evento_especial WHERE id = ?")) {
            p.setLong(1, idEvento);
            assertEquals(1, p.executeUpdate());
        }
        assertEquals(1, contar("evento_especial_aplicacao"));
        assertEquals(1, contar("evento_especial_aplicacao_evento"));
        assertEquals(1, contar("estado_reuniao_evento"));
        assertEquals(1, contar("programacao_semana"));
    }

    @Test
    void reverteRegistroDeAplicacaoSeGravacaoDoEstadoFalhar() throws Exception {
        try (Connection c = ConnectionFactory.getConnection();
             Statement s = c.createStatement()) {
            s.execute("""
                    CREATE TRIGGER falha_estado_reuniao
                    BEFORE INSERT ON estado_reuniao_evento
                    BEGIN SELECT RAISE(ABORT, 'falha simulada'); END
                    """);
        }
        assertThrows(RuntimeException.class,
                () -> service.aplicar(comando(UUID.randomUUID().toString(), true)));
        assertEquals(0, contar("evento_especial_aplicacao"));
        assertEquals(0, contar("evento_especial_aplicacao_evento"));
        assertEquals(0, contar("estado_reuniao_evento"));
        assertEquals(1, contar("programacao_semana"));
    }

    private ComandoAplicacaoEvento comando(String chave, boolean confirmado) {
        return new ComandoAplicacaoEvento(
                quarta, List.of(idEvento), AcaoResolucaoEvento.CANCELAR_REUNIAO,
                chave, confirmado, true);
    }

    private int contar(String tabela) {
        try (Connection c = ConnectionFactory.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            assertTrue(rs.next());
            return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}