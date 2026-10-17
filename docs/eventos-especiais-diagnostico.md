# Eventos especiais — diagnóstico e proposta inicial

## Objetivo e escopo

Esta etapa documenta a análise inicial para adicionar eventos especiais à programação semanal sem alterar o comportamento atual do gerador. É uma especificação de planejamento: **não implementa regras executáveis, não altera o esquema do banco e não modifica programações existentes**.

A implementação de eventos especiais deve permanecer independente do fluxo de importação de apostilas RTF/JSON.

## Decisões funcionais já acordadas

- **Visita do superintendente:** a reunião habitual de quarta-feira passa para terça-feira naquela semana. Na programação, cancelar/substituir o Estudo Bíblico de Congregação e inserir no lugar um discurso da visita com duração de 30 minutos e campo para cadastrar o tema. As demais partes, designações e ajustes manuais devem ser preservados, salvo alterações estritamente necessárias para a mudança do dia.
- **Assembleia:** cancelar a reunião de quarta-feira da semana do evento, preservando a programação cadastrada.
- **Congresso:** cancelar a reunião de quarta-feira da semana do evento, preservando a programação cadastrada.
- **Celebração:** se ocorrer de segunda a sexta-feira, a celebração substitui a reunião habitual de quarta-feira daquela semana, inclusive quando a própria celebração ocorre na quarta-feira. Se ocorrer no sábado ou domingo, a reunião de quarta-feira continua normalmente.
- **Distribuição temporal informada:** assembleia, congresso e celebração não ocorrem na mesma semana que a visita do superintendente, conforme o cenário descrito pelo usuário. Ainda assim, manter tratamento seguro para sobreposições futuras ou cadastros conflitantes.
- **Cancelamento:** registrar explicitamente que a reunião foi cancelada, sem apagar a programação já cadastrada.
- **Programação confirmada:** considera-se confirmada quando a escala daquela data está salva no banco, com status SALVA. Antes de aplicar alterações de eventos a essa data, exibir a prévia das diferenças e exigir confirmação explícita.
- **Evento retroativo:** apresentar previamente as datas e alterações propostas; nunca modificar o passado silenciosamente, independentemente do status da escala.
- **Conflitos sem regra de precedência definida:** alertar e exigir decisão explícita do usuário. Não inventar precedência automática. A escolha manual vale somente para aquela aplicação; não deve ser persistida como regra para prévias futuras.
- **Eventos com vários dias:** representar um intervalo inclusivo, mas avaliar cada data separadamente, permitindo exceções por data.
- **Visita e cancelamento na mesma data:** o cancelamento prevalece.

### Matriz resumida

| Situação | Comportamento esperado |
| --- | --- |
| Nenhum evento aplicável | Manter o comportamento habitual |
| Visita isolada | Mover a reunião de quarta para terça naquela semana; substituir o Estudo Bíblico de Congregação por discurso da visita de 30 minutos, com tema editável; preservar as demais partes |
| Assembleia | Cancelar a reunião de quarta-feira da semana do evento, preservando a programação |
| Congresso | Cancelar a reunião de quarta-feira da semana do evento, preservando a programação |
| Celebração de segunda a sexta-feira | Substituir a reunião de quarta-feira daquela semana pela celebração, inclusive quando a celebração ocorrer na quarta-feira |
| Celebração no sábado ou domingo | Manter a reunião de quarta-feira normalmente |
| Cancelamento isolado | Marcar cancelada e preservar os dados existentes |
| Visita e cancelamento na mesma data | Considerar cancelamento |
| Conflito entre tipos sem precedência definida | Alertar e solicitar decisão; a escolha vale somente para aquela aplicação |
| Evento de vários dias | Resolver separadamente para cada data |
| Evento retroativo | Mostrar prévia e exigir confirmação antes de aplicar |
| Escala da data com status SALVA | Mostrar diferenças e pedir confirmação antes de gravar |
| Escala da data com status GERADA | Mostrar prévia das alterações propostas; a aplicação seguirá o fluxo de confirmação definido para o evento |

## Diagnóstico técnico do código atual

### Programação semanal

- O método ProgramacaoSemanaService.obterOuCriar(LocalDate) não é uma consulta pura. Se a programação existe, chama sincronizarPartesFixas() e atribuirNumeroPadraoLeitura(), podendo persistir alterações.
- O método é usado tanto na tela de programação quanto no fluxo de geração de escalas. A resolução de eventos e sua prévia não devem chamar esse método como se fosse uma leitura sem efeitos colaterais.
- A programação é persistida nas tabelas programacao_semana e programacao_parte. A data da semana é única em programacao_semana; as partes têm restrições únicas por programação para parte e ordem.
- ProgramacaoSemanaDAO.salvar(...) grava a semana e suas partes usando a mesma conexão e transação. Já vários métodos de atualização individual de campos de programacao_parte abrem conexões próprias; uma futura aplicação de evento que precise alterar vários campos deve definir uma fronteira transacional apropriada.

### Escalas e histórico

- O modelo Escala possui estados GERADA e SALVA. A tabela escala mantém o status e as datas de geração/salvamento.
- A confirmação para fins de eventos será determinada pelo status SALVA da escala correspondente à data, conforme decisão funcional.
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
3. **Aplicação explícita:** após a prévia, apresentar diferenças e pedir confirmação quando houver mudanças efetivas. Para escalas com status SALVA e para eventos retroativos, a confirmação explícita é obrigatória. Só então persistir, com validações repetidas e transação adequada.

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

1. **Conflitos:** além da combinação visita + cancelamento, quais combinações têm precedência definida? As demais devem continuar exigindo decisão explícita. Foi informado que assembleia, congresso e celebração não ocorrem na mesma semana que a visita, mas o sistema ainda deve tratar cadastros sobrepostos com segurança.

A regra da visita do superintendente, a regra operacional da celebração e o caráter temporário das escolhas para conflitos estão definidos para esta proposta. Antes da implementação do serviço de resolução/aplicação, ainda é necessário definir como tratar combinações de eventos além da combinação visita + cancelamento, sem inventar precedência.

## Sequência sugerida de PRs

- **A1 — documentação e diagnóstico:** esta proposta, sem mudança de comportamento.
- **B1 — modelo e resolução pura:** tipos explícitos, validações e testes unitários para intervalos, combinações e conflitos acordados; sem gravar eventos ou aplicar alterações na programação.
- **C1 — persistência:** migração aditiva/idempotente e operações de eventos com testes em banco isolado.
- **D1 — interface e prévia:** cadastro/edição e apresentação por data das alterações e alertas.
- **E1 — aplicação confirmada:** aplicar alterações somente após confirmação, com proteção para programações existentes e transação.
- **F1 — regressão e documentação:** validar o fluxo completo, banco existente, histórico, ajustes S-140 e exports.

As etapas dependentes devem permanecer separadas e abertas para validação incremental. Não fazer merge sem autorização explícita e não misturar essas mudanças com as PRs da importação RTF/JSON.
