package minharefeicao.dominio;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class Pedido {
    final UUID id = UUID.randomUUID();
    private final Map<String, ItemPedido> itens = new LinkedHashMap<>();
    StatusPedido status = StatusPedido.EM_MONTAGEM;
    LocalDate previsao;
    public int quantidadeSelecionada(CategoriaRefeicao categoria) {
        return itens.values().stream().filter(i -> i.refeicao.categoria() == categoria)
                .mapToInt(ItemPedido::obterQuantidade).sum();
    }
    public int totalProjetado(CategoriaRefeicao categoria, Refeicao refeicao, int quantidade) {
        var anterior = itens.get(refeicao.id());
        return quantidadeSelecionada(categoria) - (anterior == null ? 0 : anterior.obterQuantidade()) + quantidade;
    }
    public boolean definirSelecao(Refeicao refeicao, int quantidade, PlanoAssinatura plano) {
        if (status != StatusPedido.EM_MONTAGEM) throw new IllegalStateException("Pedido já aprovado.");
        var categoria = refeicao.categoria();
        if (quantidade < 0 || quantidade > plano.limite(categoria)
                || !plano.aceitaQuantidade(categoria, totalProjetado(categoria, refeicao, quantidade))) return false;
        if (quantidade == 0) itens.remove(refeicao.id());
        else if (itens.containsKey(refeicao.id())) itens.get(refeicao.id()).alterarQuantidade(quantidade);
        else itens.put(refeicao.id(), new ItemPedido(refeicao, quantidade));
        return true;
    }
    public List<ItemPedido> obterItens() { return List.copyOf(itens.values()); }
    public int quantidadeDaRefeicao(Refeicao refeicao) {
        var item = itens.get(refeicao.id());
        return item == null ? 0 : item.obterQuantidade();
    }
    public void definirPrevisao(LocalDate previsao) { this.previsao = previsao; }
    public void aprovar() {
        if (previsao == null) throw new IllegalStateException("Defina a previsão.");
        status = StatusPedido.APROVADO;
    }

    public UUID obterId() { return id; }
    public StatusPedido obterStatus() { return status; }
    public LocalDate obterPrevisao() { return previsao; }
}
