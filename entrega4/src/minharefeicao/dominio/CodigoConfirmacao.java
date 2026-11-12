package minharefeicao.dominio;

/** Representa um conceito, valor ou estado do caso de uso. */
public final class CodigoConfirmacao {
    private final String valor;
    private int erros;
    private boolean encerrado;
    public CodigoConfirmacao(String valor) { this.valor = valor; }
    public ResultadoCodigo validar(String entrada) {
        if (encerrado) return ResultadoCodigo.BLOQUEADO;
        if (valor.equals(entrada)) { encerrado = true; return ResultadoCodigo.VALIDO; }
        if (++erros == 3) { encerrado = true; return ResultadoCodigo.BLOQUEADO; }
        return ResultadoCodigo.INVALIDO;
    }
    public int restantes() { return 3 - erros; }
    public void invalidar() { encerrado = true; }
}
