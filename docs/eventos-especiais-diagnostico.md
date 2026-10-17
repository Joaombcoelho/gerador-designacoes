# Eventos especiais — diagnóstico e proposta inicial

## Objetivo e escopo

Esta etapa documenta a análise inicial para adicionar eventos especiais à programação semanal sem alterar o comportamento atual do gerador. É uma especificação de planejamento: **não implementa regras executáveis, não altera o esquema do banco e não modifica programações existentes**.

A implementação de eventos especiais deve permanecer independente do fluxo de importação de apostilas RTF/JSON.

## Decisões funcionais já acordadas

- **Visita do superintendente:** ajustar a programação habitual, em vez de substituir toda a reunião por um modelo especial. Ainda é necessário definir exatamente quais partes e campos são alterados.
- **Cancelamento:** registrar explicitamente que a reunião foi cancelada, sem apagar a programação já cadastrada.
- **Programação já confirmada:** exibir uma prévia das diferenças e pedir confirmação antes de gravar qualquer mudança efetiva.
- **Evento retroativo:** apresentar previamente as datas e alterações propostas; nunca modificar o passado silenciosamente.
- **Conflitos sem regra de precedência definida:** alertar e exigir decisão explícita do usuário. Não inventar precedência automática.
- **Eventos com vários dias:** representar um intervalo inclusivo, mas avaliar cada data separadamente, permitindo exceções por data.
- **Visita e cancelamento na mesma data:** o cancelamento prevalece.

### Matriz resumida

| Situação | Comportamento esperado |
| --- | --- |
| Nenhum evento aplicável | Manter o comportamento habitual |
| Visita isolada | Propor os ajustes específicos da visita |
| Cancelamento isolado | Marcar cancelada e preservar os dados existentes |
| Visita e cancelamento na mesma data | Considerar cancelamento |
| Conflito entre tipos sem precedência definida | Alertar e solicitar decisão |
| Evento de vários dias | Resolver separadamente para cada data |
| Evento retroativo | Mostrar prévia e exigir confirmação antes de aplicar |
| Programação já confirmada | Mostrar diferenças e pedir confirmação antes de gravar |

## Diagnóstico técnico do código atual

### Programação semanal

- O método ProgramacaoSemanaService.obterOuCriar(LocalDate) não é uma consulta pura. Se a programação existe, chama sincronizarPartesFixas() e atribuirNumeroPadraoLeitura(), podendo persistir alterações.
- O método é usado tanto na tela de programação quanto no fluxo de geração de escalas. A resolução de eventos e sua prévia não devem chamar esse método como se fosse uma leitura sem efeitos colaterais.
- A programação é persistida nas tabelas programacao_semana e programacao_parte. A data da semana é única em programacao_semana; as partes têm restrições únicas por programação para parte e ordem.
- ProgramacaoSemanaDAO.salvar(...) grava a semana e suas partes usando a mesma conexão e transação. Já vários métodos de atualização individual de campos de programacao_parte abrem conexões próprias; uma futura aplicação de evento que precise alterar vários campos deve definir uma fronteira transacional apropriada.

### Escalas e histórico

- O modelo Escala possui estados GERADA e SALVA. A tabela escala mantém o status e as datas de geração/salvamento.
- A programação semanal (ProgramacaoSemana) não possui, no modelo/tabela examinados, um estado próprio de confirmação. Portanto, ainda é necessário decidir se “programação confirmada” significa uma escala salva, uma programação existente/editada ou outro conceito funcional.
- PersistenciaEscalasService.salvar(...) persiste escalas e, quando solicitado, atualiza o histórico na mesma transação SQLite.
- A persistência da escala existente pode substituir suas designações; por isso, a aplicação de eventos não deve acionar geração/salvamento de escalas como efeito indireto de cadastrar, editar ou consultar eventos.
- O histórico de participações é armazenado separadamente em historico_designacoes. A funcionalidade de eventos não deve apagá-lo nem reescrevê-lo automaticamente.

### Ajustes manuais do S-140

- Os horários ajustados manualmente ficam em horario_manual_s140, separados dos horários calculados.
- A tabela usa a chave (data, chave_item) e possui chave estrangeira para programacao_semana(data) com ON DELETE CASCADE.
- A aplicação de eventos deve preservar esses ajustes. Remover ou editar um evento não pode apagar nem “reverter” ajustes manuais; qualquer conflito entre um ajuste e um evento deve ser mostrado na prévia e resolvido explicitamente quando necessário.

