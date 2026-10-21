# 🧠 Jack Compiler — Analisador Léxico e Sintático para a Linguagem Jack (Nand2Tetris)

Este projeto implementa as duas primeiras etapas do compilador para a linguagem **Jack**, proposta no curso e livro **Nand2Tetris**.

Desenvolvido em **Java**, o sistema lê arquivos de código-fonte `.jack`, identifica os lexemas segundo a especificação formal da linguagem, verifica a conformidade com a gramática por *recursive descent parsing* e gera as saídas XML padronizadas (`*T.xml` para tokens e `*P.xml` para a árvore sintática), prontas para validação contra os gabaritos oficiais do curso:

- **Análise Léxica (`Scanner`):** Lê os arquivos `.jack`, ignora comentários (`//`, `/* */`, `/** */`) e espaços em branco, manipulando strings literais, identificadores, inteiros, símbolos e palavras-chave.
- **Estruturação de Tokens (`Token` e `TokenType`):** Representa os tokens com tipos internos específicos (como `CLASS`, `PLUS`, `NUMBER`, `IDENT` e `STRING`), seus lexemas e a posição de origem (linha e coluna).
- **Análise Sintática (`CompilationEngine`):** Parser descendente recursivo, com métodos para as principais construções da gramática Jack (`class`, `classVarDec`, `subroutineDec`, `parameterList`, `subroutineBody`, `varDec`, `statements`, `let`, `if`, `while`, `do`, `return`, `expression`, `term` e `expressionList`). Consome os tokens produzidos pelo `Scanner` e reporta erros sintáticos com linha, coluna, tipo e lexema do token encontrado.
- **Exportação XML (`XmlTokenWriter` e `XmlParseWriter`):** Serializam, respectivamente, a sequência de tokens (`<tokens> ... </tokens>`) e a árvore sintática (`<class> ... </class>`), com escape dos caracteres reservados (`<`, `>`, `&`, `"`).
- **Validação e Testes:** Testes automatizados com JUnit 5 para o scanner e para o parser, incluindo a comparação automática da saída do parser com os gabaritos oficiais (`MainP.xml`, `SquareP.xml`, `SquareGameP.xml`).

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
│   │               ├── Main.java                          🔹 Ponto de entrada CLI (--tokens, --parse ou ambos)
│   │               │
│   │               ├── lexer/                             🔹 Núcleo da análise léxica
│   │               │   ├── TokenType.java                 🔹 Enum dos tipos de token da especificação Jack
│   │               │   ├── Token.java                     🔹 Modelo de dados do token (tipo, lexema, linha, coluna)
│   │               │   └── Scanner.java                   🔹 Analisador léxico / tokenizador
│   │               │
│   │               ├── parser/                            🔹 Núcleo da análise sintática
│   │               │   └── CompilationEngine.java         🔹 Parser descendente recursivo e mensagens de erro sintático
│   │               │
│   │               └── xml/                               🔹 Geração e exportação dos dados
│   │                   ├── XmlTokenWriter.java            🔹 Serializador de tokens para XML (*T.xml)
│   │                   └── XmlParseWriter.java            🔹 Serializador da árvore sintática para XML (*P.xml)
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── jackcompiler/
│       │           ├── lexer/
│       │           │   ├── ScannerTest.java               🔹 Testes unitários do scanner (JUnit 5)
│       │           │   └── ScannerCasosEspeciaisTest.java 🔹 Casos de borda e regras léxicas especiais
│       │           │
│       │           └── parser/
│       │               ├── CompilationEngineTest.java     🔹 Classes e declarações de variáveis
│       │               ├── SubroutineTest.java            🔹 Subrotinas, parâmetros e variáveis locais
│       │               ├── ComandosExpressoesTest.java    🔹 Comandos, expressões e termos
│       │               ├── ErrosSintaticosTest.java       🔹 Mensagens e posição dos erros sintáticos
│       │               ├── CasosEspeciaisParserTest.java  🔹 Escape XML, expressões planas, arrays e else
│       │               └── ValidacaoOficialTest.java      🔹 Comparação automática com os gabaritos oficiais
│       │
│       └── resources/
│           ├── Exemplo.jack                               🔹 Exemplo simples para testes manuais
│           ├── ExemploT.xml                               🔹 Saída XML do exemplo
│           └── oficial/                                   🔹 Casos de teste do Nand2Tetris (Project 10)
│               ├── entrada/                               🔹 Fontes .jack e XMLs léxicos versionados
│               │   ├── Main.jack / MainT.xml
│               │   ├── Square.jack / SquareT.xml
│               │   └── SquareGame.jack / SquareGameT.xml
│               └── referencia/                            🔹 Gabaritos oficiais de referência
│                   ├── MainT.xml / MainP.xml
│                   ├── SquareT.xml / SquareP.xml
│                   └── SquareGameT.xml / SquareGameP.xml
│
├── pom.xml                                                🔹 Configuração Maven (dependências e build)
├── README.md
└── .gitignore
```
---

## 🔍 Como o compilador funciona

O código-fonte `.jack` passa pelo analisador léxico, que o transforma em um fluxo de tokens. O parser consome esse fluxo sob demanda e produz a árvore sintática:

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
│ Fluxo de tokens (com linha e coluna)
▼
┌────────────────────────────────────────────────────────┐
│  2. COMPILATION ENGINE (Analisador Sintático)          │
│                                                        │
│  Recursive descent: métodos para as regras de Jack.    │
│  Usa advance(), peek(), match() e consume() e olha um  │
│  token à frente para distinguir x[i], f() e a.f().     │
│                                                        │
│  compileClass → compileSubroutineDec → compileStatements│
│  → compileLet → compileExpression → compileTerm ...    │
└───────────────────────────┬────────────────────────────┘
│
▼
┌────────────────────────────────────────────────────────┐
│  3. XML WRITERS                                        │
│                                                        │
│  XmlTokenWriter → *T.xml (lista plana de tokens)       │
│  XmlParseWriter → *P.xml (árvore sintática)            │
└───────────────────────────┬────────────────────────────┘
│
▼
Arquivos *T.xml e *P.xml gerados

<letStatement>
  <keyword> let </keyword>
  <identifier> x </identifier>
  <symbol> = </symbol>
  <expression>
    <term>
      <integerConstant> 10 </integerConstant>
    </term>
    <symbol> + </symbol>
    <term>
      <identifier> y </identifier>
    </term>
  </expression>
  <symbol> ; </symbol>
</letStatement>
```

