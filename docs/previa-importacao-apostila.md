# Prévia da importação de apostilas

Esta etapa reúne a validação do JSON e o mapeamento das partes em uma prévia de domínio, sem interface gráfica e sem persistência.

## Data efetiva da reunião

O intervalo `data_inicio` / `data_fim` do JSON descreve a semana da apostila. Ele não é convertido automaticamente em data da reunião. O chamador precisa informar explicitamente um mapa da data inicial da semana da apostila para a data real da reunião.

## Proteções e bloqueios

- Não chama serviços que criam/sincronizam programações.
- Não grava no SQLite e não altera o catálogo.
- Sinaliza datas que já possuem programação.
- Bloqueia o prosseguimento quando falta uma data, quando duas semanas apontam para a mesma reunião ou quando há conflito com programação existente.
- Exige mapeamento único para as oito partes de cada semana antes de liberar a confirmação.
- Mantém avisos de validação e de mapeamento para serem apresentados na futura interface.

## Limite desta PR

Esta PR fornece o serviço/modelo da prévia e seus testes. A tela JavaFX que permite selecionar datas e revisar/ajustar correspondências será integrada depois que esta regra de domínio for validada. A gravação também fica em etapa separada, com tratamento transacional próprio.
