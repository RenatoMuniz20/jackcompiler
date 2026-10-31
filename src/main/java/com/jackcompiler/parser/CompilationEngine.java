package com.jackcompiler.parser;

import com.jackcompiler.codegen.SymbolTable;
import com.jackcompiler.codegen.SymbolTable.Kind;
import com.jackcompiler.codegen.SymbolTable.Symbol;
import com.jackcompiler.codegen.VMWriter;
import com.jackcompiler.codegen.VMWriter.Command;
import com.jackcompiler.codegen.VMWriter.Segment;
import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.lexer.Token;
import com.jackcompiler.lexer.TokenType;
import com.jackcompiler.xml.XmlParseWriter;

public class CompilationEngine {

    private final Scanner scanner;
    private final XmlParseWriter xml;
    private final SymbolTable symbolTable;
    private final VMWriter vmWriter;

    private Token atual;
    private Token proximo;
    private String className;

    public CompilationEngine(Scanner scanner) {
        this.scanner = scanner;
        this.xml = new XmlParseWriter();
        this.symbolTable = new SymbolTable();
        this.vmWriter = new VMWriter();
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

    private Token consume(TokenType tipo) {
        if (!match(tipo)) {
            throw erro("Esperado " + tipo);
        }
        Token consumido = atual;
        xml.escreverToken(atual);
        advance();
        return consumido;
    }

    private IllegalArgumentException erro(String mensagem) {
        return new IllegalArgumentException(
            "Erro sintatico na linha " + atual.getLine()
            + ", coluna " + atual.getColumn()
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
        className = consume(TokenType.IDENT).getLexeme();
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
        Kind kind;
        if (match(TokenType.STATIC)) {
            consume(TokenType.STATIC);
            kind = Kind.STATIC;
        } else {
            consume(TokenType.FIELD);
            kind = Kind.FIELD;
        }
        String type = consumeType();
        String name = consume(TokenType.IDENT).getLexeme();
        symbolTable.define(name, type, kind);

        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, kind);
        }
        consume(TokenType.SEMICOLON);
        xml.fechar("classVarDec");
    }

    private String consumeType() {
        TokenType tipo = peek().getType();
        if (tipo == TokenType.INT || tipo == TokenType.CHAR
                || tipo == TokenType.BOOLEAN || tipo == TokenType.IDENT) {
            return consume(tipo).getLexeme();
        } else {
            throw erro("Tipo de variavel esperado");
        }
    }

    public String compileVm() {
        compile();
        return vmWriter.vmOutput();
    }

    public String getVm() {
        return vmWriter.vmOutput();
    }

    // subroutineDec: ('constructor' | 'function' | 'method')
    // ('void' | type) subroutineName '(' parameterList ')' subroutineBody
    private void compileSubroutineDec() {
        xml.abrir("subroutineDec");
        symbolTable.startSubroutine();

        TokenType subroutineType;
        if (match(TokenType.CONSTRUCTOR)) {
            subroutineType = consume(TokenType.CONSTRUCTOR).getType();
        } else if (match(TokenType.FUNCTION)) {
            subroutineType = consume(TokenType.FUNCTION).getType();
        } else {
            subroutineType = consume(TokenType.METHOD).getType();
            symbolTable.define("this", className, Kind.ARG);
        }

        if (match(TokenType.VOID)) {
            consume(TokenType.VOID);
        } else {
            consumeType();
        }

        String subroutineName = consume(TokenType.IDENT).getLexeme();
        String functionName = className + "." + subroutineName;

        consume(TokenType.LPAREN);
        compileParameterList();
        consume(TokenType.RPAREN);
        compileSubroutineBody(functionName, subroutineType);
        xml.fechar("subroutineDec");
    }

    // parameterList: (type varName (',' type varName)*)?
    private void compileParameterList() {
        xml.abrir("parameterList");
        if (inicioTipo()) {
            String type = consumeType();
            String name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, Kind.ARG);
            while (match(TokenType.COMMA)) {
                consume(TokenType.COMMA);
                type = consumeType();
                name = consume(TokenType.IDENT).getLexeme();
                symbolTable.define(name, type, Kind.ARG);
            }
        }
        xml.fechar("parameterList");
    }

    private boolean inicioTipo() {
        return match(TokenType.INT) || match(TokenType.CHAR)
                || match(TokenType.BOOLEAN) || match(TokenType.IDENT);
    }

    // subroutineBody: '{' varDec* statements '}'
    private void compileSubroutineBody(String functionName, TokenType subroutineType) {
        xml.abrir("subroutineBody");
        consume(TokenType.LBRACE);
        while (match(TokenType.VAR)) {
            compileVarDec();
        }

        int nLocals = symbolTable.varCount(Kind.VAR);
        vmWriter.writeFunction(functionName, nLocals);

        if (subroutineType == TokenType.CONSTRUCTOR) {
            vmWriter.writePush(Segment.CONST, symbolTable.varCount(Kind.FIELD));
            vmWriter.writeCall("Memory.alloc", 1);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if (subroutineType == TokenType.METHOD) {
            vmWriter.writePush(Segment.ARG, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        }

        compileStatements();
        consume(TokenType.RBRACE);
        xml.fechar("subroutineBody");
    }

    // varDec: 'var' type varName (',' varName)* ';'
    private void compileVarDec() {
        xml.abrir("varDec");
        consume(TokenType.VAR);
        String type = consumeType();
        String name = consume(TokenType.IDENT).getLexeme();
        symbolTable.define(name, type, Kind.VAR);
        while (match(TokenType.COMMA)) {
            consume(TokenType.COMMA);
            name = consume(TokenType.IDENT).getLexeme();
            symbolTable.define(name, type, Kind.VAR);
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
