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

class CoordenadorGeracaoSalasIntegracaoTest extends BaseDAOTest {

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
    void devePersistirCorrespondenciaDeTodasAsParticipacoesAuxiliares() {
        LocalDate data = LocalDate.of(2026, 11, 3);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        Parte leitura = criarParte(
                "Leitura de integração",
                TipoParte.LEITURA,
                false,
                TipoParticipacao.LEITOR
        );

        Parte demonstracao = criarParte(
                "Demonstração de integração",
                TipoParte.DEMONSTRACAO,
                true,
                TipoParticipacao.RESPONSAVEL,
                TipoParticipacao.AJUDANTE
        );

        Parte discurso = criarParte(
                "Discurso de integração",
                TipoParte.DISCURSO,
                false,
                TipoParticipacao.ORADOR
        );

        List<Parte> partes = List.of(
                leitura,
                demonstracao,
                discurso
        );

        List<Pessoa> pessoas = criarPessoas(16, true, true);

        ResultadoGeracaoSalasCoordenadas resultado =
                criarCoordenador().gerar(data, partes, pessoas);

        assertFalse(
                resultado.possuiErros(),
                "A geração deveria terminar sem erros: "
                        + resultado.erros()
        );

        assertFalse(
                resultado.designacoesAuxiliares().isEmpty(),
                "Deveriam existir designações auxiliares."
        );

        for (Designacao designacao :
                resultado.designacoesAuxiliares()) {

            assertNotEquals(
                    SalaDesignacao.PRINCIPAL,
                    designacao.sala(),
                    "O gerador auxiliar não deve produzir designações "
                            + "da Sala Principal."
            );

            List<ParticipacaoDesignacao> participacoesCorrespondentes =
                    resultado.participacoesDaData().stream()
                            .filter(p -> p.data().equals(data))
                            .filter(p -> p.sala() == designacao.sala())
                            .filter(p -> p.parte().getId().equals(
                                    designacao.parte().getId()))
                            .toList();

            switch (designacao.parte().getTipo()) {
                case LEITURA -> {
                    assertTrue(
                            participacoesCorrespondentes.stream()
                                    .anyMatch(p ->
                                            p.pessoa().getId().equals(
                                                    designacao.responsavel()
                                                            .getId())
                                                    && p.tipoParticipacao()
                                                    == TipoParticipacao.LEITOR
                                    ),
                            "A leitura deve gerar uma participação "
                                    + "LEITOR na sala correspondente."
                    );
                }

                case DISCURSO -> {
                    assertTrue(
                            participacoesCorrespondentes.stream()
                                    .anyMatch(p ->
                                            p.pessoa().getId().equals(
                                                    designacao.responsavel()
                                                            .getId())
                                                    && p.tipoParticipacao()
                                                    == TipoParticipacao.ORADOR
                                    ),
                            "O discurso deve gerar uma participação "
                                    + "ORADOR na sala correspondente."
                    );
                }

                case DEMONSTRACAO -> {
                    assertTrue(
                            participacoesCorrespondentes.stream()
                                    .anyMatch(p ->
                                            p.pessoa().getId().equals(
                                                    designacao.responsavel()
                                                            .getId())
                                                    && p.tipoParticipacao()
                                                    == TipoParticipacao.RESPONSAVEL
                                    ),
                            "A demonstração deve registrar "
                                    + "o responsável."
                    );

                    assertTrue(
                            participacoesCorrespondentes.stream()
                                    .anyMatch(p ->
                                            p.pessoa().getId().equals(
                                                    designacao.ajudante()
                                                            .getId())
                                                    && p.tipoParticipacao()
                                                    == TipoParticipacao.AJUDANTE
                                    ),
                            "A demonstração deve registrar "
                                    + "o ajudante."
                    );
                }

                default -> fail(
                        "Tipo inesperado de parte auxiliar: "
                                + designacao.parte().getTipo()
                );
            }
        }

        List<ParticipacaoDesignacao> historicoPersistido =
                historicoDAO.listarTodas();

        assertEquals(
                assinaturas(resultado.participacoesDaData()),
                assinaturas(historicoPersistido),
                "O histórico persistido deve corresponder às "
                        + "participações retornadas pelo coordenador."
        );

        assertTrue(
                historicoPersistido.stream()
                        .allMatch(p -> p.data().equals(data)),
                "O histórico deste teste deve conter somente "
                        + "participações da data gerada."
        );
    }

