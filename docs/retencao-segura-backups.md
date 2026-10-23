# Retenção segura de backups

## Política

- A retenção considera somente backups automáticos com metadados explícitos, legíveis e compatíveis com o arquivo.
- São mantidos os 30 backups automáticos mais recentes, usando o instante de criação registrado nos metadados.
- Empates na data de corte são preservados, mesmo que isso deixe mais de 30 backups automáticos.
- Backups manuais nunca entram na contagem de retenção.
- Arquivos legados sem metadados, arquivos desconhecidos, links simbólicos e arquivos com classificação duvidosa não são excluídos.
- Uma falha na leitura/classificação cancela a rodada antes de qualquer exclusão. Uma falha de exclusão interrompe a limpeza para que os arquivos seguintes não sejam tentados.
- Falhas de registro dos metadados ou da retenção não desfazem um backup já criado e validado nem alteram o banco principal.

## Classificação dos backups automáticos

Após criar e validar o arquivo SQLite, o fluxo automático grava um marcador lateral com versão do formato, tipo, nome exato do arquivo, instante de criação, tamanho e SHA-256. O marcador é publicado por movimentação atômica quando o sistema de arquivos permite.

A retenção só considera o arquivo automático quando o nome segue o padrão atual e todos os metadados correspondem ao arquivo. Se o conteúdo for substituído, o marcador estiver malformado ou não puder ser lido com segurança, a rodada é cancelada e os arquivos são preservados.

O backup manual invalida o marcador de um destino que já tenha sido usado por um backup automático antes de gravar nesse destino. Essa operação e a retenção compartilham o bloqueio de arquivos usado pela criação/restauração, evitando que a limpeza concorra com essas operações.

## Arquivos de versões anteriores

Backups automáticos criados antes da introdução dos marcadores não são classificados automaticamente com base apenas no nome ou na data do arquivo. Eles permanecem preservados, mesmo que tenham um nome parecido com o padrão atual. Consequentemente, após a atualização, a pasta pode conter mais de 30 arquivos no total: o limite se aplica apenas aos backups automáticos novos que puderem ser identificados com segurança.

## Escopo

A política não altera o esquema SQLite, não executa migrações, não modifica o conteúdo do banco ativo e não exclui backups manuais. A retenção é uma manutenção posterior à criação e validação do backup.
