# Validação do instalador e da atualização segura — versão 1.2.5

**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Branch:** `test/instalador-atualizacao-segura`  
**Commit-base:** `314de245c1f75c9db9511e621defc726559f38f4`  
**Versão inspecionada:** `1.2.5`  
**Status geral:** **PENDENTE — a distribuição não está validada para lançamento**  
**Escopo desta alteração:** relatório e roteiro de validação; nenhum código de produção, versão, script de empacotamento ou banco foi alterado.

> Esta revisão foi feita por inspeção do repositório remoto. O agente não dispõe, nesta execução, de um ambiente Windows isolado com JDK 22, Maven e capacidade de executar o instalador `.exe`. Por isso, não foi possível gerar o instalador, instalar, atualizar ou desinstalar o aplicativo. Nenhum desses cenários é declarado aprovado.

## 1. Estado inspecionado

A branch `main` apontava para o commit-base `314de245c1f75c9db9511e621defc726559f38f4`, merge da PR #96 (`test(database): validar integridade referencial após migrações`). A branch desta tarefa foi criada diretamente desse commit. A PR não depende nem incorpora as mudanças das PRs #97 ou #98.

### Versão e processo oficial de empacotamento

Arquivos inspecionados:

- `pom.xml`
- `scripts/build-installer.ps1`
- `README.md`
- `src/main/java/br/com/geradordesignacoes/MainApp.java`
- `src/main/java/br/com/geradordesignacoes/database/ConnectionFactory.java`
- `src/main/java/br/com/geradordesignacoes/database/DatabaseInitializer.java`
- `src/main/java/br/com/geradordesignacoes/service/BackupService.java`
- `docs/checklist-liberacao-etapa-5.md`
- `docs/auditoria-geral-prontidao-lancamento.md`

Achados estáticos:

- `pom.xml` declara `1.2.5` e `maven.compiler.release=22`.
- O script usa explicitamente `1.2.5` para a pasta de preparação, o nome esperado do JAR e `jpackage --app-version`.
- JAR esperado: `target/gerador-designacoes-1.2.5.jar`.
- Instalador esperado: `Instalador/GeradorDesignacoes-1.2.5.exe`.
- O script exige Maven, Java, `jlink`, `jpackage` e `JAVA_HOME` apontando para um JDK com `jmods`.
- O fluxo chama `mvn clean package -DskipTests`, copia dependências, verifica a presença de bibliotecas, cria um runtime reduzido com `jlink` e empacota com `jpackage --type exe`.
- O runtime inclui os executáveis `java.exe` e `javaw.exe`; o script verifica sua presença antes de empacotar.
- O instalador solicita atalho e entrada no menu Iniciar, sob o grupo `Gerador de Designações`.
- O script falha se a pasta de preparação ou o instalador de destino já existir; isso evita sobrescrever artefatos existentes, mas exige limpeza manual deliberada antes de uma nova tentativa.
- O empacotamento pula os testes automatizados. A geração do instalador deve ocorrer somente depois da validação Maven separada.

**Nenhum arquivo instalador foi produzido nesta tarefa.** Não há nome de artefato realmente gerado, hash SHA-256 ou log de empacotamento a registrar.

## 2. Localização dos dados e inicialização

### Banco e backups

O código de `ConnectionFactory` mantém, no Windows, o caminho de dados compatível com as instalações existentes:

```text
%LOCALAPPDATA%\GeradorDesignacoes\gerador-designacoes.db
```

O serviço de backup usa a pasta:

```text
%LOCALAPPDATA%\GeradorDesignacoes\backups\
```

Os backups automáticos usam nomes com data e hora. O comportamento de retenção e restauração é coberto por código e testes próprios, mas esses testes não provam que o instalador real ou seu desinstalador preserva a pasta externa de dados.

### Inicialização sem pessoas nominais

A PR #92 removeu do fluxo normal de inicialização a inserção automática de pessoas nominais e adicionou testes de regressão. Isso é evidência de implementação e cobertura automatizada, não substitui a instalação limpa real com o executável empacotado.

Em uma instalação nova, o ensaio deve confirmar que o banco estrutural é inicializado sem pessoas nominais de exemplo. Em uma atualização, deve confirmar que as pessoas existentes continuam presentes e não são recriadas, duplicadas ou substituídas.

