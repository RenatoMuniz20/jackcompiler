# 🧠 Jack Compiler — Compilador da Linguagem Jack para Código VM (Nand2Tetris)

Este projeto implementa o compilador completo para a linguagem **Jack**, proposta no curso e livro **Nand2Tetris**: análise léxica, análise sintática, tabela de símbolos e geração de código para a máquina virtual (Project 11).

Desenvolvido em **Java**, o sistema lê arquivos de código-fonte `.jack` (um arquivo ou uma pasta inteira), identifica os lexemas segundo a especificação formal da linguagem, verifica a conformidade com a gramática por *recursive descent parsing* e gera o código `.vm` de cada classe, pronto para ser executado no **VM Emulator**. As saídas XML padronizadas das unidades anteriores (`*T.xml` para tokens e `*P.xml` para a árvore sintática) continuam disponíveis para validação contra os gabaritos oficiais do curso:

- **Análise Léxica (`Scanner`):** Lê os arquivos `.jack`, ignora comentários (`//`, `/* */`, `/** */`) e espaços em branco, manipulando strings literais, identificadores, inteiros, símbolos e palavras-chave.
- **Estruturação de Tokens (`Token` e `TokenType`):** Representa os tokens com tipos internos específicos (como `CLASS`, `PLUS`, `NUMBER`, `IDENT` e `STRING`), seus lexemas e a posição de origem (linha e coluna).
- **Análise Sintática (`CompilationEngine`):** Parser descendente recursivo, com métodos para as principais construções da gramática Jack (`class`, `classVarDec`, `subroutineDec`, `parameterList`, `subroutineBody`, `varDec`, `statements`, `let`, `if`, `while`, `do`, `return`, `expression`, `term` e `expressionList`). Consome os tokens produzidos pelo `Scanner` e reporta erros sintáticos com linha, coluna, tipo e lexema do token encontrado.
- **Exportação XML (`XmlTokenWriter` e `XmlParseWriter`):** Serializam, respectivamente, a sequência de tokens (`<tokens> ... </tokens>`) e a árvore sintática (`<class> ... </class>`), com escape dos caracteres reservados (`<`, `>`, `&`, `"`).
- **Tabela de Símbolos (`SymbolTable`):** Mantém os dois escopos do Jack (classe e subrotina) e registra, para cada identificador, seu tipo, seu tipo de variável (`static`, `field`, `arg`, `var`) e seu índice.
- **Geração de Código (`VMWriter`):** Emite os comandos da VM (`push`, `pop`, aritméticos, `label`, `goto`, `if-goto`, `call`, `function` e `return`), um por linha, conforme a especificação do capítulo 7 do livro.
- **Compilação em Lote:** Ao receber uma pasta, o compilador processa todos os `.jack` dela (sem descer em subpastas) e gera um `.vm` por classe na mesma pasta.
- **Validação e Testes:** Testes automatizados com JUnit 5 para o scanner, o parser, a tabela de símbolos e o gerador de código, incluindo a comparação automática da saída do parser com os gabaritos oficiais e a compilação em lote dos seis programas do Project 11.

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
│   │               ├── Main.java                          🔹 Ponto de entrada CLI (--vm, --tokens, --parse, --all; arquivo ou pasta)
│   │               │
│   │               ├── lexer/                             🔹 Núcleo da análise léxica
│   │               │   ├── TokenType.java                 🔹 Enum dos tipos de token da especificação Jack
│   │               │   ├── Token.java                     🔹 Modelo de dados do token (tipo, lexema, linha, coluna)
│   │               │   └── Scanner.java                   🔹 Analisador léxico / tokenizador
│   │               │
│   │               ├── parser/                            🔹 Núcleo da análise sintática
│   │               │   └── CompilationEngine.java         🔹 Parser descendente recursivo, integrado à geração de código VM
│   │               │
│   │               ├── codegen/                           🔹 Núcleo da geração de código
│   │               │   ├── SymbolTable.java               🔹 Tabela de símbolos (escopos de classe e subrotina)
│   │               │   └── VMWriter.java                  🔹 Emissor dos comandos da VM
│   │               │
│   │               └── xml/                               🔹 Geração e exportação dos dados
│   │                   ├── XmlTokenWriter.java            🔹 Serializador de tokens para XML (*T.xml)
│   │                   └── XmlParseWriter.java            🔹 Serializador da árvore sintática para XML (*P.xml)
│   │
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── jackcompiler/
│       │           ├── codegen/
│       │           │   ├── SymbolTableTest.java           🔹 Escopos, índices e contagem de variáveis
│       │           │   ├── VMWriterTest.java              🔹 Emissão isolada de cada comando VM
│       │           │   ├── GeneratorCodeTest.java         🔹 Código VM gerado para construções Jack
│       │           │   └── CompilacaoLoteTest.java        🔹 Compilação em lote dos programas do Project 11
│       │           │
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
│           └── oficial/                                   🔹 Casos de teste do Nand2Tetris (Projects 10 e 11)
│               ├── entrada/                               🔹 Fontes .jack, uma pasta por programa
│               │   ├── ArrayTest/                         🔹 Project 10
│               │   ├── ExpressionLessSquare/              🔹 Project 10
│               │   ├── Square/                            🔹 Projects 10 e 11
│               │   ├── Seven/                             🔹 Project 11 — Etapa 1
│               │   ├── ConvertToBin/                      🔹 Project 11 — Etapa 1
│               │   ├── Average/                           🔹 Project 11 — Etapa 2
│               │   ├── Pong/                              🔹 Project 11 — Etapa 2
│               │   └── ComplexArrays/                     🔹 Project 11 — Etapa 2
│               └── referencia/                            🔹 Gabaritos oficiais *T.xml e *P.xml (Project 10)
│                   ├── ArrayTest/
│                   ├── ExpressionLessSquare/
│                   └── Square/
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

