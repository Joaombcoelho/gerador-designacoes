package service;

import java.util.List;
import model.Parte;
import model.Pessoa;
import model.TipoParte;

public class RegrasService {
    public boolean podeDesignar(Pessoa pessoa, Parte parte, List<Pessoa> pessoasJaDesignadas) {
        if (pessoa == null || parte == null || pessoasJaDesignadas.contains(pessoa) || !pessoa.isAtivo()) {
            return false;
        }

        return switch (parte.getTipo()) {
            case LEITURA -> pessoa.podeFazerLeitura();
            case DISCURSO -> pessoa.podeFazerDiscurso();
            case DEMONSTRACAO -> pessoa.podeSerResponsavel() || pessoa.podeSerAjudante();
        };
    }

    public boolean podeFormarDemonstracao(Pessoa responsavel, Pessoa ajudante, List<Pessoa> pessoasJaDesignadas) {
        return responsavel != null
                && ajudante != null
                && responsavel != ajudante
                && !pessoasJaDesignadas.contains(responsavel)
                && !pessoasJaDesignadas.contains(ajudante)
                && responsavel.isAtivo()
                && ajudante.isAtivo()
                && responsavel.getSexo() == ajudante.getSexo()
                && responsavel.podeSerResponsavel()
                && ajudante.podeSerAjudante();
    }
}
