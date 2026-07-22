package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class ParteDAOTest {

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
    }
}