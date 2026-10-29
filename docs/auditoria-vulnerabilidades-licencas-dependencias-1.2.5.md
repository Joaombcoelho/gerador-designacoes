> **Atualização de estado — 2026-10-29: tratamento inicial dos alertas do Dependency-Check**
>
> A revisão do relatório produzido pelo workflow da PR #114 encontrou 25 ocorrências de CVEs em seis artefatos: Jackson Databind, Jackson Core, Log4j API, SQLite JDBC, Commons Lang e PDFBox. O relatório apontou uma ocorrência crítica, dez altas e quatorze médias. Essas contagens são ocorrências do scanner, não 25 vulnerabilidades independentes nem prova automática de explorabilidade.
>
> **Correções de versão aplicadas no `pom.xml` desta branch:**
>
> | Artefato | Versão anterior | Versão atualizada | Motivo principal |
> |---|---:|---:|---|
> | `com.fasterxml.jackson.core:jackson-databind` | 2.19.2 | 2.21.8 | Corrige o alerta crítico CVE-2026-110879 e inclui correções da série 2.21 para outros avisos do relatório. A versão do core/annotations deve ser confirmada pela árvore Maven resolvida. |
> | `org.xerial:sqlite-jdbc` | 3.50.3.0 | 3.53.4.0 | Atualiza a biblioteca empacotada e o SQLite nativo; a versão do SQLite 3.53.2 ou posterior contém a correção publicada para CVE-2026-11822 e CVE-2026-11824. |
> | `org.apache.commons:commons-lang3` (transitiva) | 3.16.0 resolvida no relatório | 3.20.0 fixada diretamente | Está acima da versão 3.18.0 que corrige CVE-2025-48924. |
> | `org.apache.logging.log4j:log4j-api` (transitiva) | 2.24.3 resolvida no relatório | 2.25.5 fixada diretamente | Inclui a correção de CVE-2026-49844, publicada em Log4j 2.25.5. |
> | `org.apache.pdfbox:pdfbox` | 3.0.5 | 3.0.8 | Versão corrigida para CVE-2026-33929. O aviso oficial informa que o problema está no módulo de exemplos, não no núcleo da biblioteca; a atualização reduz o alerta, mas a aplicabilidade ao artefato distribuído deve ser reavaliada no relatório novo. |
>
> As duas dependências transitivas foram declaradas diretamente para controlar a versão efetivamente resolvida e empacotada. Não houve alteração do esquema SQLite nem das regras de negócio. A atualização do Jackson é uma mudança de versão relevante, ainda que permaneça na linha 2.x; por isso, testes automatizados, árvore Maven, empacotamento Windows e novo scanner são obrigatórios antes de considerar a correção concluída.
>
> **Risco e limites da análise:** o programa é uma aplicação desktop local, e o código de importação anteriormente inspecionado usa Jackson para construir uma árvore JSON (`readTree`), sem evidência de configuração de tipagem polimórfica. Isso reduz a evidência de exposição aos cenários de desserialização polimórfica, mas não substitui a correção da biblioteca nem uma revisão do uso completo. O risco do SQLite também depende das funções SQLite habilitadas e de quem consegue influenciar SQL; a atualização foi preferida a depender apenas dessa avaliação contextual. A ocorrência de CVE-2026-34479 atribuída pelo scanner a `log4j-api` precisa ser conferida: o aviso oficial descreve especificamente o bridge Log4j 1-to-2 e `Log4j1XmlLayout`, não apenas a API. Não classificar esse item como corrigido só pela troca da API.
>
> **Validação pendente:** o commit que contém essas atualizações precisa concluir `mvn clean test`, `mvn clean verify`, inventário/empacotamento Windows e nova execução do Dependency-Check e do relatório de licenças. O resultado do scanner deve ser comparado CVE por CVE; alertas residuais, mudanças de árvore, compatibilidade e eventuais falsos positivos devem ser registrados com evidência. Até essa revisão, as versões foram atualizadas, mas a auditoria de segurança permanece **em validação**, não aprovada.
>
> **Fontes oficiais consultadas:** [Jackson 2.21.8 e correção CVE-2026-110879](https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.21.8), [aviso Jackson CVE-2026-110879](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph), [vulnerabilidades SQLite](https://www2.sqlite.org/cves.html), [Commons Lang CVE-2025-48924](https://nvd.nist.gov/vuln/detail/cve-2025-48924), [Log4j 2.25.5 release notes](https://logging.apache.org/log4j/2.x/release-notes.html) e [PDFBox security](https://pdfbox.apache.org/security.html).
>
> ---
>
> **Atualização de estado — 2026-10-29: falha de autenticação no OSS Index e correção preparada**
>
> A execução do workflow para o commit `db5da7b0a64f1da43c3dba83ae90ac06cf6ce35e` concluiu os testes Maven, o inventário de empacotamento Windows e o relatório de licenças com sucesso, mas a auditoria falhou: o Dependency-Check 12.1.0 recebeu HTTP `401 Unauthorized` repetidamente no endpoint antigo `ossindex.sonatype.org`. O workflow foi atualizado para usar Dependency-Check 12.2.2, o endpoint `https://api.guide.sonatype.com` e o segredo `SONATYPE_GUIDE_PAT`.
>
> **Ação necessária antes de reexecutar:** criar um Personal Access Token em Sonatype Guide e cadastrá-lo nas configurações de Actions do repositório como segredo `SONATYPE_GUIDE_PAT`. O valor não deve ser colocado no código, em logs ou nesta documentação. A chave `NVD_API_KEY` continua separada e é usada para o NVD.
>
> A correção ainda precisa ser validada por uma nova execução. Não considerar a auditoria aprovada até o scanner concluir e os relatórios gerados serem revisados.
>
> Fonte: [documentação do Dependency-Check sobre o analisador OSS Index](https://dependency-check.github.io/DependencyCheck/analyzers/oss-index-analyzer.html) e [instruções oficiais de token do Sonatype Guide](https://help.sonatype.com/en/using-guide-personal-access-tokens-with-oss-index-api-integrations.html).
>
> ---

> **Atualização de estado — 2026-10-29: execução de auditoria na PR #114**
>
> No workflow [Auditoria de dependências 1.2.5](https://github.com/Joaombcoelho/gerador-designacoes/actions/runs/40868767172), para o commit `d68db93baa342f8538fbcaf6b7c8ceb732778559`, os estados consultados foram:
>
> - **Testes Maven (Java 22): aprovado** — `clean test` e `clean verify` concluídos com sucesso.
> - **Inventário do empacotamento Windows: aprovado** — o script oficial `scripts/build-installer.ps1` concluiu com sucesso e o workflow publicou o artefato de evidências `inventario-empacotamento-windows-1.2.5`.
> - **Árvore completa e árvore runtime: concluídas com sucesso** no job de auditoria.
> - **OWASP Dependency-Check: em execução na última consulta**; o relatório de licenças transitivas ainda não havia iniciado. Portanto, a análise de CVEs e a auditoria de licenças continuam sem conclusão nesta atualização.
>
> O sucesso do empacotamento de CI comprova que o script executou no runner Windows, mas não substitui a validação manual de atualização do usuário nem comprova instalação limpa. Também não significa que o relatório de CVEs esteja limpo: o scanner ainda não havia terminado e o workflow está configurado para publicar relatórios como evidência.
>
> **Decisão provisória:** não declarar a auditoria de segurança/licenças integralmente aprovada até a conclusão do scanner, do relatório de licenças e da revisão dos resultados/artifatos.
>
> ---
# Auditoria de vulnerabilidades e licenças — versão 1.2.5

**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Commit-base:** `5b81961bcc77d1e22b62b1accb2229d95284612c`  
**Branch:** `audit/dependencias-seguranca-licencas`  
**Data da pesquisa externa:** 2026-10-23  
**Status:** **PARCIAL — foram encontrados alertas de versão relevantes; a árvore resolvida, o scanner automatizado e a auditoria integral de licenças ainda precisam ser executados.**  
**Escopo da alteração:** documentação de auditoria. Nenhuma dependência, versão, funcionalidade ou configuração de empacotamento foi alterada.

## 1. Resumo executivo

A análise estática do `pom.xml`, do script de empacotamento e do uso do Jackson identificou:

1. `jackson-databind 2.19.2` está dentro das faixas afetadas de diversos avisos oficiais publicados em agosto, setembro e outubro de 2026. Os avisos têm pré-condições diferentes; a leitura JSON encontrada no código usa `ObjectMapper.readTree(...)` para construir uma árvore `JsonNode`, sem configuração de tipagem polimórfica ou vinculação a tipos de domínio. Portanto, **não foi demonstrado que os caminhos específicos desses avisos sejam exploráveis no uso atual**. A versão ainda deve ser atualizada para uma versão corrigida após confirmar a árvore e validar compatibilidade.
2. O script de empacotamento exige que um `log4j-api-*.jar` esteja presente no diretório de runtime. A versão realmente resolvida não foi determinada nesta execução. Se for uma versão entre 2.13.1 e 2.25.4, o alerta CVE-2026-49844 se aplica à faixa publicada, embora sua exploração dependa de uso específico de `MapMessage` com serialização JSON. É necessário confirmar a versão pela árvore Maven e a presença no pacote.
3. O instalador copia dependências Maven do escopo runtime e um runtime Java criado por `jlink`. Logo, as obrigações de segurança e licenciamento abrangem dependências transitivas, bibliotecas nativas incluídas no JAR SQLite, Java/JavaFX e outros recursos realmente distribuídos.
4. Não foram encontrados no `pom.xml` nem no workflow consultado plugins ou etapas para scanner de CVEs, relatório de dependências ou relatório de licenças. Não foi localizado um SBOM ou relatório de auditoria existente na raiz inspecionada.
5. **Não é possível declarar o conjunto de dependências seguro ou integralmente compatível em licenças ainda.** A execução de Maven e de scanners não ocorreu nesta sessão, e a lista completa de versões transitivas precisa ser gerada no ambiente do projeto.

Nenhuma dependência foi alterada porque a árvore efetivamente resolvida e os resultados reproduzíveis do scanner ainda não estão disponíveis. Isso evita corrigir uma versão presumida ou introduzir mudanças sem testes.

## 2. Ambiente e ferramentas

- Data de pesquisa de avisos externos: **2026-10-23**.
- Repositório consultado: branch `main`, commit `314de245c1f75c9db9511e621defc726559f38f4`.
- Ambiente de execução desta auditoria: inspeção remota de arquivos GitHub e consulta a fontes públicas. Não havia checkout local executável com Maven/JDK para resolver dependências, executar testes ou gerar o instalador.
- Ferramentas configuradas no repositório: workflow `.github/workflows/maven-tests.yml` instala Temurin Java 22 e executa `mvn clean test`. Não há scanner de dependências nesse workflow.
- Fontes de segurança consultadas: avisos oficiais do GitHub Security Advisories do projeto Jackson, NVD/NIST e páginas de artefatos Maven Central/Sonatype.
- Fontes de licença consultadas: metadados POM/Maven Central e páginas oficiais de OpenJFX e Font Awesome.

### Comandos pendentes de execução

Na raiz do repositório, com JDK 22 e Maven instalados:

```powershell
mvn --batch-mode --no-transfer-progress dependency:tree -Dverbose -Dscope=runtime
mvn --batch-mode --no-transfer-progress dependency:tree -Dverbose
mvn --batch-mode --no-transfer-progress clean test
mvn --batch-mode --no-transfer-progress clean verify
```

Guardar a árvore completa e a árvore runtime separadamente. A árvore completa inclui dependências de teste; a árvore runtime aproxima o conjunto copiado pelo script de empacotamento.

Para gerar uma verificação de vulnerabilidades reproduzível, usar OWASP Dependency-Check ou outro scanner mantido, registrar a versão do scanner, a data de atualização da base, o relatório completo e a árvore exata que foi analisada. Dependency-Check pode depender da disponibilidade da base NVD e de uma chave de API; se isso for necessário, configurar o segredo no ambiente de CI e nunca registrar o valor no relatório. Não foi executado um scanner nesta tarefa.

Para licenças, gerar relatório de terceiros a partir da árvore resolvida (por exemplo, com `org.codehaus.mojo:license-maven-plugin:2.7.1:aggregate-third-party-report`) e revisar manualmente as licenças e avisos dos artefatos efetivamente empacotados. O relatório automático é uma entrada de auditoria, não uma decisão legal automática.

## 3. Dependências declaradas e escopo

A tabela abaixo reflete as versões diretas declaradas no `pom.xml`, não substitui uma árvore Maven resolvida.

| Dependência direta | Versão declarada | Escopo Maven | Incluída pelo fluxo de empacotamento? |
|---|---:|---|---|
| `org.xerial:sqlite-jdbc` | `3.50.3.0` | compile/runtime | Sim |
| `com.fasterxml.jackson.core:jackson-databind` | `2.19.2` | compile/runtime | Sim |
| `org.openjfx:javafx-controls` | `21.0.7` | compile/runtime | Sim |
| `org.openjfx:javafx-fxml` | `21.0.7` | compile/runtime | Sim |
| `org.kordamp.ikonli:ikonli-javafx` | `12.3.1` | compile/runtime | Sim |
| `org.kordamp.ikonli:ikonli-fontawesome5-pack` | `12.3.1` | compile/runtime | Sim |
| `org.apache.poi:poi-ooxml` | `5.4.1` | compile/runtime | Sim |
| `org.apache.pdfbox:pdfbox` | `3.0.5` | compile/runtime | Sim |
| `org.junit.jupiter:junit-jupiter` | `5.10.2` | test | Não, via `-DincludeScope=runtime` |
| `junit:junit` | `4.13.1` | test | Não, via `-DincludeScope=runtime` |

O `pom.xml` não declara perfis Maven. Os plugins de compilação, Surefire e JavaFX são ferramentas de build/execução; suas dependências não devem ser confundidas automaticamente com as bibliotecas copiadas para o instalador.

O script `scripts/build-installer.ps1` executa `mvn dependency:copy-dependencies -DincludeScope=runtime` e valida a presença de padrões como JavaFX para Windows, SQLite JDBC, Ikonli, PDFBox, POI, XMLBeans, Commons Compress/IO/Collections/Math/Codec, CurvesAPI, SparseBitSet e Log4j API. Isso confirma que o empacotamento espera alguns componentes transitivos, mas **não determina as versões efetivas nem prova que a lista de padrões contém todas as bibliotecas**.

## 4. Alertas de segurança identificados

### 4.1 Jackson Databind — versão direta 2.19.2

A versão declarada `2.19.2` está dentro de várias faixas afetadas em avisos oficiais. A avaliação abaixo separa a correspondência de versão do pré-requisito técnico para exploração.

| Aviso | Severidade publicada | Faixa que inclui 2.19.2 | Correção indicada pela fonte | Aplicabilidade ao código inspecionado |
|---|---|---|---|---|
| [CVE-2026-110879 / GHSA-m8xf-2cr7-77ph](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph) | Crítica | Jackson 2.x `2.19.0–2.21.7` | `2.21.8` | Requer caminhos de desserialização polimórfica/configuração de tipagem que não foram encontrados no serviço de importação inspecionado. |
| [GHSA-wv8q-qhhj-9h54](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-wv8q-qhhj-9h54) | Alta | `2.19.0–2.21.6` | `2.21.7` | Requer `@JsonTypeInfo(use = Id.NAME, defaultImpl=...)` ou fallback equivalente, IDs não confiáveis e mapper de longa duração. Não foi encontrado esse uso. |
| [GHSA-cxp5-3px4-pw24](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-cxp5-3px4-pw24) | Alta | `2.19.0–2.21.6` | `2.21.7` | Requer desserialização de coleções/mapas com `@JsonIdentityInfo` e referências pendentes controladas pela entrada. Não foi encontrado esse uso. |
| [GHSA-q4xh-88c3-wmh7](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-q4xh-88c3-wmh7) | Alta | `2.19.0–2.21.5` (fonte lista < `2.21.6`) | `2.21.6` | Requer vincular strings não confiáveis a `Duration` ou `XMLGregorianCalendar`. Não foi encontrado esse modelo. |
| [GHSA-wjgm-6hv5-3cvf](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-wjgm-6hv5-3cvf) | Moderada | `2.19.0–2.21.5` (fonte lista < `2.21.6`) | `2.21.6` | Requer vincular JSON não confiável a um campo `java.nio.file.Path`. Não foi encontrado esse uso. |

**Evidência do código:** `ImportacaoApostilaJsonService` instancia `new ObjectMapper()` e chama `objectMapper.readTree(arquivo.toFile())`, processando a entrada como `JsonNode`. A busca no código não encontrou `@JsonTypeInfo` nem `@JsonIdentityInfo`. Isso reduz a aplicabilidade dos caminhos específicos descritos, mas não prova ausência de todas as vulnerabilidades possíveis no Jackson ou em outros artefatos transitivos.

**Recomendação:** avaliar atualização do Jackson 2.x para uma versão corrigida comum a todos os avisos aplicáveis (a fonte oficial já lista `2.21.8` para o aviso crítico). Antes de mudar a versão, confirmar se a árvore resolve módulos Jackson alinhados e se a alteração é compatível com o formato JSON usado pela aplicação. Executar testes de importação de apostilas e testes completos. Esta PR não modifica a dependência porque não foi possível executar a árvore nem os testes nesta sessão.

### 4.2 Log4j API — dependência transitiva a confirmar

O script de empacotamento exige `log4j-api-*.jar`, logo um artefato correspondente deve estar presente na área de runtime esperada. A versão efetivamente resolvida não foi obtida nesta auditoria.

- [CVE-2026-49844](https://logging.apache.org/security.html#CVE-2026-49844): afeta Apache Log4j API `2.13.1` até antes de `2.25.5`, além de `2.26.0`; a fonte indica correção em `2.25.5` ou `2.26.1`, conforme a linha.
- O defeito descrito envolve serialização JSON de `MapMessage` com valores não finitos. A presença do JAR, isoladamente, não demonstra que a aplicação execute esse caminho.

**Ação pendente:** obter a árvore `runtime`, registrar a versão de `org.apache.logging.log4j:log4j-api`, confirmar se ela é copiada para `dist\GeradorDesignacoes-1.2.5\app` e procurar uso de `MapMessage`/layout JSON no código e na configuração. Se a versão resolvida estiver na faixa afetada, avaliar uma atualização pontual para uma versão corrigida compatível e validar a exportação DOCX/PDF e o pacote.

### 4.3 Outros componentes

- **SQLite JDBC 3.50.3.0:** a busca pública não confirmou nesta sessão uma CVE direta aplicável a essa versão. Isso não equivale a resultado limpo de scanner. O JAR contém bibliotecas nativas SQLite para múltiplas plataformas, então a análise deve considerar também a versão do SQLite incorporada e os avisos do projeto.
- **Apache POI OOXML 5.4.1:** não foi confirmada nesta sessão uma CVE diretamente explorável no uso da aplicação. O projeto POI recomenda cautela ao analisar documentos não confiáveis. A aplicação abre modelos/documentos e gera DOCX; confirmar o caminho de entrada e verificar transitivas antes de concluir.
- **PDFBox 3.0.5:** não foi confirmado nesta sessão um alerta aplicável a essa versão. A aplicação lê/gera PDF; atualizar apenas com base em um resultado de scanner e compatibilidade de exportação S-89 comprovados.
- **JavaFX 21.0.7 e Ikonli 12.3.1:** não foi feita varredura completa de CVEs para esses artefatos nem para todas as transitivas.
- **Plugins Maven:** devem ser examinados separadamente como ferramentas de build. Uma vulnerabilidade em plugin de build não implica automaticamente que o JAR esteja no instalador, mas pode afetar a cadeia de compilação.

## 5. Licenças e obrigações preliminares

A tabela resume metadados de licenças encontrados nas fontes públicas dos projetos/artefatos. Não é um relatório completo de todas as transitivas; essas precisam ser extraídas da árvore e verificadas antes da distribuição.

| Componente | Licença identificada preliminarmente | Obrigações/observações de distribuição |
|---|---|---|
| Jackson Databind 2.19.2 | Apache License 2.0 | Incluir licença e avisos de copyright aplicáveis; preservar avisos e registrar alterações quando exigido. |
| SQLite JDBC 3.50.3.0 | Apache License 2.0 para o driver; SQLite incorporado tem dedicação ao domínio público; repositório também mantém arquivo de licença histórica BSD-2-Clause | Verificar os avisos incluídos no JAR e os componentes nativos; distribuir os textos/notices correspondentes ao artefato exato. |
| JavaFX 21.0.7 | GPL v2 com Classpath Exception | A exceção de classpath é relevante para aplicações que usam JavaFX; preservar textos e avisos do componente redistribuído. Validar o conteúdo do pacote efetivo. |
| Ikonli JavaFX 12.3.1 | Apache License 2.0, a confirmar no POM do artefato exato | Incluir avisos de terceiros se aplicáveis. |
| Ikonli FontAwesome5 Pack 12.3.1 | Código/empacotamento Ikonli separado das licenças dos ativos Font Awesome | Font Awesome Free identifica ícones como CC BY 4.0, fontes como SIL OFL 1.1 e código como MIT. Confirmar quais ativos e avisos estão no JAR e preservar atribuição/licenças aplicáveis. |
| Apache POI OOXML 5.4.1 | Apache License 2.0 | Distribuição exige preservação de licença/avisos de copyright; revisar `NOTICE` e transitivas. |
| Apache PDFBox 3.0.5 | Apache License 2.0 | Preservar licença/avisos e revisar `NOTICE` e transitivas. |
| JUnit Jupiter 5.10.2 | Eclipse Public License 2.0 | Escopo `test`; não deve ser copiado pelo comando de empacotamento com `includeScope=runtime`. Manter separado no relatório de build. |
| JUnit 4.13.1 | Eclipse Public License 1.0, a confirmar no POM exato | Escopo `test`; não deve ser copiado pelo comando de empacotamento com `includeScope=runtime`. |
| Runtime Java 22 criado por `jlink` | Depende da distribuição do JDK usada para empacotar; o script não fixa um fornecedor | Guardar fornecedor/versão e incluir os avisos de licença exigidos pelo runtime efetivamente redistribuído. Não inferir a licença apenas pelo JDK usado no CI. |
| `modelos/S-89_T.pdf` e modelo Word S-140 escolhido pelo usuário | Não determinada por esta auditoria | São ativos separados das dependências Maven. Confirmar direito de redistribuição/uso e eventuais avisos antes de publicar o instalador ou modelos. |

### Situação dos avisos no repositório

Na listagem da raiz inspecionada, não foi identificado um arquivo `LICENSE` ou `THIRD-PARTY-NOTICES` do aplicativo. O script de empacotamento copia JARs para a pasta `app`, mas não foi observada etapa explícita para copiar os textos de licença e avisos de terceiros para o instalador.

Isso é uma **lacuna de conformidade a corrigir antes da distribuição**, não uma conclusão de que todas as licenças sejam incompatíveis. Recomenda-se gerar um inventário completo e acrescentar um arquivo de avisos de terceiros ao pacote, contendo nomes, versões, licença e links/textos exigidos, após revisão dos artefatos reais.

## 6. Compatibilidade e correções

Nenhuma dependência foi atualizada nesta PR. A razão é a ausência da árvore resolvida, de um scanner executado e de testes locais, além de os avisos Jackson encontrados dependerem de formas de desserialização específicas que não aparecem no serviço inspecionado.

Ordem sugerida:

1. Executar a árvore Maven completa e runtime; guardar os resultados.
2. Executar OWASP Dependency-Check (ou scanner equivalente) com base atualizada e registrar severidade, identificador, versão afetada, versão corrigida e caminho transitivo.
3. Avaliar primeiro Jackson, porque sua versão direta aparece em várias faixas afetadas. Se for atualizada, alinhar os módulos Jackson e executar testes de leitura/validação de JSON.
4. Confirmar a versão do Log4j API; se afetada, avaliar override mínimo para uma versão corrigida e validar que POI/XMLBeans continuam funcionando.
5. Executar testes completos antes de qualquer merge; gerar o instalador somente se o pacote runtime for alterado e houver ambiente Windows adequado.
6. Gerar e revisar o relatório de licenças de todas as dependências runtime e build/test; preparar `THIRD-PARTY-NOTICES` e incluir os avisos exigidos no pacote final.
7. Guardar um SBOM (preferencialmente CycloneDX) e os relatórios de scanner como evidências vinculadas ao SHA do release.

## 7. Fontes externas consultadas

Avisos de segurança oficiais:

- [Jackson — CVE-2026-110879 / GHSA-m8xf-2cr7-77ph](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-m8xf-2cr7-77ph)
- [Jackson — GHSA-wv8q-qhhj-9h54](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-wv8q-qhhj-9h54)
- [Jackson — GHSA-cxp5-3px4-pw24](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-cxp5-3px4-pw24)
- [Jackson — GHSA-q4xh-88c3-wmh7](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-q4xh-88c3-wmh7)
- [Jackson — GHSA-wjgm-6hv5-3cvf](https://github.com/FasterXML/jackson-databind/security/advisories/GHSA-wjgm-6hv5-3cvf)
- [Apache Log4j — Security advisories](https://logging.apache.org/security.html)

Licenças e metadados:

- [Maven Central — SQLite JDBC 3.50.3.0](https://central.sonatype.com/artifact/org.xerial/sqlite-jdbc/3.50.3.0)
- [Maven Central — Apache POI OOXML 5.4.1](https://central.sonatype.com/artifact/org.apache.poi/poi-ooxml/5.4.1)
- [Maven Central — PDFBox 3.0.5](https://central.sonatype.com/artifact/org.apache.pdfbox/pdfbox/3.0.5)
- [Maven Central — JUnit Jupiter 5.10.2](https://central.sonatype.com/artifact/org.junit.jupiter/junit-jupiter/5.10.2)
- [OpenJFX — projeto e licença GPL v2 com Classpath Exception](https://github.com/openjdk/jfx)
- [Font Awesome — licenças de ícones, fontes e código](https://github.com/FortAwesome/Font-Awesome)

## 8. Resultados, pendências e decisão

| Item | Status nesta auditoria |
|---|---|
| Inspeção de `pom.xml` e script de empacotamento | Concluída por leitura estática |
| Inventário de dependências diretas | Concluído para versões declaradas |
| Árvore transitiva efetivamente resolvida | **Pendente — Maven não executado** |
| Pesquisa externa de advisories | Concluída parcialmente; avisos Jackson identificados |
| Scanner automatizado de vulnerabilidades | **Pendente — não executado** |
| Verificação integral de licenças transitivas | **Pendente — não executada** |
| Verificação de textos de licença no instalador | **Pendente — pacote não gerado/inspecionado nesta tarefa** |
| Testes Maven `clean test` e `clean verify` | **Pendente — não executados** |
| Atualizações de dependências | Nenhuma realizada |
| Geração do instalador | Não executada |

**Decisão recomendada:** não declarar a auditoria integralmente aprovada nem o lançamento liberado com base apenas neste relatório. O uso atual do Jackson não aparenta acionar os caminhos específicos descritos nos avisos listados, mas a versão declarada está em faixas afetadas. A versão transitiva do Log4j API e os resultados automatizados ainda precisam ser confirmados. A distribuição também precisa de um inventário completo de licenças e avisos do runtime e de todas as dependências empacotadas.

Esta PR registra achados e pendências verificáveis, sem inventar resultados de scanners ou testes. A decisão final de distribuição depende da execução das ferramentas e da revisão dos relatórios gerados para o SHA candidato.
