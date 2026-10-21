package com.jackcompiler.parser;

import com.jackcompiler.lexer.Scanner;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrosSintaticosTest {

    private String analisar(String codigo) {
        Scanner scanner = new Scanner(codigo.getBytes(StandardCharsets.UTF_8));
        return new CompilationEngine(scanner).compile();
    }

    private void verificarErro(String codigo, int linha, int coluna,
                              String... detalhesEsperados) {
        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> analisar(codigo)
        );

        String mensagem = erro.getMessage();
        assertTrue(mensagem.contains("linha " + linha + ", coluna " + coluna),
                () -> "Localizacao incorreta: " + mensagem);

        for (String detalhe : detalhesEsperados) {
            assertTrue(mensagem.contains(detalhe),
                    () -> "Detalhe ausente ('" + detalhe + "'): " + mensagem);
        }
    }

    @Test
    void informaColunaDeNomeDeClasseAusente() {
        verificarErro("class {}", 1, 7,
                "Esperado IDENT", "LBRACE");
    }

    @Test
    void informaColunaDePontoEVirgulaAusente() {
        verificarErro("class A { field int idade }", 1, 27,
                "Esperado SEMICOLON", "RBRACE");
    }

    @Test
    void identificaExpressaoIncompletaAposQuebrasCrLf() {
        String codigo = "class A {\r\n"
                + "  function void f() {\r\n"
                + "    let x = ;\r\n"
                + "  }\r\n"
                + "}";

        verificarErro(codigo, 3, 13,
                "Termo esperado", "SEMICOLON");
    }

    @Test
    void rejeitaParentesesNaoFechados() {
        String codigo = "class A { function void f() { if (true { return; } } }";

        IllegalArgumentException erro = assertThrows(
                IllegalArgumentException.class,
                () -> analisar(codigo)
        );

        assertTrue(erro.getMessage().contains("Esperado RPAREN"));
        assertTrue(erro.getMessage().contains("LBRACE"));
        assertTrue(erro.getMessage().contains("linha 1, coluna "));
    }

    @Test
    void rejeitaDeclaracaoLocalForaDeSubrotina() {
        verificarErro("class A { var int x; }", 1, 11,
                "Esperado RBRACE", "VAR");
    }

    @Test
    void localizaFimInesperadoDoArquivo() {
        verificarErro("class A {", 1, 10,
                "Esperado RBRACE", "EOF");
    }

    @Test
    void consideraComentariosDeBlocoEMultiplasLinhas() {
        String codigo = "// abertura\r\n"
                + "class A {\r\n"
                + "  /* linha um\r\n"
                + "     linha dois */\r\n"
                + "  function void f() {\r\n"
                + "    let x = ;\r\n"
                + "  }\r\n"
                + "}";

        verificarErro(codigo, 6, 13,
                "Termo esperado", "SEMICOLON");
    }

    @Test
    void contaCorretamenteColunaAposStringComUtf8() {
        String codigo = "class A { function void f() { "
                + "do Output.printString(\"olá\"); let x = ; } }";

        verificarErro(codigo, 1, 69,
                "Termo esperado", "SEMICOLON");
    }

    @Test
    void rejeitaSegundaClasseNoMesmoArquivo() {
        verificarErro("class A {} class B {}", 1, 12,
                "Esperado fim do arquivo", "CLASS");
    }
}
