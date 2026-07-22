package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;

import java.time.LocalDate;
import java.util.List;

public class TesteHistoricoInfluenciaSelecao {

    public static void executar() {

        System.out.println(
                "\n===== TESTE INFLUÊNCIA DO HISTÓRICO ====="
        );

        PessoaDAO pessoaDAO = new PessoaDAO();
        ParteDAO parteDAO = new ParteDAO();

        List<Pessoa> pessoas = pessoaDAO.listarTodos();
        List<Parte> partes = parteDAO.listarTodos();

        System.out.println("\n=== PESSOAS CADASTRADAS ===");

        for (Pessoa pessoa : pessoas) {
            System.out.println(
                    "ID=" + pessoa.getId()
                            + " | Nome=" + pessoa.getNome()
                            + " | Privilégio=" + pessoa.getPrivilegio()
            );
        }

        System.out.println("\n=== PARTES CADASTRADAS ===");

        for (Parte parte : partes) {
            System.out.println(
                    "ID=" + parte.getId()
                            + " | Nome=" + parte.getNome()
                            + " | Tipo=" + parte.getTipo()
            );
        }

        if (pessoas.size() < 3) {
            throw new RuntimeException(
                    "Cadastre pelo menos 3 pessoas no banco."
            );
        }

        if (partes.isEmpty()) {
            throw new RuntimeException(
                    "Cadastre pelo menos uma parte no banco."
            );
        }

        RegrasService regrasService =
                new RegrasService();

        GeradorEscala gerador =
                new GeradorEscala(
                        regrasService
                );

        Parte parte = partes.stream()
                .filter(p -> p.getTipo() == TipoParte.LEITURA)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("Nenhuma parte de leitura encontrada."));

        System.out.println(
                "\nParte utilizada: "
                        + parte.getNome()
                        + " (ID="
                        + parte.getId()
                        + ")"
        );

        System.out.println(
                "\n=== PRIMEIRA GERAÇÃO ==="
        );

        ResultadoGeracaoEscala resultado1 =
                gerador.gerar(
                        LocalDate.now(),
                        List.of(parte),
                        pessoas
                );

        imprimirResultado(resultado1);

        System.out.println(
                "\n=== SEGUNDA GERAÇÃO ==="
        );

        ResultadoGeracaoEscala resultado2 =
                gerador.gerar(
                        LocalDate.now().plusDays(7),
                        List.of(parte),
                        pessoas
                );

        imprimirResultado(resultado2);

        System.out.println(
                "\n===== FIM TESTE ====="
        );
    }

    private static void imprimirResultado(
            ResultadoGeracaoEscala resultado
    ) {

            if (resultado.getDesignacoes().isEmpty()) {
                System.out.println("Nenhuma designação gerada.");
            }

            for (Designacao designacao : resultado.getDesignacoes()) {

                System.out.println(
                        "Parte: " + designacao.getParte().getNome()
                );

                System.out.println(
                        "Pessoa escolhida: "
                                + designacao.getResponsavel().getNome()
                );
            }

            if (!resultado.getErros().isEmpty()) {

                System.out.println("\nErros:");

                resultado.getErros().forEach(System.out::println);
            }
        }
    }