### Migrações e recuperação

A inicialização chama `DatabaseInitializer.initialize()`; o commit-base inclui a validação final de integridade referencial introduzida na PR #96. A inicialização falha quando encontra referências estrangeiras inválidas, mas o próprio histórico de migrações documenta que nem todas as migrações compõem uma transação global única.

Portanto, para uma atualização real, é necessário preservar uma cópia de segurança independente antes de iniciar o instalador. O teste de recuperação deve usar somente dados fictícios em uma VM descartável.

## 3. Instalação, atalhos e desinstalação

### O que foi possível confirmar por inspeção

- O script configura atalho e entrada no menu Iniciar.
- O script não define explicitamente um diretório de dados dentro da pasta do aplicativo; o banco e os backups são armazenados em `LOCALAPPDATA\GeradorDesignacoes`.
- O script não implementa uma rotina de desinstalação própria nem define flags explícitas para remover o diretório de dados.
- O código de produção consultado não contém um processo de desinstalação; a remoção do aplicativo depende do instalador Windows produzido por `jpackage`.

### O que permanece desconhecido

Não foi executado o instalador/desinstalador real. Assim, ainda não se confirmou:

- o diretório efetivo de instalação escolhido pelo pacote;
- se a atualização instala sobre a versão anterior ou apresenta comportamento diferente;
- quais arquivos o desinstalador gerado remove;
- se atalhos e entradas de menu são removidos como esperado;
- se banco, configurações e backups permanecem intactos depois da desinstalação;
- se reinstalar após desinstalar volta a reconhecer os dados existentes.

**Não alterar o comportamento de desinstalação por suposição.** Primeiro deve-se observar o comportamento real numa VM, com snapshot e dados descartáveis. Se os dados forem removidos ou se o resultado for ambíguo, abrir uma correção separada com política explícita de preservação e confirmação do usuário.

## 4. Evidências e status

| Cenário | Status | Evidência disponível / lacuna |
|---|---|---|
| Inspeção de versão e script | **Aprovado por inspeção estática** | `pom.xml` e script declaram `1.2.5`; nenhum número de versão foi alterado. |
| Testes Maven nesta tarefa | **Não executados** | Não há log de execução local ou artefato de CI associado a esta branch nesta revisão. |
| Geração do instalador | **Pendente** | Não há log de PowerShell/`jpackage`, EXE nem hash SHA-256. |
| Instalação limpa | **Pendente** | Requer Windows isolado; não foi executada. |
| Abertura, Java incluído, JavaFX, banco e atalhos | **Pendente** | Não houve execução do aplicativo instalado. |
| Banco novo sem pessoas nominais | **Cobertura automatizada existente; cenário real pendente** | PR #92 e seus testes são evidência de código, não da instalação empacotada. |
| Atualização sobre banco existente | **Pendente** | Não há comparação de dados fictícios antes/depois de uma atualização real. |
| Preservação de IDs, registros e relacionamentos | **Pendente para o instalador** | Há testes de banco/migração e integridade, mas não uma atualização real instalada. |
| Repetição da atualização sem duplicação | **Pendente** | Não foi executada a segunda atualização no ambiente Windows. |
| Preservação de configuração e backups | **Pendente** | Caminhos são separados da pasta de aplicação por inspeção; a preservação real ainda não foi observada. |
| Desinstalação e reinstalação | **Pendente** | O comportamento efetivo do pacote ainda não foi verificado. |
| Recuperação após falha de atualização | **Pendente** | Não foi feita simulação no instalador real; exige snapshot e backup fictício validado. |

Não foram inventadas capturas de tela, logs, contagens de testes ou hashes.

## 5. Roteiro reproduzível para validação Windows

Executar em VM Windows ou ambiente de teste descartável. Não usar a instalação real do usuário, seu banco pessoal ou sua pasta `LOCALAPPDATA` real. Criar snapshot antes de cada ensaio.

### A. Compilação e testes Maven

No checkout do SHA candidato, com JDK 22 e Maven instalados:

```powershell
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
```

Guardar saída completa e relatórios Surefire; registrar SHA, versões do Java/Maven e contagens de testes, falhas, erros e ignorados.

### B. Gerar o instalador

Depois dos testes:

