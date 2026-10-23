# Etapa 5 — Validação integrada e checklist de liberação

**Status da etapa:** **Bloqueado por pendências; não autorizado para distribuição**  
**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Branch-base desta revisão documental:** `main`  
**Commit-base desta revisão:** [`17a28853c899c2e6f33afb6d0c7a75df143ef2b8`](https://github.com/Joaombcoelho/gerador-designacoes/commit/17a28853c899c2e6f33afb6d0c7a75df143ef2b8)  
**Data/hora do commit-base:** 2026-10-23 19:52:16 UTC  
**Versão declarada em `pom.xml`:** `1.2.5`  
**Data desta revisão documental:** 2026-10-23

> Este documento separa inspeção do repositório, resultados observados remotamente e relatos do usuário. Não representa aprovação de liberação. O agente não tem acesso ao working tree local do usuário; portanto, o estado de alterações locais pendentes não foi verificado.

> **Importante:** o commit-base anterior do checklist (`cbc5858...`) não é mais o commit candidato atual. A auditoria geral foi registrada em `2ce671234f37fe2b05fff44bf68df8421333d1ae`; ela deve ser lida como retrato histórico daquele commit. A auditoria geral ainda precisa de atualização própria para refletir as mudanças posteriores descritas abaixo. Esta revisão altera somente este checklist.

## 1. Critérios de status e limites de evidência

Use estes status de forma consistente:

- **Aprovado:** há evidência identificável para o item. Quando a evidência for um relato do usuário, isso deve estar explícito; não significa execução independente pelo agente.
- **Reprovado:** foi observado um defeito ou requisito não atendido no escopo e commit indicados.
- **Pendente:** a ação ainda precisa ser executada ou uma decisão ainda precisa ser tomada.
- **Não verificado:** não há evidência suficiente para confirmar o resultado.

Um item não pode ser marcado como executado apenas porque existe código, um teste automatizado ou um roteiro para executá-lo. A compilação, a suíte de testes, a criação do instalador, a instalação do instalador e a atualização real do banco são validações diferentes.

## 2. Estado atual do repositório

### 2.1 Base, versão e mudanças desde a auditoria

A branch remota `main` aponta para `17a28853c899c2e6f33afb6d0c7a75df143ef2b8`, merge da PR #92, “Remove cadastros nominais automáticos da inicialização”. A versão declarada no `pom.xml` continua em `1.2.5`; esta revisão não altera a versão nem o instalador.

Mudanças posteriores à auditoria geral que afetam seus achados:

| Tema | Estado atual verificável | Limite da conclusão |
|---|---|---|
| Pessoas nominais inseridas automaticamente | A correção da PR #92 está integrada em `main`; o inicializador deixou de inserir os registros nominais e há teste de regressão no repositório. | A suíte relatada como aprovada não substitui o ensaio manual de inicialização limpa e preservação de um banco existente. |
| PDF S-140 preenchido | `modelos/S-140_T_Preenchido.pdf` não está na árvore atual de `main`; sua remoção ocorreu depois do commit da auditoria. | **O histórico Git não foi verificado quanto à remoção de versões antigas do arquivo.** Não se afirma que o histórico foi limpo. |
| Capturas de tela | O usuário confirmou que as capturas não expõem dados pessoais. | Registro baseado no relato do usuário; não houve nova inspeção visual independente nesta revisão. |
| CI | `.github/workflows/maven-tests.yml` está presente em `main` e executa testes Maven com Java 22 e Xvfb. | A presença do workflow não comprova, sozinha, uma execução bem-sucedida no commit-base atual nem substitui `mvn clean verify`. |

### 2.2 Artefatos e ferramentas de liberação

Inspeção de `pom.xml` e `scripts/build-installer.ps1`:

- Versão do projeto: `1.2.5`.
- Compilação configurada para Java 22.
- JAR esperado: `target/gerador-designacoes-1.2.5.jar`.
- Instalador Windows esperado: `Instalador/GeradorDesignacoes-1.2.5.exe`.
- O script usa `mvn clean package -DskipTests`, prepara dependências, cria runtime reduzido com `jlink` e gera o instalador com `jpackage`.
- Como o script pula os testes, sua execução não substitui `mvn clean test` ou `mvn clean verify`.

Não foi apresentada nesta revisão evidência identificável de um EXE produzido e instalado no commit candidato, nem log de empacotamento, hash SHA-256 ou roteiro de instalação concluído. A validação do instalador permanece **pendente**.

## 3. Evidências de testes automatizados

### 3.1 Registros disponíveis

| Validação | Status | Data, commit e ambiente | Resultado e limites |
|---|---|---|---|
| Suíte executada na IDE | **Aprovado conforme relato do usuário** | Relato recebido em 2026-10-23. Ambiente informado: IDE do usuário. O sistema operacional, JDK exato, comando e SHA do commit testado não foram registrados junto ao resultado. | O usuário informou **502/502 testes aprovados**. Falhas e erros: 0 conforme o relato. Quantidade de testes ignorados não informada. O log detalhado não foi anexado. Não é uma execução independente do agente. |
| GitHub Actions da PR #92 | **Aprovado na execução registrada** | [Execução 40010457742](https://github.com/Joaombcoelho/gerador-designacoes/actions/runs/40010457742), head `bfcbd3b6615605f50cb2abb6c74ad3f399b32d17`, antes do merge commit atual. | Workflow `Testes Maven`, job `Testes automatizados (Java 22)`, conclusão `success`. O total exato de testes dessa execução não foi registrado nesta revisão. |
| GitHub Actions da PR #94 | **Evidência histórica, não validação do commit candidato** | PR #94, alteração de compatibilidade do caminho do banco para CI; commit da PR anterior ao merge em `main`. | Foi relatado anteriormente que a CI passou com **499/499** testes. Esse resultado pertence à validação da PR #94 e não deve ser apresentado como total testado no commit-base atual. |
| `mvn clean verify` no commit candidato | **Não verificado** | Nenhum log/artefato correspondente disponível. | Não há evidência específica de execução e `BUILD SUCCESS` para esse comando no commit-base atual. |
| Build do instalador com PowerShell/`jpackage` | **Pendente** | Nenhum log de build, EXE e hash SHA-256 identificados nesta revisão. | A inspeção do script não prova que o instalador foi criado. |
| Instalação e execução do EXE em Windows isolado | **Pendente** | Nenhuma evidência identificada. | Não confundir compilação ou geração do EXE com instalação e execução do aplicativo instalado. |

### 3.2 Como registrar novas execuções

Para cada nova validação, guardar, quando disponível:

1. Data e hora.
2. Versão do aplicativo e SHA completo do commit testado.
3. Sistema operacional, versão do JDK e versão do Maven.
4. Comando ou roteiro realizado.
5. Resultado observado e saída de console/log.
6. Para testes: total, falhas, erros e ignorados.
7. Para instalador: caminho do artefato, versão exibida e SHA-256.
8. Para atualização de banco: origem da versão, cópia de segurança, contagens relevantes, resultado de `PRAGMA foreign_key_check` e resultado após reinicialização.

Quando algum dado não for coletado, registrar “não registrado”; não reconstruir o valor por inferência.

## 4. Checklist de validação

### 4.1 Inicialização limpa e preservação de banco existente

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Aprovado por relato/CI, com limite** | Regressão automatizada para impedir cadastro nominal automático | A PR #92 foi integrada; existe teste `DatabaseInitializerSemPessoasNominaisTest`. A suíte de 502 testes foi relatada pelo usuário como aprovada, mas o log e o SHA exato testado não foram fornecidos. |
| **Pendente** | Inicializar a aplicação com banco novo/vazio e confirmar que nenhum cadastro de pessoa é inserido automaticamente | Executar manualmente em diretório de dados isolado e registrar resultado. Um teste com banco temporário não comprova, sozinho, o fluxo de instalação e inicialização do aplicativo real. |
| **Pendente** | Abrir uma cópia de banco existente e confirmar preservação de pessoas, partes, programações, escalas, histórico e configurações | Registrar contagens e evidências antes/depois; não usar o banco pessoal real para ensaio destrutivo. |
| **Pendente** | Reabrir a aplicação após inicialização limpa e após abertura de banco existente | Confirmar idempotência e ausência de perda ou recriação indevida de registros. |
| **Pendente** | Validar migração real de uma versão anterior para a versão candidata | O teste de compatibilidade com esquema SQLite temporário é evidência de cobertura de migração, não equivale à atualização real do aplicativo instalado. |

### 4.2 Geração, histórico e transações

O relatório [de atomicidade da geração e do histórico](auditoria-atomicidade-geracao-historico-etapa-3.md) descreve as mudanças de implementação e os riscos que ainda precisam de decisão. O relatório não fornece evidência de execução manual no commit candidato.

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Aprovado por inspeção de código, não por validação integrada** | Persistência coordenada de escala e histórico no fluxo de confirmação | A implementação é descrita no relatório de atomicidade. A validação manual no commit candidato não está documentada. |
| **Pendente** | Gerar escala, cancelar a prévia e confirmar que escala/histórico não foram gravados | Executar no Windows e guardar evidência. |
| **Pendente** | Gerar, editar a prévia, salvar e comparar escala e histórico | Executar no Windows e guardar evidência. |
| **Pendente** | Regenerar uma data e confirmar preservação de outras datas e das participações auxiliares | Executar no Windows e guardar evidência. |
| **Pendente** | Simular falha de persistência e confirmar rollback e mensagem de erro | Executar em banco descartável. |
| **Pendente — decisão funcional** | Definir se editar uma escala já salva deve alterar o histórico, sobretudo para datas passadas | O relatório de atomicidade registra que essa semântica ainda precisa ser definida pelo responsável. Não alterar o comportamento por inferência. |
| **Pendente — risco potencial** | Revisar o uso futuro de métodos legados que gravam histórico em transação separada | Não há fluxo de produção confirmado no relatório anterior, mas qualquer novo uso exige revisão de atomicidade. |

### 4.3 Migrações SQLite

O [relatório de migrações da etapa 4](auditoria-migracoes-banco-etapa-4.md) descreve testes com um esquema legado reproduzido e banco temporário. Ele registra que os comandos Maven não foram executados naquele ambiente à época.

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Aprovado conforme relato do usuário, com limite** | Suíte automatizada que inclui testes de migração | O usuário relatou 502/502 testes aprovados na IDE. O commit exato e o log detalhado não foram registrados; portanto, isso não confirma independentemente todos os cenários de migração. |
| **Pendente** | Atualização real de banco de uma instalação anterior | Fazer em ambiente isolado e comparar dados antes/depois. |
| **Pendente** | Confirmar preservação de registros e integridade referencial depois da atualização real | Registrar contagens e `PRAGMA foreign_key_check`. |
| **Pendente — risco arquitetural** | Avaliar a ausência de versão formal do esquema e a atomicidade global da inicialização | O relatório de migrações descreve migrações condicionais e transações locais. A avaliação completa de todas as versões históricas ainda não está comprovada. |

### 4.4 Backups, retenção e restauração

A política está documentada em [retencao-segura-backups.md](retencao-segura-backups.md). O documento descreve o comportamento esperado, mas não é por si só um log de execução.

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Aprovado por inspeção de código/documentação** | Política de retenção conservadora para backups automáticos identificáveis | A política documentada preserva backups manuais, arquivos legados/desconhecidos e cancela a limpeza em caso de classificação insegura. |
| **Pendente** | Criar backup manual e verificar que pode ser aberto como SQLite | Guardar resultado em ambiente de teste. |
| **Pendente** | Criar backup automático e verificar arquivo e metadados | Guardar log/resultado. |
| **Pendente** | Criar mais de 30 backups elegíveis e confirmar a retenção prevista | Não marcar como executado apenas porque a implementação está integrada. |
| **Pendente** | Confirmar preservação de backups manuais, legados e de classificação desconhecida | Executar cenário de retenção e registrar arquivos preservados. |
| **Pendente** | Simular falhas de criação, leitura/classificação e exclusão | Confirmar que backups anteriores não são removidos indevidamente. |
| **Pendente** | Restaurar cópia em ambiente isolado, reiniciar e comparar dados | Não executar o ensaio no banco pessoal real. |

### 4.5 Interface e fluxos essenciais

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Não verificado** | Cadastro/edição de pessoas e partes com persistência após reinício | Nenhuma evidência de roteiro manual vinculada ao commit candidato. |
| **Não verificado** | Geração, revisão, edição e salvamento de escalas na interface | Nenhuma evidência de roteiro manual vinculada ao commit candidato. |
| **Não verificado** | Consulta e consistência do histórico na interface | Nenhuma evidência de roteiro manual vinculada ao commit candidato. |
| **Não verificado** | Tratamento de conflitos e mensagens de falha/sucesso | Nenhuma evidência de roteiro manual vinculada ao commit candidato. |
| **Não verificado** | Importação de apostila/JSON, prévia, cancelamento e confirmação | Testes automatizados existem, mas não foi registrada validação visual integrada no commit candidato. |
| **Não verificado** | Configurações persistem após fechar e reabrir a aplicação | Nenhuma evidência de roteiro manual vinculada ao commit candidato. |

### 4.6 Privacidade e arquivos rastreados

| Status | Verificação | Evidência atual / próximo passo |
|---|---|---|
| **Aprovado quanto à árvore atual** | PDF S-140 preenchido não está presente na árvore atual de `main` | A remoção ocorreu depois da auditoria geral. O histórico Git **não foi verificado** quanto a cópias antigas; não se afirma que foi limpo. |
| **Aprovado conforme relato do usuário** | Capturas de tela não expõem dados pessoais | O usuário confirmou essa revisão. Não foram incluídos nomes ou dados pessoais neste documento. |
| **Pendente** | Avaliar o histórico Git e artefatos publicados em relação ao PDF removido | Fazer revisão separada, sem afirmar que o histórico já foi limpo. |
| **Não verificado** | Varredura de vulnerabilidades das dependências | Não há relatório de ferramenta disponível nesta revisão. |
| **Não verificado** | Revisão de licenças e atribuições para distribuição | Não foi realizada avaliação jurídica/licenciamento nesta revisão. |

## 5. CI e comandos recomendados

O workflow `.github/workflows/maven-tests.yml` está presente em `main`. Ele configura Java 22, instala Xvfb e executa:

```bash
xvfb-run --auto-servernum mvn --batch-mode --no-transfer-progress clean test
```

O workflow executa `clean test`, não `clean verify`. A execução bem-sucedida da CI na PR #92 e o relato de 502/502 testes na IDE são evidências positivas, mas não devem ser convertidos em afirmação de que `mvn clean verify`, o instalador ou a atualização real foram validados.

Na máquina de validação do release, executar no SHA candidato explicitamente escolhido:

```powershell
mvn clean test
mvn clean verify
```

Guardar os logs completos e os relatórios Surefire. Registrar separadamente a geração do instalador, a instalação/execução do EXE e a atualização real de banco.

## 6. Itens ainda pendentes ou não verificados da auditoria geral

A auditoria geral de 2026-10-23 foi produzida para o commit `2ce671234f37fe2b05fff44bf68df8421333d1ae`, anterior às mudanças integradas depois. Ela ainda contém afirmações históricas que não refletem o estado atual (cadastros nominais automáticos, PDF presente na árvore e ausência de CI). Esta PR não altera aquele relatório.

Continuam visíveis e não devem ser considerados resolvidos por inferência:

- atualização da auditoria geral para o commit atual e reclassificação dos achados afetados pelas PRs posteriores;
- instalação limpa e preservação de banco existente verificadas no aplicativo real;
- `mvn clean verify` no commit candidato, com log e contagens;
- geração, instalação e execução do instalador Windows, com hash e evidência;
- atualização real de versão anterior preservando o banco e reinicializando corretamente;
- validação manual integrada de interface, geração, histórico, backup e restauração;
- matriz completa de versões históricas do esquema e avaliação da atomicidade global das migrações;
- decisão funcional sobre a relação entre edição de escalas persistidas e histórico;
- revisão dos riscos dos métodos legados de persistência de histórico antes de novos usos;
- análise de vulnerabilidades e revisão de licenças/atribuições;
- verificação do histórico Git relacionado ao PDF removido.

A existência do workflow CI já não é uma pendência de ausência de automação; ainda é necessário manter evidência da execução referente ao commit candidato.

## 7. Decisão de liberação

**Decisão: BLOQUEADO POR PENDÊNCIAS. Não declarar a versão 1.2.5 pronta para distribuição.**

A aprovação relatada da suíte de testes e a correção da inicialização nominal são progressos relevantes, mas não comprovam a validação do instalador, a instalação limpa, a atualização real e a preservação do banco existente. Nenhum item manual sem evidência foi marcado como aprovado.

Esta revisão é documental. Não altera código de produção, banco de dados, versão do aplicativo ou instalador. Não realiza publicação, distribuição ou merge.
