
package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class CompilationEngineTest {

    private String analisar(String codigo) {
        Scanner scanner = new Scanner(
            codigo.getBytes(StandardCharsets.UTF_8)
        );

        return new CompilationEngine(scanner).compile();
    }

    @Test
    void reconheceClasseVazia() {
        String xml = analisar("class Pessoa {}");

        assertEquals("""
                <class>
                  <keyword> class </keyword>
                  <identifier> Pessoa </identifier>
                  <symbol> { </symbol>
                  <symbol> } </symbol>
                </class>
                """, xml);
    }

    @Test
    void reconheceDeclaracoesDeVariaveis() {
        String xml = analisar(
            "class Pessoa { field int idade, altura; static boolean ativo; }"
        );

        assertTrue(xml.contains("<classVarDec>"));
        assertTrue(xml.contains("<keyword> field </keyword>"));
        assertTrue(xml.contains("<keyword> static </keyword>"));
        assertTrue(xml.contains("<identifier> altura </identifier>"));
        assertTrue(xml.contains("</classVarDec>"));
    }

    @Test
    void rejeitaDeclaracaoSemPontoEVirgula() {
        assertThrows(
            IllegalArgumentException.class,
            () -> analisar("class Pessoa { field int idade }")
        );
    }

    @Test
    void rejeitaTokensDepoisDaClasse() {
        assertThrows(
            IllegalArgumentException.class,
            () -> analisar("class Pessoa {} class Outra {}")
        );
    }
}
