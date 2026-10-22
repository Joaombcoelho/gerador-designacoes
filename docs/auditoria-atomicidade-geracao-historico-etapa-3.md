# Etapa 3 — Auditoria de atomicidade da geração e consistência do histórico

**Base:** `main` após os merges das PRs #83, #84 e #85.  
**Branch:** `fix/atomicidade-geracao-historico-etapa-3`  
**Escopo:** geração individual/mensal, prévia, confirmação, edição na prévia, persistência de histórico e atualização em memória.

## 1. Resumo executivo

Foi comprovado um defeito no fluxo de geração individual da tela `EscalaController`: os métodos `gerarEscala()` e `gerarNovamente()` chamavam `GeradorEscala.gerarEscala(...)`. Esse caminho chegava a `gerarInterno(..., true)` e gravava o histórico numa transação própria antes de o usuário pressionar **Salvar**. Portanto, abandonar a prévia ou falhar ao salvar a escala podia deixar o histórico alterado sem a escala correspondente.

A correção muda a tela para gerar sem persistir, prepara a programação semanal sem gravá-la durante a prévia e só persiste uma programação nova depois da confirmação. A confirmação chama `PersistenciaEscalasService.salvar(..., true)`, que grava escalas e histórico na mesma conexão/transação SQLite. O histórico em memória é recarregado somente depois do retorno bem-sucedido dessa persistência.

Também foi identificado que a edição manual na prévia alterava `Escala.designacoes`, mas mantinha `ResultadoGeracaoEscala.participacoes` com a fotografia anterior. Foi adicionado `ParticipacoesEscalaMapper` para reconstruir as participações a partir das designações finais, preservando participações de salas auxiliares da mesma data e substituindo somente a Sala Principal.

**A etapa ainda não deve ser considerada concluída.** Os comandos Maven não foram executados neste ambiente. A edição de escalas já persistidas pela tela `EdicaoEscalaController` continua dependendo de uma decisão funcional: se a edição deve reescrever o histórico da data, inclusive para datas passadas, ou se o histórico representa o que foi originalmente registrado/realizado.

## 2. Estado do repositório e dependências

- PR #83 — tratamento de falhas na exclusão: mesclada.
- PR #84 — arquivamento de pessoas e partes: mesclada.
- PR #85 — auditoria anterior do histórico: mesclada.
- Base desta etapa: commit `ecda056bdd27cd19ac40fcbe48de06227b038f87`, merge da PR #85.
- Não houve alterações de esquema, migrações, versão do aplicativo, instalador ou política de backup.

## 3. Mapa dos fluxos

### 3.1 Geração individual e regeneração

Antes da correção:

`EscalaController.gerarEscala/gerarNovamente` → `GeradorEscala.gerarEscala` → `gerarInterno(..., true)` → `salvarNovasParticipacoes` → `HistoricoDesignacoesService.salvarGeracao` → commit próprio do histórico. Só depois, ao pressionar **Salvar**, `PersistenciaEscalasService` gravava a escala; o argumento `salvarHistorico` era falso nesse caminho.

Depois da correção:

`EscalaController` → `ProgramacaoSemanaService.prepararParaGeracao` → `GeradorEscala.gerarSemPersistir` → prévia em memória → eventual edição manual → **Salvar** → persistência tardia da programação preparada → `PersistenciaEscalasService.salvar(resultados, true)` → commit conjunto de escala e histórico → recarga do histórico em memória.

A regeneração individual segue o mesmo caminho. O mapper reconstrói as participações da Sala Principal a partir da escala atual e conserva as participações já existentes das salas B/C na mesma data. As participações de outras datas não entram no lote de substituição.

### 3.2 Geração mensal

`EscalaController.gerarEscalasDoMes` → `ProgramacaoSemanaService.listarSemanasDoMes` / preparação em memória → `GeradorEscala.gerarSemPersistir` por data, usando histórico temporário → prévia mensal → **Salvar** → `PersistenciaEscalasService.salvar(resultados, true)` → commit de todas as escalas e do histórico coordenado → recarga do histórico.

A geração mensal já utilizava geração sem persistência para a Sala Principal. A alteração torna explícita a persistência do histórico também no salvamento individual e deixa de usar o sinalizador mensal para decidir se o histórico será gravado.

### 3.3 Salas auxiliares