### Geração de código VM

Na geração de código, o `CompilationEngine` não cria uma árvore intermediária: à medida que reconhece cada regra da gramática, ele consulta a `SymbolTable` e chama o `VMWriter` para emitir os comandos correspondentes.

```
Arquivo .jack  (ou pasta com vários .jack)
│
▼
┌────────────────────────────────────────────────────────┐
│  1. SCANNER  →  2. COMPILATION ENGINE                  │
│                                                        │
│  Mesmo fluxo da análise sintática descrito acima.      │
└───────────────────────────┬────────────────────────────┘
│
▼
┌────────────────────────────────────────────────────────┐
│  3. SYMBOL TABLE                                       │
│                                                        │
│  Escopo de classe: static e field                      │
│  Escopo de subrotina: argumentos e var (locais)        │
│  Cada nome → (tipo, tipo de variável, índice)          │
└───────────────────────────┬────────────────────────────┘
│
▼
┌────────────────────────────────────────────────────────┐
│  4. VM WRITER                                          │
│                                                        │
│  push / pop / add / call / label / if-goto / return …  │
└───────────────────────────┬────────────────────────────┘
│
▼
Um arquivo <Classe>.vm por classe, na mesma pasta do .jack
```

Mapeamento dos tipos de variável para os segmentos da VM:

| Tipo de variável em Jack | Segmento VM |
|---|---|
| `static` | `static` |
| `field` | `this` |
| parâmetro (`arg`) | `argument` |
| `var` (local) | `local` |

Principais regras de tradução:

- **Constantes:** inteiros viram `push constant n`; `true` vira `push constant 0` seguido de `not` (valor `-1`); `false` e `null` viram `push constant 0`.
- **Operadores:** `+`, `-`, `&`, `|`, `<`, `>`, `=` viram comandos aritméticos da VM; `*` e `/` viram `call Math.multiply 2` e `call Math.divide 2`; os unários `-` e `~` viram `neg` e `not`.
- **Strings constantes:** `String.new(tamanho)` seguido de um `String.appendChar(c)` por caractere.
- **`if` e `while`:** recebem rótulos únicos numerados (`IF_TRUE0`, `IF_FALSE0`, `IF_END0`, `WHILE_EXP0`, `WHILE_END0`...), inclusive quando estão aninhados.
- **`do`:** descarta o valor de retorno com `pop temp 0`. Uma subrotina `void` retorna `push constant 0` e `return`.
- **Construtores:** alocam o objeto com `push constant <nº de fields>`, `call Memory.alloc 1` e `pop pointer 0`.
- **Métodos:** ao entrar, ancoram `this` com `push argument 0` e `pop pointer 0`; por isso os parâmetros declarados começam em `argument 1`.
- **Chamadas de método:** em `obj.metodo(...)`, o objeto é empilhado como primeiro argumento e a chamada vira `call <Tipo de obj>.metodo n+1`; em `metodo(...)` sem prefixo, empilha-se `pointer 0` (o próprio objeto).
- **Arrays:** o endereço `base + índice` é calculado e acessado via `pointer 1` / `that 0`. Em `let a[i] = expr`, o valor de `expr` é guardado em `temp 0` antes de ajustar `pointer 1`, para não perder o endereço quando `expr` também acessa arrays.

Exemplo (trecho de código Jack e o `.vm` gerado):

```
var Array a;  var Square s;  var int i;      function Main.main 3
...                                           ...
while (i < 3) {                               label WHILE_EXP0
                                              push local 2
                                              push constant 3
                                              lt
                                              not
                                              if-goto WHILE_END0
    let a[i] = i * 2;                         push local 2
                                              push local 0
                                              add
                                              push local 2
                                              push constant 2
                                              call Math.multiply 2
                                              pop temp 0
                                              pop pointer 1
                                              push temp 0
                                              pop that 0
    let i = i + 1;                            push local 2
                                              push constant 1
                                              add
                                              pop local 2
}                                             goto WHILE_EXP0
                                              label WHILE_END0
do s.moveUp();                                push local 1
                                              call Square.moveUp 1
                                              pop temp 0
```

- **Erros semânticos:** na geração de VM, o uso de uma variável não declarada interrompe a compilação com a linha e a coluna do identificador. Exemplo: `Erro semantico na linha 3, coluna 9: variavel nao declarada 'x'`.

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

O programa aceita **um arquivo `.jack` ou uma pasta**. Com uma pasta, todos os `.jack` dela são compilados (sem descer em subpastas) e é gerado um `.vm` por classe **na mesma pasta**, de onde o VM Emulator carrega o programa. Não é preciso copiar os `.vm` do sistema operacional: o VM Emulator já tem o SO embutido.

Formato geral (argumentos passados ao `Main`): `Main [--vm | --tokens | --parse | --all] <arquivo.jack | diretorio> [saida]`. Sem flag, o modo padrão é `--vm`. Com um arquivo, `saida` é o caminho do arquivo gerado; com uma pasta, `saida` é a pasta onde os arquivos serão gravados.

| Modo | O que faz | Saída padrão |
|---|---|---|
| `--vm` *(padrão)* | Scanner + parser + geração de código | `<Nome>.vm` |
| `--tokens` | Executa apenas o scanner | `<Nome>T.xml` |
| `--parse` | Executa scanner + parser | `<Nome>P.xml` |
| `--all` | Gera os três arquivos | `<Nome>T.xml`, `<Nome>P.xml` e `<Nome>.vm` |

#### Compilar uma pasta inteira (Project 11)

