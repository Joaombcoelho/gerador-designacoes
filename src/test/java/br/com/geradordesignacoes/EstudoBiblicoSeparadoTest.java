package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class EstudoBiblicoSeparadoTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();

    @Test
    void geraDirigenteELeitorComoDesignacoesIndependentes() {
        Pessoa dirigente = pessoa("Dirigente", Privilegio.SERVO_MINISTERIAL, false, true);
        Pessoa leitor = pessoa("Leitor", Privilegio.BATIZADO, true, false);

        ResultadoGeracaoEscala resultado = gerar(List.of(dirigente, leitor));

        assertEquals(2, resultado.escala().getDesignacoes().size());
        assertTrue(resultado.escala().getDesignacoes().stream()
                .anyMatch(d -> dirigente.equals(d.responsavel()) && d.ajudante() == null));
        assertTrue(resultado.escala().getDesignacoes().stream()
                .anyMatch(d -> d.responsavel() == null && leitor.equals(d.ajudante())));
        assertEquals(List.of(TipoParticipacao.DIRIGENTE, TipoParticipacao.AJUDANTE),
                resultado.participacoes().stream().map(ParticipacaoDesignacao::tipoParticipacao).toList());
    }

    @Test
    void dirigentePermaneceQuandoLeitorNaoEstaDisponivel() {
        Pessoa dirigente = pessoa("Dirigente", Privilegio.SERVO_MINISTERIAL, false, true);

        ResultadoGeracaoEscala resultado = gerar(List.of(dirigente));

        assertEquals(1, resultado.escala().getDesignacoes().size());
        assertEquals(dirigente, resultado.escala().getDesignacoes().get(0).responsavel());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("Leitor")));
    }

    @Test
    void leitorPermaneceQuandoDirigenteNaoEstaDisponivel() {
        Pessoa leitor = pessoa("Leitor", Privilegio.BATIZADO, true, false);

        ResultadoGeracaoEscala resultado = gerar(List.of(leitor));

        assertEquals(1, resultado.escala().getDesignacoes().size());
        assertEquals(leitor, resultado.escala().getDesignacoes().get(0).ajudante());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("Dirigente")));
    }

    @Test
    void falhasDoDirigenteELeitorSaoDiagnosticadasSeparadamente() {
        ResultadoGeracaoEscala resultado = gerar(List.of());

        assertTrue(resultado.escala().getDesignacoes().isEmpty());
        assertEquals(2, resultado.erros().size());
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("Dirigente")));
        assertTrue(resultado.erros().stream().anyMatch(e -> e.contains("Leitor")));
    }

    @Test
    void persisteDesignacoesIndividuaisDoEstudoBiblico() throws Exception {
        Pessoa dirigente = pessoa("Dirigente", Privilegio.SERVO_MINISTERIAL, false, true);
        Pessoa leitor = pessoa("Leitor", Privilegio.BATIZADO, true, false);
        Parte estudo = parte();
        LocalDate data = LocalDate.of(2026, 10, 8);

        ResultadoGeracaoEscala resultado = new GeradorEscala(new RegrasService())
                .gerarEscala(data, List.of(estudo), List.of(dirigente, leitor));
        Escala salva = new EscalaDAO().salvar(resultado.escala());
        Escala recarregada = new EscalaDAO().buscarPorId(salva.getId()).orElseThrow();

        assertEquals(2, recarregada.getDesignacoes().size());
        assertTrue(recarregada.getDesignacoes().stream()
                .anyMatch(d -> d.responsavel() != null && d.ajudante() == null));
        assertTrue(recarregada.getDesignacoes().stream()
                .anyMatch(d -> d.responsavel() == null && d.ajudante() != null));

        try (var connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "PRAGMA table_info(designacao)");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if ("responsavel_id".equals(resultSet.getString("name"))) {
                    assertEquals(0, resultSet.getInt("notnull"));
                }
            }
        }
    }

    private ResultadoGeracaoEscala gerar(List<Pessoa> pessoas) {
        return new GeradorEscala(new RegrasService()).gerarEscala(
                LocalDate.of(2026, 10, 1),
                List.of(parte()),
                pessoas
        );
    }

    private Parte parte() {
        return parteDAO.salvar(new Parte(
                "Estudo Bíblico",
                TipoParte.DIRIGENTE_ESTUDO,
                Privilegio.SERVO_MINISTERIAL,
                true,
                SexoPermitido.MASCULINO,
                2,
                false,
                NivelLeitura.EXPERIENTE,
                List.of(TipoParticipacao.DIRIGENTE)
        ));
    }

    private Pessoa pessoa(String nome, Privilegio privilegio, boolean leitor, boolean dirigente) {
        return pessoaDAO.salvar(new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                leitor,
                false,
                false,
                false,
                false,
                dirigente,
                privilegio,
                leitor ? NivelLeitura.EXPERIENTE : NivelLeitura.BASICO
        ));
    }
}
