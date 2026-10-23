# Etapa 5 — Validação integrada e checklist de liberação

**Status da etapa:** **Bloqueado por pendências**  
**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Branch-base inspecionada:** `main`  
**Commit-base avaliado:** `d5a051f7973e84e5f5ec12ed2af4f813c3d0af89`  
**Versão declarada em `pom.xml` e no script de empacotamento:** `1.2.5`  
**Versão pretendida para a próxima liberação:** não definida no repositório inspecionado; não alterar sem decisão do responsável.

> Este documento registra inspeção de repositório e o estado das validações. Não representa aprovação de liberação. Inspeção de código não substitui execução no Windows, testes automatizados, instalação ou teste real de atualização.

## 1. Estado real do repositório e dependências

Na inspeção, `main` apontava para `d5a051f7973e84e5f5ec12ed2af4f813c3d0af89`, merge da PR #86 (“Corrige atomicidade da geração e do histórico”). A versão declarada é `1.2.5`.

### PRs relevantes

| PR | Tema | Estado observado | Incluída no commit avaliado? |
|---|---|---|---|
| #83 | Tratamento de falhas na exclusão | Integrada na história de `main` | Sim |
| #84 | Arquivamento de pessoas e partes | Integrada na história de `main` | Sim |
| #85 | Auditoria da preservação do histórico | Integrada na história de `main` | Sim |
| #86 | Atomicidade da geração e do histórico | Merge que forma o commit avaliado | Sim |
| #87 | Retenção segura de backups automáticos | Aberta, em rascunho | **Não** |
| #88 | Testes de compatibilidade das migrações SQLite | Aberta, em rascunho | **Não** |

As PRs #87 e #88 não foram integradas automaticamente e suas alterações não estão presentes no commit-base avaliado. São dependências relevantes para concluir a avaliação das alterações de retenção e dos novos testes de migração. Antes de uma decisão de liberação, o responsável deve validar essas PRs e escolher explicitamente o conjunto de commits a avaliar. Este documento não as aprova nem substitui seus testes.

### Versão e artefatos

- Versão do projeto em `pom.xml`: `1.2.5`.
- Versão passada ao `jpackage`: `1.2.5`.
- JAR esperado pelo script: `target/gerador-designacoes-1.2.5.jar`.
- Pasta de preparação: `dist/GeradorDesignacoes-1.2.5/`.
- Diretório de destino do instalador: `Instalador/`.
- Instalador esperado: `Instalador/GeradorDesignacoes-1.2.5.exe`.
- Script identificado: `scripts/build-installer.ps1`.
- O script usa `mvn clean package -DskipTests`, copia dependências de runtime, verifica bibliotecas, gera runtime reduzido com `jlink` e cria instalador Windows com `jpackage`.
- O script exige `mvn`, `java`, `jlink`, `jpackage` e `JAVA_HOME` apontando para um JDK 22 com `jmods`. Ele se recusa a sobrescrever a pasta de preparação ou o instalador de destino já existentes.
- O repositório inspecionado não define uma versão futura pretendida nem documenta um procedimento automatizado específico de atualização entre instaladores. Não foi presumido que a próxima versão será `1.2.6`.

## 2. Persistência e inicialização — fatos inspecionados

A fábrica de conexões define o banco de produção em:

```text
%LOCALAPPDATA%\\GeradorDesignacoes\\gerador-designacoes.db
```

A pasta de backups automáticos é documentada como:

```text
%LOCALAPPDATA%\\GeradorDesignacoes\\backups\\
```

A inicialização de `MainApp.start` abre o contexto de produção, chama `DatabaseInitializer.initialize()`, inicia o serviço de backup automático e então constrói e exibe a interface. A falha durante a inicialização é relançada depois da tentativa de encerrar os recursos; não foi executada a aplicação nesta etapa para validar a mensagem ou o comportamento visual em caso de falha.

