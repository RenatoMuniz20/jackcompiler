package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.lexer.Token;
import com.jackcompiler.lexer.TokenType;
import com.jackcompiler.xml.XmlParseWriter;

public class CompilationEngine {

    private final Scanner scanner;
    private final XmlParseWriter xml;

    private Token atual;
    private Token proximo;

    public CompilationEngine(Scanner scanner) {
        this.scanner = scanner;
        this.xml = new XmlParseWriter();
        this.atual = scanner.nextToken();
        this.proximo = scanner.nextToken();
    }

    private void advance() {
        atual = proximo;
        proximo = scanner.nextToken();
    }

    private Token peek() {
        return atual;
    }

    private boolean match(TokenType tipo) {
        return peek().getType() == tipo;
    }

    private void consume(TokenType tipo) {
        if (!match(tipo)) {
            throw erro("Esperado " + tipo);
        }
        xml.escreverToken(atual);
        advance();
    }

    private IllegalArgumentException erro(String mensagem) {
        return new IllegalArgumentException(
            "Erro sintatico na linha " + atual.getLine()
            + ": " + mensagem
            + ". Encontrado: " + atual.getType()
            + " ('" + atual.getLexeme() + "')"
        );
    }

    public String compile() {
        compileClass();
        if (!match(TokenType.EOF)) {
            throw erro("Esperado fim do arquivo");
        }
        return xml.getXml();
    }

    // class: 'class' className '{' classVarDec* subroutineDec* '}'
    private void compileClass() {
        xml.abrir("class");
        consume(TokenType.CLASS);
        consume(TokenType.IDENT);
        consume(TokenType.LBRACE);

        while (match(TokenType.STATIC) || match(TokenType.FIELD)) {
            compileClassVarDec();
        }
        while (match(TokenType.CONSTRUCTOR)
                || match(TokenType.FUNCTION)
                || match(TokenType.METHOD)) {
            compileSubroutineDec();
        }

        consume(TokenType.RBRACE);
        xml.fechar("class");
    }

    // classVarDec: ('static' | 'field') type varName (',' varName)* ';'
    private void compileClassVarDec() {
        xml.abrir("classVarDec");
        if (match(TokenType.STATIC)) {
            consume(TokenType.STATIC);
        } else {
            consume(TokenType.FIELD);
        }
        consumeType();
        consume(TokenType.IDENT);
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            consume(TokenType.IDENT);
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("classVarDec");
    }

    private void consumeType() {
        TokenType tipo = peek().getType();
        if (tipo == TokenType.INT || tipo == TokenType.CHAR
                || tipo == TokenType.BOOLEAN || tipo == TokenType.IDENT) {
            consume(tipo);
        } else {
            throw erro("Tipo de variavel esperado");
        }
    }