`CoordenadorGeracaoSalasService.gerar(...)` persiste o histórico diretamente, por meio de `HistoricoDesignacoesService.salvarGeracao`, numa transação separada da persistência da escala. A busca de referências na branch `main` encontrou usos em testes, mas não encontrou chamadores de produção para esse método. O método foi mantido para não quebrar seu contrato legado e os testes existentes; se for ligado a uma tela de prévia/confirmação, deverá usar a variante sem persistência e participar de uma persistência coordenada. Isso permanece como risco potencial, não como defeito atualmente observado num fluxo de produção.

### 3.4 Edição manual

- **Edição da prévia antes de salvar:** corrigida nesta etapa. A escala e as participações do resultado são atualizadas juntas em memória; a confirmação persiste as designações finais.
- **Edição de escala já persistida:** `EdicaoEscalaController` chama `EscalaDAO.atualizarDesignacao`, que confirma a alteração da tabela `designacao` numa transação própria e não atualiza `historico_designacoes`. A divergência técnica é observável; porém, reescrever histórico de datas passadas pode mudar o significado de um registro histórico. A política correta precisa ser confirmada antes de implementar uma alteração de comportamento.

### 3.5 Prévia, cancelamento e programação semanal

A geração individual usava `ProgramacaoSemanaService.obterOuCriar`, que podia criar uma programação ou sincronizar partes fixas no banco antes de salvar a escala. Agora usa `prepararParaGeracao`, que faz leitura e prepara partes faltantes/números padrão em memória. A programação nova é persistida apenas no caminho de confirmação por `persistirSeNova`; uma programação existente é sincronizada nesse momento.

Cancelar ou abandonar a prévia não persiste a escala, o histórico nem a programação nova preparada. A persistência da programação semanal é separada da transação coordenada de escala/histórico: se a programação for criada e a transação posterior da escala falhar, a programação pode permanecer. Ela é tratada como configuração semanal independente; validar no Windows se esse comportamento é aceitável. A gravação de múltiplas programações mensais também ocorre antes da transação de escalas, não dentro dela.

## 4. Tabela de transações e efeitos

| Fluxo | Conexão/transação | Commit e rollback | Efeito sobre histórico |
|---|---|---|---|
| Prévia individual | Geração em memória; consultas de programação/histórico | Nenhum commit de escala/histórico | Não altera o histórico persistido |
| Prévia mensal | Geração em memória e histórico temporário | Nenhum commit de escala/histórico | Não altera o histórico persistido |
| Confirmação individual | `PersistenciaEscalasService` usa uma conexão SQLite para escalas e histórico | Commit conjunto; rollback em SQL/runtime | Substitui somente datas presentes no lote, preservando outras datas e salas auxiliares da data |
| Confirmação mensal | Mesma transação coordenada para todas as escalas do lote | Commit conjunto; rollback em SQL/runtime | Escalas e participações submetidas são confirmadas juntas |
| Programação semanal nova | Transação própria de `ProgramacaoSemanaDAO.salvar` antes da persistência coordenada | Commit próprio; não faz parte da transação de escala/histórico | Não altera diretamente o histórico |
| `CoordenadorGeracaoSalasService.gerar` legado | Transação própria de `HistoricoDesignacoesDAO.substituirParticipacoesDaData` | Commit próprio; não coordena a escala | Pode atualizar histórico antes de eventual confirmação da escala se usado por um chamador futuro |
| Edição manual de escala persistida | Transação própria de `EscalaDAO.atualizarDesignacao` | Commit/rollback da designação | Não atualiza histórico; decisão funcional pendente |
| `HistoricoDesignacoesService.adicionarTodos/registrarGeracao` | Gravações por participação, cada uma com conexão/transação própria | Pode haver lote parcial se uma gravação posterior falhar | Não apaga outras datas; busca de referências não encontrou chamadores de produção para esses métodos legados |

## 5. Regras de consistência avaliadas

- **Regra A — prévia:** a tela de geração agora prepara programação e escala sem persistir alterações pendentes.
- **Regra B — confirmação:** escala e histórico são enviados à mesma transação por `PersistenciaEscalasService`.
- **Regra C — regeneração:** a substituição do histórico continua restrita à data; na geração individual, participações auxiliares já existentes dessa data são preservadas.
- **Regra D — lista vazia/incompleta:** `PersistenciaEscalasService` continua preservando o histórico anterior quando a lista filtrada de participações fica vazia. Não foi criada uma regra nova para distinguir geração incompleta de confirmação intencional sem participações.
- **Regra E — memória:** após commit bem-sucedido, o histórico em memória é recarregado. Em falha de persistência, o caminho não marca a escala como salva nem recarrega o histórico como se a gravação tivesse sido concluída.

