package br.com.geradordesignacoes.service;

import br.com.geradordesignacoes.dao.EscalaDAO;
import br.com.geradordesignacoes.dao.ProgramacaoSemanaDAO;
import br.com.geradordesignacoes.model.DadosExportacaoS140Semana;
import br.com.geradordesignacoes.model.Escala;
import br.com.geradordesignacoes.model.ProgramacaoSemana;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Carrega os dados já persistidos para a futura exportação S-140.
 *
 * Importante: este serviço não cria programação, não gera escala e não
 * persiste alterações. A geração do documento deve ser uma operação de leitura.
 */
public class CarregadorDadosExportacaoS140Service {

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final EscalaDAO escalaDAO;
    private final ProgramacaoSemanaDAO programacaoSemanaDAO;

    public CarregadorDadosExportacaoS140Service() {
        this(new EscalaDAO(), new ProgramacaoSemanaDAO());
    }

    public CarregadorDadosExportacaoS140Service(
            EscalaDAO escalaDAO,
            ProgramacaoSemanaDAO programacaoSemanaDAO
    ) {
        this.escalaDAO = Objects.requireNonNull(escalaDAO);
        this.programacaoSemanaDAO = Objects.requireNonNull(programacaoSemanaDAO);
    }

    public DadosExportacaoS140Semana carregar(LocalDate data) {
        if (data == null) {
            throw new IllegalArgumentException(
                    "Selecione uma semana para preparar a exportação S-140.");
        }

        Escala escala = escalaDAO.buscarPorData(data)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não existe uma escala salva para "
                                + data.format(FORMATO_DATA) + "."));

        ProgramacaoSemana programacao = programacaoSemanaDAO.buscarPorData(data);
        if (programacao == null) {
            throw new IllegalArgumentException(
                    "Não existe programação salva para "
                            + data.format(FORMATO_DATA) + ".");
        }

        return new DadosExportacaoS140Semana(data, programacao, escala);
    }
}