### O que cada componente faz

- **`Scanner`** — Faz a varredura do texto fonte e identifica as 5 categorias de tokens da linguagem Jack: palavras-chave (`class`, `function`, `method`, `if`, etc.), símbolos (`{`, `}`, `(`, `)`, `;`, etc.), constantes inteiras (`0` a `32767`), constantes de texto (delimitadas por aspas duplas sem quebra de linha) e identificadores válidos.
- **`Token` & `TokenType`** — Modelam cada elemento léxico, armazenando seu tipo específico, o lexema, a linha e a coluna.
- **`CompilationEngine`** — Implementa a gramática Jack por descida recursiva. Em expressões, os operadores binários são tratados como `term (op term)*` em um único nível, sem precedência, como o Jack e os gabaritos oficiais exigem. Chamadas de subrotina não geram tag própria: seus tokens aparecem diretamente dentro de `<term>` ou `<doStatement>`.
- **Tratamento de erros sintáticos** — O `CompilationEngine` lança `IllegalArgumentException` indicando a linha, a coluna, o tipo de token esperado e o token encontrado. Exemplo: `Erro sintatico na linha 1, coluna 27: Esperado SEMICOLON. Encontrado: RBRACE ('}')`.
- **`XmlTokenWriter`** e **`XmlParseWriter`** — Geram os XMLs, aplicando o escape de caracteres especiais XML (ex.: `<` vira `&lt;`, `>` vira `&gt;`, `&` vira `&amp;` e `"` vira `&quot;`).

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

