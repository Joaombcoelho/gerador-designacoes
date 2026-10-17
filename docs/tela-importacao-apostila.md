# Tela de conferência da importação de apostilas

A tela fica disponível no menu lateral em **Importar apostila**.

## Fluxo disponível

1. Selecionar um arquivo JSON produzido pelo extrator.
2. Conferir erros e avisos da validação.
3. Conferir, por semana, a leitura bíblica, os cânticos e as oito partes.
4. Informar explicitamente a data real da reunião para cada semana.
5. Consultar sugestões de correspondência e selecionar manualmente uma parte existente do catálogo.
6. Gerar a prévia técnica, que verifica se as datas já possuem programação e apresenta erros/avisos.

## Proteções

- A tela não cria nem altera programações.
- A tela não grava o JSON nem as escolhas manuais no banco.
- A verificação de programação existente usa consulta por data.
- A data da apostila não é presumida como data da reunião.
- As escolhas manuais são apenas visuais nesta etapa; ainda não são enviadas ao serviço de confirmação/importação.

## Limites desta PR

Esta PR implementa a interface de conferência e a sua navegação no menu. Não implementa a confirmação final, gravação transacional, criação de partes, nem importação de cânticos para uma programação. Essas operações devem ser desenvolvidas somente depois de as PRs de validação, mapeamento, prévia e revisão manual terem sido aprovadas.
