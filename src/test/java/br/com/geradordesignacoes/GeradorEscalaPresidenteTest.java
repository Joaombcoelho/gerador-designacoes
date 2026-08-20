package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeradorEscalaPresidenteTest extends BaseDAOTest {

    @Test
    void deveDesignarPresidenteComoParticipacaoFixaDaEscala() {

        Pessoa presidente = salvarPessoa(
                "Presidente",
                Sexo.MASCULINO,
                true,
                Privilegio.ANCIAO
        );

        Pessoa leitor = salvarPessoa(
                "Leitor",
                Sexo.MASCULINO,
                false,
                Privilegio.BATIZADO
        );

        Parte partePresidente = salvarPartePresidente();
        Parte parteLeitura = salvarParteLeitura();

        ResultadoGeracaoEscala resultado = new GeradorEscala(new RegrasService())
                .gerar(
                        LocalDate.of(2026, 8, 4),
                        List.of(parteLeitura, partePresidente),
                        List.of(presidente, leitor)
                );

        assertFalse(resultado.possuiErros());
        assertEquals(presidente, resultado.escala().getPresidente());
        assertEquals(TipoParte.PRESIDENTE_REUNIAO,
                resultado.getDesignacoes().get(0).parte().getTipo());
        assertTrue(resultado.participacoes().stream()
                .anyMatch(participacao ->
                        participacao.pessoa().equals(presidente)
                                && participacao.tipoParticipacao() == TipoParticipacao.PRESIDENTE));
    }

    @Test
    void deveAplicarHistoricoNaEscolhaDoPresidente() {

        Pessoa presidenteAnterior = salvarPessoa(
                "Presidente anterior",
                Sexo.MASCULINO,
                true,
                Privilegio.ANCIAO
        );

        Pessoa novoPresidente = salvarPessoa(
                "Novo presidente",
                Sexo.MASCULINO,
                true,
                Privilegio.ANCIAO
        );

        Parte partePresidente = salvarPartePresidente();

        ParticipacaoDesignacao historicoAnterior =
                new ParticipacaoDesignacao(
                        LocalDate.of(2026, 7, 28),
                        presidenteAnterior,
                        partePresidente,
                        TipoParticipacao.PRESIDENTE
                );

        ResultadoGeracaoEscala resultado = new GeradorEscala(new RegrasService())
                .gerar(
                        LocalDate.of(2026, 8, 4),
                        List.of(partePresidente),
                        List.of(presidenteAnterior, novoPresidente),
                        List.of(historicoAnterior)
                );

        assertFalse(resultado.possuiErros());
        assertEquals(novoPresidente, resultado.escala().getPresidente());
    }

    @Test
    void naoDeveDesignarPresidenteMulherOuNaoAnciao() {

        Pessoa mulher = salvarPessoa(
                "Irmã",
                Sexo.FEMININO,
                true,
                Privilegio.BATIZADO
        );

        Pessoa publicador = salvarPessoa(
                "Batizado",
                Sexo.MASCULINO,
                true,
                Privilegio.BATIZADO
        );

        Parte partePresidente = salvarPartePresidente();

        ResultadoGeracaoEscala resultado = new GeradorEscala(new RegrasService())
                .gerar(
                        LocalDate.of(2026, 8, 4),
                        List.of(partePresidente),
                        List.of(mulher, publicador)
                );

        assertTrue(resultado.possuiErros());
        assertNull(resultado.escala().getPresidente());
    }

    private Pessoa salvarPessoa(
            String nome,
            Sexo sexo,
            boolean podeSerPresidente,
            Privilegio privilegio
    ) {

        return new PessoaDAO().salvar(
                new Pessoa(
                        nome,
                        sexo,
                        true,
                        false,
                        false,
                        true,
                        false,
                        false,
                        podeSerPresidente,
                        false,
                        privilegio,
                        NivelLeitura.BASICO
                )
        );
    }

    private Parte salvarPartePresidente() {

        return new ParteDAO().salvar(
                new Parte(
                        "Presidente da reunião",
                        TipoParte.PRESIDENTE_REUNIAO,
                        Privilegio.ANCIAO,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.PRESIDENTE)
                )
        );
    }

    private Parte salvarParteLeitura() {

        return new ParteDAO().salvar(
                new Parte(
                        "Leitura",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.LEITOR)
                )
        );
    }
}