Via JAR executável (após `mvn clean package`):
```bash
java -jar target/jackcompiler-1.0-SNAPSHOT.jar src/test/resources/oficial/entrada/Pong
```
Ou via Maven:
```bash
mvn compile exec:java "-Dexec.args=src/test/resources/oficial/entrada/Pong"
```
> Gera `Ball.vm`, `Bat.vm`, `Main.vm` e `PongGame.vm` dentro de `src/test/resources/oficial/entrada/Pong/`.

#### Compilar um único arquivo
```bash
java -jar target/jackcompiler-1.0-SNAPSHOT.jar src/test/resources/oficial/entrada/Seven/Main.jack
```
> Gera `Main.vm` ao lado do `Main.jack`.

#### Executar no VM Emulator

1. Abra o **VM Emulator** (pacote de ferramentas do Nand2Tetris).
2. Use *File → Load Program* e selecione a **pasta** do programa (ex.: `Pong/`).
3. Escolha *No animation* e clique em *Run*.

#### Gerar o XML sintático de um arquivo
```bash
mvn compile exec:java "-Dexec.args=--parse src/test/resources/oficial/entrada/Square/Square.jack"
```
> **Nome do arquivo de saída XML do parser:** `<Nome>P.xml`, gerado no mesmo diretório do `.jack` (ex.: `SquareP.xml`). Com `--parse`, também é possível informar um caminho de saída personalizado como segundo argumento após o arquivo de entrada, por exemplo: `"-Dexec.args=--parse src/test/resources/oficial/entrada/Square/Square.jack target/SquareP.xml"`.

#### Gerar apenas os tokens
```bash
mvn compile exec:java "-Dexec.args=--tokens src/test/resources/Exemplo.jack"
```

#### Gerar os dois XMLs e o código VM de uma vez
```bash
mvn compile exec:java "-Dexec.args=--all src/test/resources/Exemplo.jack"
```

Em caso de erro léxico, sintático ou semântico (variável não declarada), a mensagem é exibida em `stderr` e o programa termina com código 1.

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
| `ValidacaoOficialTest` | 14 testes dinâmicos: compara os 7 arquivos de `Square`, `ExpressionLessSquare` e `ArrayTest` com os gabaritos oficiais `*T.xml` e `*P.xml`. |
| `SymbolTableTest` | 3 testes: definição e resolução de variáveis nos dois escopos, reinício do escopo de subrotina e rejeição de variável duplicada. |
| `VMWriterTest` | 3 testes: emissão isolada dos comandos `push`, `pop`, aritméticos, rótulos, `call`, `function` e `return`. |
| `GeneratorCodeTest` | 6 testes: código VM de expressões, `let`, `if`/`else`, `while`, strings e arrays, e erro para variável não declarada. |
| `CompilacaoLoteTest` | 6 testes: compila as pastas `Seven`, `ConvertToBin`, `Square`, `Average`, `Pong` e `ComplexArrays` e confere se cada classe gerou seu `.vm` na mesma pasta. |

**Total:** 84 casos de teste (70 testes JUnit convencionais e 14 testes dinâmicos de validação oficial). Para executar toda a suíte a partir de uma compilação limpa, use `mvn clean test`.

---

## ✅ Status de validação

