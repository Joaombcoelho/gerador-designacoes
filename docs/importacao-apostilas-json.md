# Importação de apostilas RTF/JSON

A importação usa um fluxo em etapas para separar extração, validação, conferência e gravação.

## Fluxo disponível

1. Extrair arquivos RTF diretamente pela tela ou abrir um JSON existente.
2. Validar a estrutura JSON, datas, semanas, oito partes, números, temas, duração, seções e cânticos.
3. Informar a data efetiva da reunião para cada semana, sem presumir que as datas da apostila sejam datas de reunião.
4. Conferir sugestões de mapeamento e selecionar manualmente partes já cadastradas no catálogo.
5. Gerar a prévia técnica com o mapeamento final e verificar conflitos.
6. Confirmar explicitamente a importação quando todas as semanas estiverem válidas.

## Persistência e segurança

- A extração, a validação e a prévia não gravam no banco.
- A importação aceita apenas mapeamentos completos para partes já cadastradas; esta etapa não cria novos registros no catálogo de partes.
- As partes variáveis selecionadas precisam respeitar as regras existentes do gerador, incluindo o limite de 3 a 6 por semana.
- A data efetiva de cada reunião precisa ser informada pelo usuário.
- Se uma data já possuir programação, a importação é recusada. Não utiliza `obterOuCriar`, pois esse método pode sincronizar partes fixas ou ajustar dados de uma programação existente.
- As programações de todas as semanas e suas partes são inseridas por `ProgramacaoSemanaDAO.salvarTodasNovas` numa única transação SQLite.
- A persistência verifica novamente as datas dentro da transação e também depende da restrição única da coluna `programacao_semana.data` para impedir duplicidades concorrentes.
- Se qualquer semana falhar, a transação é revertida; não deve haver importação parcial.
- A confirmação na interface é explícita e separada da prévia.

## Dados importados

Para cada semana, o processo usa os mapeamentos confirmados para associar os itens da apostila às partes existentes, aplica tema e duração às ocorrências correspondentes e transfere os três cânticos para os campos da programação semanal. As partes fixas são montadas pelo serviço de programação já existente; as partes variáveis vêm das escolhas confirmadas.

## Validação local necessária

Antes de integrar esta etapa, execute `mvn test` com JDK 22 e faça o teste prático completo com uma cópia de segurança do banco:

- Importar uma semana com mapeamentos válidos.
- Importar várias semanas numa mesma operação.
- Tentar importar uma data que já possui programação e confirmar que nada é alterado.
- Forçar uma falha de validação antes da gravação e confirmar que a importação não começa.
- Confirmar que temas, durações, cânticos e partes foram gravados como mostrado na prévia.
- Conferir que uma falha durante a gravação reverte todas as semanas.
- Verificar histórico, ajustes manuais e programações anteriores antes e depois do teste.

O resultado dos testes locais não é presumido pela documentação; deve ser confirmado na versão exata que será integrada.
