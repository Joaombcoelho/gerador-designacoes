package service;

import java.util.List;
import model.Pessoa;

public class RegrasService {
    public boolean podeDesignar(Pessoa pessoa, List<Pessoa> pessoasJaDesignadas) {
        return pessoa != null && !pessoasJaDesignadas.contains(pessoa);
    }
}
