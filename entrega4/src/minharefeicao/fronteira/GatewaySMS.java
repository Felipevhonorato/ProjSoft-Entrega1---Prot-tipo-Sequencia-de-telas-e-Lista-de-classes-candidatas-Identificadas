package minharefeicao.fronteira;

import java.io.PrintWriter;

/** Fronteira do sistema: recebe entradas ou simula a comunicação externa. */
public record GatewaySMS(PrintWriter saida) {
    public void enviar(String celular, String codigo) {
        saida.printf("[SMS simulado] código para final %s: %s%n", celular.substring(celular.length()-4), codigo);
        saida.flush();
    }
}
