package com.jackcompiler.codegen;

import com.jackcompiler.codegen.SymbolTable.Kind;
import com.jackcompiler.codegen.SymbolTable.Symbol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SymbolTableTest {

    @Test
    void defineEResolveVariaveisPorEscopo() {
        SymbolTable st = new SymbolTable();
        st.define("x", "int", Kind.FIELD);
        st.define("y", "int", Kind.FIELD);
        st.define("total", "int", Kind.STATIC);

        st.startSubroutine();
        st.define("this", "Point", Kind.ARG);
        st.define("ax", "int", Kind.ARG);
        st.define("temp", "int", Kind.VAR);

        assertEquals(2, st.varCount(Kind.FIELD));
        assertEquals(1, st.varCount(Kind.STATIC));
        assertEquals(2, st.varCount(Kind.ARG));
        assertEquals(1, st.varCount(Kind.VAR));

        Symbol sField = st.resolve("y");
        assertNotNull(sField);
        assertEquals(Kind.FIELD, sField.kind());
        assertEquals(1, sField.index());

        Symbol sArg = st.resolve("ax");
        assertNotNull(sArg);
        assertEquals(Kind.ARG, sArg.kind());
        assertEquals(1, sArg.index());
    }

    @Test
    void startSubroutineLimpaApenasEscopoLocal() {
        SymbolTable st = new SymbolTable();
        st.define("global", "boolean", Kind.STATIC);
        st.define("local1", "int", Kind.VAR);

        st.startSubroutine();

        assertEquals(1, st.varCount(Kind.STATIC));
        assertEquals(0, st.varCount(Kind.VAR));
        assertNotNull(st.resolve("global"));
        assertNull(st.resolve("local1"));
    }

    @Test
    void rejeitaVariavelDuplicadaNoMesmoEscopo() {
        SymbolTable st = new SymbolTable();
        st.define("x", "int", Kind.VAR);
        assertThrows(IllegalArgumentException.class, () -> st.define("x", "int", Kind.VAR));
    }
}