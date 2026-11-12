package minharefeicao;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

final class ControladorAssinatura {
    private final CatalogoPlanos planos = new CatalogoPlanos();
    private final CatalogoRefeicoes refeicoes = new CatalogoRefeicoes();
    private final Cardapio cardapio = new Cardapio(refeicoes);
    private final RepositorioAssinantes assinantes;
    private final RepositorioAssinaturas assinaturas;
    private final GatewaySMS sms;
    private final GatewayOperadora operadora = new GatewayOperadora();
    private final ServicoConfirmacao confirmacao = new ServicoConfirmacao();
    private final Supplier<String> geradorCodigo;
    private String celular;
    private CodigoConfirmacao codigo;
    private Assinante assinante;
    private Assinatura assinatura;
    private int etapaCategoria;
    private boolean preferenciasConfirmadas;
    private boolean resumoConfirmado;
    private boolean encerrado;

    ControladorAssinatura(Path pasta, GatewaySMS sms, Supplier<String> geradorCodigo) {
        assinantes = new RepositorioAssinantes(pasta);
        assinaturas = new RepositorioAssinaturas(pasta);
        this.sms = sms;
        this.geradorCodigo = geradorCodigo;
    }
    void solicitarCodigo(String celular) {
        if (assinante != null) throw new IllegalStateException("Celular já confirmado.");
        if (!celular.matches("[0-9]{10,11}")) throw new IllegalArgumentException("Use 10 ou 11 dígitos.");
        if (codigo != null) codigo.invalidar();
        this.celular = celular;
        String novo = geradorCodigo.get();
        codigo = new CodigoConfirmacao(novo);
        sms.enviar(celular, novo);
    }
    ResultadoCodigo validarCodigo(String entrada) {
        if (codigo == null) throw new IllegalStateException("Solicite um código.");
        if (assinante != null) throw new IllegalStateException("Celular já confirmado.");
        var resultado = codigo.validar(entrada);
        if (resultado == ResultadoCodigo.VALIDO) {
            assinante = assinantes.obterOuCadastrar(celular);
            assinante.confirmarCelular();
            assinantes.salvar(assinante);
        }
        return resultado;
    }
    int tentativasRestantes() { return codigo.restantes(); }
    List<PlanoAssinatura> listarPlanos() {
        if (assinante == null) throw new IllegalStateException("Confirme o celular.");
        return planos.listarDisponiveis();
    }
    void selecionarPlano(int id) {
        if (assinante == null || assinatura != null) throw new IllegalStateException("Etapa de plano indisponível.");
        assinatura = new Assinatura(assinante, planos.obter(id));
        assinatura.vincularPedido(new Pedido());
        assinaturas.salvar(assinatura);
    }
    void registrarPreferencias(Set<PreferenciaAlimentar> preferencias) {
        exigirMontagem();
        if (!assinatura.pedido.obterItens().isEmpty() || etapaCategoria != 0)
            throw new IllegalStateException("Preferências já aplicadas ao pedido.");
        if (preferencias.size() != 1) throw new IllegalArgumentException("Escolha uma preferência.");
        assinante.registrarPreferencias(preferencias);
        preferenciasConfirmadas = true;
        assinantes.salvar(assinante);
        assinaturas.salvar(assinatura);
    }
    CategoriaRefeicao categoriaAtual() {
        exigirMontagem();
        if (!preferenciasConfirmadas) throw new IllegalStateException("Confirme as preferências desta contratação.");
        if (etapaCategoria >= CategoriaRefeicao.values().length) throw new IllegalStateException("Seleção concluída.");
        return CategoriaRefeicao.values()[etapaCategoria];
    }
    List<Refeicao> listarRefeicoes() {
        if (assinante == null || assinante.preferencias.isEmpty())
            throw new IllegalStateException("Informe as preferências.");
        return cardapio.listarCompativeis(assinatura.plano, assinante.preferencias, categoriaAtual());
    }
    boolean selecionarRefeicao(String id, int quantidade) {
        var categoria = categoriaAtual();
        var refeicao = refeicoes.obter(id);
        if (!refeicao.compativel(assinatura.plano, assinante.preferencias, categoria))
            throw new IllegalArgumentException("Item fora do cardápio desta etapa.");
        if (!assinatura.pedido.definirSelecao(refeicao, quantidade, assinatura.plano)) return false;
        assinaturas.salvar(assinatura);
        return true;
    }
    int limiteAtual() { return assinatura.plano.limite(categoriaAtual()); }
    int quantidadeAtual() { return assinatura.pedido.quantidadeSelecionada(categoriaAtual()); }
    boolean continuarCategoria() {
        if (!assinatura.plano.categoriaCompleta(categoriaAtual(), quantidadeAtual())) return false;
        etapaCategoria++;
        return true;
    }
    void confirmarResumo() {
        exigirMontagem();
        if (etapaCategoria != CategoriaRefeicao.values().length)
            throw new IllegalStateException("Termine a seleção antes do resumo.");
        resumoConfirmado = true;
    }
    void definirEndereco(DadosEndereco dados) {
        exigirMontagem();
        if (!resumoConfirmado) throw new IllegalStateException("Confirme o resumo.");
        assinatura.definirEndereco(new Endereco(dados));
        assinatura.aguardarPagamento();
        assinaturas.salvar(assinatura);
    }
    boolean processarPagamento(DadosCartao dados) {
        if (encerrado || assinatura == null || assinatura.status != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Pagamento indisponível.");
        if (!Set.of("1111", "0000").contains(dados.identificadorTeste()))
            throw new IllegalArgumentException("Use apenas os identificadores de teste.");
        var pagamento = new Pagamento(assinatura.obterValor(), new CartaoCredito(dados), operadora.operadora);
        assinatura.registrarTentativa(pagamento);
        var resultado = operadora.autorizar(pagamento, dados);
        pagamento.registrarResultado(resultado);
        if (resultado.autorizado()) {
            var protocolo = confirmacao.gerarProtocolo();
            assinatura.pedido.definirPrevisao(confirmacao.preverPrimeiraEntrega(assinatura.endereco, assinatura.plano.periodicidade()));
            assinatura.pedido.aprovar();
            assinatura.ativar(protocolo);
        }
        assinaturas.salvar(assinatura);
        return resultado.autorizado();
    }
    void encerrarSemAtivacao() {
        if (assinatura == null || assinatura.status != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Não há pagamento pendente.");
        encerrado = true;
    }
    Assinatura resumo() { return assinatura; }
    private void exigirMontagem() {
        if (assinatura == null || assinatura.status != StatusAssinatura.EM_MONTAGEM)
            throw new IllegalStateException("A assinatura não está em montagem.");
    }
}
