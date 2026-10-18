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
- As escolhas manuais são apenas visuais nesta etapa; ainda não são enviadas ao serviço de confirmação/importação.

## Limites desta etapa

Esta etapa integra a extração RTF à tela já existente e mantém o fluxo de conferência como somente leitura. Não implementa a confirmação final, gravação transacional, criação de partes, nem importação de cânticos para uma programação. Essas operações devem ser desenvolvidas em etapa separada, depois de a extração integrada ser testada.
