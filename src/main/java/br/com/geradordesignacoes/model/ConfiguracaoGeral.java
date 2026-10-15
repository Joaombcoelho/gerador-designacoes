package br.com.geradordesignacoes.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Preferências gerais usadas na preparação da programação semanal e do S-140.
 * As durações aqui são apenas dos itens complementares da reunião;
 * a duração das partes programáveis permanece no cadastro de partes.
 */
public record ConfiguracaoGeral(
        String nomeCongregacao,
        DayOfWeek diaReuniaoHabitual,
        LocalTime horarioInicio,
        int duracaoCanticoOracaoInicialMinutos,
        int duracaoComentariosIniciaisMinutos,
        int duracaoCanticoMeioMinutos,
        int duracaoComentariosFinaisMinutos,
        int duracaoCanticoOracaoFinalMinutos
) {
    public ConfiguracaoGeral {
        nomeCongregacao = Objects.requireNonNull(nomeCongregacao,
                "O nome da congregação não pode ser nulo.").trim();
        Objects.requireNonNull(diaReuniaoHabitual,
                "O dia habitual da reunião não pode ser nulo.");
        Objects.requireNonNull(horarioInicio,
                "O horário de início não pode ser nulo.");
        validarDuracao(duracaoCanticoOracaoInicialMinutos, "cântico e oração iniciais");
        validarDuracao(duracaoComentariosIniciaisMinutos, "comentários iniciais");
        validarDuracao(duracaoCanticoMeioMinutos, "cântico do meio");
        validarDuracao(duracaoComentariosFinaisMinutos, "comentários finais");
        validarDuracao(duracaoCanticoOracaoFinalMinutos, "cântico e oração finais");
    }

    public static ConfiguracaoGeral padrao() {
        return new ConfiguracaoGeral(
                "",
                DayOfWeek.WEDNESDAY,
                LocalTime.of(19, 30),
                5,
                1,
                4,
                3,
                5
        );
    }

    private static void validarDuracao(int minutos, String descricao) {
        if (minutos <= 0) {
            throw new IllegalArgumentException(
                    "A duração de " + descricao + " deve ser maior que zero."
            );
        }
    }
}
