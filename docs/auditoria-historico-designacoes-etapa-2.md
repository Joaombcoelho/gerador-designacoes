# Auditoria do histórico de designações — Etapa 2

**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Base inspecionada:** branch `main`, código consultado em 22/10/2026.  
**Branch desta auditoria:** `audit/historico-designacoes-etapa-2`  
**Escopo:** persistência, substituição, exclusão, limpeza, transações e integridade referencial do histórico.

## 1. Resumo executivo

A inspeção encontrou proteção importante já implementada: `PersistenciaEscalasService.salvar(...)` grava as escalas e, quando solicitado, substitui o histórico na mesma conexão/transação SQLite. Em falhas SQL ou de runtime, tenta rollback e propaga erro. A substituição por data usa `DELETE FROM historico_designacoes WHERE data = ?`, valida que cada participação pertence à data solicitada e insere as novas participações na mesma transação.

A exclusão de escala não apaga diretamente o histórico. O esquema relaciona `designacao.escala_id` a `escala.id` com `ON DELETE CASCADE`, mas `historico_designacoes` é independente da tabela `escala`; suas chaves estrangeiras para pessoa e parte não usam cascata. `ConnectionFactory.getConnection()` executa `PRAGMA foreign_keys = ON` em cada conexão.

Não foi encontrado chamador de produção para a limpeza global `HistoricoDesignacoesDAO.limpar()`; a busca encontrou seu uso no teste `HistoricoDesignacoesDAOTest.deveLimparHistorico`. O método permanece público e destrutivo, mas não foi removido porque a auditoria não deve quebrar uma possível utilização externa sem uma decisão de API. A limpeza não é chamada pelo fluxo comum de edição/regeneração nas referências encontradas.

**Não foi comprovada perda indevida de histórico de outras datas nos caminhos transacionais inspecionados.** Foram identificados pontos de risco potencial que merecem atenção: métodos legados que persistem listas participação por participação podem deixar gravações parciais se uma gravação posterior falhar; e os fluxos de geração individual que chamam `HistoricoDesignacoesService.salvarGeracao` persistem o histórico em transação própria, separada da persistência posterior da escala. A intenção do produto e o fluxo de confirmação precisam ser validados manualmente antes de alterar esse comportamento legado.

## 2. Mapeamento dos caminhos encontrados

### 2.1 Gravação e atualização

