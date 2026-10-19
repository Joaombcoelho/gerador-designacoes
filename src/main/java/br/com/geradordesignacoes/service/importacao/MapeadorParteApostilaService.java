package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.SecaoParte;
import br.com.geradordesignacoes.model.TipoParte;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Sugere correspondências entre partes do JSON da apostila e o catálogo
 * já cadastrado no gerador. Este serviço é somente de consulta:
 * não cria partes, não cria programações e não grava no banco.
 */
public class MapeadorParteApostilaService {

    public ResultadoMapeamento mapear(
            ImportacaoApostilaJsonService.ParteApostila parteApostila,
            List<Parte> catalogo
    ) {
        Objects.requireNonNull(parteApostila, "A parte da apostila é obrigatória.");
        Objects.requireNonNull(catalogo, "O catálogo de partes é obrigatório.");

        TipoParte tipoInferido = inferirTipo(parteApostila);
        List<Parte> candidatos = new ArrayList<>();

        for (Parte parte : catalogo) {
            if (parte == null || parte.getSecao() != parteApostila.secao()) {
                continue;
            }
            if (tipoInferido != null && parte.getTipo() != tipoInferido) {
                continue;
            }
            candidatos.add(parte);
        }

        candidatos.sort(Comparator.comparingInt(Parte::getOrdem)
                .thenComparing(Parte::getNome, String.CASE_INSENSITIVE_ORDER));

        SituacaoMapeamento situacao;
        if (tipoInferido == null) {
            situacao = SituacaoMapeamento.REQUER_CONFERENCIA;
        } else if (candidatos.isEmpty()) {
            situacao = SituacaoMapeamento.NAO_ENCONTRADO;
        } else if (candidatos.size() == 1) {
            situacao = SituacaoMapeamento.CORRESPONDENCIA_UNICA;
        } else {
            situacao = SituacaoMapeamento.AMBIGUO;
        }

        return new ResultadoMapeamento(
                parteApostila.numero(),
                parteApostila.tema(),
                parteApostila.duracaoMinutos(),
                parteApostila.secao(),
                tipoInferido,
                situacao,
                candidatos
        );
    }

    /**
     * Infere somente os tipos que podem ser reconhecidos por um subtítulo
     * explícito. Não tenta adivinhar pelo número da parte ou apenas pela seção.
     */
    public TipoParte inferirTipo(
            ImportacaoApostilaJsonService.ParteApostila parte
    ) {
        Objects.requireNonNull(parte, "A parte da apostila é obrigatória.");
        String texto = normalizar(
                juntar(parte.tema(), parte.subtitulo(), parte.descricaoDesignacao())
        );

        // Na última seção, o tema identifica o Estudo Bíblico e a sequência
        // da apostila identifica as partes numeradas. O número nunca é ID do banco.
        if (parte.secao() == SecaoParte.NOSSA_VIDA_CRISTA) {
            if (contem(texto, "estudo biblico")) {
                return TipoParte.DIRIGENTE_ESTUDO;
            }
            if (parte.numero() == 7) {
                return TipoParte.PARTE_1;
            }
            if (parte.numero() == 8) {
                return TipoParte.PARTE_2;
            }
        }

        if (contem(texto, "iniciando conversas")) {
            return TipoParte.INICIANDO_CONVERSAS;
        }
        if (contem(texto, "cultivando interesse")) {
            return TipoParte.CULTIVANDO_INTERESSE;
        }
        if (contem(texto, "fazendo discipulos")) {
            return TipoParte.FAZENDO_DISCIPULOS;
        }
        if (contem(texto, "explicando suas crencas")) {
            return TipoParte.EXPLICANDO_SUAS_CRENCAS;
        }
        if (contem(texto, "o que voce diria")) {
            return TipoParte.O_QUE_VOCE_DIRIA;
        }
        if (contem(texto, "joias espirituais")) {
            return TipoParte.JOIAS_ESPIRITUAIS;
        }
        if (contem(texto, "discurso de tesouros")) {
            return TipoParte.DISCURSO_TESOUROS;
        }
        if (contem(texto, "leitura da biblia")) {
            return TipoParte.LEITURA;
        }
        if (contem(texto, "necessidades locais")) {
            return TipoParte.NECESSIDADES_LOCAIS;
        }
        return null;
    }

    private static String juntar(String tema, String subtitulo, String descricao) {
        return (tema == null ? "" : tema) + " "
                + (subtitulo == null ? "" : subtitulo) + " "
                + (descricao == null ? "" : descricao);
    }

    private static boolean contem(String texto, String expressao) {
        return texto.contains(expressao);
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public enum SituacaoMapeamento {
        CORRESPONDENCIA_UNICA,
        AMBIGUO,
        NAO_ENCONTRADO,
        REQUER_CONFERENCIA
    }

    public record ResultadoMapeamento(
            int numeroParteApostila,
            String tema,
            int duracaoMinutos,
            SecaoParte secao,
            TipoParte tipoInferido,
            SituacaoMapeamento situacao,
            List<Parte> candidatos
    ) {
        public ResultadoMapeamento {
            candidatos = List.copyOf(candidatos);
        }

        /**
         * Uma sugestão só é automática quando o tipo foi reconhecido
         * e existe exatamente um registro correspondente no catálogo.
         */
        public boolean possuiCorrespondenciaUnica() {
            return situacao == SituacaoMapeamento.CORRESPONDENCIA_UNICA;
        }
    }
}
