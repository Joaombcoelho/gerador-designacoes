package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;
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
}