| Classe / método | Chamador ou fluxo encontrado | Tabelas e escopo | Transação / falha | Efeito sobre outras datas |
|---|---|---|---|---|
| `HistoricoDesignacoesDAO.salvar(ParticipacaoDesignacao)` | `HistoricoDesignacoesService.adicionar`; também testes | Insere uma linha em `historico_designacoes`; `INSERT OR IGNORE` | Uma instrução em conexão própria; exceções SQL viram `RuntimeException` | Não executa exclusão nem altera outras datas |
| `HistoricoDesignacoesDAO.salvarTodos(List)` | Busca encontrou teste; não encontrou chamador de produção | Itera sobre a lista e chama `salvar` por item | Não há transação única para a lista; se uma chamada falhar depois de outras concluírem, as anteriores podem permanecer | Não apaga datas; pode haver lote parcialmente gravado se reutilizado |
| `HistoricoDesignacoesService.adicionarTodos / registrarGeracao` | Apenas declarações encontradas na busca de referências | Chama `adicionar` por participação | Cada item é persistido separadamente; não é atomicidade de lote | Não apaga datas, mas pode persistir parte da lista antes de falhar |
| `HistoricoDesignacoesDAO.substituirParticipacoesDaData(data, lista)` | `HistoricoDesignacoesService.salvarGeracao`; testes | Apaga e reinsere apenas `historico_designacoes.data = data` | Abre transação, faz commit; em SQL/runtime tenta rollback | Escopo restrito à data; lista com participação de outra data é rejeitada |
| `HistoricoDesignacoesDAO.substituirParticipacoesDaData(connection, data, lista)` | `PersistenciaEscalasService.salvar` | Mesma substituição por data, na conexão recebida | Não confirma a transação; a camada coordenadora é responsável pelo commit/rollback | Não modifica outras datas |
| `PersistenciaEscalasService.salvar(resultados, salvarHistorico)` | Coordenador de persistência das escalas geradas/mensais | Insere/atualiza `escala`, substitui `designacao` da escala existente e, se solicitado, substitui o histórico por data | Uma conexão/transação cobre escalas e histórico; em SQL/runtime tenta rollback e lança erro | A substituição histórica usa cada data da entrada; quando a lista filtrada de participações está vazia, preserva o histórico anterior por decisão explícita no código |
| `EscalaDAO.salvar(Escala)` e `salvar(Connection, Escala)` | Serviços de persistência e fluxos de geração | Insere/atualiza `escala`; em atualização apaga as linhas de `designacao` da escala e reinsere as atuais | Sobrecarga sem conexão tem transação própria; sobrecarga com conexão delega commit/rollback ao chamador | A exclusão de designações é limitada a `escala_id`; não executa DELETE em histórico |
| `EscalaDAO.atualizar(Escala)` | Edição manual da escala | Atualiza estado/salvamento, sincroniza papéis especiais e substitui designações da escala | Transação local; rollback em falha; conexão é fechada ao sair | Não modifica diretamente `historico_designacoes`; histórico permanece separado das alterações de escala |
| `EscalaDAO.atualizarDesignacao(...)` | Edição manual de designação | Atualiza designação e sincroniza papéis especiais quando aplicável | Transação local com rollback | Não atualiza diretamente o histórico |
| `GeradorEscala.gerarInterno(...)` / `salvarHistoricoGeracao(...)` | Geração individual; há delegação para `HistoricoDesignacoesService.salvarGeracao` | Histórico de uma data | A gravação do histórico é uma transação própria, separada da persistência da escala | Substituição é limitada à data; validar manualmente se o momento de persistência coincide com a confirmação final da escala |
| `CoordenadorGeracaoSalasService.gerarInterno(...)` | Geração coordenada de salas | Histórico da data, incluindo salas auxiliares | Quando solicitado, usa `HistoricoDesignacoesService.salvarGeracao`, separado da persistência de escala; o fluxo mensal pode usar geração sem persistência e coordenar depois | A operação do histórico é restrita à data |

### 2.2 Exclusão, limpeza, cascatas e migrações

| Classe / método / esquema | Chamador ou fluxo encontrado | Escopo e efeito | Avaliação |
|---|---|---|---|
| `EscalaDAO.excluir(id)` | `HistoricoController.excluirEscala`; testes | `DELETE FROM escala WHERE id = ?`; a cascata remove as linhas de `designacao` ligadas à escala | Não há FK entre histórico e escala nem DELETE direto no histórico; o histórico deve permanecer |
| `HistoricoController.excluirEscala` | Ação de exclusão na tela de histórico | Chama `EscalaDAO.excluir` e atualiza a listagem após sucesso | A exceção precisa continuar visível ao fluxo de UI; PR #83 trata mensagens/estado em caso de falha |
| `HistoricoDesignacoesDAO.limpar()` | Busca de referências encontrou somente o teste de limpeza | `DELETE FROM historico_designacoes`, sem filtro de data | Operação global destrutiva; sem chamador de produção encontrado na busca. Não remover sem decisão de API, mas não deve ser usada em edição/regeneração comum |
| `DatabaseInitializer` — esquema | Inicialização/migrações do banco | Histórico referencia `pessoa(id)` e `parte(id)` sem `ON DELETE CASCADE`; `designacao.escala_id` referencia `escala(id)` com cascata | Excluir escala limpa designações vinculadas, não histórico. Excluir pessoa/parte referenciada no histórico deve falhar por FK, salvo alteração futura de esquema |
| `ConnectionFactory.getConnection()` | Todas as operações DAO via conexão padrão | Executa `PRAGMA foreign_keys = ON` em cada conexão | Integridade referencial habilitada no caminho de conexão auditado |
| Migrações de histórico | `DatabaseInitializer` e testes de migração | Criam/adaptam colunas/tabelas de histórico | Não foi identificada, nas referências consultadas, uma migração que limpe todo o histórico |

