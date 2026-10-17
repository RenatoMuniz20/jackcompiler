
package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class SubroutineTest {

    private String analisar(String codigo) {
        Scanner scanner = new Scanner(
            codigo.getBytes(StandardCharsets.UTF_8)
        );

        return new CompilationEngine(scanner).compile();
    }

    @Test
    void reconheceFuncaoSemParametros() {
        String xml = analisar(
            "class Main { function void main() {} }"
        );

        assertEquals("""
                <class>
                  <keyword> class </keyword>
                  <identifier> Main </identifier>
                  <symbol> { </symbol>
                  <subroutineDec>
                    <keyword> function </keyword>
                    <keyword> void </keyword>
                    <identifier> main </identifier>
                    <symbol> ( </symbol>
                    <parameterList>
                    </parameterList>
                    <symbol> ) </symbol>
                    <subroutineBody>
                      <symbol> { </symbol>
                      <statements>
                      </statements>
                      <symbol> } </symbol>
                    </subroutineBody>
                  </subroutineDec>
                  <symbol> } </symbol>
                </class>
                """, xml);
    }

    @Test
    void reconheceConstrutoresMetodosEVariaveis() {
        String codigo = """
                class Teste {
                    field int idade;

                    constructor Teste new(int numero, boolean ativo) {
                        var int a, b;
                    }

                    method void mudar(Teste outro) {
                        var Array dados;
                    }

                    function int soma(int x, int y) {
                    }
                }
                """;

        String xml = analisar(codigo);

        long subrotinas = xml.lines()
                .filter(linha -> linha.strip().equals("<subroutineDec>"))
                .count();

        long variaveisLocais = xml.lines()
                .filter(linha -> linha.strip().equals("<varDec>"))
                .count();

        assertEquals(3, subrotinas);
        assertEquals(2, variaveisLocais);

        assertTrue(xml.contains("<keyword> constructor </keyword>"));
        assertTrue(xml.contains("<keyword> method </keyword>"));
        assertTrue(xml.contains("<keyword> function </keyword>"));
        assertTrue(xml.contains("<identifier> outro </identifier>"));
        assertTrue(xml.contains("<identifier> dados </identifier>"));
    }

    @Test
    void rejeitaParametroSemNome() {
        String codigo = "class A { function void f(int) {} }";

        assertThrows(
            IllegalArgumentException.class,
            () -> analisar(codigo)
        );
    }

    @Test
    void naoAceitaComandosAindaNaoImplementados() {
        String codigo = """
                class Main {
                    function void main() {
                        return;
                    }
                }
                """;

        assertThrows(
            UnsupportedOperationException.class,
            () -> analisar(codigo)
        );
    }
}
