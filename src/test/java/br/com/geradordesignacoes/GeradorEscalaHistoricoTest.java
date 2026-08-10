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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GeradorEscalaHistoricoTest {


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
    void deveConsiderarHistoricoNaNovaGeracao() {


        List<Pessoa> pessoas =
                criarPessoas();


        List<Parte> partes =
                criarPartes();


        GeradorEscala gerador =
                new GeradorEscala(
                        new RegrasService()
                );


        ResultadoGeracaoEscala primeira =
                gerador.gerar(
                        LocalDate.of(2026, 7, 28),
                        partes,
                        pessoas
                );


        assertFalse(
                primeira.possuiErros(),
                "Erros na primeira geração: " + primeira.getErros()
        );


        ResultadoGeracaoEscala segunda =
                gerador.gerar(
                        LocalDate.of(2026, 8, 4),
                        partes,
                        pessoas,
                        primeira.getParticipacoes()
                );


        assertFalse(
                segunda.possuiErros(),
                "Erros na segunda geração: " + segunda.getErros()
        );


        assertTrue(
                segunda.getParticipacoes()
                        .size()
                        >
                        primeira.getParticipacoes()
                                .size()
        );
    }


    @Test
    void deveManterHistoricoPersistidoDeOutraData() {

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();
        GeradorEscala gerador = new GeradorEscala(new RegrasService());

        ResultadoGeracaoEscala primeira =
                gerador.gerar(
                        LocalDate.of(2026, 7, 28),
                        partes,
                        pessoas
                );

        assertFalse(primeira.possuiErros());

        ResultadoGeracaoEscala segunda =
                gerador.gerar(
                        LocalDate.of(2026, 8, 4),
                        partes,
                        pessoas,
                        new HistoricoDesignacoes(
                                historicoDAO.listarTodas()
                        )
                );

        assertFalse(segunda.possuiErros());

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertEquals(
                primeira.getParticipacoes().size(),
                contarPorData(
                        historicoPersistido,
                        LocalDate.of(2026, 7, 28)
                )
        );

        assertTrue(
                contarPorData(
                        historicoPersistido,
                        LocalDate.of(2026, 8, 4)
                ) > 0
        );
    }


    @Test
    void deveSubstituirSomenteHistoricoDaMesmaDataAoRegenerar() {

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();
        LocalDate data = LocalDate.of(2026, 7, 28);

        ParticipacaoDesignacao historicoAntigo =
                new ParticipacaoDesignacao(
                        data,
                        pessoas.get(0),
                        partes.get(0),
                        TipoParticipacao.LEITOR
                );

        historicoDAO.salvar(historicoAntigo);

        GeradorEscala gerador = new GeradorEscala(new RegrasService());

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        data,
                        partes,
                        pessoas,
                        new HistoricoDesignacoes(
                                historicoDAO.listarTodas()
                        )
                );

        assertFalse(resultado.possuiErros());

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertEquals(
                resultado.getParticipacoes().size(),
                contarPorData(historicoPersistido, data)
        );

        assertFalse(
                historicoPersistido.stream()
                        .anyMatch(participacao ->
                                participacao.getData().equals(data)
                                        && participacao.getPessoa().equals(
                                        historicoAntigo.getPessoa()
                                )
                                        && participacao.getParte().equals(
                                        historicoAntigo.getParte()
                                )
                                        && participacao.getTipoParticipacao()
                                        == historicoAntigo.getTipoParticipacao()
                        )
        );
    }


    @Test
    void devePreservarOutrasDatasAoRegenerarUmaData() {

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();
        LocalDate dataPreservada = LocalDate.of(2026, 7, 21);
        LocalDate dataRegenerada = LocalDate.of(2026, 7, 28);

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        dataPreservada,
                        pessoas.get(0),
                        partes.get(0),
                        TipoParticipacao.LEITOR
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        dataRegenerada,
                        pessoas.get(0),
                        partes.get(0),
                        TipoParticipacao.LEITOR
                )
        );

        GeradorEscala gerador = new GeradorEscala(new RegrasService());

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        dataRegenerada,
                        partes,
                        pessoas,
                        new HistoricoDesignacoes(
                                historicoDAO.listarTodas()
                        )
                );

        assertFalse(resultado.possuiErros());

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertEquals(
                1,
                contarPorData(historicoPersistido, dataPreservada)
        );

        assertEquals(
                resultado.getParticipacoes().size() - 1,
                contarPorData(historicoPersistido, dataRegenerada)
        );
    }


    @Test
    void pessoaInativaNaoParticipaDeNovaGeracaoMasHistoricoPermanece() {

        List<Pessoa> pessoas = new ArrayList<>(criarPessoas());
        Pessoa inativo =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Inativo Histórico",
                                Sexo.MASCULINO,
                                false,
                                true,
                                false,
                                true,
                                true,
                                Privilegio.ANCIAO
                        )
                );
        pessoas.add(0, inativo);

        List<Parte> partes = criarPartes();
        LocalDate dataHistorica = LocalDate.of(2026, 7, 21);
        LocalDate novaData = LocalDate.of(2026, 8, 4);

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        dataHistorica,
                        inativo,
                        partes.get(0),
                        TipoParticipacao.LEITOR
                )
        );

        GeradorEscala gerador = new GeradorEscala(new RegrasService());

        ResultadoGeracaoEscala resultado =
                gerador.gerar(
                        novaData,
                        partes,
                        pessoas,
                        new HistoricoDesignacoes(
                                historicoDAO.listarTodas()
                        )
                );

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .anyMatch(participacao ->
                                participacao.getData().equals(dataHistorica)
                                        && participacao.getPessoa().equals(inativo)
                        )
        );

        assertFalse(
                resultado.getEscala().getDesignacoes().stream()
                        .anyMatch(designacao ->
                                inativo.equals(designacao.getResponsavel())
                                        || inativo.equals(designacao.getAjudante())
                        )
        );
    }


    @Test
    void historicoAnteriorNaoBloqueiaPessoaNaNovaGeracao() {

        Pessoa pessoa =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Leitor Único",
                                Sexo.MASCULINO,
                                true,
                                false,
                                false,
                                true,
                                false,
                                Privilegio.PUBLICADOR
                        )
                );
        Parte parte = criarPartes().get(0);
        LocalDate dataHistorica = LocalDate.of(2026, 7, 21);
        LocalDate novaData = LocalDate.of(2026, 7, 28);

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        dataHistorica,
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        ResultadoGeracaoEscala resultado =
                new GeradorEscala(new RegrasService())
                        .gerar(
                                novaData,
                                List.of(parte),
                                List.of(pessoa),
                                new HistoricoDesignacoes(
                                        historicoDAO.listarTodas()
                                )
                        );

        assertFalse(resultado.possuiErros());
        assertEquals(
                pessoa,
                resultado.getEscala().getDesignacoes().get(0).getResponsavel()
        );
    }


    @Test
    void rodizioContinuaConsiderandoHistoricoParaPrioridade() {

        Pessoa pessoaComHistorico =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Leitor Com Histórico",
                                Sexo.MASCULINO,
                                true,
                                false,
                                false,
                                true,
                                false,
                                Privilegio.PUBLICADOR
                        )
                );
        Pessoa pessoaSemHistorico =
                pessoaDAO.salvar(
                        new Pessoa(
                                "Leitor Sem Histórico",
                                Sexo.MASCULINO,
                                true,
                                false,
                                false,
                                true,
                                false,
                                Privilegio.PUBLICADOR
                        )
                );
        Parte parte = criarPartes().get(0);

        HistoricoDesignacoes historico =
                new HistoricoDesignacoes(
                        List.of(
                                new ParticipacaoDesignacao(
                                        LocalDate.of(2026, 7, 21),
                                        pessoaComHistorico,
                                        parte,
                                        TipoParticipacao.LEITOR
                                )
                        )
                );

        ResultadoGeracaoEscala resultado =
                new GeradorEscala(new RegrasService())
                        .gerar(
                                LocalDate.of(2026, 7, 28),
                                List.of(parte),
                                List.of(
                                        pessoaComHistorico,
                                        pessoaSemHistorico
                                ),
                                historico
                        );

        assertFalse(resultado.possuiErros());
        assertEquals(
                pessoaSemHistorico,
                resultado.getEscala().getDesignacoes().get(0).getResponsavel()
        );
    }


    private long contarPorData(
            List<ParticipacaoDesignacao> participacoes,
            LocalDate data
    ) {

        return participacoes.stream()
                .filter(participacao ->
                        participacao.getData().equals(data)
                )
                .count();
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