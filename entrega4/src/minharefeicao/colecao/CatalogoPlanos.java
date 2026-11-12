package minharefeicao.colecao;

import java.math.BigDecimal;
import java.util.List;

import minharefeicao.dominio.PlanoAssinatura;

/** Disponibiliza e consulta as opções do catálogo. */
public final class CatalogoPlanos {
    private final List<PlanoAssinatura> planos = List.of(
            new PlanoAssinatura(1, 6, 4, 2, new BigDecimal("119.40")),
            new PlanoAssinatura(2, 12, 4, 2, new BigDecimal("214.80")),
            new PlanoAssinatura(3, 20, 4, 2, new BigDecimal("318.00")));
    public List<PlanoAssinatura> listarDisponiveis() { return planos; }
    public PlanoAssinatura obter(int id) {
        return planos.stream().filter(p -> p.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plano inválido."));
    }
}