### Inicialização e migração do banco

- DatabaseInitializer.initialize() cria tabelas ausentes e executa migrações para colunas e estruturas existentes.
- O desenho futuro deve usar migração aditiva e idempotente, compatível com bancos já utilizados. Não recriar tabelas existentes nem introduzir alterações destrutivas para implementar eventos.
- A tabela de eventos deve ter persistência própria, com datas inicial/final inclusivas e dados estruturados por tipo. Não usar o texto de descrição do evento como mecanismo para inferir regras.

## Arquitetura proposta para a implementação

Separar três responsabilidades:

1. **Evento persistido:** identificador, tipo explícito, intervalo de datas, descrição opcional e dados específicos validados para aquele tipo.
2. **Resolução por data:** serviço puro que recebe os eventos aplicáveis e a programação lida sem efeitos colaterais, produzindo resultado e alertas para aquela data. Intervalos são avaliados data a data.
3. **Aplicação explícita:** após a prévia, apresentar diferenças e pedir confirmação quando houver mudanças efetivas. Só então persistir, com validações repetidas e transação adequada.

O cancelamento deve ser um estado/resultado explícito da resolução, não a exclusão da programação. A resolução deve ser determinística para os casos com regras acordadas; conflitos não definidos devem produzir alertas e solicitar decisão, sem precedência implícita.

## Proteções obrigatórias

- Não reescrever ou apagar histórico de designações.
- Preservar temas, durações, números oficiais, cânticos, conselheiro e demais campos existentes quando não forem explicitamente alterados por uma regra aprovada.
- Preservar os ajustes manuais de horário do S-140.
- Não sincronizar partes fixas nem preencher valores padrão apenas por consultar uma data durante a prévia.
- Não apagar programação quando um evento for cancelado, editado ou removido.
- Não regenerar nem salvar escalas automaticamente ao cadastrar, consultar, editar ou remover um evento.
- Aplicar alterações relacionadas de forma atômica e reverter a operação completa em caso de falha.
- Não mudar formatos de exportação S-89/S-140 sem necessidade demonstrada e aprovada.

## Decisões ainda necessárias antes das regras executáveis

1. **Visita do superintendente:** quais partes, durações, temas, cânticos, papéis ou campos específicos devem mudar? Quais valores vêm de regras fixas e quais devem ser escolhidos pelo usuário?
2. **Tipos da primeira versão:** além de visita do superintendente e cancelamento, quais outros tipos de evento devem ser suportados desde o início?
3. **Conflitos:** além da combinação visita + cancelamento, quais combinações têm precedência definida? As demais devem continuar exigindo decisão explícita.
4. **Persistência da decisão:** quando o usuário resolver um conflito, essa decisão deve valer apenas para aquela aplicação ou ficar registrada como resolução do evento/data para futuras prévias?
5. **Significado de programação confirmada:** deve ser associado ao status SALVA da escala, à existência de programação semanal ou a um estado de confirmação separado? O código atual não oferece um estado próprio de confirmação em ProgramacaoSemana.

Essas decisões devem ser fechadas antes da implementação do serviço de resolução/aplicação, para evitar codificar suposições de negócio.

## Sequência sugerida de PRs

- **A1 — documentação e diagnóstico:** esta proposta, sem mudança de comportamento.
- **B1 — modelo e resolução pura:** tipos explícitos, validações e testes unitários para intervalos, combinações e conflitos acordados; sem gravar eventos ou aplicar alterações na programação.
- **C1 — persistência:** migração aditiva/idempotente e operações de eventos com testes em banco isolado.
- **D1 — interface e prévia:** cadastro/edição e apresentação por data das alterações e alertas.
- **E1 — aplicação confirmada:** aplicar alterações somente após confirmação, com proteção para programações existentes e transação.
- **F1 — regressão e documentação:** validar o fluxo completo, banco existente, histórico, ajustes S-140 e exports.

As etapas dependentes devem permanecer separadas e abertas para validação incremental. Não fazer merge sem autorização explícita e não misturar essas mudanças com as PRs da importação RTF/JSON.
