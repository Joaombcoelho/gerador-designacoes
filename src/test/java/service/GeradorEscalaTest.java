package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;
import java.util.List;
import model.Designacao;
import model.Parte;
import model.Pessoa;
import model.Sexo;
import model.TipoParte;
import org.junit.jupiter.api.Test;

class GeradorEscalaTest {
    private final GeradorEscala geradorEscala = new GeradorEscala(new RegrasService());

    @Test
    void geraParteIndividualComPessoaPermitida() {
        Pessoa leitor = pessoa("Bruno", Sexo.MASCULINO, true, false, false, true, false);
        Parte leitura = new Parte("Leitura", TipoParte.LEITURA);

        List<Designacao> designacoes = geradorEscala.gerar(
                LocalDate.of(2026, 6, 29),
                List.of(leitura),
                List.of(leitor)
        );

        assertEquals(1, designacoes.size());
        assertSame(leitor, designacoes.get(0).getResponsavel());
        assertNull(designacoes.get(0).getAjudante());
    }

    @Test
    void geraDemonstracaoComResponsavelEAjudante() {
        Pessoa responsavel = pessoa("Ana", Sexo.FEMININO, true, true, false, false, false);
        Pessoa ajudante = pessoa("Beatriz", Sexo.FEMININO, true, false, true, false, false);
        Parte demonstracao = new Parte("Demonstração 1", TipoParte.DEMONSTRACAO);

        List<Designacao> designacoes = geradorEscala.gerar(
                LocalDate.of(2026, 6, 29),
                List.of(demonstracao),
                List.of(responsavel, ajudante)
        );

        assertEquals(1, designacoes.size());
        assertSame(responsavel, designacoes.get(0).getResponsavel());
        assertSame(ajudante, designacoes.get(0).getAjudante());
    }

    @Test
    void naoDesignaMesmaPessoaMaisDeUmaVezNaMesmaSemana() {
        Pessoa pessoa = pessoa("Bruno", Sexo.MASCULINO, true, false, false, true, true);
        Parte leitura = new Parte("Leitura", TipoParte.LEITURA);
        Parte discurso = new Parte("Discurso", TipoParte.DISCURSO);

        List<Designacao> designacoes = geradorEscala.gerar(
                LocalDate.of(2026, 6, 29),
                List.of(leitura, discurso),
                List.of(pessoa)
        );

        assertEquals(1, designacoes.size());
        assertSame(pessoa, designacoes.get(0).getResponsavel());
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
