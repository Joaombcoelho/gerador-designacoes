# Auditoria — preservação do histórico de designações

**Base inspecionada:** `main` em `314de245c1f75c9db9511e621defc726559f38f4` (merge da PR #96).  
**Branch de trabalho:** `fix/preservacao-historico-escalas`.  
**Versão-alvo:** 1.2.5.  
**Escopo desta PR:** testes de regressão e documentação da política observável; sem alteração de produção porque a regra para regeneração retroativa não está definida com segurança.

## 1. Fluxos reais verificados

### Edição manual de designação persistida

`EdicaoEscalaController.salvarAlteracoes()` chama `EscalaDAO.atualizarDesignacao()`. O DAO atualiza responsável/ajudante e, para presidente, dirigente e oração inicial, sincroniza as colunas redundantes de `escala` na mesma transação. Em caso de falha, tenta rollback. A edição não reescreve `historico_designacoes`; a escala atual e o histórico são persistências separadas.

A alteração manual permanece na tabela `designacao` e pode ser lida novamente por `EscalaDAO.buscarPorId()`. Um teste novo verifica a reabertura da escala e que a participação histórica anterior não é alterada silenciosamente.

### Salvar ou regenerar uma escala pela persistência coordenada

`PersistenciaEscalasService.salvar(resultados, true)` grava/atualiza as escalas e substitui o histórico somente para as datas incluídas no lote, usando a mesma conexão e transação. A atualização de uma escala apaga e recria suas linhas de `designacao` para aquele `escala_id`; portanto, uma regeneração explicitamente confirmada substitui as designações anteriores por aquelas presentes no resultado confirmado. Não existe mesclagem automática entre atribuições geradas e atribuições editadas manualmente.

O histórico de outras datas não é removido. Se não houver participações para uma data no resultado, o serviço preserva o histórico anterior dessa data. A repetição de uma persistência com participações para a mesma data substitui as linhas dessa data, em vez de acrescentar duplicatas. Um teste novo cobre isolamento entre datas e repetição da mesma persistência.

### Falha durante persistência

O teste existente `falhaAoPersistirHistoricoReverteEscalaEPreservaHistoricoAnterior` injeta uma referência inválida, espera uma exceção e verifica que a escala nova não permanece e que a fotografia do histórico é idêntica à anterior. A transação coordenada cobre escala e histórico. Isso não inclui toda a preparação da programação semanal: a auditoria anterior registra que a programação pode ser persistida separadamente da transação de escala/histórico.

### Pessoas e relações históricas

A exclusão física de pessoa referenciada é limitada pelas chaves estrangeiras. A política de arquivamento lógico já implementada bloqueia pessoas com participações hoje ou futuras e mantém o cadastro e o histórico de participações passadas. Os testes existentes em `PessoaDAOTest` cobrem arquivamento, bloqueio de referências atuais/futuras e preservação do histórico passado. Esta PR não muda a política nem as relações do banco.

### Programação, horários e eventos especiais

`ProgramacaoSemanaService.prepararParaGeracao()` prepara uma cópia em memória para programação existente e acrescenta partes fixas ausentes sem gravar/sincronizar a programação durante a prévia. Para partes existentes, mantém os valores já carregados, incluindo duração semanal e tema. `persistirSeNova()` só cria uma programação nova após confirmação; para uma programação existente, usa o fluxo de sincronização já existente.

A duração semanal é armazenada em `ProgramacaoParte` e usada pelo serviço de cálculo de horários S-140. Os fluxos de eventos especiais e suas prioridades já possuem auditoria/testes próprios; esta PR não altera resolvedor, datas, regras de precedência ou configurações específicas. A cobertura desta PR não substitui um teste de interface no Windows para horários e reuniões especiais.

## 2. Política observável e lacunas

| Situação | Comportamento atual comprovado | Política/documentação desta PR |
|---|---|---|
| Edição manual de designação salva | Atualiza a designação, preserva seu ID e não reescreve o histórico separado | Manter esse contrato; testar leitura após reabertura e preservação do histórico anterior |
| Nova geração ou regeneração confirmada | Substitui as designações da escala alvo e o histórico da(s) data(s) processada(s), na transação coordenada | Tratar o clique de confirmação de regeneração como substituição explícita; não mesclar automaticamente edições manuais antigas |
| Datas não incluídas no lote | Não são alvo da substituição histórica por data | Preservar e testar histórico de outras datas |
| Falha SQL/runtime durante persistência coordenada | Rollback da transação de escalas/histórico | Manter e testar a atomicidade existente |
| Pessoa com histórico passado | Arquivamento lógico preserva a linha de pessoa e as participações históricas | Manter as referências; não excluir dados históricos por cascata |
| Pessoa com participação hoje/futura | Arquivamento é bloqueado pelo DAO | Manter regra existente |
| Horários/duração manual de uma programação existente | A preparação para geração lê a programação existente e não substitui seus valores existentes por padrões durante a prévia | Não alterar o fluxo nesta PR; validar também visualmente no Windows |
| Regenerar reunião passada ou corrigir retroativamente o histórico | A persistência coordenada substitui o histórico da data | **Política definida nesta etapa:** permitir correção retroativa somente após confirmação explícita, mostrando as datas e avisando que as designações atuais e o histórico dessas datas serão substituídos; outras datas permanecem intactas |

### Decisão funcional e proteção implementada

A política escolhida é permitir correções retroativas explícitas. Antes de salvar pela tela de geração ou pela tela de programação, o sistema identifica as datas anteriores ao dia atual e apresenta uma confirmação específica com a lista das datas afetadas.

A confirmação informa que:
- as designações geradas agora substituirão as designações e participações históricas dessas datas;
- edições manuais anteriores não serão mescladas automaticamente;
- o histórico de outras datas não será alterado.

Cancelar a confirmação interrompe o salvamento antes da persistência coordenada. Confirmar segue o fluxo transacional existente para persistir escala e histórico juntos. Datas de hoje ou futuras não acionam este alerta específico. A data, por si só, não prova se uma reunião ocorreu; a confirmação explícita é a salvaguarda escolhida para autorizar a correção.

## 3. Testes desta PR

Adicionados em `AuditoriaHistoricoDesignacoesTest`:

- `edicaoManualPersistidaAoReabrirPreservaHistoricoOriginal`: salva escala e histórico, edita manualmente o responsável, reabre a escala e verifica que a alteração manual foi persistida sem reescrever o histórico separado.
- `regenerarDataAlvoPreservaOutrasDatasEContinuaSemDuplicarHistorico`: persiste duas vezes a mesma data alvo e verifica que o histórico de outra data permanece, a data alvo tem somente uma participação e a pessoa gravada corresponde ao resultado confirmado.

Testes existentes relacionados que foram inspecionados:
- rollback de escala/histórico quando falha uma FK;
- substituição de histórico limitada à data;
- exclusão de escala sem exclusão do histórico;
- atualização manual e sincronização de papéis especiais;
- arquivamento de pessoa e preservação do histórico passado;
- preparação de programação semanal sem gravação antecipada, conforme auditoria da etapa anterior.

## 4. Validação e limitações

Os testes foram adicionados ao repositório remoto, mas **não foram executados neste ambiente**: não há checkout local/Maven/JDK disponível por meio das operações remotas usadas para editar o GitHub. Nenhum resultado de build ou de testes é declarado como aprovado nesta PR.

Executar no Windows, na branch desta PR:

```powershell
mvn --batch-mode --no-transfer-progress -Dtest=AuditoriaHistoricoDesignacoesTest,EscalaDAOTest,EdicaoEscalaControllerTest,PessoaDAOTest test
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
```

Registrar os totais reais (testes, falhas, erros e ignorados). Depois, validar manualmente no IntelliJ/Windows: editar uma designação, salvar, fechar/reabrir a tela; confirmar uma regeneração futura; conferir que uma data histórica diferente permaneceu; e confirmar que os ajustes semanais de duração/tema e as regras de eventos especiais continuam intactos.

**Estado:** auditoria parcial com testes de regressão e documentação adicionados; aguarda execução Maven local e decisão funcional sobre regeneração/correção retroativa. Não marcar a política de reuniões passadas como concluída até essa decisão.


## 5.1. Implementação da política retroativa

A confirmação é acionada pelos dois pontos de entrada da interface que salvam escalas: a tela de geração e a tela de programação. A lista é derivada das datas presentes nos resultados que serão salvos. O cancelamento não chama a persistência; a confirmação prossegue para o serviço transacional existente.

A confirmação visual e o cancelamento ainda exigem validação manual no Windows. Os testes de persistência existentes cobrem rollback, substituição por data e preservação de outras datas, mas não simulam a interação modal da confirmação.