A busca de SQL encontrou exclusão por data na substituição do histórico, exclusão global apenas no método `limpar()`, exclusão de designações por `escala_id` durante atualização e exclusão de escala por ID. As referências de cascata no esquema consultado incluem também tabelas de programação/partes, mas não uma cascata de escala para histórico.

## 3. Riscos classificados

### Médio — fluxo individual com persistência separada

Os fluxos de geração individual chamam `HistoricoDesignacoesService.salvarGeracao`, que confirma a substituição histórica em uma transação separada da persistência da escala. Isso não comprova inconsistência: pode ser o contrato intencional do fluxo de confirmação. Entretanto, se escala e histórico forem considerados uma única operação de negócio, uma falha posterior ao commit do histórico pode deixar somente o histórico atualizado. A alteração estrutural não foi feita sem validar a semântica da interface e seus chamadores.

### Baixo — métodos de lote legados sem atomicidade

`HistoricoDesignacoesDAO.salvarTodos`, `HistoricoDesignacoesService.adicionarTodos` e `registrarGeracao` gravam item a item. Uma falha no meio pode deixar parte do lote persistida. As buscas por referência não encontraram chamadores de produção para esses métodos de lote, por isso não foi feita uma mudança de contrato sem uso demonstrado.

### Baixo — método de limpeza global público

`limpar()` pode remover todo o histórico se chamado. A busca de referências encontrou apenas o teste de unidade, não os fluxos comuns. Não há perda comprovada no produto por esse método atualmente; risco de manutenção caso seja usado indevidamente no futuro.

### Sem problema comprovado nos caminhos auditados

- A substituição por data filtra a exclusão por `data = ?` e rejeita participações fora da data.
- A persistência coordenada de escala e histórico tem rollback na mesma conexão.
- A exclusão de escala não exclui diretamente histórico.
- A conexão padrão ativa chaves estrangeiras.
- Não foi identificada migração que limpe indiscriminadamente o histórico.

## 4. Correções e testes desta etapa

Foi adicionado `AuditoriaHistoricoDesignacoesTest`, com cenários que verificam dados persistidos:

1. Substituição de uma data preserva registros de duas outras datas.
2. Falha de FK durante a persistência coordenada reverte a nova escala e restaura o histórico preexistente.
3. Exclusão de escala preserva a participação histórica da data.
4. A conexão de banco mantém `PRAGMA foreign_keys = 1`.

Não foram alterados esquema, migrações, política de backups, regra de exclusão de pessoas/partes nem o contrato do método de limpeza global.

## 5. Validação

A execução de Maven não está disponível por meio das operações remotas usadas para esta alteração. Portanto, **os testes adicionados ainda não foram executados nesta etapa**; nenhum resultado de build é declarado como aprovado.

Executar localmente na branch desta auditoria:

```powershell
mvn test
mvn clean verify
```

Registrar a contagem real de testes, falhas, erros e o resultado final de cada comando. Também validar manualmente no Windows a geração individual: gerar uma escala, confirmar/salvar e simular uma falha entre persistência do histórico e da escala, verificando qual operação a interface considera confirmada.

## 6. Dependências, limites e próximos passos

- A PR #84 (`fix/arquivamento-pessoas-partes`) permanece fora desta branch. A política de arquivamento lógico e os bloqueios de referências futuras devem ser auditados na etapa própria, sem mesclar seu código implicitamente.
- A PR #83 não apareceu na busca de PRs abertas; esta auditoria usa `main` como base. Confirmar o estado final da PR no GitHub se for necessário registrar a relação formal com a etapa anterior.
- Antes de corrigir os riscos potenciais do fluxo individual, rastrear a sequência completa da UI entre geração, prévia, confirmação e salvamento, e decidir se histórico e escala são uma única operação de negócio.
- Não executar `HistoricoDesignacoesDAO.limpar()` contra o banco real como parte da auditoria.

**Estado:** auditoria de código e testes adicionados; aguardando execução local de `mvn test` e `mvn clean verify`, além de revisão manual do fluxo de confirmação.
