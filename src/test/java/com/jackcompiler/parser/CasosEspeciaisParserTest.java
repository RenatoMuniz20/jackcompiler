package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CasosEspeciaisParserTest {

    private String analisar(String codigo) {
        Scanner scanner = new Scanner(codigo.getBytes(StandardCharsets.UTF_8));
        return new CompilationEngine(scanner).compile();
    }

    private long contarTags(String xml, String tag) {
        return xml.lines()
                .map(String::strip)
                .filter(linha -> linha.equals("<" + tag + ">"))
                .count();
    }

    @Test
    void reconheceIfElseEWhileComBlocosVaziosAninhados() {
        String xml = analisar("""
                class A {
                    function void f() {
                        if (true) {
                            while (false) {
                            }
                        } else {
                        }
                        return;
                    }
                }
                """);

        assertEquals(1, contarTags(xml, "ifStatement"));
        assertEquals(1, contarTags(xml, "whileStatement"));
        assertEquals(4, contarTags(xml, "statements"));
        assertEquals(1, contarTags(xml, "returnStatement"));
    }

    @Test
    void aceitaListasDeArgumentosVaziasEComMultiplasExpressoes() {
        String xml = analisar("""
                class A {
                    function void f() {
                        do executar();
                        do Output.printInt(1, true, x + 2);
                        return;
                    }
                }
                """);

        assertEquals(2, contarTags(xml, "expressionList"));
        assertEquals(3, contarTags(xml, "expression"));
        assertTrue(xml.contains("<identifier> executar </identifier>"));
        assertTrue(xml.contains("<identifier> printInt </identifier>"));
    }

    @Test
    void reconheceIndicesCompostosEChamadasDentroDeExpressoes() {
        String xml = analisar("""
                class A {
                    function void f() {
                        let vetor[(i + 1)] = vetor[i] + Math.multiply(x, 2);
                        return;
                    }
                }
                """);

        assertEquals(2, xml.lines().filter(l -> l.contains("<symbol> [ </symbol>")).count());
        assertEquals(2, xml.lines().filter(l -> l.contains("<symbol> ] </symbol>")).count());
        assertEquals(1, contarTags(xml, "expressionList"));
        assertTrue(xml.contains("<identifier> multiply </identifier>"));
    }

    @Test
    void reconheceOperadoresUnariosAninhadosEAgrupamento() {
        String xml = analisar("""
                class A {
                    function void f() {
                        let x = -~(-3);
                        return;
                    }
                }
                """);

        assertEquals(5, contarTags(xml, "term"));
        assertTrue(xml.contains("<symbol> ~ </symbol>"));
        assertTrue(xml.contains("<symbol> ( </symbol>"));
        assertTrue(xml.contains("<symbol> ) </symbol>"));
    }

    @Test
    void representaOperadoresBinariosNaMesmaExpressao() {
        String xml = analisar("""
                class A {
                    function void f() {
                        let x = 1 + 2 * 3;
                        return;
                    }
                }
                """);

        // Gramática Jack: expression = term (op term)*.
        // Não cria uma subexpressão apenas por encontrar multiplicação.
        assertEquals(1, contarTags(xml, "expression"));
        assertEquals(3, contarTags(xml, "term"));
        assertTrue(xml.contains("<symbol> + </symbol>"));
        assertTrue(xml.contains("<symbol> * </symbol>"));
    }

    @Test
    void reconheceTodasAsConstantesDePalavraChave() {
        String xml = analisar("""
                class A {
                    method void f() {
                        let a = true;
                        let b = false;
                        let c = null;
                        let d = this;
                        return;
                    }
                }
                """);

        for (String palavra : new String[]{"true", "false", "null", "this"}) {
            assertTrue(xml.contains("<keyword> " + palavra + " </keyword>"));
        }
        assertEquals(4, contarTags(xml, "letStatement"));
    }

    @Test
    void ignoraComentariosUtf8EQuebrasDeLinhaWindows() {
        String codigo = "// comentário inicial\r\n"
                + "/** documentação\r\n"
                + " com ação */\r\n"
                + "class A { function void f() { return; } }";

        String xml = analisar(codigo);
        assertTrue(xml.contains("<identifier> A </identifier>"));
        assertEquals(1, contarTags(xml, "returnStatement"));
        assertTrue(!xml.contains("comentário"));
        assertTrue(!xml.contains("documentação"));
    }

    @Test
    void geraXmlBemFormadoMesmoComCaracteresReservados() throws Exception {
        String xml = analisar("""
                class A {
                    function void f() {
                        do Output.printString("A & B < C > D");
                        return;
                    }
                }
                """);

        Document documento = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(new InputSource(new StringReader(xml)));

        assertEquals("class", documento.getDocumentElement().getTagName());
        assertEquals(1, documento.getElementsByTagName("stringConstant").getLength());
        assertEquals("A & B < C > D", documento
                .getElementsByTagName("stringConstant")
                .item(0).getTextContent().trim());
    }

    @Test
    void rejeitaVirgulaFinalNaListaDeArgumentos() {
        String codigo = "class A { function void f() { do executar(1,); return; } }";

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> analisar(codigo)
        );

        assertTrue(erro.getMessage().contains("Termo esperado"));
        assertTrue(erro.getMessage().contains("RPAREN"));
    }

    @Test
    void rejeitaDeclaracaoLocalDepoisDeIniciarOsComandos() {
        String codigo = "class A { function void f() { return; var int x; } }";

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> analisar(codigo)
        );

        assertTrue(erro.getMessage().contains("Esperado comando ou fechamento do bloco"));
        assertTrue(erro.getMessage().contains("VAR"));
    }
}
