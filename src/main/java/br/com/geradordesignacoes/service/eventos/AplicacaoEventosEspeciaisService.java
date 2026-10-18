package br.com.geradordesignacoes.service.eventos;

import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.EventoEspecial;
import br.com.geradordesignacoes.model.TipoEventoEspecial;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public class AplicacaoEventosEspeciaisService {
    private final ResolvedorEventosEspeciais resolvedor;

    public AplicacaoEventosEspeciaisService() {
        this(new ResolvedorEventosEspeciais());
    }

    AplicacaoEventosEspeciaisService(ResolvedorEventosEspeciais resolvedor) {
        this.resolvedor = Objects.requireNonNull(resolvedor);
    }

    /**
     * Aplica somente cancelamentos/substituições representados por estado explícito.
     * A visita que exige mover a programação permanece bloqueada nesta etapa.
     */
    public ResultadoAplicacaoEvento aplicar(ComandoAplicacaoEvento comando) {
        Objects.requireNonNull(comando, "O comando de aplicação é obrigatório.");
        if (!comando.confirmacaoExplicita()) {
            throw new IllegalArgumentException("A aplicação exige confirmação explícita.");
        }
        if (comando.acaoConfirmada() != AcaoResolucaoEvento.CANCELAR_REUNIAO
                && comando.acaoConfirmada() != AcaoResolucaoEvento.SUBSTITUIR_POR_CELEBRACAO) {
            throw new IllegalArgumentException(
                    "Esta etapa só aplica cancelamentos e substituições por celebração; mover a reunião ainda não está habilitado.");
        }

        LocalDate dataReuniao = comando.dataReferencia()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .plusDays(2);
        if (dataReuniao.isBefore(LocalDate.now()) && !comando.confirmacaoRetroativa()) {
            throw new IllegalArgumentException(
                    "Aplicação retroativa exige confirmação explícita adicional.");
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String fingerprint = fingerprint(comando, dataReuniao);
                ResultadoAplicacaoEvento existente =
                        buscarPorChave(connection, comando.chaveIdempotencia());
                if (existente != null) {
                    if (!fingerprint.equals(buscarFingerprint(connection, existente.idAplicacao()))) {
                        throw new IllegalArgumentException(
                                "A chave de idempotência já foi usada para outra solicitação.");
                    }
                    connection.commit();
                    return new ResultadoAplicacaoEvento(
                            ResultadoAplicacaoEvento.Situacao.JA_APLICADA,
                            existente.idAplicacao(), existente.dataReuniao(), existente.estado());
                }

                List<EventoEspecial> eventos = carregarEventos(connection, comando.idsEventos());
                ResultadoResolucaoEventos resolucao =
                        resolvedor.resolverSemana(comando.dataReferencia(), eventos);
                validarDecisao(resolucao, comando.acaoConfirmada());

                // Não criar semana nem sincronizar partes: a programação deve existir previamente.
                try (PreparedStatement statement = connection.prepareStatement(
                        "SELECT 1 FROM programacao_semana WHERE data = ?")) {
                    statement.setDate(1, Date.valueOf(dataReuniao));
                    try (ResultSet rs = statement.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException(
                                    "A programação da reunião não existe. Crie-a explicitamente antes de aplicar o evento.");
                        }
                    }
                }

                long idAplicacao = registrarAplicacao(
                        connection, comando, dataReuniao, fingerprint);
                registrarEventosAplicacao(connection, idAplicacao, comando.idsEventos());

                EstadoAplicacaoEvento estado = comando.acaoConfirmada()
                        == AcaoResolucaoEvento.CANCELAR_REUNIAO
                        ? EstadoAplicacaoEvento.CANCELADA
                        : EstadoAplicacaoEvento.SUBSTITUIDA_POR_CELEBRACAO;
                gravarEstadoAtual(connection, idAplicacao, dataReuniao, estado);

                connection.commit();
                return new ResultadoAplicacaoEvento(
                        ResultadoAplicacaoEvento.Situacao.APLICADA,
                        idAplicacao, dataReuniao, estado);
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Falha ao aplicar evento especial; a transação foi revertida.", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Falha na transação de aplicação do evento especial.", e);
        }
    }

    private static void validarDecisao(
            ResultadoResolucaoEventos resolucao,
            AcaoResolucaoEvento confirmada
    ) {
        if (resolucao.acao() == AcaoResolucaoEvento.EXIGE_DECISAO) {
            if (!resolucao.alternativas().contains(confirmada)) {
                throw new IllegalArgumentException(
                        "A ação confirmada não é uma das alternativas válidas para o conflito.");
            }
            return;
        }
        if (resolucao.acao() != confirmada) {
            throw new IllegalArgumentException(
                    "A resolução atual diverge da ação confirmada na prévia. Gere uma nova prévia.");
        }
    }

    private static List<EventoEspecial> carregarEventos(
            Connection connection,
            List<Long> ids
    ) throws SQLException {
        String placeholders = String.join(",", java.util.Collections.nCopies(ids.size(), "?"));
        String sql = """
                SELECT id, tipo, data_inicio, data_fim, tema_discurso, descricao
                FROM evento_especial
                WHERE id IN (%s)
                ORDER BY data_inicio, tipo, id
                """.formatted(placeholders);
        List<EventoEspecial> eventos = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                statement.setLong(i + 1, ids.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    eventos.add(new EventoEspecial(
                            TipoEventoEspecial.valueOf(rs.getString("tipo")),
                            LocalDate.parse(rs.getString("data_inicio")),
                            LocalDate.parse(rs.getString("data_fim")),
                            rs.getString("tema_discurso"),
                            rs.getString("descricao")));
                }
            }
        }
        if (eventos.size() != ids.size()) {
            throw new IllegalArgumentException(
                    "Um ou mais eventos selecionados não existem mais. Gere uma nova prévia.");
        }
        return List.copyOf(eventos);
    }

    private static ResultadoAplicacaoEvento buscarPorChave(
            Connection connection, String chave
    ) throws SQLException {
        String sql = """
                SELECT id, data_reuniao, estado
                FROM evento_especial_aplicacao
                WHERE chave_idempotencia = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, chave);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new ResultadoAplicacaoEvento(
                        ResultadoAplicacaoEvento.Situacao.JA_APLICADA,
                        rs.getLong("id"),
                        LocalDate.parse(rs.getString("data_reuniao")),
                        EstadoAplicacaoEvento.valueOf(rs.getString("estado")));
            }
        }
    }

    private static String buscarFingerprint(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT fingerprint FROM evento_especial_aplicacao WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Registro de aplicação inconsistente.");
                }
                return rs.getString(1);
            }
        }
    }

    private static long registrarAplicacao(
            Connection connection,
            ComandoAplicacaoEvento comando,
            LocalDate dataReuniao,
            String fingerprint
    ) throws SQLException {
        String sql = """
                INSERT INTO evento_especial_aplicacao
                    (chave_idempotencia, data_reuniao, acao, fingerprint, instante_aplicacao, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        EstadoAplicacaoEvento estado = comando.acaoConfirmada()
                == AcaoResolucaoEvento.CANCELAR_REUNIAO
                ? EstadoAplicacaoEvento.CANCELADA
                : EstadoAplicacaoEvento.SUBSTITUIDA_POR_CELEBRACAO;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, comando.chaveIdempotencia());
            statement.setString(2, dataReuniao.toString());
            statement.setString(3, comando.acaoConfirmada().name());
            statement.setString(4, fingerprint);
            statement.setString(5, LocalDateTime.now().toString());
            statement.setString(6, estado.name());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("O registro de aplicação não foi criado.");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Não foi possível obter o ID da aplicação.");
                }
                return keys.getLong(1);
            }
        }
    }

    private static void registrarEventosAplicacao(
            Connection connection, long idAplicacao, List<Long> ids
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO evento_especial_aplicacao_evento (aplicacao_id, evento_id)
                VALUES (?, ?)
                """)) {
            for (Long id : ids) {
                statement.setLong(1, idAplicacao);
                statement.setLong(2, id);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void gravarEstadoAtual(
            Connection connection,
            long idAplicacao,
            LocalDate dataReuniao,
            EstadoAplicacaoEvento estado
    ) throws SQLException {
        String sql = """
                INSERT INTO estado_reuniao_evento (data_reuniao, estado, aplicacao_id)
                VALUES (?, ?, ?)
                ON CONFLICT(data_reuniao) DO UPDATE SET
                    estado = excluded.estado,
                    aplicacao_id = excluded.aplicacao_id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, dataReuniao.toString());
            statement.setString(2, estado.name());
            statement.setLong(3, idAplicacao);
            statement.executeUpdate();
        }
    }

    private static String fingerprint(
            ComandoAplicacaoEvento comando,
            LocalDate dataReuniao
    ) {
        String canonical = dataReuniao + "|" + comando.acaoConfirmada().name() + "|"
                + comando.idsEventos().stream().sorted().map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível.", e);
        }
    }
}