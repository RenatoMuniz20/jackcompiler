package com.jackcompiler;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.parser.CompilationEngine;
import com.jackcompiler.xml.XmlTokenWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static final String USO =
            "Uso: Main [--tokens | --parse] <arquivo.jack> [saida.xml]";

    public static void main(String[] args) {
        try {
            executar(args);
        } catch (RuntimeException | IOException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void executar(String[] args) throws IOException {
        int indice = 0;
        String modo = "";

        if (args.length > 0
                && (args[0].equals("--tokens") || args[0].equals("--parse"))) {
            modo = args[0];
            indice = 1;
        }

        int restantes = args.length - indice;
        if (restantes < 1 || restantes > 2
                || (modo.isEmpty() && restantes != 1)) {
            throw new IllegalArgumentException(USO);
        }

        Path entrada = Path.of(args[indice]);
        Path saidaInformada = restantes == 2 ? Path.of(args[indice + 1]) : null;

        if (!Files.isRegularFile(entrada)) {
            throw new IllegalArgumentException(
                    "Arquivo nao encontrado: " + entrada
            );
        }

        String nomeArquivo = entrada.getFileName().toString();
        if (!nomeArquivo.endsWith(".jack")) {
            throw new IllegalArgumentException(
                    "O arquivo deve possuir extensao .jack"
            );
        }

        String base = nomeArquivo.substring(0, nomeArquivo.length() - 5);
        byte[] fonte = Files.readAllBytes(entrada);

        if (!modo.equals("--parse")) {
            Path saida = saidaInformada != null
                    ? saidaInformada
                    : resolverSaidaPadrao(entrada, base + "T.xml");
            XmlTokenWriter.write(new Scanner(fonte), saida);
            System.out.println("XML de tokens gerado: " + saida.toAbsolutePath());
        }

        if (!modo.equals("--tokens")) {
            Path saida = saidaInformada != null
                    ? saidaInformada
                    : resolverSaidaPadrao(entrada, base + "P.xml");
            String xml = new CompilationEngine(new Scanner(fonte)).compile();
            Files.writeString(saida, xml, StandardCharsets.UTF_8);
            System.out.println("XML sintatico gerado: " + saida.toAbsolutePath());
        }
    }

    private static Path resolverSaidaPadrao(Path entrada, String nomeArquivoSaida)
            throws IOException {
        String caminhoNormalizado = entrada.toString().replace('\\', '/');
        if (caminhoNormalizado.contains("src/test/resources")) {
            Path dirSaida = Path.of("target", "generated-output");
            Files.createDirectories(dirSaida);
            return dirSaida.resolve(nomeArquivoSaida);
        }
        return entrada.resolveSibling(nomeArquivoSaida);
    }
}