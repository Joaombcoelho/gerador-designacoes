package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegrasServiceHabilidadesTest {

    private final RegrasService regras = new RegrasService();

    @Test
    void deveExigirHabilidadeCorrespondenteMesmoQuandoCategoriaPermiteParte() {
        Pessoa pessoa = pessoa(Set.of());
        Parte discurso = parte(TipoParte.DISCURSO, TipoParticipacao.ORADOR);

        assertFalse(regras.podeDesignar(pessoa, discurso, List.of()));
        assertFalse(regras.podeExercerParticipacao(
                pessoa, discurso, TipoParticipacao.ORADOR));
    }

    @Test
    void devePermitirHabilidadeMasContinuarRespeitandoRegrasDaParte() {
        Pessoa comHabilidade = pessoa(Set.of("DISCURSO"));
        Parte discurso = parte(TipoParte.DISCURSO, TipoParticipacao.ORADOR);

        assertTrue(regras.podeDesignar(comHabilidade, discurso, List.of()));

        Parte leituraExperiente = new Parte(
                "Leitura", TipoParte.LEITURA, Privilegio.BATIZADO,
                false, SexoPermitido.FEMININO, 1, false,
                NivelLeitura.EXPERIENTE, SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS,
                null, false, List.of(TipoParticipacao.LEITOR));

        assertFalse(regras.podeDesignar(comHabilidade, leituraExperiente, List.of()));
    }

    @Test
    void deveExigirHabilidadeParaPresidenciaSemSubstituirRequisitosDePresidente() {
        Parte presidencia = parte(TipoParte.PRESIDENTE_REUNIAO, TipoParticipacao.PRESIDENTE);

        Pessoa semHabilidade = pessoa(Set.of());
        assertFalse(regras.podeDesignar(semHabilidade, presidencia, List.of()));

        Pessoa comHabilidade = pessoa(Set.of("PRESIDENCIA"));
        assertTrue(regras.podeDesignar(comHabilidade, presidencia, List.of()));
    }

    @Test
    void naoDeveExigirHabilidadeNovaParaPartesDoMinisterio() {
        Pessoa pessoa = pessoa(Set.of());
        Parte ministerio = parte(TipoParte.DEMONSTRACAO, TipoParticipacao.RESPONSAVEL);

        assertTrue(regras.podeDesignar(pessoa, ministerio, List.of()));
    }

    private static Pessoa pessoa(Set<String> habilidades) {
        Pessoa pessoa = new Pessoa(
                "João", Sexo.MASCULINO, true,
                true, true, true, true, true, true, true,
                Privilegio.ANCIAO, NivelLeitura.EXPERIENTE);
        pessoa.setId(1);
        pessoa.setHabilidadesDesignacao(habilidades);
        return pessoa;
    }

    private static Parte parte(TipoParte tipo, TipoParticipacao participacao) {
        return new Parte(
                "Parte de teste", tipo, Privilegio.PUBLICADOR,
                false, SexoPermitido.AMBOS, 1, false,
                NivelLeitura.BASICO, SecaoParte.NOSSA_VIDA_CRISTA,
                null, false, List.of(participacao));
    }
}