Formato geral (argumentos passados ao `Main`): `Main [--tokens | --parse] <arquivo.jack> [saida.xml]`. O caminho opcional `saida.xml` pode ser informado apenas quando se escolhe um dos modos explicitamente; sem flag, são gerados os dois XMLs nos caminhos padrão.

| Modo | O que faz | Saída padrão |
|---|---|---|
| `--tokens` | Executa apenas o scanner | `<Nome>T.xml` |
| `--parse` | Executa scanner + parser | `<Nome>P.xml` |
| *(sem flag)* | Gera os dois arquivos | `<Nome>T.xml` e `<Nome>P.xml` |

#### Gerar o XML sintático de um arquivo
```bash
mvn compile exec:java "-Dexec.args=--parse src/test/resources/oficial/entrada/Square.jack"
```
> **Nome do arquivo de saída XML do parser:** `<Nome>P.xml`, gerado no mesmo diretório do `.jack` (ex.: `SquareP.xml`). Com `--parse`, também é possível informar um caminho de saída personalizado como segundo argumento após o arquivo de entrada, por exemplo: `"-Dexec.args=--parse src/test/resources/oficial/entrada/Square.jack target/SquareP.xml"`.

#### Gerar apenas os tokens
```bash
mvn compile exec:java "-Dexec.args=--tokens src/test/resources/Exemplo.jack"
```

#### Gerar os dois XMLs de uma vez
```bash
mvn compile exec:java "-Dexec.args=src/test/resources/Exemplo.jack"
```

Em caso de erro léxico ou sintático, a mensagem é exibida em `stderr` e o programa termina com código 1.

---

### 🔹 Executar os Testes Automatizados (JUnit 5)

```bash
mvn test
```

Os testes cobrem:

| Classe de Teste | Cobertura |
|---|---|
| `ScannerTest` | 10 testes: palavras-chave, identificadores, inteiros, 19 símbolos, divisão e comentários, strings, linhas, erros léxicos e EOF. |
| `ScannerCasosEspeciaisTest` | 8 testes: limites de inteiros, valores muito grandes, acentos em strings e comentários, identificadores inválidos, finais de linha Windows e strings com quebras `\n`/`\r`. |
| `CompilationEngineTest` | 4 testes: classes, declarações `static`/`field` e rejeição de estruturas inválidas. |
| `SubroutineTest` | 4 testes: funções, construtores, métodos, parâmetros, variáveis locais e `return`. |
| `ComandosExpressoesTest` | 7 testes: `let`, `if`, `while`, `do`, `return`, expressões e termos. |
| `ErrosSintaticosTest` | 9 testes: identificação de tokens inesperados, erros em expressões e declarações, EOF, comentários, CRLF e posição (linha e coluna), inclusive após caracteres UTF-8. |
| `CasosEspeciaisParserTest` | 10 testes: blocos aninhados, listas de argumentos, arrays, operadores, constantes, comentários, XML bem formado e rejeição de sintaxes inválidas. |
| `ValidacaoOficialTest` | 3 testes dinâmicos: compara `Main`, `Square` e `SquareGame` com os gabaritos `*P.xml` oficiais. |

**Total:** 55 casos de teste (52 testes JUnit convencionais e 3 testes dinâmicos de validação oficial). Para executar toda a suíte a partir de uma compilação limpa, use `mvn clean test`.

---

## ✅ Status de validação

