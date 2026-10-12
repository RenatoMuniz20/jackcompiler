# 🧠 Jack Compiler — Analisador Léxico (Scanner) para a Linguagem Jack (Nand2Tetris)

Este projeto implementa a primeira etapa do compilador para a linguagem **Jack**, proposta no curso e livro **Nand2Tetris**.

Desenvolvido em **Java**, o sistema realiza a leitura de arquivos de código-fonte `.jack`, identifica os lexemas de acordo com a especificação formal da linguagem, categoriza-os em tokens fortemente tipados e gera a saída padronizada em formato XML (`*T.xml`), pronta para validação contra o gabarito oficial do curso:

- **Análise Léxica (`Scanner`):** Lê os arquivos `.jack`, ignora comentários (`//`, `/* */`, `/** */`) e espaços em branco, manipulando strings literais, identificadores, inteiros, símbolos e palavras-chave.
- **Estruturação de Tokens (`Token` e `TokenType`):** Representa os tokens com tipos internos específicos (como `CLASS`, `PLUS`, `NUMBER`, `IDENT` e `STRING`), seus lexemas e o número da linha de origem.
- **Exportação XML (`XmlTokenWriter`):** Serializa a sequência de tokens em formato XML com os devidos escapes para caracteres reservados (`<`, `>`, `&`, `"`), gerando os arquivos de tokens (`<tokens> ... </tokens>`).
- **Validação e Testes:** Bateria de 18 testes automatizados com JUnit 5 para regras léxicas e casos especiais, complementada por comparação manual das saídas XML com os gabaritos oficiais (`MainT.xml`, `SquareT.xml`, `SquareGameT.xml`).

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
│   │               ├── Main.java                          🔹 Ponto de entrada CLI (um arquivo .jack por execução)
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
│       │               ├── ScannerTest.java               🔹 Testes unitários do scanner (JUnit 5)
│       │               └── ScannerCasosEspeciaisTest.java 🔹 Testes de casos de borda e regras léxicas especiais
│       │
│       └── resources/
│           ├── Exemplo.jack                               🔹 Exemplo simples para testes manuais
│           ├── ExemploT.xml                               🔹 Saída XML do exemplo
│           └── oficial/                                   🔹 Casos de teste do Nand2Tetris (Project 10)
│               ├── entrada/                               🔹 Arquivos .jack e XMLs gerados
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
│   → [LET: let] [IDENT: x] [EQ: =]                      │
│     [NUMBER: 10] [PLUS: +] [IDENT: y]                  │
│     [SEMICOLON: ;]                                     │
└───────────────────────────┬────────────────────────────┘
│
│ Fluxo de tokens
▼
┌────────────────────────────────────────────────────────┐
│  2. XML TOKEN WRITER                                   │
│                                                        │
│  Formata e escapa os tokens gerando o arquivo XML      │
│  conforme a especificação do Nand2Tetris.              │
└───────────────────────────┬────────────────────────────┘
│
▼
Arquivo *T.xml gerado

<tokens>
<keyword> let </keyword>
<identifier> x </identifier>
<symbol> = </symbol>
<integerConstant> 10 </integerConstant>
<symbol> + </symbol>
<identifier> y </identifier>
<symbol> ; </symbol>
</tokens>
```

### O que cada componente faz

- **`Scanner`** — Faz a varredura do texto fonte e identifica as 5 categorias de tokens suportadas pela linguagem Jack: palavras-chave (`class`, `function`, `method`, `if`, etc.), símbolos (`{`, `}`, `(`, `)`, `;`, etc.), constantes inteiras (`0` a `32767`), constantes de texto (delimitadas por aspas duplas sem quebra de linha) e identificadores válidos.
- **`Token` & `TokenType`** — Modelam cada elemento léxico, armazenando seu tipo específico, o lexema e o número da linha.
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

O programa processa **um arquivo `.jack` por execução**, usando o Maven. Não há suporte nesta versão para processar uma pasta inteira nem configuração de JAR executável via `java -jar`.

#### Processar um arquivo `.jack` específico
```bash
mvn compile exec:java "-Dexec.args=src/test/resources/Exemplo.jack"
```
> O arquivo `ExemploT.xml` será gerado no mesmo diretório do arquivo `.jack`. Para outros arquivos, substitua o caminho no argumento `-Dexec.args`.

---

### 🔹 Executar os Testes Automatizados (JUnit 5)

Para validar automaticamente o reconhecimento de tokens e o tratamento de erros (18 testes JUnit 5):

```bash
mvn test
```

Os testes cobrem:

| Classe de Teste | Cobertura |
|---|---|
| `ScannerTest` | 10 testes: palavras-chave, identificadores, inteiros, 19 símbolos, divisão e comentários, strings, linhas, erros léxicos e EOF. |
| `ScannerCasosEspeciaisTest` | 8 testes: limites de inteiros, valores muito grandes, acentos em strings e comentários, identificadores inválidos, finais de linha Windows e strings com quebras `\n`/`\r`. |

---

## ✅ Status de validação — Project 10 (Léxico)

Os três arquivos oficiais foram processados e seus XMLs gerados foram comparados com os gabaritos por conteúdo de linha usando `Compare-Object` no PowerShell (desconsiderando diferenças entre `LF` e `CRLF`):

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainT.xml` | ✅ Passou |
| `Square.jack` | `SquareT.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameT.xml` | ✅ Passou |

Para reproduzir a validação, gere as saídas com os arquivos de `src/test/resources/oficial/entrada/`:

```powershell
foreach ($nome in @("Main", "Square", "SquareGame")) {
    mvn compile exec:java "-Dexec.args=src/test/resources/oficial/entrada/$nome.jack"
}
```

Depois compare os resultados com os arquivos em `src/test/resources/oficial/referencia/`:

```powershell
$base = "src/test/resources/oficial"
foreach ($nome in @("Main", "Square", "SquareGame")) {
    $gerado = Get-Content "$base/entrada/${nome}T.xml"
    $referencia = Get-Content "$base/referencia/${nome}T.xml"
    $diferencas = Compare-Object $referencia $gerado
    if ($diferencas) { Write-Host "$nome : DIFERENTE" }
    else { Write-Host "$nome : APROVADO" }
}
```

O arquivo `Exemplo.jack` foi utilizado adicionalmente como teste manual local, gerando `ExemploT.xml`; ele não faz parte dos três gabaritos oficiais.

---

## 📌 Observações

- A formatação de saída segue a convenção `<tokens> ... </tokens>` estipulada para os testes do Project 10 do Nand2Tetris.
- O tratamento de caracteres especiais de XML (`&`, `<`, `>`, `"`) é realizado na geração do XML para permitir comparação com os arquivos de referência.
- Artefatos gerados em tempo de compilação ou execução dentro da pasta `target/` não são versionados.

---

## 👥 Créditos

**Alunos:**

- **José Nunes de Sousa Neto** — Matrícula: **20250071231**
- **Renato Muniz Gomes** — Matrícula: **20250071330**

**Disciplina:** EECP0026 — Compiladores  

**Professor:** Prof. Dr. Sergio Souza Costa  

**Instituição:** UFMA — Universidade Federal do Maranhão  

**Semestre:** 2026.2

---

<div align="center">

**Este repositório possui fins acadêmicos.**

</div>

---