    @Test
    void deveRemoverDaDataAsParticipacoesDaSalaDesabilitadaNaRegeneracao() {
        LocalDate data = LocalDate.of(2026, 11, 10);

        Parte leitura = criarParte(
                "Leitura para regeneração",
                TipoParte.LEITURA,
                false,
                TipoParticipacao.LEITOR
        );

        List<Parte> partes = List.of(leitura);
        List<Pessoa> pessoas = criarPessoas(8, true, true);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        ResultadoGeracaoSalasCoordenadas primeiraGeracao =
                criarCoordenador().gerar(data, partes, pessoas);

        assertFalse(
                primeiraGeracao.possuiErros(),
                "A primeira geração deveria ser concluída sem erros: "
                        + primeiraGeracao.erros()
        );

        assertTrue(
                historicoDAO.listarTodas().stream()
                        .anyMatch(p ->
                                p.data().equals(data)
                                        && p.sala() == SalaDesignacao.C),
                "A primeira geração deveria registrar a Sala C."
        );

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, false));

        ResultadoGeracaoSalasCoordenadas segundaGeracao =
                criarCoordenador().gerar(data, partes, pessoas);

        assertFalse(
                segundaGeracao.possuiErros(),
                "A regeneração deveria ser concluída sem erros: "
                        + segundaGeracao.erros()
        );

        List<ParticipacaoDesignacao> historico =
                historicoDAO.listarTodas();

        assertTrue(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.PRINCIPAL),
                "A Sala Principal deve continuar registrada."
        );

        assertTrue(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.B),
                "A Sala B deve continuar registrada."
        );

        assertFalse(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.C),
                "As participações antigas da Sala C devem ser "
                        + "removidas ao regenerar a data."
        );
    }

    @Test
    void deveManterSalaBQuandoSalaCEstaSemCandidatos() {
        LocalDate data = LocalDate.of(2026, 11, 17);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        Parte leitura = criarParte(
                "Leitura com falha parcial",
                TipoParte.LEITURA,
                false,
                TipoParticipacao.LEITOR
        );

        List<Pessoa> pessoas = criarPessoas(6, true, false);

        ResultadoGeracaoSalasCoordenadas resultado =
                criarCoordenador().gerar(
                        data,
                        List.of(leitura),
                        pessoas
                );

        assertTrue(
                resultado.possuiErros(),
                "A ausência de candidatos para a Sala C deve ser "
                        + "informada como erro."
        );

        assertTrue(
                resultado.erros().stream()
                        .anyMatch(erro -> erro.contains("Sala C")),
                "Deve existir uma mensagem de erro referente à Sala C."
        );

        assertTrue(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.B),
                "A designação válida da Sala B deve ser mantida."
        );

        assertFalse(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d -> d.sala() == SalaDesignacao.C),
                "Não deve haver designação na Sala C sem candidatos."
        );

        List<ParticipacaoDesignacao> historico =
                historicoDAO.listarTodas();

        assertTrue(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.B),
                "As participações válidas da Sala B devem ser persistidas."
        );

        assertFalse(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.C),
                "O histórico não deve conter participações inválidas "
                        + "da Sala C."
        );

        assertTrue(
                historico.stream().anyMatch(p ->
                        p.data().equals(data)
                                && p.sala() == SalaDesignacao.PRINCIPAL),
                "As participações válidas da Sala Principal "
                        + "devem ser preservadas."
        );
    }

    @Test
    void naoDeveGerarSalasAuxiliaresParaPartesNaoElegiveis() {
        LocalDate data = LocalDate.of(2026, 11, 24);

        configuracaoDAO.salvar(new ConfiguracaoSalas(true, true));

        Parte leitura = criarParte(
                "Leitura elegível",
                TipoParte.LEITURA,
                false,
                TipoParticipacao.LEITOR
        );

        Parte discursoTesouros = criarParte(
                "Discurso de tesouros não elegível",
                TipoParte.DISCURSO_TESOUROS,
                false,
                TipoParticipacao.ORADOR
        );

        List<Pessoa> pessoas = criarPessoas(12, true, true);

        ResultadoGeracaoSalasCoordenadas resultado =
                criarCoordenador().gerar(
                        data,
                        List.of(leitura, discursoTesouros),
                        pessoas
                );

        assertTrue(
                resultado.designacoesAuxiliares().stream()
                        .allMatch(d ->
                                d.parte().getTipo() == TipoParte.LEITURA
                                        || d.parte().getTipo()
                                        == TipoParte.DEMONSTRACAO
                                        || d.parte().getTipo()
                                        == TipoParte.DISCURSO),
                "As salas auxiliares devem conter somente partes "
                        + "dos tipos elegíveis."
        );

        assertFalse(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d ->
                                d.parte().getTipo()
                                        == TipoParte.DISCURSO_TESOUROS),
                "O discurso de tesouros não deve ser gerado "
                        + "nas salas auxiliares."
        );

        assertTrue(
                resultado.designacoesAuxiliares().stream()
                        .anyMatch(d ->
                                d.parte().getTipo() == TipoParte.LEITURA),
                "A parte de leitura elegível deve ser gerada "
                        + "nas salas auxiliares."
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

    private List<Pessoa> criarPessoas(
            int quantidade,
            boolean salaB,
            boolean salaC
    ) {
        return java.util.stream.IntStream.rangeClosed(1, quantidade)
                .mapToObj(indice -> pessoaDAO.salvar(
                        new Pessoa(
                                "Participante " + indice,
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
                ))
                .toList();
    }

    private Parte criarParte(
            String nome,
            TipoParte tipo,
            boolean exigeAjudante,
            TipoParticipacao... participacoes
    ) {
        return parteDAO.salvar(
                new Parte(
                        nome,
                        tipo,
                        Privilegio.PUBLICADOR,
                        exigeAjudante,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(participacoes)
                )
        );
    }

    private List<String> assinaturas(
            List<ParticipacaoDesignacao> participacoes
    ) {
        return participacoes.stream()
                .map(p -> p.data()
                        + "|" + p.pessoa().getId()
                        + "|" + p.parte().getId()
                        + "|" + p.tipoParticipacao()
                        + "|" + p.sala())
                .sorted()
                .toList();
    }
}
