package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.database.ConnectionFactory;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class EscalaDAOTest extends BaseDAOTest {


    private final EscalaDAO escalaDAO = new EscalaDAO();


    @Test
    void deveSalvarUmaEscala() {

        Escala escala = criarEscalaSemDesignacoes();


        Escala salva = escalaDAO.salvar(escala);


        assertNotNull(salva.getId());
        assertTrue(salva.getId() > 0);
    }


    @Test
    void deveBuscarEscalaPorId() {

        Escala escala = criarEscalaSemDesignacoes();

        Escala salva = escalaDAO.salvar(escala);


        Escala encontrada =
                escalaDAO.buscarPorId(salva.getId())
                        .orElseThrow();


        assertEquals(
                salva.getId(),
                encontrada.getId()
        );

        assertEquals(
                salva.getData(),
                encontrada.getData()
        );
    }


    @Test
    void deveRetornarVazioQuandoEscalaNaoExiste() {

        assertTrue(
                escalaDAO.buscarPorId(999)
                        .isEmpty()
        );
    }




    @Test
    void deveExcluirUmaEscala() {


        Escala escala =
                escalaDAO.salvar(
                        criarEscalaSemDesignacoes()
                );


        escalaDAO.excluir(
                escala.getId()
        );


        assertTrue(
                escalaDAO.buscarPorId(
                        escala.getId()
                ).isEmpty()
        );
    }


    @Test
    void deveLancarExcecaoAoExcluirEscalaInexistente() {
        RuntimeException excecao = assertThrows(
                RuntimeException.class,
                () -> escalaDAO.excluir(999999)
        );

        assertEquals("Escala não encontrada para exclusão.", excecao.getMessage());
        assertTrue(escalaDAO.buscarPorId(999999).isEmpty());
    }


    @Test
    void devePreservarHistoricoAoExcluirEscala() {
        Pessoa pessoa = criarPessoa();
        Parte parte = criarParte();
        LocalDate dataHistorico = LocalDate.of(2026, 9, 2);
        Escala escala = escalaDAO.salvar(criarEscalaSemDesignacoes());

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement inserir = connection.prepareStatement("""
                     INSERT INTO historico_designacoes
                         (data, pessoa_id, parte_id, tipo_participacao, sala)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            inserir.setString(1, dataHistorico.toString());
            inserir.setInt(2, pessoa.getId());
            inserir.setInt(3, parte.getId());
            inserir.setString(4, TipoParticipacao.LEITOR.name());
            inserir.setString(5, SalaDesignacao.PRINCIPAL.name());
            assertEquals(1, inserir.executeUpdate());
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Falha ao preparar histórico de teste.", e);
        }

        escalaDAO.excluir(escala.getId());

        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement consultar = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM historico_designacoes
                     WHERE data = ? AND pessoa_id = ? AND parte_id = ?
                     """)) {
            consultar.setString(1, dataHistorico.toString());
            consultar.setInt(2, pessoa.getId());
            consultar.setInt(3, parte.getId());
            try (ResultSet resultado = consultar.executeQuery()) {
                assertTrue(resultado.next());
                assertEquals(1, resultado.getInt(1),
                        "Excluir uma escala não deve remover o histórico de designações.");
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Falha ao verificar histórico de teste.", e);
        }
    }


    @Test
    void deveSalvarDesignacoesDaEscala() {


        Pessoa pessoa =
                criarPessoa();


        Parte parte =
                criarParte();


        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );


        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );


        Escala salva =
                escalaDAO.salvar(escala);


        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();


        assertEquals(
                1,
                encontrada.getDesignacoes().size()
        );


        assertEquals(
                pessoa.getId(),
                encontrada.getDesignacoes()
                        .getFirst()
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveAtualizarResponsavelDaDesignacao() {

        Pessoa responsavelOriginal =
                criarPessoa();

        Pessoa novoResponsavel =
                new Pessoa(
                        "Novo Responsavel",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novoResponsavel =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novoResponsavel);

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavelOriginal,
                        null
                );

        Escala escala =
                escalaDAO.salvar(
                        new Escala(
                                LocalDate.now(),
                                List.of(designacao)
                        )
                );

        Escala escalaSalva =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                escalaSalva.getDesignacoes()
                        .getFirst();

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                novoResponsavel.getId(),
                null
        );

        Escala encontrada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                novoResponsavel.getId(),
                encontrada.getDesignacoes()
                        .getFirst()
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveSincronizarPapeisEspeciaisAoAtualizarDesignacaoManual() {
        LocalDate data = LocalDate.of(2027, 8, 4);
        br.com.geradordesignacoes.dao.PessoaDAO pessoaDAO =
                new br.com.geradordesignacoes.dao.PessoaDAO();
        Pessoa original = pessoaDAO.salvar(new Pessoa(
                "Responsável original", Sexo.MASCULINO,
                true, true, true, false, false, Privilegio.ANCIAO
        ));
        Pessoa atualizado = pessoaDAO.salvar(new Pessoa(
                "Carlos", Sexo.MASCULINO,
                true, true, true, false, false, Privilegio.ANCIAO
        ));
        br.com.geradordesignacoes.dao.ParteDAO parteDAO =
                new br.com.geradordesignacoes.dao.ParteDAO();

        Parte presidente = parteDAO.salvar(new Parte(
                "Presidente", TipoParte.PRESIDENTE_REUNIAO,
                Privilegio.ANCIAO, false, SexoPermitido.AMBOS,
                1, true, List.of(TipoParticipacao.PRESIDENTE)
        ));
        Parte dirigente = parteDAO.salvar(new Parte(
                "Dirigente do estudo", TipoParte.DIRIGENTE_ESTUDO,
                Privilegio.ANCIAO, false, SexoPermitido.AMBOS,
                1, true, List.of(TipoParticipacao.DIRIGENTE)
        ));
        Parte oracaoInicial = parteDAO.salvar(new Parte(
                "Oração inicial", TipoParte.ORACAO_INICIAL,
                Privilegio.ANCIAO, false, SexoPermitido.AMBOS,
                1, true, List.of(TipoParticipacao.ORACAO_INICIAL)
        ));

        Escala escala = new Escala(data, List.of(
                new Designacao(data, presidente, original, null),
                new Designacao(data, dirigente, original, null),
                new Designacao(data, oracaoInicial, original, null)
        ));
        escala.setPresidente(original);
        escala.setDirigente(original);
        escala.setOracaoInicial(original);
        Escala salva = escalaDAO.salvar(escala);

        Escala carregada = escalaDAO.buscarPorId(salva.getId()).orElseThrow();
        for (Designacao designacao : carregada.getDesignacoes()) {
            escalaDAO.atualizarDesignacao(
                    designacao.id(), atualizado.getId(), null
            );
        }

        // Simula colunas redundantes antigas que ficaram com o valor da
        // primeira geração, enquanto as designações atuais já foram editadas.
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE escala
                     SET presidente_id = ?, dirigente_id = ?, oracao_inicial_id = ?
                     WHERE id = ?
                     """)) {
            statement.setInt(1, original.getId());
            statement.setInt(2, original.getId());
            statement.setInt(3, original.getId());
            statement.setInt(4, salva.getId());
            assertEquals(1, statement.executeUpdate());
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }

        Escala reaberta = escalaDAO.buscarPorId(salva.getId()).orElseThrow();
        assertEquals(atualizado.getId(), reaberta.getPresidente().getId());
        assertEquals(atualizado.getId(), reaberta.getDirigente().getId());
        assertEquals(atualizado.getId(), reaberta.getOracaoInicial().getId());
        assertTrue(reaberta.getDesignacoes().stream()
                .allMatch(d -> d.responsavel().getId().equals(atualizado.getId())));
    }

    @Test
    void deveAtualizarAjudanteDaDesignacao() {

        Pessoa responsavel =
                criarPessoa();

        Pessoa ajudanteOriginal =
                new Pessoa(
                        "Ajudante Original",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        ajudanteOriginal =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(ajudanteOriginal);

        Pessoa novoAjudante =
                new Pessoa(
                        "Novo Ajudante",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novoAjudante =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novoAjudante);

        Parte parte =
                new Parte(
                        "Demonstracao",
                        TipoParte.DEMONSTRACAO,
                        Privilegio.PUBLICADOR,
                        true,
                        SexoPermitido.AMBOS,
                        2,
                        true,
                        List.of(
                                TipoParticipacao.RESPONSAVEL,
                                TipoParticipacao.AJUDANTE
                        )
                );

        parte =
                new br.com.geradordesignacoes.dao.ParteDAO()
                        .salvar(parte);

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavel,
                        ajudanteOriginal
                );

        Escala escala =
                escalaDAO.salvar(
                        new Escala(
                                LocalDate.now(),
                                List.of(designacao)
                        )
                );

        Escala escalaSalva =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                escalaSalva.getDesignacoes()
                        .getFirst();

        assertEquals(
                ajudanteOriginal.getId(),
                designacaoSalva.ajudante().getId()
        );

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                responsavel.getId(),
                novoAjudante.getId()
        );

        Escala encontrada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                novoAjudante.getId(),
                encontrada.getDesignacoes()
                        .getFirst()
                        .ajudante()
                        .getId()
        );
    }


    @Test
    void deveEncontrarSomenteOutrasDesignacoesDaPessoaNoMesmoMes() {

        Pessoa pessoa = criarPessoa();
        Pessoa outraPessoa = criarPessoa();
        Parte parte = criarParte();

        Escala escalaAtual = escalaDAO.salvar(new Escala(
                LocalDate.of(2026, 8, 5),
                List.of(new Designacao(LocalDate.of(2026, 8, 5), parte, pessoa, null))
        ));
        Designacao designacaoAtual = escalaDAO.buscarPorId(escalaAtual.getId())
                .orElseThrow().getDesignacoes().getFirst();

        escalaDAO.salvar(new Escala(
                LocalDate.of(2026, 8, 19),
                List.of(new Designacao(LocalDate.of(2026, 8, 19), parte, pessoa, null))
        ));
        escalaDAO.salvar(new Escala(
                LocalDate.of(2026, 8, 26),
                List.of(new Designacao(LocalDate.of(2026, 8, 26), parte, outraPessoa, pessoa))
        ));
        escalaDAO.salvar(new Escala(
                LocalDate.of(2026, 9, 2),
                List.of(new Designacao(LocalDate.of(2026, 9, 2), parte, pessoa, null))
        ));

        List<LocalDate> datas = escalaDAO.listarDatasDeOutrasDesignacoesNoMes(
                pessoa.getId(), YearMonth.of(2026, 8), designacaoAtual.id()
        );

        assertEquals(List.of(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 26)
        ), datas);
    }

    private Escala criarEscalaSemDesignacoes() {

        return new Escala(
                LocalDate.now(),
                List.of()
        );
    }


    private Pessoa criarPessoa() {

        Pessoa pessoa =
                new Pessoa(
                        "João",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return new br.com.geradordesignacoes.dao.PessoaDAO()
                .salvar(pessoa);
    }


    private Parte criarParte() {

        Parte parte =
                new Parte(

                        "Leitura",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                );


        return new br.com.geradordesignacoes.dao.ParteDAO()
                .salvar(parte);
    }

    @Test
    void deveCriarDesignacaoSemId() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        assertNull(
                designacao.id()
        );
    }


    @Test
    void deveCriarDesignacaoComId() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        10,
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        assertEquals(
                10,
                designacao.id()
        );
    }
    @Test
    void deveRecuperarIdDaDesignacaoAoBuscarEscala() {

        Pessoa pessoa =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoa,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );

        Escala salva =
                escalaDAO.salvar(escala);

        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoEncontrada =
                encontrada.getDesignacoes()
                        .getFirst();

        assertNotNull(
                designacaoEncontrada.id()
        );

        assertTrue(
                designacaoEncontrada.id() > 0
        );
    }

    @Test
    void deveAtualizarDesignacao() {

        Pessoa responsavelOriginal =
                criarPessoa();

        Pessoa novoResponsavel =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacao =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        responsavelOriginal,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacao)
                );

        Escala salva =
                escalaDAO.salvar(escala);

        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoSalva =
                encontrada.getDesignacoes()
                        .getFirst();

        assertNotNull(
                designacaoSalva.id()
        );

        escalaDAO.atualizarDesignacao(
                designacaoSalva.id(),
                novoResponsavel.getId(),
                null
        );

        Escala escalaAtualizada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();

        Designacao designacaoAtualizada =
                escalaAtualizada.getDesignacoes()
                        .getFirst();

        assertEquals(
                novoResponsavel.getId(),
                designacaoAtualizada.responsavel().getId()
        );

        assertNull(
                designacaoAtualizada.ajudante()
        );
    }

    @Test
    void deveAtualizarUmaEscala() {

        Pessoa pessoaOriginal =
                criarPessoa();

        Parte parte =
                criarParte();

        Designacao designacaoOriginal =
                new Designacao(
                        LocalDate.now(),
                        parte,
                        pessoaOriginal,
                        null
                );

        Escala escala =
                new Escala(
                        LocalDate.now(),
                        List.of(designacaoOriginal)
                );

        Escala salva =
                escalaDAO.salvar(escala);


        Pessoa novaPessoa =
                new Pessoa(
                        "Carlos",
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        novaPessoa =
                new br.com.geradordesignacoes.dao.PessoaDAO()
                        .salvar(novaPessoa);


        Designacao novaDesignacao =
                new Designacao(
                        escala.getData(),
                        parte,
                        novaPessoa,
                        null
                );


        escala.substituirDesignacao(
                0,
                novaDesignacao
        );


        escalaDAO.atualizar(
                escala
        );


        Escala encontrada =
                escalaDAO.buscarPorId(
                        salva.getId()
                ).orElseThrow();


        assertEquals(
                1,
                encontrada.getDesignacoes().size()
        );


        assertEquals(
                novaPessoa.getId(),
                encontrada.getDesignacoes()
                        .getFirst()
                        .responsavel()
                        .getId()
        );
    }

    @Test
    void deveImpedirAtualizacaoDeEscalaSemId() {

        Escala escala =
                criarEscalaSemDesignacoes();


        assertThrows(
                IllegalArgumentException.class,
                () -> escalaDAO.atualizar(escala)
        );
    }


    @Test
    void deveSalvarERecuperarDesignacoesDeTodasAsSalas() {

        Pessoa pessoa = criarPessoa();
        Parte parte = criarParte();
        LocalDate data = LocalDate.of(2026, 10, 14);

        Designacao principal = new Designacao(
                data,
                parte,
                pessoa,
                null,
                SalaDesignacao.PRINCIPAL
        );

        Designacao salaB = new Designacao(
                data,
                parte,
                pessoa,
                null,
                SalaDesignacao.B
        );

        Designacao salaC = new Designacao(
                data,
                parte,
                pessoa,
                null,
                SalaDesignacao.C
        );

        Escala escala = escalaDAO.salvar(
                new Escala(
                        data,
                        List.of(principal, salaB, salaC)
                )
        );

        Escala recuperada = escalaDAO.buscarPorId(
                escala.getId()
        ).orElseThrow();

        Set<SalaDesignacao> salasRecuperadas =
                recuperada.getDesignacoes()
                        .stream()
                        .map(Designacao::sala)
                        .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        SalaDesignacao.PRINCIPAL,
                        SalaDesignacao.B,
                        SalaDesignacao.C
                ),
                salasRecuperadas
        );

        assertEquals(3, recuperada.getDesignacoes().size());
    }

    @Test
    void devePersistirEPermitirRecuperarPapeisEspeciaisDaEscala() {
        Pessoa pessoa = criarPessoa();
        LocalDate data = LocalDate.of(2026, 10, 21);

        Escala escala = new Escala(data, List.of());
        escala.setPresidente(pessoa);
        escala.setDirigente(pessoa);
        escala.setOracaoInicial(pessoa);

        Escala salva = escalaDAO.salvar(escala);
        Escala encontrada = escalaDAO.buscarPorId(salva.getId()).orElseThrow();

        assertEquals(pessoa.getId(), encontrada.getPresidente().getId());
        assertEquals(pessoa.getId(), encontrada.getDirigente().getId());
        assertEquals(pessoa.getId(), encontrada.getOracaoInicial().getId());
    }

}
