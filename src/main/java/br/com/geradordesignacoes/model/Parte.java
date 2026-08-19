package br.com.geradordesignacoes.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
    private final NivelLeitura nivelLeituraMinimo;

    /*
     * Classificação utilizada na programação semanal.
     */
    private final SecaoParte secao;
    private final TipoVariacaoParte tipoVariacao;

    /*
     * Indica se a parte precisa receber um tema
     * durante a programação da semana.
     */
    private final boolean possuiTema;


    /**
     * Construtor principal.
     */
    public Parte(
            Integer id,
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            NivelLeitura nivelLeituraMinimo,
            SecaoParte secao,
            TipoVariacaoParte tipoVariacao,
            boolean possuiTema,
            List<TipoParticipacao> participacoesNecessarias
    ) {

        this.id = id;

        this.nome = Objects.requireNonNull(nome);
        this.tipo = Objects.requireNonNull(tipo);

        this.privilegioMinimo =
                Objects.requireNonNull(privilegioMinimo);

        this.exigeAjudante = exigeAjudante;

        this.sexoPermitido =
                Objects.requireNonNull(sexoPermitido);

        this.quantidadeMinimaParticipantes =
                quantidadeMinimaParticipantes;

        this.geraFormulario =
                geraFormulario;

        this.nivelLeituraMinimo =
                Objects.requireNonNull(nivelLeituraMinimo);

        this.secao = secao;

        this.tipoVariacao = tipoVariacao;

        this.possuiTema = possuiTema;

        this.participacoesNecessarias =
                List.copyOf(
                        Objects.requireNonNull(
                                participacoesNecessarias
                        )
                );
    }


    /**
     * Construtor utilizado pelo cadastro normal.
     */
    public Parte(
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            NivelLeitura nivelLeituraMinimo,
            SecaoParte secao,
            TipoVariacaoParte tipoVariacao,
            boolean possuiTema,
            List<TipoParticipacao> participacoesNecessarias
    ) {

        this(
                null,
                nome,
                tipo,
                privilegioMinimo,
                exigeAjudante,
                sexoPermitido,
                quantidadeMinimaParticipantes,
                geraFormulario,
                nivelLeituraMinimo,
                secao,
                tipoVariacao,
                possuiTema,
                participacoesNecessarias
        );
    }


    /**
     * Construtor utilizado pelo cadastro normal
     * sem classificação de seção/variação.
     *
     * Mantido para compatibilidade.
     */
    public Parte(
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            NivelLeitura nivelLeituraMinimo,
            List<TipoParticipacao> participacoesNecessarias
    ) {

        this(
                null,
                nome,
                tipo,
                privilegioMinimo,
                exigeAjudante,
                sexoPermitido,
                quantidadeMinimaParticipantes,
                geraFormulario,
                nivelLeituraMinimo,
                null,
                null,
                false,
                participacoesNecessarias
        );
    }


    /**
     * Construtor utilizado pelo DAO e mantido
     * para compatibilidade com o cadastro atual.
     */
    public Parte(
            Integer id,
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            NivelLeitura nivelLeituraMinimo,
            List<TipoParticipacao> participacoesNecessarias
    ) {

        this(
                id,
                nome,
                tipo,
                privilegioMinimo,
                exigeAjudante,
                sexoPermitido,
                quantidadeMinimaParticipantes,
                geraFormulario,
                nivelLeituraMinimo,
                null,
                null,
                false,
                participacoesNecessarias
        );
    }


    /**
     * Construtor legado.
     *
     * Mantido para compatibilidade
     * com testes antigos.
     */
    public Parte(
            String nome,
            TipoParte tipo,
            Privilegio privilegioMinimo,
            boolean exigeAjudante,
            SexoPermitido sexoPermitido,
            int quantidadeMinimaParticipantes,
            boolean geraFormulario,
            List<TipoParticipacao> participacoesNecessarias
    ) {

        this(
                null,
                nome,
                tipo,
                privilegioMinimo,
                exigeAjudante,
                sexoPermitido,
                quantidadeMinimaParticipantes,
                geraFormulario,
                NivelLeitura.BASICO,
                null,
                null,
                false,
                participacoesNecessarias
        );
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


    public boolean geraFormulario() {
        return geraFormulario;
    }


    public NivelLeitura getNivelLeituraMinimo() {
        return nivelLeituraMinimo;
    }


    public SecaoParte getSecao() {
        return secao;
    }


    public TipoVariacaoParte getTipoVariacao() {
        return tipoVariacao;
    }


    public boolean possuiTema() {
        return possuiTema;
    }


    public List<TipoParticipacao> getParticipacoesNecessarias() {

        return Collections.unmodifiableList(
                participacoesNecessarias
        );
    }


    public boolean necessitaParticipacao(
            TipoParticipacao tipo
    ) {

        return participacoesNecessarias.contains(tipo);
    }


    public boolean podeSerRealizadaPor(
            Pessoa pessoa
    ) {

        return regrasBasicasAtendidas(pessoa);
    }


    public boolean pessoaPodeExercerParticipacao(
            Pessoa pessoa,
            TipoParticipacao tipo
    ) {

        if (tipo == null) {
            return false;
        }

        if (!participacoesNecessarias.contains(tipo)) {
            return false;
        }

        if (!regrasBasicasAtendidas(pessoa)) {
            return false;
        }

        if (tipo == TipoParticipacao.LEITOR) {

            return nivelLeituraPermitido(pessoa);
        }

        return pessoa.podeExercer(tipo);
    }


    private boolean regrasBasicasAtendidas(
            Pessoa pessoa
    ) {

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


    private boolean privilegioPermitido(
            Pessoa pessoa
    ) {

        return pessoa.getPrivilegio()
                .atende(privilegioMinimo);
    }


    private boolean sexoPermitido(
            Pessoa pessoa
    ) {

        if (sexoPermitido == SexoPermitido.AMBOS) {
            return true;
        }

        return
                (sexoPermitido == SexoPermitido.MASCULINO
                        &&
                        pessoa.getSexo() == Sexo.MASCULINO)

                        ||

                        (sexoPermitido == SexoPermitido.FEMININO
                                &&
                                pessoa.getSexo() == Sexo.FEMININO);
    }


    private boolean nivelLeituraPermitido(
            Pessoa pessoa
    ) {

        return pessoa.getNivelLeitura()
                .atende(nivelLeituraMinimo);
    }


    @Override
    public String toString() {

        return nome +
                (id != null
                        ? " [id=" + id + "]"
                        : "");
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