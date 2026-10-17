# Mapeamento de partes da apostila

Esta etapa adiciona uma camada somente de consulta entre as partes validadas do JSON e o catálogo de partes já cadastrado no Gerador de Designações.

## Comportamento

- Usa a seção identificada no JSON para limitar os candidatos aos registros da mesma seção.
- Infere alguns valores de `TipoParte` somente quando há um subtítulo ou uma descrição reconhecível.
- Normaliza acentos e diferenças de maiúsculas/minúsculas para comparar os textos.
- Retorna candidatos e uma situação explícita: correspondência única, ambígua, não encontrada ou requer conferência.
- Considera automática apenas uma correspondência com tipo inferido e exatamente um candidato.

## Limites intencionais

- Não cria nem altera registros do catálogo.
- Não chama `ProgramacaoSemanaService.obterOuCriar`, pois esse método pode criar ou sincronizar uma programação.
- Não grava dados no SQLite e não modifica programações existentes.
- Não infere o tipo apenas pelo número da parte ou pela seção quando o texto não é suficiente.
- Cânticos, leitura bíblica e associação da semana à data efetiva da reunião ainda não são importados nesta etapa.
- A correspondência única é uma sugestão para a prévia, não uma autorização para gravar sem confirmação.

## Próxima etapa

Integrar a validação e as sugestões numa prévia revisável, definir explicitamente como a data da reunião será selecionada e, só depois da confirmação do usuário, planejar a gravação transacional que preserva as programações e os ajustes manuais existentes.
