package br.com.geradordesignacoes.database;

import br.com.geradordesignacoes.dao.HabilidadeDesignacaoDAO;
import br.com.geradordesignacoes.model.HabilidadeDesignacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HabilidadeDesignacaoDAOTest {

    @TempDir
    Path diretorioTemporario;

    private ConnectionFactory.TestContext contexto;

    @AfterEach
    void fecharContexto() throws Exception {
        if (contexto != null) {
            contexto.close();
        }
    }

    @Test
    void deveCadastrarListarERecuperarHabilidadesDaPessoa() throws Exception {
        prepararBanco("habilidades-dao.db");
        HabilidadeDesignacaoDAO dao = new HabilidadeDesignacaoDAO();
        HabilidadeDesignacao discurso = dao.salvar(
                new HabilidadeDesignacao(null, "TESTE_DISCURSO", "Discurso", true));
        HabilidadeDesignacao presidencia = dao.salvar(
                new HabilidadeDesignacao(null, "TESTE_PRESIDENCIA", "Presidência", true));

        dao.substituirHabilidades(501, List.of(discurso.id(), presidencia.id()));

        assertEquals(7, dao.listarAtivas().size());
        assertEquals(List.of("Discurso", "Presidência"),
                dao.listarPorPessoa(501).stream().map(HabilidadeDesignacao::nome).toList());
    }

    @Test
    void deveReverterSubstituicaoSeUmaHabilidadeNaoExistir() throws Exception {
        prepararBanco("habilidades-rollback.db");
        HabilidadeDesignacaoDAO dao = new HabilidadeDesignacaoDAO();
        HabilidadeDesignacao discurso = dao.salvar(
                new HabilidadeDesignacao(null, "TESTE_DISCURSO", "Discurso", true));
        dao.substituirHabilidades(501, List.of(discurso.id()));

        assertThrows(RuntimeException.class,
                () -> dao.substituirHabilidades(501, List.of(999999)));

        assertEquals(List.of("Discurso"),
                dao.listarPorPessoa(501).stream().map(HabilidadeDesignacao::nome).toList());
    }

    @Test
    void deveRejeitarIdsDuplicadosEIdsDePessoaInvalidos() throws Exception {
        prepararBanco("habilidades-validacao.db");
        HabilidadeDesignacaoDAO dao = new HabilidadeDesignacaoDAO();
        HabilidadeDesignacao discurso = dao.salvar(
                new HabilidadeDesignacao(null, "TESTE_DISCURSO", "Discurso", true));

        assertThrows(IllegalArgumentException.class,
                () -> dao.substituirHabilidades(501, List.of(discurso.id(), discurso.id())));
        assertThrows(IllegalArgumentException.class,
                () -> dao.substituirHabilidades(999999, List.of(discurso.id())));
        assertThrows(IllegalArgumentException.class,
                () -> dao.listarPorPessoa(null));
    }

    private void prepararBanco(String nome) throws Exception {
        contexto = ConnectionFactory.openTestContext(diretorioTemporario.resolve(nome));
        DatabaseInitializer.initialize();
        try (Connection connection = ConnectionFactory.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    INSERT INTO pessoa
                        (id, nome, sexo, ativo, pode_ser_responsavel,
                         pode_ser_ajudante, pode_fazer_leitura,
                         pode_fazer_discurso, privilegio, nivel_leitura)
                    VALUES (501, 'Pessoa de teste', 'MASCULINO', 1, 0,
                            0, 0, 0, 'PUBLICADOR', 'BASICO')
                    """);
        }
    }
}
