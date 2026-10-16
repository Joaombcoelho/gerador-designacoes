package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.model.SecaoParte;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lê e valida o JSON produzido pelo protótipo ExtratorApostilaRtf.
 *
 * Esta etapa é somente de leitura: não acessa DAOs, não grava no banco
 * e não modifica programações existentes.
 */
public class ImportacaoApostilaJsonService {

    private final ObjectMapper objectMapper;

    public ImportacaoApostilaJsonService() {
        this(new ObjectMapper());
    }

    ImportacaoApostilaJsonService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResultadoValidacao validar(Path arquivo) throws IOException {
        if (arquivo == null) {
            throw new IllegalArgumentException("Selecione um arquivo JSON.");
        }
        if (!java.nio.file.Files.isRegularFile(arquivo)) {
            throw new IllegalArgumentException("O arquivo JSON selecionado não existe.");
        }

        JsonNode raiz = objectMapper.readTree(arquivo.toFile());
        if (raiz == null || !raiz.isObject()) {
            throw new IOException("O JSON precisa conter um objeto na raiz.");
        }

        List<String> erros = new ArrayList<>();
        List<String> avisos = new ArrayList<>();
        List<SemanaApostila> semanas = new ArrayList<>();

        JsonNode semanasNode = raiz.path("semanas");
        if (!semanasNode.isArray() || semanasNode.isEmpty()) {
            erros.add("O JSON não contém uma lista 'semanas' válida.");
            return new ResultadoValidacao(semanas, erros, avisos);
        }

        if (!"1.0".equals(raiz.path("schema_version").asText())) {
            avisos.add("A versão do formato JSON não é reconhecida; confira a compatibilidade do arquivo.");
        }
        if (!"pendente".equalsIgnoreCase(raiz.path("status_conferencia").asText("pendente"))) {
            avisos.add("O arquivo não está marcado como pendente de conferência. Os dados ainda serão validados.");
        }

        Set<LocalDate> datasIniciais = new HashSet<>();
        int indiceSemana = 0;
        for (JsonNode semanaNode : semanasNode) {
            indiceSemana++;
            String contexto = "Semana " + indiceSemana;
            if (!semanaNode.isObject()) {
                erros.add(contexto + ": o item não é um objeto JSON.");
                continue;
            }

            LocalDate inicio = lerData(semanaNode.path("data_inicio"), contexto + ", data inicial", erros);
            LocalDate fim = lerData(semanaNode.path("data_fim"), contexto + ", data final", erros);
            if (inicio == null || fim == null) {
                continue;
            }
            if (fim.isBefore(inicio)) {
                erros.add(contexto + ": a data final é anterior à data inicial.");
                continue;
            }
            if (!datasIniciais.add(inicio)) {
                erros.add(contexto + ": existe outra semana com a mesma data inicial (" + inicio + ").");
            }

            String arquivoOrigem = textoOpcional(semanaNode.path("arquivo_origem"));
            String leitura = textoOpcional(semanaNode.path("leitura_biblica"));
            if (leitura == null) {
                avisos.add(contexto + ": leitura bíblica não identificada.");
            }

            JsonNode canticosNode = semanaNode.path("canticos");
            CanticosApostila canticos = new CanticosApostila(
                    lerCantico(canticosNode.path("abertura"), contexto + ", cântico inicial", erros),
                    lerCantico(canticosNode.path("intermediario"), contexto + ", cântico intermediário", erros),
                    lerCantico(canticosNode.path("encerramento"), contexto + ", cântico final", erros)
            );
            if (canticos.quantidadePreenchida() != 3) {
                avisos.add(contexto + ": foram identificados " + canticos.quantidadePreenchida()
                        + " de 3 cânticos; conferir a apostila.");
            }

            JsonNode partesNode = semanaNode.path("partes");
            List<ParteApostila> partes = new ArrayList<>();
            Set<Integer> numeros = new HashSet<>();
            if (!partesNode.isArray()) {
                erros.add(contexto + ": a propriedade 'partes' não é uma lista.");
            } else {
                int indiceParte = 0;
                for (JsonNode parteNode : partesNode) {
                    indiceParte++;
                    String contextoParte = contexto + ", item de parte " + indiceParte;
                    if (!parteNode.isObject()) {
                        erros.add(contextoParte + ": o item não é um objeto JSON.");
                        continue;
                    }

                    Integer numero = inteiroPositivo(parteNode.path("numero"));
                    if (numero == null || numero > 8) {
                        erros.add(contextoParte + ": número da parte inválido; esperado de 1 a 8.");
                        continue;
                    }
                    if (!numeros.add(numero)) {
                        erros.add(contexto + ": o número de parte " + numero + " está duplicado.");
                        continue;
                    }

                    String tema = textoOpcional(parteNode.path("tema"));
                    if (tema == null) {
                        erros.add(contextoParte + ": o tema está vazio.");
                        continue;
                    }
                    Integer duracao = inteiroPositivo(parteNode.path("duracao_minutos"));
                    if (duracao == null || duracao > 120) {
                        erros.add(contextoParte + ": duração inválida.");
                        continue;
                    }

                    SecaoParte secao = mapearSecao(parteNode.path("secao").asText(null));
                    if (secao == null) {
                        erros.add(contextoParte + ": seção ausente ou não reconhecida: "
                                + parteNode.path("secao").asText("(ausente)") + ".");
                        continue;
                    }

                    String secaoTitulo = textoOpcional(parteNode.path("secao_titulo"));
                    String subtitulo = textoOpcional(parteNode.path("subtitulo"));
                    String descricao = textoOpcional(parteNode.path("descricao_designacao"));
                    if (numero >= 4 && numero <= 6 && subtitulo == null && descricao == null) {
                        avisos.add(contextoParte + ": tipo de designação não identificado automaticamente.");
                    }

                    partes.add(new ParteApostila(numero, tema, duracao, secao,
                            secaoTitulo, subtitulo, descricao));
                }
            }

            if (partes.size() != 8) {
                erros.add(contexto + ": foram validadas " + partes.size()
                        + " partes; são esperadas 8.");
            }
            for (int numero = 1; numero <= 8; numero++) {
                if (!numeros.contains(numero)) {
                    erros.add(contexto + ": a parte " + numero + " não foi identificada.");
                }
            }
            partes.sort(java.util.Comparator.comparingInt(ParteApostila::numero));

            semanas.add(new SemanaApostila(arquivoOrigem, inicio, fim, leitura, canticos, partes));
        }

        JsonNode alertasOrigem = raiz.path("alertas");
        if (alertasOrigem.isArray()) {
            for (JsonNode alerta : alertasOrigem) {
                if (alerta.isTextual() && !alerta.asText().isBlank()) {
                    avisos.add("Extrator: " + alerta.asText());
                }
            }
        }

        return new ResultadoValidacao(semanas, erros, avisos);
    }

