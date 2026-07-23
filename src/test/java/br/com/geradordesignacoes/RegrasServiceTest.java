package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RegrasServiceTest {

    private RegrasService regrasService;

    @BeforeEach
    void setUp() {
        regrasService = new RegrasService();
    }

    @Test
    void devePermitirLeitura() {

        Pessoa pessoa = new Pessoa(
                "Leitor",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Parte parte = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        assertTrue(
                regrasService.podeDesignar(
                        pessoa,
                        parte,
                        new ArrayList<>()
                )
        );
    }

    @Test
    void naoDevePermitirPessoaInativa() {

        Pessoa pessoa = new Pessoa(
                "Inativo",
                Sexo.MASCULINO,
                false,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Parte parte = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        assertFalse(
                regrasService.podeDesignar(
                        pessoa,
                        parte,
                        new ArrayList<>()
                )
        );
    }

    @Test
    void naoDevePermitirPessoaJaDesignada() {

        Pessoa pessoa = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Parte parte = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        List<Pessoa> jaDesignadas = new ArrayList<>();

        jaDesignadas.add(pessoa);

        assertFalse(
                regrasService.podeDesignar(
                        pessoa,
                        parte,
                        jaDesignadas
                )
        );
    }

    @Test
    void devePermitirFormarDemonstracao() {

        Pessoa responsavel = new Pessoa(
                "Responsável",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Pessoa ajudante = new Pessoa(
                "Ajudante",
                Sexo.MASCULINO,
                true,
                false,
                true,
                false,
                false,
                Privilegio.PUBLICADOR
        );

        assertTrue(
                regrasService.podeFormarDemonstracao(
                        responsavel,
                        ajudante,
                        new ArrayList<>()
                )
        );
    }

    @Test
    void naoDevePermitirDemonstracaoComSexoDiferente() {

        Pessoa responsavel = new Pessoa(
                "Responsável",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Pessoa ajudante = new Pessoa(
                "Ajudante",
                Sexo.FEMININO,
                true,
                false,
                true,
                false,
                false,
                Privilegio.PUBLICADOR
        );

        assertFalse(
                regrasService.podeFormarDemonstracao(
                        responsavel,
                        ajudante,
                        new ArrayList<>()
                )
        );
    }

    @Test
    void naoDevePermitirMesmoParticipante() {

        Pessoa pessoa = new Pessoa(
                "Mesmo",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        assertFalse(
                regrasService.podeFormarDemonstracao(
                        pessoa,
                        pessoa,
                        new ArrayList<>()
                )
        );
    }

    @Test
    void devePermitirDemonstracaoComParte() {

        Pessoa responsavel = new Pessoa(
                "Responsável",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        Pessoa ajudante = new Pessoa(
                "Ajudante",
                Sexo.MASCULINO,
                true,
                false,
                true,
                false,
                false,
                Privilegio.PUBLICADOR
        );

        Parte parte = new Parte(
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                Privilegio.PUBLICADOR,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                List.of(
                        TipoParticipacao.RESPONSAVEL,
                        TipoParticipacao.AJUDANTE
                )
        );

        assertTrue(
                regrasService.podeFormarDemonstracao(
                        parte,
                        responsavel,
                        ajudante,
                        new ArrayList<>()
                )
        );
    }
}