package com.jackcompiler;

import com.jackcompiler.lexer.Scanner;
import com.jackcompiler.parser.CompilationEngine;
import com.jackcompiler.xml.XmlTokenWriter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    private static final String USO =
            "Uso: Main [--tokens | --parse | --vm | --all] <arquivo.jack | diretorio> [saida]";

    public static void main(String[] args) {
        try {
            executar(args);
        } catch (RuntimeException | IOException e) {
            System.err.println("Erro: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void executar(String[] args) throws IOException {
        int indice = 0;
        String modo = "--vm";

        if (args.length > 0 && (args[0].equals("--tokens")
                || args[0].equals("--parse")
                || args[0].equals("--vm")
                || args[0].equals("--all"))) {
            modo = args[0];
            indice = 1;
        }

        int restantes = args.length - indice;
        if (restantes < 1 || restantes > 2) {
            throw new IllegalArgumentException(USO);
        }

        Path entrada = Path.of(args[indice]);
        Path saidaInformada = restantes == 2 ? Path.of(args[indice + 1]) : null;

        if (!Files.exists(entrada)) {
            throw new IllegalArgumentException("Caminho nao encontrado: " + entrada);
        }

        if (Files.isDirectory(entrada)) {
            compilarDiretorio(entrada, modo, saidaInformada);
        } else if (Files.isRegularFile(entrada)) {
            compilarArquivo(entrada, modo, saidaInformada);
        } else {
            throw new IllegalArgumentException("Caminho invalido: " + entrada);
        }
    }

    private static void compilarDiretorio(Path diretorio, String modo, Path dirSaida)
            throws IOException {
        List<Path> arquivosJack;
        try (Stream<Path> stream = Files.list(diretorio)) {
            arquivosJack = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".jack"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        if (arquivosJack.isEmpty()) {
            throw new IllegalArgumentException(
                    "Nenhum arquivo .jack encontrado no diretorio: " + diretorio
            );
        }

        if (dirSaida != null) {
            Files.createDirectories(dirSaida);
        }

        for (Path arquivo : arquivosJack) {
            Path destinoBase = dirSaida != null
                    ? dirSaida.resolve(arquivo.getFileName())
                    : arquivo;
            compilarArquivoInterno(arquivo, destinoBase, modo, null);
        }
    }

    private static void compilarArquivo(Path entrada, String modo, Path saidaInformada)
            throws IOException {
        String nomeArquivo = entrada.getFileName().toString();
        if (!nomeArquivo.endsWith(".jack")) {
            throw new IllegalArgumentException("O arquivo deve possuir extensao .jack");
        }
        compilarArquivoInterno(entrada, entrada, modo, saidaInformada);
    }

    private static void compilarArquivoInterno(
            Path origem, Path referenciaSaida, String modo, Path saidaExplicita)
            throws IOException {
        String nomeArquivo = origem.getFileName().toString();
        String base = nomeArquivo.substring(0, nomeArquivo.length() - 5);
        byte[] fonte = Files.readAllBytes(origem);

        if (modo.equals("--tokens") || modo.equals("--all")) {
            Path saida = saidaExplicita != null
                    ? saidaExplicita
                    : referenciaSaida.resolveSibling(base + "T.xml");
            XmlTokenWriter.write(new Scanner(fonte), saida);
            System.out.println("XML de tokens gerado: " + saida.toAbsolutePath());
        }

        if (modo.equals("--parse") || modo.equals("--all")) {
            Path saida = saidaExplicita != null
                    ? saidaExplicita
                    : referenciaSaida.resolveSibling(base + "P.xml");
            String xml = new CompilationEngine(new Scanner(fonte)).compile();
            Files.writeString(saida, xml, StandardCharsets.UTF_8);
            System.out.println("XML sintatico gerado: " + saida.toAbsolutePath());
        }

        if (modo.equals("--vm") || modo.equals("--all")) {
            Path saida = saidaExplicita != null
                    ? saidaExplicita
                    : referenciaSaida.resolveSibling(base + ".vm");
            String vm = new CompilationEngine(new Scanner(fonte)).compileVm();
            Files.writeString(saida, vm, StandardCharsets.UTF_8);
            System.out.println("Codigo VM gerado: " + saida.toAbsolutePath());
        }
    }

    private static Path resolverSaidaPadrao(Path referencia, String nomeArquivoSaida)
            throws IOException {
        String caminhoNormalizado = referencia.toString().replace('\\', '/');
        if (caminhoNormalizado.contains("src/test/resources")) {
            Path parent = referencia.getParent();
            String subpasta = parent != null ? parent.getFileName().toString() : "default";
            Path dirSaida = Path.of("target", "generated-output", subpasta);
            Files.createDirectories(dirSaida);
            return dirSaida.resolve(nomeArquivoSaida);
        }
        return referencia.resolveSibling(nomeArquivoSaida);
    }
}