A inicialização SQLite utiliza inspeção do esquema para decidir migrações; não foi identificada uma tabela formal de versão do esquema no código inspecionado. O relatório da Etapa 4 está na PR #88, ainda não integrada no commit avaliado. A retenção segura está descrita na PR #87, também ainda não integrada.

A documentação do README descreve backup manual/restauração pela interface. A restauração substitui o banco local pelo arquivo selecionado e orienta reiniciar a aplicação. Esse fluxo deve ser testado somente com dados descartáveis em ambiente isolado.

## 3. Testes automatizados

### Execuções desta etapa

| Comando | Resultado real | Total / falhas / erros / ignorados |
|---|---|---|
| `mvn clean test` | **Não executado pelo agente** | Não disponível |
| `mvn clean verify` | **Não executado pelo agente** | Não disponível |

O ambiente de trabalho remoto usado para inspecionar e editar o repositório não disponibilizou um checkout executável com Maven e JDK 22. A tentativa anterior de clonar o repositório falhou por resolução DNS de `github.com`; o ambiente observado tinha OpenJDK 21 e não tinha Maven instalado. Portanto, não há evidência de compilação, execução da suíte, empacotamento ou CI para o commit desta etapa.

### Resultados históricos que não devem ser confundidos com esta execução

A descrição da PR #86 registra **482/482 testes aprovados conforme validação local reportada pelo autor**. Esse resultado é evidência histórica reportada para aquela etapa; não é uma execução feita pelo agente nesta etapa e não inclui os testes adicionados nas PRs #87 e #88, que permanecem abertas.

Na máquina Windows com JDK 22, executar na branch/commit escolhido para a avaliação:

```powershell
mvn clean test
mvn clean verify
```

Registrar dos relatórios Surefire o total de testes, falhas, erros e ignorados. Em seguida, confirmar que `mvn clean verify` terminou com `BUILD SUCCESS` e que os artefatos esperados existem. Se um comando falhar, anexar a saída relevante e não declarar a validação concluída.

## 4. Checklist manual reproduzível

**Legenda:** cada item começa como **Não executado**. Alterar o status somente depois de realizar o passo no ambiente adequado e registrar a evidência. Inspeção do código, por si só, não marca um fluxo visual ou de instalação como aprovado.

### 4.1 Inicialização e banco de dados

- [ ] **Não executado** — Abrir a aplicação com um banco válido existente e conferir a tela principal.
- [ ] **Não executado** — Em ambiente isolado, iniciar sem banco existente e confirmar criação e inicialização do banco.
- [ ] **Não executado** — Atualizar uma cópia de banco antigo e comparar os dados antes/depois.
- [ ] **Não executado** — Fechar e reabrir a aplicação duas vezes; conferir que a inicialização não duplica estruturas nem apaga dados.
- [ ] **Não executado** — Executar `PRAGMA foreign_key_check` na cópia de teste e confirmar ausência de violações; verificar `PRAGMA foreign_keys` em novas conexões.
- [ ] **Não executado** — Conferir que o banco usado pela aplicação está em `%LOCALAPPDATA%\\GeradorDesignacoes\\gerador-designacoes.db`.
- [ ] **Não executado** — Em ambiente descartável, provocar uma falha de inicialização controlada e confirmar que não é apresentada como sucesso nem deixa a aplicação aparentemente pronta.

### 4.2 Escalas e histórico

- [ ] **Não executado** — Gerar uma escala individual e abandonar/cancelar a prévia; comparar escala, programação e histórico persistidos antes/depois.
- [ ] **Não executado** — Gerar, revisar, confirmar e salvar uma escala; confirmar designações e histórico correspondentes no banco e na interface.
- [ ] **Não executado** — Cancelar uma operação sem confirmar e verificar que alterações não confirmadas não foram gravadas.
- [ ] **Não executado** — Regenerar uma data e verificar que o histórico de outras datas permanece idêntico.
- [ ] **Não executado** — Simular uma falha de persistência em banco descartável e verificar rollback e mensagens de erro.
- [ ] **Não executado** — Se salas B/C estiverem habilitadas, validar geração e regeneração sem perda das participações auxiliares existentes.
- [ ] **Não executado** — Arquivar uma pessoa/parte com histórico passado e confirmar que as referências históricas continuam disponíveis; conferir bloqueio para participações hoje/futuras conforme as regras atuais.
- [ ] **Não executado** — Conferir que a interface só exibe confirmação de sucesso depois de persistência concluída.

