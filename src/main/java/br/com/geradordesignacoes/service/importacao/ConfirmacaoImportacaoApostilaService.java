package br.com.geradordesignacoes.service.importacao;

import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.Parte;
import br.com.geradordesignacoes.model.ProgramacaoParte;
import br.com.geradordesignacoes.model.ProgramacaoSemana;
import br.com.geradordesignacoes.model.TipoVariacaoParte;
import br.com.geradordesignacoes.service.ProgramacaoSemanaService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Confirma a importação somente após uma prévia válida e persiste todas as
 * semanas novas em uma única transação. Não atualiza programações existentes.
 */
public class ConfirmacaoImportacaoApostilaService {

    private final ProgramacaoSemanaService programacaoService;
    private final ProgramacaoSemanaDAO programacaoDAO;

    public ConfirmacaoImportacaoApostilaService() {
        this(new ProgramacaoSemanaService(), new ProgramacaoSemanaDAO());
    }

    ConfirmacaoImportacaoApostilaService(
            ProgramacaoSemanaService programacaoService,
            ProgramacaoSemanaDAO programacaoDAO
    ) {
        this.programacaoService = Objects.requireNonNull(programacaoService);
        this.programacaoDAO = Objects.requireNonNull(programacaoDAO);
    }

    public List<ProgramacaoSemana> importar(
            PreviaImportacaoApostilaService.PreviaImportacao previa
    ) {
        Objects.requireNonNull(previa, "Gere a prévia antes de confirmar a importação.");
        if (!previa.podeProsseguirParaConfirmacao()) {
            throw new IllegalArgumentException(
                    "A prévia contém erros, mapeamentos pendentes ou datas conflitantes. "
                            + "Corrija as pendências antes de importar.");
        }

        List<ProgramacaoSemana> programacoes = new ArrayList<>();
        for (var semana : previa.semanas()) {
            List<Parte> partesVariaveis = semana.partes().stream()
                    .map(mapeamento -> mapeamento.candidatos().getFirst())
                    .filter(parte -> parte.getTipoVariacao() == TipoVariacaoParte.VARIAVEL)
                    .toList();

            ProgramacaoSemana programacao = programacaoService.montarProgramacao(
                    semana.dataReuniao(), partesVariaveis);
            Map<Integer, ProgramacaoParte> ocorrenciasPorParteId = new HashMap<>();
            for (ProgramacaoParte ocorrencia : programacao.partes()) {
                ocorrenciasPorParteId.put(ocorrencia.getParte().getId(), ocorrencia);
            }

            for (var mapeamento : semana.partes()) {
                Parte selecionada = mapeamento.candidatos().getFirst();
                ProgramacaoParte ocorrencia = ocorrenciasPorParteId.get(selecionada.getId());
                if (ocorrencia == null) {
                    throw new IllegalArgumentException(
                            "A parte '" + selecionada.getNome()
                                    + "' não faz parte da programação que seria criada para "
                                    + semana.dataReuniao() + ". Nenhuma programação foi gravada.");
                }
                ocorrencia.setTema(mapeamento.tema());
                ocorrencia.setDuracaoMinutos(mapeamento.duracaoMinutos());
            }

            var canticos = semana.canticos();
            programacoes.add(new ProgramacaoSemana(
                    null,
                    programacao.data(),
                    programacao.partes(),
                    programacao.salaBHabilitada(),
                    programacao.salaCHabilitada(),
                    null,
                    textoCantico(canticos.abertura()),
                    textoCantico(canticos.intermediario()),
                    textoCantico(canticos.encerramento())
            ));
        }

        return programacaoDAO.salvarTodasNovas(programacoes);
    }

    private static String textoCantico(Integer numero) {
        return numero == null ? null : numero.toString();
    }
}
