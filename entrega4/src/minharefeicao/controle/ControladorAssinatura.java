package minharefeicao.controle;

import java.nio.file.Path;
import java.util.Set;
import java.util.List;
import java.util.function.Supplier;

import minharefeicao.dominio.Assinante;
import minharefeicao.dominio.PlanoAssinatura;
import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.Refeicao;
import minharefeicao.dominio.Pedido;
import minharefeicao.dominio.Endereco;
import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.CartaoCredito;
import minharefeicao.dominio.Cardapio;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.dominio.CategoriaRefeicao;
import minharefeicao.dominio.StatusAssinatura;
import minharefeicao.dominio.CodigoConfirmacao;
import minharefeicao.dominio.ResultadoCodigo;
import minharefeicao.dto.DadosEndereco;
import minharefeicao.dto.DadosCartao;
import minharefeicao.fronteira.GatewaySMS;
import minharefeicao.fronteira.GatewayOperadora;
import minharefeicao.colecao.CatalogoPlanos;
import minharefeicao.colecao.CatalogoRefeicoes;
import minharefeicao.persistencia.RepositorioAssinantes;
import minharefeicao.persistencia.RepositorioAssinaturas;
import minharefeicao.servico.ServicoConfirmacao;

/** Coordena as etapas da contratação e delega as regras aos objetos responsáveis. */
public final class ControladorAssinatura {
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

    public ControladorAssinatura(Path pasta, GatewaySMS sms, Supplier<String> geradorCodigo) {
        assinantes = new RepositorioAssinantes(pasta);
        assinaturas = new RepositorioAssinaturas(pasta);
        this.sms = sms;
        this.geradorCodigo = geradorCodigo;
    }
    public void solicitarCodigo(String celular) {
        if (assinante != null) throw new IllegalStateException("Celular já confirmado.");
        if (!celular.matches("[0-9]{10,11}")) throw new IllegalArgumentException("Use 10 ou 11 dígitos.");
        if (codigo != null) codigo.invalidar();
        this.celular = celular;
        String novo = geradorCodigo.get();
        codigo = new CodigoConfirmacao(novo);
        sms.enviar(celular, novo);
    }
    public ResultadoCodigo validarCodigo(String entrada) {
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
    public int tentativasRestantes() { return codigo.restantes(); }
    public List<PlanoAssinatura> listarPlanos() {
        if (assinante == null) throw new IllegalStateException("Confirme o celular.");
        return planos.listarDisponiveis();
    }
    public void selecionarPlano(int id) {
        if (assinante == null || assinatura != null) throw new IllegalStateException("Etapa de plano indisponível.");
        assinatura = new Assinatura(assinante, planos.obter(id));
        assinatura.vincularPedido(new Pedido());
        assinaturas.salvar(assinatura);
    }
    public void registrarPreferencias(Set<PreferenciaAlimentar> preferencias) {
        exigirMontagem();
        if (!assinatura.obterPedido().obterItens().isEmpty() || etapaCategoria != 0)
            throw new IllegalStateException("Preferências já aplicadas ao pedido.");
        if (preferencias.size() != 1) throw new IllegalArgumentException("Escolha uma preferência.");
        assinante.registrarPreferencias(preferencias);
        preferenciasConfirmadas = true;
        assinantes.salvar(assinante);
        assinaturas.salvar(assinatura);
    }
    public CategoriaRefeicao categoriaAtual() {
        exigirMontagem();
        if (!preferenciasConfirmadas) throw new IllegalStateException("Confirme as preferências desta contratação.");
        if (etapaCategoria >= CategoriaRefeicao.values().length) throw new IllegalStateException("Seleção concluída.");
        return CategoriaRefeicao.values()[etapaCategoria];
    }
    public List<Refeicao> listarRefeicoes() {
        if (assinante == null || assinante.obterPreferencias().isEmpty())
            throw new IllegalStateException("Informe as preferências.");
        return cardapio.listarCompativeis(assinatura.obterPlano(), assinante.obterPreferencias(), categoriaAtual());
    }
    public boolean selecionarRefeicao(String id, int quantidade) {
        var categoria = categoriaAtual();
        var refeicao = refeicoes.obter(id);
        if (!refeicao.compativel(assinatura.obterPlano(), assinante.obterPreferencias(), categoria))
            throw new IllegalArgumentException("Item fora do cardápio desta etapa.");
        if (!assinatura.obterPedido().definirSelecao(refeicao, quantidade, assinatura.obterPlano())) return false;
        assinaturas.salvar(assinatura);
        return true;
    }
    public int limiteAtual() { return assinatura.obterPlano().limite(categoriaAtual()); }
    public int quantidadeAtual() { return assinatura.obterPedido().quantidadeSelecionada(categoriaAtual()); }
    public boolean continuarCategoria() {
        if (!assinatura.obterPlano().categoriaCompleta(categoriaAtual(), quantidadeAtual())) return false;
        etapaCategoria++;
        return true;
    }
    public void confirmarResumo() {
        exigirMontagem();
        if (etapaCategoria != CategoriaRefeicao.values().length)
            throw new IllegalStateException("Termine a seleção antes do resumo.");
        resumoConfirmado = true;
    }
    public void definirEndereco(DadosEndereco dados) {
        exigirMontagem();
        if (!resumoConfirmado) throw new IllegalStateException("Confirme o resumo.");
        assinatura.definirEndereco(new Endereco(dados));
        assinatura.aguardarPagamento();
        assinaturas.salvar(assinatura);
    }
    public boolean processarPagamento(DadosCartao dados) {
        if (encerrado || assinatura == null || assinatura.obterStatus() != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Pagamento indisponível.");
        if (!Set.of("1111", "0000").contains(dados.identificadorTeste()))
            throw new IllegalArgumentException("Use apenas os identificadores de teste.");
        var pagamento = new Pagamento(assinatura.obterValor(), new CartaoCredito(dados), operadora.obterOperadora());
        assinatura.registrarTentativa(pagamento);
        var resultado = operadora.autorizar(pagamento, dados);
        pagamento.registrarResultado(resultado);
        if (resultado.autorizado()) {
            var protocolo = confirmacao.gerarProtocolo();
            assinatura.obterPedido().definirPrevisao(confirmacao.preverPrimeiraEntrega(assinatura.obterEndereco(), assinatura.obterPlano().periodicidade()));
            assinatura.obterPedido().aprovar();
            assinatura.ativar(protocolo);
        }
        assinaturas.salvar(assinatura);
        return resultado.autorizado();
    }
    public void encerrarSemAtivacao() {
        if (assinatura == null || assinatura.obterStatus() != StatusAssinatura.AGUARDANDO_PAGAMENTO)
            throw new IllegalStateException("Não há pagamento pendente.");
        encerrado = true;
    }
    public Assinatura resumo() { return assinatura; }
    private void exigirMontagem() {
        if (assinatura == null || assinatura.obterStatus() != StatusAssinatura.EM_MONTAGEM)
            throw new IllegalStateException("A assinatura não está em montagem.");
    }
}
