# Revisão manual do mapeamento de apostilas

Esta etapa adiciona uma operação de domínio para aplicar uma escolha manual do catálogo a uma sugestão de mapeamento.

## Regras

- A escolha precisa apontar para uma parte já existente no catálogo (ID não nulo).
- A seção da parte escolhida deve ser igual à seção identificada no JSON.
- Se o extrator reconheceu um tipo, a escolha precisa respeitar esse tipo.
- Se o tipo não foi reconhecido, a seleção manual pode resolver a pendência, respeitando a seção.
- A resposta passa a representar uma correspondência única e contém somente a parte escolhida.

## Segurança e limites

O serviço é puro: não acessa DAO, não grava a escolha, não cria partes e não altera programações. A futura interface deve manter as escolhas do usuário em memória durante a revisão e reaplicar esta validação antes de permitir a confirmação. A persistência da importação continua sendo uma etapa posterior e separada.

Esta PR não inclui a tela JavaFX nem gravação no SQLite.
