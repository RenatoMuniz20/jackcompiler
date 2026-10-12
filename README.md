# jackcompiler
Compilador para a linguagem Jack desenvolvido na disciplina de Compiladores.

## Testes e Validação

O analisador léxico possui testes automatizados com JUnit 5 para verificar o reconhecimento dos tokens, comentários, strings, números inteiros e tratamento de erros.

Para executar os testes automatizados:

```powershell
mvn clean test
```

Também realizamos a validação com os três programas oficiais do Nand2Tetris — Project 10:

| Arquivo testado | Resultado |
|---|---|
| Main.jack | Aprovado |
| Square.jack | Aprovado |
| SquareGame.jack | Aprovado |

Os arquivos XML gerados foram comparados com os respectivos `*T.xml` oficiais por meio do comando `Compare-Object` do PowerShell. Não foram encontradas diferenças no conteúdo das linhas.

Os arquivos oficiais estão disponíveis em:
https://github.com/nand2tetris/projects/tree/main/projects/10/Square