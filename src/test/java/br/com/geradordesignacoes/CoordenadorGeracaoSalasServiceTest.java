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

import static org.junit.jupiter.api.Assertions.*;

class CoordenadorGeracaoSalasServiceTest extends BaseDAOTest {


    private PessoaDAO pessoaDAO;
    private ParteDAO parteDAO;
    private HistoricoDesignacoesDAO historicoDAO;
    private ConfiguracaoSalasDAO configuracaoDAO;

    @BeforeEach
    void configurar() {
        pessoaDAO = new PessoaDAO();
        parteDAO = new ParteDAO();
        historicoDAO = new HistoricoDesignacoesDAO();
        configuracaoDAO = new ConfiguracaoSalasDAO();
    }

    @Test
    void deveGerarSomenteSalaPrincipalQuandoSalasAuxiliaresEstaoDesabilitadas() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(
                new ConfiguracaoSalas(false, false)
        );

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        CoordenadorGeracaoSalasService coordenador =
                new CoordenadorGeracaoSalasService(
                        new GeradorEscala(new RegrasService()),
                        new GeradorSalasAuxiliaresService(),
                        new ConfiguracaoSalasService(configuracaoDAO),
                        new HistoricoDesignacoesService(historicoDAO)
                );

        ResultadoGeracaoSalasCoordenadas resultado =
                coordenador.gerar(data, partes, pessoas);

        assertFalse(
                resultado.resultadoPrincipal().escala()
                        .getDesignacoes().isEmpty(),
                "A Sala Principal deveria ter designações."
        );

        assertTrue(
                resultado.designacoesAuxiliares().isEmpty(),
                "Não deveria haver designações nas salas auxiliares."
        );

        assertFalse(
                resultado.possuiErros(),
                "Erros na geração: " + resultado.erros()
        );

        assertFalse(
                resultado.participacoesDaData().isEmpty(),
                "Deveria haver participações registradas para a data."
        );

        assertTrue(
                resultado.participacoesDaData().stream()
                        .allMatch(p ->
                                p.data().equals(data)
                                        && p.sala() == SalaDesignacao.PRINCIPAL
                        ),
                "Todas as participações deveriam pertencer à Sala Principal e à data gerada."
        );

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertFalse(
                historicoPersistido.isEmpty(),
                "O histórico deveria ser persistido."
        );

