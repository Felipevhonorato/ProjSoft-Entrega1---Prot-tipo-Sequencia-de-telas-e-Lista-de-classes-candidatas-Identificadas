package minharefeicao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

enum CategoriaRefeicao { PRINCIPAL, ACOMPANHAMENTO, SOBREMESA }
enum StatusAssinatura { EM_MONTAGEM, AGUARDANDO_PAGAMENTO, ATIVA }
enum StatusPedido { EM_MONTAGEM, APROVADO }
enum StatusPagamento { PENDENTE, CONFIRMADO, NAO_AUTORIZADO }
enum ResultadoCodigo { VALIDO, INVALIDO, BLOQUEADO }
enum PreferenciaAlimentar { TRADICIONAL, VEGETARIANA, SEM_LACTOSE }

final class CodigoConfirmacao {
    private final String valor;
    private int erros;
    private boolean encerrado;
    CodigoConfirmacao(String valor) { this.valor = valor; }
    ResultadoCodigo validar(String entrada) {
        if (encerrado) return ResultadoCodigo.BLOQUEADO;
        if (valor.equals(entrada)) { encerrado = true; return ResultadoCodigo.VALIDO; }
        if (++erros == 3) { encerrado = true; return ResultadoCodigo.BLOQUEADO; }
        return ResultadoCodigo.INVALIDO;
    }
    int restantes() { return 3 - erros; }
    void invalidar() { encerrado = true; }
}

final class Assinante {
    final String celular;
    boolean celularValidado;
    Set<PreferenciaAlimentar> preferencias = Set.of();
    Assinante(String celular) { this.celular = celular; }
    void confirmarCelular() { celularValidado = true; }
    void registrarPreferencias(Set<PreferenciaAlimentar> preferencias) {
        if (preferencias.isEmpty()) throw new IllegalArgumentException("Informe uma preferência.");
        this.preferencias = Set.copyOf(preferencias);
    }
}

record PlanoAssinatura(int id, int quantidade, int acompanhamentos, int sobremesas, BigDecimal valor) {
    String periodicidade() { return "SEMANAL"; }
    int limite(CategoriaRefeicao categoria) {
        return switch(categoria) {
            case PRINCIPAL -> quantidade;
            case ACOMPANHAMENTO -> acompanhamentos;
            case SOBREMESA -> sobremesas;
        };
    }
    boolean aceitaQuantidade(CategoriaRefeicao categoria, int quantidade) {
        return quantidade >= 0 && quantidade <= limite(categoria);
    }
    boolean categoriaCompleta(CategoriaRefeicao categoria, int quantidade) {
        return categoria == CategoriaRefeicao.PRINCIPAL ? quantidade == limite(categoria)
                : quantidade > 0 && aceitaQuantidade(categoria, quantidade);
    }
}

record Refeicao(String id, String nome, CategoriaRefeicao categoria,
               Set<Integer> planos, Set<PreferenciaAlimentar> preferencias, boolean disponivel) {
    boolean compativel(PlanoAssinatura plano, Set<PreferenciaAlimentar> filtros, CategoriaRefeicao categoria) {
        return disponivel && planos.contains(plano.id()) && this.categoria == categoria
                && preferencias.containsAll(filtros);
    }
}

final class ItemPedido {
    final Refeicao refeicao;
    private int quantidade;
    ItemPedido(Refeicao refeicao, int quantidade) { this.refeicao = refeicao; alterarQuantidade(quantidade); }
    int obterQuantidade() { return quantidade; }
    void alterarQuantidade(int quantidade) { this.quantidade = quantidade; }
}

final class Pedido {
    private final Map<String, ItemPedido> itens = new LinkedHashMap<>();
    StatusPedido status = StatusPedido.EM_MONTAGEM;
    LocalDate previsao;
    int quantidadeSelecionada(CategoriaRefeicao categoria) {
        return itens.values().stream().filter(i -> i.refeicao.categoria() == categoria)
                .mapToInt(ItemPedido::obterQuantidade).sum();
    }
    int totalProjetado(CategoriaRefeicao categoria, Refeicao refeicao, int quantidade) {
        var anterior = itens.get(refeicao.id());
        return quantidadeSelecionada(categoria) - (anterior == null ? 0 : anterior.obterQuantidade()) + quantidade;
    }
    boolean definirSelecao(Refeicao refeicao, int quantidade, PlanoAssinatura plano) {
        if (status != StatusPedido.EM_MONTAGEM) throw new IllegalStateException("Pedido já aprovado.");
        var categoria = refeicao.categoria();
        if ((quantidade != 0 && quantidade != 1)
                || !plano.aceitaQuantidade(categoria, totalProjetado(categoria, refeicao, quantidade))) return false;
        if (quantidade == 0) itens.remove(refeicao.id());
        else if (itens.containsKey(refeicao.id())) itens.get(refeicao.id()).alterarQuantidade(quantidade);
        else itens.put(refeicao.id(), new ItemPedido(refeicao, quantidade));
        return true;
    }
    List<ItemPedido> obterItens() { return List.copyOf(itens.values()); }
    void definirPrevisao(LocalDate previsao) { this.previsao = previsao; }
    void aprovar() {
        if (previsao == null) throw new IllegalStateException("Defina a previsão.");
        status = StatusPedido.APROVADO;
    }
}