    // subroutineDec: ('constructor' | 'function' | 'method')
    // ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    private void compileSubroutineDec() {
        xml.abrir("subroutineDec");
        if (match(TokenType.CONSTRUCTOR)) {
            consume(TokenType.CONSTRUCTOR);
        } else if (match(TokenType.FUNCTION)) {
            consume(TokenType.FUNCTION);
        } else {
            consume(TokenType.METHOD);
        }
        if (match(TokenType.VOID)) {
            consume(TokenType.VOID);
        } else {
            consumeType();
        }
        consume(TokenType.IDENT);
        consume(TokenType.LPAREN);
        compileParameterList();
        consume(TokenType.RPAREN);
        compileSubroutineBody();
        xml.fechar("subroutineDec");
    }

    // parameterList: (type varName (',' type varName)*)?
    private void compileParameterList() {
        xml.abrir("parameterList");
        if (inicioTipo()) {
            consumeType();
            consume(TokenType.IDENT);
            while (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                consumeType();
                consume(TokenType.IDENT);
            }
        }
        xml.fechar("parameterList");
    }

    private boolean inicioTipo() {
        return match(TokenType.INT) || match(TokenType.CHAR)
                || match(TokenType.BOOLEAN) || match(TokenType.IDENT);
    }

    // subroutineBody: '{' varDec* statements '}'
    private void compileSubroutineBody() {
        xml.abrir("subroutineBody");
        consume(TokenType.LBRACE);
        while (match(TokenType.VAR)) {
            compileVarDec();
        }
        compileStatements();
        consume(TokenType.RBRACE);
        xml.fechar("subroutineBody");
    }

    // varDec: 'var' type varName (',' varName)* ';'
    private void compileVarDec() {
        xml.abrir("varDec");
        consume(TokenType.VAR);
        consumeType();
        consume(TokenType.IDENT);
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            consume(TokenType.IDENT);
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("varDec");
    }

    // statements: statement* (let | if | while | do | return)
    private void compileStatements() {
        xml.abrir("statements");
        while (match(TokenType.LET) || match(TokenType.IF)
                || match(TokenType.WHILE) || match(TokenType.DO)
                || match(TokenType.RETURN)) {
            if (match(TokenType.LET)) {
                compileLet();
            } else if (match(TokenType.IF)) {
                compileIf();
            } else if (match(TokenType.WHILE)) {
                compileWhile();
            } else if (match(TokenType.DO)) {
                compileDo();
            } else {
                compileReturn();
            }
        }
        if (!match(TokenType.RBRACE)) {
            throw erro("Esperado comando ou fechamento do bloco");
        }
        xml.fechar("statements");
    }

    // letStatement: 'let' varName ('[' expression ']')? '=' expression ';'
    private void compileLet() {
        xml.abrir("letStatement");
        consume(TokenType.LET);
        consume(TokenType.IDENT);
        if (match(TokenType.LBRACKET)) {
            consume(TokenType.LBRACKET);
            compileExpression();
            consume(TokenType.RBRACKET);
        }
        consume(TokenType.EQ);
        compileExpression();
        consume(TokenType.SEMICOLON);
        xml.fechar("letStatement");
    }

    // ifStatement: 'if' '(' expression ')' '{' statements '}'
    //              ('else' '{' statements '}')?
    private void compileIf() {
        xml.abrir("ifStatement");
        consume(TokenType.IF);
        consume(TokenType.LPAREN);
        compileExpression();
        consume(TokenType.RPAREN);
        consume(TokenType.LBRACE);
        compileStatements();
        consume(TokenType.RBRACE);
        if (match(TokenType.ELSE)) {
            consume(TokenType.ELSE);
            consume(TokenType.LBRACE);
            compileStatements();
            consume(TokenType.RBRACE);
        }
        xml.fechar("ifStatement");
    }

    // whileStatement: 'while' '(' expression ')' '{' statements '}'
    private void compileWhile() {
        xml.abrir("whileStatement");
        consume(TokenType.WHILE);
        consume(TokenType.LPAREN);
        compileExpression();
        consume(TokenType.RPAREN);
        consume(TokenType.LBRACE);
        compileStatements();
        consume(TokenType.RBRACE);
        xml.fechar("whileStatement");
    }

    // doStatement: 'do' subroutineCall ';'
    private void compileDo() {
        xml.abrir("doStatement");
        consume(TokenType.DO);
        compileSubroutineCall();
        consume(TokenType.SEMICOLON);
        xml.fechar("doStatement");
    }

    // returnStatement: 'return' expression? ';'
    private void compileReturn() {
        xml.abrir("returnStatement");
        consume(TokenType.RETURN);
        if (!match(TokenType.SEMICOLON)) {
            compileExpression();
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("returnStatement");
    }

    // expression: term (op term)*
    // Jack nao define precedencia diferente para operadores binarios.
    private void compileExpression() {
        xml.abrir("expression");
        compileTerm();
        while (operadorBinario(peek().getType())) {
            consume(peek().getType());
            compileTerm();
        }
        xml.fechar("expression");
    }

    private boolean operadorBinario(TokenType tipo) {
        return switch (tipo) {
            case PLUS, MINUS, ASTERISK, SLASH, AND, OR, LT, GT, EQ -> true;
            default -> false;
        };
    }

    // term: integerConstant | stringConstant | keywordConstant
    //      | varName | varName '[' expression ']'
    //      | subroutineCall | '(' expression ')' | unaryOp term
    private void compileTerm() {
        xml.abrir("term");
        TokenType tipo = peek().getType();
        switch (tipo) {
            case NUMBER, STRING, TRUE, FALSE, NULL, THIS -> consume(tipo);
            case IDENT -> {
                if (proximo.getType() == TokenType.LPAREN
                        || proximo.getType() == TokenType.DOT) {
                    compileSubroutineCall();
                } else {
                    consume(TokenType.IDENT);
                    if (match(TokenType.LBRACKET)) {
                        consume(TokenType.LBRACKET);
                        compileExpression();
                        consume(TokenType.RBRACKET);
                    }
                }
            }
            case LPAREN -> {
                consume(TokenType.LPAREN);
                compileExpression();
                consume(TokenType.RPAREN);
            }
            case MINUS, NOT -> {
                consume(tipo);
                compileTerm();
            }
            default -> throw erro("Termo esperado");
        }
        xml.fechar("term");
    }

    // subroutineCall: subroutineName '(' expressionList ')'
    //               | (className | varName) '.' subroutineName '(' expressionList ')'
    private void compileSubroutineCall() {
        consume(TokenType.IDENT);
        if (match(TokenType.DOT)) {
            consume(TokenType.DOT);
            consume(TokenType.IDENT);
        }
        consume(TokenType.LPAREN);
        compileExpressionList();
        consume(TokenType.RPAREN);
    }

    // expressionList: (expression (',' expression)*)?
    private void compileExpressionList() {
        xml.abrir("expressionList");
        if (!match(TokenType.RPAREN)) {
            compileExpression();
            while (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                compileExpression();
            }
        }
        xml.fechar("expressionList");
    }
}
