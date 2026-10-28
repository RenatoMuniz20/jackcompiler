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

    private Map<String, Symbol> scope(Kind kind) {
        return (kind == Kind.STATIC || kind == Kind.FIELD) ? classScope : subroutineScope;
    }

    public void define(String name, String type, Kind kind) {
        Map<String, Symbol> scopeTable = scope(kind);
        if (scopeTable.containsKey(name)) {
            throw new IllegalArgumentException("Variavel ja definida no escopo: " + name);
        }
        int index = varCount(kind);
        scopeTable.put(name, new Symbol(name, type, kind, index));
        countVars.put(kind, index + 1);
    }

    public Symbol resolve(String name) {
        Symbol symbol = subroutineScope.get(name);
        if (symbol != null) {
            return symbol;
        }
        return classScope.get(name);
    }

    public int varCount(Kind kind) {
        return countVars.getOrDefault(kind, 0);
    }
}