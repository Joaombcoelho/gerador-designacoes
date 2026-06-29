package service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import model.Parte;
import model.Pessoa;
import model.Sexo;
import model.TipoParte;
import org.junit.jupiter.api.Test;

class RegrasServiceTest {
    private final RegrasService regrasService = new RegrasService();

    @Test
    void permiteLeituraQuandoPessoaEstaAtivaETemPermissao() {
        Pessoa pessoa = pessoa("Ana", Sexo.FEMININO, true, false, false, true, false);
        Parte leitura = new Parte("Leitura", TipoParte.LEITURA);

        boolean podeDesignar = regrasService.podeDesignar(pessoa, leitura, List.of());

        assertTrue(podeDesignar);
    }

    @Test
    void naoPermiteLeituraQuandoPessoaNaoTemPermissao() {
        Pessoa pessoa = pessoa("Ana", Sexo.FEMININO, true, false, false, false, false);
        Parte leitura = new Parte("Leitura", TipoParte.LEITURA);

        boolean podeDesignar = regrasService.podeDesignar(pessoa, leitura, List.of());

        assertFalse(podeDesignar);
    }

    @Test
    void naoPermiteDesignarPessoaInativa() {
        Pessoa pessoa = pessoa("Ana", Sexo.FEMININO, false, true, true, true, true);
        Parte discurso = new Parte("Discurso", TipoParte.DISCURSO);

        boolean podeDesignar = regrasService.podeDesignar(pessoa, discurso, List.of());

        assertFalse(podeDesignar);
    }

    @Test
    void permiteDiscursoQuandoPessoaEstaAtivaETemPermissao() {
        Pessoa pessoa = pessoa("Carlos", Sexo.MASCULINO, true, false, false, false, true);
        Parte discurso = new Parte("Discurso", TipoParte.DISCURSO);

        boolean podeDesignar = regrasService.podeDesignar(pessoa, discurso, List.of());

        assertTrue(podeDesignar);
    }

    @Test
    void naoPermiteDesignarPessoaJaDesignadaNaSemana() {
        Pessoa pessoa = pessoa("Bruno", Sexo.MASCULINO, true, true, true, true, true);
        Parte leitura = new Parte("Leitura", TipoParte.LEITURA);

        boolean podeDesignar = regrasService.podeDesignar(pessoa, leitura, new ArrayList<>(List.of(pessoa)));

        assertFalse(podeDesignar);
    }

    @Test
    void permiteDemonstracaoComResponsavelEAjudanteDoMesmoSexo() {
        Pessoa responsavel = pessoa("Ana", Sexo.FEMININO, true, true, false, false, false);
        Pessoa ajudante = pessoa("Beatriz", Sexo.FEMININO, true, false, true, false, false);

        boolean podeFormar = regrasService.podeFormarDemonstracao(responsavel, ajudante, List.of());

        assertTrue(podeFormar);
    }

    @Test
    void naoPermiteDemonstracaoComPessoasDeSexosDiferentes() {
        Pessoa responsavel = pessoa("Ana", Sexo.FEMININO, true, true, false, false, false);
        Pessoa ajudante = pessoa("Bruno", Sexo.MASCULINO, true, false, true, false, false);

        boolean podeFormar = regrasService.podeFormarDemonstracao(responsavel, ajudante, List.of());

        assertFalse(podeFormar);
    }

    @Test
    void naoPermiteMesmaPessoaComoResponsavelEAjudante() {
        Pessoa pessoa = pessoa("Ana", Sexo.FEMININO, true, true, true, false, false);

        boolean podeFormar = regrasService.podeFormarDemonstracao(pessoa, pessoa, List.of());

        assertFalse(podeFormar);
    }

    private Pessoa pessoa(
            String nome,
            Sexo sexo,
            boolean ativo,
            boolean podeSerResponsavel,
            boolean podeSerAjudante,
            boolean podeFazerLeitura,
            boolean podeFazerDiscurso
    ) {
        return new Pessoa(
                nome,
                sexo,
                ativo,
                podeSerResponsavel,
                podeSerAjudante,
                podeFazerLeitura,
                podeFazerDiscurso
        );
    }
}
