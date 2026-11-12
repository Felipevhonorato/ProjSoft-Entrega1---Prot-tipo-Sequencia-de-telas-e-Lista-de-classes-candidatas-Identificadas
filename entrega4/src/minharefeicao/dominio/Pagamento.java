package minharefeicao.dominio;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.UUID;

import minharefeicao.dto.ResultadoAutorizacao;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class Pagamento {
    final UUID id = UUID.randomUUID();
    final java.time.OffsetDateTime dataHora = java.time.OffsetDateTime.now(java.time.ZoneId.of("America/Sao_Paulo"));
    final BigDecimal valor;
    final CartaoCredito cartao;
    final OperadoraCartaoCredito operadora;
    StatusPagamento status = StatusPagamento.PENDENTE;
    String codigoAutorizacao = "";
    public Pagamento(BigDecimal valor, CartaoCredito cartao, OperadoraCartaoCredito operadora) {
        this.valor = valor; this.cartao = cartao; this.operadora = operadora;
    }
    public void registrarResultado(ResultadoAutorizacao resultado) {
        status = resultado.autorizado() ? StatusPagamento.CONFIRMADO : StatusPagamento.NAO_AUTORIZADO;
        codigoAutorizacao = resultado.codigoAutorizacao();
    }

    public UUID obterId() { return id; }
    public java.time.OffsetDateTime obterDataHora() { return dataHora; }
    public BigDecimal obterValor() { return valor; }
    public CartaoCredito obterCartao() { return cartao; }
    public OperadoraCartaoCredito obterOperadora() { return operadora; }
    public StatusPagamento obterStatus() { return status; }
    public String obterCodigoAutorizacao() { return codigoAutorizacao; }
}
