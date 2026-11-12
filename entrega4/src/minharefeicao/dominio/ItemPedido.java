package minharefeicao.dominio;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class ItemPedido {
    final Refeicao refeicao;
    private int quantidade;
    public ItemPedido(Refeicao refeicao, int quantidade) { this.refeicao = refeicao; alterarQuantidade(quantidade); }
    public int obterQuantidade() { return quantidade; }
    public void alterarQuantidade(int quantidade) {
        if (quantidade != 1) throw new IllegalArgumentException("Item selecionado deve ter quantidade 1; desmarcar remove o item do pedido.");
        this.quantidade = quantidade;
    }

    public Refeicao obterRefeicao() { return refeicao; }
}
