package com.jackcompiler.codegen;

public class VMWriter {

    public enum Segment {
        CONST("constant"),
        ARG("argument"),
        LOCAL("local"),
        STATIC("static"),
        THIS("this"),
        THAT("that"),
        POINTER("pointer"),
        TEMP("temp");

        public final String value;

        Segment(String value) {
            this.value = value;
        }
    }

    public enum Command {
        ADD, SUB, NEG, EQ, GT, LT, AND, OR, NOT
    }

    private final StringBuilder vmOutput = new StringBuilder();

    public String vmOutput() {
        return vmOutput.toString();
    }
}