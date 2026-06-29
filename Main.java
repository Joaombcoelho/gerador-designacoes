import java.time.LocalDate;
import java.util.List;
import model.Designacao;
import model.Parte;
import model.Pessoa;
import model.Sexo;
import model.TipoParte;
import repository.PessoaRepository;
import service.GeradorEscala;
import service.RegrasService;

public class Main {
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
}
