package com.jackcompiler.lexer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScannerTest {

    // Executa o Scanner e retorna todos os tokens encontrados.
    private List<Token> tokenizar(String codigo) {

        Scanner scanner = new Scanner(
                codigo.getBytes(StandardCharsets.UTF_8)
        );

        List<Token> tokens = new ArrayList<>();

        Token token = scanner.nextToken();

        while (token.getType() != TokenType.EOF) {
            tokens.add(token);
            token = scanner.nextToken();
        }

        return tokens;
    }

    @Test
    void reconheceAs21PalavrasReservadas() {

        String codigo =
                "class constructor function method field static var " +
                "int char boolean void true false null this " +
                "let do if else while return";

        List<TokenType> esperado = List.of(
                TokenType.CLASS,
                TokenType.CONSTRUCTOR,
                TokenType.FUNCTION,
                TokenType.METHOD,
                TokenType.FIELD,
                TokenType.STATIC,
                TokenType.VAR,
                TokenType.INT,
                TokenType.CHAR,
                TokenType.BOOLEAN,
                TokenType.VOID,
                TokenType.TRUE,
                TokenType.FALSE,
                TokenType.NULL,
                TokenType.THIS,
                TokenType.LET,
                TokenType.DO,
                TokenType.IF,
                TokenType.ELSE,
                TokenType.WHILE,
                TokenType.RETURN
        );

        List<Token> tokens = tokenizar(codigo);

        assertEquals(
                esperado,
                tokens.stream().map(Token::getType).toList()
        );
    }

    @Test
    void reconheceIdentificadoresENumeros() {

        List<Token> tokens = tokenizar(
                "contador _valor x2 123 007"
        );

        assertEquals(5, tokens.size());

        assertEquals(TokenType.IDENT, tokens.get(0).getType());
        assertEquals("contador", tokens.get(0).getLexeme());

        assertEquals(TokenType.IDENT, tokens.get(1).getType());
        assertEquals("_valor", tokens.get(1).getLexeme());

        assertEquals(TokenType.IDENT, tokens.get(2).getType());

        assertEquals(TokenType.NUMBER, tokens.get(3).getType());
        assertEquals("123", tokens.get(3).getLexeme());

        assertEquals(TokenType.NUMBER, tokens.get(4).getType());
        assertEquals("007", tokens.get(4).getLexeme());
    }

    @Test
    void reconheceOs19Simbolos() {

        String codigo = "{}()[].,;+-*/&|<>=~";

        List<TokenType> esperado = List.of(
                TokenType.LBRACE,
                TokenType.RBRACE,
                TokenType.LPAREN,
                TokenType.RPAREN,
                TokenType.LBRACKET,
                TokenType.RBRACKET,
                TokenType.DOT,
                TokenType.COMMA,
                TokenType.SEMICOLON,
                TokenType.PLUS,
                TokenType.MINUS,
                TokenType.ASTERISK,
                TokenType.SLASH,
                TokenType.AND,
                TokenType.OR,
                TokenType.LT,
                TokenType.GT,
                TokenType.EQ,
                TokenType.NOT
        );

        List<Token> tokens = tokenizar(codigo);

        assertEquals(
                esperado,
                tokens.stream().map(Token::getType).toList()
        );
    }

    @Test
    void diferenciaDivisaoDeComentarios() {

        String codigo = """
                8 / 2 // comentario de linha
                /** comentario de documentacao */
                9 /* comentario de bloco */
                """;

        List<Token> tokens = tokenizar(codigo);

        assertEquals(4, tokens.size());

        assertEquals(TokenType.NUMBER, tokens.get(0).getType());
        assertEquals(TokenType.SLASH, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(TokenType.NUMBER, tokens.get(3).getType());

        assertEquals("9", tokens.get(3).getLexeme());
    }

    @Test
    void reconheceStringSemAsAspas() {

        List<Token> tokens = tokenizar(
                "\"Ola, mundo!\""
        );

        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).getType());
        assertEquals("Ola, mundo!", tokens.get(0).getLexeme());
    }

    @Test
    void registraCorretamenteANumeroDaLinha() {

        String codigo = """
                // comentario

                let x = 10;
                """;

        List<Token> tokens = tokenizar(codigo);

        assertEquals(TokenType.LET, tokens.get(0).getType());
        assertEquals(3, tokens.get(0).getLine());
    }

    @Test
    void rejeitaCaractereInvalido() {

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> tokenizar("let x = #;")
        );

        assertTrue(erro.getMessage().contains("linha 1"));
        assertTrue(erro.getMessage().contains("inválido"));
    }

    @Test
    void rejeitaStringNaoFechada() {

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> tokenizar("\"texto sem fechar")
        );

        assertTrue(erro.getMessage().contains("string não fechada"));
    }

    @Test
    void rejeitaComentarioDeBlocoNaoFechado() {

        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> tokenizar("/* comentario sem fechar")
        );

        assertTrue(erro.getMessage().contains("não foi fechado"));
    }

    @Test
    void reconheceFimDoArquivo() {

        Scanner scanner = new Scanner(new byte[0]);

        Token token = scanner.nextToken();

        assertEquals(TokenType.EOF, token.getType());
        assertEquals("", token.getLexeme());
    }
}