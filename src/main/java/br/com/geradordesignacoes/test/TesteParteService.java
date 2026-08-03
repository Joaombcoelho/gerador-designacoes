package br.com.geradordesignacoes.test;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.ParteService;

import java.util.List;

public class TesteParteService {

    public static void executar() {

        System.out.println("\n=== TESTE PARTE SERVICE ===");

        ParteService service = new ParteService(
                new ParteDAO()
        );


        Parte parte = new Parte(

                "Leitura",
                TipoParte.LEITURA,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                true,
                List.of(TipoParticipacao.LEITOR)
        );


        Parte salva = service.salvar(parte);


        System.out.println(
                "Parte salva: "
                        + salva.getNome()
                        + " ID: "
                        + salva.getId()
        );


        List<Parte> partes = service.listarTodas();

        System.out.println(
                "Total de partes cadastradas: "
                        + partes.size()
        );


        System.out.println("=== FIM TESTE ===");
    }
}