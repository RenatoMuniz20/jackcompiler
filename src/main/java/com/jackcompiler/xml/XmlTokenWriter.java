package com.jackcompiler.xml;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.lexer.Token;
import com.jackcompiler.lexer.TokenType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class XmlTokenWriter {

    public static void write(Scanner scanner, Path outputPath)
            throws IOException {

        StringBuilder xml = new StringBuilder();

        xml.append("<tokens>\n");

        Token token = scanner.nextToken();

        while (token.getType() != TokenType.EOF) {

            String category = getCategory(token.getType());
            String lexeme = escapeXml(token.getLexeme());

            xml.append("<")
               .append(category)
               .append("> ")
               .append(lexeme)
               .append(" </")
               .append(category)
               .append(">\n");

            token = scanner.nextToken();
        }

        xml.append("</tokens>\n");

        Files.writeString(
            outputPath,
            xml.toString(),
            StandardCharsets.UTF_8
        );
    }

    private static String getCategory(TokenType type) {

        return switch (type) {

            case NUMBER -> "integerConstant";
            case IDENT -> "identifier";
            case STRING -> "stringConstant";

            case LPAREN, RPAREN,
                 LBRACE, RBRACE,
                 LBRACKET, RBRACKET,
                 COMMA, SEMICOLON, DOT,
                 PLUS, MINUS, ASTERISK, SLASH,
                 AND, OR, NOT, LT, GT, EQ -> "symbol";

            case EOF -> throw new IllegalArgumentException(
                "EOF nao deve ser convertido para XML"
            );

            default -> "keyword";
        };
    }

    private static String escapeXml(String text) {

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}