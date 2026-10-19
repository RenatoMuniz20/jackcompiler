package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ValidacaoOficialTest {

    private static final List<String> PROGRAMAS =
            List.of("Main", "Square", "SquareGame");

    private static byte[] recurso(String caminho) throws IOException {
        try (InputStream in =
                     ValidacaoOficialTest.class.getResourceAsStream(caminho)) {
            assertNotNull(in, "Recurso nao encontrado: " + caminho);
            return in.readAllBytes();
        }
    }

    private static String normalizar(String conteudo) {
        return conteudo.lines()
                .map(String::strip)
                .filter(linha -> !linha.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    @TestFactory
    Stream<DynamicTest> xmlSintaticoEquivaleAoOficial() {
        return PROGRAMAS.stream().map(nome ->
                DynamicTest.dynamicTest(nome + ".jack -> " + nome + "P.xml", () -> {
                    byte[] fonte = recurso("/jack/" + nome + ".jack");
                    String esperado = new String(
                            recurso("/expected/" + nome + "P.xml"),
                            StandardCharsets.UTF_8
                    );

                    String gerado =
                            new CompilationEngine(new Scanner(fonte)).compile();

                    assertEquals(normalizar(esperado), normalizar(gerado));
                })
        );
    }
}
