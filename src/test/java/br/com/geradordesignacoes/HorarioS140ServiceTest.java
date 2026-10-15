package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ConfiguracaoGeralDAO;
import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.HorarioS140Item;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.CalculadoraHorarioS140;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import br.com.geradordesignacoes.service.HorarioS140Service;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorarioS140ServiceTest extends BaseDAOTest {

    @Test
    void deveUsarPartesReaisDoMinisterioENossaVidaCristaSemSomarBlocosFixos() {
        salvarConfiguracaoPadrao();
        ProgramacaoSemana programacao = new ProgramacaoSemana(LocalDate.of(2027, 6, 2));

        programacao.adicionarParte(criarParteProgramada(
                1, "Demonstração", TipoParte.DEMONSTRACAO,
                SecaoParte.MINISTERIO, 2, 4));
        programacao.adicionarParte(criarParteProgramada(
                2, "Iniciando conversas", TipoParte.INICIANDO_CONVERSAS,
                SecaoParte.MINISTERIO, 1, 5));
        programacao.adicionarParte(criarParteProgramada(
                3, "Necessidades locais", TipoParte.NECESSIDADES_LOCAIS,
                SecaoParte.NOSSA_VIDA_CRISTA, 3, 6));
        programacao.adicionarParte(criarParteProgramada(
                4, "Discurso", TipoParte.DISCURSO,
                SecaoParte.NOSSA_VIDA_CRISTA, 4, 9));
        programacao.adicionarParte(criarParteProgramada(
                5, "Estudo Bíblico", TipoParte.DIRIGENTE_ESTUDO,
                SecaoParte.NOSSA_VIDA_CRISTA, 5, 30));

        HorarioS140Service service = criarService();
        List<HorarioS140Item> itens = service.criarItens(programacao);

        assertFalse(itens.stream().anyMatch(item -> item.chave().equals("BLOCO_MINISTERIO")));
        assertFalse(itens.stream().anyMatch(item -> item.chave().equals("BLOCO_NOSSA_VIDA_CRISTA")));
        assertEquals(List.of("PARTE_2", "PARTE_1", "PARTE_3", "PARTE_4"),
                itens.stream()
                        .filter(item -> item.chave().startsWith("PARTE_"))
                        .map(HorarioS140Item::chave)
                        .toList());

        assertEquals(4, itens.stream()
                .filter(item -> item.chave().startsWith("PARTE_"))
                .count());
        assertEquals(30, itens.stream()
                .filter(item -> item.chave().equals("ESTUDO_BIBLICO"))
                .findFirst()
                .orElseThrow()
                .duracaoMinutos());
    }

    @Test
    void deveCalcularHorariosPelaDuracaoDasPartesSemAcrescentarDezoitoMinutos() {
        salvarConfiguracaoPadrao();
        ProgramacaoSemana programacao = new ProgramacaoSemana(LocalDate.of(2027, 6, 9));
        programacao.adicionarParte(criarParteProgramada(
                11, "Demonstração", TipoParte.DEMONSTRACAO,
                SecaoParte.MINISTERIO, 1, 4));
        programacao.adicionarParte(criarParteProgramada(
                12, "Necessidades locais", TipoParte.NECESSIDADES_LOCAIS,
                SecaoParte.NOSSA_VIDA_CRISTA, 2, 6));

        List<br.com.geradordesignacoes.model.HorarioS140Calculado> calculados =
                criarService().calcular(programacao);

        var ministerio = calculados.stream()
                .filter(item -> item.chave().equals("PARTE_11"))
                .findFirst()
                .orElseThrow();
        var vidaCrista = calculados.stream()
                .filter(item -> item.chave().equals("PARTE_12"))
                .findFirst()
                .orElseThrow();
        var estudo = calculados.stream()
                .filter(item -> item.chave().equals("ESTUDO_BIBLICO"))
                .findFirst()
                .orElseThrow();

        // 19:30 + 5 min iniciais + 1 min de comentários + 4 min de cântico do meio.
        assertEquals(LocalTime.of(19, 40), ministerio.horarioInicio());
        assertEquals(LocalTime.of(19, 44), vidaCrista.horarioInicio());
        assertEquals(LocalTime.of(19, 50), estudo.horarioInicio());
    }

    private void salvarConfiguracaoPadrao() {
        new ConfiguracaoGeralDAO().salvar(new ConfiguracaoGeral(
                "Teste", DayOfWeek.WEDNESDAY, LocalTime.of(19, 30),
                5, 1, 4, 3, 5
        ));
    }

    private HorarioS140Service criarService() {
        return new HorarioS140Service(
                new ConfiguracaoGeralService(),
                new HorarioManualS140DAO(),
                new CalculadoraHorarioS140()
        );
    }

    private ProgramacaoParte criarParteProgramada(
            int id,
            String nome,
            TipoParte tipo,
            SecaoParte secao,
            int ordem,
            int duracao
    ) {
        Parte parte = new Parte(
                id,
                nome,
                tipo,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                secao,
                TipoVariacaoParte.VARIAVEL,
                true,
                ordem,
                List.of()
        );
        parte.setDuracaoPadraoMinutos(duracao);

        ProgramacaoParte programacaoParte = new ProgramacaoParte(parte, ordem, null);
        programacaoParte.setDuracaoMinutos(duracao);
        return programacaoParte;
    }
}
