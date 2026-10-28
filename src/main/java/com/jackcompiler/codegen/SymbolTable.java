package com.jackcompiler.codegen;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    public enum Kind {
        STATIC, FIELD, ARG, VAR
    }

    public record Symbol(String name, String type, Kind kind, int index) {
    }

    private final Map<String, Symbol> classScope = new HashMap<>();
    private final Map<String, Symbol> subroutineScope = new HashMap<>();
    private final Map<Kind, Integer> countVars = new HashMap<>();

    public SymbolTable() {
        countVars.put(Kind.STATIC, 0);
        countVars.put(Kind.FIELD, 0);
        countVars.put(Kind.ARG, 0);
        countVars.put(Kind.VAR, 0);
    }

    public void startSubroutine() {
        subroutineScope.clear();
        countVars.put(Kind.ARG, 0);
        countVars.put(Kind.VAR, 0);
    }
}