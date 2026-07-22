package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;

import java.util.List;

public class TesteCadastroBanco {


    public static void executar() {

        System.out.println(
                "\n===== TESTE CADASTRO BANCO ====="
        );


        PessoaDAO pessoaDAO =
                new PessoaDAO();

        ParteDAO parteDAO =
                new ParteDAO();



        // Pessoas

        List<Pessoa> pessoas =
                pessoaDAO.listarTodos();


        if (pessoas.isEmpty()) {


            System.out.println(
                    "Cadastrando pessoas de teste..."
            );


            pessoaDAO.salvar(
                    new Pessoa(
                            "João Teste",
                            Sexo.MASCULINO,
                            true,
                            true,
                            true,
                            true,
                            true,
                            Privilegio.ANCIAO
                    )
            );


            pessoaDAO.salvar(
                    new Pessoa(
                            "Carlos Teste",
                            Sexo.MASCULINO,
                            true,
                            true,
                            true,
                            true,
                            true,
                            Privilegio.SERVO_MINISTERIAL
                    )
            );


            pessoaDAO.salvar(
                    new Pessoa(
                            "Lucas Teste",
                            Sexo.MASCULINO,
                            true,
                            true,
                            true,
                            true,
                            true,
                            Privilegio.SERVO_MINISTERIAL
                    )
            );


        } else {

            System.out.println(
                    "Pessoas já cadastradas: "
                            + pessoas.size()
            );
        }



        // Partes

        List<Parte> partes =
                parteDAO.listarTodos();


        if (partes.isEmpty()) {


            System.out.println(
                    "Cadastrando partes de teste..."
            );


            parteDAO.salvar(
                    new Parte(
                            null,
                            "Leitura Teste",
                            TipoParte.LEITURA,
                            Privilegio.SERVO_MINISTERIAL,
                            false,
                            SexoPermitido.AMBOS,
                            1,
                            false,
                            List.of(
                                    TipoParticipacao.LEITOR
                            )
                    )
            );


            parteDAO.salvar(
                    new Parte(
                            null,
                            "Demonstração Teste",
                            TipoParte.DEMONSTRACAO,
                            Privilegio.SERVO_MINISTERIAL,
                            true,
                            SexoPermitido.AMBOS,
                            2,
                            false,
                            List.of(
                                    TipoParticipacao.RESPONSAVEL,
                                    TipoParticipacao.AJUDANTE
                            )
                    )
            );


        } else {

            System.out.println(
                    "Partes já cadastradas: "
                            + partes.size()
            );
        }



        System.out.println(
                "===== FIM CADASTRO BANCO ====="
        );
    }
}