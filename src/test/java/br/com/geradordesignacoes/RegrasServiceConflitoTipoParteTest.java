package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.ControleDesignacoes;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

class RegrasServiceConflitoTipoParteTest {

    private final RegrasService regrasService = new RegrasService();
    private final LocalDate dataReuniao = LocalDate.of(2026, 10, 20);

    @Test
    void deveBloquearTesourosQuandoPessoaJaTemJoiasComNomePersonalizado() {
        Pessoa pessoa = criarPessoa();
        Parte joias = criarParte(
                "Estudo aprofundado personalizado",
                TipoParte.JOIAS_ESPIRITUAIS
        );
        Parte tesouros = criarParte(
                "Palestra de abertura",
                TipoParte.DISCURSO_TESOUROS
        );
        ControleDesignacoes controle = new ControleDesignacoes(
                List.of(new ParticipacaoDesignacao(
                        dataReuniao,
                        pessoa,
                        joias,
                        TipoParticipacao.RESPONSAVEL
                ))
        );

        assertFalse(regrasService.podeDesignar(
                pessoa,
                tesouros,
                controle,
                dataReuniao
        ));
    }

    @Test
    void deveBloquearJoiasQuandoPessoaJaTemTesourosComNomePersonalizado() {
        Pessoa pessoa = criarPessoa();
        Parte tesouros = criarParte(
                "Mensagem temática personalizada",
                TipoParte.DISCURSO_TESOUROS
        );
        Parte joias = criarParte(
                "Considerações da semana",
                TipoParte.JOIAS_ESPIRITUAIS
        );
        ControleDesignacoes controle = new ControleDesignacoes(
                List.of(new ParticipacaoDesignacao(
                        dataReuniao,
                        pessoa,
                        tesouros,
                        TipoParticipacao.RESPONSAVEL
                ))
        );

        assertFalse(regrasService.podeDesignar(
                pessoa,
                joias,
                controle,
                dataReuniao
        ));
    }

    private Pessoa criarPessoa() {
        return new Pessoa(
                "Pessoa de teste",
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                false,
                Privilegio.BATIZADO
        );
    }

    private Parte criarParte(String nome, TipoParte tipo) {
        return new Parte(
                nome,
                tipo,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.RESPONSAVEL)
        );
    }
}
