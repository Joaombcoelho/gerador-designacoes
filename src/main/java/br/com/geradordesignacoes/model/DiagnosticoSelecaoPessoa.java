package br.com.geradordesignacoes.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DiagnosticoSelecaoPessoa {

    private final Parte parte;
    private final List<ResultadoAvaliacaoPessoa> candidatos;
    private final ResultadoAvaliacaoPessoa escolhido;

    public DiagnosticoSelecaoPessoa(
            Parte parte,
            List<ResultadoAvaliacaoPessoa> candidatos,
            ResultadoAvaliacaoPessoa escolhido
    ) {
        this.parte = parte;
        this.candidatos = candidatos;
        this.escolhido = escolhido;
    }

    public Parte getParte() {
        return parte;
    }

    public List<ResultadoAvaliacaoPessoa> getCandidatos() {
        return candidatos;
    }

    public ResultadoAvaliacaoPessoa getEscolhido() {
        return escolhido;
    }


    @Override
    public String toString() {

        StringBuilder texto = new StringBuilder();

        texto.append("\n=== ANÁLISE DA ESCOLHA ===\n\n");

        texto.append("Parte: ")
                .append(parte.getNome())
                .append("\n\n");


        texto.append("RANKING DE CANDIDATOS:\n\n");


        List<ResultadoAvaliacaoPessoa> ranking =
                new ArrayList<>(candidatos);


        ranking.sort(
                Comparator.comparingInt(
                        ResultadoAvaliacaoPessoa::getTotal
                ).reversed()
        );


        int posicao = 1;


        for (ResultadoAvaliacaoPessoa resultado : ranking) {

            texto.append(posicao)
                    .append("º lugar\n");

            texto.append(resultado)
                    .append("\n");

            posicao++;
        }


        texto.append("\nESCOLHIDO:\n\n");


        if (escolhido != null) {

            texto.append(escolhido);

            texto.append("\n\nMotivo: Maior pontuação geral.");

        } else {

            texto.append("Nenhum candidato encontrado.");
        }


        return texto.toString();
    }
}