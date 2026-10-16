package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.ResultadoGeracaoEscala;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Coordena o salvamento de escalas geradas e, quando aplicável,
 * do histórico de participação usando uma única transação SQLite.
 */
public class PersistenciaEscalasService {

    private final EscalaDAO escalaDAO;
    private final HistoricoDesignacoesDAO historicoDAO;

    public PersistenciaEscalasService() {
        this(new EscalaDAO(), new HistoricoDesignacoesDAO());
    }

    public PersistenciaEscalasService(
            EscalaDAO escalaDAO,
            HistoricoDesignacoesDAO historicoDAO
    ) {
        this.escalaDAO = escalaDAO;
        this.historicoDAO = historicoDAO;
    }

    public int salvar(
            Map<LocalDate, ResultadoGeracaoEscala> resultados,
            boolean salvarHistorico
    ) {
        if (resultados == null || resultados.isEmpty()) {
            return 0;
        }

        try (Connection connection = ConnectionFactory.getConnection()) {
            boolean autoCommitOriginal = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);

                int quantidadeSalva = 0;
                for (ResultadoGeracaoEscala resultado : resultados.values()) {
                    if (resultado == null) {
                        continue;
                    }
                    escalaDAO.salvar(connection, resultado.escala());
                    quantidadeSalva++;
                }

                if (salvarHistorico) {
                    for (Map.Entry<LocalDate, ResultadoGeracaoEscala> entrada
                            : resultados.entrySet()) {
                        ResultadoGeracaoEscala resultado = entrada.getValue();
                        if (resultado == null) {
                            continue;
                        }

                        LocalDate data = entrada.getKey();
                        List<ParticipacaoDesignacao> participacoesDaData =
                                resultado.participacoes().stream()
                                        .filter(p -> p.data().equals(data))
                                        .toList();

                        // Preserva o comportamento anterior: uma geração sem
                        // participações não apaga histórico previamente salvo.
                        if (!participacoesDaData.isEmpty()) {
                            historicoDAO.substituirParticipacoesDaData(
                                    connection,
                                    data,
                                    participacoesDaData
                            );
                        }
                    }
                }

                connection.commit();
                return quantidadeSalva;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw new RuntimeException(
                        "Não foi possível salvar as escalas e o histórico de forma atômica.",
                        e
                );
            } finally {
                if (connection.getAutoCommit() != autoCommitOriginal) {
                    connection.setAutoCommit(autoCommitOriginal);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Erro de persistência ao salvar as escalas geradas.",
                    e
            );
        }
    }
}
