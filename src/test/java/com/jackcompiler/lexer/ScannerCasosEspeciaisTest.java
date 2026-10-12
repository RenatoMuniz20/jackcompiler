package com.jackcompiler.lexer;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScannerCasosEspeciaisTest {

    private List<Token> tokenizar(String codigo) {
        Scanner scanner = new Scanner(codigo.getBytes(StandardCharsets.UTF_8));
        List<Token> tokens = new ArrayList<>();

        Token token = scanner.nextToken();
        while (token.getType() != TokenType.EOF) {
            tokens.add(token);
            token = scanner.nextToken();
        }
        return tokens;
    }

    @Test
    void aceitaMaiorInteiroDeJack() {
        List<Token> tokens = tokenizar("32767 0 00032767");

        assertEquals(3, tokens.size());
        assertEquals("32767", tokens.get(0).getLexeme());
    }

    @Test
    void rejeitaInteiroMaiorQue32767() {
        RuntimeException erro = assertThrows(
                RuntimeException.class,
                () -> tokenizar("let x = 32768;")
        );

        assertTrue(erro.getMessage().contains("linha 1"));
    }

    @Test
    void rejeitaInteiroGiganteSemEstourarOParse() {
        assertThrows(
                RuntimeException.class,
                () -> tokenizar("99999999999999999999")
        );
    }

    @Test
    void mantemAcentosDentroDeStringsEComentarios() {
        List<Token> tokens = tokenizar(
                "// comentário com ação\n\"olá, ç\" /* não é código */"
        );

        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).getType());
        assertEquals("olá, ç", tokens.get(0).getLexeme());
    }

    @Test
    void rejeitaLetraAcentuadaForaDeStringOuComentario() {
        assertThrows(RuntimeException.class, () -> tokenizar("let ação = 1;"));
    }

    @Test
    void contaLinhasComFimDeLinhaDoWindows() {
        List<Token> tokens = tokenizar("a\r\nb\r\n\r\nc");

        assertEquals(1, tokens.get(0).getLine());
        assertEquals(2, tokens.get(1).getLine());
        assertEquals(4, tokens.get(2).getLine());
    }

    @Test
    void stringNaoPodeQuebrarLinha() {
        assertThrows(RuntimeException.class, () -> tokenizar("\"abc\ndef\""));
    }
}