        assertTrue(
                historicoPersistido.stream()
                        .allMatch(p ->
                                p.data().equals(data)
                                        && p.sala() == SalaDesignacao.PRINCIPAL
                        ),
                "O histórico persistido deveria conter somente participações da Sala Principal nessa data."
        );
    }

    @Test
    void deveGerarEDersistirDesignacoesNasSalasPrincipalBEC() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(
                new ConfiguracaoSalas(true, true)
        );

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        CoordenadorGeracaoSalasService coordenador =
                new CoordenadorGeracaoSalasService(
                        new GeradorEscala(new RegrasService()),
                        new GeradorSalasAuxiliaresService(),
                        new ConfiguracaoSalasService(configuracaoDAO),
                        new HistoricoDesignacoesService(historicoDAO)
                );

        ResultadoGeracaoSalasCoordenadas resultado =
                coordenador.gerar(data, partes, pessoas);

        assertFalse(
                resultado.possuiErros(),
                "Erros na geração: " + resultado.erros()
        );

        assertFalse(
                resultado.resultadoPrincipal().escala()
                        .getDesignacoes().isEmpty(),
                "A Sala Principal deveria ter designações."
        );

        assertEquals(
                2,
                resultado.designacoesAuxiliares().size(),
                "Deveria haver uma designação em cada sala auxiliar."
        );

        assertTrue(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.B),
                "Deveria existir uma designação na Sala B."
        );

        assertTrue(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.C),
                "Deveria existir uma designação na Sala C."
        );

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertFalse(
                historicoPersistido.isEmpty(),
                "O histórico deveria conter participações persistidas."
        );

        assertTrue(
                historicoPersistido.stream()
                        .allMatch(p -> p.data().equals(data)),
                "O histórico gerado deveria conter somente participações da data testada."
        );

        assertTrue(
                historicoPersistido.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.PRINCIPAL),
                "O histórico deveria conter participações da Sala Principal."
        );

        assertTrue(
                historicoPersistido.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.B),
                "O histórico deveria conter participações da Sala B."
        );

        assertTrue(
                historicoPersistido.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.C),
                "O histórico deveria conter participações da Sala C."
        );
    }



    @Test
    void naoDeveReutilizarParticipantesDaMesmaParteEntreAsSalas() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        CoordenadorGeracaoSalasService coordenador =
                criarCoordenador();

        ResultadoGeracaoSalasCoordenadas resultado =
                coordenador.gerar(data, partes, pessoas);

        assertFalse(resultado.possuiErros(), resultado.erros().toString());

        List<ParticipacaoDesignacao> leitores =
                resultado.participacoesDaData().stream()
                        .filter(p -> p.data().equals(data))
                        .filter(p -> p.tipoParticipacao() == TipoParticipacao.LEITOR)
                        .toList();

        assertEquals(
                3,
                leitores.size(),
                "Deveria haver um leitor para a mesma parte em cada sala."
        );

        assertEquals(
                3,
                leitores.stream()
                        .map(p -> p.pessoa().getId())
                        .distinct()
                        .count(),
                "Cada sala deve ter um participante diferente para a mesma parte."
        );

        assertTrue(leitores.stream().anyMatch(
                p -> p.sala() == SalaDesignacao.PRINCIPAL));
        assertTrue(leitores.stream().anyMatch(
                p -> p.sala() == SalaDesignacao.B));
        assertTrue(leitores.stream().anyMatch(
                p -> p.sala() == SalaDesignacao.C));
    }


    @Test
    void devePreservarHistoricoDeOutrasDatasAoGerar() {
        LocalDate dataAnterior = LocalDate.of(2026, 10, 13);
        LocalDate dataNova = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(new ConfiguracaoSalas(false, false));

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        ParticipacaoDesignacao participacaoAnterior =
                new ParticipacaoDesignacao(
                        dataAnterior,
                        pessoas.get(0),
                        partes.get(0),
                        TipoParticipacao.LEITOR,
                        SalaDesignacao.PRINCIPAL
                );

        historicoDAO.substituirParticipacoesDaData(
                dataAnterior,
                List.of(participacaoAnterior)
        );

        criarCoordenador().gerar(dataNova, partes, pessoas);

        List<ParticipacaoDesignacao> historico =
                historicoDAO.listarTodas();

        assertTrue(
                historico.stream().anyMatch(
                        p -> p.data().equals(dataAnterior)
                                && p.pessoa().getId().equals(
                                pessoas.get(0).getId())
                ),
                "O histórico da data anterior deveria continuar existindo."
        );

        assertTrue(
                historico.stream().anyMatch(
                        p -> p.data().equals(dataNova)),
                "O histórico da nova data deveria ser registrado."
        );
    }


    @Test
    void deveSubstituirHistoricoAoRegenerarAMesmaData() {
        LocalDate data = LocalDate.of(2026, 10, 20);
        LocalDate outraData = LocalDate.of(2026, 10, 27);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        List<Pessoa> pessoas = criarPessoas();
        List<Parte> partes = criarPartes();

        ParticipacaoDesignacao participacaoOutraData =
                new ParticipacaoDesignacao(
                        outraData,
                        pessoas.get(0),
                        partes.get(0),
                        TipoParticipacao.LEITOR,
                        SalaDesignacao.PRINCIPAL
                );

        historicoDAO.substituirParticipacoesDaData(
                outraData,
                List.of(participacaoOutraData)
        );

        CoordenadorGeracaoSalasService coordenador = criarCoordenador();

        coordenador.gerar(data, partes, pessoas);

        List<ParticipacaoDesignacao> primeiraGeracao =
                historicoDAO.listarTodas().stream()
                        .filter(p -> p.data().equals(data))
                        .toList();

        coordenador.gerar(data, partes, pessoas);

        List<ParticipacaoDesignacao> segundaGeracao =
                historicoDAO.listarTodas().stream()
                        .filter(p -> p.data().equals(data))
                        .toList();

        assertFalse(primeiraGeracao.isEmpty());

        assertEquals(
                primeiraGeracao.size(),
                segundaGeracao.size(),
                "A regeneração não deve acumular participações."
        );

        assertEquals(
                segundaGeracao.size(),
                segundaGeracao.stream()
                        .map(p -> p.pessoa().getId() + "-"
                                + p.parte().getId() + "-"
                                + p.tipoParticipacao() + "-"
                                + p.sala())
                        .distinct()
                        .count(),
                "Não deve haver participações duplicadas na mesma sala."
        );

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .anyMatch(p -> p.data().equals(outraData)),
                "A regeneração não deve apagar o histórico de outras datas."
        );
    }


    @Test
    void deveRegistrarErrosQuandoNaoHaCandidatosNasSalasAuxiliares() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        List<Pessoa> pessoas = List.of(
                criarPessoa("João", false, false),
                criarPessoa("Carlos", false, false),
                criarPessoa("Lucas", false, false),
                criarPessoa("Pedro", false, false)
        );

        List<Parte> partes = criarPartes();

        ResultadoGeracaoSalasCoordenadas resultado =
                criarCoordenador().gerar(data, partes, pessoas);

        assertTrue(
                resultado.possuiErros(),
                "A geração deveria reportar erros de falta de candidatos."
        );

        assertTrue(
                resultado.designacoesAuxiliares().isEmpty(),
                "Não deveria haver designações auxiliares sem candidatos autorizados."
        );

        assertTrue(
                resultado.erros().stream().anyMatch(
                        erro -> erro.contains("Sala B")),
                "Deveria haver um erro indicando a Sala B."
        );

        assertTrue(
                resultado.erros().stream().anyMatch(
                        erro -> erro.contains("Sala C")),
                "Deveria haver um erro indicando a Sala C."
        );

        assertTrue(
                resultado.participacoesDaData().stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.PRINCIPAL),
                "As participações válidas da Sala Principal devem ser preservadas."
        );

        assertFalse(
                historicoDAO.listarTodas().isEmpty(),
                "As participações válidas da Sala Principal devem ser persistidas."
        );

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .noneMatch(p -> p.sala() == SalaDesignacao.B
                                || p.sala() == SalaDesignacao.C),
                "O histórico não deve conter designações auxiliares inválidas."
        );
    }


    @Test
    void deveRejeitarArgumentosNulos() {
        CoordenadorGeracaoSalasService coordenador = criarCoordenador();

        LocalDate data = LocalDate.of(2026, 10, 20);
        List<Parte> partes = criarPartes();
        List<Pessoa> pessoas = criarPessoas();

        assertThrows(
                NullPointerException.class,
                () -> coordenador.gerar(null, partes, pessoas)
        );

        assertThrows(
                NullPointerException.class,
                () -> coordenador.gerar(data, null, pessoas)
        );

        assertThrows(
                NullPointerException.class,
                () -> coordenador.gerar(data, partes, null)
        );
    }


    @Test
    void deveCorrigirHistoricoAoRegenerarDataQueTeveErros() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        Pessoa pessoaPrincipal = criarPessoa(
                "Participante Principal",
                false,
                false
        );

        Pessoa participanteAntigoB = criarPessoa(
                "Participante Antigo B",
                true,
                false
        );

        List<Parte> partes = criarPartes();

        CoordenadorGeracaoSalasService coordenador = criarCoordenador();

        ResultadoGeracaoSalasCoordenadas primeiraGeracao =
                coordenador.gerar(
                        data,
                        partes,
                        List.of(pessoaPrincipal, participanteAntigoB)
                );

        assertTrue(
                primeiraGeracao.possuiErros(),
                "A primeira geração deveria apresentar erro na Sala C."
        );

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .anyMatch(p ->
                                p.data().equals(data)
                                        && p.pessoa().getId().equals(
                                        participanteAntigoB.getId())
                                        && p.sala() == SalaDesignacao.B),
                "A primeira geração deveria registrar a designação válida da Sala B."
        );

        // Regenera a mesma data com participantes suficientes.
        List<Pessoa> novasPessoas = criarPessoas();

        ResultadoGeracaoSalasCoordenadas segundaGeracao =
                coordenador.gerar(data, partes, novasPessoas);

        assertFalse(
                segundaGeracao.possuiErros(),
                "A segunda geração deveria concluir sem erros: "
                        + segundaGeracao.erros()
        );

        List<ParticipacaoDesignacao> historicoDaData =
                historicoDAO.listarTodas().stream()
                        .filter(p -> p.data().equals(data))
                        .toList();

        assertTrue(
                historicoDaData.stream()
                        .noneMatch(p ->
                                p.pessoa().getId().equals(
                                        participanteAntigoB.getId())),
                "O histórico não deve manter a designação antiga da Sala B."
        );

        assertTrue(
                historicoDaData.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.PRINCIPAL),
                "A nova geração deve registrar a Sala Principal."
        );

        assertTrue(
                historicoDaData.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.B),
                "A nova geração deve registrar a Sala B."
        );

        assertTrue(
                historicoDaData.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.C),
                "A nova geração deve registrar a Sala C."
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

    private List<Pessoa> criarPessoas() {
        return List.of(
                criarPessoa("João"),
                criarPessoa("Carlos"),
                criarPessoa("Lucas"),
                criarPessoa("Pedro")
        );
    }

    private Pessoa criarPessoa(String nome) {
        return criarPessoa(nome, true, true);
    }

    private Pessoa criarPessoa(
            String nome,
            boolean salaB,
            boolean salaC
    ) {
        return pessoaDAO.salvar(
                new Pessoa(
                        nome,
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
                        salaB,
                        salaC
                )
        );
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
