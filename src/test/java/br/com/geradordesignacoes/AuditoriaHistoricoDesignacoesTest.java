package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.PersistenciaEscalasService;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuditoriaHistoricoDesignacoesTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final EscalaDAO escalaDAO = new EscalaDAO();
    private final HistoricoDesignacoesDAO historicoDAO =
            new HistoricoDesignacoesDAO();

    @Test
    void substituirUmaDataPreservaHistoricoDasOutrasDuasDatas() {
        LocalDate dataAnterior = LocalDate.of(2026, 10, 7);
        LocalDate dataAlvo = LocalDate.of(2026, 10, 14);
        LocalDate dataPosterior = LocalDate.of(2026, 10, 21);

        Pessoa pessoaAntiga = salvarPessoa("Pessoa antiga");
        Pessoa pessoaNova = salvarPessoa("Pessoa nova");
        Parte parteAntiga = salvarParte("Parte antiga");
        Parte parteNova = salvarParte("Parte nova");

        historicoDAO.salvar(participacao(dataAnterior, pessoaAntiga, parteAntiga));
        historicoDAO.salvar(participacao(dataAlvo, pessoaAntiga, parteAntiga));
        historicoDAO.salvar(participacao(dataPosterior, pessoaAntiga, parteAntiga));

        historicoDAO.substituirParticipacoesDaData(
                dataAlvo,
                List.of(participacao(dataAlvo, pessoaNova, parteNova))
        );

        List<ParticipacaoDesignacao> historico = historicoDAO.listarTodas();
        assertEquals(3, historico.size());
        assertTrue(historico.stream().anyMatch(p ->
                p.data().equals(dataAnterior)
                        && p.pessoa().getId().equals(pessoaAntiga.getId())
                        && p.parte().getId().equals(parteAntiga.getId())));
        assertTrue(historico.stream().anyMatch(p ->
                p.data().equals(dataAlvo)
                        && p.pessoa().getId().equals(pessoaNova.getId())
                        && p.parte().getId().equals(parteNova.getId())));
        assertTrue(historico.stream().anyMatch(p ->
                p.data().equals(dataPosterior)
                        && p.pessoa().getId().equals(pessoaAntiga.getId())
                        && p.parte().getId().equals(parteAntiga.getId())));
    }

    @Test
    void falhaAoPersistirHistoricoReverteEscalaEPreservaHistoricoAnterior() {
        LocalDate dataAlvo = LocalDate.of(2026, 11, 4);
        LocalDate outraData = LocalDate.of(2026, 11, 11);
        Pessoa pessoaValida = salvarPessoa("Pessoa válida");
        Parte parte = salvarParte("Parte de rollback");

        historicoDAO.salvar(participacao(dataAlvo, pessoaValida, parte));
        historicoDAO.salvar(participacao(outraData, pessoaValida, parte));
        List<String> historicoAntes = fotografarHistorico();

        Pessoa inexistente = new Pessoa(
                "Pessoa inexistente",
                Sexo.MASCULINO,
                true, true, true, true, false, Privilegio.BATIZADO
        );
        inexistente.setId(987654321);

        ParticipacaoDesignacao participacaoInvalida =
                participacao(dataAlvo, inexistente, parte);
        ResultadoGeracaoEscala resultado = new ResultadoGeracaoEscala(
                new Escala(dataAlvo, List.of()),
                List.of(participacaoInvalida),
                List.of(),
                List.of()
        );
        PersistenciaEscalasService service =
                new PersistenciaEscalasService(escalaDAO, historicoDAO);

        assertThrows(
                RuntimeException.class,
                () -> service.salvar(Map.of(dataAlvo, resultado), true)
        );

        assertTrue(escalaDAO.listarTodas().stream()
                .noneMatch(escala -> escala.getData().equals(dataAlvo)),
                "A escala criada na transação falha não deve permanecer.");
        assertEquals(historicoAntes, fotografarHistorico(),
                "O histórico das duas datas deve permanecer idêntico após rollback.");
    }

    @Test
    void excluirEscalaNaoExcluiHistoricoDaData() {
        LocalDate data = LocalDate.of(2026, 12, 2);
        Pessoa pessoa = salvarPessoa("Pessoa após exclusão");
        Parte parte = salvarParte("Parte após exclusão");
        historicoDAO.salvar(participacao(data, pessoa, parte));
        Escala escala = escalaDAO.salvar(new Escala(data, List.of()));

        escalaDAO.excluir(escala.getId());

        assertTrue(escalaDAO.listarTodas().stream()
                .noneMatch(item -> item.getData().equals(data)));
        assertTrue(historicoDAO.listarTodas().stream().anyMatch(item ->
                item.data().equals(data)
                        && item.pessoa().getId().equals(pessoa.getId())
                        && item.parte().getId().equals(parte.getId())));
    }

    @Test
    void conexaoHabilitaChavesEstrangeiras() throws Exception {
        try (Connection connection =
                     br.com.geradordesignacoes.database.ConnectionFactory.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement("PRAGMA foreign_keys");
             var resultSet = statement.executeQuery()) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
    }

    private Pessoa salvarPessoa(String nome) {
        return pessoaDAO.salvar(new Pessoa(
                nome,
                Sexo.MASCULINO,
                true, true, true, true, false, Privilegio.BATIZADO
        ));
    }

    private Parte salvarParte(String nome) {
        return parteDAO.salvar(new Parte(
                nome,
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        ));
    }

    private ParticipacaoDesignacao participacao(
            LocalDate data,
            Pessoa pessoa,
            Parte parte
    ) {
        return new ParticipacaoDesignacao(
                data, pessoa, parte, TipoParticipacao.LEITOR
        );
    }

    private List<String> fotografarHistorico() {
        return historicoDAO.listarTodas().stream()
                .map(p -> p.data() + "|" + p.pessoa().getId() + "|"
                        + p.parte().getId() + "|" + p.tipoParticipacao()
                        + "|" + p.sala())
                .toList();
    }
}
