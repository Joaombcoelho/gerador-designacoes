package br.com.geradordesignacoes.model;import java.util.Objects;
import java.util.List;
import java.util.Collections;


public class Parte {

    private final Integer id;
    private final String nome;
    private final TipoParte tipo;
    private final Privilegio privilegioMinimo;
    private final List<TipoParticipacao> participacoesNecessarias;

    private final boolean exigeAjudante;
    private final SexoPermitido sexoPermitido;
    private final boolean geraFormulario;
    private final int quantidadeMinimaParticipantes;


    private boolean privilegioPermitido(Pessoa pessoa) {
        return pessoa.getPrivilegio().atende(privilegioMinimo);
    }


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
            Integer id,
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            List<TipoParticipacao> participacoesNecessarias) {

        this.id = id;
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


    public Integer getId() {
        return id;
    }


    public String getNome() {
        return nome;
    }


    public TipoParte getTipo() {
        return tipo;
    }


    @Override
    public String toString() {
        return nome + (id != null ? " [id=" + id + "]" : "");
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

    public Parte(
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            List<TipoParticipacao> participacoesNecessarias) {

        this(
                null,
                nome,
                tipo,
                privilegioMinimo,
                exigeAjudante,
                sexoPermitido,
                quantidadeMinimaParticipantes,
                geraFormulario,
                participacoesNecessarias
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Parte outra)) {
            return false;
        }

        if (id == null || outra.id == null) {
            return false;
        }

        return id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}