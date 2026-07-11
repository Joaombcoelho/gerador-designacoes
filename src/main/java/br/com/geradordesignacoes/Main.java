package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.Pessoa;
import br.com.geradordesignacoes.model.Sexo;

import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        PessoaDAO pessoaDAO = new PessoaDAO();


        // ============================
        // TESTE SALVAR
        // ============================

        Pessoa pessoa = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false
        );

        pessoaDAO.salvar(pessoa);

        System.out.println("Pessoa salva com ID: " + pessoa.getId());


        // ============================
        // TESTE LISTAR TODOS
        // ============================

        System.out.println("\nLista de pessoas:");

        for (Pessoa p : pessoaDAO.listarTodos()) {

            System.out.println("-------------------------");
            System.out.println("ID: " + p.getId());
            System.out.println("Nome: " + p.getNome());
            System.out.println("Sexo: " + p.getSexo());
            System.out.println("Ativo: " + p.isAtivo());
            System.out.println("Responsável: " + p.podeSerResponsavel());
            System.out.println("Ajudante: " + p.podeSerAjudante());
            System.out.println("Leitura: " + p.podeFazerLeitura());
            System.out.println("Discurso: " + p.podeFazerDiscurso());
        }


        // ============================
        // TESTE BUSCAR POR ID
        // ============================

        System.out.println("\nBuscando pessoa pelo ID 50:");

        Optional<Pessoa> pessoaEncontrada = pessoaDAO.buscarPorId(50);

        if (pessoaEncontrada.isPresent()) {

            Pessoa p = pessoaEncontrada.get();

            System.out.println("Encontrada:");
            System.out.println("ID: " + p.getId());
            System.out.println("Nome: " + p.getNome());

        } else {

            System.out.println("Pessoa não encontrada.");
        }


        // ============================
        // TESTE ATUALIZAR
        // ============================

        System.out.println("\nTeste de atualização:");

        Pessoa pessoaAlterada = new Pessoa(
                "João Alterado",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false
        );

        pessoaAlterada.setId(1);

        pessoaDAO.atualizar(pessoaAlterada);


        // ============================
        // CONFIRMAR ALTERAÇÃO
        // ============================

        Optional<Pessoa> pessoaDepoisAtualizacao =
                pessoaDAO.buscarPorId(1);

        if (pessoaDepoisAtualizacao.isPresent()) {

            Pessoa p = pessoaDepoisAtualizacao.get();

            System.out.println("Após atualização:");
            System.out.println("ID: " + p.getId());
            System.out.println("Nome: " + p.getNome());

        } else {

            System.out.println("Pessoa não encontrada após atualização.");
        }
        // ============================
// TESTE EXCLUIR
// ============================

        System.out.println("\nTeste de exclusão:");

        Pessoa pessoaParaExcluir = new Pessoa(
                "Pessoa Teste Exclusao",
                Sexo.FEMININO,
                true,
                false,
                true,
                true,
                false
        );

        pessoaDAO.salvar(pessoaParaExcluir);

        Integer idExclusao = pessoaParaExcluir.getId();

        System.out.println("Pessoa criada para exclusão. ID: " + idExclusao);


// Executa exclusão

        pessoaDAO.excluir(idExclusao);

        System.out.println("Pessoa excluída.");


// Confirma se foi removida

        Optional<Pessoa> pessoaRemovida = pessoaDAO.buscarPorId(idExclusao);

        if (pessoaRemovida.isPresent()) {

            System.out.println("ERRO: Pessoa ainda existe no banco.");

        } else {

            System.out.println("Sucesso: Pessoa não encontrada após exclusão.");
        }
    }
}