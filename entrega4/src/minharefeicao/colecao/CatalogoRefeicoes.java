package minharefeicao.colecao;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import minharefeicao.dominio.PlanoAssinatura;
import minharefeicao.dominio.Refeicao;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.dominio.CategoriaRefeicao;

/** Disponibiliza e consulta as opções do catálogo. */
public final class CatalogoRefeicoes {
    private final List<Refeicao> refeicoes = new ArrayList<>();
    public CatalogoRefeicoes() {
        for (var preferencia : PreferenciaAlimentar.values()) {
            for (var categoria : CategoriaRefeicao.values()) {
                int total = switch (categoria) { case PRINCIPAL -> 21; case ACOMPANHAMENTO -> 5; case SOBREMESA -> 3; };
                String nome = switch (categoria) {
                    case PRINCIPAL -> preferencia == PreferenciaAlimentar.VEGETARIANA ? "Prato vegetal" : "Prato principal";
                    case ACOMPANHAMENTO -> "Acompanhamento";
                    case SOBREMESA -> "Sobremesa";
                };
                String prefixo = preferencia.name().substring(0,1) + categoria.name().substring(0,1);
                for (int i = 1; i <= total; i++)
                    refeicoes.add(new Refeicao(prefixo + i, nome + " " + i, categoria,
                            Set.of(1, 2, 3), Set.of(preferencia), true));
            }
        }
    }
    public List<Refeicao> filtrar(PlanoAssinatura plano, Set<PreferenciaAlimentar> preferencias, CategoriaRefeicao categoria) {
        return refeicoes.stream().filter(r -> r.compativel(plano, preferencias, categoria)).toList();
    }
    public Refeicao obter(String id) {
        return refeicoes.stream().filter(r -> r.id().equalsIgnoreCase(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refeição não encontrada."));
    }
}
