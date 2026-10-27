package br.com.geradordesignacoes.database;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.PessoaService;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HabilidadesElegibilidadeIntegracaoTest {

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
    void habilidadesDevemPersistirEControlarElegibilidadeAposRecarregarPessoa() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("habilidades-integracao.db"));
        DatabaseInitializer.initialize();

        PessoaService pessoas = new PessoaService(new PessoaDAO());
        Pessoa pessoa = pessoa("João", Set.of("DISCURSO"));
        Pessoa salva = pessoas.salvar(pessoa);

        Pessoa recarregada = pessoas.buscarPorId(salva.getId()).orElseThrow();
        Pessoa carregadaDiretamentePeloDAO = new PessoaDAO().listarAtivos().stream()
                .filter(p -> p.getId().equals(salva.getId())).findFirst().orElseThrow();
        Parte discurso = parte(TipoParte.DISCURSO, SexoPermitido.AMBOS);
        RegrasService regras = new RegrasService();

        assertTrue(recarregada.possuiHabilidade("DISCURSO"));
        assertTrue(carregadaDiretamentePeloDAO.possuiHabilidade("DISCURSO"),
                "Fluxos que usam PessoaDAO diretamente também devem carregar habilidades.");
        assertTrue(regras.podeDesignar(recarregada, discurso, List.of()));

        recarregada.setHabilidadesDesignacao(Set.of());
        pessoas.atualizar(recarregada);

        Pessoa semHabilidade = pessoas.buscarPorId(salva.getId()).orElseThrow();
        assertFalse(regras.podeDesignar(semHabilidade, discurso, List.of()));
    }

    @Test
    void habilidadeNaoPodeAnularRestricaoDeSexoOuCategoria() throws Exception {
        contexto = ConnectionFactory.openTestContext(
                diretorioTemporario.resolve("habilidades-regras.db"));
        DatabaseInitializer.initialize();

        PessoaService pessoas = new PessoaService(new PessoaDAO());
        Pessoa pessoa = pessoa("Maria", Set.of("DISCURSO"));
        pessoa = new Pessoa(
                pessoa.getNome(), Sexo.FEMININO, true,
                true, true, false, false, false, false, false,
                Privilegio.PUBLICADOR, NivelLeitura.BASICO);
        pessoa.setHabilidadesDesignacao(Set.of("DISCURSO"));
        pessoa = pessoas.salvar(pessoa);

        Parte discursoMasculino = parte(TipoParte.DISCURSO, SexoPermitido.MASCULINO);

        assertFalse(new RegrasService().podeDesignar(pessoa, discursoMasculino, List.of()));
    }

    private static Pessoa pessoa(String nome, Set<String> habilidades) {
        Pessoa pessoa = new Pessoa(
                nome, Sexo.MASCULINO, true,
                true, true, true, true, true, true, true,
                Privilegio.ANCIAO, NivelLeitura.EXPERIENTE);
        pessoa.setHabilidadesDesignacao(habilidades);
        return pessoa;
    }

    private static Parte parte(TipoParte tipo, SexoPermitido sexo) {
        return new Parte(
                "Discurso de teste", tipo, Privilegio.PUBLICADOR,
                false, sexo, 1, false,
                NivelLeitura.BASICO, SecaoParte.NOSSA_VIDA_CRISTA,
                null, false, List.of(TipoParticipacao.ORADOR));
    }
}
