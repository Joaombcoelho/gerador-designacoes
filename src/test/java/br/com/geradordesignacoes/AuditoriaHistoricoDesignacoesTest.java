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

    @Test
    void edicaoManualPersistidaAoReabrirPreservaHistoricoOriginal() {
        LocalDate data = LocalDate.of(2099, 2, 4);
        Pessoa pessoaOriginal = salvarPessoa("Pessoa original da escala");
        Pessoa pessoaEditada = salvarPessoa("Pessoa após edição manual");
        Parte parte = salvarParte("Parte de edição manual");
        historicoDAO.salvar(participacao(data, pessoaOriginal, parte));

        Escala escala = escalaDAO.salvar(new Escala(
                data,
                List.of(new Designacao(data, parte, pessoaOriginal, null))
        ));
        Designacao designacaoSalva = escalaDAO.buscarPorId(escala.getId())
                .orElseThrow().getDesignacoes().getFirst();

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                pessoaEditada.getId(),
                null
        );

        Escala reaberta = escalaDAO.buscarPorId(escala.getId()).orElseThrow();
        assertEquals(pessoaEditada.getId(),
                reaberta.getDesignacoes().getFirst().responsavel().getId(),
                "A edição manual deve continuar persistida após recarregar a escala.");

        List<ParticipacaoDesignacao> historico = historicoDAO.listarTodas();
        assertEquals(1, historico.stream().filter(p -> p.data().equals(data)).count());
        assertEquals(pessoaOriginal.getId(), historico.stream()
                .filter(p -> p.data().equals(data))
                .findFirst().orElseThrow().pessoa().getId(),
                "Editar a escala não deve reescrever silenciosamente o histórico separado.");
    }

    @Test
    void regenerarDataAlvoPreservaOutrasDatasEContinuaSemDuplicarHistorico() {
        LocalDate dataAnterior = LocalDate.of(2026, 10, 14);
        LocalDate dataAlvo = LocalDate.of(2099, 2, 11);
        Pessoa pessoaHistorica = salvarPessoa("Pessoa do histórico");
        Pessoa pessoaGerada = salvarPessoa("Pessoa da nova geração");
        Parte parte = salvarParte("Parte de regeneração");

        historicoDAO.salvar(participacao(dataAnterior, pessoaHistorica, parte));
        historicoDAO.salvar(participacao(dataAlvo, pessoaHistorica, parte));

        ResultadoGeracaoEscala resultado = new ResultadoGeracaoEscala(
                new Escala(dataAlvo, List.of(
                        new Designacao(dataAlvo, parte, pessoaGerada, null)
                )),
                List.of(participacao(dataAlvo, pessoaGerada, parte)),
                List.of(),
                List.of()
        );
        PersistenciaEscalasService service =
                new PersistenciaEscalasService(escalaDAO, historicoDAO);

        service.salvar(Map.of(dataAlvo, resultado), true);
        service.salvar(Map.of(dataAlvo, resultado), true);

        List<ParticipacaoDesignacao> historico = historicoDAO.listarTodas();
        assertEquals(2, historico.size(),
                "Repetir a persistência da mesma data não deve duplicar participações.");
        assertTrue(historico.stream().anyMatch(p ->
                p.data().equals(dataAnterior)
                        && p.pessoa().getId().equals(pessoaHistorica.getId())),
                "A regeneração não deve remover o histórico de outra data.");
        List<ParticipacaoDesignacao> historicoDaDataAlvo = historico.stream()
                .filter(p -> p.data().equals(dataAlvo))
                .toList();
        assertEquals(1, historicoDaDataAlvo.size());
        assertEquals(pessoaGerada.getId(), historicoDaDataAlvo.getFirst().pessoa().getId());
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
