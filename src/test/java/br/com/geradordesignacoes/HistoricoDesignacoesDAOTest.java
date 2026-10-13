package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HistoricoDesignacoesDAO;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoricoDesignacoesDAOTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final HistoricoDesignacoesDAO historicoDAO =
            new HistoricoDesignacoesDAO();


    @Test
    void deveSalvarECarregarHistorico() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Teste Histórico",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = new Parte(
                "Leitura Histórico",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );

        Parte parteSalva = parteDAO.salvar(parte);

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parteSalva,
                        TipoParticipacao.LEITOR
                );

        historicoDAO.salvar(participacao);

        HistoricoDesignacoes historico =
                historicoDAO.carregarHistorico();

        assertNotNull(historico);

        assertFalse(
                historico.participacoes().isEmpty()
        );

        ParticipacaoDesignacao recuperada =
                historico.participacoes()
                        .get(historico.participacoes().size() - 1);

        assertEquals(
                pessoa.getId(),
                recuperada.pessoa().getId()
        );

        assertEquals(
                parteSalva.getId(),
                recuperada.parte().getId()
        );

        assertEquals(
                TipoParticipacao.LEITOR,
                recuperada.tipoParticipacao()
        );

        assertEquals(
                LocalDate.now(),
                recuperada.data()
        );
    }


    @Test
    void deveBuscarParticipacoesPorPessoa() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Buscar",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Buscar",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.buscarPorPessoa(
                        pessoa.getId()
                );

        assertFalse(resultado.isEmpty());

        assertEquals(
                pessoa.getId(),
                resultado.get(0).pessoa().getId()
        );
    }


    @Test
    void deveBuscarParticipacoesPorParte() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Parte",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Teste",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.buscarPorParte(
                        parte.getId()
                );

        assertFalse(resultado.isEmpty());

        assertEquals(
                parte.getId(),
                resultado.get(0).parte().getId()
        );
    }


    @Test
    void deveSalvarVariasParticipacoes() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Lista",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Lista",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        List<ParticipacaoDesignacao> lista = List.of(

                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                ),

                new ParticipacaoDesignacao(
                        LocalDate.now().plusDays(1),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        historicoDAO.salvarTodos(lista);

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        assertEquals(
                2,
                resultado.size()
        );
    }


    @Test
    void deveLimparHistorico() {

        Pessoa pessoa = new Pessoa(
                "Pessoa Limpar",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Limpar",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        historicoDAO.salvar(
                new ParticipacaoDesignacao(
                        LocalDate.now(),
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR
                )
        );

        assertFalse(
                historicoDAO.listarTodas().isEmpty()
        );

        historicoDAO.limpar();

        assertTrue(
                historicoDAO.listarTodas().isEmpty()
        );
    }

    @Test
    void deveSalvarERecuperarParticipacaoNaSalaB() {
        Pessoa pessoa = new Pessoa("Pessoa Sala B", Sexo.MASCULINO, true, true, true, true, false, Privilegio.BATIZADO);
        pessoaDAO.salvar(pessoa);
        Parte parte = parteDAO.salvar(new Parte("Parte Sala B", TipoParte.LEITURA, Privilegio.BATIZADO, false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
        ParticipacaoDesignacao participacao = new ParticipacaoDesignacao(LocalDate.now(), pessoa, parte, TipoParticipacao.LEITOR, SalaDesignacao.B);
        historicoDAO.salvar(participacao);
        List<ParticipacaoDesignacao> resultado = historicoDAO.listarTodas();
        ParticipacaoDesignacao recuperada = resultado.stream().filter(p -> p.pessoa().getId().equals(pessoa.getId())).filter(p -> p.parte().getId().equals(parte.getId())).findFirst().orElseThrow();
        assertEquals(SalaDesignacao.B, recuperada.sala());
    }

    @Test
    void devePreservarSalaAoSubstituirParticipacoesDaData() {

        LocalDate data = LocalDate.of(2026, 10, 15);

        Pessoa pessoa = new Pessoa(
                "Pessoa Substituicao Sala B",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Substituicao Sala B",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        ParticipacaoDesignacao participacao =
                new ParticipacaoDesignacao(
                        data,
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR,
                        SalaDesignacao.B
                );

        historicoDAO.substituirParticipacoesDaData(
                data,
                List.of(participacao)
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        ParticipacaoDesignacao recuperada = resultado.stream()
                .filter(p -> p.pessoa().getId().equals(pessoa.getId()))
                .filter(p -> p.parte().getId().equals(parte.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(data, recuperada.data());
        assertEquals(SalaDesignacao.B, recuperada.sala());
        assertEquals(
                TipoParticipacao.LEITOR,
                recuperada.tipoParticipacao()
        );
    }

    @Test
    void devePermitirMesmaParticipacaoEmSalasDiferentes() {

        LocalDate data = LocalDate.of(2026, 10, 16);

        Pessoa pessoa = new Pessoa(
                "Pessoa Duas Salas",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );

        pessoaDAO.salvar(pessoa);

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte Duas Salas",
                        TipoParte.LEITURA,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        ParticipacaoDesignacao salaPrincipal =
                new ParticipacaoDesignacao(
                        data,
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR,
                        SalaDesignacao.PRINCIPAL
                );

        ParticipacaoDesignacao salaB =
                new ParticipacaoDesignacao(
                        data,
                        pessoa,
                        parte,
                        TipoParticipacao.LEITOR,
                        SalaDesignacao.B
                );

        historicoDAO.salvar(salaPrincipal);
        historicoDAO.salvar(salaB);

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        List<ParticipacaoDesignacao> encontradas = resultado.stream()
                .filter(p -> p.pessoa().getId().equals(pessoa.getId()))
                .filter(p -> p.parte().getId().equals(parte.getId()))
                .filter(p -> p.data().equals(data))
                .toList();

        assertEquals(2, encontradas.size());

        assertTrue(
                encontradas.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.PRINCIPAL)
        );

        assertTrue(
                encontradas.stream()
                        .anyMatch(p -> p.sala() == SalaDesignacao.B)
        );
    }
    
    @Test
    void deveSubstituirParticipacoesPreservandoSalasEOutrasDatas() {

        LocalDate data = LocalDate.of(2026, 10, 20);
        LocalDate outraData = data.plusDays(1);

        Pessoa pessoa = criarPessoa("Pessoa Substituicao Completa");
        Parte parte = criarParte("Parte Substituicao Completa");

        historicoDAO.salvar(
                criarParticipacao(
                        data, pessoa, parte, SalaDesignacao.C
                )
        );

        historicoDAO.salvar(
                criarParticipacao(
                        outraData, pessoa, parte, SalaDesignacao.B
                )
        );

        historicoDAO.substituirParticipacoesDaData(
                data,
                List.of(
                        criarParticipacao(
                                data, pessoa, parte, SalaDesignacao.PRINCIPAL
                        ),
                        criarParticipacao(
                                data, pessoa, parte, SalaDesignacao.B
                        )
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        List<ParticipacaoDesignacao> daData = resultado.stream()
                .filter(p -> p.pessoa().getId().equals(pessoa.getId()))
                .filter(p -> p.parte().getId().equals(parte.getId()))
                .filter(p -> p.data().equals(data))
                .toList();

        assertEquals(2, daData.size());

        assertTrue(daData.stream()
                .anyMatch(p -> p.sala() == SalaDesignacao.PRINCIPAL));

        assertTrue(daData.stream()
                .anyMatch(p -> p.sala() == SalaDesignacao.B));

        assertTrue(resultado.stream()
                .anyMatch(p ->
                        p.pessoa().getId().equals(pessoa.getId())
                                && p.parte().getId().equals(parte.getId())
                                && p.data().equals(outraData)
                                && p.sala() == SalaDesignacao.B
                ));
    }

    @Test
    void deveRemoverSomenteDataInformadaQuandoListaVazia() {

        LocalDate data = LocalDate.of(2026, 10, 22);
        LocalDate outraData = data.plusDays(1);

        Pessoa pessoa = criarPessoa("Pessoa Remocao Por Data");
        Parte parte = criarParte("Parte Remocao Por Data");

        historicoDAO.salvar(
                criarParticipacao(
                        data, pessoa, parte, SalaDesignacao.PRINCIPAL
                )
        );

        historicoDAO.salvar(
                criarParticipacao(
                        outraData, pessoa, parte, SalaDesignacao.B
                )
        );

        historicoDAO.substituirParticipacoesDaData(
                data,
                List.of()
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        assertFalse(resultado.stream().anyMatch(p ->
                p.pessoa().getId().equals(pessoa.getId())
                        && p.parte().getId().equals(parte.getId())
                        && p.data().equals(data)
        ));

        assertTrue(resultado.stream().anyMatch(p ->
                p.pessoa().getId().equals(pessoa.getId())
                        && p.parte().getId().equals(parte.getId())
                        && p.data().equals(outraData)
                        && p.sala() == SalaDesignacao.B
        ));
    }

    @Test
    void deveRejeitarParticipacaoDeOutraDataSemAlterarHistorico() {

        LocalDate data = LocalDate.of(2026, 10, 24);

        Pessoa pessoa = criarPessoa("Pessoa Data Invalida");
        Parte parte = criarParte("Parte Data Invalida");

        historicoDAO.salvar(
                criarParticipacao(
                        data, pessoa, parte, SalaDesignacao.PRINCIPAL
                )
        );

        ParticipacaoDesignacao participacaoInvalida =
                criarParticipacao(
                        data.plusDays(1),
                        pessoa,
                        parte,
                        SalaDesignacao.B
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> historicoDAO.substituirParticipacoesDaData(
                        data,
                        List.of(participacaoInvalida)
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        assertTrue(resultado.stream().anyMatch(p ->
                p.pessoa().getId().equals(pessoa.getId())
                        && p.parte().getId().equals(parte.getId())
                        && p.data().equals(data)
                        && p.sala() == SalaDesignacao.PRINCIPAL
        ));
    }

    @Test
    void deveReverterSubstituicaoQuandoOcorreDuplicidade() {

        LocalDate data = LocalDate.of(2026, 10, 26);

        Pessoa pessoa = criarPessoa("Pessoa Reversao");
        Parte parte = criarParte("Parte Reversao");

        historicoDAO.salvar(
                criarParticipacao(
                        data, pessoa, parte, SalaDesignacao.PRINCIPAL
                )
        );

        ParticipacaoDesignacao duplicada =
                criarParticipacao(
                        data, pessoa, parte, SalaDesignacao.B
                );

        assertThrows(
                RuntimeException.class,
                () -> historicoDAO.substituirParticipacoesDaData(
                        data,
                        List.of(duplicada, duplicada)
                )
        );

        List<ParticipacaoDesignacao> resultado =
                historicoDAO.listarTodas();

        List<ParticipacaoDesignacao> daData = resultado.stream()
                .filter(p -> p.pessoa().getId().equals(pessoa.getId()))
                .filter(p -> p.parte().getId().equals(parte.getId()))
                .filter(p -> p.data().equals(data))
                .toList();

        assertEquals(1, daData.size());
        assertEquals(SalaDesignacao.PRINCIPAL, daData.get(0).sala());
    }

    private Pessoa criarPessoa(String nome) {
        Pessoa pessoa = new Pessoa(nome, Sexo.MASCULINO, true, true, true, true, false, Privilegio.BATIZADO);
        pessoaDAO.salvar(pessoa);
        return pessoa;
    }

    private Parte criarParte(String nome) {
        return parteDAO.salvar(new Parte(nome, TipoParte.LEITURA, Privilegio.BATIZADO, false, SexoPermitido.AMBOS, 1, false, List.of(TipoParticipacao.LEITOR)));
    }

    private ParticipacaoDesignacao criarParticipacao(LocalDate data, Pessoa pessoa, Parte parte, SalaDesignacao sala) {
        return new ParticipacaoDesignacao(data, pessoa, parte, TipoParticipacao.LEITOR, sala);
    }
}