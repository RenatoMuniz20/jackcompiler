# 🧠 Jack Compiler — Analisador Léxico (Scanner) para a Linguagem Jack (Nand2Tetris)

Este projeto implementa a primeira etapa do compilador para a linguagem **Jack**, proposta no curso e livro **Nand2Tetris**.

Desenvolvido em **Java**, o sistema realiza a leitura de arquivos de código-fonte `.jack`, identifica os lexemas de acordo com a especificação formal da linguagem, categoriza-os em tokens fortemente tipados e gera a saída padronizada em formato XML (`*T.xml`), pronta para validação contra o gabarito oficial do curso:

- **Análise Léxica (`Scanner`):** Lê os arquivos `.jack`, ignora comentários (`//`, `/* */`, `/** */`) e espaços em branco, manipulando strings literais, identificadores, inteiros, símbolos e palavras-chave.
- **Estruturação de Tokens (`Token` e `TokenType`):** Encapsula os tipos léxicos (`KEYWORD`, `SYMBOL`, `IDENTIFIER`, `INT_CONST`, `STRING_CONST`) e seus respectivos valores.
- **Exportação XML (`XmlTokenWriter`):** Serializa a sequência de tokens em formato XML com os devidos escapes para caracteres reservados (`<`, `>`, `&`, `"`), gerando os arquivos de tokens (`<tokens> ... </tokens>`).
- **Validação e Testes:** Bateria de testes automatizados com JUnit 5 cobrindo casos gerais, cenários especiais de formatação e comparação direta com os gabaritos oficiais (`MainT.xml`, `SquareT.xml`, `SquareGameT.xml`).

---

## 📁 Estrutura do Projeto

```
jackcompiler/
│
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── jackcompiler/
│   │               ├── Main.java                          🔹 Ponto de entrada CLI (execução individual e em diretórios)
│   │               │
│   │               ├── lexer/                             🔹 Núcleo da análise léxica
│   │               │   ├── TokenType.java                 🔹 Enum dos tipos de token da especificação Jack
│   │               │   ├── Token.java                     🔹 Representação e modelo de dados do token
│   │               │   └── Scanner.java                   🔹 Analisador léxico / tokenizador
│   │               │
│   │               └── xml/                               🔹 Geração e exportação dos dados
│   │                   └── XmlTokenWriter.java            🔹 Serializador de tokens para XML formatado
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── jackcompiler/
│       │           └── lexer/
│       │               ├── ScannerTest.java               🔹 Testes unitários do scanner e validação oficial (JUnit 5)
│       │               └── ScannerCasosEspeciaisTest.java 🔹 Testes de casos de borda e regras léxicas especiais
│       │
│       └── resources/
│           ├── Exemplo.jack                               🔹 Exemplo simples para testes manuais
│           ├── ExemploT.xml                               🔹 Gabarito XML do exemplo
│           └── oficial/                                   🔹 Casos de teste do Nand2Tetris (Project 10)
│               ├── entrada/                               🔹 Arquivos .jack e saídas esperadas
│               │   ├── Main.jack / MainT.xml
│               │   ├── Square.jack / SquareT.xml
│               │   └── SquareGame.jack / SquareGameT.xml
│               └── referencia/                            🔹 Gabaritos oficiais de referência
│                   ├── MainT.xml
│                   ├── SquareT.xml
│                   └── SquareGameT.xml
│
├── pom.xml                                                🔹 Configuração Maven (dependências e build)
├── README.md
└── .gitignore
```
---

## 🔍 Como o Scanner funciona

O código-fonte `.jack` passa pelo analisador léxico para ser transformado em um fluxo estruturado de tokens antes de qualquer análise gramatical:

```
Arquivo .jack
│
▼
┌────────────────────────────────────────────────────────┐
│  1. SCANNER (Analisador Léxico)                        │
│                                                        │
│  Lê o fluxo de texto caractere por caractere, ignora   │
│  espaços e comentários, e agrupa em tokens.            │
│                                                        │
│  "let x = 10 + y;"                                     │
│   → [KEYWORD: let] [IDENTIFIER: x] [SYMBOL: =]        │
│     [INT_CONST: 10] [SYMBOL: +] [IDENTIFIER: y]        │
│     [SYMBOL: ;]                                        │
└───────────────────────────┬────────────────────────────┘
│
│ List
▼
┌────────────────────────────────────────────────────────┐
│  2. XML TOKEN WRITER                                   │
│                                                        │
│  Formata e escapa os tokens gerando o arquivo XML      │
│  conforme a especificação do Nand2Tetris.              │
└───────────────────────────┬────────────────────────────┘
│
▼
Arquivo T.xml gerado

 let x = 10 + y 
 ;
```

### O que cada componente faz

- **`Scanner`** — Faz a varredura do texto fonte e identifica as 5 categorias de tokens suportadas pela linguagem Jack: palavras-chave (`class`, `function`, `method`, `if`, etc.), símbolos (`{`, `}`, `(`, `)`, `;`, etc.), constantes inteiras (`0` a `32767`), constantes de texto (delimitadas por aspas duplas sem quebra de linha) e identificadores válidos.
- **`Token` & `TokenType`** — Modelam os dados de cada elemento léxico, armazenando seu tipo e seu valor textual literal.
- **`XmlTokenWriter`** — Transforma a sequência de tokens gerada em uma árvore XML bem formada, aplicando automaticamente o tratamento de caracteres especiais XML (ex.: `<` vira `&lt;`, `>` vira `&gt;`, `&` vira `&amp;` e `"` vira `&quot;`).

---

## 🚀 Como executar

### Pré-requisitos

- Java 17+
- Maven 3.6+

### 🔹 Compilação (Build)

```bash
mvn clean package
```

### 🔹 Execução via linha de comando (CLI)

O executável permite compilar tanto um arquivo individual quanto um diretório completo com múltiplos arquivos:

#### 1. Processar um arquivo `.jack` específico
```bash
java -jar target/jackcompiler-1.0-SNAPSHOT.jar caminho/para/Arquivo.jack
```
> O arquivo `ArquivoT.xml` será gerado automaticamente no mesmo diretório de entrada.

#### 2. Processar uma pasta com múltiplos arquivos `.jack`
```bash
java -jar target/jackcompiler-1.0-SNAPSHOT.jar caminho/para/diretorio/
```
> Todos os arquivos `.jack` encontrados na pasta serão tokenizados e seus respectivos `*T.xml` serão gerados no mesmo local.

---

### 🔹 Executar os Testes Automatizados (JUnit 5)

Para validar a conformidade com as regras léxicas e comparar os resultados com os gabaritos oficiais:

```bash
mvn test
```

Os testes cobrem:

| Classe de Teste | Cobertura |
|---|---|
| `ScannerTest` | Tokenização completa, integridade dos tipos e comparação direta com os gabaritos oficiais do Project 10 (`MainT.xml`, `SquareT.xml`, `SquareGameT.xml`). |
| `ScannerCasosEspeciaisTest` | Casos de borda: comentários aninhados/multilinha, strings com caracteres especiais, operadores adjacentes e escape de entidades XML. |

---

## ✅ Status de validação — Project 10 (Léxico)

Todos os casos de teste do curso oficial foram processados e validados contra os gabaritos:

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainT.xml` | ✅ Passou |
| `Square.jack` | `SquareT.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameT.xml` | ✅ Passou |
| `Exemplo.jack` | `ExemploT.xml` | ✅ Passou |

---

## 📌 Observações

- A formatação de saída segue a convenção `<tokens> ... </tokens>` estipulada para os testes do Project 10 do Nand2Tetris.
- O tratamento de caracteres especiais de XML (`&`, `<`, `>`, `"`) é realizado na geração do XML para garantir compatibilidade com os comparadores de texto (`TextComparer`).
- Artefatos gerados em tempo de compilação ou execução dentro da pasta `target/` não são versionados.

---

## 👥 Créditos

**Alunos:** Renato Muniz Gomes e José Nunes de Sousa Neto

**Disciplina:** EECP0026 — Compiladores  

**Professor:** Prof. Dr. Sergio Souza Costa  

**Instituição:** UFMA — Universidade Federal do Maranhão  

**Semestre:** 2026.2

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---
