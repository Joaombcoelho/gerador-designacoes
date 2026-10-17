# Tela de conferência da importação de apostilas

A tela fica disponível no menu lateral em **Importar apostila**.

## Fluxo disponível

1. Extrair diretamente os arquivos RTF pela tela do aplicativo, selecionando a pasta com os arquivos; ou selecionar um arquivo JSON já produzido anteriormente.
2. Conferir erros e avisos da validação.
3. Conferir, por semana, a leitura bíblica, os cânticos e as oito partes.
4. Informar explicitamente a data real da reunião para cada semana.
5. Consultar sugestões de correspondência e selecionar manualmente uma parte existente do catálogo.
6. Gerar a prévia técnica, que verifica se as datas já possuem programação e apresenta erros/avisos.

## Extração integrada

- A extração reutiliza as regras do protótipo `ExtratorApostilaRtf`, agora encapsuladas em um serviço Java chamado pela interface.
- A extração integrada não abre a IDE, o terminal nem uma janela de outro programa.
- O JSON é gerado inicialmente em arquivo temporário e carregado automaticamente para validação.
- Salvar uma cópia permanente é opcional.
- A pasta selecionada é examinada apenas no primeiro nível; arquivos RTF em subpastas não são percorridos.

## Proteções

- A tela não cria nem altera programações.
- A tela não grava o JSON nem as escolhas manuais no banco. O JSON gerado fica temporariamente em arquivo local até o encerramento/limpeza pelo sistema.
- A verificação de programação existente usa consulta por data.
- A data da apostila não é presumida como data da reunião.
- As escolhas manuais são enviadas ao serviço de prévia por data inicial da semana e número da parte; a prévia aplica as escolhas ao resultado em memória.
- A prévia mostra o mapeamento final, leitura bíblica, cânticos e conflitos de programação existente.
- Se a mesma parte do catálogo for atribuída a mais de um item da mesma semana, a prévia sinaliza erro.
- O serviço de prévia continua sem DAO e não persiste escolhas ou programação.

## Confirmação e gravação

Quando a prévia não contém erros, todas as semanas têm oito mapeamentos únicos e não há datas conflitantes, o botão **Confirmar importação** fica habilitado. A tela apresenta uma confirmação explícita antes de gravar.

- A confirmação usa o resultado da prévia atual; qualquer alteração posterior nas datas ou nas seleções invalida a prévia e exige conferência novamente.
- O serviço monta cada programação em memória, usa as partes fixas do serviço de programação e as partes variáveis selecionadas, e aplica os temas e durações confirmados.
- Os cânticos são gravados nos campos da programação semanal.
- A persistência usa uma única transação para todas as semanas.
- A transação verifica novamente se alguma data já possui programação. Se houver conflito ou qualquer falha, toda a operação é revertida.
- Programações existentes não são atualizadas ou sincronizadas e o processo não usa `obterOuCriar`.
- A importação não cria novos registros no catálogo de partes.

## Limites e validação

A etapa de confirmação está implementada nesta versão, mas precisa ser validada localmente antes de ser integrada. Executar `mvn test` com JDK 22 e testar uma importação simples, várias semanas, conflito com data existente, cancelamento do diálogo e ausência de alterações nas programações anteriores. A documentação não substitui a confirmação desses testes.