### 4.3 Backups

- [ ] **Não executado** — Criar backup manual e confirmar arquivo, tamanho e possibilidade de leitura em ambiente de teste.
- [ ] **Não executado** — Criar backup automático e confirmar arquivo gerado.
- [ ] **Não executado** — Com a implementação da PR #87 validada e integrada, criar mais de 30 backups automáticos elegíveis e confirmar retenção dos 30 mais recentes.
- [ ] **Não executado** — Confirmar que backup manual não é excluído pela retenção.
- [ ] **Não executado** — Confirmar que backup legado ou de classificação desconhecida é preservado.
- [ ] **Não executado** — Provocar falha de criação e confirmar que backups anteriores não são removidos indevidamente.
- [ ] **Não executado** — Provocar falha de exclusão em ambiente descartável e conferir registro/interrupção conforme a implementação.
- [ ] **Não executado** — Restaurar uma cópia de backup em ambiente isolado, reabrir a aplicação e comparar dados. Nunca usar o banco real para este ensaio.

**Dependência:** até que a PR #87 seja validada e integrada no conjunto de commits escolhido, o comportamento de retenção proposto por ela não faz parte da versão avaliada.

### 4.4 Interface e funcionalidades essenciais

- [ ] **Não executado** — Cadastrar e editar uma pessoa; fechar e reabrir a tela para conferir persistência.
- [ ] **Não executado** — Cadastrar e editar uma parte; conferir regras e persistência.
- [ ] **Não executado** — Gerar, revisar, editar e salvar uma escala.
- [ ] **Não executado** — Consultar o histórico antes e depois de salvar uma escala.
- [ ] **Não executado** — Visualizar e tratar um conflito de designação.
- [ ] **Não executado** — Importar uma apostila/JSON de teste, conferir associações e confirmar importação; verificar que cancelar a prévia não grava dados.
- [ ] **Não executado** — Alterar configurações, fechar a aplicação e conferir os valores depois de reiniciar.
- [ ] **Não executado** — Verificar mensagens de erro e confirmação nos fluxos acima, inclusive falhas de persistência.

## 5. Validação do instalador

**Status: Não executado.** Não há evidência de que o instalador tenha sido gerado, instalado ou iniciado nesta etapa.

No Windows de teste, depois de escolher explicitamente o commit a avaliar:

1. Conferir `java -version`, `mvn -version` e se `JAVA_HOME` aponta para JDK 22.
2. Confirmar que `dist/GeradorDesignacoes-1.2.5` e `Instalador/GeradorDesignacoes-1.2.5.exe` ainda não existem; o script se recusa a sobrescrever esses destinos. Preservar ou arquivar artefatos anteriores em vez de apagá-los sem autorização.
3. Executar `powershell -ExecutionPolicy Bypass -File .\\scripts\\build-installer.ps1` a partir da raiz do repositório.
4. Registrar a saída completa e confirmar a existência de `target/gerador-designacoes-1.2.5.jar` e `Instalador/GeradorDesignacoes-1.2.5.exe`.
5. Conferir propriedades/nome/versão do instalador e registrar hash SHA-256 do artefato testado.
6. Instalar em máquina virtual ou ambiente Windows descartável, iniciar pelo atalho instalado e testar banco, recursos JavaFX, backup e exportações suportadas.
7. Registrar resultado, logs e caminho de instalação.

O script usa `mvn clean package -DskipTests`; por isso, o sucesso do script **não substitui** `mvn clean test` e `mvn clean verify`. Não alterar a versão nem distribuir o instalador como parte desta etapa.

