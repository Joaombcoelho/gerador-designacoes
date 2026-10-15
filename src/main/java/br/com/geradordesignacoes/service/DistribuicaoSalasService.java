package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.SecaoParte;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DistribuicaoSalasService {


    public boolean partePodeSerDistribuida(Parte parte) {
        if (parte == null || parte.getTipo() == null) {
            return false;
        }

        TipoParte tipo = parte.getTipo();

        if (tipo == TipoParte.O_QUE_VOCE_DIRIA) {
            return false;
        }

        if (tipo == TipoParte.LEITURA) {
            return true;
        }

        return parte.getSecao() == SecaoParte.MINISTERIO;
    }

    public boolean responsavelElegivel(Pessoa pessoa) {
        if (pessoa == null || !pessoa.isAtivo()) {
            return false;
        }

        if (!pessoa.podeSerResponsavel()) {
            return false;
        }

        Privilegio privilegio = pessoa.getPrivilegio();

        return privilegio == Privilegio.ANCIAO
                || privilegio == Privilegio.SERVO_MINISTERIAL;
    }

    public boolean participantesSaoDiferentes(
            List<Pessoa> salaB,
            List<Pessoa> salaC
    ) {
        if (salaB == null || salaC == null) {
            return false;
        }

        for (Pessoa pessoaB : salaB) {
            for (Pessoa pessoaC : salaC) {
                if (mesmaPessoa(pessoaB, pessoaC)) {
                    return false;
                }
            }
        }

        return true;
    }

    public List<ProgramacaoParte> partesElegiveis(
            List<ProgramacaoParte> programacaoOriginal
    ) {
        if (programacaoOriginal == null) {
            return List.of();
        }

        List<ProgramacaoParte> elegiveis = new ArrayList<>();

        for (ProgramacaoParte programacaoParte : programacaoOriginal) {
            if (programacaoParte == null) {
                continue;
            }

            if (partePodeSerDistribuida(programacaoParte.getParte())) {
                elegiveis.add(programacaoParte);
            }
        }

        return List.copyOf(elegiveis);
    }

    private boolean mesmaPessoa(Pessoa pessoaA, Pessoa pessoaB) {
        if (pessoaA == pessoaB) {
            return true;
        }

        if (pessoaA == null || pessoaB == null) {
            return false;
        }

        if (pessoaA.getId() != null && pessoaB.getId() != null) {
            return Objects.equals(pessoaA.getId(), pessoaB.getId());
        }

        return false;
    }
}
