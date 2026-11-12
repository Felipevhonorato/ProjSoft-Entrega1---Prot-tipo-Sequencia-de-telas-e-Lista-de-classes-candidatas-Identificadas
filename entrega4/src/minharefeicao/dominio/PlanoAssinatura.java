package minharefeicao.dominio;

import java.math.BigDecimal;

/** Representa um conceito, valor ou estado do caso de uso. */
public record PlanoAssinatura(int id, int quantidade, int acompanhamentos, int sobremesas, BigDecimal valor) {
    public String periodicidade() { return "SEMANAL"; }
    public int limite(CategoriaRefeicao categoria) {
        return switch(categoria) {
            case PRINCIPAL -> quantidade;
            case ACOMPANHAMENTO -> acompanhamentos;
            case SOBREMESA -> sobremesas;
        };
    }
    public boolean aceitaQuantidade(CategoriaRefeicao categoria, int quantidade) {
        return quantidade >= 0 && quantidade <= limite(categoria);
    }
    public boolean categoriaCompleta(CategoriaRefeicao categoria, int quantidade) {
        return categoria == CategoriaRefeicao.PRINCIPAL ? quantidade == limite(categoria)
                : quantidade > 0 && aceitaQuantidade(categoria, quantidade);
    }
}
