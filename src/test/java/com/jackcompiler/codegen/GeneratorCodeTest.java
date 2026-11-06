package com.jackcompiler.codegen;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.parser.CompilationEngine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeneratorCodeTest {

    private String compilar(String input) {
        Scanner scanner = new Scanner(input.getBytes());
        CompilationEngine engine = new CompilationEngine(scanner);
        // O método compileVm() é chamado e devolve os comandos VM como String
        return engine.compileVm();
    }

    @Test
    void testExpressaoSimples() {
        String input = "class Main { function void main() { do Output.printInt(1 + (2 * 3)); return; } }";
        String vm = compilar(input);
        
        String esperado = 
                "function Main.main 0\n" +
                "push constant 1\n" +
                "push constant 2\n" +
                "push constant 3\n" +
                "call Math.multiply 2\n" +
                "add\n" +
                "call Output.printInt 1\n" +
                "pop temp 0\n" +
                "push constant 0\n" +
                "return\n";

        assertEquals(esperado.trim(), vm.trim());
    }

    @Test
    void testVariaveisELet() {
        String input = "class Main { function void main() { var int x, y; let x = 5; let y = x + 2; return; } }";
        String vm = compilar(input);

        String esperado =
                "function Main.main 2\n" +
                "push constant 5\n" +
                "pop local 0\n" +
                "push local 0\n" +
                "push constant 2\n" +
                "add\n" +
                "pop local 1\n" +
                "push constant 0\n" +
                "return\n";

        assertEquals(esperado.trim(), vm.trim());
    }

    @Test
    void testIfElse() {
        String input = "class Main { function void main() { var boolean x; if (x) { do Output.printInt(1); } else { do Output.printInt(0); } return; } }";
        String vm = compilar(input);

        String esperado =
                "function Main.main 1\n" +
                "push local 0\n" +
                "if-goto IF_TRUE0\n" +
                "goto IF_FALSE0\n" +
                "label IF_TRUE0\n" +
                "push constant 1\n" +
                "call Output.printInt 1\n" +
                "pop temp 0\n" +
                "goto IF_END0\n" +
                "label IF_FALSE0\n" +
                "push constant 0\n" +
                "call Output.printInt 1\n" +
                "pop temp 0\n" +
                "label IF_END0\n" +
                "push constant 0\n" +
                "return\n";

        assertEquals(esperado.trim(), vm.trim());
    }

    @Test
    void testWhile() {
        String input = "class Main { function void main() { var int i; let i = 0; while (i < 5) { let i = i + 1; } return; } }";
        String vm = compilar(input);

        String esperado =
                "function Main.main 1\n" +
                "push constant 0\n" +
                "pop local 0\n" +
                "label WHILE_EXP0\n" +
                "push local 0\n" +
                "push constant 5\n" +
                "lt\n" +
                "not\n" +
                "if-goto WHILE_END0\n" +
                "push local 0\n" +
                "push constant 1\n" +
                "add\n" +
                "pop local 0\n" +
                "goto WHILE_EXP0\n" +
                "label WHILE_END0\n" +
                "push constant 0\n" +
                "return\n";

        assertEquals(esperado.trim(), vm.trim());
    }
    
    @Test
    void testStringsEArrays() {
        String input = "class Main { function void main() { var Array a; let a[2] = 5; do Output.printString(\"Oi\"); return; } }";
        String vm = compilar(input);
        
        String esperado = 
                "function Main.main 1\n" +
                "push constant 2\n" +
                "push local 0\n" +
                "add\n" +
                "push constant 5\n" +
                "pop temp 0\n" +
                "pop pointer 1\n" +
                "push temp 0\n" +
                "pop that 0\n" +
                "push constant 2\n" +
                "call String.new 1\n" +
                "push constant 79\n" +
                "call String.appendChar 2\n" +
                "push constant 105\n" +
                "call String.appendChar 2\n" +
                "call Output.printString 1\n" +
                "pop temp 0\n" +
                "push constant 0\n" +
                "return\n";
                
        assertEquals(esperado.trim(), vm.trim());
    }
}