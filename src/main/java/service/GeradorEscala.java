package service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Designacao;
import model.Parte;
import model.Pessoa;

public class GeradorEscala {
    private final RegrasService regrasService;

    public GeradorEscala(RegrasService regrasService) {
        this.regrasService = regrasService;
    }

    public List<Designacao> gerar(LocalDate data, List<Parte> partes, List<Pessoa> pessoas) {
        List<Designacao> designacoes = new ArrayList<>();
        List<Pessoa> pessoasJaDesignadas = new ArrayList<>();

        int indicePessoa = 0;
        for (Parte parte : partes) {
            while (indicePessoa < pessoas.size()) {
                Pessoa pessoa = pessoas.get(indicePessoa++);
                if (regrasService.podeDesignar(pessoa, pessoasJaDesignadas)) {
                    designacoes.add(new Designacao(data, parte, pessoa));
                    pessoasJaDesignadas.add(pessoa);
                    break;
                }
            }
        }

        return designacoes;
    }
}
