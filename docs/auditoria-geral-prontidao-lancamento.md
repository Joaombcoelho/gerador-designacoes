> **Atualização de estado — 2026-10-29 (instalador 1.2.5 e atualização de instalação existente)**
>
> O responsável pelo projeto informou que gerou o instalador Windows da versão `1.2.5`, executou a atualização e confirmou que o processo funcionou e que o banco de dados existente foi preservado.
>
> **Evidência e limites:** este registro é baseado na validação manual relatada pelo usuário; não foram anexados log de empacotamento, arquivo EXE, hash SHA-256, capturas ou inventário antes/depois do banco. A evidência confirma o cenário de atualização relatado, mas não comprova instalação limpa, repetição da atualização, desinstalação/reinstalação, restauração de backup ou validação de todas as tabelas e configurações.
>
> **Estado atualizado:** a geração do instalador `1.2.5` e a atualização de uma instalação existente com preservação do banco deixam de ser pendências sem validação e passam a constar como **aprovadas conforme relato do usuário**. Permanecem pendentes os demais itens sem evidência: instalação limpa, ensaio integrado de backup/restauração, revisão do histórico Git do PDF anteriormente sinalizado e conclusão da análise de dependências/licenças. Não declarar o lançamento integralmente liberado apenas com base neste teste.
>
> ---
> **Atualização de estado — 2026-10-28 (após a PR #112)**  
>  
> Esta atualização complementa a auditoria histórica abaixo, originalmente referente a 2026-10-23 e ao commit `2ce671234f37fe2b05fff44bf68df8421333d1ae`. A `main` foi verificada em `23f51c7e7561272e173e83edb7bbe9bb1a38e818`, merge da PR #112 (`feat(history): require explicit confirmation for retroactive corrections`).  
>  
> **Novas evidências registradas:**  
> - A PR [#112](https://github.com/Joaombcoelho/gerador-designacoes/pull/112) está mesclada.  
> - O usuário informou que o teste direcionado executou **63 testes com sucesso**, e que `mvn clean test` e `mvn clean verify` concluíram com **530 testes aprovados**. Estes são resultados relatados pelo usuário; os logs completos, o número de ignorados e a associação exata de cada execução ao SHA não foram anexados a este documento.  
> - O usuário confirmou a validação manual no Windows dos fluxos de correção retroativa: confirmação, cancelamento sem salvar e preservação das demais datas históricas, incluindo os fluxos testados nas telas de geração e programação. Registro baseado no relato do usuário, não em execução independente do auditor.  
> - Os workflows do GitHub Actions consultados para o head da PR #112 — **Testes Maven** e **Auditoria de dependências 1.2.5** — terminaram com conclusão `success`. Isso comprova as execuções daquela PR, não uma validação do instalador.  
>  
> **Estado atual da liberação:** continua **BLOQUEADO POR PENDÊNCIAS**. Ainda não há evidência registrada de geração e hash do instalador EXE 1.2.5, instalação limpa, atualização real sobre banco fictício de versão anterior, desinstalação/reinstalação, ou ensaio integrado de restauração. Permanecem também as verificações pendentes de histórico Git do PDF removido, dependências/licenças e atualização dos documentos com os resultados dessas etapas.  
>  
> Esta atualização não altera o escopo da auditoria histórica nem declara o aplicativo pronto para distribuição.  
>  
> ---

# Auditoria Geral de Prontidão para Lançamento — Gerador de Designações

**Data da auditoria:** 2026-10-23  
**Repositório:** [Joaombcoelho/gerador-designacoes](https://github.com/Joaombcoelho/gerador-designacoes)  
**Branch examinada:** `main`  
**Commit examinado:** [`2ce671234f37fe2b05fff44bf68df8421333d1ae`](https://github.com/Joaombcoelho/gerador-designacoes/commit/2ce671234f37fe2b05fff44bf68df8421333d1ae)  
**Versão declarada:** `1.2.5` em `pom.xml` e `scripts/build-installer.ps1`  
**Status final:** **Não pronto para distribuição**  
**Escopo de alterações:** somente este relatório nesta branch. Nenhum código de produção, banco, versão, instalador ou artefato de distribuição foi alterado.

> A auditoria foi feita por inspeção do repositório remoto e de arquivos rastreados. Não houve checkout executável com Maven/JDK 22 neste ambiente. Resultados de testes ou validações manuais relatados pelo usuário são identificados como tais, não como execuções independentes do auditor.

## 1. Resumo executivo

A aplicação possui uma estrutura de camadas reconhecível, testes automatizados para regras de negócio e persistência, migrações SQLite, confirmação transacional da importação, persistência coordenada de escala/histórico e uma política conservadora de retenção de backups. As PRs recentes #87, #88, #89 e #90 estão integradas na história de `main`; no momento da consulta, a API do GitHub não listava PRs abertas.

**Há um bloqueador confirmado para distribuição:** `DatabaseInitializer.initialize()` chama `cadastrarPessoasIniciais()`, que contém **44 registros de pessoas com nomes específicos** e os insere quando não encontra o nome no banco. Uma instalação nova pode, portanto, iniciar com pessoas que não foram cadastradas pelo novo usuário. Como a rotina é executada na inicialização, esses registros também podem reaparecer depois de removidos. Os nomes e demais atributos pessoais foram deliberadamente omitidos deste relatório.

Há também um arquivo público chamado `modelos/S-140_T_Preenchido.pdf` e capturas de tela rastreadas no repositório. O conteúdo binário dessas imagens/documentos não foi inspecionado nesta auditoria; não afirmo que contenham dados pessoais, mas a distribuição pública desses artefatos precisa ser verificada antes do lançamento. O script de instalador observado copia o modelo `S-89_T.pdf`, não esse PDF preenchido; isso não elimina a exposição potencial pelo próprio repositório público.

### Contagem do checklist desta auditoria

A tabela de controle abaixo contém **30 itens**:

| Status | Quantidade |
|---|---:|
| Aprovado por inspeção de código/repositório ou relato explicitamente identificado | 10 |
| Reprovado — defeito/risco confirmado | 2 |
| Pendente — ação ou decisão necessária | 5 |
| Não verificado — evidência insuficiente | 13 |
| **Total** | **30** |

“Aprovado” não significa que o sistema inteiro esteja aprovado para distribuição. O relatório contém um bloqueador confirmado e pendências de validação.

## 2. Escopo, método e limitações

Foram inspecionados:
- metadados do repositório e o commit atual de `main`;
- histórico recente de commits, PRs recentes e branches listadas pelo GitHub;
- `pom.xml`, `README.md`, `.gitignore` e `scripts/build-installer.ps1`;
- `DatabaseInitializer`, `ConnectionFactory`, `MainApp`, serviços de backup, restauração, persistência de escalas e confirmação de importação;
- testes de migração, retenção, backup/restauração e importação;
- relatórios anteriores de atomicidade, migrações e checklist de liberação;
- árvore de arquivos rastreados para localizar bancos, arquivos de dados e artefatos de distribuição.

Limitações:
- Não foi possível executar `mvn clean test`, `mvn clean verify`, o aplicativo JavaFX, o script PowerShell ou o instalador neste ambiente.
- O usuário relatou anteriormente que a suíte completa passou depois da correção da PR #88; o total e a saída completa dessa execução não estavam disponíveis para esta auditoria.
- A API de status do commit não retornou verificações de CI. A árvore atual de `main` não contém `.github/workflows`.
- O conteúdo interno do PDF preenchido, do DOCX e das capturas de tela não foi analisado; por isso, a presença de dados pessoais nesses binários é classificada como não verificada.
- Não foi realizada varredura especializada de vulnerabilidades de dependências nem avaliação jurídica de licenças.
- Não foi acessado nem alterado o banco pessoal do usuário.

## 3. Estado do Git, branches, PRs e versão

### 3.1 Base examinada

O commit examinado é `2ce671234f37fe2b05fff44bf68df8421333d1ae`, cujo assunto é uma atualização do checklist de liberação. Esse commit é posterior ao merge da PR #90; portanto, o commit-base escrito no checklist de liberação (`cbc5858...`) está desatualizado em relação à `main) atual.

PRs recentes verificadas como integradas:
- [#87 — retenção segura de backups automáticos](https://github.com/Joaombcoelho/gerador-designacoes/pull/87)
- [#88 — compatibilidade das migrações SQLite](https://github.com/Joaombcoelho/gerador-designacoes/pull/88)
- [#89 — checklist de liberação da etapa 5](https://github.com/Joaombcoelho/gerador-designacoes/pull/89)
- [#90 — revisão do checklist após merges e testes](https://github.com/Joaombcoelho/gerador-designacoes/pull/90)

A consulta direta à API do GitHub retornou zero PRs abertas no momento da auditoria. A branch `main` não está marcada como protegida na resposta de branches consultada. A listagem contém muitas branches históricas de funcionalidades e correções; isso não prova que haja código pendente de integração, mas recomenda uma limpeza de branches antigas depois de confirmar que não são mais necessárias.

A PR #73, destinada a configurar testes Maven no GitHub Actions, aparece fechada sem `merged_at`; não há diretório `.github/workflows` na árvore atual de `main`. Assim, não foi encontrada automação CI ativa no código examinado.

### 3.2 Versão e empacotamento

- `pom.xml`: versão `1.2.5`, compilação com `maven.compiler.release=22`.
- `scripts/build-installer.ps1`: versão `1.2.5` no diretório de preparação, nome esperado do JAR e parâmetro `jpackage --app-version`.
- O script exige Maven, Java, `jlink`, `jpackage` e `JAVA_HOME` apontando para JDK 22 com `jmods`.
- O script executa `mvn clean package -DskipTests`, copia dependências, verifica bibliotecas requeridas, cria runtime reduzido e tenta produzir um instalador Windows.
- O script copia `modelos/S-89_T.pdf` para a área de empacotamento. Não copia `modelos/S-140_T_Preenchido.pdf`.
- Há arquivos de backup antigos de POM/script rastreados. Eles não são referenciados pelo script principal, mas podem confundir manutenção e deveriam ser avaliados separadamente.

A inspeção estática confirma coerência da versão declarada; não confirma que o JAR/EXE foi gerado, instalado ou executado com sucesso.

## 4. Achados classificados por prioridade

### BLOQ-01 — Pessoas com nomes específicos são inseridas automaticamente

**Categoria:** distribuição sem dados pessoais; inicialização/banco.  
**Status:** Reprovado — confirmado por inspeção de código.  
**Evidência:** [`DatabaseInitializer.java`](https://github.com/Joaombcoelho/gerador-designacoes/blob/main/src/main/java/br/com/geradordesignacoes/database/DatabaseInitializer.java), método `initialize()` chama `cadastrarPessoasIniciais(connection)`; esse método contém 44 chamadas de inserção de pessoas com nomes específicos. A rotina consulta a existência pelo nome e insere o registro quando não encontra correspondência.

**Impacto:** uma instalação nova pode conter dados de pessoas que o novo usuário não cadastrou. Como a verificação ocorre durante a inicialização, registros dessa lista podem voltar a ser inseridos quando estiverem ausentes. Isso pode expor dados pessoais, induzir o usuário a gerar designações incorretas e violar o requisito de distribuição limpa.

**Ação recomendada:** bloquear a distribuição até que o responsável confirme quais registros são dados reais e quais, se houver, são exemplos sintéticos necessários. Propor em PR separada uma inicialização que cadastre apenas dados estruturais indispensáveis e nunca pessoas reais por padrão. Se dados de demonstração forem necessários, devem ser sintéticos, explicitamente opt-in e isolados do fluxo normal. Não remover nem alterar dados do banco real como parte desta auditoria.

**Teste de confirmação/correção:** em banco temporário vazio, executar a inicialização e verificar que não existe nenhuma pessoa pré-cadastrada; reinicializar e confirmar idempotência; verificar que cadastros feitos pelo usuário não são apagados nem recriados após remoção legítima. Testar também uma cópia de banco existente para garantir preservação dos dados reais.

### ALTA-01 — Documento potencialmente preenchido rastreado no repositório público

**Categoria:** privacidade e distribuição.  
**Status:** Não verificado — risco concreto que exige inspeção.  
**Evidência:** [`modelos/S-140_T_Preenchido.pdf`](https://github.com/Joaombcoelho/gerador-designacoes/blob/main/modelos/S-140_T_Preenchido.pdf) está rastreado na branch pública. O nome indica que pode conter conteúdo preenchido, mas o conteúdo binário não foi inspecionado nesta auditoria.

**Impacto:** se o arquivo contiver nomes, datas, congregação ou designações reais, esses dados podem estar acessíveis a qualquer pessoa com acesso ao repositório. O fato de o instalador principal não copiar esse PDF não remove o arquivo do repositório público.

**Ação recomendada:** verificar o conteúdo em ambiente apropriado sem publicar nomes no relatório. Se houver dados reais, substituir o arquivo por modelo vazio ou exemplo sintético e avaliar o histórico Git e os artefatos publicados. A remoção de um arquivo da versão atual não apaga cópias em commits antigos.

### ALTA-02 — Capturas de tela públicas não tiveram conteúdo verificado

**Categoria:** privacidade e documentação.  
**Status:** Não verificado.  
**Evidência:** a árvore rastreada inclui capturas em `screenshot/`, inclusive telas de histórico, cadastro e programação. O conteúdo visual não foi inspecionado.

**Impacto:** capturas podem conter nomes ou informações operacionais de uma congregação. Não há evidência suficiente para afirmar que isso ocorre.

**Ação recomendada:** revisar localmente cada imagem antes de distribuição pública; substituir dados reais por exemplos sintéticos ou ocultar informações. Registrar apenas o resultado da verificação, sem reproduzir nomes.

### ALTA-03 — Ausência de CI ativa na branch principal

**Categoria:** testes e distribuição.  
**Status:** Pendente.  
**Evidência:** não foi encontrado `.github/workflows` na árvore de `main`; a PR #73, voltada a configurar testes Maven no GitHub Actions, está fechada sem merge. A consulta de status do commit não retornou verificações.

**Impacto:** mudanças podem ser integradas sem uma execução automatizada obrigatória de compilação/testes. O relato local de testes aprovados é útil, mas não substitui uma verificação repetível associada ao commit.

**Ação recomendada:** propor workflow separado que use JDK 22, execute `mvn clean verify` e publique o resumo dos testes. Confirmar que a execução funciona no GitHub Actions antes de tornar a verificação obrigatória.

### ALTA-04 — Migrações sem versão formal e inicialização não globalmente atômica

**Categoria:** banco de dados e atualização.  
**Status:** Pendente — risco arquitetural identificado em documentação/código.  
**Evidência:** `DatabaseInitializer.initialize()` aplica migrações por inspeção de colunas, índices e definição SQL. Algumas reconstruções usam transações locais; a inicialização completa executa uma sequência de operações fora de uma única transação global. [Relatório da etapa de migrações](https://github.com/Joaombcoelho/gerador-designacoes/blob/main/docs/auditoria-migracoes-banco-etapa-4.md).

**Impacto:** uma falha depois de algumas alterações aditivas pode deixar o banco parcialmente atualizado. Os testes cobrem um esquema legado reproduzido e cenários de rollback local, mas não estabelecem por si só uma matriz completa de todas as versões históricas suportadas.

**Ação recomendada:** manter uma matriz explícita de esquemas históricos e testar cópias representativas. Avaliar versionamento de esquema em etapa própria; não introduzir refatoração ampla durante a auditoria.

### MÉDIA-01 — Checklist de liberação contém informações inconsistentes e evidência incompleta

**Categoria:** documentação e controle de release.  
**Status:** Reprovado — inconsistência documental confirmada.  
**Evidência:** [`docs/checklist-liberacao-etapa-5.md`](https://github.com/Joaombcoelho/gerador-designacoes/blob/main/docs/checklist-liberacao-etapa-5.md) mantém como commit examinado `cbc5858...`, embora `main` esteja em `2ce6712...`. Os itens de teste manual estão marcados como executados, mas não registram data, versão/commit testado, ambiente, resultado observado, hash do instalador ou links para logs. A mesma seção marca os cenários de backup como executados e, logo abaixo, afirma que os cenários manuais de retenção continuam pendentes. O documento também marca `mvn clean verify`, instalador e atualização como executados em uma seção, enquanto outras partes dizem que não há evidência específica.

**Impacto:** dificulta distinguir execução real, relato, intenção e pendência; uma decisão de release pode ser tomada com base em um checklist contraditório.

**Ação recomendada:** atualizar o commit-base e para cada execução registrar data, commit, ambiente, comando/roteiro, resultado, artefato e evidência. Não transformar um item em aprovado sem o respectivo registro.

### MÉDIA-02 — Política de edição de escalas existentes e histórico ainda exige decisão funcional

**Categoria:** persistência/histórico.  
**Status:** Pendente — decisão de negócio necessária.  
**Evidência:** [relatório de atomicidade da geração e histórico](https://github.com/Joaombcoelho/gerador-designacoes/blob/main/docs/auditoria-atomicidade-geracao-historico-etapa-3.md) registra que a edição de uma escala já persistida atualiza `designacao` sem atualizar `historico_designacoes`. O relatório ressalta que o histórico pode significar a designação originalmente registrada/realizada, portanto a semântica precisa ser decidida antes de mudar o comportamento.

**Impacto:** escala atual e histórico podem divergir após edição. Isso pode ser correto se o histórico for imutável, ou incorreto se ele representar sempre o estado atual; não cabe ao auditor decidir essa regra.

**Ação recomendada:** o responsável deve definir se edições, especialmente de datas passadas, alteram o histórico. Depois, documentar e testar a regra escolhida.

### MÉDIA-03 — Método legado pode persistir histórico separadamente

**Categoria:** persistência/histórico.  
**Status:** Pendente — risco potencial, não defeito de fluxo de produção confirmado.  
**Evidência:** o relatório de atomicidade registra que `CoordenadorGeracaoSalasService.gerar(...)` persiste histórico em transação própria; a busca de referências documentada não encontrou chamadores de produção. O método foi mantido por compatibilidade.

**Impacto:** se esse método for conectado a um novo fluxo de prévia/confirmar, poderá gravar histórico antes da confirmação coordenada.

**Ação recomendada:** manter o contrato explicitamente documentado e exigir revisão/teste de transação antes de reutilizar o método em qualquer fluxo de produção.

### MÉDIA-04 — Avaliação de vulnerabilidades e licenças não executada

**Categoria:** dependências e distribuição.  
**Status:** Não verificado.  
**Evidência:** `pom.xml` declara versões específicas de SQLite JDBC, Jackson, JavaFX, Ikonli, Apache POI, PDFBox e plugins Maven. Nenhuma ferramenta de análise de vulnerabilidades foi executada; não foi feita revisão jurídica das licenças/atribuições.

**Impacto:** não é possível concluir que todas as dependências estão livres de vulnerabilidades conhecidas ou que os avisos/licenças necessários estão completos.

**Ação recomendada:** executar uma ferramenta de análise de dependências em um ambiente com acesso aos repositórios Maven, revisar os resultados e registrar as licenças/atribuições aplicáveis. Não declarar conformidade legal com base apenas nesta auditoria.

### BAIXA-01 — Muitas branches históricas permanecem no repositório

**Categoria:** manutenção Git.  
**Status:** Pendente — limpeza opcional.  
**Evidência:** a listagem de branches inclui numerosas branches antigas de funcionalidades e correções, algumas correspondentes a PRs já integradas.

**Impacto:** aumenta o ruído na navegação e pode dificultar a identificação da branch de trabalho correta. Não há evidência de que essas branches alterem o conteúdo de `main`.

**Ação recomendada:** depois de confirmar que nenhuma branch é necessária para recuperação ou trabalho em andamento, apagar branches obsoletas em uma tarefa de manutenção separada. Não apagar branches durante esta auditoria.

## 5. Checklist de controle — 30 itens

| # | Item | Status | Evidência/observação |
|---:|---|---|---|
| 1 | Branch principal e commit atual identificados | **Aprovado** | `main` em `2ce6712...` |
| 2 | PRs recentes e estado de integração consultados | **Aprovado** | #87–#90 integradas; consulta retornou zero PRs abertas |
| 3 | Versão do POM e do script comparada | **Aprovado** | Ambos declaram `1.2.5` |
| 4 | Arquitetura em camadas existe | **Aprovado** | Model, DAO, service, controller/view e database presentes |
| 5 | Ausência de banco/CSV/JSON/SQL rastreado na árvore atual | **Aprovado** | Busca por extensões na árvore; não substitui inspeção do conteúdo binário |
| 6 | Script principal não empacota o PDF S-140 preenchido | **Aprovado** | Copia o modelo S-89 para a área de preparação |
| 7 | Testes automatizados relevantes existem | **Aprovado** | Classes para migração, backup/restauração, retenção, importação e geração |
| 8 | Política de retenção conservadora implementada | **Aprovado** | Código valida metadados/hash e preserva classificação incerta |
| 9 | Testes de backup/restauração incluem falhas e banco temporário | **Aprovado** | Testes de integridade, arquivo inválido e rollback |
| 10 | Importação exige prévia válida e persiste o lote em transação | **Aprovado** | `ConfirmacaoImportacaoApostilaService` e testes relacionados |
| 11 | Estado de limpeza do working tree local | **Não verificado** | Inspeção remota não revela alterações locais do usuário |
| 12 | Conteúdo do PDF S-140 preenchido | **Não verificado** | Arquivo rastreado; binário não inspecionado |
| 13 | Conteúdo das capturas de tela públicas | **Não verificado** | Imagens rastreadas; conteúdo não inspecionado |
| 14 | Saída detalhada e total exato da última suíte Maven | **Não verificado** | Usuário relatou aprovação; log/total não anexado |
| 15 | `mvn clean verify` no commit atual | **Não verificado** | Sem execução independente ou evidência específica |
| 16 | Evidência reproduzível dos testes manuais | **Não verificado** | Checklist marca itens como executados, mas não vincula registros |
| 17 | Geração e execução real do instalador Windows | **Não verificado** | Checklist marca executado, mas não registra hash/log/ambiente |
| 18 | Atualização real preservando banco e reinicialização | **Não verificado** | Checklist marca executado, sem comparação/evidência anexada |
| 19 | Varredura de vulnerabilidades de dependências | **Não verificado** | Ferramenta/relatório não disponível |
| 20 | Licenças e atribuições para distribuição | **Não verificado** | Não foi feita avaliação específica |
| 21 | Revisão visual completa de acessibilidade/usabilidade | **Não verificado** | Sem execução da interface nesta auditoria |
| 22 | Cobertura de todas as versões históricas de esquema | **Não verificado** | Teste cobre esquema legado reproduzido, não uma matriz completa |
| 23 | Execução de empacotamento nesta auditoria | **Não verificado** | Script apenas inspecionado |
| 24 | Inicialização de banco novo sem pessoas pré-cadastradas | **Reprovado** | 44 registros nominais são inseridos pelo inicializador |
| 25 | Consistência do checklist de liberação | **Reprovado** | Commit-base desatualizado e status contraditórios |
| 26 | CI automatizada na branch principal | **Pendente** | Nenhum workflow na árvore de `main`; PR #73 fechada sem merge |
| 27 | Estratégia explícita de versão/atomicidade global das migrações | **Pendente** | Migrações condicionais e transações locais; sem tabela formal de versão |
| 28 | Decisão de negócio sobre edição de escalas e histórico | **Pendente** | Semântica precisa ser confirmada pelo responsável |
| 29 | Proteção do método legado de geração de salas antes de novos usos | **Pendente** | Persistência de histórico separada; nenhum chamador de produção encontrado no relatório anterior |
| 30 | Limpeza das branches históricas | **Pendente** | Tarefa de manutenção opcional após confirmar que não são necessárias |

## 6. Dados pessoais e conteúdo da distribuição

### Confirmado
- O inicializador contém 44 chamadas para inserir pessoas com nomes específicos, com atributos associados, e é executado na inicialização. Isso é incompatível com a expectativa de uma instalação nova limpa.

### Não confirmado
- Se `modelos/S-140_T_Preenchido.pdf` contém informações pessoais reais.
- Se as imagens em `screenshot/` exibem nomes ou outros dados reais.
- Se algum artefato já publicado ou cópia histórica do repositório contém outros dados que não aparecem na árvore atual.

### Pontos favoráveis observados
- Não foi encontrado arquivo de banco SQLite, CSV, JSON ou SQL rastreado na árvore atual pela busca de extensões.
- O script principal de empacotamento copia o modelo `S-89_T.pdf`, e não o PDF S-140 preenchido.
- `.gitignore` ignora bancos `.db`, arquivos WAL/SHM, diretório de backup, logs, JARs e EXEs. Isso reduz o risco de novos arquivos locais serem adicionados por engano, mas não remove arquivos já rastreados e não substitui revisão do histórico.

## 7. Testes e validações

### Automatizados
- O usuário relatou que a suíte completa passou depois da correção de migração na PR #88.
- Não há contagem exata ou log completo dessa execução anexado ao repositório no material consultado.
- Não executei `mvn clean test` nem `mvn clean verify` neste ambiente.
- Não foram retornados status checks de CI para o commit atual.
- Existem testes de compatibilidade de migração, transações de escala/histórico, retenção de backups, restauração e importação. A presença desses testes não comprova a execução recente nem cobre automaticamente todas as versões históricas e fluxos visuais.

### Validação manual/distribuição
O checklist atual assinala diversos testes manuais, instalador e atualização como executados, mas não apresenta evidência suficiente para auditar de forma independente cada execução. Não estou descartando o relato do responsável; apenas separo o status declarado da evidência verificável no repositório.

Para fechar essa lacuna, registrar:
1. commit exato testado;
2. data, Windows e versão do JDK;
3. saídas de `mvn clean test` e `mvn clean verify`, com contagem de testes/falhas/erros/ignorados;
4. saída do script de empacotamento e SHA-256 do EXE;
5. resultado de instalação em ambiente isolado;
6. versão anterior usada na atualização, cópia de segurança, comparação do banco antes/depois e resultado após reinicialização;
7. verificação de conteúdo do PDF preenchido e das capturas, sem anexar dados pessoais ao relatório.

## 8. Plano de ação recomendado, em ordem

1. **Bloquear a distribuição** até resolver a inicialização de pessoas com nomes específicos.
2. Revisar o PDF S-140 preenchido e as capturas de tela para confirmar se há dados pessoais; substituir/remover conteúdo real em uma PR própria caso necessário e avaliar o histórico Git.
3. Corrigir o checklist de liberação para usar o commit atual e registrar evidências auditáveis, mantendo pendente qualquer item sem evidência.
4. Adicionar CI para executar `mvn clean verify` com JDK 22 e registrar o resultado por commit.
5. Completar a matriz de migrações históricas e documentar a política de falha/recuperação de inicialização; avaliar versionamento de esquema sem refatoração ampla.
6. Confirmar a regra de negócio para edição de escalas persistidas e histórico.
7. Executar a análise de vulnerabilidades e revisar licenças/atribuições.
8. Validar instalador, atualização real e recuperação em ambiente Windows descartável, preservando o banco real.
9. Depois de concluídas as ações anteriores, revisar branches históricas obsoletas em uma tarefa separada.

## 9. Critérios objetivos para considerar o sistema pronto

A decisão de prontidão só deve ser reconsiderada quando:
- uma instalação nova não inserir pessoas reais ou registros nominais não solicitados;
- PDF e capturas forem revisados e não expuserem dados particulares;
- `mvn clean test` e `mvn clean verify` passarem no commit candidato com logs/contagens registrados;
- CI estiver executando os testes no commit candidato;
- cenários de migração histórica, persistência, backup, retenção e restauração estiverem cobertos e validados;
- instalador for gerado, instalado e executado em ambiente Windows isolado;
- atualização de uma versão anterior preservar o banco e reiniciar corretamente;
- decisões pendentes sobre histórico estiverem documentadas;
- licenças e dependências tiverem sido revisadas;
- o checklist de release refletir o commit real e contiver evidências verificáveis.

## 10. Decisão final

**Não pronto para distribuição.**

O motivo principal é o bloqueador confirmado de dados nominais inseridos pelo inicializador. Há também um risco de privacidade não verificado em um PDF preenchido rastreado no repositório público, falta de CI ativa na branch principal e evidências de release incompletas/inconsistentes. Isso não significa que as funcionalidades centrais não funcionem; significa que ainda não há base suficiente para distribuir o programa a outras congregações com segurança.

Esta PR entrega somente o diagnóstico. Nenhuma correção de produção, mudança de versão, alteração de banco, geração de instalador, merge ou publicação foi realizada.
