# E1 — contrato de aplicação transacional de eventos especiais

## Objetivo

Definir a fronteira segura para a futura aplicação de eventos especiais à programação semanal. Esta etapa é intencionalmente preparatória: não altera dados em tempo de execução, não executa migrações e não apresenta um botão de aplicação.

A aplicação real só deve ser habilitada depois da validação das etapas B1, C1 e D1 e da implementação dos testes transacionais descritos abaixo.

## Regras obrigatórias

1. **Prévia separada da aplicação.** A prévia usa o resolvedor puro e não pode gravar dados, sincronizar partes fixas, preencher valores padrão ou criar programação.
2. **Confirmação explícita.** Toda aplicação deve receber uma confirmação explícita. Para eventos retroativos, a confirmação é obrigatória independentemente do status da escala.
3. **Conflitos sem decisão implícita.** Se o resolvedor retornar `EXIGE_DECISAO`, a aplicação deve recusar a operação até receber uma alternativa válida escolhida pelo usuário. A escolha vale somente para aquela aplicação.
4. **Transação única.** Leitura dos dados que serão alterados, validações finais, gravação dos efeitos e registro de aplicação devem ocorrer sob uma única conexão/transação. Qualquer falha deve provocar rollback total.
5. **Idempotência.** Repetir a mesma solicitação não pode duplicar efeitos nem aplicar novamente mudanças já concluídas. A identidade da aplicação deve permitir detectar repetição e distinguir uma nova confirmação explícita.
6. **Sem exclusão/recriação de programação.** Nunca excluir ou recriar `programacao_semana` para mover uma reunião: `horario_manual_s140` referencia a data da programação e possui comportamento de cascata na exclusão.
7. **Sem efeitos colaterais em escalas e histórico.** Não chamar serviços de geração/salvamento de escalas nem serviços que alterem `historico_designacoes`. Não editar nem limpar `horario_manual_s140`.
8. **Não desfazer por exclusão do evento.** Editar ou excluir o cadastro do evento não pode reverter a aplicação já confirmada. Reversões futuras exigem fluxo separado, prévia e confirmação próprias.
9. **Não criar programação silenciosamente.** Se a semana necessária não existir, a aplicação deve parar e pedir uma ação explícita; não chamar `ProgramacaoSemanaService.obterOuCriar`, pois esse método pode criar programação e sincronizar partes fixas.
10. **Falha segura.** Se não for possível determinar com segurança a parte a substituir, a data de destino, o estado da escala ou o efeito do evento, a operação deve ser recusada sem gravação parcial.

## Efeitos por tipo de evento

- **Visita do superintendente:** mover a reunião de quarta para terça e substituir o Estudo Bíblico de Congregação por discurso de 30 minutos com tema editável. Preservar as demais partes e todos os ajustes manuais. Antes de mover a data, validar colisão com programação já existente na terça; não sobrescrever uma semana existente.
- **Assembleia e congresso:** registrar explicitamente o cancelamento da reunião daquela semana, preservando a programação e as designações já armazenadas.
- **Celebração:** se ocorrer de segunda a sexta, registrar a substituição da reunião daquela semana; se ocorrer sábado/domingo, manter a reunião normal.
- **Cancelamento genérico:** registrar explicitamente a reunião cancelada, sem apagar a programação.
- **Visita e cancelamento na mesma data:** o cancelamento prevalece, conforme a regra já definida.
- **Conflitos restantes:** não escolher automaticamente; exigir alternativa explícita.

## Arquitetura a implementar na etapa executável

A implementação deve manter a lógica de decisão separada das gravações:

1. Um comando imutável de aplicação contendo a semana, a decisão confirmada e os dados necessários para validar a prévia apresentada.
2. Um serviço de aplicação que valide o comando, abra uma única conexão e inicie uma transação.
3. DAOs com variantes que aceitem a conexão/transação existente; não usar métodos que abram conexões independentes no meio da operação.
4. Um registro persistido da aplicação, incluindo evento(s), semana afetada, ação efetivamente confirmada, datas anterior/nova quando aplicável e instante da aplicação.
5. Uma representação explícita do cancelamento/substituição que possa ser consultada pelas telas e pelos fluxos de geração sem apagar os dados subjacentes.
6. Um resultado que diferencie aplicação concluída, já aplicada, confirmação ausente, conflito não resolvido, dados incompatíveis e falha revertida.

A alteração da data de uma programação existente requer atenção especial: validar referências por data, restrições de unicidade e ajustes S-140 antes de escolher a estratégia. Não implementar um `DELETE + INSERT` como atalho. Se não houver uma estratégia que preserve todas as referências e os dados, recusar a operação até o modelo ser adaptado com migração aditiva e testes.

## Testes de aceitação obrigatórios

- Nenhuma escrita durante a prévia ou ao abrir/editar/excluir o cadastro de um evento.
- Aplicação bem-sucedida de cada tipo de evento e verificação dos efeitos esperados.
- Conflito sem decisão, confirmação ausente e evento retroativo sem confirmação recusados sem alterar nenhuma tabela.
- Falha simulada em cada ponto de gravação provoca rollback de todos os efeitos e do registro de aplicação.
- Aplicação repetida é idempotente.
- A programação, as partes, as designações, o histórico e os ajustes S-140 são comparados antes/depois; somente as alterações explicitamente autorizadas podem diferir.
- Visita com programação já existente na terça não sobrescreve nem remove dados.
- Cancelar/editar/excluir o cadastro do evento após aplicação não apaga nem reverte a programação.
- Testes executados com banco temporário isolado, sem acesso ao banco real.

## Limites desta etapa preparatória

Este documento não implementa a aplicação e não modifica o comportamento do programa. O serviço executável e as migrações só devem entrar após a revisão do contrato, a validação da persistência C1 e a confirmação da estratégia de preservação das referências de programação/S-140.
