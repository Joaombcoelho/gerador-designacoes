# Etapa 4 — Auditoria e testes de migração SQLite

## Estado da inspeção

- Branch base: `main`, commit `d5a051f7973e84e5f5ec12ed2af4f813c3d0af89`.
- A PR #87 (retenção de backups) estava aberta em rascunho e não integrada no momento da inspeção. Esta branch foi criada diretamente a partir de `main`, sem incorporar a PR #87.
- A branch antiga `fix/migracoes-sqlite-etapa-3` corresponde à PR #49, já integrada; está muito atrás da `main` e não foi incorporada novamente.
- O esquema não possui uma tabela/versão explícita de migração. `DatabaseInitializer.initialize()` aplica verificações por introspecção SQLite (`PRAGMA table_info`, `sqlite_master`, índices) e executa correções de forma condicional.
- Por não existir um catálogo formal de versões suportadas, não é seguro declarar que cada versão histórica do aplicativo está identificada por número. Os testes abaixo registram explicitamente o esquema legado reproduzido e os caminhos de migração cobertos.

## Migrações inspecionadas

Em `DatabaseInitializer`:

- Adição condicional de colunas em `pessoa`: permissões por sala e `arquivado`.
- Adição condicional de colunas em `parte`: `ordem`, `duracao_padrao_minutos` e `arquivado`; correção de `tipo_variacao` vazio/nulo.
- Criação de `parte_participacao_necessaria` e remoção da participação de ajudante para dirigente do estudo bíblico.
- Migração de `historico_designacoes` para adicionar `sala` e alterar a chave única para incluir sala.
- Adição de papéis especiais em `escala`.
- Reconstrução transacional de `designacao` para permitir `responsavel_id` nulo e garantir a coluna `sala`.
- Adição de colunas de salas e dados S-140 em `programacao_semana`.
- Adição de `numero_oficial` e `duracao_minutos` em `programacao_parte`.
- Reconstrução transacional de `programacao_parte` para remover a unicidade antiga por semana/parte, mantendo a unicidade da ordem dentro da semana.
- Consolidação de nomes de partes legados, com remapeamento das referências e validação por `PRAGMA foreign_key_check`.
- Inserção condicional de registros iniciais e preenchimento de ordens padrão.

## Limites transacionais observados

A inicialização inteira não é uma única transação. Algumas alterações aditivas são executadas diretamente, enquanto reconstruções que exigem alteração de restrições usam transações locais com `commit`/`rollback`. Os testes não presumem atomicidade global: verificam o rollback da reconstrução de `programacao_parte` quando a cópia viola a restrição de ordem, preservação da tabela e dos registros de origem, e nova inicialização após corrigir o dado incompatível.

As migrações de `designacao` e `historico_designacoes` desativam temporariamente `PRAGMA foreign_keys` porque o SQLite não permite alterar esse pragma no meio de uma transação. Elas restauram a configuração depois da reconstrução. A validação final é feita por `PRAGMA foreign_key_check` e por uma tentativa de inserir uma referência inválida.

## Cobertura acrescentada

`DatabaseMigrationCompatibilityTest` usa arquivos SQLite temporários criados com JDBC diretamente, antes de abrir um contexto de teste. O teste reproduz um esquema legado sem as colunas adicionadas pelas migrações identificadas e insere dados ligados de pessoa, parte, escala, designação, histórico e programação semanal.

- Atualização do esquema legado por meio de `DatabaseInitializer.initialize()`.
- Preservação de IDs, nomes, tema e relações existentes.
- Verificação de colunas novas e de seus valores padrão/nulos, sem inventar dados históricos.
- Reexecução da inicialização duas vezes, conferindo contagens, configuração personalizada e atributos já existentes.
- Confirmação de `PRAGMA foreign_keys = 1` em conexões distintas e de ausência de linhas em `PRAGMA foreign_key_check`.
- Confirmação de que a restrição rejeita uma referência estrangeira inválida.
- Falha simulada na reconstrução de `programacao_parte`, verificação de rollback local sem tabela temporária residual e recuperação na próxima inicialização depois de corrigir o dado incompatível.

Os testes existentes em `DatabaseInitializerTest` continuam cobrindo migrações isoladas de `designacao`, `historico_designacoes`, papéis especiais e consolidação de partes duplicadas.

## Validação

Os testes foram adicionados ao repositório remoto, mas não foram executados neste ambiente. A validação local deve executar:

```powershell
mvn test
mvn clean verify
```

Registrar os resultados reais dos dois comandos antes de tirar a PR do rascunho. Não foram alteradas regras de negócio, esquema de produção, política de backups, instalador ou número de versão.


## Revisão complementar — validação final da inicialização

Na revisão baseada em `main` no commit `516c500ea3e6997a23365ddd64502433b1d9eec8`, foram confirmadas as coberturas descritas acima. A lacuna pontual identificada foi que `initialize()` imprimia a mensagem de sucesso sem executar uma verificação referencial final comum a todas as migrações.

A correção proposta adiciona uma validação final na mesma conexão usada pela inicialização: confirma que `PRAGMA foreign_keys` está ativo e executa `PRAGMA foreign_key_check`. Se encontrar uma violação, a inicialização lança `SQLException`, encapsulada pelo erro de inicialização já existente, com tabela, rowid e tabela referenciada para diagnóstico. Um teste em banco temporário injeta deliberadamente uma referência órfã e confirma que a validação a rejeita.

### Garantias e limites

- O esquema legado usado por `DatabaseMigrationCompatibilityTest` é um fixture representativo explícito, não uma matriz numerada de todas as versões históricas do aplicativo. O repositório não possui catálogo formal que permita afirmar compatibilidade universal com versões antigas.
- As reconstruções transacionais já existentes mantêm seus próprios limites e testes de rollback. Esta alteração não transforma toda a sequência de inicialização em uma transação global.
- Se uma migração anterior for concluída e uma migração posterior falhar, alterações anteriores fora da unidade transacional que falhou podem permanecer. A aplicação deve tratar a inicialização como falha e não iniciar o fluxo normal; recomenda-se manter backup antes de atualizar um banco real.
- Não foi adicionada tabela de versão do esquema: a inspeção não demonstrou que ela seja necessária para corrigir a lacuna específica coberta por esta alteração.
- Esta documentação descreve o código e os testes presentes na PR. Os comandos Maven só devem ser marcados como executados após registro de seus resultados reais.
