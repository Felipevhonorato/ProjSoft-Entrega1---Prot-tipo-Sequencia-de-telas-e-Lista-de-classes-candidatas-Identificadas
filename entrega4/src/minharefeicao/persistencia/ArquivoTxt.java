package minharefeicao.persistencia;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Lê ou grava os registros TXT da demonstração. */
public final class ArquivoTxt {
    public static Properties ler(Path arquivo) {
        Properties dados = new Properties();
        if (!Files.exists(arquivo)) return dados;
        try (var reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            dados.load(reader);
            return dados;
        } catch(IOException e) { throw new UncheckedIOException(e); }
    }
    public static void salvar(Path arquivo, Properties dados) {
        Path temporario = null;
        try {
            Files.createDirectories(arquivo.toAbsolutePath().getParent());
            temporario = Files.createTempFile(arquivo.toAbsolutePath().getParent(), "gravacao-", ".tmp");
            try(var writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                dados.store(writer, "Minha Refeição - dados da demonstração");
            }
            try { Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException("Não foi possível gravar " + arquivo, e); }
        finally {
            if (temporario != null) try { Files.deleteIfExists(temporario); } catch (IOException ignored) {}
        }
    }
}
