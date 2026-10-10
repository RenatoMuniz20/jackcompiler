package com.jackcompiler.lexer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Scanner {

    private final byte[] input;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords = new HashMap<>();

    static {
        keywords.put("class", TokenType.CLASS);
        keywords.put("constructor", TokenType.CONSTRUCTOR);
        keywords.put("function", TokenType.FUNCTION);
        keywords.put("method", TokenType.METHOD);
        keywords.put("field", TokenType.FIELD);
        keywords.put("static", TokenType.STATIC);
        keywords.put("var", TokenType.VAR);
        keywords.put("int", TokenType.INT);
        keywords.put("char", TokenType.CHAR);
        keywords.put("boolean", TokenType.BOOLEAN);
        keywords.put("void", TokenType.VOID);
        keywords.put("true", TokenType.TRUE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("null", TokenType.NULL);
        keywords.put("this", TokenType.THIS);
        keywords.put("let", TokenType.LET);
        keywords.put("do", TokenType.DO);
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("return", TokenType.RETURN);
    }

    public Scanner(byte[] input) {
        this.input = input;
    }

    private char peek() {
        if (current >= input.length) {
            return '\0';
        }

        return (char) input[current];
    }

    private char peekNext() {
        if (current + 1 >= input.length) {
            return '\0';
        }
        return (char) input[current + 1];
    }

    private void advance() {
        if (current < input.length) {
            current++;
        }
    }

    private boolean isAlpha(char ch) {
        return Character.isLetter(ch) || ch == '_';
    }

    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || Character.isDigit(ch);
    }

    private void skipWhitespaceAndComments() {
        while (true) {
            char ch = peek();

            if (ch == ' ' || ch == '\t' || ch == '\r') {
                advance();
            } else if (ch == '\n') {
                line++;
                advance();
            } else if (ch == '/' && peekNext() == '/') {
                skipLineComment();
            } else if (ch == '/' && peekNext() == '*') {
                skipBlockComment();
            }
            } else {
                break;
            }
        }
    }

    private void skipLineComment() {
        while (peek() != '\n' && peek() != '\0') {
            advance();
        }
    }

    private void skipBlockComment() {
        int startLine = line;
        advance(); // '/'
        advance(); // '*'

        while (!(peek() == '*' && peekNext() == '/')) {
            if (peek() == '\0') {
                throw new RuntimeException(
                    "Erro léxico na linha " + startLine
                            + ": comentário de bloco não foi fechado");
        }
            if (peek() == '\n') {
            line++;
            }
            advance();
        }
        advance(); // '*'
        advance(); // '/'
    }

    private Token number() {
        int start = current;

        while (Character.isDigit(peek())) {
            advance();
        }

        String lexeme = new String(
            input,
            start,
            current - start,
            StandardCharsets.UTF_8
        );

        return new Token(TokenType.NUMBER, lexeme, line);
    }

    private Token identifier() {
        int start = current;

        while (isAlphaNumeric(peek())) {
            advance();
        }

        String lexeme = new String(
            input,
            start,
            current - start,
            StandardCharsets.UTF_8
        );

        TokenType type = keywords.getOrDefault(
            lexeme,
            TokenType.IDENT
        );

        return new Token(type, lexeme, line);
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        char ch = peek();

        if (ch == '\0') {
            return new Token(TokenType.EOF, "", line);
        }

        if (Character.isDigit(ch)) {
            return number();
        }

        if (isAlpha(ch)) {
            return identifier();
        }

        throw new RuntimeException(
            "Erro léxico na linha " + line
            + ": caractere inválido '" + ch + "'"
        );
    }
}
