package br.com.geradordesignacoes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.TipoParte;

public class GeradorEscala {
    private final RegrasService regrasService;

    public GeradorEscala(br.com.geradordesignacoes.service.RegrasService regrasService) {
        this.regrasService = regrasService;
    }

    public List<Designacao> gerar(LocalDate data, List<Parte> partes, List<Pessoa> pessoas) {
        List<Designacao> designacoes = new ArrayList<>();
        List<Pessoa> pessoasJaDesignadas = new ArrayList<>();

        for (Parte parte : partes) {
            if (parte.getTipo() == TipoParte.DEMONSTRACAO) {
                designarDemonstracao(data, parte, pessoas, designacoes, pessoasJaDesignadas);
            } else {
                designarParteIndividual(data, parte, pessoas, designacoes, pessoasJaDesignadas);
            }
        }

        return designacoes;
    }

    private Optional<Pessoa> encontrarPessoa(
            Parte parte,
            List<Pessoa> pessoas,
            List<Pessoa> pessoasJaDesignadas) {

        for (Pessoa pessoa : pessoas) {
            if (regrasService.podeDesignar(pessoa, parte, pessoasJaDesignadas)) {
                return Optional.of(pessoa);
            }
        }

        return Optional.empty();
    }
    private void designarParteIndividual(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            List<Pessoa> pessoasJaDesignadas
    ) {
        Optional<Pessoa> pessoa = encontrarPessoa(
                parte,
                pessoas,
                pessoasJaDesignadas);

        pessoa.ifPresent(p -> {
            designacoes.add(new Designacao(data, parte, p));
            pessoasJaDesignadas.add(p);
        });
    }

    private void designarDemonstracao(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoes,
            List<Pessoa> pessoasJaDesignadas
    ) {
        for (Pessoa responsavel : pessoas) {
            for (Pessoa ajudante : pessoas) {
                if (regrasService.podeFormarDemonstracao(responsavel, ajudante, pessoasJaDesignadas)) {
                    designacoes.add(new Designacao(data, parte, responsavel, ajudante));
                    pessoasJaDesignadas.add(responsavel);
                    pessoasJaDesignadas.add(ajudante);
                    return;
                }
            }
        }
    }
}
