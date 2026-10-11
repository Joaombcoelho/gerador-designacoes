package br.com.geradordesignacoes;

import br.com.geradordesignacoes.controller.EdicaoEscalaController;
import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EdicaoEscalaControllerTest extends BaseDAOTest {

    private final PessoaDAO pessoaDAO = new PessoaDAO();
    private final ParteDAO parteDAO = new ParteDAO();
    private final EscalaDAO escalaDAO = new EscalaDAO();

    private final EdicaoEscalaController controller =
            new EdicaoEscalaController();


    @Test
    void deveAlterarResponsavelDaDesignacao() {

        Pessoa responsavelOriginal = criarPessoa("Responsavel Original");

        Pessoa novoResponsavel = criarPessoa("Novo Responsavel");

        Parte parte = criarParteSemAjudante();

        Escala escala = criarEscala(
                LocalDate.of(2026, 9, 3),
                new Designacao(
                        LocalDate.of(2026, 9, 3),
                        parte,
                        responsavelOriginal,
                        null
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(escala.getId());

        controller.salvarAlteracoes(
                designacao.id(),
                novoResponsavel,
                null
        );

        Escala atualizada =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow();

        assertEquals(
                novoResponsavel.getId(),
                atualizada.getDesignacoes()
                        .get(0)
                        .responsavel()
                        .getId()
        );
    }


    @Test
    void deveAlterarAjudanteDaDesignacao() {

        Pessoa responsavel = criarPessoa("Responsavel");

        Pessoa ajudanteOriginal = criarPessoa("Ajudante Original");

        Pessoa novoAjudante = criarPessoa("Novo Ajudante");

        Parte parte = criarParteComAjudante();

        Escala escala = criarEscala(
                LocalDate.of(2026, 9, 3),
                new Designacao(
                        LocalDate.of(2026, 9, 3),
                        parte,
                        responsavel,
                        ajudanteOriginal
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(escala.getId());

        controller.salvarAlteracoes(
                designacao.id(),
                responsavel,
                novoAjudante
        );

        Escala atualizada =
                escalaDAO.buscarPorId(escala.getId())
                        .orElseThrow();

        assertEquals(
                novoAjudante.getId(),
                atualizada.getDesignacoes()
                        .get(0)
                        .ajudante()
                        .getId()
        );
    }


    @Test
    void deveDetectarConflitoDePessoaNoMesmoMes() {

        Pessoa pessoa = criarPessoa("Pessoa em Conflito");

        Parte parte1 = criarParteSemAjudante("Parte 1");

        Parte parte2 = criarParteSemAjudante("Parte 2");

        LocalDate primeiraData =
                LocalDate.of(2026, 9, 3);

        LocalDate segundaData =
                LocalDate.of(2026, 9, 10);

        criarEscala(
                primeiraData,
                new Designacao(
                        primeiraData,
                        parte1,
                        pessoa,
                        null
                )
        );

        Escala segundaEscala = criarEscala(
                segundaData,
                new Designacao(
                        segundaData,
                        parte2,
                        pessoa,
                        null
                )
        );

        Designacao designacao =
                escalaDAO.buscarPorId(segundaEscala.getId())
                        .orElseThrow()
                        .getDesignacoes()
                        .get(0);

        controller.selecionarEscala(
                segundaEscala.getId()
        );

        List<LocalDate> conflitos =
                controller.verificarConflitos(
                        designacao.id(),
                        pessoa,
                        null
                );

        assertEquals(
                List.of(primeiraData),
                conflitos
        );
    }

    @Test
    void deveListarSomentePessoasElegiveisParaParticipacao() {
        Pessoa elegivel = pessoa(
                "Elegível",
                Sexo.FEMININO,
                true,
                Privilegio.ANCIAO,
                NivelLeitura.BASICO,
                true
        );
        Pessoa inativa = pessoa(
                "Inativa",
                Sexo.MASCULINO,
                false,
                Privilegio.ANCIAO,
                NivelLeitura.BASICO,
                true
        );
        Pessoa sexoInvalido = pessoa(
                "Sexo inválido",
                Sexo.MASCULINO,
                true,
                Privilegio.ANCIAO,
                NivelLeitura.BASICO,
                true
        );
        Pessoa privilegioInsuficiente = pessoa(
                "Privilégio insuficiente",
                Sexo.FEMININO,
                true,
                Privilegio.PUBLICADOR,
                NivelLeitura.BASICO,
                true
        );

        Parte parte = parteDAO.salvar(
                new Parte(
                        "Parte feminina",
                        TipoParte.PARTE_1,
                        Privilegio.BATIZADO,
                        false,
                        SexoPermitido.FEMININO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.RESPONSAVEL)
                )
        );

        List<Pessoa> pessoas =
                controller.listarPessoasParaSelecao(
                        parte,
                        TipoParticipacao.RESPONSAVEL
                );

        assertTrue(pessoas.contains(elegivel));
        assertFalse(pessoas.contains(privilegioInsuficiente));
        assertFalse(pessoas.contains(inativa));
        assertFalse(pessoas.contains(sexoInvalido));
    }

    @Test
    void deveManterParticipanteAtualMesmoQueNaoEstejaMaisElegivel() {
        Pessoa atual = pessoa(
                "Participante atual",
                Sexo.MASCULINO,
                false,
                Privilegio.ANCIAO,
                NivelLeitura.BASICO,
                true
        );

        Parte parte = criarParteSemAjudante("Parte atual");

        assertTrue(
                controller.listarPessoasParaSelecao(
                        parte,
                        TipoParticipacao.RESPONSAVEL,
                        atual
                ).contains(atual)
        );
    }

    @Test
    void deveFiltrarParticipantesPelaPermissaoDaParticipacaoConcreta() {
        Pessoa leitor = criarPessoaComPermissoes(
                "Leitor",
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                false,
                true,
                false,
                false,
                false,
                false
        );
        Pessoa semPermissaoDeLeitura = criarPessoaComPermissoes(
                "Sem leitura",
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                true,
                false,
                true,
                false,
                false,
                false
        );
        Parte parte = parteDAO.salvar(
                new Parte(
                        "Leitura concreta",
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.LEITOR)
                )
        );

        List<Pessoa> pessoas = controller.listarPessoasParaSelecao(
                parte,
                TipoParticipacao.LEITOR
        );

        assertTrue(pessoas.contains(leitor));
        assertFalse(pessoas.contains(semPermissaoDeLeitura));
    }

    @Test
    void deveFiltrarDirigenteELeitorInternoDoEstudoBiblico() {
        Pessoa dirigente = criarPessoaComPermissoes(
                "Dirigente",
                Sexo.MASCULINO,
                Privilegio.SERVO_MINISTERIAL,
                false,
                false,
                false,
                false,
                false,
                false,
                true
        );
        Pessoa leitorExperiente = criarPessoaComPermissoes(
                "Leitor experiente",
                Sexo.MASCULINO,
                Privilegio.BATIZADO,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.EXPERIENTE
        );
        Pessoa leitorBasico = criarPessoaComPermissoes(
                "Leitor básico",
                Sexo.MASCULINO,
                Privilegio.BATIZADO,
                false,
                true,
                false,
                false,
                false,
                false,
                false,
                NivelLeitura.BASICO
        );
        Parte estudo = parteDAO.salvar(
                new Parte(
                        "Estudo Bíblico",
                        TipoParte.DIRIGENTE_ESTUDO,
                        Privilegio.SERVO_MINISTERIAL,
                        false,
                        SexoPermitido.MASCULINO,
                        2,
                        true,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.DIRIGENTE)
                )
        );

        List<Pessoa> dirigentes = controller.listarPessoasParaSelecao(
                estudo,
                TipoParticipacao.DIRIGENTE
        );
        List<Pessoa> leitores = controller.listarPessoasParaSelecao(
                estudo,
                TipoParticipacao.AJUDANTE
        );

        assertTrue(dirigentes.contains(dirigente));
        assertFalse(dirigentes.contains(leitorExperiente));
        assertTrue(leitores.contains(leitorExperiente));
        assertFalse(leitores.contains(leitorBasico));
    }

    @Test
    void naoDeveOferecerPresidenteParaOutraDesignacaoNaMesmaReuniao() {
        Pessoa presidente = criarPessoaComPermissoes(
                "Presidente",
                Sexo.MASCULINO,
                Privilegio.ANCIAO,
                true,
                true,
                false,
                true,
                true,
                true,
                false
        );
        Parte partePresidente = parteDAO.salvar(
                new Parte(
                        "Presidente",
                        TipoParte.PRESIDENTE_REUNIAO,
                        Privilegio.ANCIAO,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.PRESIDENTE)
                )
        );
        Parte outraParte = criarParteSemAjudante("Outra parte");
        Escala escala = criarEscala(
                LocalDate.of(2026, 9, 3),
                new Designacao(
                        LocalDate.of(2026, 9, 3),
                        partePresidente,
                        presidente,
                        null
                )
        );
        escalaDAO.adicionarDesignacao(
                escala.getId(),
                outraParte.getId(),
                presidente.getId(),
                null
        );

        controller.selecionarEscala(escala.getId());

        List<Pessoa> pessoas = controller.listarPessoasParaSelecao(
                outraParte,
                TipoParticipacao.RESPONSAVEL
        );

        assertFalse(pessoas.contains(presidente));
    }

    @Test
    void naoDeveOferecerPessoaComConflitoTesourosEJoiasNaMesmaReuniao() {
        Pessoa pessoa = criarPessoaComPermissoes(
                "Pessoa com Tesouros",
                Sexo.MASCULINO,
                Privilegio.SERVO_MINISTERIAL,
                true,
                false,
                false,
                true,
                false,
                false,
                false
        );
        Parte tesouros = parteDAO.salvar(
                new Parte(
                        "Discurso - Tesouros da Palavra de Deus",
                        TipoParte.DISCURSO_TESOUROS,
                        Privilegio.SERVO_MINISTERIAL,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.ORADOR)
                )
        );
        Parte joias = parteDAO.salvar(
                new Parte(
                        "Joias espirituais",
                        TipoParte.JOIAS_ESPIRITUAIS,
                        Privilegio.SERVO_MINISTERIAL,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.ORADOR)
                )
        );
        LocalDate data = LocalDate.of(2026, 9, 3);
        Escala escala = criarEscala(
                data,
                new Designacao(data, tesouros, pessoa, null)
        );

        controller.selecionarEscala(escala.getId());

        assertFalse(
                controller.listarPessoasParaSelecao(
                        joias,
                        TipoParticipacao.ORADOR
                ).contains(pessoa)
        );
    }

    @Test
    void devePermitirSubstituicaoSemConsiderarADesignacaoAtualComoConflito() {
        Pessoa pessoa = criarPessoaComPermissoes(
                "Participante atual",
                Sexo.MASCULINO,
                Privilegio.SERVO_MINISTERIAL,
                true,
                false,
                false,
                true,
                false,
                false,
                false
        );
        Parte parte = parteDAO.salvar(
                new Parte(
                        "Tesouros atual",
                        TipoParte.DISCURSO_TESOUROS,
                        Privilegio.SERVO_MINISTERIAL,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.ORADOR)
                )
        );
        LocalDate data = LocalDate.of(2026, 9, 3);
        Escala escala = criarEscala(
                data,
                new Designacao(data, parte, pessoa, null)
        );

        Designacao designacao = escalaDAO.buscarPorId(escala.getId())
                .orElseThrow()
                .getDesignacoes()
                .get(0);
        Parte joias = parteDAO.salvar(
                new Parte(
                        "Joias atuais",
                        TipoParte.JOIAS_ESPIRITUAIS,
                        Privilegio.SERVO_MINISTERIAL,
                        false,
                        SexoPermitido.MASCULINO,
                        1,
                        false,
                        NivelLeitura.BASICO,
                        List.of(TipoParticipacao.ORADOR)
                )
        );

        controller.selecionarEscala(escala.getId());

        List<Pessoa> candidatos = controller.listarPessoasParaSelecao(
                joias,
                TipoParticipacao.ORADOR,
                null,
                designacao.id()
        );

        assertTrue(
                candidatos.contains(pessoa)
        );
    }

    @Test
    void deveConsiderarTodasAsDesignacoesAoAdicionarParte() {
        Pessoa pessoaJaDesignada = criarPessoaComPermissoes(
                "Pessoa já designada",
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                false,
                false,
                false,
                false,
                false,
                false
        );
        Pessoa novoResponsavel = criarPessoaComPermissoes(
                "Novo responsável",
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                false,
                false,
                false,
                false,
                false,
                false
        );
        Parte parteExistente = criarParteSemAjudante(
                "Parte existente"
        );
        Parte novaParte = criarParteSemAjudante(
                "Nova parte"
        );
        LocalDate data = LocalDate.of(2026, 9, 3);
        Escala escala = criarEscala(
                data,
                new Designacao(
                        data,
                        parteExistente,
                        pessoaJaDesignada,
                        null
                )
        );

        controller.selecionarEscala(escala.getId());

        List<Pessoa> candidatos = controller.listarPessoasParaSelecao(
                novaParte,
                TipoParticipacao.RESPONSAVEL,
                null,
                null
        );

        assertFalse(candidatos.contains(pessoaJaDesignada));
        assertTrue(candidatos.contains(novoResponsavel));
    }

    @Test
    void deveRejeitarParticipanteInvalidoEnviadoDiretamenteAoSalvar() {
        Pessoa responsavel = criarPessoa("Responsável válido");
        Pessoa inativo = criarPessoaInativo("Responsável inativo");
        Parte parte = criarParteSemAjudante("Parte direta");
        LocalDate data = LocalDate.of(2026, 9, 3);
        Escala escala = criarEscala(
                data,
                new Designacao(data, parte, responsavel, null)
        );
        Designacao designacao = escalaDAO.buscarPorId(escala.getId())
                .orElseThrow()
                .getDesignacoes()
                .get(0);

        controller.selecionarEscala(escala.getId());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.salvarAlteracoes(
                        designacao.id(),
                        inativo,
                        null
                )
        );
    }

    @Test
    void deveRejeitarDemonstracaoComResponsavelEAjudanteDeSexosDiferentes() {
        Pessoa responsavel = criarPessoaComPermissoes(
                "Responsável",
                Sexo.MASCULINO,
                Privilegio.PUBLICADOR,
                true,
                true,
                false,
                false,
                false,
                false,
                false
        );
        Pessoa ajudante = criarPessoaComPermissoes(
                "Ajudante",
                Sexo.FEMININO,
                Privilegio.PUBLICADOR,
                true,
                true,
                false,
                false,
                false,
                false,
                false
        );
        Parte parte = criarParteComAjudante();
        Escala escala = criarEscalaVazia(
                LocalDate.of(2026, 9, 3)
        );

        controller.selecionarEscala(escala.getId());

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.adicionarParte(
                        parte,
                        responsavel,
                        ajudante
                )
        );
    }


    @Test
    void deveAdicionarNovaParteNaEscala() {

        Pessoa responsavelOriginal =
                criarPessoa("Responsavel Original");

        Pessoa novoResponsavel =
                criarPessoa("Novo Responsavel");

        Parte parteOriginal =
                criarParteSemAjudante("Parte Original");

        Parte novaParte =
                criarParteSemAjudante("Nova Parte");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscala(
                        data,
                        new Designacao(
                                data,
                                parteOriginal,
                                responsavelOriginal,
                                null
                        )
                );

        controller.selecionarEscala(
                escala.getId()
        );

        controller.adicionarParte(
                novaParte,
                novoResponsavel,
                null
        );

        Escala atualizada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                2,
                atualizada.getDesignacoes().size()
        );

        assertTrue(
                atualizada.getDesignacoes()
                        .stream()
                        .anyMatch(d ->
                                d.parte().equals(novaParte)
                                        &&
                                        d.responsavel().getId()
                                                .equals(novoResponsavel.getId())
                        )
        );
    }


    @Test
    void deveImpedirAdicionarParteDuplicada() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteSemAjudante("Parte Única");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscala(
                        data,
                        new Designacao(
                                data,
                                parte,
                                responsavel,
                                null
                        )
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                null
                        )
                );

        assertEquals(
                "Esta parte já está adicionada nesta escala.",
                excecao.getMessage()
        );
    }


    @Test
    void deveImpedirResponsavelInvalido() {

        Pessoa responsavelValido =
                criarPessoa("Responsavel Válido");

        Pessoa responsavelInvalido =
                criarPessoaInativo("Responsavel Inativo");

        Parte parte =
                criarParteSemAjudante("Parte");

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavelInvalido,
                                null
                        )
                );

        assertEquals(
                "O responsável selecionado não pode realizar esta parte.",
                excecao.getMessage()
        );

        assertNotNull(responsavelValido);
    }


    @Test
    void deveImpedirAjudanteInvalido() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Pessoa ajudanteInvalido =
                criarPessoaInativo("Ajudante Inativo");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                ajudanteInvalido
                        )
                );

        assertEquals(
                "O ajudante selecionado não pode realizar esta parte.",
                excecao.getMessage()
        );
    }


    @Test
    void deveExigirAjudanteQuandoParteNecessita() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                responsavel,
                                null
                        )
                );

        assertEquals(
                "Esta parte exige um ajudante.",
                excecao.getMessage()
        );
    }


    @Test
    void deveImpedirResponsavelIgualAjudante() {

        Pessoa pessoa =
                criarPessoa("Mesma Pessoa");

        Parte parte =
                criarParteComAjudante();

        Escala escala =
                criarEscalaVazia(
                        LocalDate.of(2026, 9, 3)
                );

        controller.selecionarEscala(
                escala.getId()
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> controller.adicionarParte(
                                parte,
                                pessoa,
                                pessoa
                        )
                );

        assertEquals(
                "O responsável e o ajudante devem ser pessoas diferentes.",
                excecao.getMessage()
        );
    }


    @Test
    void devePersistirNovaDesignacaoAoAdicionarParte() {

        Pessoa responsavel =
                criarPessoa("Responsavel");

        Parte parte =
                criarParteSemAjudante("Nova Parte");

        LocalDate data =
                LocalDate.of(2026, 9, 3);

        Escala escala =
                criarEscalaVazia(data);

        controller.selecionarEscala(
                escala.getId()
        );

        controller.adicionarParte(
                parte,
                responsavel,
                null
        );

        Escala recuperada =
                escalaDAO.buscarPorId(
                        escala.getId()
                ).orElseThrow();

        assertEquals(
                1,
                recuperada.getDesignacoes().size()
        );

        Designacao designacao =
                recuperada.getDesignacoes()
                        .get(0);

        assertNotNull(
                designacao.id()
        );

        assertEquals(
                data,
                designacao.data()
        );

        assertEquals(
                parte.getId(),
                designacao.parte().getId()
        );

        assertEquals(
                responsavel.getId(),
                designacao.responsavel().getId()
        );

        assertNull(
                designacao.ajudante()
        );
    }


    private Pessoa criarPessoa(String nome) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        true,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return pessoaDAO.salvar(pessoa);
    }

    private Pessoa pessoa(
            String nome,
            Sexo sexo,
            boolean ativo,
            Privilegio privilegio,
            NivelLeitura nivelLeitura,
            boolean podeSerResponsavel
    ) {
        return pessoaDAO.salvar(
                new Pessoa(
                        nome,
                        sexo,
                        ativo,
                        podeSerResponsavel,
                        true,
                        true,
                        false,
                        false,
                        false,
                        false,
                        privilegio,
                        nivelLeitura
                )
        );
    }

    private Pessoa criarPessoaComPermissoes(
            String nome,
            Sexo sexo,
            Privilegio privilegio,
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            boolean oracao,
            boolean presidente,
            boolean dirigente
    ) {
        return criarPessoaComPermissoes(
                nome,
                sexo,
                privilegio,
                responsavel,
                ajudante,
                leitura,
                discurso,
                oracao,
                presidente,
                dirigente,
                NivelLeitura.BASICO
        );
    }

    private Pessoa criarPessoaComPermissoes(
            String nome,
            Sexo sexo,
            Privilegio privilegio,
            boolean responsavel,
            boolean ajudante,
            boolean leitura,
            boolean discurso,
            boolean oracao,
            boolean presidente,
            boolean dirigente,
            NivelLeitura nivelLeitura
    ) {
        return pessoaDAO.salvar(
                new Pessoa(
                        nome,
                        sexo,
                        true,
                        responsavel,
                        ajudante,
                        leitura,
                        discurso,
                        oracao,
                        presidente,
                        dirigente,
                        privilegio,
                        nivelLeitura
                )
        );
    }


    private Pessoa criarPessoaInativo(String nome) {

        Pessoa pessoa =
                new Pessoa(
                        nome,
                        Sexo.MASCULINO,
                        false,
                        true,
                        true,
                        false,
                        false,
                        Privilegio.ANCIAO
                );

        return pessoaDAO.salvar(pessoa);
    }


    private Parte criarParteSemAjudante() {

        return criarParteSemAjudante("Parte");
    }


    private Parte criarParteSemAjudante(String nome) {

        Parte parte =
                new Parte(
                        nome,
                        TipoParte.LEITURA,
                        Privilegio.PUBLICADOR,
                        false,
                        SexoPermitido.AMBOS,
                        1,
                        false,
                        List.of(
                                TipoParticipacao.RESPONSAVEL
                        )
                );

        return parteDAO.salvar(parte);
    }


    private Parte criarParteComAjudante() {

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

        return parteDAO.salvar(parte);
    }


    private Escala criarEscala(
            LocalDate data,
            Designacao designacao
    ) {

        return escalaDAO.salvar(
                new Escala(
                        data,
                        List.of(designacao)
                )
        );
    }


    private Escala criarEscalaVazia(
            LocalDate data
    ) {

        return escalaDAO.salvar(
                new Escala(
                        data,
                        List.of()
                )
        );
    }

}