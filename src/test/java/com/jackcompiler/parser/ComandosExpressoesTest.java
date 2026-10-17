package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ComandosExpressoesTest {

    private String analisar(String codigo) {
        Scanner scanner = new Scanner(codigo.getBytes(StandardCharsets.UTF_8));
        return new CompilationEngine(scanner).compile();
    }

    @Test
    void reconheceOsCincoComandos() {
        String xml = analisar("""
            class Main {
                function void main() {
                    var int i;
                    let i = 0;
                    while (i < 10) {
                        if (i = 5) {
                            do Output.printInt(i);
                        } else {
                            do Output.printInt(0);
                        }
                        let i = i + 1;
                    }
                    return;
                }
            }
            """);

        assertTrue(xml.contains("<letStatement>"));
        assertTrue(xml.contains("<whileStatement>"));
        assertTrue(xml.contains("<ifStatement>"));
        assertTrue(xml.contains("<doStatement>"));
        assertTrue(xml.contains("<returnStatement>"));
        assertTrue(xml.contains("<keyword> else </keyword>"));
    }

    @Test
    void reconheceArraysUnariosEAgrupamento() {
        String xml = analisar("""
            class A {
                function void f() {
                    var Array a;
                    var int i, x;
                    let a[i + 1] = -(x + 2) * ~0;
                    return;
                }
            }
            """);

        assertTrue(xml.contains("<symbol> [ </symbol>"));
        assertTrue(xml.contains("<symbol> ] </symbol>"));
        assertTrue(xml.contains("<symbol> - </symbol>"));
        assertTrue(xml.contains("<symbol> ~ </symbol>"));
        assertTrue(xml.contains("<symbol> * </symbol>"));
    }

    @Test
    void aceitaChamadasSimplesEQualificadas() {
        String xml = analisar("""
            class A {
                function int f() {
                    var int x;
                    do desenhar();
                    do Output.printInt(2);
                    let x = Math.multiply(3, 4);
                    return x;
                }
            }
            """);

        long listas = xml.lines()
                .filter(linha -> linha.strip().equals("<expressionList>"))
                .count();

        assertEquals(3, listas);
        assertTrue(xml.contains("<identifier> Math </identifier>"));
        assertTrue(xml.contains("<identifier> desenhar </identifier>"));
    }

    @Test
    void escapaCaracteresEspeciaisNoXml() {
        String xml = analisar("""
            class A {
                function void f() {
                    if (x < 10 & y > 0) {
                        do Output.printString("A&B<>");
                    }
                    return;
                }
            }
            """);

        assertTrue(xml.contains("<symbol> &lt; </symbol>"));
        assertTrue(xml.contains("<symbol> &amp; </symbol>"));
        assertTrue(xml.contains("<symbol> &gt; </symbol>"));
        assertTrue(xml.contains("<stringConstant> A&amp;B&lt;&gt; </stringConstant>"));
    }

    @Test
    void rejeitaExpressaoIncompleta() {
        String codigo = "class A { function void f() { let x = ; } }";
        assertThrows(IllegalArgumentException.class, () -> analisar(codigo));
    }

    @Test
    void rejeitaChamadaSemParenteses() {
        String codigo = "class A { function void f() { do Output.printInt; } }";
        assertThrows(IllegalArgumentException.class, () -> analisar(codigo));
    }

    @Test
    void rejeitaArraySemColcheteDeFechamento() {
        String codigo = "class A { function void f() { let a[2 = 10; } }";
        assertThrows(IllegalArgumentException.class, () -> analisar(codigo));
    }
}
