package minharefeicao.dominio;

import java.util.Set;
import java.util.List;

import minharefeicao.colecao.CatalogoRefeicoes;

/** Representa um conceito, valor ou estado do caso de uso. */
public record Cardapio(CatalogoRefeicoes catalogo) {
    public List<Refeicao> listarCompativeis(PlanoAssinatura plano, Set<PreferenciaAlimentar> preferencias, CategoriaRefeicao categoria) {
        return catalogo.filtrar(plano, preferencias, categoria);
    }
}