    private static LocalDate lerData(JsonNode node, String contexto, List<String> erros) {
        if (!node.isTextual() || node.asText().isBlank()) {
            erros.add(contexto + ": data ausente.");
            return null;
        }
        try {
            return LocalDate.parse(node.asText());
        } catch (DateTimeParseException e) {
            erros.add(contexto + ": data inválida '" + node.asText() + "'.");
            return null;
        }
    }

    private static Integer lerCantico(JsonNode node, String contexto, List<String> erros) {
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        Integer numero = inteiroPositivo(node);
        if (numero == null || numero > 200) {
            erros.add(contexto + ": número inválido.");
            return null;
        }
        return numero;
    }

    private static Integer inteiroPositivo(JsonNode node) {
        if (!node.isIntegralNumber() || !node.canConvertToInt()) {
            return null;
        }
        int valor = node.intValue();
        return valor > 0 ? valor : null;
    }

    private static String textoOpcional(JsonNode node) {
        if (!node.isTextual()) {
            return null;
        }
        String valor = node.asText().trim();
        return valor.isEmpty() ? null : valor;
    }

    private static SecaoParte mapearSecao(String secao) {
        if (secao == null) {
            return null;
        }
        return switch (secao.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "tesouros" -> SecaoParte.TESOUROS_DA_PALAVRA_DE_DEUS;
            case "ministerio" -> SecaoParte.MINISTERIO;
            case "vida_crista" -> SecaoParte.NOSSA_VIDA_CRISTA;
            default -> null;
        };
    }

    public record ResultadoValidacao(
            List<SemanaApostila> semanas,
            List<String> erros,
            List<String> avisos
    ) {
        public ResultadoValidacao {
            semanas = List.copyOf(semanas);
            erros = List.copyOf(erros);
            avisos = List.copyOf(avisos);
        }

        public boolean validoParaPrevia() {
            return erros.isEmpty() && !semanas.isEmpty();
        }

        public boolean podeProsseguirParaPrevia() {
            return validoParaPrevia();
        }
    }

    public record SemanaApostila(
            String arquivoOrigem,
            LocalDate dataInicio,
            LocalDate dataFim,
            String leituraBiblica,
            CanticosApostila canticos,
            List<ParteApostila> partes
    ) {
        public SemanaApostila {
            partes = List.copyOf(partes);
        }
    }

    public record CanticosApostila(Integer abertura, Integer intermediario, Integer encerramento) {
        public int quantidadePreenchida() {
            int quantidade = 0;
            if (abertura != null) quantidade++;
            if (intermediario != null) quantidade++;
            if (encerramento != null) quantidade++;
            return quantidade;
        }
    }

    public record ParteApostila(
            int numero,
            String tema,
            int duracaoMinutos,
            SecaoParte secao,
            String secaoTitulo,
            String subtitulo,
            String descricaoDesignacao
    ) {
    }
}
