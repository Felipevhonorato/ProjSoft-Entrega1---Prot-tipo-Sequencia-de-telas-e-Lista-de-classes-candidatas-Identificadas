package minharefeicao.dominio;

import java.util.Objects;

import minharefeicao.dto.DadosEndereco;

/** Representa um conceito, valor ou estado do caso de uso. */
public record Endereco(DadosEndereco dados) {
    public Endereco {
        if (dados == null) throw new IllegalArgumentException("Informe os dados do endereço.");
        validarCampo(CampoEndereco.CEP, dados.cep());
        validarCampo(CampoEndereco.LOGRADOURO, dados.logradouro());
        validarCampo(CampoEndereco.NUMERO, dados.numero());
        validarCampo(CampoEndereco.COMPLEMENTO, dados.complemento());
        validarCampo(CampoEndereco.BAIRRO, dados.bairro());
        validarCampo(CampoEndereco.CIDADE, dados.cidade());
    }
    public static void validarCampo(CampoEndereco campo, String valor) {
        Objects.requireNonNull(campo, "Campo de endereço obrigatório.");
        if (valor == null) throw new IllegalArgumentException(campo.rotulo() + " não informado.");
        String texto = valor.strip();
        boolean valido = switch (campo) {
            case CEP -> texto.matches("[0-9]{5}-?[0-9]{3}");
            case NUMERO -> texto.matches("(?iu)([0-9]+([ /-]?[\\p{L}0-9]+)?|s/?n)");
            case LOGRADOURO, BAIRRO, CIDADE -> texto.matches("(?s).*\\p{L}.*");
            case COMPLEMENTO -> true;
        };
        if (!valido) {
            String regra = switch (campo) {
                case CEP -> "use oito dígitos, com ou sem hífen (ex.: 01310-100).";
                case NUMERO -> "informe um número, como 100 ou 100-A, ou S/N.";
                case LOGRADOURO, BAIRRO, CIDADE -> "preencha com um nome que contenha letras.";
                case COMPLEMENTO -> "campo opcional.";
            };
            throw new IllegalArgumentException(campo.rotulo() + " inválido: " + regra);
        }
    }
    public String descricao() {
        return "%s, %s %s | %s, %s | CEP %s".formatted(dados.logradouro(), dados.numero(),
                dados.complemento(), dados.bairro(), dados.cidade(), dados.cep());
    }
}

