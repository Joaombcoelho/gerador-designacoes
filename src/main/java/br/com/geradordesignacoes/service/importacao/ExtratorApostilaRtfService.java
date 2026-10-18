package br.com.geradordesignacoes.service.importacao;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.*;

/**
 * Protótipo isolado para extrair dados de apostilas RTF.
 * Não acessa o banco e não altera a programação do gerador.
 * Uso: java ExtratorApostilaRtf [pasta-com-rtf] [arquivo-json-saida]
 */
public class ExtratorApostilaRtfService {
    private static final Pattern UNICODE = Pattern.compile("\\\\u(-?\\d+)\\??");
    private static final Pattern HEX = Pattern.compile("\\\\'([0-9a-fA-F]{2})");
    private static final Pattern CONTROL = Pattern.compile("\\\\([a-zA-Z]+)(-?\\d+)? ?");
    private static final Pattern WEEK_SAME_MONTH = Pattern.compile("(?is)(\\d{1,2})\\s+a\\s+(\\d{1,2})\\s+de\\s+([\\p{L}-]+)\\s*\\((.{1,100}?)\\)");
    private static final Pattern WEEK_CROSS_MONTH = Pattern.compile("(?is)(\\d{1,2})\\s+de\\s+([\\p{L}-]+)\\s+a\\s+(\\d{1,2})\\s+de\\s+([\\p{L}-]+)\\s*\\((.{1,100}?)\\)");
    private static final Pattern PART = Pattern.compile("(?im)^\\s*(\\d{1,2})\\.\\s+(.+?)\\s*\\((\\d{1,2})\\s*min\\s*\\)\\s*$");
    private static final Pattern SONG = Pattern.compile("(?i)C[âa]ntico\\s+(\\d{1,3})");
    private static final Pattern SECTION = Pattern.compile("(?im)^\\s*(Tesouros da Palavra de Deus|Faça seu melhor no ministério|Nossa vida cristã)\\s*$");
    private static final Pattern SUBTITLE_START = Pattern.compile("(?i)^(De casa em casa|De casa em casa\\.|De loja em loja|Testemunho público|Em lugares públicos|Revisita|Estudo bíblico|Discurso|Explicação|Discurso público|Fazendo discípulos)(?:\\.|\\s|$)");
    private static final Map<String, String> MONTHS = Map.ofEntries(
            Map.entry("janeiro", "01"), Map.entry("fevereiro", "02"), Map.entry("março", "03"),
            Map.entry("marco", "03"), Map.entry("abril", "04"), Map.entry("maio", "05"),
            Map.entry("junho", "06"), Map.entry("julho", "07"), Map.entry("agosto", "08"),
            Map.entry("setembro", "09"), Map.entry("outubro", "10"), Map.entry("novembro", "11"),
            Map.entry("dezembro", "12"));