### Project 10 — Léxico

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainT.xml` | ✅ Passou |
| `Square.jack` | `SquareT.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameT.xml` | ✅ Passou |
| `ExpressionLessSquare/*.jack` | `MainT.xml`, `SquareT.xml`, `SquareGameT.xml` | ✅ Passou |
| `ArrayTest/Main.jack` | `MainT.xml` | ✅ Passou |

### Project 10 — Sintático

A comparação é automática, feita por `ValidacaoOficialTest`. Antes de comparar, o teste remove espaços nas pontas de cada linha e linhas em branco, o que elimina diferenças de indentação e de `LF`/`CRLF`. A ordem, a hierarquia de tags e os tokens são comparados integralmente. Os gabaritos sintáticos de referência estão na pasta `referencia`; as saídas `*P.xml` são geradas em `entrada` quando o programa é executado com `--parse` sem um caminho de saída alternativo.

| Arquivo de Entrada | Gabarito Esperado | Validação |
|---|---|---|
| `Main.jack` | `MainP.xml` | ✅ Passou |
| `Square.jack` | `SquareP.xml` | ✅ Passou |
| `SquareGame.jack` | `SquareGameP.xml` | ✅ Passou |
| `ExpressionLessSquare/*.jack` | `MainP.xml`, `SquareP.xml`, `SquareGameP.xml` | ✅ Passou |
| `ArrayTest/Main.jack` | `MainP.xml` | ✅ Passou |

Para reproduzir a validação manualmente, gere as saídas com os arquivos de `src/test/resources/oficial/entrada/Square/`:

```powershell
foreach ($nome in @("Main", "Square", "SquareGame")) {
    mvn compile exec:java "-Dexec.args=--parse src/test/resources/oficial/entrada/Square/$nome.jack"
}
```

Depois compare os resultados com os arquivos em `src/test/resources/oficial/referencia/Square/`:

```powershell
$base = "src/test/resources/oficial"
foreach ($nome in @("Main", "Square", "SquareGame")) {
    $gerado = (Get-Content "$base/entrada/Square/${nome}P.xml" |
        ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }) -join "`n"
    $referencia = (Get-Content "$base/referencia/Square/${nome}P.xml" |
        ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }) -join "`n"

    if ($gerado -ceq $referencia) { Write-Host "$nome : APROVADO" }
    else { Write-Host "$nome : DIFERENTE" }
}
```

O arquivo `Exemplo.jack` foi utilizado adicionalmente como teste manual local, gerando `ExemploT.xml`; ele não faz parte dos gabaritos oficiais.

### Project 11 — Geração de código VM

Não há arquivos `.cmp` no Project 11: a validação é feita compilando a pasta de cada programa e executando o resultado no **VM Emulator**. O código gerado não precisa ser idêntico ao do compilador oficial (os nomes dos rótulos, por exemplo, são diferentes); o que importa é o programa funcionar.

| Etapa | Programa | Arquivos | Como conferir | Validação |
|---|---|---|---|---|
| 1 | **Seven** | `Main.jack` | Imprime `7` no canto da tela | ✅ Passou |
| 1 | **ConvertToBin** | `Main.jack` | Com um valor em `RAM[8000]`, `RAM[8001..8016]` recebem os bits do número (0 ou 1) | ✅ Passou |
| 2 | **Square** | `Main.jack`, `Square.jack`, `SquareGame.jack` | O quadrado se move com as setas e muda de tamanho com `z`/`x` | ✅ Passou |
| 2 | **Average** | `Main.jack` | Pede N números e imprime a média | ✅ Passou |
| 2 | **Pong** 🏆 | `Main.jack`, `PongGame.jack`, `Ball.jack`, `Bat.jack` | Bola, raquete e placar funcionando | ✅ Passou |
| 2 | **ComplexArrays** | `Main.jack` | Cada teste imprime o valor esperado e o obtido, e os dois são iguais | ✅ Passou |

O fim da Etapa 1 (`Seven` e `ConvertToBin` funcionando) está marcado no repositório com a tag **`etapa1`**.

---

## 🧗 Relato dos desafios e aprendizados

### Unidade 1 — Análise léxica e sintática

- **Strings e quebras de linha:** o scanner precisou tratar `\n` e `\r` dentro de strings e manter a contagem de linhas correta também dentro de comentários de bloco.
- **Identificadores e inteiros:** restringimos identificadores a ASCII e validamos o limite dos inteiros de Jack (`0..32767`), sem risco de overflow.
- **Compatibilidade com os gabaritos:** a diferença entre `LF` e `CRLF` gerava falsos negativos na comparação, resolvida com a normalização das linhas.
- **Expressões sem precedência:** o Jack não define precedência entre operadores binários, e os gabaritos oficiais esperam `term (op term)*` em um único nível. Tentar impor precedência quebraria a hierarquia de tags.
- **Chamadas de subrotina:** `x[i]`, `f()` e `a.f()` começam todos com um identificador. O parser olha um token à frente para decidir qual caso aplicar.
- **Tags que não existem:** chamadas de subrotina não geram tag própria na saída oficial. Já `parameterList`, `expressionList` e `statements` aparecem mesmo quando vazios.
- **Erros com localização:** para reportar linha e coluna, o `Scanner` passou a registrar essas posições nos tokens (inclusive após comentários e quebras de linha), e o parser passou a mostrar o token esperado e o encontrado. As mensagens atuais usam os nomes internos do enum, por exemplo `Esperado SEMICOLON` e `Encontrado: RBRACE`.

### Unidade 2 — Geração de código VM

- **`this` como argumento 0:** em um método, `argument 0` é o próprio objeto. Para os parâmetros declarados começarem em `argument 1`, registramos `this` na tabela de símbolos antes da lista de parâmetros.
- **Chamadas de método x funções:** em `a.f()`, o compilador precisa saber se `a` é uma variável (chamada de método, com o objeto empilhado e o tipo de `a` como nome da classe) ou o nome de uma classe (função ou construtor). A tabela de símbolos resolve essa dúvida.
- **Ordem em `let a[i] = expr`:** se `expr` também acessa arrays, ela altera `pointer 1`. Por isso o valor é guardado em `temp 0` e o endereço só é ancorado em `pointer 1` depois que a expressão termina. O `ComplexArrays` foi o programa que mais exigiu atenção nesse ponto.
- **Rótulos únicos:** `if` e `while` aninhados precisam de rótulos diferentes; usamos contadores separados para cada comando.
- **Compilação de pastas:** o VM Emulator carrega o programa a partir da pasta, então os `.vm` precisam ser gerados ao lado dos `.jack`, um por classe.
- **Variáveis não declaradas:** inicialmente, um identificador inexistente era ignorado e gerava um `.vm` incompleto sem nenhum aviso. Passamos a reportar o erro com linha e coluna.
- **Aprendizados:** entendemos na prática como construções de alto nível (objetos, métodos, arrays e strings) são reduzidas a uma pilha e a poucos segmentos de memória, e como o compilador depende da tabela de símbolos para decidir o código de cada identificador.

---

## 📌 Observações

- A formatação do XML léxico segue a convenção `<tokens> ... </tokens>`, e a do sintático segue a hierarquia oficial do Project 10 do Nand2Tetris.
- O tratamento de caracteres especiais de XML (`&`, `<`, `>`, `"`) é realizado na geração do XML para permitir comparação com os arquivos de referência.
- Artefatos gerados em tempo de compilação ou execução dentro da pasta `target/` não são versionados.
- Referências de testes e formato XML: [Nand2Tetris — Project 10 (Square)](https://github.com/nand2tetris/projects/tree/main/projects/10/Square) e [gramática Jack](https://lambdageo-edu.github.io/book-compiladores-com-nand2tetris/apendices/a-gramatica-jack/).
- Referência de compilação em lote: [jackcompiler-java](https://github.com/lambdageo-edu/jackcompiler-java).

---

## 🤖 Uso de Inteligência Artificial

- **Ferramentas e modelos:** Gemini (Google), modelo Gemini 3.1 Pro; e Claude (Anthropic), modelo Claude Opus 5.5, usado pelo Claude Code.
- **Finalidade:** tirar dúvidas pontuais sobre a especificação da VM e revisar partes do código e do README.
- **Validação:** todo o código foi implementado, integrado e revisado pela dupla, e validado com os testes automatizados e com a execução dos programas do Project 11 no VM Emulator.

---

## 🎥 Vídeo de demonstração

🔗 **Link:** *(https://youtu.be/Y61FsQNilkc?is=NGEUENCxy6b5urQx)*

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
