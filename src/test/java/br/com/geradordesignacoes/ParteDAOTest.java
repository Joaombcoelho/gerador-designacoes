package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.database.ConnectionFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ParteDAOTest extends BaseDAOTest {

    @Test
    void deveSalvarEBuscarParte() {

        ParteDAO parteDAO = new ParteDAO();

        Parte parte = new Parte(
                "Leitura Teste",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        Parte salva = parteDAO.salvar(parte);

        assertNotNull(salva.getId());

        Optional<Parte> encontrada = parteDAO.buscarPorId(salva.getId());

        assertTrue(encontrada.isPresent());

        Parte recuperada = encontrada.get();

        assertEquals(salva.getNome(), recuperada.getNome());
        assertEquals(salva.getTipo(), recuperada.getTipo());
        assertEquals(salva.getPrivilegioMinimo(), recuperada.getPrivilegioMinimo());
        assertEquals(salva.getExigeAjudante(), recuperada.getExigeAjudante());
        assertEquals(salva.getSexoPermitido(), recuperada.getSexoPermitido());
        assertEquals(salva.getQuantidadeMinimaParticipantes(), recuperada.getQuantidadeMinimaParticipantes());
        assertEquals(salva.geraFormulario(), recuperada.geraFormulario());
        assertEquals(
                List.of(TipoParticipacao.LEITOR),
                recuperada.getParticipacoesNecessarias()
        );
    }

    @Test
    void devePersistirDuracaoPadraoOpcional() {
        ParteDAO dao = new ParteDAO();
        Parte parte = new Parte(
                "Parte com duração",
                TipoParte.DISCURSO,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(TipoParticipacao.ORADOR)
        );
        parte.setDuracaoPadraoMinutos(10);
        Parte salva = dao.salvar(parte);
        assertEquals(10, dao.buscarPorId(salva.getId()).orElseThrow().getDuracaoPadraoMinutos());
        salva.setDuracaoPadraoMinutos(null);
        dao.atualizar(salva);
        assertNull(dao.buscarPorId(salva.getId()).orElseThrow().getDuracaoPadraoMinutos());
    }

    @Test
    void deveRejeitarDuracaoPadraoNaoPositiva() {
        Parte parte = new Parte(
                "Parte inválida",
                TipoParte.DISCURSO,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(TipoParticipacao.ORADOR)
        );
        assertThrows(IllegalArgumentException.class, () -> parte.setDuracaoPadraoMinutos(0));
    }

    @Test
    void deveListarParteComParticipacoesNecessarias() {

        ParteDAO parteDAO = new ParteDAO();

        Parte salva = parteDAO.salvar(
                new Parte(
                        "Demonstração Teste",
                        TipoParte.DEMONSTRACAO,
                        Privilegio.BATIZADO,
                        true,
                        SexoPermitido.AMBOS,
                        2,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                )
        );

        Parte recuperada = parteDAO.listarTodos().stream()
                .filter(parte -> parte.getId().equals(salva.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                ),
                recuperada.getParticipacoesNecessarias()
        );
    }

    @Test
    void deveArquivarParteSemParticipacoesSemApagarCadastro() {
        ParteDAO dao = new ParteDAO();
        Parte parte = dao.salvar(new Parte("Parte arquivável", TipoParte.LEITURA, Privilegio.PUBLICADOR,
                false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));

        dao.arquivar(parte.getId());

        assertTrue(dao.buscarPorId(parte.getId()).orElseThrow().isArquivado());
        assertFalse(dao.listarAtivos().stream().anyMatch(p -> p.getId().equals(parte.getId())));
        assertTrue(dao.listarArquivados().stream().anyMatch(p -> p.getId().equals(parte.getId())));
    }

    @Test
    void deveBloquearArquivamentoParteComHistoricoHoje() throws Exception {
        Pessoa pessoa = new PessoaDAO().salvar(new Pessoa("Pessoa teste parte", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR));
        ParteDAO parteDAO = new ParteDAO();
        Parte parte = parteDAO.salvar(new Parte("Parte com participação futura", TipoParte.LEITURA, Privilegio.PUBLICADOR,
                false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, LocalDate.now().toString());
            ps.setInt(2, pessoa.getId());
            ps.setInt(3, parte.getId());
            ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }

        assertThrows(IllegalStateException.class, () -> parteDAO.arquivar(parte.getId()));
        assertFalse(parteDAO.buscarPorId(parte.getId()).orElseThrow().isArquivado());
    }

    @Test
    void devePreservarHistoricoAoArquivarParteComParticipacaoPassada() throws Exception {
        Pessoa pessoa = new PessoaDAO().salvar(new Pessoa("Pessoa histórico parte", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR));
        ParteDAO parteDAO = new ParteDAO();
        Parte parte = parteDAO.salvar(new Parte("Parte com histórico passado", TipoParte.LEITURA, Privilegio.PUBLICADOR,
                false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        LocalDate dataHistorica = LocalDate.now().minusDays(1);
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, dataHistorica.toString());
            ps.setInt(2, pessoa.getId());
            ps.setInt(3, parte.getId());
            ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }

        parteDAO.arquivar(parte.getId());

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM historico_designacoes WHERE parte_id = ? AND data = ?")) {
            ps.setInt(1, parte.getId());
            ps.setString(2, dataHistorica.toString());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1));
            }
        }
    }


    @Test
    void deveBloquearArquivamentoParteComHistoricoFuturo() throws Exception {
        Pessoa pessoa = new PessoaDAO().salvar(new Pessoa("Pessoa data futura", Sexo.MASCULINO, true, true, false, false, false, Privilegio.PUBLICADOR));
        ParteDAO parteDAO = new ParteDAO();
        Parte parte = parteDAO.salvar(new Parte("Parte com data futura", TipoParte.LEITURA, Privilegio.PUBLICADOR,
                false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO historico_designacoes (data, pessoa_id, parte_id, tipo_participacao) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, LocalDate.now().plusDays(1).toString());
            ps.setInt(2, pessoa.getId()); ps.setInt(3, parte.getId()); ps.setString(4, TipoParticipacao.LEITOR.name());
            ps.executeUpdate();
        }
        assertThrows(IllegalStateException.class, () -> parteDAO.arquivar(parte.getId()));
    }

}