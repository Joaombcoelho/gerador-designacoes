# Importação de apostilas — etapa 1

Esta etapa adiciona a leitura e a validação do JSON produzido por `ExtratorApostilaRtf`.

## O que está implementado

- Leitura do JSON com Jackson.
- Validação de datas, semanas repetidas, oito partes, números duplicados, tema, duração e identificadores de seção.
- Conversão dos identificadores de seção do JSON para `SecaoParte` do sistema.
- Leitura de cânticos, subtítulos e descrição curta da designação.
- Separação de erros impeditivos e avisos que exigem conferência.
- Testes unitários para um JSON válido, data inválida, seção desconhecida, parte duplicada e lista de semanas vazia.

## Limite intencional desta etapa

O serviço `ImportacaoApostilaJsonService` **não acessa DAOs e não grava nem modifica o banco de dados**. Ele devolve uma estrutura validada para ser usada pela próxima etapa, que construirá a prévia de importação.

O importador ainda não decide que registro de `Parte` corresponde a cada item da apostila. Esse mapeamento deverá usar os tipos e as regras existentes no sistema, além da seção, e será feito separadamente para evitar associações automáticas incorretas.

## Próximas etapas

1. Mapear cada parte extraída para as partes/tipos já cadastrados no banco.
2. Definir como a semana da apostila se relaciona com a data usada pela programação semanal do aplicativo.
3. Criar a tela de prévia, com conflitos e dados que precisam de revisão.
4. Implementar a confirmação e a gravação transacional, protegendo programações e ajustes manuais existentes.
