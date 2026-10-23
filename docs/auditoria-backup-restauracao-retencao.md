# Auditoria — backups, restauração e retenção

**Versão-alvo:** 1.2.5  
**Base inspecionada:** `main` no commit `314de245c1f75c9db9511e621defc726559f38f4`.  
**Branch:** `test/backup-restauracao-retencao`.  
**Escopo:** testes de regressão e documentação; sem alteração de produção, esquema, dependências, versão ou instalador.

## 1. Implementação real encontrada

### Criação de backup

`BackupDatabase.criarBackup(Path)` obtém o caminho do banco pelo contexto explícito da `ConnectionFactory`, rejeita destino igual ao banco ativo, cria arquivo temporário no diretório de destino, gera uma fotografia SQLite consistente com `VACUUM INTO`, valida o arquivo e só então move o temporário para o destino final. O temporário é limpo em `finally` e as operações de arquivo usam um bloqueio compartilhado.

A validação comum em `DatabaseFileSupport.validateDatabase(Path)` rejeita arquivo ausente, não regular, vazio, estruturalmente inválido, com violações de chave estrangeira ou sem as tabelas centrais `pessoa`, `parte`, `escala` e `designacao`.

O fluxo automático em `BackupService.criarBackup()` cria o backup antes de tentar registrar metadados e executar a retenção. Falha da manutenção de retenção é reportada no log, sem transformar o backup já criado em falha. Falha na criação é propagada como exceção; não é reportada como sucesso.

### Retenção automática

A política já estava implementada na base pela PR #87. `BackupRetentionService` só classifica como automático um arquivo com nome esperado, marcador lateral regular e legível e metadados que correspondem ao arquivo, incluindo tipo, nome, instante de criação, tamanho e SHA-256.

- O limite é de 30 backups automáticos identificados com segurança.
- A ordenação usa o instante `createdAt` registrado no marcador.
- Backups manuais não entram na contagem.
- Arquivos legados sem marcador, nomes desconhecidos, arquivos inválidos sem classificação confiável e links simbólicos não são elegíveis à exclusão.
- Se uma classificação/metadado não puder ser validado, a rodada cancela antes da primeira exclusão.
- Empates no instante de corte são preservados, podendo resultar em mais de 30 automáticos identificados.
- Se uma exclusão falhar, a limpeza é interrompida.
- Reutilizar um destino de backup automático para gravação manual invalida primeiro o marcador, sob o bloqueio compartilhado.

O limite de 30 aplica-se aos automáticos com metadados válidos; a pasta pode conter mais de 30 arquivos no total porque manuais e arquivos legados/desconhecidos são preservados deliberadamente.

### Restauração

`RestaurarDatabase.restaurar(Path)` valida a origem antes de alterar o banco ativo, rejeita caminho inexistente/não regular, o próprio banco ativo e banco SQLite sem o esquema central esperado. Copia a origem para um temporário no diretório do banco, valida a cópia e, se houver banco ativo, cria e valida uma fotografia de recuperação antes da substituição. Remove arquivos laterais SQLite durante a substituição coordenada e valida o banco depois da movimentação. Se houver falha após iniciar a substituição, tenta restaurar a fotografia anterior; se a recuperação também falhar, preserva o arquivo de recuperação e informa o caminho no erro.

A inicialização do banco executa `PRAGMA foreign_key_check` no fluxo comum. Esta PR amplia a evidência específica da restauração e não altera a implementação de produção.

## 2. Cobertura existente e reforçada

### Criação e retenção

- `BackupRestoreTest.deveCriarBackupConsistenteMesmoComWALERestaurarOsDados`: backup abre no SQLite, contém os dados esperados e permite recuperar a fotografia anterior apesar de mudanças posteriores; inclui modo WAL e integridade estrutural.
- `BackupRetentionServiceTest.deveManterOsTrintaBackupsAutomaticosMaisRecentes`: cria 35 automáticos identificados e verifica remoção dos cinco mais antigos e preservação dos 30 mais recentes.
- `devePreservarBackupsManuaisEArquivosLegadosSemMetadados`: mantém mais de 30 arquivos manuais, legados e agora também arquivos desconhecidos/inválidos sem classificação comprovada.
- Outros testes de retenção cobrem marcador inválido, falha ao ler marcador, hash divergente, empates no corte, interrupção após falha de exclusão e invalidação do marcador ao reutilizar destino manual.
- `BackupServiceRetentionFailureTest` já verifica que falha ao registrar metadados não apaga backups existentes nem altera o banco principal; foi adicionado teste para falha de criação não ser reportada como sucesso.

### Restauração

- O teste de WAL verifica que a fotografia tem os dados corretos, é restaurada e continua íntegra.
- Arquivo corrompido e banco SQLite sem esquema da aplicação são rejeitados sem alterar os dados ativos.
- Foi adicionado caso de arquivo inexistente e arquivo vazio.
- O teste de falha controlada foi fortalecido: o mecanismo de movimentação executa a substituição real e só então lança a exceção, obrigando o fluxo de recuperação a restaurar o banco anterior.
- Foi adicionado teste de restauração de dados fictícios ligados entre si: pessoa, parte, escala, designação, histórico de participação, programação semanal, parte programada, configuração geral e configuração de salas. Após a restauração, o teste reinicializa o banco e verifica valores, relacionamentos, `PRAGMA integrity_check` e `PRAGMA foreign_key_check`.

Todos os cenários usam `ConnectionFactory.openTestContext` e diretórios temporários. Nenhum teste abre o contexto de produção nem usa o banco pessoal.

## 3. Comandos de validação

Com JDK 22 e Maven, executar no checkout local:

```powershell
mvn --batch-mode --no-transfer-progress -Dtest=BackupRestoreTest,BackupRetentionServiceTest,BackupServiceRetentionFailureTest test
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
```

**Resultado deste agente:** os comandos Maven não foram executados. A edição ocorreu via API do GitHub, sem checkout local executável com JDK 22/Maven. Não há contagem de testes executados nem aprovação local a declarar; aguardar CI e validação local.

## 4. Limitações e critérios de revisão

- A retenção pode manter mais de 30 arquivos no total por desenho conservador; o limite é somente para backups automáticos identificados com evidência válida.
- A política preserva todos os arquivos empatados na data de corte, então pode manter mais de 30 backups automáticos se os metadados não permitem distinguir qual é o 30.º.
- A recuperação automática depende de que a fotografia do banco anterior seja criada e validada com sucesso. Se o rollback também falhar, o sistema tenta preservar o arquivo de recuperação e relata o caminho; esse cenário extremo não é simulado nesta PR.
- Os testes validam a camada de serviço/SQLite em banco temporário, não o diálogo JavaFX de restauração no Windows nem a restauração de uma instalação real.
- A validação de compatibilidade é baseada nas tabelas centrais exigidas pela implementação, integridade estrutural e chaves estrangeiras; não equivale a comprovar toda combinação de dados de todas as versões históricas.
- Não foram alterados código de produção, caminho do banco, política já implementada, esquema, dependências, versão, workflow ou instalador.

## 5. Conclusão

A política obrigatória de retenção já estava implementada e tem testes específicos substanciais. Esta PR não a reimplementa: reforça evidências que faltavam na criação/restauração, inclui dados fictícios relacionados, rejeição de arquivos ausentes/vazios, falha pós-substituição e preservação de arquivos desconhecidos. A conclusão final depende da execução dos testes pela CI e no ambiente local com JDK 22.
