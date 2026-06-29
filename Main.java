import java.time.LocalDate;
import java.util.List;
import model.Designacao;
import model.Parte;
import model.Pessoa;
import repository.PessoaRepository;
import service.GeradorEscala;
import service.RegrasService;

public class Main {
    public static void main(String[] args) {
        PessoaRepository pessoaRepository = new PessoaRepository();
        pessoaRepository.adicionar(new Pessoa("Ana", "F", true, true, true, false));
        pessoaRepository.adicionar(new Pessoa("Bruno", "M", true, true, true, true));

        List<Parte> partes = List.of(
                new Parte("Leitura"),
                new Parte("Discurso")
        );

        GeradorEscala geradorEscala = new GeradorEscala(new RegrasService());
        List<Designacao> designacoes = geradorEscala.gerar(
                LocalDate.now(),
                partes,
                pessoaRepository.listarTodos()
        );

        designacoes.forEach(System.out::println);
    }
}
