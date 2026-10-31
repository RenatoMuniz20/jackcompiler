package com.jackcompiler.codegen;

import com.jackcompiler.codegen.VMWriter.Command;
import com.jackcompiler.codegen.VMWriter.Segment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VMWriterTest {

    @Test
    void emitePushEPopCorretamente() {
        VMWriter vm = new VMWriter();
        vm.writePush(Segment.CONST, 5);
        vm.writePop(Segment.LOCAL, 1);

        assertEquals("push constant 5\npop local 1\n", vm.vmOutput());
    }

    @Test
    void emiteComandosAritmeticosEFluxo() {
        VMWriter vm = new VMWriter();
        vm.writeArithmetic(Command.ADD);
        vm.writeArithmetic(Command.NEG);
        vm.writeLabel("LOOP");
        vm.writeIf("END");
        vm.writeGoto("LOOP");

        assertEquals("""
                add
                neg
                label LOOP
                if-goto END
                goto LOOP
                """, vm.vmOutput());
    }

    @Test
    void emiteChamadasEFuncoes() {
        VMWriter vm = new VMWriter();
        vm.writeFunction("Main.main", 2);
        vm.writeCall("Math.abs", 1);
        vm.writeReturn();

        assertEquals("""
                function Main.main 2
                call Math.abs 1
                return
                """, vm.vmOutput());
    }
}