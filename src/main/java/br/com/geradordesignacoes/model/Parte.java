package br.com.geradordesignacoes.model;

import java.util.Objects;
import java.util.List;
import java.util.Collections;


public class Parte {
    private final String nome;
    private final TipoParte tipo;
    private final Privilegio privilegioMinimo;
    private final List<TipoParticipacao> participacoesNecessarias;

    private boolean privilegioPermitido(Pessoa pessoa) {
        return pessoa.getPrivilegio().atende(privilegioMinimo);
    }
    private final boolean exigeAjudante;
    private final SexoPermitido sexoPermitido;
    private final boolean geraFormulario;
    private final int quantidadeMinimaParticipantes;

    private boolean regrasBasicasAtendidas(Pessoa pessoa) {

        if (pessoa == null) {
            return false;
        }

        if (!pessoa.isAtivo()) {
            return false;
        }

        if (!privilegioPermitido(pessoa)) {
            return false;
        }

        return sexoPermitido(pessoa);
    }
    private boolean sexoPermitido(Pessoa pessoa) {

        if (sexoPermitido == SexoPermitido.AMBOS) {
            return true;
        }

        return (sexoPermitido == SexoPermitido.MASCULINO
                && pessoa.getSexo() == Sexo.MASCULINO)
                ||
                (sexoPermitido == SexoPermitido.FEMININO
                        && pessoa.getSexo() == Sexo.FEMININO);
    }


    public Parte(
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            List<TipoParticipacao> participacoesNecessarias) {

        this.nome = Objects.requireNonNull(nome);
        this.tipo = Objects.requireNonNull(tipo);
        this.privilegioMinimo = Objects.requireNonNull(privilegioMinimo);
        this.exigeAjudante = exigeAjudante;
        this.sexoPermitido = Objects.requireNonNull(sexoPermitido);
        this.quantidadeMinimaParticipantes = quantidadeMinimaParticipantes;
        this.geraFormulario = geraFormulario;
        this.participacoesNecessarias =
                List.copyOf(participacoesNecessarias);
    }

    public String getNome() {
        return nome;
    }

    public TipoParte getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return nome;
    }

    public Privilegio getPrivilegioMinimo() {
        return privilegioMinimo;
    }

    public boolean getExigeAjudante() {
        return exigeAjudante;
    }

    public SexoPermitido getSexoPermitido() {
        return sexoPermitido;
    }

    public int getQuantidadeMinimaParticipantes() {
        return quantidadeMinimaParticipantes;
    }

    public boolean podeSerRealizadaPor(Pessoa pessoa) {
        return regrasBasicasAtendidas(pessoa);
    }

    public boolean geraFormulario() {
        return geraFormulario;
    }

    public List<TipoParticipacao> getParticipacoesNecessarias() {
        return Collections.unmodifiableList(participacoesNecessarias);
    }

    public boolean necessitaParticipacao(TipoParticipacao tipo) {
        return participacoesNecessarias.contains(tipo);
    }

    public boolean pessoaPodeExercerParticipacao(
            Pessoa pessoa,
            TipoParticipacao tipo) {

        if (tipo == null) {
            return false;
        }

        if (!participacoesNecessarias.contains(tipo)) {
            return false;
        }

        return regrasBasicasAtendidas(pessoa)
                && pessoa.podeExercer(tipo);
    }


}
