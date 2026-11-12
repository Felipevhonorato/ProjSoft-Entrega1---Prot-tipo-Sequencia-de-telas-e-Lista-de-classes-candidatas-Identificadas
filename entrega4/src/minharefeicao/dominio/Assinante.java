package minharefeicao.dominio;

import java.util.Set;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class Assinante {
    final String celular;
    boolean celularValidado;
    Set<PreferenciaAlimentar> preferencias = Set.of();
    public Assinante(String celular) { this.celular = celular; }
    public void confirmarCelular() { celularValidado = true; }
    public void registrarPreferencias(Set<PreferenciaAlimentar> preferencias) {
        if (preferencias.isEmpty()) throw new IllegalArgumentException("Informe uma preferência.");
        this.preferencias = Set.copyOf(preferencias);
    }

    public String obterCelular() { return celular; }
    public boolean obterCelularValidado() { return celularValidado; }
    public Set<PreferenciaAlimentar> obterPreferencias() { return preferencias; }
}
