package br.com.geradordesignacoes.model;

public class ResultadoAvaliacaoPessoa {

    private final Pessoa pessoa;

    private final int pontosParticipacoes;

    private final int pontosPrivilegio;

    private final int penalidadeRepeticao;

    private final int total;


    public ResultadoAvaliacaoPessoa(
            Pessoa pessoa,
            int pontosParticipacoes,
            int pontosPrivilegio,
            int penalidadeRepeticao
    ) {

        this.pessoa = pessoa;
        this.pontosParticipacoes = pontosParticipacoes;
        this.pontosPrivilegio = pontosPrivilegio;
        this.penalidadeRepeticao = penalidadeRepeticao;

        this.total =
                pontosParticipacoes
                        + pontosPrivilegio
                        - penalidadeRepeticao;
    }


    public Pessoa getPessoa() {
        return pessoa;
    }


    public int getPontosParticipacoes() {
        return pontosParticipacoes;
    }


    public int getPontosPrivilegio() {
        return pontosPrivilegio;
    }


    public int getPenalidadeRepeticao() {
        return penalidadeRepeticao;
    }


    public int getTotal() {
        return total;
    }


    @Override
    public String toString() {

        return """
                Pessoa: %s
                
                Participações: %d
                Privilégio: %d
                Penalidade repetição: -%d
                
                TOTAL: %d
                """.formatted(
                pessoa.getNome(),
                pontosParticipacoes,
                pontosPrivilegio,
                penalidadeRepeticao,
                total
        );
    }
}