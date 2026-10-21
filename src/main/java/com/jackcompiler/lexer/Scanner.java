package com.jackcompiler.lexer;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Scanner {

    private final byte[] input;
    private int current = 0;
    private int line = 1;
    private int column = 1;
    private static final int MAIOR_INTEIRO_JACK = 32767;

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
        return (char) (input[current] & 0xFF);
    }

    private char peekNext() {
        if (current + 1 >= input.length) {
            return '\0';
        }
        return (char) (input[current + 1] & 0xFF);
    }

    // Atualiza linha e coluna a cada byte consumido. Os bytes de continuacao
    // UTF-8 nao ocupam novas colunas; CRLF corresponde a uma unica quebra.
    private void advance() {
        if (current >= input.length) {
            return;
        }

        int value = input[current] & 0xFF;
        if (value == '\r') {
            line++;
            column = 1;
        } else if (value == '\n') {
            if (current == 0 || input[current - 1] != '\r') {
                line++;
            }
            column = 1;
        } else if ((value & 0xC0) != 0x80) {
            column++;
        }
        current++;
    }

    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z')
                || (ch >= 'A' && ch <= 'Z')
                || ch == '_';
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private void skipWhitespaceAndComments() {
        while (true) {
            char ch = peek();

            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                advance();
            } else if (ch == '/' && peekNext() == '/') {
                skipLineComment();
            } else if (ch == '/' && peekNext() == '*') {
                skipBlockComment();
            } else {
                break;
            }
        }
    }

    private void skipLineComment() {
        while (peek() != '\n' && peek() != '\r' && peek() != '\0') {
            advance();
        }
    }

    private void skipBlockComment() {
        int startLine = line;
        int startColumn = column;
        advance(); // pula '/'
        advance(); // pula '*'

        while (!(peek() == '*' && peekNext() == '/')) {
            if (peek() == '\0') {
                throw new RuntimeException(
                        "Erro léxico na linha " + startLine + ", coluna "
                                + startColumn + ": comentário de bloco não foi fechado");
            }
            advance();
        }
        advance(); // pula '*'
        advance(); // pula '/'
    }

    private Token number() {
        int start = current;
        int startColumn = column;

        while (isDigit(peek())) {
            advance();
        }

        String lexeme = new String(
                input,
                start,
                current - start,
                StandardCharsets.UTF_8
        );

        if (!cabeEmInteiroJack(lexeme)) {
            throw new RuntimeException(
                    "Erro léxico na linha " + line + ", coluna " + startColumn
                            + ": inteiro fora do intervalo 0..32767: " + lexeme
            );
        }

        return new Token(TokenType.NUMBER, lexeme, line, startColumn);
    }

    // Ignora zeros à esquerda e confere o limite sem risco de overflow
    private boolean cabeEmInteiroJack(String lexeme) {
        String semZeros = lexeme.replaceFirst("^0+(?=\\d)", "");
        return semZeros.length() <= 5
                && Integer.parseInt(semZeros) <= MAIOR_INTEIRO_JACK;
    }

    private Token string() {
        int startLine = line;
        int startColumn = column;
        advance(); // pula a aspa de abertura '"'
        int start = current;

        while (peek() != '"' && peek() != '\0'
                && peek() != '\n' && peek() != '\r') {
            advance();
        }

        if (peek() != '"') {
            throw new RuntimeException(
                    "Erro léxico na linha " + startLine + ", coluna "
                            + startColumn + ": string não fechada"
            );
        }

        String lexeme = new String(
                input,
                start,
                current - start,
                StandardCharsets.UTF_8
        );
        advance(); // consome a aspa de fechamento '"'

        return new Token(TokenType.STRING, lexeme, startLine, startColumn);
    }

    private Token identifier() {
        int start = current;
        int startColumn = column;

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

        return new Token(type, lexeme, line, startColumn);
    }

    private TokenType symbolType(char ch) {
        switch (ch) {
            case '(': return TokenType.LPAREN;
            case ')': return TokenType.RPAREN;
            case '{': return TokenType.LBRACE;
            case '}': return TokenType.RBRACE;
            case '[': return TokenType.LBRACKET;
            case ']': return TokenType.RBRACKET;
            case ',': return TokenType.COMMA;
            case ';': return TokenType.SEMICOLON;
            case '.': return TokenType.DOT;
            case '+': return TokenType.PLUS;
            case '-': return TokenType.MINUS;
            case '*': return TokenType.ASTERISK;
            case '/': return TokenType.SLASH;
            case '&': return TokenType.AND;
            case '|': return TokenType.OR;
            case '~': return TokenType.NOT;
            case '<': return TokenType.LT;
            case '>': return TokenType.GT;
            case '=': return TokenType.EQ;
            default:  return null;
        }
    }

    public Token nextToken() {
        skipWhitespaceAndComments();

        char ch = peek();
        int startColumn = column;

        if (ch == '\0') {
            return new Token(TokenType.EOF, "", line, startColumn);
        }

        if (ch == '"') {
            return string();
        }

        if (isDigit(ch)) {
            return number();
        }

        if (isAlpha(ch)) {
            return identifier();
        }

        TokenType symbol = symbolType(ch);
        if (symbol != null) {
            advance();
            return new Token(symbol, String.valueOf(ch), line, startColumn);
        }

        throw new RuntimeException(
                "Erro léxico na linha " + line + ", coluna " + startColumn
                        + ": caractere inválido '" + ch + "'"
        );
    }
}