    /**
     * Extrai os arquivos RTF de uma pasta e devolve o JSON provisório em memória.
     * Não cria arquivos, não abre janelas e não acessa o banco de dados.
     */
    public ResultadoExtracao extrair(Path folder) throws IOException {
        if (folder == null || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("A pasta informada não existe ou não é uma pasta válida.");
        }

        List<Path> files;
        try (var stream = Files.list(folder)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".rtf"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
        if (files.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo .rtf encontrado na pasta selecionada.");
        }

        List<Map<String, Object>> weeks = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (Path file : files) {
            try {
                String text = decodeRtf(Files.readString(file, StandardCharsets.ISO_8859_1));
                Map<String, Object> week = extractWeek(file.getFileName().toString(), text, errors);
                if (week != null) weeks.add(week);
            } catch (Exception e) {
                errors.add(file.getFileName() + ": falha na leitura: " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schema_version", "1.0");
        result.put("origem", "importacao_local_rtf");
        result.put("status_conferencia", "pendente");
        result.put("pasta_origem", folder.toAbsolutePath().toString());
        result.put("arquivos_lidos", files.stream().map(p -> p.getFileName().toString()).toList());
        result.put("semanas", weeks);
        result.put("alertas", errors);

        return new ResultadoExtracao(toJson(result), files.size(), weeks.size(), List.copyOf(errors));
    }

    public record ResultadoExtracao(
            String json,
            int quantidadeArquivos,
            int quantidadeSemanas,
            List<String> alertas
    ) {
    }

    static String decodeRtf(String raw) {
        // Remove instruções técnicas dos campos de hiperlink, preservando o texto visível do link.
        raw = Pattern.compile("\\{\\\\\\*\\\\fldinst\\s*\\{HYPERLINK[^{}]*\\}\\}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
                .matcher(raw).replaceAll("");
        Matcher um = UNICODE.matcher(raw); StringBuffer unicode = new StringBuffer();
        while (um.find()) { int n = Integer.parseInt(um.group(1)); char c = (char)(n < 0 ? n + 65536 : n); um.appendReplacement(unicode, Matcher.quoteReplacement(String.valueOf(c))); }
        um.appendTail(unicode);
        Matcher hm = HEX.matcher(unicode.toString()); StringBuffer hex = new StringBuffer(); Charset cp1252 = Charset.forName("windows-1252");
        while (hm.find()) { String ch = new String(new byte[]{(byte)Integer.parseInt(hm.group(1), 16)}, cp1252); hm.appendReplacement(hex, Matcher.quoteReplacement(ch)); }
        hm.appendTail(hex);
        String s = hex.toString().replaceAll("\\\\(par|line)\\b ?", "\n").replaceAll("\\\\tab\\b ?", "\t");
        s = CONTROL.matcher(s).replaceAll("");
        s = s.replace("\\{", "{").replace("\\}", "}").replace("\\\\", "\\");
        s = s.replace("{", "").replace("}", "");
        s = s.replace('\u00a0', ' ').replaceAll("[\\t ]+", " ").replaceAll(" *\\n *", "\n");
        return s;
    }

    private static Map<String, Object> extractWeek(String filename, String text, List<String> alerts) {
        Matcher wm = WEEK_SAME_MONTH.matcher(text);
        String month;
        String endMonthName;
        int startDay, endDay;
        String reading;
        if (wm.find()) {
            startDay = Integer.parseInt(wm.group(1)); endDay = Integer.parseInt(wm.group(2));
            month = normalize(wm.group(3)); endMonthName = month; reading = wm.group(4).trim();
        } else {
            Matcher cross = WEEK_CROSS_MONTH.matcher(text);
            if (!cross.find()) { alerts.add(filename + ": não foi possível identificar o cabeçalho da semana."); return null; }
            startDay = Integer.parseInt(cross.group(1)); month = normalize(cross.group(2));
            endDay = Integer.parseInt(cross.group(3)); endMonthName = normalize(cross.group(4)); reading = cross.group(5).trim();
        }

        int year = 0;
        Matcher yearMatcher = Pattern.compile("(20\\d{2})").matcher(filename);
        if (yearMatcher.find()) year = Integer.parseInt(yearMatcher.group(1));
        else {
            yearMatcher = Pattern.compile("(?i)(?:novembro-dezembro\\s+de\\s+|de\\s+)(20\\d{2})").matcher(text);
            if (yearMatcher.find()) year = Integer.parseInt(yearMatcher.group(1));
        }
        String dateStart = null, dateEnd = null;
        String monthNum = MONTHS.get(month);
        String endMonthNum = MONTHS.get(endMonthName);
        if (monthNum == null) alerts.add(filename + ": mês inicial não reconhecido: " + month);
        if (endMonthNum == null) alerts.add(filename + ": mês final não reconhecido: " + endMonthName);
        if (year == 0) alerts.add(filename + ": ano não identificado; confirmar manualmente.");
        if (year > 0 && monthNum != null && endMonthNum != null) {
            try {
                int startMonth = Integer.parseInt(monthNum), finishMonth = Integer.parseInt(endMonthNum);
                LocalDate start = LocalDate.of(year, startMonth, startDay);
                int endYear = year;
                if (finishMonth < startMonth) endYear++;
                LocalDate finish = LocalDate.of(endYear, finishMonth, endDay);
                if (finish.isBefore(start)) alerts.add(filename + ": a data final da semana é anterior à data inicial.");
                else if (java.time.temporal.ChronoUnit.DAYS.between(start, finish) > 8)
                    alerts.add(filename + ": intervalo entre início e fim da semana parece incomum; conferir as datas.");
                dateStart = start.toString(); dateEnd = finish.toString();
            } catch (DateTimeException ex) {
                alerts.add(filename + ": data inválida no cabeçalho da semana; conferir dia e mês.");
            }
        }

        List<Map<String, Object>> parts = new ArrayList<>();
        Matcher pm = PART.matcher(text);
        Matcher sections = SECTION.matcher(text);
        List<int[]> sectionRanges = new ArrayList<>();
        List<String> sectionNames = new ArrayList<>();
        while (sections.find()) {
            sectionRanges.add(new int[]{sections.start(), sections.end()});
            sectionNames.add(sections.group(1).trim());
        }
        while (pm.find()) {
            int number = Integer.parseInt(pm.group(1));
            if (number < 1 || number > 8) continue;
            Map<String, Object> part = new LinkedHashMap<>();
            part.put("numero", number);
            part.put("tema", pm.group(2).trim());
            part.put("duracao_minutos", Integer.parseInt(pm.group(3)));

            String sectionTitle = null;
            for (int i = 0; i < sectionRanges.size(); i++) {
                if (sectionRanges.get(i)[1] <= pm.start()) sectionTitle = sectionNames.get(i);
                else break;
            }
            if (sectionTitle == null) {
                alerts.add(filename + ": não foi possível associar a parte " + number + " a uma seção.");
                part.put("secao", null);
                part.put("secao_titulo", null);
            } else {
                part.put("secao", sectionCode(sectionTitle));
                part.put("secao_titulo", sectionTitle);
            }

            String designacao = findNextLine(text, pm.end());
            if (number >= 4 && number <= 6 && designacao != null) {
                // Preserva a instrução da designação para conferência, mesmo quando não há subtítulo conhecido.
                part.put("descricao_designacao", primeiraFrase(designacao));
            }
            String subtitle = findSubtitle(text, pm.end());
            if (subtitle != null) part.put("subtitulo", subtitle);
            parts.add(part);
        }
        parts.sort(Comparator.comparingInt(p -> (Integer)p.get("numero")));
        Set<Integer> seen = new HashSet<>();
        for (Map<String, Object> p : parts) if (!seen.add((Integer)p.get("numero"))) alerts.add(filename + ": parte " + p.get("numero") + " apareceu mais de uma vez; precisa conferência.");
        for (int n=1;n<=8;n++) if (!seen.contains(n)) alerts.add(filename + ": parte " + n + " não identificada.");
        if (parts.size() != 8) alerts.add(filename + ": foram identificadas " + parts.size() + " partes; esperado: 8.");

        List<Integer> songs = new ArrayList<>();
        Matcher sm = SONG.matcher(text); while (sm.find()) songs.add(Integer.parseInt(sm.group(1)));
        Map<String, Object> songsMap = new LinkedHashMap<>();
        songsMap.put("abertura", songs.size() > 0 ? songs.get(0) : null);
        songsMap.put("intermediario", songs.size() > 1 ? songs.get(1) : null);
        songsMap.put("encerramento", songs.size() > 2 ? songs.get(2) : null);
        if (songs.size() != 3) alerts.add(filename + ": foram identificados " + songs.size() + " cânticos; esperado: 3. Conferir na apostila.");

        Map<String, Object> week = new LinkedHashMap<>();
        week.put("arquivo_origem", filename); week.put("data_inicio", dateStart); week.put("data_fim", dateEnd);
        week.put("leitura_biblica", reading); week.put("canticos", songsMap); week.put("partes", parts);
        week.put("status_conferencia", "pendente");
        return week;
    }

    private static String sectionCode(String title) {
        String normalized = normalize(title);
        if (normalized.startsWith("tesouros")) return "tesouros";
        if (normalized.startsWith("faca")) return "ministerio";
        if (normalized.startsWith("nossa")) return "vida_crista";
        return "nao_identificada";
    }

    /** Obtém a primeira linha de conteúdo após o cabeçalho, sem atravessar outra parte/seção. */
    private static String primeiraFrase(String texto) {
        if (texto == null) return null;
        String limpo = texto.trim();
        int ponto = limpo.indexOf('.');
        return ponto >= 0 ? limpo.substring(0, ponto + 1).trim() : limpo;
    }

    private static String findNextLine(String text, int afterPart) {
        int limit = Math.min(text.length(), afterPart + 500);
        String[] lines = text.substring(afterPart, limit).split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;
            if (PART.matcher(line).matches() || SECTION.matcher(line).matches()) return null;
            return line;
        }
        return null;
    }

    /** Procura um tipo de designação reconhecível na primeira linha após o cabeçalho. */
    private static String findSubtitle(String text, int afterPart) {
        String line = findNextLine(text, afterPart);
        if (line == null) return null;
        Matcher m = SUBTITLE_START.matcher(line);
        return m.find() ? m.group(1).replaceAll("\\.$", "").trim() : null;
    }
    private static String normalize(String s) { return Normalizer.normalize(s.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}+", ""); }
    private static String jsonEscape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> out.append("\\\\");
                case '\"' -> out.append("\\\"");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> { if (c < 0x20) out.append(String.format(Locale.ROOT, "\\u%04x", (int)c)); else out.append(c); }
            }
        }
        return out.toString();
    }
    private static String toJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return "\"" + jsonEscape(s) + "\"";
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof Map<?,?> map) { StringJoiner j = new StringJoiner(",\n", "{\n", "\n}"); map.forEach((k,v) -> j.add("  \""+jsonEscape(String.valueOf(k))+"\": "+indent(toJson(v),2))); return j.toString(); }
        if (value instanceof Iterable<?> it) { StringJoiner j = new StringJoiner(",\n", "[\n", "\n]"); for (Object v:it) j.add(indent(toJson(v),2)); return j.toString(); }
        return "null";
    }
    private static String indent(String s, int n) { String pad = " ".repeat(n); return s.replace("\n", "\n"+pad); }
}
