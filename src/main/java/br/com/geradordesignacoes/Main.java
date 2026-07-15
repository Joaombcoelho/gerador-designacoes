package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.database.DatabaseInitializer;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.PessoaService;
import br.com.geradordesignacoes.service.RegrasService;

import java.util.List;
import java.util.Optional;

public class Main {

    public static void main(String[] args) {

        DatabaseInitializer.initialize();

        System.out.println("===== TESTE PRIVILÉGIOS =====");

        System.out.println(
                "Ancião atende Servo? "
                        + Privilegio.ANCIAO.atende(Privilegio.SERVO_MINISTERIAL)
        );

        System.out.println(
                "Servo atende Batizado? "
                        + Privilegio.SERVO_MINISTERIAL.atende(Privilegio.BATIZADO)
        );

        System.out.println(
                "Batizado atende Ancião? "
                        + Privilegio.BATIZADO.atende(Privilegio.ANCIAO)
        );

        System.out.println(
                "Publicador atende Batizado? "
                        + Privilegio.PUBLICADOR.atende(Privilegio.BATIZADO)
        );

        System.out.println(
                "Ancião atende Publicador? "
                        + Privilegio.ANCIAO.atende(Privilegio.PUBLICADOR)
        );

        System.out.println();

        PessoaDAO pessoaDAO = new PessoaDAO();
        PessoaService pessoaService = new PessoaService(pessoaDAO);

        // ============================
        // TESTE SALVAR
        // ============================

        Pessoa pessoa = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                true,
                false,
                true,
                false,
                Privilegio.SERVO_MINISTERIAL
        );

        pessoaService.salvar(pessoa);

        System.out.println("Pessoa salva com ID: " + pessoa.getId());

        //============================
        // TESTE PODE EXERCER FUNÇÃO
        //============================

        System.out.println(
                "Pode exercer LEITOR: "
                        + pessoa.podeExercer(TipoParticipacao.LEITOR)
        );

        System.out.println(
                "Pode exercer AJUDANTE: "
                        + pessoa.podeExercer(TipoParticipacao.AJUDANTE)
        );

        System.out.println(
                "Pode exercer PRESIDENTE: "
                        + pessoa.podeExercer(TipoParticipacao.PRESIDENTE)
        );

        // ============================
        // TESTE LISTAR TODOS
        // ============================

        System.out.println("\nLista de pessoas:");

        for (Pessoa p : pessoaService.listarTodas()) {

            System.out.println("-------------------------");
            System.out.println("ID: " + p.getId());
            System.out.println("Nome: " + p.getNome());
            System.out.println("Sexo: " + p.getSexo());
            System.out.println("Ativo: " + p.isAtivo());
            System.out.println("Responsável: " + p.podeSerResponsavel());
            System.out.println("Ajudante: " + p.podeSerAjudante());
            System.out.println("Leitura: " + p.podeFazerLeitura());
            System.out.println("Discurso: " + p.podeFazerDiscurso());
            System.out.println("Privilégio: " + p.getPrivilegio());
        }

        // ============================
        // TESTE BUSCAR POR ID
        // ============================

        System.out.println("\nBuscando pessoa pelo ID 50:");

        Optional<Pessoa> pessoaEncontrada =
                pessoaService.buscarPorId(50);

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
                false,
                Privilegio.SERVO_MINISTERIAL
        );

        pessoaAlterada.setId(1);

        pessoaService.atualizar(pessoaAlterada);

        // ============================
        // CONFIRMAR ALTERAÇÃO
        // ============================

        Optional<Pessoa> pessoaDepoisAtualizacao = pessoaService.buscarPorId(1);

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
                false,
                Privilegio.BATIZADO
        );

        pessoaService.salvar(pessoaParaExcluir);

        Integer idExclusao = pessoaParaExcluir.getId();

        System.out.println("Pessoa criada para exclusão. ID: " + idExclusao);

        pessoaService.excluir(idExclusao);

        System.out.println("Pessoa excluída.");

        Optional<Pessoa> pessoaRemovida = pessoaService.buscarPorId(idExclusao);

        if (pessoaRemovida.isPresent()) {

            System.out.println("ERRO: Pessoa ainda existe no banco.");

        } else {

            System.out.println("Sucesso: Pessoa não encontrada após exclusão.");
        }
        System.out.println("\n===== TESTE PARTE =====");

        Pessoa pessoaBatizada = new Pessoa(
                "João",
                Sexo.MASCULINO,
                true,
                false,
                false,
                true,
                false,
                Privilegio.BATIZADO
        );

        Pessoa pessoaPublicador = new Pessoa(
                "Pedro",
                Sexo.MASCULINO,
                true,
                false,
                false,
                true,
                false,
                Privilegio.PUBLICADOR
        );

        Parte leitura = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        RegrasService regrasService = new RegrasService();
        System.out.println(
                "Pode formar demonstração: "
                        + regrasService.podeFormarDemonstracao(
                        pessoaBatizada,
                        pessoaPublicador,
                        List.of()
                )
        );
        System.out.println(
                "Batizado pode fazer leitura? "
                        + leitura.podeSerRealizadaPor(pessoaBatizada)
        );

        System.out.println(
                "Publicador pode fazer leitura? "
                        + leitura.podeSerRealizadaPor(pessoaPublicador)
        );

        System.out.println("\n===== TESTE SEXO PERMITIDO =====");

        Parte leituraMasculina = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.MASCULINO,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        Pessoa pessoaFeminina = new Pessoa(
                "Maria",
                Sexo.FEMININO,
                true,
                false,
                false,
                true,
                false,
                Privilegio.BATIZADO
        );

        Parte parteAmbos = new Parte(
                "Primeira Conversa",
                TipoParte.DEMONSTRACAO,
                Privilegio.BATIZADO,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        System.out.println(
                "Mulher pode fazer leitura masculina? "
                        + leituraMasculina.podeSerRealizadaPor(pessoaFeminina)
        );

        System.out.println(
                "Mulher pode fazer parte permitida para ambos? "
                        + parteAmbos.podeSerRealizadaPor(pessoaFeminina)
        );

        System.out.println(
                "Pessoa pode ser leitor: "
                        + leitura.pessoaPodeExercerParticipacao(
                        pessoa,
                        TipoParticipacao.LEITOR
                )
        );
    }

}