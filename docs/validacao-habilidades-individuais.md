# Habilidades individuais — validação integrada

Esta etapa integra as mudanças empilhadas nas PRs #101–#107 e registra os critérios para validação local.

## Fluxo integrado coberto

- Catálogo de habilidades é criado e populado idempotentemente na inicialização.
- O cadastro de pessoa salva e recupera os códigos de habilidades.
- O gerador consulta a habilidade exigida pelo tipo de parte.
- Uma pessoa sem a habilidade exigida não é elegível para as partes mapeadas.
- Habilidade não substitui as restrições já existentes de sexo, privilégio, nível de leitura, participação, disponibilidade ou conflitos.
- Partes sem mapeamento novo continuam usando as regras atuais.

## Importante para cadastros existentes

As habilidades não são inferidas automaticamente a partir de Publicador, Batizado, Servo ministerial ou Ancião. A migração adiciona `habilidades_configuradas` com valor padrão falso para preservar a operação durante a revisão gradual. Após a aplicação da pilha completa, cadastros antigos terão uma flag de revisão desligada e continuarão temporariamente usando as regras anteriores até que alguém salve suas habilidades no formulário. Ao salvar, a pessoa passa a usar as habilidades marcadas; se nenhuma for marcada, ela não será elegível para as partes que exigem essas habilidades. Pessoas novas já entram no modo de habilidades configuradas ao salvar o formulário.

Antes de usar a geração de programação, abra os cadastros das pessoas e marque as habilidades que elas já podem exercer. A categoria continua sendo mantida e continua sujeita às regras atuais.

## Discurso e dados legados

A importação da apostila passa a inferir o tipo comum `DISCURSO` para as partes numeradas 7 e 8 e aceita tipos legados como candidatos compatíveis. A inicialização normaliza `parte.tipo` de `PARTE_1`, `PARTE_2` e `PARTE_3` para `DISCURSO`, mantendo os IDs, nomes, ordens e vínculos históricos das linhas. Os valores antigos permanecem no enum para leitura de dados legados fora do catálogo. Quando várias partes antigas correspondem ao mesmo discurso, o mapeador retorna `AMBIGUO` em vez de escolher uma arbitrariamente; a seleção manual continua necessária.

## Validação

O teste `HabilidadesElegibilidadeIntegracaoTest` usa um banco SQLite temporário e cobre persistência, recarga, elegibilidade e preservação de restrições. Não usa o banco real do usuário. Os testes Maven locais não foram executados nesta sessão; consultar o status da CI e executar a suíte completa antes de qualquer merge.
