package minharefeicao.dominio;

import java.util.Set;

/** Representa um conceito, valor ou estado do caso de uso. */
public record Refeicao(String id, String nome, CategoriaRefeicao categoria,
               Set<Integer> planos, Set<PreferenciaAlimentar> preferencias, boolean disponivel) {
    public boolean compativel(PlanoAssinatura plano, Set<PreferenciaAlimentar> filtros, CategoriaRefeicao categoria) {
        return disponivel && planos.contains(plano.id()) && this.categoria == categoria
                && preferencias.containsAll(filtros);
    }
}
