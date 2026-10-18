package br.com.geradordesignacoes.service.eventos;

/**
 * Resultado lógico da resolução. Nenhuma ação deste enum grava dados.
 */
public enum AcaoResolucaoEvento {
    MANTER_PROGRAMACAO,
    MOVER_REUNIAO_PARA_TERCA,
    CANCELAR_REUNIAO,
    SUBSTITUIR_POR_CELEBRACAO,
    EXIGE_DECISAO
}
