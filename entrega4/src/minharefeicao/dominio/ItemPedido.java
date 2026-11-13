package minharefeicao.dominio;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class ItemPedido {
    final Refeicao refeicao;
    private int quantidade;
    public ItemPedido(Refeicao refeicao, int quantidade) { this.refeicao = refeicao; alterarQuantidade(quantidade); }
    public int obterQuantidade() { return quantidade; }
    public void alterarQuantidade(int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("A quantidade do item deve ser positiva; quantidade zero remove o item do pedido.");
        this.quantidade = quantidade;
    }

    public Refeicao obterRefeicao() { return refeicao; }
}
