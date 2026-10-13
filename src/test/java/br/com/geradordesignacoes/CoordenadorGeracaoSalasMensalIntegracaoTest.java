
package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoSalasDAO;
import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CoordenadorGeracaoSalasMensalIntegracaoTest
        extends BaseDAOTest {

    private ConfiguracaoSalasDAO configuracaoDAO;
    private HistoricoDesignacoesDAO historicoDAO;
    private PessoaDAO pessoaDAO;
    private ParteDAO parteDAO;

    private List<Pessoa> pessoas;
    private List<Parte> partes;

    private final List<LocalDate> datas = List.of(
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 8),
            LocalDate.of(2026, 7, 15),
            LocalDate.of(2026, 7, 22),
            LocalDate.of(2026, 7, 29)
    );

    @BeforeEach
    void configurar() {
        configuracaoDAO = new ConfiguracaoSalasDAO();
        historicoDAO = new HistoricoDesignacoesDAO();
        pessoaDAO = new PessoaDAO();
        parteDAO = new ParteDAO();

        pessoas = criarPessoas(20);
        partes = criarPartes();
    }

    @Test
    void deveGerarAsCincoDatasNasTresSalas() {
        configuracaoDAO.salvar(
                new ConfiguracaoSalas(true, true)
        );

        CoordenadorGeracaoSalasService coordenador =
                criarCoordenador();

        for (LocalDate data : datas) {
            ResultadoGeracaoSalasCoordenadas resultado =
                    coordenador.gerar(data, partes, pessoas);

            assertFalse(
                    resultado.possuiErros(),
                    "Pendências em " + data + ": "
                            + resultado.erros()
            );

            assertTrue(
                    resultado.participacoesDaData().stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.PRINCIPAL),
                    "Faltou a Sala Principal em " + data
            );

            assertTrue(
                    resultado.participacoesDaData().stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.B),
                    "Faltou a Sala B em " + data
            );

            assertTrue(
                    resultado.participacoesDaData().stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.C),
                    "Faltou a Sala C em " + data
            );

            assertTrue(
                    resultado.participacoesDaData().stream()
                            .allMatch(p -> p.data().equals(data)),
                    "O resultado contém participação de outra data."
            );
        }

        List<ParticipacaoDesignacao> historico =
                historicoDAO.listarTodas();

        for (LocalDate data : datas) {
            List<ParticipacaoDesignacao> participacoesDaData =
                    historico.stream()
                            .filter(p -> p.data().equals(data))
                            .toList();

            assertTrue(
                    participacoesDaData.stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.PRINCIPAL),
                    "Histórico sem Sala Principal em " + data
            );

            assertTrue(
                    participacoesDaData.stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.B),
                    "Histórico sem Sala B em " + data
            );

            assertTrue(
                    participacoesDaData.stream()
                            .anyMatch(p ->
                                    p.sala() == SalaDesignacao.C),
                    "Histórico sem Sala C em " + data
            );
        }
    }

    @Test
    void deveRespeitarConfiguracaoGlobalNasCincoDatas() {
        configuracaoDAO.salvar(
                new ConfiguracaoSalas(false, false)
        );

        CoordenadorGeracaoSalasService coordenador =
                criarCoordenador();

        for (LocalDate data : datas) {
            ResultadoGeracaoSalasCoordenadas resultado =
                    coordenador.gerar(data, partes, pessoas);

            assertTrue(
                    resultado.designacoesAuxiliares().isEmpty(),
                    "Não deveria haver designações auxiliares em "
                            + data
            );

            assertTrue(
                    resultado.participacoesDaData().stream()
                            .allMatch(p ->
                                    p.sala() == SalaDesignacao.PRINCIPAL),
                    "Foi encontrada participação auxiliar em " + data
            );
        }

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .allMatch(p ->
                                p.sala() == SalaDesignacao.PRINCIPAL),
                "O histórico contém salas auxiliares desabilitadas."
        );
    }

    @Test
    void devePreservarHistoricoDasOutrasDatasAoRegenerarUmaDataDoMes() {
        configuracaoDAO.salvar(
                new ConfiguracaoSalas(true, true)
        );

        CoordenadorGeracaoSalasService coordenador =
                criarCoordenador();

        for (LocalDate data : datas) {
            coordenador.gerar(data, partes, pessoas);
        }

        Map<LocalDate, Long> quantidadesAntes =
                historicoDAO.listarTodas().stream()
                        .filter(p -> datas.contains(p.data()))
                        .collect(Collectors.groupingBy(
                                ParticipacaoDesignacao::data,
                                Collectors.counting()
                        ));

        assertEquals(
                5,
                quantidadesAntes.size(),
                "As cinco datas deveriam estar no histórico."
        );

        LocalDate dataRegenerada = datas.get(2);

        coordenador.gerar(dataRegenerada, partes, pessoas);

        List<ParticipacaoDesignacao> historicoDepois =
                historicoDAO.listarTodas();

        for (LocalDate data : datas) {
            long quantidadeDepois =
                    historicoDepois.stream()
                            .filter(p -> p.data().equals(data))
                            .count();

            assertEquals(
                    quantidadesAntes.get(data).longValue(),
                    quantidadeDepois,
                    "A quantidade de participações mudou indevidamente em "
                            + data
            );
        }

        Set<LocalDate> datasPersistidas =
                historicoDepois.stream()
                        .map(ParticipacaoDesignacao::data)
                        .filter(datas::contains)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.copyOf(datas),
                datasPersistidas,
                "A regeneração removeu uma das datas do mês."
        );
    }

    private CoordenadorGeracaoSalasService criarCoordenador() {
        return new CoordenadorGeracaoSalasService(
                new GeradorEscala(new RegrasService()),
                new GeradorSalasAuxiliaresService(),
                new ConfiguracaoSalasService(configuracaoDAO),
                new HistoricoDesignacoesService(historicoDAO)
        );
    }

    private List<Pessoa> criarPessoas(int quantidade) {
        return java.util.stream.IntStream.rangeClosed(1, quantidade)
                .mapToObj(indice ->
                        pessoaDAO.salvar(
                                new Pessoa(
                                        "Pessoa Teste " + indice,
                                        Sexo.MASCULINO,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        true,
                                        Privilegio.ANCIAO,
                                        NivelLeitura.BASICO,
                                        true,
                                        true,
                                        true
                                )
                        )
                )
                .toList();
    }

    private List<Parte> criarPartes() {
        Parte leitura = parteDAO.salvar(
                new Parte(
                        "Leitura",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        return List.of(leitura);
    }
}
