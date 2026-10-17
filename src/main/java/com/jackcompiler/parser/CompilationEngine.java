
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

    // Avanca para o proximo token
    private void advance() {
        atual = proximo;
        proximo = scanner.nextToken();
    }

    // Consulta o token atual sem consumi-lo
    private Token peek() {
        return atual;
    }

    // Verifica se o token atual tem o tipo esperado
    private boolean match(TokenType tipo) {
        return peek().getType() == tipo;
    }

    // Exige um token, escreve no XML e avanca
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

    // Ponto de entrada da analise sintatica
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

    // classVarDec: ('static'|'field') type varName (',' varName)* ';'
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

    // type: 'int' | 'char' | 'boolean' | className
    private void consumeType() {
        TokenType tipo = peek().getType();

        if (tipo == TokenType.INT
                || tipo == TokenType.CHAR
                || tipo == TokenType.BOOLEAN
                || tipo == TokenType.IDENT) {

            consume(tipo);
        } else {
            throw erro("Tipo de variavel esperado");
        }
    }

    // subroutineDec:
    // ('constructor' | 'function' | 'method')
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
        return match(TokenType.INT)
                || match(TokenType.CHAR)
                || match(TokenType.BOOLEAN)
                || match(TokenType.IDENT);
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

    // Implementacao provisoria: reconhece apenas blocos sem comandos.
    // Na proxima etapa sera substituida pelo parser completo de statements.
    private void compileStatements() {
        xml.abrir("statements");

        if (match(TokenType.LET)
                || match(TokenType.IF)
                || match(TokenType.WHILE)
                || match(TokenType.DO)
                || match(TokenType.RETURN)) {
            throw new UnsupportedOperationException(
                "Analise de comandos ainda nao implementada"
                + " (linha " + atual.getLine() + ")"
            );
        }

        if (!match(TokenType.RBRACE)) {
            throw erro("Esperado comando ou fechamento do bloco");
        }

        xml.fechar("statements");
    }


}
