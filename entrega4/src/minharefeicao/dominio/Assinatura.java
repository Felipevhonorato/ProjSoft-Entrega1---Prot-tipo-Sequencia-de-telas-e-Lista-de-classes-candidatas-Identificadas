package minharefeicao.dominio;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class Assinatura {
    final UUID id = UUID.randomUUID();
    final Assinante assinante;
    final PlanoAssinatura plano;
    Pedido pedido;
    Endereco endereco;
    StatusAssinatura status = StatusAssinatura.EM_MONTAGEM;
    String protocolo = "";
    private final List<Pagamento> pagamentos = new ArrayList<>();
    public Assinatura(Assinante assinante, PlanoAssinatura plano) {
        if (!assinante.celularValidado) throw new IllegalStateException("Celular não validado.");
        this.assinante = assinante; this.plano = plano;
    }
    public void vincularPedido(Pedido pedido) { this.pedido = pedido; }
    public void definirEndereco(Endereco endereco) { this.endereco = endereco; }
    public void aguardarPagamento() {
        if (endereco == null || pedido == null || !Arrays.stream(CategoriaRefeicao.values())
                .allMatch(c -> plano.categoriaCompleta(c, pedido.quantidadeSelecionada(c))))
            throw new IllegalStateException("Complete o pedido e o endereço.");
        status = StatusAssinatura.AGUARDANDO_PAGAMENTO;
    }
    public BigDecimal obterValor() { return plano.valor(); }
    public void registrarTentativa(Pagamento pagamento) {
        if (status != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Assinatura não aguarda pagamento.");
        pagamentos.add(pagamento);
    }
    public List<Pagamento> pagamentos() { return List.copyOf(pagamentos); }
    public void ativar(String protocolo) {
        if (status != StatusAssinatura.AGUARDANDO_PAGAMENTO || pedido.status != StatusPedido.APROVADO
                || protocolo.isBlank() || pagamentos.stream().filter(p -> p.status == StatusPagamento.CONFIRMADO).count() != 1)
            throw new IllegalStateException("Ativação sem confirmação válida.");
        this.protocolo = protocolo;
        status = StatusAssinatura.ATIVA;
    }

    public UUID obterId() { return id; }
    public Assinante obterAssinante() { return assinante; }
    public PlanoAssinatura obterPlano() { return plano; }
    public Pedido obterPedido() { return pedido; }
    public Endereco obterEndereco() { return endereco; }
    public StatusAssinatura obterStatus() { return status; }
    public String obterProtocolo() { return protocolo; }
}
