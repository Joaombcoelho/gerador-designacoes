
package br.com.geradordesignacoes;
import br.com.geradordesignacoes.database.ConnectionFactory;

/*public class Main {
    public static void main(String[] args) {
        PessoaRepository pessoaRepository = new PessoaRepository();
        pessoaRepository.adicionar(new Pessoa("Ana", Sexo.FEMININO, true, true, true, true, false));
        pessoaRepository.adicionar(new Pessoa("Beatriz", Sexo.FEMININO, true, false, true, true, false));
        pessoaRepository.adicionar(new Pessoa("Bruno", Sexo.MASCULINO, true, true, true, true, true));
        pessoaRepository.adicionar(new Pessoa("Carlos", Sexo.MASCULINO, true, true, false, false, true));
        

        List<Parte> partes = List.of(
                new Parte("Demonstração 1", TipoParte.DEMONSTRACAO),
                new Parte("Leitura", TipoParte.LEITURA),
                new Parte("Discurso", TipoParte.DISCURSO)
        );

        GeradorEscala geradorEscala = new GeradorEscala(new RegrasService());
        List<Designacao> designacoes = geradorEscala.gerar(
                LocalDate.now(),
                partes,
                pessoaRepository.listarTodos()
        );

        designacoes.forEach(System.out::println);
    }
}*/
import java.sql.Connection;
import java.sql.SQLException;


public class Main {

    public static void main(String[] args) {
        System.out.println("Diretório atual: " + System.getProperty("user.dir"));

        try (Connection connection = ConnectionFactory.getConnection()) {

            System.out.println("Conexão com SQLite realizada com sucesso!");

        } catch (SQLException e) {

            System.out.println("Erro ao conectar:");
            e.printStackTrace();

        }

    }
}