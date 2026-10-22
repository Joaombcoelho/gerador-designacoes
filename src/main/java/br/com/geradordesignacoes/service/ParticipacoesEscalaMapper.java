package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.Designacao;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ParticipacaoDesignacao;
import br.com.geradordesignacoes.model.SalaDesignacao;
import br.com.geradordesignacoes.model.TipoParte;
import br.com.geradordesignacoes.model.TipoParticipacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Converte as designações atuais de uma escala em participações históricas.
 * Usado no momento da confirmação, depois de eventuais ajustes manuais na prévia.
 */
public final class ParticipacoesEscalaMapper {

    private ParticipacoesEscalaMapper() {
    }

    public static List<ParticipacaoDesignacao> mapear(Escala escala) {
        Objects.requireNonNull(escala, "A escala não pode ser nula.");

        List<ParticipacaoDesignacao> participacoes = new ArrayList<>();
        for (Designacao designacao : escala.getDesignacoes()) {
            Parte parte = designacao.parte();

            if (designacao.responsavel() != null) {
                participacoes.add(new ParticipacaoDesignacao(
                        designacao.data(),
                        designacao.responsavel(),
                        parte,
                        tipoResponsavel(parte),
                        designacao.sala()
                ));
            }

            if (designacao.ajudante() != null) {
                participacoes.add(new ParticipacaoDesignacao(
                        designacao.data(),
                        designacao.ajudante(),
                        parte,
                        TipoParticipacao.AJUDANTE,
                        designacao.sala()
                ));
            }
        }

        return List.copyOf(participacoes);
    }

    /**
     * Substitui as participações da Sala Principal pela escala atual,
     * preservando as participações já registradas nas salas auxiliares
     * da mesma data. Histórico de outras datas nunca é incluído no lote.
     */
    public static List<ParticipacaoDesignacao> mapearPreservandoSalasAuxiliares(
            Escala escala,
            List<ParticipacaoDesignacao> historicoExistente
    ) {
        Objects.requireNonNull(historicoExistente, "O histórico existente não pode ser nulo.");

        List<ParticipacaoDesignacao> participacoes = new ArrayList<>();
        historicoExistente.stream()
                .filter(p -> p.data().equals(escala.getData()))
                .filter(p -> p.sala() != SalaDesignacao.PRINCIPAL)
                .forEach(participacoes::add);
        participacoes.addAll(mapear(escala));
        return List.copyOf(participacoes);
    }

    private static TipoParticipacao tipoResponsavel(Parte parte) {
        if (parte.getTipo() == TipoParte.DIRIGENTE_ESTUDO) {
            return TipoParticipacao.DIRIGENTE;
        }

        return parte.getParticipacoesNecessarias().stream()
                .filter(tipo -> tipo != TipoParticipacao.AJUDANTE)
                .findFirst()
                .orElse(TipoParticipacao.RESPONSAVEL);
    }
}