```powershell
$env:JAVA_HOME
java -version
mvn -version
powershell -ExecutionPolicy Bypass -File .\scripts\build-installer.ps1
Get-Item .\Instalador\GeradorDesignacoes-1.2.5.exe
Get-FileHash .\Instalador\GeradorDesignacoes-1.2.5.exe -Algorithm SHA256
```

O script requer que `dist\GeradorDesignacoes-1.2.5` e o EXE de destino ainda não existam; verificar o estado antes de executá-lo. Não apagar diretórios sem confirmar que são artefatos descartáveis do build. Guardar log, metadados do arquivo e hash.

### C. Instalação limpa

1. Restaurar snapshot limpo da VM e usar uma conta Windows de teste.
2. Instalar o EXE produzido e registrar a versão apresentada pelo instalador.
3. Confirmar a criação de atalho/menu e iniciar pelo atalho e pelo menu.
4. Verificar que o runtime empacotado permite abrir o aplicativo sem depender de uma instalação Java externa.
5. Confirmar inicialização SQLite e integridade do banco recém-criado.
6. Confirmar que não há pessoas nominais de exemplo.
7. Fechar e reabrir o aplicativo; confirmar que continua iniciando sem erro.
8. Guardar logs e capturas sem nomes ou outros dados pessoais.

### D. Atualização com banco fictício

1. Instalar a versão de origem suportada na VM e abrir uma vez para criar sua estrutura.
2. Popular o banco com dados sintéticos: pessoas, partes, configurações, escalas, designações, histórico, programação e relacionamentos válidos.
3. Guardar uma cópia independente do banco e registrar IDs, contagens, configurações e `PRAGMA foreign_key_check`.
4. Encerrar completamente a aplicação e confirmar que não há processo usando o banco.
5. Executar o instalador 1.2.5 pelo fluxo suportado, sem apagar a pasta de dados.
6. Iniciar o aplicativo e conferir se migrações foram concluídas sem erro.
7. Comparar IDs, registros, configurações e relacionamentos antes/depois; executar `PRAGMA integrity_check` e `PRAGMA foreign_key_check`.
8. Fechar e reabrir a aplicação e repetir as verificações para detectar efeitos de inicialização.
9. Repetir o processo de atualização uma segunda vez e confirmar que não há duplicação.
10. Guardar o log e a cópia de recuperação fora da pasta de instalação.

### E. Falha, recuperação e desinstalação

- Simular falha somente em VM descartável e a partir de um snapshot recuperável.
- Verificar que o usuário tem uma cópia válida do banco original e que a aplicação não é apresentada como atualizada se a migração falhou.
- Registrar a mensagem de erro, os arquivos deixados e o procedimento necessário para recuperar.
- Antes de desinstalar, criar e validar um backup adicional.
- Executar a desinstalação pelo mecanismo Windows instalado e registrar quais arquivos/atalhos foram removidos.
- Verificar explicitamente se banco, configurações e backups ainda existem.
- Reinstalar e confirmar se os dados existentes são reconhecidos. Não inferir esse resultado apenas do caminho de armazenamento no código.

## 6. Histórico de validação anterior

As PRs #89, #90, #91 e #95 já documentaram etapas da auditoria de liberação e as limitações de evidência. A PR #92 removeu a criação automática de pessoas nominais e a PR #96 acrescentou uma validação final de integridade referencial na inicialização.

Esses registros foram usados para evitar repetir a análise de versão, banco e pessoas nominais sem necessidade. Não foi localizado nesta revisão um log comprovando instalação limpa, atualização real, desinstalação ou execução do instalador no commit-base atual.

A presença de testes de migração, backup e restauração é relevante para o risco de dados, mas não substitui o teste de atualização do pacote instalado.

## 7. Resultado e decisão

**Decisão: manter a versão 1.2.5 bloqueada para distribuição até que as validações pendentes sejam executadas.**

Esta alteração documenta o estado observado e fornece roteiro para recolher evidências reproduzíveis. Ela não declara o instalador gerado, a instalação limpa, a atualização, a preservação de dados ou a desinstalação como aprovados. Também não altera scripts, versão, comportamento de instalação ou desinstalação.

**Limitação principal:** ambiente Windows isolado indisponível nesta execução. A instalação e a atualização reais permanecem pendentes e devem ser validadas antes de declarar o lançamento pronto.
