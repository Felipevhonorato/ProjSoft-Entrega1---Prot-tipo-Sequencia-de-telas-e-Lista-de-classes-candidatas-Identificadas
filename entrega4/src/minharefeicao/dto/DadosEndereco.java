package minharefeicao.dto;

/** Transporta dados entre a fronteira, o controlador e os objetos do cenário. */
public record DadosEndereco(String cep, String logradouro, String numero, String complemento, String bairro, String cidade) {}
