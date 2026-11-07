package com.jackcompiler.codegen;

import com.jackcompiler.Main;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class CompilacaoLoteTest {

    @TempDir
    Path pastaTemporaria;

    private void compilarEVerificarPasta(String nomePasta, List<String> classesEsperadas) throws IOException {
        Path origem = Paths.get("src/test/resources/oficial/entrada/" + nomePasta);
        Path destinoTemp = pastaTemporaria.resolve(nomePasta);
        
        // Copia a pasta de teste para uma diretoria temporária (@TempDir)
        Files.createDirectories(destinoTemp);
        try (Stream<Path> stream = Files.list(origem)) {
            stream.filter(p -> p.toString().endsWith(".jack"))
                  .forEach(p -> {
                      try {
                          Files.copy(p, destinoTemp.resolve(p.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                      } catch (IOException e) {
                          throw new RuntimeException(e);
                      }
                  });
        }

        // Chama o método executar() do Main (main() usa System.exit em caso de erro,
        // o que encerraria a JVM dos testes em vez de reprovar o teste)
        assertDoesNotThrow(() -> Main.executar(new String[]{destinoTemp.toString()}));

        // Verifica se todos os ficheiros .vm esperados foram criados na mesma pasta
        // e se o conteúdo pertence à classe correspondente
        for (String classe : classesEsperadas) {
            Path vmGerado = destinoTemp.resolve(classe + ".vm");
            assertTrue(Files.exists(vmGerado), "O ficheiro " + classe + ".vm não foi gerado em lote.");

            String vm = Files.readString(vmGerado);
            assertTrue(vm.startsWith("function " + classe + "."),
                    "O ficheiro " + classe + ".vm não começa com uma função da classe " + classe);
            assertTrue(vm.contains("return"),
                    "O ficheiro " + classe + ".vm não contém nenhum return");
        }
    }

    @Test
    void testLoteSeven() throws IOException {
        compilarEVerificarPasta("Seven", List.of("Main"));
    }

    @Test
    void testLoteConvertToBin() throws IOException {
        compilarEVerificarPasta("ConvertToBin", List.of("Main"));
    }
    
    @Test
    void testLoteSquare() throws IOException {
        compilarEVerificarPasta("Square", List.of("Main", "Square", "SquareGame"));
    }

    @Test
    void testLoteAverage() throws IOException {
        compilarEVerificarPasta("Average", List.of("Main"));
    }

    @Test
    void testLoteComplexArrays() throws IOException {
        compilarEVerificarPasta("ComplexArrays", List.of("Main"));
    }

    @Test
    void testLotePong() throws IOException {
        compilarEVerificarPasta("Pong", List.of("Main", "PongGame", "Ball", "Bat"));
    }
}