## 6. Atualização e preservação do banco

**Status: Não executado; atualização real pendente.**

Não foi confirmada nesta inspeção a disponibilidade de um instalador anterior apropriado para executar uma atualização real. Não foi simulada uma atualização nem declarada preservação comprovada.

Quando houver uma versão anterior e ambiente Windows isolado:

1. Instalar/executar a versão anterior em uma VM ou ambiente descartável.
2. Criar dados representativos: pessoas ativas/arquivadas, partes ativas/arquivadas, programação semanal, escala, histórico e configurações.
3. Fechar completamente o aplicativo e registrar a localização do banco e uma cópia de segurança.
4. Confirmar que a cópia pode ser aberta como SQLite e registrar SHA-256, contagens relevantes e resultados de `PRAGMA foreign_key_check`.
5. Executar o procedimento real de atualização para a nova versão, sem remover a pasta de dados do usuário.
6. Iniciar a nova versão e conferir pessoas, partes, programações, escalas, histórico e configurações.
7. Conferir migrações e integridade referencial no banco de teste.
8. Fechar e abrir novamente a aplicação; repetir as verificações e comparar com a evidência inicial.
9. Guardar logs, hashes, contagens e capturas de tela como evidência.

Se não houver versão anterior/instalador ou ambiente isolado, manter este item como **Não executado** e registrar a limitação. Não executar o ensaio destrutivo no banco real.

## 7. Recuperação e restauração

O README descreve backup manual e restauração de arquivo `.db` pela interface, com orientação para reiniciar a aplicação após a restauração. Procedimento prudente para um incidente:

1. Fechar completamente o aplicativo.
2. Fazer uma cópia separada do banco atual antes de qualquer tentativa de recuperação.
3. Selecionar um backup conhecido e restaurá-lo pela funcionalidade suportada, preferencialmente primeiro em ambiente isolado.
4. Reiniciar a aplicação e conferir registros, configurações e histórico.
5. Manter intactos o banco original e a cópia anterior até verificar que a restauração foi bem-sucedida.

A restauração real não foi executada nesta etapa. A política proposta de retenção da PR #87 também ainda não está integrada ao commit avaliado.

## 8. Defeitos, correções e limitações

- Nenhum defeito novo foi reproduzido nesta etapa, pois não foi possível executar a suíte, interface, instalador ou cenário de atualização.
- Nenhuma correção de produção foi feita; esta alteração é documental.
- Dependências abertas: validação local das PRs #87 e #88 e decisão sobre o conjunto de commits que representará a próxima avaliação.
- A versão pretendida para a próxima liberação não está definida no repositório inspecionado.
- A validação visual da interface, a geração e execução do instalador, a atualização real e a restauração de backup exigem ambiente Windows e permanecem pendentes.
- A contagem de 482 testes aprovada na PR #86 é um resultado local reportado naquela PR, não um resultado desta etapa.
- Não há evidência nesta etapa de execução de `mvn clean test`, `mvn clean verify`, script de empacotamento, instalador, atualização ou reinicialização.

## 9. Decisão de liberação

**Decisão atual: Bloqueado por pendências.**

Condições mínimas para reavaliar:

- [ ] Escolher e registrar o commit exato que contém as alterações aprovadas das etapas anteriores.
- [ ] Validar as PRs dependentes #87 e #88 antes de incluí-las no conjunto de avaliação.
- [ ] Executar e registrar `mvn clean test` e `mvn clean verify` com JDK 22.
- [ ] Preencher os checklists manuais com evidências, sem marcar itens não executados como aprovados.
- [ ] Gerar e executar o instalador em ambiente Windows isolado.
- [ ] Realizar teste real de atualização e reinicialização, ou manter a limitação explicitamente aceita pelo responsável sem alegar validação.
- [ ] Revisar os resultados e decidir formalmente a prontidão para liberação.

Nenhum merge, alteração de versão, publicação ou distribuição foi realizado por esta etapa.
