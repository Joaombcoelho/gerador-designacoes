# Gerador de Designações

Aplicação desktop em Java com JavaFX para gerenciar pessoas, partes de reunião, programação semanal e geração de escala de designações. O sistema usa SQLite como banco local, persiste histórico de participações e tenta distribuir as atribuições considerando regras de privilégio, sexo, participação, nível de leitura e frequência histórica.

## Sobre o projeto

O projeto nasceu como uma ferramenta para auxiliar na organização de designações de reuniões, com foco em rotatividade, consistência e redução de atribuições manuais. A aplicação permite cadastrar pessoas e partes de reunião, gerar uma escala para uma data específica e manter um histórico de participações para apoiar a distribuição das funções.

No estado atual do código, o sistema é uma aplicação desktop com interface JavaFX que combina CRUD de dados, lógica de negócio para geração de designações e persistência local em SQLite. A estrutura foi organizada em camadas bem definidas: modelos, DAOs, serviços, controladores e views.

O público-alvo é quem precisa controlar designações em uma reunião ou contexto semelhante, especialmente quando há regras de elegibilidade e necessidade de evitar repetições indevidas. As principais funcionalidades já implementadas incluem cadastro e edição de pessoas, cadastro de partes, geração automática de escala e configuração da programação semanal.

Características relevantes da solução:

- persistência local em banco SQLite;
- geração automática de escala por data;
- histórico de participação por pessoa e parte;
- regras de privilégio, sexo, leitura e participação;
- suporte a partes fixas e variáveis na programação semanal;
- backup automático e restauração do banco;
- interface desktop em JavaFX.

## Funcionalidades

As funcionalidades abaixo foram identificadas diretamente no código e na estrutura do projeto:

- gerenciamento de pessoas;
- gerenciamento de partes de reunião;
- cadastro de papéis e participações exigidas por parte;
- geração automática de escala de designações;
- suporte a designações com responsável e ajudante;
- programação semanal com ordenação das partes;
- atribuição de temas para partes da programação semanal;
- histórico de designações e participações;
- persistência em SQLite;
- backup e restauração do banco de dados;
- regras de distribuição baseadas em histórico, privilégio e elegibilidade.

## Tecnologias utilizadas

As tecnologias e bibliotecas realmente presentes no projeto são:

- Java 22 (definido em `pom.xml` via `maven.compiler.release`);
- Maven;
- JavaFX 21.0.7;
- SQLite;
- JDBC via `sqlite-jdbc` 3.50.3.0;
- JUnit 5.10.2;
- JUnit 4.13.1;
- Maven Surefire Plugin;
- JavaFX Maven Plugin.

## Arquitetura e estrutura do projeto

A organização do projeto segue a separação por camadas, com classes Java organizadas por pacote:

```text
gerador-designacoes/
├── .idea/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── br/com/geradordesignacoes/
│   │   │       ├── controller/
│   │   │       ├── dao/
│   │   │       ├── database/
│   │   │       ├── model/
│   │   │       ├── service/
│   │   │       ├── view/
│   │   │       ├── Main.java
│   │   │       ├── MainApp.java
│   │   │       └── ...
│   │   └── resources/
│   └── test/
│       └── java/
│           └── br/com/geradordesignacoes/
├── data/
├── pom.xml
├── README.md
├── .gitignore
├── target/
└── ...
```

Principais responsabilidades por pacote:

- `model`: entidades do domínio, enums e objetos de regra de negócio, como `Pessoa`, `Parte`, `Escala`, `Designacao`, `ProgramacaoSemana` e demais modelos auxiliares.
- `dao`: acesso direto ao SQLite, incluindo consultas, inserções, atualizações e carregamento de dados.
- `service`: lógica de regras de negócio, avaliação de candidatos, geração de escala e programação semanal.
- `controller`: integração entre a interface gráfica e os serviços.
- `view`: telas JavaFX para pessoas, partes, escala, programação, histórico e edição da escala.
- `database`: criação do banco, setup inicial e utilitários de backup/restauração.

## Banco de dados

O banco utilizado é SQLite. A conexão é iniciada em `ConnectionFactory`, que cria o diretório `%LOCALAPPDATA%\GeradorDesignacoes` e usa o arquivo `gerador-designacoes.db` como base local.

```java
private static final Path DATABASE_DIRECTORY =
        Path.of(System.getenv("LOCALAPPDATA"), "GeradorDesignacoes");

private static final Path DATABASE_PATH =
        DATABASE_DIRECTORY.resolve("gerador-designacoes.db");
```

Os principais objetos persistidos no banco são:

- `pessoa`
- `parte`
- `parte_participacao_necessaria`
- `historico_designacoes`
- `escala`
- `designacao`
- `programacao_semana`
- `programacao_parte`

Relacionamentos principais:

- `designacao` referencia `escala`, `parte`, `responsavel_id` e `ajudante_id`;
- `historico_designacoes` referencia `pessoa` e `parte`;
- `parte_participacao_necessaria` referencia `parte` e armazena as participações necessárias para cada parte;
- `programacao_parte` referencia `programacao_semana` e `parte`;
- `programacao_semana` possui uma data e lista de partes programadas para a semana.

A criação e atualização da estrutura do banco ocorrem em `DatabaseInitializer.initialize()`. Esse inicializador cria as tabelas se não existirem e também realiza um seed inicial de partes e pessoas. O seed define partes como "Presidente", "Oração inicial", "Discurso — Tesouros", "Joias Espirituais", "Leitura", "Estudo Bíblico" e "Oração final", além de uma lista inicial de pessoas com atributos de privilégio e permissões.

## Regras de negócio

As principais regras identificadas no código são as seguintes:

- somente pessoas com `ativo = true` podem ser designadas;
- a pessoa precisa ter pelo menos uma participação que a parte exige;
- a parte define `privilegio_minimo`, `sexo_permitido`, `quantidade_minima_participantes`, `nivel_leitura_minimo`, `exige_ajudante`, `gera_formulario` e `possui_tema`;
- o presidente não pode receber uma segunda designação na mesma reunião;
- anciãos e servos ministeriais podem receber mais de uma designação na mesma reunião;
- uma pessoa só pode ser selecionada para uma parte se estiver habilitada para a participação necessária;
- a geração de escala considera o histórico para reduzir repetição de tarefas e priorizar pessoas menos frequentes;
- a avaliação de candidatos usa três critérios principais:
  1. quantidade de participações anteriores;
  2. compatibilidade de privilégio com a parte;
  3. penalidade por repetição de participação na mesma parte;
- a seleção de dupla para demostração exige pessoa responsável e ajudante diferentes, ativos, do mesmo sexo e habilitados para as funções;
- a programação semanal exige entre 3 e 6 partes variáveis e rejeita partes duplicadas ou não variáveis;
- partes da programação semanal podem ter tema e ordem em sequência;
- a ordem padrão das partes da reunião é definida pelo `ProgramacaoSemanaService` com base nos nomes das partes.

Em termos práticos, a lógica simula uma distribuição balanceada, evitando que a mesma pessoa fique repetidamente na mesma função e respeitando os limites do perfil da pessoa e da parte.

## Testes

O projeto possui testes automatizados em JUnit sob `src/test/java`. Há várias classes de teste cobrindo a lógica de geração de escala, regras de negócio, DAO, partes, pessoas, programação semanal e avaliação de candidatos.

O framework principal é:

- JUnit 5 (presente em `org.junit.jupiter`)
- JUnit 4 (presente em `junit:junit`)

Os relatórios gerados em `target/surefire-reports` confirmam que existem testes automatizados no projeto, mas o estado atual da suíte não está estável em toda a execução. Os arquivos de relatório mostram erros de SQLite do tipo `SQLITE_BUSY` e `database is locked`, com falha durante a inicialização do banco em testes que chamam `DatabaseInitializer.initialize()`. Esse problema está ligado à concorrência/lock do SQLite durante a criação das tabelas e seed inicial dos testes.

Portanto, o projeto contém testes automatizados, mas a suíte atual não pode ser considerada completamente estável no ambiente de execução verificado.

## Como executar o projeto

Pré-requisitos confirmados pelo projeto:

- JDK 22;
- Maven instalado e configurado no PATH;
- ambiente Windows, pois a aplicação grava o banco em `%LOCALAPPDATA%\GeradorDesignacoes`;
- dependências JavaFX e SQLite gerenciadas pelo Maven.

Fluxo de execução recomendado a partir da raiz do projeto:

```bash
git clone https://github.com/<seu-usuario>/gerador-designacoes.git
cd gerador-designacoes
mvn javafx:run
```

O plugin JavaFX no `pom.xml` aponta `br.com.geradordesignacoes.MainApp` como classe principal, então a execução da aplicação por esse comando é a forma mais direta de iniciar a interface.

## Requisitos

- Java 22;
- Maven;
- sistema operacional Windows para o armazenamento atual do banco em `LOCALAPPDATA`;
- acesso a um ambiente com JavaFX disponível via dependências Maven;
- SQLite driver gerenciado automaticamente pelo `sqlite-jdbc`.

## Configuração

Não há arquivo de configuração externo como `application.properties`, `.env` ou YAML no projeto. A aplicação cria o banco e o diretório de dados automaticamente na primeira execução.

O banco e os backups são salvos em:

```text
%LOCALAPPDATA%\GeradorDesignacoes\
├── gerador-designacoes.db
└── backups/
```

## Uso da aplicação

O fluxo principal da aplicação, inferido a partir dos pacotes e controladores, é:

1. iniciar a aplicação (`MainApp`);
2. inicializar o banco e o seed inicial (`DatabaseInitializer`);
3. abrir a tela principal (`MainView`);
4. cadastrar pessoas e partes no menu de cadastros;
5. definir a programação semanal, quando necessário;
6. gerar uma escala para uma data específica;
7. revisar, salvar e editar a escala gerada;
8. consultar o histórico de designações;
9. fazer backup e restaurar o banco quando necessário.

## Estrutura das principais entidades

| Entidade | Responsabilidade |
| --- | --- |
| `Pessoa` | representa um participante com nome, sexo, privilégio, nível de leitura, status de participação e permissões operacionais. |
| `Parte` | representa uma tarefa ou parte da reunião, com tipo, privilégio mínimo, sexo permitido, exigência de ajudante e participações necessárias. |
| `ParticipacaoDesignacao` | registra uma participação específica de uma pessoa em uma parte em uma data. |
| `Designacao` | representa a atribuição concreta de uma pessoa (e opcionalmente ajudante) a uma parte em uma escala. |
| `Escala` | representa a geração de designações para uma data, com status, data de geração e lista de designações. |
| `ProgramacaoSemana` | agrupa uma data e a sequência de partes da programação semanal. |
| `ProgramacaoParte` | representa uma parte dentro da programação semanal, com ordem e tema. |
| `HistoricoDesignacoes` | consolida o histórico de participações para cálculo de distribuição e regras. |

## Status do projeto

### Concluído

- estrutura de aplicação desktop JavaFX;
- cadastro de pessoas;
- cadastro de partes de reunião;
- geração de escala por data;
- histórico de designações;
- persistência em SQLite;
- programação semanal com partes fixas/variáveis;
- backup automático e restauração do banco.

