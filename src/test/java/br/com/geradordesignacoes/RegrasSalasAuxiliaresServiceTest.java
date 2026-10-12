package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.RegrasSalasAuxiliaresService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegrasSalasAuxiliaresServiceTest {

    private RegrasSalasAuxiliaresService regras;

    @BeforeEach
    void setUp() {
        regras = new RegrasSalasAuxiliaresService();
    }

    @Test
    void devePermitirParteLeituraIndependentementeDaSecao() {
        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoParticipacao.LEITOR
        );

        assertTrue(regras.parteElegivel(parte));
    }

    @Test
    void devePermitirParteDaSecaoMinisterio() {
        Parte parte = criarParte(
                TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO,
                TipoParticipacao.RESPONSAVEL
        );

        assertTrue(regras.parteElegivel(parte));
    }

    @Test
    void naoDevePermitirParteOQueVoceDiria() {
        Parte parte = criarParte(
                TipoParte.O_QUE_VOCE_DIRIA,
                SecaoParte.MINISTERIO,
                TipoParticipacao.RESPONSAVEL
        );

        assertFalse(regras.parteElegivel(parte));
    }

    @Test
    void naoDevePermitirParteForaDasCategoriasElegiveis() {
        Parte parte = criarParte(
                TipoParte.DISCURSO_TESOUROS,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoParticipacao.ORADOR
        );

        assertFalse(regras.parteElegivel(parte));
    }

    @Test
    void deveRespeitarPermissaoIndividualDaSalaB() {
        Pessoa pessoa = criarPessoa(
                true, true, false, Privilegio.BATIZADO
        );

        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoParticipacao.LEITOR
        );

        assertTrue(regras.parteElegivel(parte));
        assertTrue(pessoa.isAtivo());
        assertTrue(pessoa.podeParticiparSalaB());
        assertFalse(pessoa.podeParticiparSalaC());

        assertTrue(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.B)
        );

        assertFalse(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.C)
        );
    }

    @Test
    void naoDevePermitirPessoaSemPermissaoParaSalaB() {
        Pessoa pessoa = criarPessoa(
                true, false, true, Privilegio.BATIZADO
        );
        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoParticipacao.LEITOR
        );

        assertFalse(regras.podeParticipar(
                pessoa, parte, SalaAuxiliar.B
        ));
    }

    @Test
    void deveRespeitarPermissaoIndividualDaSalaC() {
        Pessoa pessoa = criarPessoa(
                true,
                false,
                true,
                Privilegio.BATIZADO
        );

        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.MINISTERIO,
                TipoParticipacao.LEITOR
        );

        RegrasSalasAuxiliaresService regras =
                new RegrasSalasAuxiliaresService();

        assertFalse(
                pessoa.podeParticiparSalaB(),
                "A pessoa não deveria ter permissão para a Sala B"
        );

        assertTrue(
                pessoa.podeParticiparSalaC(),
                "A pessoa deveria ter permissão para a Sala C"
        );

        assertTrue(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.C),
                "A pessoa deveria poder participar na Sala C"
        );

        assertFalse(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.B),
                "A pessoa não deveria poder participar na Sala B"
        );
    }
    @Test
    void naoDevePermitirPessoaInativa() {
        Pessoa pessoa = criarPessoa(
                false, true, true, Privilegio.BATIZADO
        );
        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                TipoParticipacao.LEITOR
        );

        assertFalse(regras.podeParticipar(
                pessoa, parte, SalaAuxiliar.B
        ));
    }

    @Test
    void naoDevePermitirPessoaSemQualificacaoParaParte() {
        Pessoa pessoaSemQualificacao = new Pessoa(
                "Pessoa sem qualificação",
                Sexo.MASCULINO,
                true,  // ativa
                true,  // pode ser responsável
                true,  // pode ser ajudante
                false, // não pode fazer leitura
                true,  // pode fazer discurso
                true,  // pode fazer oração
                true,  // pode ser presidente
                true,  // pode ser dirigente
                Privilegio.BATIZADO,
                NivelLeitura.BASICO,
                true,  // Sala Principal
                true,  // Sala B
                true   // Sala C
        );

        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.MINISTERIO,
                TipoParticipacao.LEITOR
        );

        RegrasSalasAuxiliaresService regras =
                new RegrasSalasAuxiliaresService();

        assertFalse(
                regras.podeParticipar(
                        pessoaSemQualificacao,
                        parte,
                        SalaAuxiliar.B
                )
        );

        assertFalse(
                regras.podeParticipar(
                        pessoaSemQualificacao,
                        parte,
                        SalaAuxiliar.C
                )
        );
    }


    @Test
    void naoDevePermitirArgumentosNulos() {
        Pessoa pessoa = criarPessoa(
                true, true, true, Privilegio.ANCIAO
        );

        Parte parte = criarParte(
                TipoParte.LEITURA,
                SecaoParte.MINISTERIO,
                TipoParticipacao.LEITOR
        );

        assertFalse(regras.parteElegivel(null));

        assertFalse(
                regras.podeParticipar(null, parte, SalaAuxiliar.B)
        );

        assertFalse(
                regras.podeParticipar(pessoa, null, SalaAuxiliar.B)
        );

        assertFalse(
                regras.podeParticipar(pessoa, parte, null)
        );
    }

    @Test
    void naoDevePermitirBatizadoComoResponsavel() {
        Pessoa pessoa = criarPessoa(
                true, true, true, Privilegio.BATIZADO
        );

        Parte parte = criarParte(
                TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO,
                TipoParticipacao.RESPONSAVEL
        );

        assertFalse(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.B),
                "Um batizado sem privilégio suficiente não deve ser responsável"
        );
    }

    @Test
    void devePermitirAnciaoComoResponsavel() {
        Pessoa pessoa = criarPessoa(
                true, true, true, Privilegio.ANCIAO
        );

        Parte parte = criarParte(
                TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO,
                TipoParticipacao.RESPONSAVEL
        );

        assertTrue(
                regras.podeParticipar(pessoa, parte, SalaAuxiliar.B),
                "Um ancião qualificado e autorizado deve poder ser responsável"
        );
    }
    private Pessoa criarPessoa(
            boolean ativa,
            boolean salaB,
            boolean salaC,
            Privilegio privilegio
    ) {
        return new Pessoa(
                "Pessoa de teste",
                Sexo.MASCULINO,
                ativa,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                privilegio,
                NivelLeitura.BASICO,
                true,
                salaB,
                salaC
        );
    }

    private Parte criarParte(
            TipoParte tipo,
            SecaoParte secao,
            TipoParticipacao participacao
    ) {
        return new Parte(
                "Parte de teste",
                tipo,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                secao,
                null,
                false,
                List.of(participacao)
        );
    }
}