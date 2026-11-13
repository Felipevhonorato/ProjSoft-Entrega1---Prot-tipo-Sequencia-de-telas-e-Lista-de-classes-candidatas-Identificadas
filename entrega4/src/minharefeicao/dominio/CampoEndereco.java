package minharefeicao.dominio;

/** Identifica os campos usados pela validação de Endereco. */
public enum CampoEndereco {
    CEP("CEP"), LOGRADOURO("Rua"), NUMERO("Número"),
    COMPLEMENTO("Complemento opcional"), BAIRRO("Bairro"), CIDADE("Cidade");

    private final String rotulo;
    CampoEndereco(String rotulo) { this.rotulo = rotulo; }
    public String rotulo() { return rotulo; }
}
