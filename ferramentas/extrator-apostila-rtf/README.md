# Protótipo isolado — extrator de apostilas RTF

Este protótipo lê arquivos `.rtf` de uma pasta, identifica a semana, leitura bíblica, cânticos e as oito partes, e gera um JSON provisório para conferência humana.

## Segurança e limites

- Não acessa nem altera o banco de dados.
- Não grava nada na programação do gerador.
- O JSON é marcado com `status_conferencia: "pendente"` tanto no documento quanto em cada semana.
- A extração é experimental e precisa de conferência humana antes de qualquer uso operacional.
- O protótipo usa somente classes do JDK; não adiciona dependências ao projeto.
- A janela de seleção de pasta/arquivo usa Swing apenas neste protótipo. Na integração ao gerador JavaFX, a seleção deverá usar o seletor de diretórios da própria interface.

## Campos principais do JSON

Cada parte contém:

- `numero`, `tema` e `duracao_minutos`;
- `secao`, com um identificador estável (`tesouros`, `ministerio` ou `vida_crista`);
- `secao_titulo`, com o título da seção conforme aparece na apostila;
- `subtitulo`, quando o extrator reconhece um tipo de designação, por exemplo `De casa em casa` ou `Testemunho público`;
- `descricao_designacao`, para as partes 4 a 6, guardando somente a primeira frase da linha de instrução (até o primeiro ponto final, inclusive), mesmo quando o subtítulo ainda não é reconhecido automaticamente. Se não houver ponto final, preserva o texto disponível.

A lógica do extrator registra a estrutura e o texto da apostila. A interpretação dos tipos internos do gerador e as regras de negócio devem continuar no `gerador-designacoes`, não ser duplicadas neste protótipo.

## Como executar

Requer JDK instalado.

```bash
javac -encoding UTF-8 ExtratorApostilaRtf.java
java -cp . ExtratorApostilaRtf
```

Sem argumentos, abre uma janela para selecionar a pasta com os RTFs e outra para escolher onde salvar o JSON.

Também aceita caminhos como argumentos:

```bash
java -cp . ExtratorApostilaRtf "C:\\caminho\\para\\apostila Rtf" "C:\\caminho\\apostila-extraida.json"
```

O protótipo considera os arquivos `.rtf` diretamente na pasta selecionada; não percorre subpastas.

## Validações automáticas

- Identificação de cabeçalho, ano e datas válidas;
- tratamento de semanas que atravessam a virada do mês/ano;
- presença das oito partes, duplicidade de números e associação da parte a uma seção;
- quantidade esperada de três cânticos;
- alertas para conferência manual quando algo não é identificado ou parece inconsistente.

Essas validações não garantem a correção semântica de cada campo. O status permanece pendente e os dados precisam ser conferidos contra a apostila original.

## Teste local de novembro–dezembro de 2026

A versão revisada foi compilada e executada contra os cinco arquivos RTF disponíveis. Foram identificadas cinco semanas e oito partes por semana, com associação às três seções. O JSON gerado foi validado como JSON válido. Essa amostra não substitui testes com outras edições e variações de formatação.

Este protótipo está versionado em uma pasta própria, mas permanece isolado: não está conectado à interface, às regras de negócio ou ao banco de dados do `gerador-designacoes`.
