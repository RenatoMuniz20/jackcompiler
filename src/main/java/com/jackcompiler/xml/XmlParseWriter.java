
package com.jackcompiler.xml;

import com.jackcompiler.lexer.Token;
import com.jackcompiler.lexer.TokenType;

public class XmlParseWriter {

    private final StringBuilder xml = new StringBuilder();
    private int nivel = 0;

    public void abrir(String tag) {
        indentar();
        xml.append("<").append(tag).append(">\n");
        nivel++;
    }

    public void fechar(String tag) {
        nivel--;
        indentar();
        xml.append("</").append(tag).append(">\n");
    }

    public void escreverToken(Token token) {
        String categoria = categoria(token.getType());
        String valor = escapar(token.getLexeme());

        indentar();
        xml.append("<").append(categoria).append("> ")
           .append(valor)
           .append(" </").append(categoria).append(">\n");
    }

    private String categoria(TokenType tipo) {
        return switch (tipo) {
            case NUMBER -> "integerConstant";
            case STRING -> "stringConstant";
            case IDENT -> "identifier";

            case LPAREN, RPAREN, LBRACE, RBRACE,
                 LBRACKET, RBRACKET, COMMA, SEMICOLON,
                 DOT, PLUS, MINUS, ASTERISK, SLASH,
                 AND, OR, NOT, LT, GT, EQ -> "symbol";

            case EOF -> throw new IllegalArgumentException(
                "EOF nao deve aparecer no XML"
            );

            default -> "keyword";
        };
    }

    private String escapar(String valor) {
        return valor.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }

    private void indentar() {
        xml.append("  ".repeat(nivel));
    }

    public String getXml() {
        return xml.toString();
    }
}
