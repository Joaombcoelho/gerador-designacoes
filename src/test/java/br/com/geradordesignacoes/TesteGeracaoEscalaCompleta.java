package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TesteGeracaoEscalaCompleta {


    private PessoaDAO pessoaDAO;
    private ParteDAO parteDAO;
    private HistoricoDesignacoesDAO historicoDAO;


    @BeforeEach
    void configurar() {

        pessoaDAO = new PessoaDAO();
        parteDAO = new ParteDAO();
        historicoDAO = new HistoricoDesignacoesDAO();

        historicoDAO.limpar();
    }


    @Test
    void deveGerarEscalaCompletaComPersistencia() {


        List<Pessoa> pessoas =
                criarPessoas();


        List<Parte> partes =
                criarPartes();


        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );


        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        LocalDate.of(2026, 7, 28),
                        partes,
                        pessoas
                );


        assertFalse(
                resultado.possuiErros(),
                "A geração não deveria possuir erros"
        );


        assertEquals(
                3,
                resultado.getDesignacoes().size()
        );


        assertEquals(
                4,
                resultado.getParticipacoes().size()
        );


        assertFalse(
                historicoDAO.listarTodas().isEmpty()
        );
    }
    private List<Pessoa> criarPessoas() {

        Pessoa joao =
                pessoaDAO.salvar(
                        new Pessoa(
                                "João",
                                Sexo.MASCULINO,
                                true,
                                true,
                                false,
                                false,
                                true,
                                Privilegio.ANCIAO
                        )
                );

        Pessoa carlos =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Carlos",
                                Sexo.MASCULINO,
                                true,
                                false,
                                false,
                                true,
                                false,
                                Privilegio.SERVO_MINISTERIAL
                        )
                );

        Pessoa lucas =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Lucas",
                                Sexo.MASCULINO,
                                true,
                                true,
                                false,
                                false,
                                false,
                                Privilegio.SERVO_MINISTERIAL
                        )
                );

        Pessoa pedro =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Pedro",
                                Sexo.MASCULINO,
                                true,
                                false,
                                true,
                                false,
                                false,
                                Privilegio.SERVO_MINISTERIAL
                        )
                );


        return List.of(
                joao,
                carlos,
                lucas,
                pedro
        );
    }
    private List<Parte> criarPartes() {

        Parte leitura =
                parteDAO.salvar(
                        new Parte(
                                "Leitura",
                                TipoParte.LEITURA,
                                Privilegio.PUBLICADOR,
                                false,
                                SexoPermitido.MASCULINO,
                                1,
                                false,
                                List.of(
                                        TipoParticipacao.LEITOR
                                )
                        )
                );


        Parte discurso =
                parteDAO.salvar(
                        new Parte(
                                "Discurso",
                                TipoParte.DISCURSO,
                                Privilegio.ANCIAO,
                                false,
                                SexoPermitido.MASCULINO,
                                1,
                                false,
                                List.of(
                                        TipoParticipacao.ORADOR
                                )
                        )
                );


        Parte demonstracao =
                parteDAO.salvar(
                        new Parte(
                                "Demonstração",
                                TipoParte.DEMONSTRACAO,
                                Privilegio.SERVO_MINISTERIAL,
                                true,
                                SexoPermitido.MASCULINO,
                                2,
                                false,
                                List.of(
                                        TipoParticipacao.RESPONSAVEL,
                                        TipoParticipacao.AJUDANTE
                                )
                        )
                );


        return List.of(
                leitura,
                discurso,
                demonstracao
        );
    }
}