### Project 10 — Léxico

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainT.xml` | ✅ Passou |
| `Square.jack` | `SquareT.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameT.xml` | ✅ Passou |

### Project 10 — Sintático

A comparação é automática, feita por `ValidacaoOficialTest`. Antes de comparar, o teste remove espaços nas pontas de cada linha e linhas em branco, o que elimina diferenças de indentação e de `LF`/`CRLF`. A ordem, a hierarquia de tags e os tokens são comparados integralmente. Os gabaritos sintáticos de referência estão na pasta `referencia`; as saídas `*P.xml` são geradas em `entrada` quando o programa é executado com `--parse` sem um caminho de saída alternativo.

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainP.xml` | ✅ Passou |
| `Square.jack` | `SquareP.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameP.xml` | ✅ Passou |

Para reproduzir a validação manualmente, gere as saídas com os arquivos de `src/test/resources/oficial/entrada/`:

```powershell
foreach ($nome in @("Main", "Square", "SquareGame")) {
    mvn compile exec:java "-Dexec.args=--parse src/test/resources/oficial/entrada/$nome.jack"
}
```

Depois compare os resultados com os arquivos em `src/test/resources/oficial/referencia/`:

```powershell
$base = "src/test/resources/oficial"
foreach ($nome in @("Main", "Square", "SquareGame")) {
    $gerado = (Get-Content "$base/entrada/${nome}P.xml" |
        ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }) -join "`n"
    $referencia = (Get-Content "$base/referencia/${nome}P.xml" |
        ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }) -join "`n"

    if ($gerado -ceq $referencia) { Write-Host "$nome : APROVADO" }
    else { Write-Host "$nome : DIFERENTE" }
}
```

O arquivo `Exemplo.jack` foi utilizado adicionalmente como teste manual local, gerando `ExemploT.xml`; ele não faz parte dos gabaritos oficiais.

---

## 🧗 Relato dos desafios da unidade

- **Strings e quebras de linha:** o scanner precisou tratar `\n` e `\r` dentro de strings e manter a contagem de linhas correta também dentro de comentários de bloco.
- **Identificadores e inteiros:** restringimos identificadores a ASCII e validamos o limite dos inteiros de Jack (`0..32767`), sem risco de overflow.
- **Compatibilidade com os gabaritos:** a diferença entre `LF` e `CRLF` gerava falsos negativos na comparação, resolvida com a normalização das linhas.
- **Expressões sem precedência:** o Jack não define precedência entre operadores binários, e os gabaritos oficiais esperam `term (op term)*` em um único nível. Tentar impor precedência quebraria a hierarquia de tags.
- **Chamadas de subrotina:** `x[i]`, `f()` e `a.f()` começam todos com um identificador. O parser olha um token à frente para decidir qual caso aplicar.
- **Tags que não existem:** chamadas de subrotina não geram tag própria na saída oficial. Já `parameterList`, `expressionList` e `statements` aparecem mesmo quando vazios.
- **Erros com localização:** para reportar linha e coluna, o `Scanner` passou a registrar essas posições nos tokens (inclusive após comentários e quebras de linha), e o parser passou a mostrar o token esperado e o encontrado. As mensagens atuais usam os nomes internos do enum, por exemplo `Esperado SEMICOLON` e `Encontrado: RBRACE`.

---

## 📌 Observações

- A formatação do XML léxico segue a convenção `<tokens> ... </tokens>`, e a do sintático segue a hierarquia oficial do Project 10 do Nand2Tetris.
- O tratamento de caracteres especiais de XML (`&`, `<`, `>`, `"`) é realizado na geração do XML para permitir comparação com os arquivos de referência.
- Artefatos gerados em tempo de compilação ou execução dentro da pasta `target/` não são versionados.
- Este repositório será a base das próximas unidades, que ampliarão o compilador para incluir geração de código VM.
- Referências de testes e formato XML: [Nand2Tetris — Project 10 (Square)](https://github.com/nand2tetris/projects/tree/main/projects/10/Square) e [gramática Jack](https://lambdageo-edu.github.io/book-compiladores-com-nand2tetris/apendices/a-gramatica-jack/).

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