record DadosEndereco(String cep, String logradouro, String numero, String complemento, String bairro, String cidade) {}
record Endereco(DadosEndereco dados) {
    Endereco {
        if (!dados.cep().matches("[0-9]{5}-?[0-9]{3}")
                || List.of(dados.logradouro(), dados.numero(), dados.bairro(), dados.cidade()).stream().anyMatch(String::isBlank))
            throw new IllegalArgumentException("Informe CEP válido, rua, número, bairro e cidade.");
    }
    String descricao() {
        return "%s, %s %s | %s, %s | CEP %s".formatted(dados.logradouro(), dados.numero(),
                dados.complemento(), dados.bairro(), dados.cidade(), dados.cep());
    }
}

// O terminal recebe apenas um identificador fictício; não solicita PAN, validade ou CVV.
record DadosCartao(String identificadorTeste) {}
record CartaoCredito(String ultimosDigitos) {
    CartaoCredito(DadosCartao dados) { this(dados.identificadorTeste()); }
}
record OperadoraCartaoCredito(String nome) {}
record ResultadoAutorizacao(boolean autorizado, String codigoAutorizacao) {}

final class Pagamento {
    final UUID id = UUID.randomUUID();
    final BigDecimal valor;
    final CartaoCredito cartao;
    final OperadoraCartaoCredito operadora;
    StatusPagamento status = StatusPagamento.PENDENTE;
    String codigoAutorizacao = "";
    Pagamento(BigDecimal valor, CartaoCredito cartao, OperadoraCartaoCredito operadora) {
        this.valor = valor; this.cartao = cartao; this.operadora = operadora;
    }
    void registrarResultado(ResultadoAutorizacao resultado) {
        status = resultado.autorizado() ? StatusPagamento.CONFIRMADO : StatusPagamento.NAO_AUTORIZADO;
        codigoAutorizacao = resultado.codigoAutorizacao();
    }
}

final class Assinatura {
    final UUID id = UUID.randomUUID();
    final Assinante assinante;
    final PlanoAssinatura plano;
    Pedido pedido;
    Endereco endereco;
    StatusAssinatura status = StatusAssinatura.EM_MONTAGEM;
    String protocolo = "";
    private final List<Pagamento> pagamentos = new ArrayList<>();
    Assinatura(Assinante assinante, PlanoAssinatura plano) {
        if (!assinante.celularValidado) throw new IllegalStateException("Celular não validado.");
        this.assinante = assinante; this.plano = plano;
    }
    void vincularPedido(Pedido pedido) { this.pedido = pedido; }
    void definirEndereco(Endereco endereco) { this.endereco = endereco; }
    void aguardarPagamento() {
        if (endereco == null || pedido == null || !Arrays.stream(CategoriaRefeicao.values())
                .allMatch(c -> plano.categoriaCompleta(c, pedido.quantidadeSelecionada(c))))
            throw new IllegalStateException("Complete o pedido e o endereço.");
        status = StatusAssinatura.AGUARDANDO_PAGAMENTO;
    }
    BigDecimal obterValor() { return plano.valor(); }
    void registrarTentativa(Pagamento pagamento) {
        if (status != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Assinatura não aguarda pagamento.");
        pagamentos.add(pagamento);
    }
    List<Pagamento> pagamentos() { return List.copyOf(pagamentos); }
    void ativar(String protocolo) {
        if (status != StatusAssinatura.AGUARDANDO_PAGAMENTO || pedido.status != StatusPedido.APROVADO
                || protocolo.isBlank() || pagamentos.stream().filter(p -> p.status == StatusPagamento.CONFIRMADO).count() != 1)
            throw new IllegalStateException("Ativação sem confirmação válida.");
        this.protocolo = protocolo;
        status = StatusAssinatura.ATIVA;
    }
}
