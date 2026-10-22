package br.com.geradordesignacoes;

import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.ParticipacoesEscalaMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipacoesEscalaMapperTest {

    @Test
    void mapeiaResponsavelEAjudanteMantendoDataESala() {
        LocalDate data = LocalDate.of(2026, 11, 4);
        Pessoa responsavel = pessoa(1, "Responsável");
        Pessoa ajudante = pessoa(2, "Ajudante");
        Parte parte = new Parte(
                "Demonstração",
                TipoParte.DEMONSTRACAO,
                Privilegio.BATIZADO,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                List.of(TipoParticipacao.RESPONSAVEL, TipoParticipacao.AJUDANTE)
        );
        Escala escala = new Escala(data, List.of(
                new Designacao(data, parte, responsavel, ajudante, SalaDesignacao.B)
        ));

        List<ParticipacaoDesignacao> resultado =
                ParticipacoesEscalaMapper.mapear(escala);

        assertEquals(2, resultado.size());
        assertEquals(responsavel, resultado.get(0).pessoa());
        assertEquals(TipoParticipacao.RESPONSAVEL, resultado.get(0).tipoParticipacao());
        assertEquals(ajudante, resultado.get(1).pessoa());
        assertEquals(TipoParticipacao.AJUDANTE, resultado.get(1).tipoParticipacao());
        assertEquals(data, resultado.get(0).data());
        assertEquals(SalaDesignacao.B, resultado.get(0).sala());
        assertEquals(SalaDesignacao.B, resultado.get(1).sala());
    }

    @Test
    void mapeiaDirigenteDoEstudoBiblicoComTipoHistoricoCorreto() {
        LocalDate data = LocalDate.of(2026, 11, 11);
        Pessoa dirigente = pessoa(3, "Dirigente");
        Parte parte = new Parte(
                "Estudo Bíblico",
                TipoParte.DIRIGENTE_ESTUDO,
                Privilegio.BATIZADO,
                true,
                SexoPermitido.AMBOS,
                2,
                false,
                List.of(TipoParticipacao.DIRIGENTE, TipoParticipacao.AJUDANTE)
        );
        Escala escala = new Escala(data, List.of(
                new Designacao(data, parte, dirigente, null)
        ));

        List<ParticipacaoDesignacao> resultado =
                ParticipacoesEscalaMapper.mapear(escala);

        assertEquals(1, resultado.size());
        assertEquals(TipoParticipacao.DIRIGENTE, resultado.getFirst().tipoParticipacao());
        assertEquals(dirigente, resultado.getFirst().pessoa());
    }

    @Test
    void substituiSalaPrincipalEPreservaHistoricoDasSalasAuxiliares() {
        LocalDate data = LocalDate.of(2026, 11, 18);
        Pessoa participanteAtual = pessoa(4, "Participante atual");
        Pessoa participanteAntigo = pessoa(5, "Participante antigo");
        Parte parte = new Parte(
                "Leitura",
                TipoParte.LEITURA,
                Privilegio.BATIZADO,
                false,
                SexoPermitido.AMBOS,
                1,
                false,
                List.of(TipoParticipacao.LEITOR)
        );
        Escala escala = new Escala(data, List.of(
                new Designacao(data, parte, participanteAtual, null, SalaDesignacao.PRINCIPAL)
        ));
        List<ParticipacaoDesignacao> historicoExistente = List.of(
                new ParticipacaoDesignacao(data, participanteAntigo, parte,
                        TipoParticipacao.LEITOR, SalaDesignacao.PRINCIPAL),
                new ParticipacaoDesignacao(data, participanteAntigo, parte,
                        TipoParticipacao.LEITOR, SalaDesignacao.B),
                new ParticipacaoDesignacao(LocalDate.of(2026, 11, 25),
                        participanteAntigo, parte, TipoParticipacao.LEITOR, SalaDesignacao.C)
        );

        List<ParticipacaoDesignacao> resultado =
                ParticipacoesEscalaMapper.mapearPreservandoSalasAuxiliares(
                        escala,
                        historicoExistente
                );

        assertEquals(2, resultado.size());
        assertEquals(participanteAtual, resultado.get(0).pessoa());
        assertEquals(SalaDesignacao.PRINCIPAL, resultado.get(0).sala());
        assertEquals(participanteAntigo, resultado.get(1).pessoa());
        assertEquals(SalaDesignacao.B, resultado.get(1).sala());
        assertEquals(data, resultado.get(1).data());
    }

    private Pessoa pessoa(int id, String nome) {
        Pessoa pessoa = new Pessoa(
                nome,
                Sexo.MASCULINO,
                true,
                true,
                true,
                true,
                true,
                Privilegio.BATIZADO
        );
        pessoa.setId(id);
        return pessoa;
    }
}
