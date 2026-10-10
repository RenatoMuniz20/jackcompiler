package com.jackcompiler;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.xml.XmlTokenWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println(
                "Uso: informe o caminho de um arquivo .jack"
            );
            return;
        }

        Path inputPath = Path.of(args[0]);

        if (!Files.isRegularFile(inputPath)) {
            throw new IllegalArgumentException(
                "Arquivo nao encontrado: " + inputPath
            );
        }

        String fileName = inputPath.getFileName().toString();

        if (!fileName.endsWith(".jack")) {
            throw new IllegalArgumentException(
                "O arquivo deve possuir extensao .jack"
            );
        }

        byte[] input = Files.readAllBytes(inputPath);

        Scanner scanner = new Scanner(input);

        String outputName =
                fileName.substring(0, fileName.length() - 5)
                + "T.xml";

        Path outputPath = inputPath.resolveSibling(outputName);

        XmlTokenWriter.write(scanner, outputPath);

        System.out.println(
            "Arquivo XML gerado: " + outputPath.toAbsolutePath()
        );
    }
}