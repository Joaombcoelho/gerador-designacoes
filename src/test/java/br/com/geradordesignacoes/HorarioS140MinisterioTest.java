package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.HorarioManualS140DAO;
import br.com.geradordesignacoes.model.ConfiguracaoGeral;
import br.com.geradordesignacoes.model.HorarioS140Item;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.Privilegio;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.SexoPermitido;
import br.com.geradordesignacoes.model.NivelLeitura;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.CalculadoraHorarioS140;
import br.com.geradordesignacoes.service.ConfiguracaoGeralService;
import br.com.geradordesignacoes.service.HorarioS140Service;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HorarioS140MinisterioTest {

    @Test
    void naoDeveAdicionarDezoitoMinutosFixosQuandoMinisterioNaoTemPartes() {
        HorarioS140Service service = criarService();
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                LocalDate.of(2027, 7, 7)
        );

        List<HorarioS140Item> itens = service.criarItens(programacao);

        assertFalse(itens.stream()
                .anyMatch(item -> item.chave().equals("BLOCO_MINISTERIO")));
    }

    @Test
    void deveUsarDuracaoDaParteDoMinisterioEmVezDeDezoitoMinutosFixos() {
        HorarioS140Service service = criarService();
        ProgramacaoSemana programacao = new ProgramacaoSemana(
                LocalDate.of(2027, 7, 7)
        );

        Parte demonstracao = new Parte(
                10,
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                Privilegio.PUBLICADOR,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                NivelLeitura.BASICO,
                SecaoParte.MINISTERIO,
                TipoVariacaoParte.VARIAVEL,
                true,
                1,
                List.of()
        );
        ProgramacaoParte parteProgramada = new ProgramacaoParte(
                demonstracao,
                1
        );
        parteProgramada.setDuracaoMinutos(5);
        programacao.adicionarParte(parteProgramada);

        List<HorarioS140Item> itens = service.criarItens(programacao);

        HorarioS140Item itemMinisterio = itens.stream()
                .filter(item -> item.chave().equals("PARTE_10"))
                .findFirst()
                .orElseThrow();

        assertEquals(5, itemMinisterio.duracaoMinutos());
        assertFalse(itens.stream()
                .anyMatch(item -> item.chave().equals("BLOCO_MINISTERIO")));
    }

    private HorarioS140Service criarService() {
        ConfiguracaoGeralService configuracaoService =
                new ConfiguracaoGeralService() {
                    @Override
                    public ConfiguracaoGeral buscar() {
                        return ConfiguracaoGeral.padrao();
                    }
                };

        return new HorarioS140Service(
                configuracaoService,
                new HorarioManualS140DAO(),
                new CalculadoraHorarioS140()
        );
    }
}
