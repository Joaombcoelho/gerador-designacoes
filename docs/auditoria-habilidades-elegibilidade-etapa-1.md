# Auditoria de habilidades e elegibilidade de designações — Etapa 1

**Repositório:** `Joaombcoelho/gerador-designacoes`  
**Base inspecionada:** `main`, commit `df5254377f78f68e74292830085d6b072ad7e95e` (merge da PR #98).  
**Branch:** `audit/habilidades-elegibilidade-etapa-1`  
**Versão-alvo:** 1.2.5  
**Escopo:** diagnóstico estático do modelo atual; nenhuma alteração no comportamento de produção nesta etapa.

## 1. Objetivo e decisões de produto

O objetivo é permitir que cada pessoa tenha habilidades individuais para designações, configuráveis no cadastro, sem remover ou enfraquecer os dados e regras existentes de sexo, privilégio/categoria, nível de leitura, participação por sala, disponibilidade, conflitos e restrições específicas.

Decisões confirmadas pelo responsável pelo produto:

- Manter as categorias/privilégios atuais (Publicador, Batizado, Servo ministerial e Ancião) porque outras partes dependem deles.
- Manter sexo, nível de leitura/experiência e permissões por sala.
- Habilidades individuais devem complementar, não substituir, as regras existentes.
- Uma habilidade não pode anular restrições de elegibilidade ou conflitos atuais.
- Avaliar a unificação de Parte 1, Parte 2 e Parte 3 sob a habilidade/tipo Discurso, mas só após mapear os usos e preservar a programação e o histórico.
- Fazer alterações pequenas em PRs empilhadas, com testes por etapa; não realizar merge. A validação final será executada localmente pelo responsável.
- Não usar o banco real para testes; utilizar fixtures e bancos SQLite temporários.

## 2. Arquivos e responsabilidades encontrados

| Área | Arquivo(s) | Observação |
|---|---|---|
| Modelo da pessoa | `model/Pessoa.java` | Hoje guarda flags específicas (`podeFazerLeitura`, `podeFazerDiscurso`, `podeFazerOracao`, presidente, dirigente, responsável e ajudante), além de sexo, `Privilegio` e `NivelLeitura`. |
| Formulário de pessoa | `view/pessoa/PessoaFormularioView.java` | Os checkboxes atuais são ligados diretamente às flags do modelo. Sexo e privilégio acionam rotinas de atualização de permissões. |
| Persistência da pessoa | `dao/PessoaDAO.java` | Insere/atualiza flags e recupera a pessoa; a mudança de habilidades deve ser persistida sem perder dados existentes. |
| Regras por parte | `model/Parte.java` | Parte possui tipo, privilégio mínimo, sexo permitido, nível mínimo de leitura, participações necessárias, exigência de ajudante, seção, variação e outros metadados. |
| Tipos de parte | `model/TipoParte.java` | Contém `DISCURSO`, `PARTE_1`, `PARTE_2`, `PARTE_3` e tipos próprios de Tesouros, Joias, ministério e estudo. |
| Elegibilidade | `service/RegrasService.java` | Verifica pessoa ativa, participações exigidas, presidente, privilégios, repetição de designações e conflito Tesouros/Joias. Deve continuar sendo o ponto central de composição das regras ou delegar a um componente explícito. |
| Seleção | `service/SeletorPessoaService.java` | Integra as regras de elegibilidade com seleção de pessoas; requer revisão para identificar filtros paralelos ou duplicados. |
| Persistência das partes | `dao/ParteDAO.java`, `database/DatabaseInitializer.java` | O cadastro e as migrações das partes definem tipos e requisitos atuais; não renomear tipos sem inventariar dependências e dados legados. |
| Importação da programação | `service/importacao/MapeadorParteApostilaService.java` | O mapeamento de partes da apostila usa números oficiais e atualmente associa algumas ocorrências a `PARTE_1`, `PARTE_2` etc. A eventual unificação deve preservar o número/ordem da ocorrência. |
| Testes existentes | `PessoaTest`, `PessoaDAOTest`, `PessoaFormularioViewTest`, `RegrasServiceTest`, `RegrasServiceConflitoTipoParteTest`, `RegraLeituraTest`, `RegraTesouroJoiasTest`, `SeletorPessoaServiceTest`, `MapeadorParteApostilaServiceTest`, testes de integração do gerador | Estender os testes existentes e adicionar testes específicos para habilidades, migração e compatibilidade. |

## 3. Constatações da auditoria estática

1. O modelo atual combina atributos pessoais, privilégios e capacidades em flags booleanas específicas. Isso limita o treinamento progressivo por modalidade, pois as flags atuais não cobrem todas as modalidades desejadas (por exemplo, Joias espirituais, Tesouros, Necessidades Locais).
2. O formulário reage a sexo e privilégio para atualizar permissões. Essa automação precisa ser entendida e preservada para regras que dependem desses atributos; não deve continuar sobrescrevendo silenciosamente habilidades que o usuário configurou individualmente.
3. `Parte` já contém requisitos próprios: privilégio mínimo, sexo permitido, nível mínimo de leitura e participações necessárias. A nova habilidade será um requisito adicional, não um substituto desses campos.
4. `RegrasService` contém regras de repetição de designação, presidência e conflito entre Tesouros e Joias. A nova camada deve ser aplicada sem contornar esses bloqueios.
5. `TipoParte` contém tanto `DISCURSO` quanto `PARTE_1`, `PARTE_2` e `PARTE_3`. A busca estática também encontrou referências em `DatabaseInitializer`, no mapeador de importação e em testes. A unificação exige compatibilidade de dados e dos números/ordem da programação, não apenas renomear o enum.
6. O projeto possui migrações SQLite em `DatabaseInitializer`; qualquer esquema de habilidades precisa ser adicionado de modo idempotente e testado contra banco novo e banco existente.
7. A branch principal consultada estava no commit indicado acima. As PRs abertas #99 e #100 são de validação do instalador e auditoria de dependências, respectivamente; esta proposta não as altera.

## 4. Arquitetura proposta para a próxima etapa

Adicionar um modelo de habilidades explícito e independente dos atributos pessoais, com identificadores estáveis. Avaliar uma tabela de catálogo de habilidades e uma tabela de relacionamento pessoa-habilidade; o formato final será decidido após conferir as convenções de DAO, migração e testes.

O vínculo entre parte e habilidade exigida deve ser explícito e compatível com as partes atuais. Quando uma parte exigir múltiplas habilidades, a regra (todas ou alguma) deverá ser declarada sem ambiguidade; não inferir essa semântica automaticamente.

O comportamento para pessoas existentes sem habilidades cadastradas deve ser definido por migração explícita. Não interpretar ausência de configuração como autorização irrestrita e não converter categoria em todas as habilidades. A conversão automática só poderá usar permissões atuais quando a equivalência for segura e demonstrável; os demais casos devem ficar para revisão manual.

## 5. Sequência de PRs planejada

1. **Etapa 1 — Auditoria (esta PR):** registrar o mapa de regras, riscos e plano.
2. **Etapa 2 — Modelo e persistência:** catálogo/relacionamento de habilidades, migração idempotente e testes de DAO/compatibilidade em SQLite temporário; sem mudar a seleção de produção.
3. **Etapa 3 — Regras de elegibilidade:** aplicar habilidades como requisito adicional, preservando privilégio, sexo, nível de leitura, sala, conflitos e restrições; testes unitários de regressão.
4. **Etapa 4 — Formulário de pessoa:** checkboxes individuais com carregamento/salvamento e testes de interface; não deixar regras automáticas de sexo/privilégio sobrescreverem escolhas individuais.
5. **Etapa 5 — Tipos de discurso:** após inventário completo, normalizar Parte 1/2/3 somente se for seguro, com migração e testes de programação/importação/histórico.
6. **Etapa 6 — Integração e regressão:** testes de integração, geração e persistência, além da suíte completa executada pelo responsável localmente.

Cada etapa de código terá seu próprio PR e será baseada na PR anterior (pilha). Nenhuma PR será mesclada automaticamente.

## 6. Validação e limitações

- Esta etapa foi uma inspeção estática do código disponível no GitHub.
- Nenhum teste Maven foi executado nesta sessão; nenhum resultado de teste é declarado.
- Nenhuma alteração foi feita no código de produção ou no banco de dados.
- As etapas seguintes devem acrescentar testes automatizados e registrar claramente quais comandos foram ou não executados.
