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

As habilidades não são inferidas automaticamente a partir de Publicador, Batizado, Servo ministerial ou Ancião. Após a aplicação da pilha completa, pessoas que ainda não tiverem habilidades cadastradas não serão elegíveis para Presidência, Joias espirituais, Tesouros, Necessidades Locais e Discurso até que suas habilidades sejam revisadas no formulário.

Antes de usar a geração de programação, abra os cadastros das pessoas e marque as habilidades que elas já podem exercer. A categoria continua sendo mantida e continua sujeita às regras atuais.

## Discurso e dados legados

A importação da apostila passa a inferir o tipo comum `DISCURSO` para as partes numeradas 7 e 8. Os valores `PARTE_1`, `PARTE_2` e `PARTE_3` permanecem no enum nesta etapa para compatibilidade com registros antigos. Não foi executada migração destrutiva de tipos no banco. A normalização física e a resolução automática de múltiplas correspondências exigem validação adicional com dados de exemplo.

## Validação

O teste `HabilidadesElegibilidadeIntegracaoTest` usa um banco SQLite temporário e cobre persistência, recarga, elegibilidade e preservação de restrições. Não usa o banco real do usuário. Os testes Maven locais não foram executados nesta sessão; consultar o status da CI e executar a suíte completa antes de qualquer merge.
