package br.com.geradordesignacoes;

import br.com.geradordesignacoes.dao.ParteDAO;
import br.com.geradordesignacoes.dao.PessoaDAO;
import br.com.geradordesignacoes.model.*;
import br.com.geradordesignacoes.service.GeradorEscala;
import br.com.geradordesignacoes.service.RegrasService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GeradorEscalaTodasPartesTest extends BaseDAOTest {
    private static final LocalDate DATA = LocalDate.of(2026, 10, 7);

    @Test
    void deveGerarEscalaComTodasAsPartesDisponiveis() {
        ParteDAO parteDAO = new ParteDAO();
        PessoaDAO pessoaDAO = new PessoaDAO();
        List<Parte> partes = parteDAO.listarTodos();
        List<Pessoa> pessoas = pessoaDAO.listarTodos();
        System.out.println();
        System.out.println("============================================================");
        System.out.println(" TESTE DE GERAÇÃO COM TODAS AS PARTES DISPONÍVEIS");
        System.out.println("============================================================");
        System.out.println("Data: " + DATA);
        System.out.println("Total de partes: " + partes.size());
        System.out.println("Total de pessoas: " + pessoas.size());
        System.out.println();
        System.out.println("PARTES ENVIADAS AO GERADOR:");
        for (Parte parte : partes) {
            System.out.println("- " + parte.getNome() + " | tipo=" + parte.getTipo() + " | variação=" + parte.getTipoVariacao() + " | ordem=" + parte.getOrdem());
        }
        // ============================================================ // CONFIGURAÇÃO TEMPORÁRIA DO TESTE // ============================================================ //
        // Algumas pessoas serão tratadas como leitores EXPERIENTES // somente durante este teste. //
        // Isso NÃO altera o banco de dados. //
        // ============================================================
        String[] nomesLeitoresExperientes = {"Heitor", "Luís Cláudio", "João Vaz"};
        List<Pessoa> pessoasParaGeracao = pessoas.stream().map(pessoa -> {
            for (String nome : nomesLeitoresExperientes) {
                if (pessoa.getNome().equals(nome)) {
                    Pessoa copia = new Pessoa(pessoa.getNome(), pessoa.getSexo(), pessoa.isAtivo(), pessoa.podeSerResponsavel(), pessoa.podeSerAjudante(), pessoa.podeFazerLeitura(), pessoa.podeFazerDiscurso(), pessoa.podeFazerOracao(), pessoa.podeSerPresidente(), pessoa.podeSerDirigente(), pessoa.getPrivilegio(), NivelLeitura.EXPERIENTE);
                    copia.setId(pessoa.getId());
                    return copia;
                }
            }
            return pessoa;
        }).toList();
        System.out.println();
        System.out.println("LEITORES CONFIGURADOS COMO EXPERIENTES:");
        for (Pessoa pessoa : pessoasParaGeracao) {
            if (pessoa.getNivelLeitura() == NivelLeitura.EXPERIENTE) {
                System.out.println("- " + pessoa.getNome() + " | privilégio=" + pessoa.getPrivilegio() + " | nível=" + pessoa.getNivelLeitura());
            }
        }
        System.out.println();
        System.out.println("INICIANDO GERAÇÃO...");
        GeradorEscala gerador = new GeradorEscala(new RegrasService());
        // IMPORTANTE: // Usa a lista modificada somente durante este teste.
        ResultadoGeracaoEscala resultado = gerador.gerar( DATA, partes, pessoasParaGeracao ); assertNotNull( resultado, "O resultado da geração não pode ser nulo." ); System.out.println(); System.out.println( "PARTES GERADAS:" ); List<Designacao> designacoes = resultado.escala() .getDesignacoes(); for (Designacao designacao : designacoes) { System.out.println( "- " + designacao.parte().getNome() + " -> " + ( designacao.responsavel() != null ? designacao.responsavel().getNome() : "" ) + ( designacao.ajudante() != null ? " + " + designacao.ajudante().getNome() : "" ) ); } System.out.println(); System.out.println( "ERROS ENCONTRADOS:" ); if (resultado.erros().isEmpty()) { System.out.println( "- Nenhum erro." ); } else { for (String erro : resultado.erros()) { System.out.println( "- " + erro ); } } System.out.println(); System.out.println( "DIAGNÓSTICO DO ESTUDO BÍBLICO:" ); Parte estudoBiblico = partes.stream() .filter(parte -> parte.getTipo() == TipoParte.DIRIGENTE_ESTUDO ) .findFirst() .orElse(null); if (estudoBiblico == null) { System.out.println( "❌ Estudo Bíblico NÃO encontrado na lista de partes." ); } else { System.out.println( "Parte encontrada: SIM" ); System.out.println( "Nome: " + estudoBiblico.getNome() ); System.out.println( "Tipo: " + estudoBiblico.getTipo() ); System.out.println( "Variação: " + estudoBiblico.getTipoVariacao() ); System.out.println( "Ordem: " + estudoBiblico.getOrdem() ); System.out.println( "Participações necessárias: " + estudoBiblico.getParticipacoesNecessarias() ); boolean gerouEstudoBiblico = designacoes.stream() .anyMatch(designacao -> designacao.parte() .getTipo() == TipoParte.DIRIGENTE_ESTUDO ); System.out.println(); System.out.println( "PESSOAS ELEGÍVEIS PARA DIRIGENTE:" ); for (Pessoa pessoa : pessoasParaGeracao) { boolean podeDirigir = estudoBiblico.pessoaPodeExercerParticipacao( pessoa, TipoParticipacao.DIRIGENTE ); if (podeDirigir) { System.out.println( "- " + pessoa.getNome() + " | privilégio=" + pessoa.getPrivilegio() + " | nívelLeitura=" + pessoa.getNivelLeitura() + " | ativo=" + pessoa.isAtivo() ); } } System.out.println(); System.out.println( "PESSOAS ELEGÍVEIS PARA LEITOR/AJUDANTE:" ); for (Pessoa pessoa : pessoasParaGeracao) { boolean podeLer = estudoBiblico.pessoaPodeExercerParticipacao( pessoa, TipoParticipacao.AJUDANTE ); System.out.println( "- " + pessoa.getNome() + " | privilégio=" + pessoa.getPrivilegio() + " | nívelLeitura=" + pessoa.getNivelLeitura() + " | ativo=" + pessoa.isAtivo() + " | podeSerAjudante=" + pessoa.podeExercer( TipoParticipacao.AJUDANTE ) + " | ELEGÍVEL=" + podeLer ); } System.out.println(); System.out.println( "Designação gerada: " + ( gerouEstudoBiblico ? "SIM" : "NÃO" ) ); boolean possuiErroEstudoBiblico = resultado.erros() .stream() .anyMatch(erro -> erro.contains( estudoBiblico.getNome() ) ); System.out.println( "Erro específico da parte: " + ( possuiErroEstudoBiblico ? "SIM" : "NÃO" ) ); } System.out.println(); System.out.println( "Total de designações: " + designacoes.size() ); System.out.println( "Total de participações: " + resultado.participacoes().size() ); System.out.println( "Total de erros: " + resultado.erros().size() ); System.out.println( "============================================================" ); System.out.println( " FIM DO TESTE" ); System.out.println( "============================================================" ); System.out.println(); assertFalse( partes.isEmpty(), "O banco deveria possuir partes cadastradas." ); assertFalse( pessoas.isEmpty(), "O banco deveria possuir pessoas cadastradas." ); } }