## 6. Riscos comprovados, potenciais e decisões pendentes

### Corrigidos nesta branch
1. Histórico da geração individual era gravado antes da confirmação.
2. Prévia individual podia criar/sincronizar programação semanal no banco.
3. Edição manual da prévia podia deixar a lista de participações desatualizada em relação à escala confirmada.
4. A regeneração individual poderia substituir as salas auxiliares da mesma data; o mapper agora preserva as participações não pertencentes à Sala Principal.

### Riscos potenciais
1. O método legado `CoordenadorGeracaoSalasService.gerar` persiste histórico diretamente; nenhum chamador de produção foi encontrado na busca atual.
2. Programações semanais novas são confirmadas separadamente da transação de escala/histórico; podem permanecer se a persistência coordenada falhar depois.
3. Métodos legados de gravação de histórico por item não são atômicos para lotes, mas não foram encontrados chamadores de produção.
4. A regra de preservação do histórico quando a lista está vazia evita apagar dados, mas não distingue geração incompleta de uma confirmação intencional sem participações.

### Decisão necessária
Definir se editar uma escala já persistida deve reescrever `historico_designacoes`, e sob quais condições de data/status. Até essa definição, esta etapa não altera esse fluxo para não mudar silenciosamente o significado do histórico.

## 7. Correções e justificativas

- `EscalaController`: usa geração sem persistência; confirma escala e histórico juntos; recarrega o histórico só depois do sucesso.
- `ProgramacaoSemanaService.prepararParaGeracao`: prepara partes fixas e número padrão em memória sem sincronizar o banco durante a prévia.
- `ProgramacaoSemanaService.persistirSeNova`: grava programação nova após a confirmação e sincroniza programação existente apenas nesse momento.
- `ParticipacoesEscalaMapper`: converte as designações atuais em participações e preserva salas auxiliares da mesma data.
- Nenhuma alteração foi feita em regras de seleção de pessoas, esquema do banco, migrações, backups ou instalador.

## 8. Testes adicionados

- `ParticipacoesEscalaMapperTest.mapeiaResponsavelEAjudanteMantendoDataESala`
- `ParticipacoesEscalaMapperTest.mapeiaDirigenteDoEstudoBiblicoComTipoHistoricoCorreto`
- `ParticipacoesEscalaMapperTest.substituiSalaPrincipalEPreservaHistoricoDasSalasAuxiliares`
- `ProgramacaoSemanaServiceTest.persistirSeNovaGravaSomenteDepoisDaConfirmacao`

Os testes existentes da PR #85 cobrem rollback de escala/histórico, preservação de outras datas, exclusão de escala e chaves estrangeiras. A classe existente `ProgramacaoSemanaServiceTest.buscarOuPrepararNaoDevePersistirUmaNovaProgramacao` também cobre preparação sem persistência.

## 9. Validação executada

**Não executada neste ambiente:** `mvn test` e `mvn clean verify`. O acesso remoto usado para editar os arquivos não fornece um checkout local executável do projeto. Não há contagem de testes, falhas ou resultado de build a declarar nesta etapa.

Executar localmente na branch desta PR:

```powershell
mvn test
mvn clean verify
```

Revisão manual pendente no Windows:
1. Gerar uma escala e cancelar/fechar sem salvar; conferir que nem histórico nem programação nova foram gravados.
2. Gerar, editar uma designação na prévia e salvar; confirmar que o histórico contém o participante final.
3. Regenerar uma data que já tem histórico de Sala B/C; conferir que outras datas e salas auxiliares continuam intactas.
4. Simular falha de persistência e confirmar que escala/histórico permanecem no estado anterior.
5. Gerar e salvar um mês com programações novas e observar o comportamento se a persistência de escala falhar depois de criar as programações.

## 10. Branch e entrega

- Branch: `fix/atomicidade-geracao-historico-etapa-3`
- Base: `main` no commit `ecda056bdd27cd19ac40fcbe48de06227b038f87`
- PR: a abrir em rascunho para validação local.
- Merge: não realizado.
