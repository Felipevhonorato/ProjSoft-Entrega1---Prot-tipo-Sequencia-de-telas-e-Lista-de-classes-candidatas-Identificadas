package minharefeicao.dominio;

import java.util.List;

import minharefeicao.dto.DadosEndereco;

/** Representa um conceito, valor ou estado do caso de uso. */
public record Endereco(DadosEndereco dados) {
    public Endereco {
        if (!dados.cep().matches("[0-9]{5}-?[0-9]{3}")
                || List.of(dados.logradouro(), dados.numero(), dados.bairro(), dados.cidade()).stream().anyMatch(String::isBlank))
            throw new IllegalArgumentException("Informe CEP válido, rua, número, bairro e cidade.");
    }
    public String descricao() {
        return "%s, %s %s | %s, %s | CEP %s".formatted(dados.logradouro(), dados.numero(),
                dados.complemento(), dados.bairro(), dados.cidade(), dados.cep());
    }
}

// O terminal recebe apenas um identificador fictício; não solicita PAN, validade ou CVV.
