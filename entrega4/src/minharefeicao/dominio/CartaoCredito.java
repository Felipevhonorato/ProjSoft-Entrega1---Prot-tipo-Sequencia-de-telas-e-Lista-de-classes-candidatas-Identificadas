package minharefeicao.dominio;

import minharefeicao.dto.DadosCartao;

/** Representa um conceito, valor ou estado do caso de uso. */
public record CartaoCredito(String ultimosDigitos) {
    public CartaoCredito(DadosCartao dados) { this(dados.identificadorTeste()); }
}
