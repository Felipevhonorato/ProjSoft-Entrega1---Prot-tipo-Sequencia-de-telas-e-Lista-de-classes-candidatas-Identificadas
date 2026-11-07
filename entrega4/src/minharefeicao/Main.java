package minharefeicao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;

public final class Main {
    public static void main(String[] args) throws IOException {
        var saida = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true);
        var entrada = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        var random = new SecureRandom();
        var ctrl = new ControladorAssinatura(Path.of(args.length == 0 ? "dados" : args[0]),
                new GatewaySMS(saida), () -> "%04d".formatted(random.nextInt(10000)));
        try { new TelaAssinatura(entrada, saida, ctrl).executar(); }
        catch (EOFException e) { saida.println(e.getMessage()); }
        catch (UncheckedIOException e) { System.err.println(e.getMessage()); System.exit(1); }
    }
}
