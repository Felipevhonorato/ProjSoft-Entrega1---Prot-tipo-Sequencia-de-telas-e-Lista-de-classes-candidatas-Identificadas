package minharefeicao.fronteira;

import java.util.Set;

import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.OperadoraCartaoCredito;
import minharefeicao.dto.DadosCartao;
import minharefeicao.dto.ResultadoAutorizacao;

/** Fronteira do sistema: recebe entradas ou simula a comunicação externa. */
public final class GatewayOperadora {
    final OperadoraCartaoCredito operadora = new OperadoraCartaoCredito("Operadora simulada");
    public ResultadoAutorizacao autorizar(Pagamento pagamento, DadosCartao dados) {
        if (!Set.of("1111","0000").contains(dados.identificadorTeste()))
            throw new IllegalArgumentException("Use 1111 (aprova) ou 0000 (recusa).");
        boolean autorizado = dados.identificadorTeste().equals("1111");
        return new ResultadoAutorizacao(autorizado, autorizado ? "AUTH-" + pagamento.obterId() : "");
    }

    public OperadoraCartaoCredito obterOperadora() { return operadora; }
}
