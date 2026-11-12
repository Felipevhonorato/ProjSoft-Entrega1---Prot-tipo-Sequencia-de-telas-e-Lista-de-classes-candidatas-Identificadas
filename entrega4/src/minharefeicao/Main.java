package minharefeicao;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import minharefeicao.fronteira.TelaAssinatura;
import minharefeicao.fronteira.GatewaySMS;
import minharefeicao.controle.ControladorAssinatura;

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
