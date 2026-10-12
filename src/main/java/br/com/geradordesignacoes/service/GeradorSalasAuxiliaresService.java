
package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gera designações exclusivamente para as salas auxiliares.
 *
 * Não altera nem regenera as designações da Sala Principal.
 */
public class GeradorSalasAuxiliaresService {

    private final RegrasSalasAuxiliaresService regrasService;

    public GeradorSalasAuxiliaresService() {
        this(new RegrasSalasAuxiliaresService());
    }

    public GeradorSalasAuxiliaresService(
            RegrasSalasAuxiliaresService regrasService
    ) {
        this.regrasService = Objects.requireNonNull(regrasService);
    }

    /**
     * Retorna as partes elegíveis para uma sala auxiliar.
     */
    public List<Parte> filtrarPartesElegiveis(
            List<Parte> partes
    ) {
        if (partes == null || partes.isEmpty()) {
            return List.of();
        }

        return partes.stream()
                .filter(regrasService::parteElegivel)
                .toList();
    }

    /**
     * Retorna as pessoas aptas a exercer uma participação
     * específica em determinada sala.
     */
    public List<Pessoa> filtrarCandidatos(
            List<Pessoa> pessoas,
            Parte parte,
            SalaAuxiliar sala,
            TipoParticipacao participacao
    ) {
        if (pessoas == null
                || parte == null
                || sala == null
                || participacao == null) {
            return List.of();
        }

        return pessoas.stream()
                .filter(Objects::nonNull)
                .filter(pessoa ->
                        regrasService.podeExercerParticipacaoNaSala(
                                pessoa,
                                parte,
                                sala,
                                participacao
                        )
                )
                .toList();
    }

    /**
     * Converte a sala auxiliar para o modelo de designação.
     */
    public SalaDesignacao converterSala(
            SalaAuxiliar sala
    ) {
        Objects.requireNonNull(sala, "A sala não pode ser nula.");

        return switch (sala) {
            case B -> SalaDesignacao.B;
            case C -> SalaDesignacao.C;
        };
    }


    public ResultadoGeracaoSalasAuxiliares gerar(
            LocalDate data,
            List<Parte> partes,
            List<Pessoa> pessoas,
            List<Designacao> designacoesPrincipais,
            boolean salaBHabilitada,
            boolean salaCHabilitada
    ) {
        Objects.requireNonNull(data, "A data não pode ser nula.");
        Objects.requireNonNull(partes, "As partes não podem ser nulas.");
        Objects.requireNonNull(pessoas, "As pessoas não podem ser nulas.");
        Objects.requireNonNull(
                designacoesPrincipais,
                "As designações principais não podem ser nulas."
        );

        List<Designacao> geradas = new ArrayList<>();
        List<String> erros = new ArrayList<>();

        if (!salaBHabilitada && !salaCHabilitada) {
            return new ResultadoGeracaoSalasAuxiliares(geradas, erros);
        }

        List<Parte> partesElegiveis = filtrarPartesElegiveis(partes);

        for (Parte parte : partesElegiveis) {
            if (salaBHabilitada) {
                gerarParaSala(
                        data, parte, pessoas, designacoesPrincipais,
                        geradas, SalaAuxiliar.B, erros
                );
            }

            if (salaCHabilitada) {
                gerarParaSala(
                        data, parte, pessoas, designacoesPrincipais,
                        geradas, SalaAuxiliar.C, erros
                );
            }
        }

        return new ResultadoGeracaoSalasAuxiliares(geradas, erros);
    }

    private void gerarParaSala(
            LocalDate data,
            Parte parte,
            List<Pessoa> pessoas,
            List<Designacao> designacoesPrincipais,
            List<Designacao> geradas,
            SalaAuxiliar sala,
            List<String> erros
    ) {
        List<Pessoa> bloqueadas = new ArrayList<>();

        adicionarParticipantesDaParte(
                bloqueadas, designacoesPrincipais, parte
        );
        adicionarParticipantesDaParte(
                bloqueadas, geradas, parte
        );

        SalaDesignacao salaDesignacao = converterSala(sala);

        if (parte.getTipo() == TipoParte.LEITURA) {
            Pessoa leitor = selecionarCandidato(
                    pessoas, bloqueadas, parte, sala,
                    TipoParticipacao.LEITOR
            );

            if (leitor == null) {
                erros.add(
                        "Não foi possível designar um leitor para a parte '"
                                + parte.getNome() + "' na Sala " + sala + "."
                );
                return;
            }

            geradas.add(
                    new Designacao(data, parte, leitor, null, salaDesignacao)
            );
            return;
        }

        if (parte.getTipo() == TipoParte.DISCURSO) {
            Pessoa orador = selecionarCandidato(
                    pessoas,
                    bloqueadas,
                    parte,
                    sala,
                    TipoParticipacao.ORADOR
            );

            if (orador == null) {
                erros.add(
                        "Não foi possível designar um orador para a parte '"
                                + parte.getNome()
                                + "' na Sala " + sala + "."
                );
                return;
            }

            geradas.add(
                    new Designacao(
                            data,
                            parte,
                            orador,
                            null,
                            salaDesignacao
                    )
            );
            return;
        }

        if (parte.getTipo() == TipoParte.DEMONSTRACAO) {
            Pessoa responsavel = selecionarCandidato(
                    pessoas, bloqueadas, parte, sala,
                    TipoParticipacao.RESPONSAVEL
            );

            if (responsavel == null) {
                erros.add(
                        "Não foi possível designar um responsável para a parte '"
                                + parte.getNome() + "' na Sala " + sala + "."
                );
                return;
            }

            bloqueadas.add(responsavel);

            Pessoa ajudante = selecionarCandidato(
                    pessoas, bloqueadas, parte, sala,
                    TipoParticipacao.AJUDANTE
            );

            if (ajudante == null) {
                erros.add(
                        "Não foi possível designar um ajudante para a parte '"
                                + parte.getNome() + "' na Sala " + sala + "."
                );
                return;
            }

            geradas.add(
                    new Designacao(
                            data, parte, responsavel, ajudante, salaDesignacao
                    )
            );
        }
    }

    private Pessoa selecionarCandidato(
            List<Pessoa> pessoas,
            List<Pessoa> bloqueadas,
            Parte parte,
            SalaAuxiliar sala,
            TipoParticipacao participacao
    ) {
        return filtrarCandidatos(pessoas, parte, sala, participacao)
                .stream()
                .filter(pessoa -> !bloqueadas.contains(pessoa))
                .findFirst()
                .orElse(null);
    }

    private void adicionarParticipantesDaParte(
            List<Pessoa> destino,
            List<Designacao> designacoes,
            Parte parte
    ) {
        for (Designacao designacao : designacoes) {
            if (!mesmaParte(designacao.parte(), parte)) {
                continue;
            }

            if (designacao.responsavel() != null
                    && !destino.contains(designacao.responsavel())) {
                destino.add(designacao.responsavel());
            }

            if (designacao.ajudante() != null
                    && !destino.contains(designacao.ajudante())) {
                destino.add(designacao.ajudante());
            }
        }
    }

    private boolean mesmaParte(Parte primeira, Parte segunda) {
        if (primeira == segunda || primeira.equals(segunda)) {
            return true;
        }

        if (primeira.getId() != null && segunda.getId() != null) {
            return primeira.getId().equals(segunda.getId());
        }

        return primeira.getTipo() == segunda.getTipo()
                && primeira.getNome().equals(segunda.getNome());
    }
}
