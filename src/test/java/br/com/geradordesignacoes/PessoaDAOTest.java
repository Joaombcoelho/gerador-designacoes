package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.TipoParticipacao;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.Sexo;
import br.com.geradordesignacoes.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class PessoaDAOTest extends BaseDAOTest {

    @Test
    void deveSalvarEBuscarPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Teste João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        assertNotNull(pessoa.getId());

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isPresent());

        Pessoa recuperada = encontrada.get();

        assertEquals(pessoa.getNome(), recuperada.getNome());
        assertEquals(pessoa.getSexo(), recuperada.getSexo());
        assertEquals(pessoa.getPrivilegio(), recuperada.getPrivilegio());
        assertEquals(pessoa.isAtivo(), recuperada.isAtivo());
        assertEquals(pessoa.podeSerResponsavel(), recuperada.podeSerResponsavel());
        assertEquals(pessoa.podeSerAjudante(), recuperada.podeSerAjudante());
        assertEquals(pessoa.podeFazerLeitura(), recuperada.podeFazerLeitura());
        assertEquals(pessoa.podeFazerDiscurso(), recuperada.podeFazerDiscurso());
    }
    @Test
    void deveAtualizarPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Original",
                Sexo.MASCULINO,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoaDAO.salvar(pessoa);

        Pessoa atualizada = new Pessoa(
                "Pessoa Atualizada",
                Sexo.FEMININO,
                false,
                false,
                true,
                true,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        atualizada.setId(pessoa.getId());

        pessoaDAO.atualizar(atualizada);

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isPresent());

        Pessoa recuperada = encontrada.get();

        assertEquals(pessoa.getId(), recuperada.getId());
        assertEquals("Pessoa Atualizada", recuperada.getNome());
        assertEquals(Sexo.FEMININO, recuperada.getSexo());
        assertFalse(recuperada.isAtivo());

        assertFalse(recuperada.podeSerResponsavel());
        assertTrue(recuperada.podeSerAjudante());
        assertTrue(recuperada.podeFazerLeitura());
        assertTrue(recuperada.podeFazerDiscurso());
        assertTrue(recuperada.podeFazerOracao());
        assertTrue(recuperada.podeSerPresidente());
        assertTrue(recuperada.podeSerDirigente());

        assertEquals(Privilegio.BATIZADO, recuperada.getPrivilegio());
        assertEquals(NivelLeitura.EXPERIENTE, recuperada.getNivelLeitura());

    }

    @Test
    void deveRetornarVazioAoBuscarPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(999999);

        assertTrue(encontrada.isEmpty());

    }

    @Test
    void deveListarTodasAsPessoas() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa1 = new Pessoa(
                "Pessoa Lista 1",
                Sexo.MASCULINO,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        Pessoa pessoa2 = new Pessoa(
                "Pessoa Lista 2",
                Sexo.FEMININO,
                true,
                false,
                true,
                true,
                false,
                false,
                false,
                false,
                Privilegio.BATIZADO,
                NivelLeitura.EXPERIENTE
        );

        pessoaDAO.salvar(pessoa1);
        pessoaDAO.salvar(pessoa2);

        List<Pessoa> pessoas = pessoaDAO.listarTodos();

        assertNotNull(pessoas);
        assertTrue(pessoas.size() >= 2);

        assertTrue(
                pessoas.stream()
                        .anyMatch(p -> p.getId().equals(pessoa1.getId()))
        );

        assertTrue(
                pessoas.stream()
                        .anyMatch(p -> p.getId().equals(pessoa2.getId()))
        );

    }

    @Test
    void deveExcluirPessoa() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Para Excluir",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoaDAO.salvar(pessoa);

        assertNotNull(pessoa.getId());

        pessoaDAO.excluir(pessoa.getId());

        Optional<Pessoa> encontrada =
                pessoaDAO.buscarPorId(pessoa.getId());

        assertTrue(encontrada.isEmpty());

    }

    @Test
    void deveLancarExcecaoAoAtualizarPessoaSemId() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Sem ID",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> pessoaDAO.atualizar(pessoa)
                );

        assertEquals(
                "A pessoa precisa possuir um ID para ser atualizada.",
                excecao.getMessage()
        );

    }

    @Test
    void deveLancarExcecaoAoExcluirPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        RuntimeException excecao =
                assertThrows(
                        RuntimeException.class,
                        () -> pessoaDAO.excluir(999999)
                );

        assertEquals(
                "Pessoa não encontrada para exclusão.",
                excecao.getMessage()
        );

    }

    @Test
    void devePreservarPessoaQuandoExclusaoFalhaPorChaveEstrangeira() {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa(
                "Pessoa vinculada", Sexo.MASCULINO, true,
                false, false, false, false, false, false, false,
                Privilegio.PUBLICADOR, NivelLeitura.BASICO
        );
        pessoaDAO.salvar(pessoa);

        br.com.geradordesignacoes.dao.ParteDAO parteDAO =
                new br.com.geradordesignacoes.dao.ParteDAO();
        br.com.geradordesignacoes.model.Parte parte = parteDAO.salvar(
                new br.com.geradordesignacoes.model.Parte(
                        "Parte vinculada",
                        br.com.geradordesignacoes.model.TipoParte.LEITURA,
                        Privilegio.PUBLICADOR, false,
                        br.com.geradordesignacoes.model.SexoPermitido.AMBOS,
                        1, false,
                        java.util.List.of(br.com.geradordesignacoes.model.TipoParticipacao.LEITOR)
                )
        );
        new br.com.geradordesignacoes.dao.EscalaDAO().salvar(
                new br.com.geradordesignacoes.model.Escala(
                        java.time.LocalDate.of(2027, 1, 6),
                        java.util.List.of(new br.com.geradordesignacoes.model.Designacao(
                                java.time.LocalDate.of(2027, 1, 6), parte, pessoa, null
                        ))
                )
        );

        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> pessoaDAO.excluir(pessoa.getId())
        );

        assertEquals("Erro ao excluir pessoa.", excecao.getMessage());
        assertTrue(pessoaDAO.buscarPorId(pessoa.getId()).isPresent());
    }


    @Test
    void deveLancarExcecaoAoAtualizarPessoaInexistente() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Pessoa Inexistente",
                Sexo.MASCULINO,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO
        );

        pessoa.setId(999999);

        RuntimeException excecao =
                assertThrows(
                        RuntimeException.class,
                        () -> pessoaDAO.atualizar(pessoa)
                );

        assertEquals(
                "Erro ao atualizar pessoa.",
                excecao.getMessage()
        );
    }

    @Test
    void deveSalvarBuscarEAtualizarPermissoesDeSalas() {

        PessoaDAO pessoaDAO = new PessoaDAO();

        Pessoa pessoa = new Pessoa(
                "Teste Permissões de Salas",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                Privilegio.BATIZADO,
                NivelLeitura.BASICO,
                true,
                false,
                true
        );

        pessoaDAO.salvar(pessoa);

        Pessoa salva = pessoaDAO.buscarPorId(pessoa.getId())
                .orElseThrow();

        assertTrue(salva.podeParticiparSalaPrincipal());
        assertFalse(salva.podeParticiparSalaB());
        assertTrue(salva.podeParticiparSalaC());

        Pessoa atualizada = new Pessoa(
                "Teste Permissões de Salas",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                Privilegio.BATIZADO,
                NivelLeitura.BASICO,
                false,
                true,
                false
        );

        atualizada.setId(pessoa.getId());

        pessoaDAO.atualizar(atualizada);

        Pessoa recuperada = pessoaDAO.buscarPorId(pessoa.getId())
                .orElseThrow();

        assertFalse(recuperada.podeParticiparSalaPrincipal());
        assertTrue(recuperada.podeParticiparSalaB());
        assertFalse(recuperada.podeParticiparSalaC());
    }

    @Test
    void deveArquivarPessoaSemParticipacoesSemApagarCadastro() {
        PessoaDAO dao = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Pessoa arquivável", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR);
        dao.salvar(pessoa);

        dao.arquivar(pessoa.getId());

        assertTrue(dao.buscarPorId(pessoa.getId()).orElseThrow().isArquivado());
        assertFalse(dao.listarAtivos().stream().anyMatch(p -> p.getId().equals(pessoa.getId())));
        assertTrue(dao.listarArquivados().stream().anyMatch(p -> p.getId().equals(pessoa.getId())));
    }

    @Test
    void deveBloquearArquivamentoPessoaComHistoricoHoje() throws Exception {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Pessoa com participação futura", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR);
        pessoaDAO.salvar(pessoa);
        Parte parte = new ParteDAO().salvar(new Parte("Parte teste arquivamento", TipoParte.LEITURA,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, LocalDate.now().toString());
            ps.setInt(2, pessoa.getId());
            ps.setInt(3, parte.getId());
            ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }

        assertThrows(IllegalStateException.class, () -> pessoaDAO.arquivar(pessoa.getId()));
        assertFalse(pessoaDAO.buscarPorId(pessoa.getId()).orElseThrow().isArquivado());
    }

    @Test
    void devePreservarHistoricoAoArquivarPessoaComParticipacaoPassada() throws Exception {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Pessoa com histórico passado", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR);
        pessoaDAO.salvar(pessoa);
        Parte parte = new ParteDAO().salvar(new Parte("Parte teste histórico", TipoParte.LEITURA,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        LocalDate dataHistorica = LocalDate.now().minusDays(1);
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, dataHistorica.toString());
            ps.setInt(2, pessoa.getId());
            ps.setInt(3, parte.getId());
            ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }

        pessoaDAO.arquivar(pessoa.getId());

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM historico_designacoes WHERE pessoa_id = ? AND data = ?")) {
            ps.setInt(1, pessoa.getId());
            ps.setString(2, dataHistorica.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }


    @Test
    void deveBloquearArquivamentoPessoaComHistoricoFuturo() throws Exception {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Pessoa com participação futura", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR);
        pessoaDAO.salvar(pessoa);
        Parte parte = new ParteDAO().salvar(new Parte("Parte teste data futura", TipoParte.LEITURA,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, LocalDate.now().plusDays(1).toString());
            ps.setInt(2, pessoa.getId()); ps.setInt(3, parte.getId()); ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }
        assertThrows(IllegalStateException.class, () -> pessoaDAO.arquivar(pessoa.getId()));
    }


    @Test
    void devePermitirArquivarPessoaComEscalaApenasGeradaSemHistorico() {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Irineu - escala gerada", Sexo.MASCULINO, true, true,
                false, false, false, Privilegio.PUBLICADOR);
        pessoaDAO.salvar(pessoa);
        Parte parte = new ParteDAO().salvar(new Parte("Parte escala gerada", TipoParte.LEITURA,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS, 1, false,
                List.of(TipoParticipacao.LEITOR)));
        LocalDate data = LocalDate.of(2026, 12, 2);
        br.com.geradordesignacoes.model.Escala escala =
                new br.com.geradordesignacoes.model.Escala(data, List.of(
                        new br.com.geradordesignacoes.model.Designacao(data, parte, pessoa, null)));
        new br.com.geradordesignacoes.dao.EscalaDAO().salvar(escala);

        assertEquals(br.com.geradordesignacoes.model.StatusEscala.GERADA, escala.getStatus());
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement(
                     "SELECT COUNT(*) FROM historico_designacoes WHERE pessoa_id = ? AND data = ?")) {
            ps.setInt(1, pessoa.getId());
            ps.setString(2, data.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }

        pessoaDAO.arquivar(pessoa.getId());

        assertTrue(pessoaDAO.buscarPorId(pessoa.getId()).orElseThrow().isArquivado());
    }

    @Test
    void deveBloquearArquivamentoPessoaComEscalaSalvaMesmoSemHistorico() {
        PessoaDAO pessoaDAO = new PessoaDAO();
        Pessoa pessoa = new Pessoa("Pessoa escala salva", Sexo.MASCULINO, true, true,
                false, false, false, Privilegio.PUBLICADOR);
        pessoaDAO.salvar(pessoa);
        Parte parte = new ParteDAO().salvar(new Parte("Parte escala salva", TipoParte.LEITURA,
                Privilegio.PUBLICADOR, false, SexoPermitido.AMBOS, 1, false,
                List.of(TipoParticipacao.LEITOR)));
        LocalDate data = LocalDate.now().plusDays(10);
        br.com.geradordesignacoes.model.Escala escala =
                new br.com.geradordesignacoes.model.Escala(data, List.of(
                        new br.com.geradordesignacoes.model.Designacao(data, parte, pessoa, null)));
        escala.setStatus(br.com.geradordesignacoes.model.StatusEscala.SALVA);
        new br.com.geradordesignacoes.dao.EscalaDAO().salvar(escala);

        IllegalStateException erro = assertThrows(
                IllegalStateException.class, () -> pessoaDAO.arquivar(pessoa.getId()));
        assertTrue(erro.getMessage().contains(data.toString()));
        assertFalse(pessoaDAO.buscarPorId(pessoa.getId()).orElseThrow().isArquivado());
    }

}